package pokemon.memoria;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import pokemon.interfaz.VentanaPokemon;

public class Memoria {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pokémon Battle");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 750);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new VentanaPokemon());
            frame.setVisible(true);
        });
    }
}
