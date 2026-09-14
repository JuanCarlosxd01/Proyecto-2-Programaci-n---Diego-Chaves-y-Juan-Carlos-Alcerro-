package interfaz;

import java.awt.*;
import java.io.File;
import javax.swing.*;

public class EscritorioWindowsPane extends JDesktopPane {

    private Image fondo;
    private File fondoArchivo;
    private Color colorFondo = new Color(18, 56, 92);

    public EscritorioWindowsPane() {
        setOpaque(true);
    }

    public void setFondoArchivo(File archivo) {
        if (archivo == null || !archivo.exists()) {
            return;
        }

        fondoArchivo = archivo;

        ImageIcon icono = new ImageIcon(archivo.getAbsolutePath());
        fondo = icono.getImage();

        revalidate();
        repaint();
    }

    public void setColorFondo(Color color) {
        if (color == null) {
            return;
        }

        colorFondo = color;
        fondo = null;
        fondoArchivo = null;

        revalidate();
        repaint();
    }

    public File getFondoArchivo() {
        return fondoArchivo;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        if (fondo != null) {
            int anchoImagen = fondo.getWidth(this);
            int altoImagen = fondo.getHeight(this);

            if (anchoImagen > 0 && altoImagen > 0) {
                double escala = Math.max((double) getWidth() / anchoImagen, (double) getHeight() / altoImagen);

                int ancho = (int) (anchoImagen * escala);
                int alto = (int) (altoImagen * escala);

                int x = (getWidth() - ancho) / 2;
                int y = (getHeight() - alto) / 2;

                g2.drawImage(fondo, x, y, ancho, alto, this);
            }
        } else {
            GradientPaint gp = new GradientPaint(0, 0, colorFondo.brighter(), getWidth(), getHeight(), colorFondo.darker());
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        g2.dispose();
    }
}