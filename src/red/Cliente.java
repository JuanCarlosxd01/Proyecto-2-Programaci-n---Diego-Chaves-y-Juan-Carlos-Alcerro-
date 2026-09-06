
package red;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import java.time.LocalDateTime;
import java.util.Objects;


public class Cliente implements AutoCloseable {

    private static final int TIEMPO_CONEXION_MS = 5_000;
    private static final int TIEMPO_RESPUESTA_MS = 30_000;

    private static final long MAX_BYTES_RESPUESTA =
            256 * 1024;

    private final String host;
    private final int puerto;

    private String tokenSesion;
    private Respuesta.DatosUsuario usuarioActual;

    private boolean cerrado;

    public Cliente() {
        this(
                "127.0.0.1",
                Servidor.PUERTO_PREDETERMINADO
        );
    }

    public Cliente(String host, int puerto) {
        Objects.requireNonNull(
                host,
                "La dirección del servidor no puede ser null."
        );

        if (host.isBlank()) {
            throw new IllegalArgumentException(
                    "Debes indicar la dirección del servidor."
            );
        }

        if (puerto < 1 || puerto > 65535) {
            throw new IllegalArgumentException(
                    "El puerto debe estar entre 1 y 65535."
            );
        }

        this.host = host.strip();
        this.puerto = puerto;

        this.tokenSesion = "";
        this.usuarioActual = null;
        this.cerrado = false;
    }

    public synchronized Respuesta registrarUsuario(
            String nombreCompleto,
            char genero,
            String username,
            String password,
            int edad
    ) throws IOException {

        Solicitud solicitud = Solicitud.registrarUsuario(
                nombreCompleto,
                genero,
                username,
                password,
                edad
        );

        return enviar(solicitud);
    }

    public synchronized Respuesta iniciarSesion(
            String username,
            String password
    ) throws IOException {

        comprobarAbierto();

        if (tieneSesion()) {
            throw new IllegalStateException(
                    "Debes cerrar la sesión actual "
                    + "antes de iniciar otra."
            );
        }

        Solicitud solicitud = Solicitud.iniciarSesion(
                username,
                password
        );

        Respuesta respuesta = enviar(solicitud);

        if (respuesta.esExitosa()) {
            validarRespuestaLogin(respuesta);

            this.tokenSesion = respuesta.getTokenSesion();
            this.usuarioActual = respuesta.getUsuario();
        }

        return respuesta;
    }

    public synchronized Respuesta cerrarSesion()
            throws IOException {

        comprobarAbierto();

        if (!tieneSesion()) {
            limpiarSesion();

            return Respuesta.exito(
                    "No hay una sesión abierta."
            );
        }

        Solicitud solicitud =
                Solicitud.cerrarSesion(tokenSesion);

        Respuesta respuesta = enviar(solicitud);

        if (respuesta.esExitosa()
                || respuesta.getCodigo()
                == Respuesta.Codigo.SESION_INVALIDA) {

            limpiarSesion();
        }

        return respuesta;
    }

    public synchronized boolean tieneSesion() {
        return !tokenSesion.isEmpty();
    }

    public synchronized Respuesta.DatosUsuario getUsuarioActual() {
        return usuarioActual;
    }

   
    private Respuesta enviar(Solicitud solicitud)
            throws IOException {

        comprobarAbierto();

        try (Socket socket = new Socket()) {
            socket.connect(
                    new InetSocketAddress(host, puerto),
                    TIEMPO_CONEXION_MS
            );

            socket.setSoTimeout(TIEMPO_RESPUESTA_MS);

            try (
                    ObjectOutputStream salida =
                            new ObjectOutputStream(
                                    socket.getOutputStream()
                            )
            ) {
                /*
                 * El servidor también crea primero su salida.
                 * Ambos envían la cabecera antes de abrir
                 * el ObjectInputStream.
                 */
                salida.flush();

                InputStream entradaLimitada =
                        new EntradaLimitada(
                                socket.getInputStream(),
                                MAX_BYTES_RESPUESTA
                        );

                ObjectInputStream entrada =
                        new ObjectInputStream(entradaLimitada);

                entrada.setObjectInputFilter(
                        Cliente::filtrarRespuesta
                );

                salida.writeObject(solicitud);
                salida.flush();

                Object recibido = entrada.readObject();

                if (!(recibido instanceof Respuesta)) {
                    throw new IOException(
                            "El servidor no devolvió "
                            + "una respuesta válida de INSTA+."
                    );
                }

                Respuesta respuesta = (Respuesta) recibido;

                if (respuesta.getCodigo() == null
                        || respuesta.getMensaje() == null) {

                    throw new IOException(
                            "La respuesta del servidor está incompleta."
                    );
                }

                return respuesta;
            }

        } catch (ConnectException e) {
            throw new IOException(
                    "No se pudo conectar con INSTA+. "
                    + "Comprueba que el servidor esté iniciado "
                    + "en " + host + ":" + puerto + ".",
                    e
            );

        } catch (SocketTimeoutException e) {
            throw new IOException(
                    "Se agotó el tiempo de espera "
                    + "para conectar o recibir la respuesta.",
                    e
            );

        } catch (ClassNotFoundException e) {
            throw new IOException(
                    "El cliente no reconoce una clase "
                    + "enviada por el servidor.",
                    e
            );
        }
    }

    private void validarRespuestaLogin(
            Respuesta respuesta
    ) throws IOException {

        if (!respuesta.tieneUsuario()) {
            throw new IOException(
                    "El servidor aceptó el login, "
                    + "pero no devolvió los datos del usuario."
            );
        }

        String token = respuesta.getTokenSesion();

        if (token == null || token.isBlank()) {
            throw new IOException(
                    "El servidor no devolvió un token de sesión."
            );
        }

        String username =
                respuesta.getUsuario().getUsername();

        if (username == null || username.isBlank()) {
            throw new IOException(
                    "Los datos del usuario están incompletos."
            );
        }
    }

    private void comprobarAbierto() {
        if (cerrado) {
            throw new IllegalStateException(
                    "Este cliente ya fue cerrado."
            );
        }
    }

    private void limpiarSesion() {
        tokenSesion = "";
        usuarioActual = null;
    }


    @Override
    public synchronized void close() throws IOException {
        if (cerrado) {
            return;
        }

        try {
            if (tieneSesion()) {
                cerrarSesion();
            }

        } finally {
            limpiarSesion();
            cerrado = true;
        }
    }

    private static ObjectInputFilter.Status filtrarRespuesta(
            ObjectInputFilter.FilterInfo informacion
    ) {
        if (informacion.depth() > 16
                || informacion.references() > 200
                || informacion.streamBytes() > MAX_BYTES_RESPUESTA) {

            return ObjectInputFilter.Status.REJECTED;
        }

        Class<?> clase = informacion.serialClass();

        if (clase == null) {
            return ObjectInputFilter.Status.UNDECIDED;
        }

        
        boolean permitida =
                clase == Respuesta.class
                || clase == Respuesta.Codigo.class
                || clase == Respuesta.DatosUsuario.class
                || clase == String.class
                || clase == Enum.class
                || clase == LocalDateTime.class
                || clase.getName().equals("java.time.Ser")
                || clase.isPrimitive();

        return permitida
                ? ObjectInputFilter.Status.ALLOWED
                : ObjectInputFilter.Status.REJECTED;
    }

    private static final class EntradaLimitada
            extends FilterInputStream {

        private long restantes;

        private EntradaLimitada(
                InputStream entrada,
                long limite
        ) {
            super(entrada);
            this.restantes = limite;
        }

        @Override
        public int read() throws IOException {
            comprobarLimite();

            int valor = in.read();

            if (valor != -1) {
                restantes--;
            }

            return valor;
        }

        @Override
        public int read(
                byte[] buffer,
                int offset,
                int longitud
        ) throws IOException {

            if (longitud == 0) {
                return 0;
            }

            comprobarLimite();

            int permitido = (int) Math.min(
                    longitud,
                    restantes
            );

            int leidos = in.read(
                    buffer,
                    offset,
                    permitido
            );

            if (leidos > 0) {
                restantes -= leidos;
            }

            return leidos;
        }

        @Override
        public long skip(long cantidad) throws IOException {
            if (cantidad <= 0) {
                return 0;
            }

            comprobarLimite();

            long omitidos = in.skip(
                    Math.min(cantidad, restantes)
            );

            restantes -= omitidos;

            return omitidos;
        }

        private void comprobarLimite() throws IOException {
            if (restantes <= 0) {
                throw new IOException(
                        "La respuesta supera el tamaño permitido."
                );
            }
        }
    }
} 
