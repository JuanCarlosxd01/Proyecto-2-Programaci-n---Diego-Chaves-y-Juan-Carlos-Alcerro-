package interfaz;

import java.awt.*;
import javax.swing.*;
import hilos.*;
import java.awt.event.*;
import java.io.*;
import java.util.Arrays;
import multimedia.*;
import sistema.*;

public class ReproductorPanel extends JPanel {

    private JLabel lblCaratula;
    private JLabel lblCancion;
    private JLabel lblDescripcion;

    private JButton btnPlay;
    private JButton btnPause;
    private JButton btnStop;
    private JButton btnActualizar;

    private JSlider progreso;
    private Runnable accionCerrar;

    private JList<String> listaCanciones;
    private DefaultListModel<String> modeloCanciones;

    private ReproductorMusica reproductor;
    private HiloReproductor hiloReproductor;
    private File carpetaMusica;
    private File[] canciones;
    private int indiceActual;
    private boolean moviendoSlider;

    public ReproductorPanel() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        reproductor = new ReproductorMusica();
        indiceActual = -1;
        moviendoSlider = false;

        crearInformacionCancion();
        crearControles();
        crearListaCanciones();
        cargarCarpetaMusica();
        configurarEventos();
        iniciarHilo();
    }

    private void crearInformacionCancion() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        lblCaratula = new JLabel("♫");
        lblCaratula.setHorizontalAlignment(SwingConstants.CENTER);
        lblCaratula.setFont(new Font("Arial", Font.PLAIN, 100));
        lblCaratula.setPreferredSize(new Dimension(250, 250));
        lblCaratula.setMaximumSize(new Dimension(250, 250));
        lblCaratula.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        lblCaratula.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblCancion = new JLabel("Ninguna canción seleccionada");
        lblCancion.setFont(new Font("Arial", Font.BOLD, 22));
        lblCancion.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblDescripcion = new JLabel("Seleccione una canción");
        lblDescripcion.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(lblCaratula);
        panel.add(Box.createVerticalStrut(20));
        panel.add(lblCancion);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblDescripcion);

        add(panel, BorderLayout.CENTER);
    }

    private void crearControles() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        progreso = new JSlider();
        progreso.setMinimum(0);
        progreso.setMaximum(100);
        progreso.setValue(0);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnPlay = new JButton("▶ Play");
        btnPause = new JButton("⏸ Pause");
        btnStop = new JButton("■ Stop");
        btnActualizar = new JButton("↻ Actualizar");

        botones.add(btnPlay);
        botones.add(btnPause);
        botones.add(btnStop);
        botones.add(btnActualizar);

        panel.add(progreso);
        panel.add(botones);

        add(panel, BorderLayout.SOUTH);
    }

    private void crearListaCanciones() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel titulo = new JLabel("Lista de canciones");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));

        modeloCanciones = new DefaultListModel<>();
        listaCanciones = new JList<>(modeloCanciones);
        listaCanciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(listaCanciones);

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        panel.setPreferredSize(new Dimension(300, 0));

        add(panel, BorderLayout.EAST);
    }

    private void cargarCarpetaMusica() {
        modeloCanciones.clear();
        indiceActual = -1;
        progreso.setValue(0);

        if (Sesion.getUsuarioActual() == null) {
            canciones = new File[0];
            lblDescripcion.setText("No hay una sesión iniciada");
            return;
        }

        String usuario = Sesion.getUsuarioActual().getUsername();
        carpetaMusica = new File("Z" + File.separator + usuario + File.separator + "Música");

        System.out.println("Usuario actual: " + usuario);
        System.out.println("Buscando música en: " + carpetaMusica.getAbsolutePath());
        System.out.println("Existe carpeta: " + carpetaMusica.exists());

        if (!carpetaMusica.exists()) {
            carpetaMusica.mkdirs();
        }

        canciones = carpetaMusica.listFiles(archivo -> {
            if (!archivo.isFile()) {
                return false;
            }

            String nombre = archivo.getName().toLowerCase();
            return nombre.endsWith(".wav") || nombre.endsWith(".au") || nombre.endsWith(".aiff") || nombre.endsWith(".aif") || nombre.endsWith(".mp3");
        });

        if (canciones == null) {
            canciones = new File[0];
        }

        System.out.println("Canciones encontradas: " + canciones.length);

        for (File archivo : canciones) {
            System.out.println("Canción: " + archivo.getAbsolutePath());
        }

        Arrays.sort(canciones, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        for (File cancion : canciones) {
            modeloCanciones.addElement(cancion.getName());
        }

        if (canciones.length == 0) {
            lblCancion.setText("Ninguna canción seleccionada");
            lblDescripcion.setText("No hay canciones en la carpeta Música");
        } else {
            lblDescripcion.setText(canciones.length + " canciones encontradas");
        }
    }

    private void configurarEventos() {
        btnPlay.addActionListener(e -> reproducirSeleccionada());

        btnPause.addActionListener(e -> {
            if (!reproductor.estaCargada()) {
                JOptionPane.showMessageDialog(this, "Seleccione una canción primero.");
                return;
            }

            reproductor.pausar();
            lblDescripcion.setText("Canción pausada");
        });

        btnStop.addActionListener(e -> {
            if (!reproductor.estaCargada()) {
                return;
            }

            reproductor.detener();
            progreso.setValue(0);
            lblDescripcion.setText("Reproducción detenida");
        });

        btnActualizar.addActionListener(e -> {
            reproductor.cerrar();
            progreso.setValue(0);
            lblCancion.setText("Ninguna canción seleccionada");
            cargarCarpetaMusica();
        });

        listaCanciones.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    reproducirSeleccionada();
                }
            }
        });

        progreso.addChangeListener(e -> {
            if (progreso.getValueIsAdjusting()) {
                moviendoSlider = true;
                return;
            }

            if (moviendoSlider && reproductor.estaCargada()) {
                long duracion = reproductor.getDuracion();
                long nuevaPosicion = (duracion * progreso.getValue()) / 100;

                reproductor.cambiarPosicion(nuevaPosicion);

                moviendoSlider = false;
            }
        });
    }

    private void reproducirSeleccionada() {
        if (canciones == null || canciones.length == 0) {
            JOptionPane.showMessageDialog(this, "No hay canciones compatibles en la carpeta Música.");
            return;
        }

        int seleccion = listaCanciones.getSelectedIndex();

        if (seleccion == -1) {
            if (indiceActual >= 0 && reproductor.estaCargada()) {
                reproductor.reproducir();
                lblDescripcion.setText("Reproduciendo");
                return;
            }

            seleccion = 0;
            listaCanciones.setSelectedIndex(0);
        }

        if (seleccion != indiceActual) {
            cargarCancion(seleccion);
        } else {
            reproductor.reproducir();
            lblDescripcion.setText("Reproduciendo");
        }
    }

    private void cargarCancion(int indice) {
        if (canciones == null || indice < 0 || indice >= canciones.length) {
            return;
        }

        try {
            File archivo = canciones[indice];

            reproductor.cargarCancion(archivo);

            indiceActual = indice;

            listaCanciones.setSelectedIndex(indice);

            lblCancion.setText(archivo.getName());
            lblDescripcion.setText("Reproduciendo");
            lblCaratula.setText("♫");

            progreso.setValue(0);

            reproductor.reproducir();

        } catch (Exception ex) {
            indiceActual = -1;

            progreso.setValue(0);

            lblCancion.setText("No se pudo cargar");
            lblDescripcion.setText("Formato no compatible");

            JOptionPane.showMessageDialog(this, "No se pudo reproducir la canción:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void siguienteCancion() {
        if (canciones == null || canciones.length == 0) {
            return;
        }

        if (indiceActual == -1) {
            cargarCancion(0);
            return;
        }

        int siguiente = indiceActual + 1;

        if (siguiente >= canciones.length) {
            siguiente = 0;
        }

        cargarCancion(siguiente);
    }

    private void iniciarHilo() {
        hiloReproductor = new HiloReproductor(reproductor, progreso);

        hiloReproductor.setAccionCancionTerminada(() -> {
            siguienteCancion();
        });

        hiloReproductor.start();
    }

    public void cerrarReproductor() {
        if (hiloReproductor != null) {
            hiloReproductor.detenerHilo();
            hiloReproductor = null;
        }

        reproductor.cerrar();

        if (accionCerrar != null) {
            accionCerrar.run();
        }
    }

    public JButton getBtnPlay() {
        return btnPlay;
    }

    public JButton getBtnPause() {
        return btnPause;
    }

    public JButton getBtnStop() {
        return btnStop;
    }

    public JList<String> getListaCanciones() {
        return listaCanciones;
    }

    public DefaultListModel<String> getModeloCanciones() {
        return modeloCanciones;
    }

    public JLabel getLblCaratula() {
        return lblCaratula;
    }

    public JLabel getLblCancion() {
        return lblCancion;
    }

    public JLabel getLblDescripcion() {
        return lblDescripcion;
    }

    public JSlider getProgreso() {
        return progreso;
    }

    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }

    public void cerrar() {
        cerrarReproductor();
    }
}