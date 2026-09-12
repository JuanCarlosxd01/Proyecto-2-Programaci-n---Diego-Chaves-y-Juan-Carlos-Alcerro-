
package insta.interfaz;

import java.awt.Color;

public class TemaInsta {

    public static boolean oscuro;

    public static Color FONDO;
    public static Color FONDO_SECUNDARIO;
    public static Color TARJETA;
    public static Color TEXTO;
    public static Color TEXTO_SECUNDARIO;
    public static Color BORDE;
    public static Color INPUT;
    public static Color BOTON;
    public static Color BOTON_TEXTO;
    public static Color HOVER;

    static {
        cambiarTema(false);
    }

    public static void cambiarTema(boolean modoOscuro) {
        oscuro = modoOscuro;

        if (oscuro) {
            FONDO = new Color(0, 0, 0);
            FONDO_SECUNDARIO = new Color(18, 18, 18);
            TARJETA = new Color(24, 24, 24);
            TEXTO = new Color(245, 245, 245);
            TEXTO_SECUNDARIO = new Color(168, 168, 168);
            BORDE = new Color(45, 45, 45);
            INPUT = new Color(38, 38, 38);
            BOTON = new Color(0, 149, 246);
            BOTON_TEXTO = Color.WHITE;
            HOVER = new Color(30, 30, 30);
        } else {
            FONDO = Color.WHITE;
            FONDO_SECUNDARIO = new Color(250, 250, 250);
            TARJETA = Color.WHITE;
            TEXTO = new Color(20, 20, 20);
            TEXTO_SECUNDARIO = new Color(110, 110, 110);
            BORDE = new Color(220, 220, 220);
            INPUT = new Color(239, 239, 239);
            BOTON = new Color(0, 149, 246);
            BOTON_TEXTO = Color.WHITE;
            HOVER = new Color(245, 245, 245);
        }
    }
}