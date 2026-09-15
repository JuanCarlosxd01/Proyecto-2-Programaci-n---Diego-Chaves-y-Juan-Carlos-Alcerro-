package insta.interfaz;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class PanelSuperpuestoInsta {

    private PanelSuperpuestoInsta() {
    }

    public static Runnable mostrar(Component padre, String titulo, JComponent contenido, Dimension tamano) {
        JInternalFrame ventanaInsta = (JInternalFrame) SwingUtilities.getAncestorOfClass(JInternalFrame.class, padre);
        JRootPane raiz = ventanaInsta != null ? ventanaInsta.getRootPane() : SwingUtilities.getRootPane(padre);
        if (raiz == null) {
            return () -> { };
        }

        Component anterior = raiz.getGlassPane();
        boolean anteriorVisible = anterior != null && anterior.isVisible();

        JPanel vidrio = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 145));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        vidrio.setOpaque(false);

        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setPreferredSize(tamano);
        tarjeta.setMaximumSize(tamano);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(TemaInsta.BORDE), new EmptyBorder(8, 8, 8, 8)));
        tarjeta.setBackground(TemaInsta.FONDO);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setForeground(TemaInsta.TEXTO);
        JButton btnCerrar = new JButton("✕");
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBackground(TemaInsta.INPUT);
        btnCerrar.setForeground(TemaInsta.TEXTO);
        encabezado.add(lblTitulo, BorderLayout.WEST);
        encabezado.add(btnCerrar, BorderLayout.EAST);

        tarjeta.add(encabezado, BorderLayout.NORTH);
        tarjeta.add(contenido, BorderLayout.CENTER);
        vidrio.add(tarjeta);

        Runnable cerrar = () -> {
            if (raiz.getGlassPane() == vidrio) {
                vidrio.setVisible(false);
                raiz.setGlassPane(anterior);
                if (anterior != null) anterior.setVisible(anteriorVisible);
            }
        };

        btnCerrar.addActionListener(e -> cerrar.run());
        vidrio.addMouseListener(new java.awt.event.MouseAdapter() { });
        raiz.setGlassPane(vidrio);
        vidrio.setVisible(true);
        vidrio.requestFocusInWindow();
        return cerrar;
    }
}
