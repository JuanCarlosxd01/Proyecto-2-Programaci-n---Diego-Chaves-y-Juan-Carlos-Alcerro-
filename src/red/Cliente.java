
package red;

import java.io.*;
import java.net.*;
import static red.Solicitud.Operacion.*;

public class Cliente implements AutoCloseable {

    private final String host;
    private final int puerto;

    private volatile String token = "";
    private volatile Respuesta.DatosUsuario usuario;
    private volatile long versionSesion;

    private boolean cerrado;

    public Cliente() {
        this("127.0.0.1", Servidor.PUERTO_PREDETERMINADO);
    }

    public Cliente(String host, int puerto) {
        if (host == null
                || host.isBlank()
                || puerto < 1
                || puerto > 65535) {
            throw new IllegalArgumentException(
                    "Servidor inválido."
            );
        }

        this.host = host;
        this.puerto = puerto;
    }

    private synchronized Respuesta enviar(Solicitud solicitud)
            throws IOException {

        if (cerrado) {
            throw new IOException("Cliente cerrado.");
        }

        if (solicitud.getArchivo() != null
                && solicitud.getArchivo().length > 4 * 1024 * 1024) {
            throw new IOException("La imagen supera 4 MB.");
        }

        try (Socket socket = new Socket()) {
            socket.connect(
                    new InetSocketAddress(host, puerto),
                    5000
            );

            socket.setSoTimeout(30000);

            try (ObjectOutputStream salida =
                         new ObjectOutputStream(socket.getOutputStream())) {

                salida.flush();

                try (ObjectInputStream entrada =
                             FiltroRed.entrada(socket.getInputStream())) {

                    salida.writeObject(solicitud);
                    salida.flush();

                    Object objeto = entrada.readObject();

                    if (!(objeto instanceof Respuesta respuesta)
                            || respuesta.getCodigo() == null
                            || respuesta.getMensaje() == null) {
                        throw new IOException("Respuesta inválida.");
                    }

                    if (respuesta.getCodigo()
                            == Respuesta.Codigo.SESION_INVALIDA) {
                        token = "";
                        usuario = null;
                        versionSesion++;
                    }

                    if (respuesta.esExitosa()
                            && respuesta.tieneUsuario()
                            && usuario != null
                            && respuesta.getUsuario().getUsername()
                                    .equals(usuario.getUsername())) {
                        usuario = respuesta.getUsuario();
                    }

                    return respuesta;
                }
            }

        } catch (ClassNotFoundException e) {
            throw new IOException(
                    "Versiones incompatibles de cliente y servidor.",
                    e
            );
        }
    }

    private synchronized Respuesta accion(
            Solicitud.Operacion operacion,
            byte[] bytes,
            String... argumentos
    ) throws IOException {

        if (!tieneSesion()) {
            return Respuesta.error(
                    Respuesta.Codigo.SESION_INVALIDA,
                    "Inicia sesión."
            );
        }

        Respuesta respuesta = enviar(new Solicitud(
                operacion,
                token,
                bytes,
                argumentos
        ));

        if (respuesta.esExitosa() && respuesta.tieneUsuario() && usuario != null
                && respuesta.getUsuario().getUsername().equalsIgnoreCase(usuario.getUsername())) {
            usuario = respuesta.getUsuario();
        }

        return respuesta;
    }

    public synchronized Respuesta registrarUsuario(
            String nombre,
            char genero,
            String username,
            String password,
            int edad
    ) throws IOException {

        return registrarUsuario(
                nombre,
                genero,
                username,
                password,
                edad,
                null
        );
    }

    public synchronized Respuesta registrarUsuario(
            String nombre,
            char genero,
            String username,
            String password,
            int edad,
            byte[] foto
    ) throws IOException {

        return enviar(new Solicitud(
                REGISTRAR_USUARIO,
                "",
                foto,
                nombre,
                String.valueOf(genero),
                username,
                password,
                String.valueOf(edad)
        ));
    }

    public synchronized Respuesta iniciarSesion(
            String username,
            String password
    ) throws IOException {

        if (tieneSesion()) {
            throw new IllegalStateException(
                    "Cierra primero la sesión actual."
            );
        }

        Respuesta respuesta = enviar(
                Solicitud.iniciarSesion(username, password)
        );

        if (respuesta.esExitosa()) {
            if (!respuesta.tieneUsuario()
                    || respuesta.getTokenSesion() == null
                    || respuesta.getTokenSesion().isBlank()) {
                throw new IOException("Login incompleto.");
            }

            token = respuesta.getTokenSesion();
            usuario = respuesta.getUsuario();
            versionSesion++;
        }

        return respuesta;
    }

    public synchronized Respuesta cerrarSesion()
            throws IOException {

        if (!tieneSesion()) {
            return Respuesta.exito("No hay sesión.");
        }

        Respuesta respuesta = enviar(
                Solicitud.cerrarSesion(token)
        );

        if (respuesta.esExitosa()
                || respuesta.getCodigo()
                == Respuesta.Codigo.SESION_INVALIDA) {
            token = "";
            usuario = null;
            versionSesion++;
        }

        return respuesta;
    }

    public boolean tieneSesion() {
        return !token.isEmpty();
    }

    public long getVersionSesion() {
        return versionSesion;
    }

    public Respuesta.DatosUsuario getUsuarioActual() {
        return usuario;
    }

    public Respuesta publicarTexto(String texto) throws IOException {
        return accion(PUBLICAR_TEXTO, null, texto);
    }

    public Respuesta publicarImagen(
            byte[] imagen,
            String texto,
            String carpeta
    ) throws IOException {
        return accion(PUBLICAR_IMAGEN, imagen, texto, carpeta);
    }

    public Respuesta publicarSticker(String texto, String id)
            throws IOException {
        return accion(PUBLICAR_STICKER, null, texto, id);
    }

    public Respuesta alternarLike(String autor, String publicacionId) throws IOException {
        return accion(LIKE_PUBLICACION, null, autor, publicacionId);
    }

    public Respuesta comentarPublicacion(String autor, String publicacionId, String texto) throws IOException {
        return accion(COMENTAR_PUBLICACION, null, autor, publicacionId, texto);
    }

    public Respuesta comentarSticker(String autor, String publicacionId, String stickerId) throws IOException {
        return accion(COMENTAR_STICKER, null, autor, publicacionId, stickerId);
    }

    public Respuesta eliminarComentario(String autorPublicacion, String publicacionId, String comentarioId) throws IOException {
        return accion(ELIMINAR_COMENTARIO, null, autorPublicacion, publicacionId, comentarioId);
    }

    public Respuesta editarPublicacion(String publicacionId, String contenido) throws IOException {
        return accion(EDITAR_PUBLICACION, null, publicacionId, contenido);
    }

    public Respuesta eliminarPublicacion(String publicacionId) throws IOException {
        return accion(ELIMINAR_PUBLICACION, null, publicacionId);
    }

    public Respuesta publicaciones(String usuario, int desde)
            throws IOException {
        return accion(PUBLICACIONES, null, usuario, "" + desde);
    }

    public Respuesta timeline(int desde) throws IOException {
        return accion(TIMELINE, null, "" + desde);
    }

    public Respuesta seguir(String usuario) throws IOException {
        return accion(SEGUIR, null, usuario);
    }

    public Respuesta dejarDeSeguir(String usuario)
            throws IOException {
        return accion(DEJAR_SEGUIR, null, usuario);
    }

    public Respuesta seguidores(String usuario, int desde)
            throws IOException {
        return accion(SEGUIDORES, null, usuario, "" + desde);
    }

    public Respuesta seguidos(String usuario, int desde)
            throws IOException {
        return accion(SEGUIDOS, null, usuario, "" + desde);
    }

    public Respuesta perfil(String usuario) throws IOException {
        return accion(PERFIL, null, usuario);
    }

    public Respuesta editarPerfil(
            String nombre,
            char genero,
            int edad,
            String actual,
            String nueva,
            String biografia
    ) throws IOException {
        return accion(
                EDITAR_PERFIL,
                null,
                nombre,
                "" + genero,
                "" + edad,
                actual,
                nueva,
                biografia == null ? "" : biografia
        );
    }

    public Respuesta cambiarFoto(byte[] foto) throws IOException {
        return accion(FOTO_PERFIL, foto);
    }

    public Respuesta desactivarCuenta() throws IOException {
        return accion(DESACTIVAR, null);
    }

    public Respuesta reactivarCuenta() throws IOException {
        return accion(REACTIVAR, null);
    }

    public Respuesta buscarPersonas(String texto, int desde)
            throws IOException {
        return accion(BUSCAR_PERSONAS, null, texto, "" + desde);
    }

    public Respuesta buscarHashtag(String texto, int desde)
            throws IOException {
        return accion(BUSCAR_HASHTAG, null, texto, "" + desde);
    }

    public Respuesta menciones(int desde) throws IOException {
        return accion(MENCIONES, null, "" + desde);
    }

    public Respuesta enviarMensaje(String usuario, String texto)
            throws IOException {
        return accion(ENVIAR_MENSAJE, null, usuario, texto);
    }

    public Respuesta enviarSticker(String usuario, String id)
            throws IOException {
        return accion(ENVIAR_STICKER, null, usuario, id);
    }

    public Respuesta conversacion(String usuario, int desde)
            throws IOException {
        return accion(CONVERSACION, null, usuario, "" + desde);
    }

    public Respuesta marcarLeidos(String usuario)
            throws IOException {
        return accion(LEER_CONVERSACION, null, usuario);
    }

    public Respuesta eliminarConversacion(String usuario)
            throws IOException {
        return accion(ELIMINAR_CONVERSACION, null, usuario);
    }

    public Respuesta noLeidos(int desde) throws IOException {
        return accion(NO_LEIDOS, null, "" + desde);
    }

    public Respuesta stickers(int desde) throws IOException {
        return accion(STICKERS, null, "" + desde);
    }

    public Respuesta importarSticker(
            String nombre,
            String archivo,
            byte[] bytes
    ) throws IOException {
        return accion(IMPORTAR_STICKER, bytes, nombre, archivo);
    }

    public Respuesta crearCarpeta(String nombre)
            throws IOException {
        return accion(CREAR_CARPETA, null, nombre);
    }

    public Respuesta carpetas(int desde) throws IOException {
        return accion(CARPETAS, null, "" + desde);
    }

    public Respuesta descargarImagen(String referencia)
            throws IOException {
        return accion(ARCHIVO, null, referencia);
    }

    @Override
    public synchronized void close() throws IOException {
        if (cerrado) {
            return;
        }

        try {
            cerrarSesion();

        } finally {
            cerrado = true;
            token = "";
            usuario = null;
            versionSesion++;
        }
    }
}
