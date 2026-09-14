
package hilos;

import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import java.util.function.BiConsumer;
import multimedia.ReproductorMusica;

public class HiloReproductor extends Thread {

    private ReproductorMusica reproductor;
    private JSlider progreso;
    private volatile boolean activo;
    private Runnable accionCancionTerminada;
    private BiConsumer<Long, Long> accionTiempoActualizado;
    private boolean finalProcesado;

    public HiloReproductor(ReproductorMusica reproductor, JSlider progreso) {
        this.reproductor = reproductor;
        this.progreso = progreso;
        this.activo = true;
        this.finalProcesado = false;
    }

    @Override
    public void run() {
        while (activo) {
            try {
                long actual = reproductor.getPosicionActual();
                long duracion = reproductor.getDuracion();

                if (duracion > 0) {
                    int porcentaje = (int) ((actual * 100) / duracion);

                    SwingUtilities.invokeLater(() -> {
                        progreso.setValue(porcentaje);
                        if (accionTiempoActualizado != null) accionTiempoActualizado.accept(actual, duracion);
                    });
                }

                if (reproductor.terminoCancion()) {
                    if (!finalProcesado && accionCancionTerminada != null) {
                        finalProcesado = true;

                        SwingUtilities.invokeLater(() -> {
                            accionCancionTerminada.run();
                            finalProcesado = false;
                        });
                    }
                } else {
                    finalProcesado = false;
                }

                Thread.sleep(300);

            } catch (InterruptedException e) {
                activo = false;
                Thread.currentThread().interrupt();
            }
        }
    }

    public void setAccionCancionTerminada(Runnable accion) {
        this.accionCancionTerminada = accion;
    }

    public void setAccionTiempoActualizado(BiConsumer<Long, Long> accion) {
        this.accionTiempoActualizado = accion;
    }

    public void detenerHilo() {
        activo = false;
        interrupt();
    }
}