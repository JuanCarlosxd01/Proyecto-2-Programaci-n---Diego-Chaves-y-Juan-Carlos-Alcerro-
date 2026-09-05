
package EditorTexto;
import java.awt.*;
public class Formato {
    public static final byte NEGRITA = 1;
    public static final byte CURSIVA = 2;
    public static final byte SUBRAYADO = 4;
    public static final byte TACHADO = 8;
    
    
    private String fuente;
    private int tamano;
    private byte estilo;
    private int colorArgb;

    public Formato(String fuente, int tamano, byte estilo, int colorArgb) {
        this.fuente = fuente;
        this.tamano = tamano;
        this.estilo = estilo;
        this.colorArgb = colorArgb;
    }
    
    public static Formato desde(String fuente, int tamano, boolean negrita, boolean cursiva, boolean subrayado, boolean tachado, Color color){
    byte estilo = 0;
        if (negrita) {
            estilo |= NEGRITA;
        }
        if (cursiva) {
            estilo |= CURSIVA;
        }
        if (subrayado) {
            estilo |= SUBRAYADO;
        }
        if (tachado) {
            estilo |= TACHADO;
        }
        return new Formato(fuente, tamano, estilo, color.getRGB());
    }

    public String getFuente() {
        return fuente;
    }

    public int getTamano() {
        return tamano;
    }

    public byte getEstilo() {
        return estilo;
    }

    public int getColorArgb() {
        return colorArgb;
    }
    
    public boolean esNegrita(){
    return (estilo & NEGRITA) != 0;
    }
    public boolean esCursiva(){
    return (estilo & CURSIVA) != 0;
    }
    public boolean esSubrayado(){
    return (estilo & SUBRAYADO)!= 0;
    }
    
    public boolean esTachado(){
    return (estilo & TACHADO) != 0;
    }
    
    public Color getColor(){
    return new Color(colorArgb, true);
    }
    
    public boolean equivale(Formato otro) {
        if (otro == null) return false;
        return this.fuente.equals(otro.fuente)
            && this.tamano == otro.tamano
            && this.estilo == otro.estilo
            && this.colorArgb == otro.colorArgb;
    }
    
}
