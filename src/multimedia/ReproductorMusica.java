package multimedia;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;

public class ReproductorMusica {

    private Clip clip;
    private File archivoActual;
    private boolean mp3;
    private volatile AdvancedPlayer mp3Player;
    private volatile Thread mp3Thread;
    private volatile boolean reproduciendoMp3;
    private volatile boolean pausado;
    private volatile long posicionMp3Ms;
    private volatile long inicioReproduccionMs;
    private long duracionMp3Ms;
    private int framesMp3;
    private volatile int generacionMp3;
    private long posicionPausa;

    public ReproductorMusica() {
        posicionPausa = 0;
    }

    public synchronized void cargarCancion(File archivo) throws Exception {
        if (archivo == null || !archivo.exists()) throw new IllegalArgumentException("El archivo no existe.");
        cerrarActual();
        archivoActual = archivo;
        mp3 = archivo.getName().toLowerCase().endsWith(".mp3");
        if (mp3) {
            analizarMp3ConJLayer(archivo);
            posicionMp3Ms = 0;
            pausado = false;
            return;
        }

        AudioInputStream original = AudioSystem.getAudioInputStream(archivo);
        AudioFormat f = original.getFormat();
        AudioFormat pcm = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, f.getSampleRate(), 16, f.getChannels(), f.getChannels() * 2, f.getSampleRate(), false);
        AudioInputStream decoded = AudioSystem.getAudioInputStream(pcm, original);
        clip = AudioSystem.getClip();
        clip.open(decoded);
        decoded.close();
        original.close();
        posicionPausa = 0;
        pausado = false;
    }

    private void analizarMp3ConJLayer(File archivo) throws Exception {
        int cantidadFrames = 0;
        double duracionMs = 0;
        try (BufferedInputStream entrada = new BufferedInputStream(new FileInputStream(archivo))) {
            Bitstream bitstream = new Bitstream(entrada);
            try {
                Header header;
                while ((header = bitstream.readFrame()) != null) {
                    cantidadFrames++;
                    duracionMs += header.ms_per_frame();
                    bitstream.closeFrame();
                }
            } finally {
                try { bitstream.close(); } catch (Exception ignored) {}
            }
        }
        if (cantidadFrames <= 0 || duracionMs <= 0) {
            throw new IllegalArgumentException("El archivo no contiene frames MP3 válidos. Puede tener extensión .mp3 pero ser otro formato.");
        }
        framesMp3 = cantidadFrames;
        duracionMp3Ms = Math.max(1, Math.round(duracionMs));
    }

    public synchronized void reproducir() {
        if (mp3) {
            iniciarMp3Desde(posicionMp3Ms);
            return;
        }
        if (clip == null) return;
        if (clip.getMicrosecondPosition() >= clip.getMicrosecondLength()) clip.setMicrosecondPosition(0);
        if (pausado) clip.setMicrosecondPosition(posicionPausa);
        clip.start();
        pausado = false;
    }

    private synchronized void iniciarMp3Desde(long desdeMs) {
        if (archivoActual == null || reproduciendoMp3) return;
        if (desdeMs >= duracionMp3Ms) desdeMs = 0;
        posicionMp3Ms = Math.max(0, desdeMs);
        int frameInicio = (int) Math.min(framesMp3 - 1L, (posicionMp3Ms * framesMp3) / duracionMp3Ms);
        inicioReproduccionMs = System.currentTimeMillis() - posicionMp3Ms;
        reproduciendoMp3 = true;
        pausado = false;
        int generacion = ++generacionMp3;
        File archivoReproduccion = archivoActual;
        mp3Thread = new Thread(() -> {
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(archivoReproduccion))) {
                AdvancedPlayer player = new AdvancedPlayer(in);
                synchronized (ReproductorMusica.this) {
                    if (generacion != generacionMp3) {
                        player.close();
                        return;
                    }
                    mp3Player = player;
                }
                player.play(frameInicio, Integer.MAX_VALUE);
            } catch (Exception e) {
                if (reproduciendoMp3) System.err.println("Error reproduciendo MP3: " + e.getMessage());
            } finally {
                synchronized (ReproductorMusica.this) {
                    if (generacion == generacionMp3) {
                        if (reproduciendoMp3) posicionMp3Ms = duracionMp3Ms;
                        reproduciendoMp3 = false;
                        mp3Player = null;
                    }
                }
            }
        }, "Reproductor-MP3");
        mp3Thread.setDaemon(true);
        mp3Thread.start();
    }

    public synchronized void pausar() {
        if (mp3) {
            if (!reproduciendoMp3) return;
            posicionMp3Ms = Math.min(duracionMp3Ms, Math.max(0, System.currentTimeMillis() - inicioReproduccionMs));
            generacionMp3++;
            reproduciendoMp3 = false;
            pausado = true;
            AdvancedPlayer p = mp3Player;
            if (p != null) p.close();
            return;
        }
        if (clip != null && clip.isRunning()) {
            posicionPausa = clip.getMicrosecondPosition();
            clip.stop();
            pausado = true;
        }
    }

    public synchronized void detener() {
        if (mp3) {
            generacionMp3++;
            reproduciendoMp3 = false;
            pausado = false;
            posicionMp3Ms = 0;
            AdvancedPlayer p = mp3Player;
            if (p != null) p.close();
            return;
        }
        if (clip == null) return;
        clip.stop();
        clip.setMicrosecondPosition(0);
        posicionPausa = 0;
        pausado = false;
    }

    public synchronized void cambiarPosicion(long posicionMicrosegundos) {
        if (mp3) {
            long ms = Math.max(0, Math.min(duracionMp3Ms, posicionMicrosegundos / 1000));
            boolean seguir = reproduciendoMp3;
            generacionMp3++;
            reproduciendoMp3 = false;
            AdvancedPlayer p = mp3Player;
            if (p != null) p.close();
            posicionMp3Ms = ms;
            pausado = !seguir;
            if (seguir) iniciarMp3Desde(ms);
            return;
        }
        if (clip == null) return;
        long posicion = Math.max(0, Math.min(clip.getMicrosecondLength(), posicionMicrosegundos));
        boolean estaba = clip.isRunning();
        clip.stop();
        clip.setMicrosecondPosition(posicion);
        posicionPausa = posicion;
        if (estaba) { clip.start(); pausado = false; } else pausado = true;
    }

    public long getPosicionActual() {
        if (mp3) {
            long ms = reproduciendoMp3 ? System.currentTimeMillis() - inicioReproduccionMs : posicionMp3Ms;
            return Math.min(duracionMp3Ms, Math.max(0, ms)) * 1000;
        }
        return clip == null ? 0 : clip.getMicrosecondPosition();
    }

    public long getDuracion() { return mp3 ? duracionMp3Ms * 1000 : clip == null ? 0 : clip.getMicrosecondLength(); }
    public boolean estaReproduciendo() { return mp3 ? reproduciendoMp3 : clip != null && clip.isRunning(); }
    public boolean estaCargada() { return mp3 ? archivoActual != null : clip != null && clip.isOpen(); }
    public boolean estaPausada() { return pausado; }
    public boolean terminoCancion() { return estaCargada() && getDuracion() > 0 && getPosicionActual() >= getDuracion(); }

    private synchronized void cerrarActual() {
        generacionMp3++;
        reproduciendoMp3 = false;
        AdvancedPlayer p = mp3Player;
        if (p != null) p.close();
        mp3Player = null;
        if (clip != null) { clip.stop(); clip.close(); clip = null; }
        archivoActual = null;
        mp3 = false;
        posicionMp3Ms = 0;
        duracionMp3Ms = 0;
        framesMp3 = 0;
        posicionPausa = 0;
        pausado = false;
    }

    public void cerrar() { cerrarActual(); }
}
