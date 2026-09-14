
package pokemon.interfaz;

import java.awt.*;
import java.net.URL;
import javax.swing.*;

public class FondoPokemonPanel extends JPanel {

    private Image fondo;

    public FondoPokemonPanel() {
        cargarFondo();
    }

    private void cargarFondo() {
        URL recurso = FondoPokemonPanel.class.getResource("/Imagenes/FondoBatalla.png");

        if (recurso != null) {
            fondo = new ImageIcon(recurso).getImage();
        } else {
            System.out.println("No se encontró /Imagenes/FondoBatalla.png");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (fondo != null) {
            g.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
        }
    }
}