

package interfaz;

import insta.interfaz.ImagenTemporal;
import insta.servicio.ImagenesInsta;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import multimedia.LectorMetadataMP3;
import multimedia.MetadataCancion;
import multimedia.ReproductorMusica;
import sistema.RutasSistema;
import sistema.SeguridadArchivos;

public class ReproductorPanel extends JPanel {

    private final ReproductorMusica reproductor = new ReproductorMusica();

    private final ScheduledExecutorService hilo =
            Executors.newSingleThreadScheduledExecutor(tarea -> {
                Thread nuevoHilo = new Thread(tarea, "musica");
                nuevoHilo.setDaemon(true);
                return nuevoHilo;
            });

    private final DefaultListModel<File> modelo = new DefaultListModel<>();
    private final JList<File> canciones = new JList<>(modelo);

    private final JLabel titulo = new JLabel(
            "Tu música",
            SwingConstants.CENTER
    );

    private final JLabel descripcion = new JLabel(
            "Selecciona una canción",
            SwingConstants.CENTER
    );

    private final JLabel caratula = new JLabel(
            "♫",
            SwingConstants.CENTER
    );

    private final JSlider progreso = new JSlider(0, 1000);

    private File cargada;
    private volatile boolean cerrado;
    private volatile long duracion;
    private Runnable accionCerrar;

    public ReproductorPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(25, 25, 30));

        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));

        descripcion.setForeground(Color.WHITE);
        caratula.setForeground(Color.WHITE);
        caratula.setPreferredSize(new Dimension(250, 250));

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.add(caratula, BorderLayout.CENTER);
        centro.add(descripcion, BorderLayout.SOUTH);

        add(titulo, BorderLayout.NORTH);
        add(new JScrollPane(canciones), BorderLayout.WEST);
        add(centro, BorderLayout.CENTER);

        JPanel controles = new JPanel(new BorderLayout());
        JPanel botones = new JPanel();

        JButton play = new JButton("Play");
        JButton pause = new JButton("Pause");
        JButton stop = new JButton("Stop");
        JButton actualizar = new JButton("Actualizar");

        botones.add(play);
        botones.add(pause);
        botones.add(stop);
        botones.add(actualizar);

        controles.add(botones, BorderLayout.NORTH);
        controles.add(progreso, BorderLayout.SOUTH);

        add(controles, BorderLayout.SOUTH);

        canciones.setCellRenderer(
                (lista, archivo, indice, seleccionado, enfoque) -> {
                    JLabel etiqueta = new JLabel(archivo.getName());
                    etiqueta.setOpaque(true);

                    etiqueta.setBackground(
                            seleccionado
                                    ? lista.getSelectionBackground()
                                    : lista.getBackground()
                    );

                    etiqueta.setForeground(
                            seleccionado
                                    ? lista.getSelectionForeground()
                                    : lista.getForeground()
                    );

                    return etiqueta;
                }
        );

        play.addActionListener(e -> {
            File archivo = canciones.getSelectedValue();

            if (archivo != null) {
                reproducir(archivo);
            }
        });

        pause.addActionListener(e -> tarea(reproductor::pausar));
        stop.addActionListener(e -> tarea(reproductor::detener));
        actualizar.addActionListener(e -> actualizar());

        canciones.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2
                        && canciones.getSelectedValue() != null) {
                    reproducir(canciones.getSelectedValue());
                }
            }
        });

        progreso.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                long posicion = duracion * progreso.getValue() / 1000;
                tarea(() -> reproductor.cambiarPosicion(posicion));
            }
        });

        hilo.scheduleWithFixedDelay(() -> {
            duracion = reproductor.getDuracion();
            long posicion = reproductor.getPosicionActual();

            int valor = duracion == 0
                    ? 0
                    : (int) (posicion * 1000 / duracion);

            SwingUtilities.invokeLater(() -> {
                if (!cerrado && !progreso.getValueIsAdjusting()) {
                    progreso.setValue(valor);
                }
            });
        }, 0, 300, TimeUnit.MILLISECONDS);

        actualizar();
    }

    private void actualizar() {
        File carpeta = RutasSistema.getMusicaUsuarioActual();

        if (carpeta == null) {
            return;
        }

        tarea(() -> {
            File[] archivos = carpeta.listFiles(archivo ->
                    archivo.isFile()
                    && archivo.getName().toLowerCase()
                            .matches(".*\\.(mp3|wav|au|aiff|aif)$")
            );

            if (archivos == null) {
                archivos = new File[0];
            }

            Arrays.sort(
                    archivos,
                    Comparator.comparing(
                            File::getName,
                            String.CASE_INSENSITIVE_ORDER
                    )
            );

            File[] lista = archivos;

            SwingUtilities.invokeLater(() -> {
                if (cerrado) {
                    return;
                }

                modelo.clear();

                for (File archivo : lista) {
                    modelo.addElement(archivo);
                }
            });
        });
    }

    public void abrirArchivo(File archivo) {
        if (!SeguridadArchivos.esPermitido(archivo)) {
            return;
        }

        if (!modelo.contains(archivo)) {
            modelo.addElement(archivo);
        }

        canciones.setSelectedValue(archivo, true);
        reproducir(archivo);
    }

    private void reproducir(File archivo) {
        if (!SeguridadArchivos.esPermitido(archivo)) {
            return;
        }

        titulo.setText("Cargando...");

        tarea(() -> {
            if (!archivo.equals(cargada)) {
                cargada = null;
                reproductor.cargarCancion(archivo);
                cargada = archivo;
            }

            MetadataCancion metadata = LectorMetadataMP3.leer(archivo);

            if (cerrado) {
                reproductor.cerrar();
                return;
            }

            reproductor.reproducir();

            SwingUtilities.invokeLater(() -> {
                if (cerrado) {
                    return;
                }

                titulo.setText(metadata.getTitulo());

                descripcion.setText(
                        metadata.getArtista() + " — "
                        + metadata.getAlbum() + " — "
                        + metadata.getAnio()
                );

                ImagenTemporal.cargar(
                        caratula,
                        () -> metadata.getCaratula() != null
                                ? metadata.getCaratula()
                                : ImagenesInsta.dibujar(
                                        metadata.getTitulo(),
                                        0x425b76
                                ),
                        250,
                        250
                );
            });
        });
    }

    private interface Trabajo {
        void ejecutar() throws Exception;
    }

    private void tarea(Trabajo trabajo) {
        if (cerrado) {
            return;
        }

        hilo.execute(() -> {
            if (cerrado) {
                return;
            }

            try {
                trabajo.ejecutar();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    if (!cerrado) {
                        descripcion.setText(
                                "No se pudo completar: " + e.getMessage()
                        );
                    }
                });
            }
        });
    }

    public void cerrarReproductor() {
        if (cerrado) {
            return;
        }

        cerrado = true;
        hilo.execute(reproductor::cerrar);
        hilo.shutdown();
    }

    public void setAccionCerrar(Runnable accion) {
        accionCerrar = accion;
    }

    public void cerrar() {
        cerrarReproductor();

        if (accionCerrar != null) {
            accionCerrar.run();
        }
    }
}