package hilos;

import estructuras.ListaEnlazada;
import insta.modelo.Comentario;
import insta.modelo.Mensaje;
import insta.modelo.Publicacion;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import red.Cliente;
import red.Respuesta;

public class HiloNotificaciones implements AutoCloseable {

    private final Cliente cliente;

    private final Consumer<ListaEnlazada<Mensaje>> recibirMensajes;
    private final Consumer<ListaEnlazada<Publicacion>> recibirMenciones;
    private final Consumer<Throwable> error;

    private final Set<String> mensajesAvisados = new HashSet<>();
    private final Set<String> mencionesAvisadas = new HashSet<>();

    private final AtomicBoolean iniciado = new AtomicBoolean();

    private final ScheduledExecutorService hilo = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "insta-notificaciones");
        thread.setDaemon(true);
        return thread;
    });

    private boolean errorNotificado;
    private boolean primeraRevisionMenciones = true;

    private volatile boolean cerrado;

    private long version;

    public HiloNotificaciones(Cliente cliente, Consumer<ListaEnlazada<Mensaje>> recibirMensajes, Consumer<ListaEnlazada<Publicacion>> recibirMenciones, Consumer<Throwable> error) {
        this.cliente = cliente;
        this.recibirMensajes = recibirMensajes;
        this.recibirMenciones = recibirMenciones;
        this.error = error;
    }

    public void iniciar() {
        if (!cerrado && iniciado.compareAndSet(false, true)) {
            version = cliente.getVersionSesion();
            hilo.scheduleWithFixedDelay(this::revisar, 0, 2, TimeUnit.SECONDS);
        }
    }

    private void revisar() {
        if (cerrado || !cliente.tieneSesion() || cliente.getVersionSesion() != version) {
            close();
            return;
        }

        try {
            revisarMensajes();
            revisarMenciones();

            errorNotificado = false;

        } catch (Exception e) {
            if (!cerrado && !errorNotificado) {
                errorNotificado = true;
                error.accept(e);
            }
        }
    }

    private void revisarMensajes() throws Exception {
        Set<String> actuales = new HashSet<>();

        ListaEnlazada<Mensaje> nuevos = new ListaEnlazada<>();

        int desde = 0;
        int total;

        do {
            verificarSesion();

            Respuesta respuesta = cliente.noLeidos(desde);

            validarRespuesta(respuesta);

            ListaEnlazada<Mensaje> mensajes = respuesta.getMensajes();

            total = respuesta.getTotal();

            for (Mensaje mensaje : mensajes) {
                actuales.add(mensaje.getId());

                if (!mensajesAvisados.contains(mensaje.getId())) {
                    nuevos.agregar(mensaje);
                }
            }

            if (mensajes.isEmpty()) {
                break;
            }

            desde += mensajes.size();

        } while (desde < total);

        verificarSesion();

        if (!nuevos.isEmpty()) {
            recibirMensajes.accept(nuevos);
        }

        mensajesAvisados.clear();
        mensajesAvisados.addAll(actuales);
    }

    private void revisarMenciones() throws Exception {
        Set<String> actuales = new HashSet<>();

        ListaEnlazada<Publicacion> nuevas = new ListaEnlazada<>();

        Set<String> publicacionesNuevas = new HashSet<>();

        String username = cliente.getUsuarioActual() == null ? "" : cliente.getUsuarioActual().getUsername();

        int desde = 0;
        int total;

        do {
            verificarSesion();

            Respuesta respuesta = cliente.menciones(desde);

            validarRespuesta(respuesta);

            ListaEnlazada<Publicacion> publicaciones = respuesta.getPublicaciones();

            total = respuesta.getTotal();

            for (Publicacion publicacion : publicaciones) {

                if (menciona(publicacion.getContenido(), username) && !publicacion.getAutor().equalsIgnoreCase(username)) {

                    String clave = "P:" + publicacion.getId();

                    actuales.add(clave);

                    if (!primeraRevisionMenciones && !mencionesAvisadas.contains(clave) && publicacionesNuevas.add(publicacion.getId())) {
                        nuevas.agregar(publicacion);
                    }
                }

                for (Comentario comentario : publicacion.getComentarios()) {

                    if (comentario.esTexto() && !comentario.getAutor().equalsIgnoreCase(username) && menciona(comentario.getTexto(), username)) {

                        String clave = "C:" + comentario.getId();

                        actuales.add(clave);

                        if (!primeraRevisionMenciones && !mencionesAvisadas.contains(clave) && publicacionesNuevas.add(publicacion.getId())) {
                            nuevas.agregar(publicacion);
                        }
                    }
                }
            }

            if (publicaciones.isEmpty()) {
                break;
            }

            desde += publicaciones.size();

        } while (desde < total);

        verificarSesion();

        if (!nuevas.isEmpty()) {
            recibirMenciones.accept(nuevas);
        }

        mencionesAvisadas.clear();
        mencionesAvisadas.addAll(actuales);

        primeraRevisionMenciones = false;
    }

    private boolean menciona(String texto, String username) {
        if (texto == null || username == null || username.isBlank()) {
            return false;
        }

        return texto.toLowerCase().matches("(?s).*@" + java.util.regex.Pattern.quote(username.toLowerCase()) + "(?![\\p{L}\\p{N}_]).*");
    }

    private void verificarSesion() {
        if (cerrado || Thread.currentThread().isInterrupted() || cliente.getVersionSesion() != version) {
            throw new IllegalStateException("Sesión finalizada.");
        }
    }

    private void validarRespuesta(Respuesta respuesta) {
        if (!respuesta.esExitosa()) {

            if (respuesta.getCodigo() == Respuesta.Codigo.SESION_INVALIDA || respuesta.getCodigo() == Respuesta.Codigo.CUENTA_DESACTIVADA) {
                close();
            }

            throw new IllegalStateException(respuesta.getMensaje());
        }
    }

    @Override
    public void close() {
        cerrado = true;
        hilo.shutdownNow();
    }
}