package interfaz;

import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ConfiguracionEscritorioPanel extends JPanel {

    public interface Listener {
        void seleccionarFondo(File archivo);
        void seleccionarColor(Color color);
        void restaurarFondo();
    }

    public ConfiguracionEscritorioPanel(Listener listener) {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        JLabel titulo = new JLabel("Personalización", SwingConstants.LEFT);
        titulo.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        titulo.setBorder(BorderFactory.createEmptyBorder(25, 30, 10, 30));
        add(titulo, BorderLayout.NORTH);

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));

        JLabel fondo = new JLabel("Fondo del escritorio");
        fondo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        fondo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnImagen = new JButton("Examinar una imagen...");
        JButton btnColor = new JButton("Elegir color sólido...");
        JButton btnPredeterminado = new JButton("Restaurar fondo predeterminado");
        Dimension d = new Dimension(280, 38);
        for (JButton boton : new JButton[]{btnImagen, btnColor, btnPredeterminado}) {
            boton.setMaximumSize(d);
            boton.setAlignmentX(Component.LEFT_ALIGNMENT);
            boton.setFocusPainted(false);
        }

        btnImagen.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "png", "jpg", "jpeg", "bmp", "gif"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                listener.seleccionarFondo(chooser.getSelectedFile());
            }
        });

        btnColor.addActionListener(e -> {
            Color color = JColorChooser.showDialog(this, "Elegir color del escritorio", new Color(18, 56, 92));
            if (color != null) {
                listener.seleccionarColor(color);
            }
        });

        btnPredeterminado.addActionListener(e -> listener.restaurarFondo());

        contenido.add(fondo);
        contenido.add(Box.createVerticalStrut(18));
        contenido.add(btnImagen);
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(btnColor);
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(btnPredeterminado);
        contenido.add(Box.createVerticalStrut(35));

        JLabel nota = new JLabel("<html>El fondo seleccionado se guarda para el usuario actual y se vuelve a aplicar al iniciar sesión.</html>");
        nota.setForeground(new Color(90, 90, 90));
        nota.setMaximumSize(new Dimension(500, 80));
        nota.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(nota);

        add(contenido, BorderLayout.CENTER);
    }
}
