
package red;

import insta.servicio.InstaServicio;
import insta.modelo.UsuarioInsta;
import excepciones.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Servidor implements AutoCloseable {

    public static final int PUERTO_PREDETERMINADO = 5050;

    private record SesionRed(String usuario, long vence) {
    }

    private final ServerSocket socket;
    private final InstaServicio servicio;

    private final ConcurrentHashMap<String, SesionRed> sesiones =
            new ConcurrentHashMap<>();

    private final Set<Socket> conexiones =
            ConcurrentHashMap.newKeySet();

    private final ExecutorService hilos =
            new ThreadPoolExecutor(
                    0,
                    32,
                    30,
                    TimeUnit.SECONDS,
                    new SynchronousQueue<>()
            );

    private final AtomicBoolean cerrado = new AtomicBoolean();
    private final AtomicBoolean iniciado = new AtomicBoolean();

    public Servidor()
            throws IOException, ArchivoCorruptoException {
        this(PUERTO_PREDETERMINADO, "INSTA_RAIZ");
    }

    public Servidor(int puerto, String raiz)
            throws IOException, ArchivoCorruptoException {

        socket = new ServerSocket(puerto);

        try {
            servicio = new InstaServicio(raiz);

        } catch (IOException
                | ArchivoCorruptoException
                | RuntimeException e) {
            socket.close();
            throw e;
        }
    }

    public int getPuerto() {
        return socket.getLocalPort();
    }

    public void iniciar() throws IOException {
        if (cerrado.get()
                || !iniciado.compareAndSet(false, true)) {
            throw new IllegalStateException(
                    "Servidor ya iniciado o cerrado."
            );
        }

        while (!cerrado.get()) {
            try {
                Socket cliente = socket.accept();
                conexiones.add(cliente);

                try {
                    hilos.execute(
                            new ManejadorCliente(cliente, this)
                    );

                } catch (RejectedExecutionException e) {
                    conexiones.remove(cliente);
                    cliente.close();
                }

            } catch (SocketException e) {
                if (!cerrado.get()) {
                    throw e;
                }
            }
        }
    }

    String obtenerUsuarioDeSesion(String token) {
        if (token == null) {
            return null;
        }

        long ahora = System.currentTimeMillis();

        SesionRed sesion = sesiones.computeIfPresent(
                token,
                (clave, anterior) ->
                        anterior.vence() < ahora
                                ? null
                                : new SesionRed(
                                        anterior.usuario(),
                                        ahora + 7200000
                                )
        );

        return sesion == null ? null : sesion.usuario();
    }

    Respuesta procesarSolicitud(Solicitud solicitud) {
        try {
            if (cerrado.get()
                    || solicitud == null
                    || solicitud.getOperacion() == null) {
                throw new IllegalArgumentException(
                        "Solicitud inválida."
                );
            }

            if (solicitud.getOperacion()
                    == Solicitud.Operacion.REGISTRAR_USUARIO) {

                if (solicitud.arg(1).length() != 1) {
                    throw new IllegalArgumentException(
                            "Género inválido."
                    );
                }

                return Respuesta.cuentaCreada(
                        servicio.registrarUsuario(
                                solicitud.arg(0),
                                solicitud.arg(1).charAt(0),
                                solicitud.arg(2),
                                solicitud.arg(3),
                                Integer.parseInt(solicitud.arg(4)),
                                solicitud.getArchivo()
                        )
                );
            }

            if (solicitud.getOperacion()
                    == Solicitud.Operacion.INICIAR_SESION) {

                UsuarioInsta usuario = servicio.iniciarSesion(
                        solicitud.arg(0),
                        solicitud.arg(1)
                );

                if (usuario == null) {
                    return Respuesta.error(
                            Respuesta.Codigo.CREDENCIALES_INCORRECTAS,
                            "Usuario o contraseña incorrectos."
                    );
                }

                var perfil = servicio.operar(
                        usuario.getUsername(),
                        new Solicitud(
                                Solicitud.Operacion.PERFIL,
                                "",
                                null,
                                usuario.getUsername()
                        )
                ).getUsuario();

                long ahora = System.currentTimeMillis();

                sesiones.entrySet().removeIf(
                        entrada -> entrada.getValue().vence() < ahora
                );

                String token = UUID.randomUUID().toString();

                sesiones.put(
                        token,
                        new SesionRed(
                                usuario.getUsername(),
                                ahora + 7200000
                        )
                );

                return Respuesta.sesionIniciada(usuario, token)
                        .conUsuario(perfil);
            }

            String actor = obtenerUsuarioDeSesion(
                    solicitud.getTokenSesion()
            );

            if (actor == null) {
                return Respuesta.error(
                        Respuesta.Codigo.SESION_INVALIDA,
                        "Inicia sesión nuevamente."
                );
            }

            if (solicitud.getOperacion()
                    == Solicitud.Operacion.CERRAR_SESION) {

                sesiones.remove(solicitud.getTokenSesion());

                return Respuesta.exito("Sesión cerrada.");
            }

            Respuesta respuesta =
                    servicio.operar(actor, solicitud);

            if (solicitud.getOperacion()
                    == Solicitud.Operacion.DESACTIVAR
                    || solicitud.getOperacion()
                    == Solicitud.Operacion.EDITAR_PERFIL) {

                sesiones.entrySet().removeIf(
                        entrada -> entrada.getValue()
                                .usuario().equals(actor)
                                && !entrada.getKey().equals(
                                        solicitud.getTokenSesion()
                                )
                );
            }

            return respuesta;

        } catch (UsernameDuplicadoException e) {
            return Respuesta.error(
                    Respuesta.Codigo.USERNAME_DUPLICADO,
                    e.getMessage()
            );

        } catch (CuentaDesactivadaException e) {
            return Respuesta.error(
                    Respuesta.Codigo.CUENTA_DESACTIVADA,
                    "La cuenta está desactivada."
            );

        } catch (ArchivoCorruptoException | IOException e) {
            System.err.println(e.getMessage());

            return Respuesta.error(
                    Respuesta.Codigo.ERROR_ALMACENAMIENTO,
                    "No se pudo completar el almacenamiento. Revisa el servidor."
            );

        } catch (IllegalArgumentException | NullPointerException e) {
            return Respuesta.error(
                    Respuesta.Codigo.DATOS_INVALIDOS,
                    Objects.toString(
                            e.getMessage(),
                            "Datos inválidos."
                    )
            );

        } catch (Exception e) {
            System.err.println(e);

            return Respuesta.error(
                    Respuesta.Codigo.ERROR_INTERNO,
                    "No se pudo completar la operación."
            );
        }
    }

    void finalizarConexion(Socket socket) {
        conexiones.remove(socket);
    }

    @Override
    public void close() {
        if (!cerrado.compareAndSet(false, true)) {
            return;
        }

        try {
            socket.close();
        } catch (IOException e) {
        }

        for (Socket conexion : conexiones) {
            try {
                conexion.close();
            } catch (IOException e) {
            }
        }

        hilos.shutdown();

        try {
            if (!hilos.awaitTermination(35, TimeUnit.SECONDS)) {
                hilos.shutdownNow();
            }
        } catch (InterruptedException e) {
            hilos.shutdownNow();
            Thread.currentThread().interrupt();
        }

        try {
            servicio.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        sesiones.clear();
    }

    public static void main(String[] args) {
        try (Servidor servidor = new Servidor()) {
            Runtime.getRuntime().addShutdownHook(
                    new Thread(servidor::close)
            );

            System.out.println(
                    "INSTA+ en puerto " + servidor.getPuerto()
            );

            servidor.iniciar();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}