

package multimedia;

import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class ReproductorMusica {

    private Clip clip;
    private long posicionPausa;
    private boolean pausado;

    public ReproductorMusica() {
        posicionPausa = 0;
        pausado = false;
    }

    public void cargarCancion(File archivo) throws Exception {
        if (archivo == null || !archivo.exists()) {
            throw new IllegalArgumentException("El archivo no existe.");
        }

        cerrarClipActual();

        AudioInputStream audioOriginal = AudioSystem.getAudioInputStream(
                archivo
        );

        AudioFormat formatoOriginal = audioOriginal.getFormat();

        AudioFormat formatoDecodificado = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                formatoOriginal.getSampleRate(),
                16,
                formatoOriginal.getChannels(),
                formatoOriginal.getChannels() * 2,
                formatoOriginal.getSampleRate(),
                false
        );

        AudioInputStream audioDecodificado = AudioSystem.getAudioInputStream(
                formatoDecodificado,
                audioOriginal
        );

        clip = AudioSystem.getClip();
        clip.open(audioDecodificado);

        audioDecodificado.close();
        audioOriginal.close();

        posicionPausa = 0;
        pausado = false;

        clip.setMicrosecondPosition(0);
    }

    public void reproducir() {
        if (clip == null) {
            return;
        }

        if (clip.getMicrosecondPosition() >= clip.getMicrosecondLength()) {
            clip.setMicrosecondPosition(0);
            posicionPausa = 0;
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
        if (clip == null) {
            return;
        }

        clip.stop();
        clip.setMicrosecondPosition(0);

        posicionPausa = 0;
        pausado = false;
    }

    public void cambiarPosicion(long posicion) {
        if (clip == null) {
            return;
        }

        if (posicion < 0) {
            posicion = 0;
        }

        if (posicion > clip.getMicrosecondLength()) {
            posicion = clip.getMicrosecondLength();
        }

        boolean estabaReproduciendo = clip.isRunning();

        clip.stop();
        clip.setMicrosecondPosition(posicion);
        posicionPausa = posicion;

        if (estabaReproduciendo) {
            clip.start();
            pausado = false;
        } else {
            pausado = true;
        }
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
        return clip != null && clip.isOpen();
    }

    public boolean estaPausada() {
        return pausado;
    }

    public boolean terminoCancion() {
        if (clip == null || !clip.isOpen()) {
            return false;
        }

        return clip.getMicrosecondLength() > 0
                && clip.getMicrosecondPosition()
                >= clip.getMicrosecondLength();
    }

    private void cerrarClipActual() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }

        posicionPausa = 0;
        pausado = false;
    }

    public void cerrar() {
        cerrarClipActual();
    }
}