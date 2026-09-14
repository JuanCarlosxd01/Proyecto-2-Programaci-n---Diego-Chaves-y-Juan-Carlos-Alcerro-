
package pokemon.interfaz;

import java.awt.*;
import java.io.InputStream;

public class FuentePokemon {

    private static Font fuentePokemon;

    public static Font obtenerFuente(float tamaño) {
        try {
            if (fuentePokemon == null) {
                InputStream archivo = FuentePokemon.class.getResourceAsStream("/Imagenes/pokemon-emerald.otf");

                if (archivo == null) {
                    System.out.println("No se encontró la fuente pokemon-emerald.otf");
                    return new Font("Arial", Font.BOLD, (int) tamaño);
                }

                fuentePokemon = Font.createFont(Font.TRUETYPE_FONT, archivo);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(fuentePokemon);

                archivo.close();
            }

            return fuentePokemon.deriveFont(Font.PLAIN, tamaño);

        } catch (Exception e) {
            System.out.println("Error cargando fuente Pokémon: " + e.getMessage());
            return new Font("Arial", Font.BOLD, (int) tamaño);
        }
    }
}