
package hilos;
import estructuras.ListaEnlazada;
import insta.modelo.Mensaje;
import red.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class HiloNotificaciones implements AutoCloseable {

    private final Cliente cliente;
    private final Consumer<ListaEnlazada<Mensaje>> recibir;
    private final Consumer<Throwable> error;

    private final Set<String> avisados = new HashSet<>();
    private final AtomicBoolean iniciado = new AtomicBoolean();

    private final ScheduledExecutorService hilo =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(
                        runnable,
                        "insta-notificaciones"
                );

                thread.setDaemon(true);
                return thread;
            });

    private boolean errorNotificado;
    private volatile boolean cerrado;
    private long version;

    public HiloNotificaciones(
            Cliente cliente,
            Consumer<ListaEnlazada<Mensaje>> recibir,
            Consumer<Throwable> error
    ) {
        this.cliente = cliente;
        this.recibir = recibir;
        this.error = error;
    }

    public void iniciar() {
        if (!cerrado && iniciado.compareAndSet(false, true)) {
            version = cliente.getVersionSesion();

            hilo.scheduleWithFixedDelay(
                    this::revisar,
                    0,
                    2,
                    TimeUnit.SECONDS
            );
        }
    }

    private void revisar() {
        if (cerrado
                || !cliente.tieneSesion()
                || cliente.getVersionSesion() != version) {
            close();
            return;
        }

        try {
            Set<String> actuales = new HashSet<>();
            ListaEnlazada<Mensaje> nuevos = new ListaEnlazada<>();

            int desde = 0;
            int total;

            do {
                if (cerrado
                        || Thread.currentThread().isInterrupted()
                        || cliente.getVersionSesion() != version) {
                    return;
                }

                Respuesta respuesta = cliente.noLeidos(desde);

                if (!respuesta.esExitosa()) {
                    if (respuesta.getCodigo()
                            == Respuesta.Codigo.SESION_INVALIDA
                            || respuesta.getCodigo()
                            == Respuesta.Codigo.CUENTA_DESACTIVADA) {
                        close();
                    }

                    throw new IllegalStateException(
                            respuesta.getMensaje()
                    );
                }

                var mensajes = respuesta.getMensajes();
                total = respuesta.getTotal();

                for (Mensaje mensaje : mensajes) {
                    if (actuales.add(mensaje.getId())
                            && !avisados.contains(mensaje.getId())) {
                        nuevos.agregar(mensaje);
                    }
                }

                if (mensajes.isEmpty()) {
                    break;
                }

                desde += mensajes.size();

            } while (desde < total);

            if (cerrado
                    || cliente.getVersionSesion() != version) {
                return;
            }

            if (!nuevos.isEmpty()) {
                recibir.accept(nuevos);
            }

            avisados.clear();
            avisados.addAll(actuales);
            errorNotificado = false;

        } catch (Exception e) {
            if (!cerrado && !errorNotificado) {
                errorNotificado = true;
                error.accept(e);
            }
        }
    }

    @Override
    public void close() {
        cerrado = true;
        hilo.shutdownNow();
    }
}
