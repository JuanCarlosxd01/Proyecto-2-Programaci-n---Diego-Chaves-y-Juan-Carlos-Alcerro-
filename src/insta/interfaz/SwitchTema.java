
package insta.interfaz;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class SwitchTema extends JComponent{

    private boolean activado = false;

    public SwitchTema() {
        setPreferredSize(new Dimension(52, 28));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                activado = !activado;
                repaint();
                firePropertyChange("activado", !activado, activado);
            }
        });
    }

    public boolean isActivado() {
        return activado;
    }

    public void setActivado(boolean activado) {
        this.activado = activado;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int ancho = getWidth();
        int alto = getHeight();

        if (activado) {
            g2.setColor(new Color(0, 149, 246));
        } else {
            g2.setColor(new Color(190, 190, 190));
        }

        g2.fillRoundRect(0, 0, ancho, alto, alto, alto);

        g2.setColor(Color.WHITE);

        int diametro = alto - 6;

        int x;

        if (activado) {
            x = ancho - diametro - 3;
        } else {
            x = 3;
        }

        g2.fillOval(x, 3, diametro, diametro);

        g2.dispose();
    }
}
