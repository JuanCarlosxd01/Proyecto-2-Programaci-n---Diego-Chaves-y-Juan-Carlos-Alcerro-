package interfaz;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class VisorImagenPanel extends JPanel {

    private JLabel lblImagen;
    private JLabel lblNombre;
    private JButton btnAnterior;
    private JButton btnSiguiente;
    private JPanel panelMiniaturas;
    private JPanel panelVisor;
    private JScrollPane scrollMiniaturas;
    private ArrayList<File> imagenes;
    private int indiceActual;
    private Runnable accionCerrar;

    public VisorImagenPanel(File carpeta) {
        imagenes = new ArrayList<>();
        indiceActual = 0;

        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 30));

        crearInterfaz();
        cargarImagenes(carpeta);
        crearMiniaturas();

        if (!imagenes.isEmpty()) {
            mostrarImagen();
        } else {
            lblImagen.setText("No hay imágenes en esta carpeta.");
            lblImagen.setForeground(Color.WHITE);
            btnAnterior.setEnabled(false);
            btnSiguiente.setEnabled(false);
        }
    }

    private void crearInterfaz() {
        lblNombre = new JLabel("Visor de imágenes", SwingConstants.CENTER);
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblNombre.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(lblNombre, BorderLayout.NORTH);

        panelVisor = new JPanel(new BorderLayout());
        panelVisor.setBackground(new Color(20, 20, 20));

        lblImagen = new JLabel("", SwingConstants.CENTER);
        lblImagen.setForeground(Color.WHITE);

        panelVisor.add(lblImagen, BorderLayout.CENTER);
        add(panelVisor, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(new Color(35, 35, 35));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        panelBotones.setOpaque(false);

        btnAnterior = new JButton("◀ Anterior");
        btnSiguiente = new JButton("Siguiente ▶");

        btnAnterior.setFocusPainted(false);
        btnSiguiente.setFocusPainted(false);

        panelBotones.add(btnAnterior);
        panelBotones.add(btnSiguiente);

        panelInferior.add(panelBotones, BorderLayout.NORTH);

        panelMiniaturas = new JPanel();
        panelMiniaturas.setLayout(new BoxLayout(panelMiniaturas, BoxLayout.X_AXIS));
        panelMiniaturas.setBackground(new Color(25, 25, 25));
        panelMiniaturas.setBorder(new EmptyBorder(8, 8, 8, 8));

        scrollMiniaturas = new JScrollPane(panelMiniaturas);
        scrollMiniaturas.setPreferredSize(new Dimension(0, 125));
        scrollMiniaturas.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollMiniaturas.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollMiniaturas.getHorizontalScrollBar().setUnitIncrement(15);
        scrollMiniaturas.setBorder(null);

        panelInferior.add(scrollMiniaturas, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        btnAnterior.addActionListener(e -> anterior());
        btnSiguiente.addActionListener(e -> siguiente());

        panelVisor.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (!imagenes.isEmpty()) {
                    mostrarImagen();
                }
            }
        });
    }

    private void cargarImagenes(File carpeta) {
        imagenes.clear();

        if (carpeta == null) {
            return;
        }

        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        if (!carpeta.isDirectory()) {
            return;
        }

        File[] archivos = carpeta.listFiles();

        if (archivos == null) {
            return;
        }

        Arrays.sort(archivos, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        for (File archivo : archivos) {
            if (archivo.isFile() && esImagen(archivo)) {
                imagenes.add(archivo);
            }
        }
    }

    private boolean esImagen(File archivo) {
        String nombre = archivo.getName().toLowerCase();

        return nombre.endsWith(".png") || nombre.endsWith(".jpg") || nombre.endsWith(".jpeg") || nombre.endsWith(".gif") || nombre.endsWith(".bmp");
    }

    private void crearMiniaturas() {
        panelMiniaturas.removeAll();

        if (imagenes.isEmpty()) {
            JLabel mensaje = new JLabel("No hay imágenes");
            mensaje.setForeground(Color.LIGHT_GRAY);
            panelMiniaturas.add(mensaje);
            panelMiniaturas.revalidate();
            panelMiniaturas.repaint();
            return;
        }

        for (int i = 0; i < imagenes.size(); i++) {
            File archivo = imagenes.get(i);
            JButton miniatura = crearMiniatura(archivo, i);

            panelMiniaturas.add(miniatura);
            panelMiniaturas.add(Box.createHorizontalStrut(10));
        }

        panelMiniaturas.revalidate();
        panelMiniaturas.repaint();
    }

    private JButton crearMiniatura(File archivo, int indice) {
        JButton boton = new JButton();

        boton.setPreferredSize(new Dimension(100, 95));
        boton.setMinimumSize(new Dimension(100, 95));
        boton.setMaximumSize(new Dimension(100, 95));
        boton.setFocusPainted(false);
        boton.setBackground(new Color(45, 45, 45));
        boton.setToolTipText(archivo.getName());

        try {
            BufferedImage original = ImageIO.read(archivo);

            if (original != null) {
                Image imagenMiniatura = escalarImagen(original, 85, 75);
                boton.setIcon(new ImageIcon(imagenMiniatura));
            }
        } catch (IOException e) {
            boton.setText("?");
        }

        boton.addActionListener(e -> {
            indiceActual = indice;
            mostrarImagen();
        });

        return boton;
    }

    private void mostrarImagen() {
        if (imagenes.isEmpty()) {
            return;
        }

        if (indiceActual < 0 || indiceActual >= imagenes.size()) {
            return;
        }

        File archivo = imagenes.get(indiceActual);

        try {
            BufferedImage original = ImageIO.read(archivo);

            if (original == null) {
                lblImagen.setIcon(null);
                lblImagen.setText("No se pudo cargar la imagen.");
                return;
            }

            int ancho = panelVisor.getWidth() - 40;
            int alto = panelVisor.getHeight() - 40;

            if (ancho <= 100) {
                ancho = 700;
            }

            if (alto <= 100) {
                alto = 450;
            }

            Image imagenEscalada = escalarImagen(original, ancho, alto);

            lblImagen.setText("");
            lblImagen.setIcon(new ImageIcon(imagenEscalada));
            lblNombre.setText(archivo.getName() + "     " + (indiceActual + 1) + " de " + imagenes.size());

            actualizarSeleccionMiniaturas();

        } catch (IOException e) {
            lblImagen.setIcon(null);
            lblImagen.setText("Error al abrir " + archivo.getName());
        }
    }

    private Image escalarImagen(BufferedImage original, int anchoMaximo, int altoMaximo) {
        int anchoOriginal = original.getWidth();
        int altoOriginal = original.getHeight();

        double escalaAncho = (double) anchoMaximo / anchoOriginal;
        double escalaAlto = (double) altoMaximo / altoOriginal;
        double escala = Math.min(escalaAncho, escalaAlto);

        if (escala > 1) {
            escala = 1;
        }

        int nuevoAncho = (int) (anchoOriginal * escala);
        int nuevoAlto = (int) (altoOriginal * escala);

        return original.getScaledInstance(nuevoAncho, nuevoAlto, Image.SCALE_SMOOTH);
    }

    private void siguiente() {
        if (imagenes.isEmpty()) {
            return;
        }

        indiceActual++;

        if (indiceActual >= imagenes.size()) {
            indiceActual = 0;
        }

        mostrarImagen();
    }

    private void anterior() {
        if (imagenes.isEmpty()) {
            return;
        }

        indiceActual--;

        if (indiceActual < 0) {
            indiceActual = imagenes.size() - 1;
        }

        mostrarImagen();
    }

    private void actualizarSeleccionMiniaturas() {
        int numeroBoton = 0;

        for (Component componente : panelMiniaturas.getComponents()) {
            if (componente instanceof JButton) {
                JButton boton = (JButton) componente;

                if (numeroBoton == indiceActual) {
                    boton.setBorder(new LineBorder(new Color(0, 120, 215), 3));
                } else {
                    boton.setBorder(new LineBorder(Color.GRAY, 1));
                }

                numeroBoton++;
            }
        }
    }

    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }

    public void cerrar() {
        if (accionCerrar != null) {
            accionCerrar.run();
        }
    }
}