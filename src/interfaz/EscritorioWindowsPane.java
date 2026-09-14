package interfaz;

import java.awt.*;
import java.io.File;
import javax.swing.*;

public class EscritorioWindowsPane extends JDesktopPane {

    private Image fondo;
    private Color colorFondo = new Color(18, 56, 92);

    public void setFondoArchivo(File archivo) {
        if (archivo == null || !archivo.exists()) {
            fondo = null;
        } else {
            fondo = new ImageIcon(archivo.getAbsolutePath()).getImage();
        }
        repaint();
    }

    public void setColorFondo(Color color) {
        if (color != null) {
            colorFondo = color;
            fondo = null;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(colorFondo);
        g2.fillRect(0, 0, getWidth(), getHeight());
        if (fondo != null) {
            double escala = Math.max((double) getWidth() / fondo.getWidth(null), (double) getHeight() / fondo.getHeight(null));
            int w = (int) (fondo.getWidth(null) * escala);
            int h = (int) (fondo.getHeight(null) * escala);
            int x = (getWidth() - w) / 2;
            int y = (getHeight() - h) / 2;
            g2.drawImage(fondo, x, y, w, h, null);
        } else {
            GradientPaint gp = new GradientPaint(0, 0, colorFondo.brighter(), getWidth(), getHeight(), colorFondo.darker());
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.dispose();
    }
}
