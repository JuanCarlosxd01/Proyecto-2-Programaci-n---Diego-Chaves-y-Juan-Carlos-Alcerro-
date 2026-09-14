package insta.interfaz;

import java.awt.Image;
import javax.swing.ImageIcon;

final class ImagenUI {

    private ImagenUI() {
    }

    static ImageIcon ajustar(byte[] datos, int anchoMax, int altoMax) {
        if (datos == null || datos.length == 0) return null;
        return ajustar(new ImageIcon(datos), anchoMax, altoMax);
    }

    static ImageIcon ajustar(ImageIcon original, int anchoMax, int altoMax) {
        if (original == null || original.getIconWidth() <= 0 || original.getIconHeight() <= 0) return null;
        double escala = Math.min((double) anchoMax / original.getIconWidth(), (double) altoMax / original.getIconHeight());
        int ancho = Math.max(1, (int) Math.round(original.getIconWidth() * escala));
        int alto = Math.max(1, (int) Math.round(original.getIconHeight() * escala));
        Image imagen = original.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        return new ImageIcon(imagen);
    }
}
