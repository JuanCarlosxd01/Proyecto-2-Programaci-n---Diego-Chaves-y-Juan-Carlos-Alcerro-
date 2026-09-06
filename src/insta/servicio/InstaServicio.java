
package insta.servicio;

import estructuras.ListaEnlazada;

import excepciones.ArchivoCorruptoException;
import excepciones.CuentaDesactivadaException;
import excepciones.UsernameDuplicadoException;

import insta.modelo.Mensaje;
import insta.modelo.Publicacion;
import insta.modelo.Seguimiento;
import insta.modelo.Sticker;
import insta.modelo.UsuarioInsta;

import persistencia.GestorBinario;
import persistencia.GestorUsuariosInstaBinario;

import java.io.IOException;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import java.util.Locale;
import java.util.Objects;

public class InstaServicio {

    private static final String[] CARPETAS_PERSONALES = {
        "imagenes",
        "folders_personales",
        "stickers_personales"
    };

    private static final String[] ARCHIVOS_PERSONALES = {
        "following.ins",
        "followers.ins",
        "insta.ins",
        "inbox.ins",
        "stickers.ins"
    };

    private final Path carpetaRaiz;

    private final GestorBinario gestorBinario;
    private final GestorUsuariosInstaBinario gestorUsuarios;

    public InstaServicio()
            throws IOException, ArchivoCorruptoException {

        this("INSTA_RAIZ");
    }

    public InstaServicio(String rutaRaiz)
            throws IOException, ArchivoCorruptoException {

        Objects.requireNonNull(
                rutaRaiz,
                "La carpeta raíz no puede ser null."
        );

        if (rutaRaiz.isBlank()) {
            throw new IllegalArgumentException(
                    "Debes indicar la carpeta raíz de INSTA+."
            );
        }

        this.carpetaRaiz = Path.of(rutaRaiz)
                .toAbsolutePath()
                .normalize();

        this.gestorBinario = new GestorBinario();

        this.gestorUsuarios = new GestorUsuariosInstaBinario(
                carpetaRaiz.toString()
        );

        inicializarAlmacenamiento();
    }


    private void inicializarAlmacenamiento()
            throws IOException, ArchivoCorruptoException {

        Files.createDirectories(carpetaRaiz);

        try {
            gestorUsuarios.cargarUsuarios();

        } catch (NoSuchFileException e) {

            try (
                    DirectoryStream<Path> contenido =
                            Files.newDirectoryStream(carpetaRaiz)
            ) {
                if (contenido.iterator().hasNext()) {
                    throw new ArchivoCorruptoException(
                            "No se encontró users.ins, pero "
                            + "INSTA_RAIZ ya contiene datos. "
                            + "Es necesario revisar el almacenamiento "
                            + "antes de continuar.",
                            e
                    );
                }
            }

            ListaEnlazada<UsuarioInsta> usuariosIniciales =
                    new ListaEnlazada<>();

            gestorUsuarios.guardarUsuarios(usuariosIniciales);
        }

        Files.createDirectories(
                carpetaRaiz.resolve("stickers_globales")
        );
    }

    public synchronized UsuarioInsta registrarUsuario(
            String nombreCompleto,
            char genero,
            String username,
            String password,
            int edad
    ) throws IOException,
            ArchivoCorruptoException,
            UsernameDuplicadoException {

        String usernameNormalizado =
                normalizarUsername(username);

        char generoNormalizado =
                Character.toUpperCase(genero);

        validarDatosRegistro(
                nombreCompleto,
                generoNormalizado,
                usernameNormalizado,
                password,
                edad
        );

        ListaEnlazada<UsuarioInsta> usuarios =
                gestorUsuarios.cargarUsuarios();

        UsuarioInsta existente = usuarios.buscar(
                usuario -> usuario.getUsername()
                        .equalsIgnoreCase(usernameNormalizado)
        );

        if (existente != null) {
            throw new UsernameDuplicadoException(
                    "El username @"
                    + usernameNormalizado
                    + " ya está registrado."
            );
        }

        SeguridadPassword.Credenciales credenciales =
                SeguridadPassword.generarCredenciales(password);

        UsuarioInsta nuevoUsuario = new UsuarioInsta(
                nombreCompleto.strip(),
                generoNormalizado,
                usernameNormalizado,
                credenciales.getPasswordHash(),
                credenciales.getPasswordSalt(),
                edad,
                ""
        );

        Path carpetaUsuario =
                carpetaRaiz.resolve(usernameNormalizado);


        Files.createDirectory(carpetaUsuario);

        try {
            prepararEspacioUsuario(carpetaUsuario);

            usuarios.agregar(nuevoUsuario);
            gestorUsuarios.guardarUsuarios(usuarios);

        } catch (IOException | RuntimeException error) {


            try {
                retirarEspacioInicial(carpetaUsuario);

            } catch (IOException errorLimpieza) {
                error.addSuppressed(errorLimpieza);
            }

            throw error;
        }

        return nuevoUsuario;
    }

    public synchronized UsuarioInsta iniciarSesion(
            String username,
            String password
    ) throws IOException, ArchivoCorruptoException {

        if (username == null || password == null) {
            return null;
        }

        String usernameNormalizado =
                normalizarUsername(username);

        ListaEnlazada<UsuarioInsta> usuarios =
                gestorUsuarios.cargarUsuarios();

        UsuarioInsta encontrado = usuarios.buscar(
                usuario -> usuario.getUsername()
                        .equalsIgnoreCase(usernameNormalizado)
        );

        if (encontrado == null) {
            return null;
        }

        boolean passwordCorrecta = SeguridadPassword.verificar(
                password,
                encontrado.getPasswordHash(),
                encontrado.getPasswordSalt()
        );

        if (!passwordCorrecta) {
            return null;
        }

   
        return encontrado;
    }


    public synchronized UsuarioInsta buscarUsuario(
            String username
    ) throws IOException, ArchivoCorruptoException {

        String usernameNormalizado =
                normalizarUsername(username);

        ListaEnlazada<UsuarioInsta> usuarios =
                gestorUsuarios.cargarUsuarios();

        return usuarios.buscar(
                usuario -> usuario.getUsername()
                        .equalsIgnoreCase(usernameNormalizado)
        );
    }


    public synchronized void exigirCuentaActiva(
            String username
    ) throws IOException,
            ArchivoCorruptoException,
            CuentaDesactivadaException {

        UsuarioInsta usuario = buscarUsuario(username);

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "La cuenta indicada no existe."
            );
        }

        if (!usuario.estaActiva()) {
            throw new CuentaDesactivadaException();
        }
    }

    private void prepararEspacioUsuario(
            Path carpetaUsuario
    ) throws IOException {

        for (String nombreCarpeta : CARPETAS_PERSONALES) {
            Files.createDirectory(
                    carpetaUsuario.resolve(nombreCarpeta)
            );
        }

        gestorBinario.guardar(
                carpetaUsuario.resolve("following.ins").toString(),
                new ListaEnlazada<Seguimiento>()
        );

        gestorBinario.guardar(
                carpetaUsuario.resolve("followers.ins").toString(),
                new ListaEnlazada<Seguimiento>()
        );

        gestorBinario.guardar(
                carpetaUsuario.resolve("insta.ins").toString(),
                new ListaEnlazada<Publicacion>()
        );

        gestorBinario.guardar(
                carpetaUsuario.resolve("inbox.ins").toString(),
                new ListaEnlazada<Mensaje>()
        );

        gestorBinario.guardar(
                carpetaUsuario.resolve("stickers.ins").toString(),
                new ListaEnlazada<Sticker>()
        );
    }


    private void retirarEspacioInicial(
            Path carpetaUsuario
    ) throws IOException {

        for (String nombreArchivo : ARCHIVOS_PERSONALES) {
            Files.deleteIfExists(
                    carpetaUsuario.resolve(nombreArchivo)
            );
        }

        for (String nombreCarpeta : CARPETAS_PERSONALES) {
            Files.deleteIfExists(
                    carpetaUsuario.resolve(nombreCarpeta)
            );
        }

        Files.deleteIfExists(carpetaUsuario);
    }

    private String normalizarUsername(String username) {
        Objects.requireNonNull(
                username,
                "El username no puede ser null."
        );

        return username.strip().toLowerCase(Locale.ROOT);
    }

    private void validarDatosRegistro(
            String nombreCompleto,
            char genero,
            String username,
            String password,
            int edad
    ) {
        if (nombreCompleto == null
                || nombreCompleto.isBlank()) {

            throw new IllegalArgumentException(
                    "Debes ingresar tu nombre completo."
            );
        }

     
        if (!username.matches("[a-z][a-z0-9_]{2,23}")) {
            throw new IllegalArgumentException(
                    "El username debe tener de 3 a 24 caracteres, "
                    + "empezar por una letra y contener únicamente "
                    + "letras sin acentos, números o guion bajo."
            );
        }

        boolean nombreReservado = username.matches(
                "con|prn|aux|nul|com[1-9]|lpt[1-9]"
        );

        if (nombreReservado
                || username.equals("stickers_globales")) {

            throw new IllegalArgumentException(
                    "Ese username está reservado. Elige otro."
            );
        }

        if (genero != 'M' && genero != 'F') {
            throw new IllegalArgumentException(
                    "El género debe ser M o F."
            );
        }

        if (edad < 1 || edad > 120) {
            throw new IllegalArgumentException(
                    "La edad debe estar entre 1 y 120 años."
            );
        }

        if (password == null
                || password.isBlank()
                || password.length() < 4
                || password.length() > 128) {

            throw new IllegalArgumentException(
                    "La contraseña debe tener entre 4 y 128 "
                    + "caracteres y no estar formada solo por espacios."
            );
        }
    }

    public String getRutaRaiz() {
        return carpetaRaiz.toString();
    }
}