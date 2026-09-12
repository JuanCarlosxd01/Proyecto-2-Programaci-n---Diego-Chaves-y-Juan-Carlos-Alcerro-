
package insta.servicio;


import estructuras.ListaEnlazada;
import excepciones.*;
import insta.modelo.*;
import persistencia.*;
import red.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class InstaServicio implements AutoCloseable {

    final RepositorioInsta repo;

    final GestorPublicacionesBinario publicaciones;
    final GestorSeguidoresBinario seguidores;
    final GestorSeguidoresBinario seguidos;
    final GestorInboxBinario inbox;
    final GestorStickersBinario stickers;

    final PublicacionServicio publicacionServicio;
    final SeguimientoServicio seguimientoServicio;
    final PerfilServicio perfilServicio;
    final BusquedaServicio busquedaServicio;
    final InboxServicio inboxServicio;
    final StickerServicio stickerServicio;

    public InstaServicio()
            throws IOException, ArchivoCorruptoException {
        this("INSTA_RAIZ");
    }

    public InstaServicio(String raiz)
            throws IOException, ArchivoCorruptoException {

        repo = new RepositorioInsta(raiz);

        publicaciones = new GestorPublicacionesBinario(repo);
        seguidores = new GestorSeguidoresBinario(repo, true);
        seguidos = new GestorSeguidoresBinario(repo, false);
        inbox = new GestorInboxBinario(repo);
        stickers = new GestorStickersBinario(repo);

        publicacionServicio = new PublicacionServicio(this);
        seguimientoServicio = new SeguimientoServicio(this);
        perfilServicio = new PerfilServicio(this);
        busquedaServicio = new BusquedaServicio(this);
        inboxServicio = new InboxServicio(this);
        stickerServicio = new StickerServicio(this);

        try {
            if (!repo.existe("users.ins")) {
                try (DirectoryStream<Path> contenido =
                             Files.newDirectoryStream(repo.ruta(""))) {

                    for (Path path : contenido) {
                        if (!path.getFileName().toString()
                                .equals("servidor.lock")) {
                            throw new ArchivoCorruptoException(
                                    "Falta users.ins en un almacenamiento con datos."
                            );
                        }
                    }
                }

                repo.guardar(new RepositorioInsta.Cambio(
                        "users.ins",
                        new ListaEnlazada<UsuarioInsta>()
                ));
            }

            usuarios();
            stickerServicio.inicializar();
            ejemplos();

        } catch (Exception e) {
            repo.close();

            if (e instanceof ArchivoCorruptoException corrupto) {
                throw corrupto;
            }

            if (e instanceof IOException io) {
                throw io;
            }

            throw new IOException(
                    "No se pudo inicializar Instagram.",
                    e
            );
        }
    }

    ListaEnlazada<UsuarioInsta> usuarios()
            throws IOException, ArchivoCorruptoException {

        ListaEnlazada<UsuarioInsta> lista =
                repo.leer("users.ins", UsuarioInsta.class);

        Set<String> nombres = new HashSet<>();

        for (UsuarioInsta usuario : lista) {
            if (usuario.getUsername() == null
                    || !usuario.getUsername()
                            .matches("[a-z][a-z0-9_]{2,23}")
                    || !nombres.add(usuario.getUsername())) {
                throw new ArchivoCorruptoException(
                        "Usuarios inválidos o duplicados."
                );
            }
        }

        return lista;
    }

    static String normalizar(String valor) {
        return Objects.requireNonNull(
                valor,
                "Falta el username."
        ).strip().toLowerCase(Locale.ROOT);
    }

    UsuarioInsta usuario(String nombre)
            throws IOException, ArchivoCorruptoException {

        UsuarioInsta usuario = buscarUsuario(nombre);

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "La cuenta no existe."
            );
        }

        return usuario;
    }

    boolean visible(String nombre)
            throws IOException, ArchivoCorruptoException {

        UsuarioInsta usuario = buscarUsuario(nombre);

        return usuario != null && usuario.estaActiva();
    }

    void guardarUsuario(
            UsuarioInsta actualizado,
            RepositorioInsta.Cambio... extras
    ) throws IOException, ArchivoCorruptoException {

        ListaEnlazada<UsuarioInsta> lista = usuarios();

        lista.eliminarSi(
                usuario -> usuario.getUsername()
                        .equals(actualizado.getUsername())
        );

        lista.agregar(actualizado);

        if (lista.size() > 100000) {
            throw new IllegalArgumentException(
                    "Límite de cuentas alcanzado."
            );
        }

        RepositorioInsta.Cambio[] lote =
                Arrays.copyOf(extras, extras.length + 1);

        lote[extras.length] = new RepositorioInsta.Cambio(
                "users.ins",
                lista
        );

        repo.guardar(lote);
    }

    static void validarDatos(String nombre, char genero, int edad, String password) {
        if (nombre == null || nombre.isBlank() || nombre.length() > 100) {
            throw new IllegalArgumentException("El nombre debe tener entre 1 y 100 caracteres.");
        }

        if (genero != 'M' && genero != 'F') {
            throw new IllegalArgumentException("El género debe ser M o F.");
        }

        if (edad < 1 || edad > 120) {
            throw new IllegalArgumentException("Edad inválida.");
        }

        if (password != null) {
            validarPassword(password);
        }
    }
    
    private static void validarPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }

        if (password.length() < 8 || password.length() > 128) {
            throw new IllegalArgumentException("La contraseña debe tener entre 8 y 128 caracteres.");
        }

        boolean tieneMayuscula = false;
        boolean tieneNumero = false;
        boolean tieneSimbolo = false;

        for (char caracter : password.toCharArray()) {
            if (Character.isUpperCase(caracter)) {
                tieneMayuscula = true;
            } else if (Character.isDigit(caracter)) {
                tieneNumero = true;
            } else if (!Character.isLetterOrDigit(caracter)) {
                tieneSimbolo = true;
            }
        }

        if (!tieneMayuscula) {
            throw new IllegalArgumentException("La contraseña debe contener al menos una letra mayúscula.");
        }

        if (!tieneNumero) {
            throw new IllegalArgumentException("La contraseña debe contener al menos un número.");
        }

        if (!tieneSimbolo) {
            throw new IllegalArgumentException("La contraseña debe contener al menos un símbolo.");
        }
    }

    public synchronized UsuarioInsta registrarUsuario(
            String nombre,
            char genero,
            String username,
            String password,
            int edad
    ) throws IOException,
            ArchivoCorruptoException,
            UsernameDuplicadoException {

        return registrarUsuario(
                nombre,
                genero,
                username,
                password,
                edad,
                null
        );
    }

    public synchronized UsuarioInsta registrarUsuario(
            String nombre,
            char genero,
            String username,
            String password,
            int edad,
            byte[] foto
    ) throws IOException,
            ArchivoCorruptoException,
            UsernameDuplicadoException {

        String id = normalizar(username);
        genero = Character.toUpperCase(genero);

        Objects.requireNonNull(password, "Falta la contraseña.");
        validarDatos(nombre, genero, edad, password);

        if (!id.matches("[a-z][a-z0-9_]{2,23}")
                || id.matches(
                        "con|prn|aux|nul|com[1-9]|lpt[1-9]|stickers_globales"
                )) {
            throw new IllegalArgumentException(
                    "Username inválido o reservado."
            );
        }

        if (buscarUsuario(id) != null) {
            throw new UsernameDuplicadoException();
        }

        if (repo.existe(id)) {
            throw new IOException(
                    "Ya existe una carpeta sin cuenta con ese nombre."
            );
        }

        var credenciales =
                SeguridadPassword.generarCredenciales(password);

        String rutaFoto = id
                + "/imagenes/perfil-"
                + UUID.randomUUID()
                + ".png";

        byte[] imagen = foto == null
                ? ImagenesInsta.dibujar(
                        id.substring(0, 1).toUpperCase(Locale.ROOT),
                        0x425b76
                )
                : ImagenesInsta.validar(foto);

        UsuarioInsta usuario = new UsuarioInsta(
                nombre.strip(),
                genero,
                id,
                credenciales.getPasswordHash(),
                credenciales.getPasswordSalt(),
                edad,
                rutaFoto
        );

        guardarUsuario(
                usuario,
                publicaciones.cambio(id, new ListaEnlazada<>()),
                seguidores.cambio(id, new ListaEnlazada<>()),
                seguidos.cambio(id, new ListaEnlazada<>()),
                inbox.cambio(id, new ListaEnlazada<>()),
                stickers.cambio(
                        id,
                        repo.leer("globales.ins", Sticker.class)
                ),
                new RepositorioInsta.Cambio(rutaFoto, imagen),
                new RepositorioInsta.Cambio(
                        id + "/folders_personales",
                        Boolean.TRUE
                ),
                new RepositorioInsta.Cambio(
                        id + "/stickers_personales",
                        Boolean.TRUE
                )
        );

        return usuario;
    }

    public synchronized UsuarioInsta iniciarSesion(
            String username,
            String password
    ) throws IOException, ArchivoCorruptoException {

        if (username == null
                || password == null
                || username.length() > 24
                || password.length() > 128) {
            return null;
        }

        UsuarioInsta usuario = buscarUsuario(username);

        return usuario != null
                && SeguridadPassword.verificar(
                        password,
                        usuario.getPasswordHash(),
                        usuario.getPasswordSalt()
                )
                ? usuario
                : null;
    }

    public synchronized UsuarioInsta buscarUsuario(String username)
            throws IOException, ArchivoCorruptoException {

        String id = normalizar(username);

        return usuarios().buscar(
                usuario -> usuario.getUsername().equals(id)
        );
    }

    public synchronized void exigirCuentaActiva(String username)
            throws IOException,
            ArchivoCorruptoException,
            CuentaDesactivadaException {

        if (!usuario(username).estaActiva()) {
            throw new CuentaDesactivadaException();
        }
    }

    public synchronized Respuesta operar(
            String actor,
            Solicitud solicitud
    ) throws Exception {

        usuario(actor);

        boolean perfilPropio =
                solicitud.getOperacion() == Solicitud.Operacion.PERFIL
                && normalizar(solicitud.arg(0)).equals(actor);

        if (solicitud.getOperacion()
                != Solicitud.Operacion.REACTIVAR
                && !perfilPropio) {
            exigirCuentaActiva(actor);
        }

        return switch (solicitud.getOperacion()) {
            case PUBLICAR_TEXTO, PUBLICAR_IMAGEN,
                    PUBLICAR_STICKER, PUBLICACIONES, TIMELINE ->
                publicacionServicio.ejecutar(actor, solicitud);

            case SEGUIR, DEJAR_SEGUIR, SEGUIDORES, SEGUIDOS ->
                seguimientoServicio.ejecutar(actor, solicitud);

            case PERFIL, EDITAR_PERFIL, FOTO_PERFIL,
                    DESACTIVAR, REACTIVAR ->
                perfilServicio.ejecutar(actor, solicitud);

            case BUSCAR_PERSONAS, BUSCAR_HASHTAG, MENCIONES ->
                busquedaServicio.ejecutar(actor, solicitud);

            case ENVIAR_MENSAJE, ENVIAR_STICKER,
                    CONVERSACION, LEER_CONVERSACION,
                    ELIMINAR_CONVERSACION, NO_LEIDOS ->
                inboxServicio.ejecutar(actor, solicitud);

            case STICKERS, IMPORTAR_STICKER,
                    CREAR_CARPETA, CARPETAS, ARCHIVO ->
                stickerServicio.ejecutar(actor, solicitud);

            default ->
                throw new IllegalArgumentException(
                        "Operación no soportada."
                );
        };
    }

    <T> Respuesta pagina(
            ListaEnlazada<T> lista,
            String desplazamiento
    ) {
        int desde = Integer.parseInt(desplazamiento);

        if (desde < 0) {
            throw new IllegalArgumentException("Página inválida.");
        }

        ListaEnlazada<T> pagina = new ListaEnlazada<>();
        int indice = 0;

        for (T valor : lista) {
            if (indice++ < desde) {
                continue;
            }

            if (pagina.size() == 100) {
                break;
            }

            pagina.agregar(valor);
        }

        return Respuesta.exito("Consulta completada.")
                .conLista(pagina)
                .conTotal(lista.size());
    }

    private void ejemplos() throws Exception {
        String[] bases = {
            "noticias_demo",
            "deporte_demo",
            "cine_demo"
        };

        if (!repo.existe("semilla.ins")) {
            ListaEnlazada<String> nombres = new ListaEnlazada<>();

            for (String base : bases) {
                String nombre = base;
                int numero = 1;

                while (buscarUsuario(nombre) != null || repo.existe(nombre)) {
                    nombre = base + numero++;
                }

                nombres.agregar(nombre);
            }

            repo.guardar(new RepositorioInsta.Cambio(
                    "semilla.ins",
                    nombres
            ));
        }

        ListaEnlazada<String> nombres = repo.leer("semilla.ins", String.class);

        String[] temas = {
            "Noticias del campus #noticias",
            "Entrenamos juntos #deporte",
            "Recomendaciones de cine #cine"
        };

        int indice = 0;

        for (String id : nombres) {
            if (buscarUsuario(id) == null) {
                registrarUsuario(
                        "Cuenta de ejemplo " + (indice + 1),
                        'M',
                        id,
                        "Demo1234!",
                        20
                );
            }

            ListaEnlazada<Publicacion> lista = publicaciones.cargar(id);

            if (lista.isEmpty()) {
                lista.agregar(Publicacion.crearTexto(
                        id,
                        temas[indice % temas.length]
                ));

                lista.agregar(Publicacion.crearTexto(
                        id,
                        "Comparte tu mirada. " + temas[indice % temas.length]
                ));

                publicaciones.guardar(id, lista);
            }

            indice++;
        }
    }

    public String getRutaRaiz() {
        return repo.getRutaRaiz();
    }

    @Override
    public synchronized void close() throws IOException {
        repo.close();
    }
}