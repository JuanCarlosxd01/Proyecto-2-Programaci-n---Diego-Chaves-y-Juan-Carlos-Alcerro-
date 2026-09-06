
package red;

import excepciones.ArchivoCorruptoException;
import excepciones.UsernameDuplicadoException;

import insta.modelo.UsuarioInsta;
import insta.servicio.InstaServicio;

import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import java.util.Set;
import java.util.UUID;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.atomic.AtomicBoolean;

public class Servidor implements AutoCloseable {

    public static final int PUERTO_PREDETERMINADO = 5050;

    private final ServerSocket socketServidor;
    private final InstaServicio instaServicio;


    private final ConcurrentHashMap<String, String> sesiones =
            new ConcurrentHashMap<>();

    private final Set<Socket> conexiones =
            ConcurrentHashMap.newKeySet();


    private final ExecutorService trabajadores =
            new ThreadPoolExecutor(
                    0,
                    32,
                    30,
                    TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>()
            );

    private final AtomicBoolean iniciado =
            new AtomicBoolean(false);

    private final AtomicBoolean cerrado =
            new AtomicBoolean(false);

    public Servidor()
            throws IOException, ArchivoCorruptoException {

        this(PUERTO_PREDETERMINADO, "INSTA_RAIZ");
    }

    public Servidor(
            int puerto,
            String carpetaRaiz
    ) throws IOException, ArchivoCorruptoException {

        if (puerto < 0 || puerto > 65535) {
            throw new IllegalArgumentException(
                    "El puerto debe estar entre 0 y 65535."
            );
        }

  
        ServerSocket socketTemporal = new ServerSocket(puerto);

        try {
            this.instaServicio = new InstaServicio(carpetaRaiz);

        } catch (
                IOException
                | ArchivoCorruptoException
                | RuntimeException error
        ) {
            try {
                socketTemporal.close();

            } catch (IOException errorCierre) {
                error.addSuppressed(errorCierre);
            }

            throw error;
        }

        this.socketServidor = socketTemporal;
    }


    public void iniciar() throws IOException {
        if (cerrado.get()) {
            throw new IllegalStateException(
                    "El servidor ya está cerrado."
            );
        }

        if (!iniciado.compareAndSet(false, true)) {
            throw new IllegalStateException(
                    "El servidor ya fue iniciado."
            );
        }

        System.out.println(
                "INSTA+ escuchando en el puerto "
                + getPuerto()
        );

        while (!cerrado.get()) {
            Socket cliente;

            try {
                cliente = socketServidor.accept();

            } catch (SocketException e) {
                if (cerrado.get()) {
                    break;
                }

                throw e;
            }

            conexiones.add(cliente);

            try {
                trabajadores.execute(
                        new ManejadorCliente(cliente, this)
                );

            } catch (RejectedExecutionException e) {
                finalizarConexion(cliente);
                cerrarSocket(cliente);

                if (!cerrado.get()) {
                    System.err.println(
                            "No se aceptó la conexión: "
                            + "el servidor está ocupado."
                    );
                }
            }
        }
    }

 
    Respuesta procesarSolicitud(Solicitud solicitud) {
        if (cerrado.get()) {
            return Respuesta.error(
                    Respuesta.Codigo.ERROR_INTERNO,
                    "El servidor se está cerrando."
            );
        }

        if (solicitud == null
                || solicitud.getOperacion() == null) {

            return Respuesta.error(
                    Respuesta.Codigo.DATOS_INVALIDOS,
                    "La solicitud no contiene una operación válida."
            );
        }

        try {
            switch (solicitud.getOperacion()) {

                case REGISTRAR_USUARIO: {
                    UsuarioInsta nuevo =
                            instaServicio.registrarUsuario(
                                    solicitud.getNombreCompleto(),
                                    solicitud.getGenero(),
                                    solicitud.getUsername(),
                                    solicitud.getPassword(),
                                    solicitud.getEdad()
                            );

                    return Respuesta.cuentaCreada(nuevo);
                }

                case INICIAR_SESION: {
                    UsuarioInsta usuario =
                            instaServicio.iniciarSesion(
                                    solicitud.getUsername(),
                                    solicitud.getPassword()
                            );

                    if (usuario == null) {
                        return Respuesta.error(
                                Respuesta.Codigo.CREDENCIALES_INCORRECTAS,
                                "Usuario o contraseña incorrectos."
                        );
                    }

                    String token = UUID.randomUUID().toString();

                    sesiones.put(
                            token,
                            usuario.getUsername()
                    );

             
                    return Respuesta.sesionIniciada(
                            usuario,
                            token
                    );
                }

                case CERRAR_SESION: {
                    String token = solicitud.getTokenSesion();

                    if (token == null || token.isBlank()) {
                        return respuestaSesionInvalida();
                    }

                    String usuario = sesiones.remove(token);

                    if (usuario == null) {
                        return respuestaSesionInvalida();
                    }

                    return Respuesta.exito(
                            "Sesión cerrada correctamente."
                    );
                }

                default:
                    return Respuesta.error(
                            Respuesta.Codigo.OPERACION_NO_SOPORTADA,
                            "La operación todavía no está disponible."
                    );
            }

        } catch (UsernameDuplicadoException e) {
            return Respuesta.error(
                    Respuesta.Codigo.USERNAME_DUPLICADO,
                    e.getMessage()
            );

        } catch (ArchivoCorruptoException e) {
            System.err.println(
                    "Almacenamiento INSTA+: " + e.getMessage()
            );

            return Respuesta.error(
                    Respuesta.Codigo.ERROR_ALMACENAMIENTO,
                    "Se detectó un problema en los archivos "
                    + "de INSTA+. Revisa el servidor."
            );

        } catch (IOException e) {
            System.err.println(
                    "Error de almacenamiento INSTA+: "
                    + e.getMessage()
            );

            return Respuesta.error(
                    Respuesta.Codigo.ERROR_ALMACENAMIENTO,
                    "No se pudo acceder al almacenamiento. "
                    + "Revisa el servidor antes de continuar."
            );

        } catch (IllegalArgumentException | NullPointerException e) {
            String mensaje = e.getMessage();

            if (mensaje == null || mensaje.isBlank()) {
                mensaje = "Los datos enviados no son válidos.";
            }

            return Respuesta.error(
                    Respuesta.Codigo.DATOS_INVALIDOS,
                    mensaje
            );

        } catch (RuntimeException e) {
            System.err.println(
                    "Error interno INSTA+: "
                    + e.getClass().getSimpleName()
            );

            return Respuesta.error(
                    Respuesta.Codigo.ERROR_INTERNO,
                    "No se pudo completar la operación."
            );
        }
    }


    String obtenerUsuarioDeSesion(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        return sesiones.get(token);
    }

    private Respuesta respuestaSesionInvalida() {
        return Respuesta.error(
                Respuesta.Codigo.SESION_INVALIDA,
                "La sesión no existe o ya terminó."
        );
    }

    void finalizarConexion(Socket cliente) {
        conexiones.remove(cliente);
    }

    public int getPuerto() {
        return socketServidor.getLocalPort();
    }

    private void cerrarSocket(Socket socket) {
        try {
            socket.close();

        } catch (IOException e) {
            System.err.println(
                    "No se pudo cerrar una conexión."
            );
        }
    }

    @Override
    public void close() {
        if (!cerrado.compareAndSet(false, true)) {
            return;
        }

        try {
            socketServidor.close();

        } catch (IOException e) {
            System.err.println(
                    "No se pudo cerrar el socket del servidor."
            );
        }

        for (Socket cliente : conexiones) {
            cerrarSocket(cliente);
        }

        trabajadores.shutdown();

        try {
            if (!trabajadores.awaitTermination(
                    10,
                    TimeUnit.SECONDS
            )) {
                trabajadores.shutdownNow();
            }

        } catch (InterruptedException e) {
            trabajadores.shutdownNow();
            Thread.currentThread().interrupt();
        }

        conexiones.clear();
        sesiones.clear();
    }

    public static void main(String[] args) {
        try (Servidor servidor = new Servidor()) {

            Runtime.getRuntime().addShutdownHook(
                    new Thread(
                            servidor::close,
                            "insta-cierre-servidor"
                    )
            );

            servidor.iniciar();

        } catch (IOException | ArchivoCorruptoException e) {
            System.err.println(
                    "No se pudo iniciar INSTA+: "
                    + e.getMessage()
            );
        }
    }
}
