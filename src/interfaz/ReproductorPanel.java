package interfaz;

import hilos.HiloReproductor;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import multimedia.*;
import sistema.*;

public class ReproductorPanel extends JPanel {

    private static final Color FONDO = new Color(18, 18, 18);
    private static final Color PANEL = new Color(24, 24, 24);
    private static final Color PANEL_2 = new Color(32, 32, 32);
    private static final Color TEXTO = Color.WHITE;
    private static final Color TEXTO_SEC = new Color(180, 180, 180);
    private static final Color VERDE = new Color(29, 185, 84);

    private JLabel lblCaratula;
    private JLabel lblCancion;
    private JLabel lblDescripcion;
    private JButton btnPlay;
    private JButton btnPause;
    private JButton btnStop;
    private JButton btnActualizar;
    private JSlider progreso;
    private JLabel lblTiempoActual;
    private JLabel lblDuracion;
    private Runnable accionCerrar;
    private JList<String> listaCanciones;
    private DefaultListModel<String> modeloCanciones;
    private ReproductorMusica reproductor;
    private HiloReproductor hiloReproductor;
    private File carpetaMusica;
    private File[] canciones;
    private int indiceActual;
    private boolean moviendoSlider;
    private int solicitudMetadata;

    public ReproductorPanel() {
        reproductor = new ReproductorMusica();
        indiceActual = -1;
        moviendoSlider = false;
        setLayout(new BorderLayout());
        setBackground(FONDO);
        crearCabecera();
        crearListaCanciones();
        crearInformacionCancion();
        crearControles();
        cargarCarpetaMusica();
        configurarEventos();
        iniciarHilo();
    }

    private void crearCabecera() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(FONDO);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 10, 20));

        JLabel titulo = new JLabel("Tu música");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titulo.setForeground(TEXTO);

        btnActualizar = new JButton("Actualizar biblioteca");
        estilizarBotonOscuro(btnActualizar, PANEL_2, TEXTO);
        btnActualizar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(75, 75, 75)), BorderFactory.createEmptyBorder(9, 14, 9, 14)));

        header.add(titulo, BorderLayout.WEST);
        header.add(btnActualizar, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    private void crearListaCanciones() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 16, 18, 16));
        panel.setPreferredSize(new Dimension(310, 0));

        JLabel titulo = new JLabel("BIBLIOTECA");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titulo.setForeground(TEXTO_SEC);

        modeloCanciones = new DefaultListModel<>();
        listaCanciones = new JList<>(modeloCanciones);
        listaCanciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaCanciones.setBackground(PANEL);
        listaCanciones.setForeground(TEXTO);
        listaCanciones.setSelectionBackground(new Color(60, 60, 60));
        listaCanciones.setSelectionForeground(Color.WHITE);
        listaCanciones.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listaCanciones.setFixedCellHeight(38);
        listaCanciones.setBorder(BorderFactory.createEmptyBorder());

        JScrollPane scroll = new JScrollPane(listaCanciones);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PANEL);

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        add(panel, BorderLayout.WEST);
    }

    private void crearInformacionCancion() {
        JPanel centro = new JPanel(new GridBagLayout());
        centro.setBackground(FONDO);
        centro.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        lblCaratula = new JLabel("♫", SwingConstants.CENTER);
        lblCaratula.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 110));
        lblCaratula.setForeground(TEXTO_SEC);
        lblCaratula.setOpaque(true);
        lblCaratula.setBackground(PANEL_2);
        lblCaratula.setPreferredSize(new Dimension(280, 280));
        lblCaratula.setMaximumSize(new Dimension(280, 280));
        lblCaratula.setMinimumSize(new Dimension(280, 280));
        lblCaratula.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblCancion = new JLabel("Ninguna canción seleccionada", SwingConstants.CENTER);
        lblCancion.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblCancion.setForeground(TEXTO);
        lblCancion.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblDescripcion = new JLabel("Seleccione una canción de su biblioteca", SwingConstants.CENTER);
        lblDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDescripcion.setForeground(TEXTO_SEC);
        lblDescripcion.setAlignmentX(Component.CENTER_ALIGNMENT);

        contenido.add(lblCaratula);
        contenido.add(Box.createVerticalStrut(22));
        contenido.add(lblCancion);
        contenido.add(Box.createVerticalStrut(8));
        contenido.add(lblDescripcion);
        centro.add(contenido);
        add(centro, BorderLayout.CENTER);
    }

    private void crearControles() {
        JPanel inferior = new JPanel();
        inferior.setLayout(new BoxLayout(inferior, BoxLayout.Y_AXIS));
        inferior.setBackground(PANEL_2);
        inferior.setBorder(BorderFactory.createEmptyBorder(12, 30, 14, 30));

        progreso = new JSlider(0, 100, 0);
        progreso.setOpaque(false);
        progreso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        lblTiempoActual = new JLabel("0:00");
        lblTiempoActual.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTiempoActual.setForeground(TEXTO_SEC);
        lblDuracion = new JLabel("0:00");
        lblDuracion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDuracion.setForeground(TEXTO_SEC);

        JPanel lineaTiempo = new JPanel(new BorderLayout(10, 0));
        lineaTiempo.setOpaque(false);
        lineaTiempo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        lineaTiempo.add(lblTiempoActual, BorderLayout.WEST);
        lineaTiempo.add(progreso, BorderLayout.CENTER);
        lineaTiempo.add(lblDuracion, BorderLayout.EAST);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 5));
        botones.setOpaque(false);

        btnPause = botonControl("", false);
        btnPause.setIcon(new PauseIcon(14, 16, Color.WHITE));
        btnPause.setToolTipText("Pausar");
        btnPlay = botonControl("▶", true);
        btnPlay.setToolTipText("Reproducir");
        btnStop = botonControl("■", false);
        btnStop.setToolTipText("Detener");

        botones.add(btnPause);
        botones.add(btnPlay);
        botones.add(btnStop);

        inferior.add(lineaTiempo);
        inferior.add(botones);
        add(inferior, BorderLayout.SOUTH);
    }

    private JButton botonControl(String texto, boolean principal) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI Symbol", Font.BOLD, principal ? 22 : 17));
        estilizarBotonOscuro(boton, principal ? VERDE : new Color(48, 48, 48), principal ? Color.BLACK : Color.WHITE);
        boton.setPreferredSize(principal ? new Dimension(54, 44) : new Dimension(46, 38));
        boton.setBorder(BorderFactory.createLineBorder(principal ? VERDE.darker() : new Color(85, 85, 85)));
        return boton;
    }

    private void estilizarBotonOscuro(JButton boton, Color fondo, Color texto) {
        boton.setUI(new BasicButtonUI());
        boton.setFocusPainted(false);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorderPainted(true);
        boton.setBackground(fondo);
        boton.setForeground(texto);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void cargarCarpetaMusica() {
        modeloCanciones.clear();
        indiceActual = -1;
        progreso.setValue(0);
        actualizarTiempo(0, 0);

        if (Sesion.getUsuarioActual() == null) {
            canciones = new File[0];
            lblDescripcion.setText("No hay una sesión iniciada");
            return;
        }

        carpetaMusica = RutasSistema.getMusicaUsuarioActual();
        if (carpetaMusica == null) {
            canciones = new File[0];
            return;
        }
        if (!carpetaMusica.exists()) {
            carpetaMusica.mkdirs();
        }

        canciones = carpetaMusica.listFiles(archivo -> {
            if (!archivo.isFile()) return false;
            String nombre = archivo.getName().toLowerCase();
            return nombre.endsWith(".wav") || nombre.endsWith(".au") || nombre.endsWith(".aiff") || nombre.endsWith(".aif") || nombre.endsWith(".mp3");
        });

        if (canciones == null) canciones = new File[0];
        Arrays.sort(canciones, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        for (File cancion : canciones) modeloCanciones.addElement("  ♪  " + quitarExtension(cancion.getName()));

        if (canciones.length == 0) {
            limpiarInformacionCancion();
            lblDescripcion.setText("No hay canciones en la carpeta Música");
        } else {
            lblDescripcion.setText(canciones.length + " canciones disponibles");
        }
    }

    private void configurarEventos() {
        btnPlay.addActionListener(e -> reproducirSeleccionada());
        btnPause.addActionListener(e -> {
            if (!reproductor.estaCargada()) return;
            reproductor.pausar();
            if (indiceActual >= 0 && indiceActual < canciones.length) mostrarInformacionCancion(canciones[indiceActual]);
        });
        btnStop.addActionListener(e -> {
            if (!reproductor.estaCargada()) return;
            reproductor.detener();
            progreso.setValue(0);
            if (indiceActual >= 0 && indiceActual < canciones.length) mostrarInformacionCancion(canciones[indiceActual]);
        });
        btnActualizar.addActionListener(e -> {
            reproductor.cerrar();
            limpiarInformacionCancion();
            cargarCarpetaMusica();
        });
        listaCanciones.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int indice = listaCanciones.getSelectedIndex();
            if (indice < 0 || canciones == null || indice >= canciones.length) return;
            if (indice != indiceActual) cargarCancion(indice);
        });
        progreso.addChangeListener(e -> {
            if (progreso.getValueIsAdjusting()) {
                moviendoSlider = true;
                return;
            }
            if (moviendoSlider && reproductor.estaCargada()) {
                long nuevaPosicion = (reproductor.getDuracion() * progreso.getValue()) / 100;
                reproductor.cambiarPosicion(nuevaPosicion);
                moviendoSlider = false;
            }
        });
    }

    private void reproducirSeleccionada() {
        if (canciones == null || canciones.length == 0) {
            DialogosWindows.showMessageDialog(this, "No hay canciones compatibles en la carpeta Música.");
            return;
        }
        int seleccion = listaCanciones.getSelectedIndex();
        if (seleccion == -1) {
            if (indiceActual >= 0 && reproductor.estaCargada()) {
                reproductor.reproducir();
                mostrarInformacionCancion(canciones[indiceActual]);
                return;
            }
            seleccion = 0;
            listaCanciones.setSelectedIndex(0);
        }
        if (seleccion != indiceActual) cargarCancion(seleccion);
        else reproductor.reproducir();
    }

    private void cargarCancion(int indice) {
        if (canciones == null || indice < 0 || indice >= canciones.length) return;
        File archivo = canciones[indice];
        cargarArchivoEnSegundoPlano(archivo, indice);
    }

    public void reproducirArchivo(File archivo) {
        if (archivo == null || !archivo.exists() || !archivo.isFile()) {
            DialogosWindows.showMessageDialog(this, "El archivo de audio no existe.", "Música", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String nombre = archivo.getName().toLowerCase();
        if (!(nombre.endsWith(".mp3") || nombre.endsWith(".wav") || nombre.endsWith(".au") || nombre.endsWith(".aiff") || nombre.endsWith(".aif"))) {
            DialogosWindows.showMessageDialog(this, "El archivo seleccionado no es un audio compatible.", "Música", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int indice = -1;
        if (canciones != null) {
            for (int i = 0; i < canciones.length; i++) {
                try {
                    if (canciones[i].getCanonicalFile().equals(archivo.getCanonicalFile())) {
                        indice = i;
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        cargarArchivoEnSegundoPlano(archivo, indice);
    }

    private void cargarArchivoEnSegundoPlano(File archivo, int indiceBiblioteca) {
        btnPlay.setEnabled(false);
        btnPause.setEnabled(false);
        btnStop.setEnabled(false);
        lblCancion.setText("Cargando " + quitarExtension(archivo.getName()) + "...");
        lblDescripcion.setText("Preparando audio en segundo plano");

        SwingWorker<MetadataCancion, Void> worker = new SwingWorker<>() {
            @Override
            protected MetadataCancion doInBackground() throws Exception {
                reproductor.cargarCancion(archivo);
                if (archivo.getName().toLowerCase().endsWith(".mp3")) return LectorMetadataMP3.leer(archivo);
                MetadataCancion metadata = new MetadataCancion();
                metadata.setTitulo(quitarExtension(archivo.getName()));
                metadata.setArtista("Archivo de audio");
                metadata.setAlbum(archivo.getParentFile() == null ? "" : archivo.getParentFile().getName());
                return metadata;
            }

            @Override
            protected void done() {
                btnPlay.setEnabled(true);
                btnPause.setEnabled(true);
                btnStop.setEnabled(true);
                try {
                    MetadataCancion metadata = get();
                    indiceActual = indiceBiblioteca;
                    if (indiceBiblioteca >= 0) listaCanciones.setSelectedIndex(indiceBiblioteca);
                    mostrarInformacionMetadata(archivo, metadata);
                    progreso.setValue(0);
                    actualizarTiempo(0, reproductor.getDuracion());
                    reproductor.reproducir();
                } catch (Exception ex) {
                    indiceActual = -1;
                    progreso.setValue(0);
                    lblCancion.setText("No se pudo cargar");
                    lblDescripcion.setText("Formato no compatible: " + mensajeError(ex));
                    lblCaratula.setIcon(null);
                    lblCaratula.setText("♫");
                    DialogosWindows.showMessageDialog(ReproductorPanel.this, "No se pudo reproducir la canción:\n" + mensajeError(ex), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private String mensajeError(Exception e) {
        Throwable t = e;
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() == null ? "Error desconocido" : t.getMessage();
    }

    private void siguienteCancion() {
        if (canciones == null || canciones.length == 0) return;
        int siguiente = indiceActual < 0 ? 0 : (indiceActual + 1) % canciones.length;
        cargarCancion(siguiente);
    }

    private void iniciarHilo() {
        hiloReproductor = new HiloReproductor(reproductor, progreso);
        hiloReproductor.setAccionTiempoActualizado((actual, duracion) -> actualizarTiempo(actual, duracion));
        hiloReproductor.setAccionCancionTerminada(this::siguienteCancion);
        hiloReproductor.start();
    }

    public void cerrarReproductor() {
        if (hiloReproductor != null) {
            hiloReproductor.detenerHilo();
            hiloReproductor = null;
        }
        reproductor.cerrar();
        if (accionCerrar != null) accionCerrar.run();
    }

    private void mostrarInformacionCancion(File archivo) {
        if (archivo == null) {
            limpiarInformacionCancion();
            return;
        }
        String nombre = archivo.getName().toLowerCase();
        if (!nombre.endsWith(".mp3")) {
            lblCancion.setText(quitarExtension(archivo.getName()));
            lblDescripcion.setText("Archivo de audio");
            lblCaratula.setIcon(null);
            lblCaratula.setText("♫");
            return;
        }

        int numeroSolicitud = ++solicitudMetadata;
        lblCancion.setText(quitarExtension(archivo.getName()));
        lblDescripcion.setText("Leyendo carátula y metadatos...");
        SwingWorker<MetadataCancion, Void> trabajador = new SwingWorker<>() {
            @Override
            protected MetadataCancion doInBackground() {
                return LectorMetadataMP3.leer(archivo);
            }

            @Override
            protected void done() {
                if (numeroSolicitud != solicitudMetadata) return;
                try {
                    mostrarInformacionMetadata(archivo, get());
                } catch (Exception e) {
                    lblCancion.setText(quitarExtension(archivo.getName()));
                    lblDescripcion.setText("No se pudieron leer los metadatos");
                    lblCaratula.setIcon(null);
                    lblCaratula.setText("♫");
                }
            }
        };
        trabajador.execute();
    }

    private void mostrarInformacionMetadata(File archivo, MetadataCancion metadata) {
        lblCancion.setText(metadata.getTitulo());
        String descripcion = "<html><div style='text-align:center;color:#b3b3b3'><b>" + escapar(metadata.getArtista()) + "</b><br>" + escapar(metadata.getAlbum());
        if (!metadata.getAnio().isBlank()) descripcion += " · " + escapar(metadata.getAnio());
        if (!metadata.getDescripcion().isBlank()) descripcion += "<br><span style='font-size:11px'>" + escapar(metadata.getDescripcion()) + "</span>";
        descripcion += "</div></html>";
        lblDescripcion.setText(descripcion);
        mostrarCaratula(metadata.getCaratula());
    }

    private void mostrarCaratula(byte[] datosImagen) {
        if (datosImagen == null || datosImagen.length == 0) {
            lblCaratula.setIcon(null);
            lblCaratula.setText("♫");
            return;
        }
        ImageIcon original = new ImageIcon(datosImagen);
        int w = Math.max(1, original.getIconWidth());
        int h = Math.max(1, original.getIconHeight());
        double escala = Math.min(280.0 / w, 280.0 / h);
        Image imagen = original.getImage().getScaledInstance(Math.max(1, (int)(w * escala)), Math.max(1, (int)(h * escala)), Image.SCALE_SMOOTH);
        lblCaratula.setText("");
        lblCaratula.setIcon(new ImageIcon(imagen));
    }

    private void limpiarInformacionCancion() {
        lblCancion.setText("Ninguna canción seleccionada");
        lblDescripcion.setText("Seleccione una canción de su biblioteca");
        lblCaratula.setIcon(null);
        lblCaratula.setText("♫");
    }

    private void actualizarTiempo(long actualMicrosegundos, long duracionMicrosegundos) {
        if (lblTiempoActual == null || lblDuracion == null) return;
        lblTiempoActual.setText(formatearTiempo(actualMicrosegundos));
        lblDuracion.setText(formatearTiempo(duracionMicrosegundos));
    }

    private String formatearTiempo(long microsegundos) {
        long totalSegundos = Math.max(0, microsegundos / 1_000_000L);
        long minutos = totalSegundos / 60;
        long segundos = totalSegundos % 60;
        return String.format("%d:%02d", minutos, segundos);
    }

    private static class PauseIcon implements Icon {
        private final int ancho;
        private final int alto;
        private final Color color;

        PauseIcon(int ancho, int alto, Color color) {
            this.ancho = ancho;
            this.alto = alto;
            this.color = color;
        }

        @Override
        public int getIconWidth() { return ancho; }

        @Override
        public int getIconHeight() { return alto; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(color);
            int barra = Math.max(3, ancho / 3);
            g2.fillRoundRect(x, y, barra, alto, 2, 2);
            g2.fillRoundRect(x + ancho - barra, y, barra, alto, 2, 2);
            g2.dispose();
        }
    }

    private String quitarExtension(String nombre) {
        int punto = nombre.lastIndexOf('.');
        return punto > 0 ? nombre.substring(0, punto) : nombre;
    }

    private String escapar(String texto) {
        if (texto == null) return "";
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public JButton getBtnPlay() { return btnPlay; }
    public JButton getBtnPause() { return btnPause; }
    public JButton getBtnStop() { return btnStop; }
    public JList<String> getListaCanciones() { return listaCanciones; }
    public DefaultListModel<String> getModeloCanciones() { return modeloCanciones; }
    public JLabel getLblCaratula() { return lblCaratula; }
    public JLabel getLblCancion() { return lblCancion; }
    public JLabel getLblDescripcion() { return lblDescripcion; }
    public JSlider getProgreso() { return progreso; }
    public void setAccionCerrar(Runnable accionCerrar) { this.accionCerrar = accionCerrar; }
    public void cerrar() { cerrarReproductor(); }
}
