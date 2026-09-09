
package hilos;

import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import multimedia.ReproductorMusica;

public class HiloReproductor extends Thread {

    private ReproductorMusica reproductor;
    private JSlider progreso;
    private boolean activo;

    public HiloReproductor(
            ReproductorMusica reproductor,
            JSlider progreso) {

        this.reproductor = reproductor;
        this.progreso = progreso;
        this.activo = true;
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
                    });
                }

                Thread.sleep(500);

            } catch (InterruptedException e) {
                activo = false;
            }
        }
    }

    public void detenerHilo() {
        activo = false;
        interrupt();
    }
}
