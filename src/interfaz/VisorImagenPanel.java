
package interfaz;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;

public class VisorImagenPanel extends JPanel {
    private JLabel lblImagen;
    private JLabel lblNombre;
    private JButton btnAnterior;
    private JButton btnSiguiente;
    private Runnable accionCerrar;

    private ArrayList<File> imagenes;
    private int indiceActual;

    public VisorImagenPanel(File carpeta) {
        setLayout(new BorderLayout(10, 10));

        imagenes = new ArrayList<>();
        indiceActual = 0;

        crearInterfaz();
        cargarImagenes(carpeta);

        if (!imagenes.isEmpty()) {
            mostrarImagen();
        } else {
            lblImagen.setText("No hay imágenes en esta carpeta.");
            btnAnterior.setEnabled(false);
            btnSiguiente.setEnabled(false);
        }
    }

    private void crearInterfaz() {
        lblNombre = new JLabel("Visor de imágenes", SwingConstants.CENTER);
        lblNombre.setFont(new Font("Arial", Font.BOLD, 18));

        lblImagen = new JLabel("", SwingConstants.CENTER);

        btnAnterior = new JButton("Anterior");
        btnSiguiente = new JButton("Siguiente");

        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.add(btnAnterior);
        panelBotones.add(btnSiguiente);

        add(lblNombre, BorderLayout.NORTH);
        add(new JScrollPane(lblImagen), BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        btnAnterior.addActionListener(e -> anterior());
        btnSiguiente.addActionListener(e -> siguiente());
    }

    private void cargarImagenes(File carpeta) {
        if (carpeta == null || !carpeta.isDirectory()) {
            return;
        }

        File[] archivos = carpeta.listFiles();

        if (archivos == null) {
            return;
        }

        Arrays.sort(archivos);

        for (File archivo : archivos) {
            if (archivo.isFile() && esImagen(archivo)) {
                imagenes.add(archivo);
            }
        }
    }

    private boolean esImagen(File archivo) {
        String nombre = archivo.getName().toLowerCase();

        return nombre.endsWith(".png") || nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")  || nombre.endsWith(".gif") || nombre.endsWith(".bmp");
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

    private void mostrarImagen() {
        File archivo = imagenes.get(indiceActual);

        ImageIcon iconoOriginal = new ImageIcon(archivo.getAbsolutePath());
        Image imagenOriginal = iconoOriginal.getImage();

        int anchoMaximo = 800;
        int altoMaximo = 500;

        int anchoOriginal = iconoOriginal.getIconWidth();
        int altoOriginal = iconoOriginal.getIconHeight();

        if (anchoOriginal <= 0 || altoOriginal <= 0) {
            lblImagen.setIcon(null);
            lblImagen.setText("No se pudo cargar la imagen.");
            return;
        }

        double escalaAncho = (double) anchoMaximo / anchoOriginal;
        double escalaAlto = (double) altoMaximo / altoOriginal;
        double escala = Math.min(escalaAncho, escalaAlto);

        if (escala > 1) {
            escala = 1;
        }

        int nuevoAncho = (int) (anchoOriginal * escala);
        int nuevoAlto = (int) (altoOriginal * escala);

        Image imagenEscalada = imagenOriginal.getScaledInstance(nuevoAncho, nuevoAlto, Image.SCALE_SMOOTH);

        lblImagen.setText("");
        lblImagen.setIcon(new ImageIcon(imagenEscalada));

        lblNombre.setText(archivo.getName() + "   (" + (indiceActual + 1) + " de " + imagenes.size() + ")");
    }
    
    public void setAccionCerrar(Runnable accionCerrar){
        this.accionCerrar = accionCerrar;
    }
    
    public void cerrar(){
        if(accionCerrar != null){
            accionCerrar.run();
        }
    }
}