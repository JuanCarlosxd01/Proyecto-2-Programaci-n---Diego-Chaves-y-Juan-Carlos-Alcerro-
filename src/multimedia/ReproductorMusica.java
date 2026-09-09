
package multimedia;

import java.io.File;
import javax.sound.sampled.*;

public class ReproductorMusica {

    private Clip clip;
    private long posicionPausa;
    private boolean pausado;

    public ReproductorMusica() {
        posicionPausa = 0;
        pausado = false;
    }

    public void cargarCancion(File archivo) throws Exception {

        detener();

        AudioInputStream audio =
                AudioSystem.getAudioInputStream(archivo);

        clip = AudioSystem.getClip();
        clip.open(audio);

        posicionPausa = 0;
        pausado = false;
    }

    public void reproducir() {

        if (clip == null) {
            return;
        }

        if (pausado) {
            clip.setMicrosecondPosition(posicionPausa);
        }

        clip.start();
        pausado = false;
    }

    public void pausar() {

        if (clip == null) {
            return;
        }

        if (clip.isRunning()) {
            posicionPausa = clip.getMicrosecondPosition();
            clip.stop();
            pausado = true;
        }
    }

    public void detener() {

        if (clip != null) {
            clip.stop();
            clip.setMicrosecondPosition(0);
        }

        posicionPausa = 0;
        pausado = false;
    }

    public long getPosicionActual() {

        if (clip == null) {
            return 0;
        }

        return clip.getMicrosecondPosition();
    }

    public long getDuracion() {

        if (clip == null) {
            return 0;
        }

        return clip.getMicrosecondLength();
    }

    public boolean estaReproduciendo() {

        return clip != null && clip.isRunning();
    }

    public boolean estaCargada() {
        return clip != null;
    }

    public boolean terminoCancion() {

        if (clip == null) {
            return false;
        }

        return clip.getMicrosecondPosition()
                >= clip.getMicrosecondLength();
    }
}