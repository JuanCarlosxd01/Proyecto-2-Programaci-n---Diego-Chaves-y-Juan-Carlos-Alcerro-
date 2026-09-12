
package insta.interfaz;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class InteraccionesPanel extends JPanel implements Tematizable {

    private JPanel panelEncabezado;
    private JPanel panelInteracciones;

    private JButton btnActualizar;

    public InteraccionesPanel() {
        setLayout(new BorderLayout());

        crearEncabezado();
        crearContenido();

        aplicarTema();
    }

    private void crearEncabezado() {
        panelEncabezado = new JPanel(new BorderLayout());
        panelEncabezado.setBorder(new EmptyBorder(20, 30, 15, 30));

        JLabel titulo = new JLabel("Interacciones");
        titulo.setFont(new Font("Arial", Font.BOLD, 25));

        btnActualizar = new JButton("Actualizar");

        panelEncabezado.add(titulo, BorderLayout.WEST);
        panelEncabezado.add(btnActualizar, BorderLayout.EAST);

        add(panelEncabezado, BorderLayout.NORTH);
    }

    private void crearContenido() {
        panelInteracciones = new JPanel();
        panelInteracciones.setLayout(new BoxLayout(panelInteracciones, BoxLayout.Y_AXIS));
        panelInteracciones.setBorder(new EmptyBorder(20, 50, 20, 50));

        JLabel mensaje = new JLabel("Aquí aparecerán las publicaciones donde te mencionen.");
        mensaje.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelInteracciones.add(Box.createVerticalStrut(40));
        panelInteracciones.add(mensaje);

        JScrollPane scroll = new JScrollPane(panelInteracciones);
        scroll.setBorder(null);

        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void aplicarTema() {
        setBackground(TemaInsta.FONDO);
        panelEncabezado.setBackground(TemaInsta.FONDO);
        panelInteracciones.setBackground(TemaInsta.FONDO_SECUNDARIO);

        btnActualizar.setBackground(TemaInsta.INPUT);
        btnActualizar.setForeground(TemaInsta.TEXTO);

        cambiarTexto(this);

        revalidate();
        repaint();
    }

    private void cambiarTexto(Container contenedor) {
        for (Component componente : contenedor.getComponents()) {
            if (componente instanceof JLabel label) {
                label.setForeground(TemaInsta.TEXTO);
            }

            if (componente instanceof JScrollPane scroll) {
                scroll.getViewport().setBackground(TemaInsta.FONDO_SECUNDARIO);
            }

            if (componente instanceof Container interno) {
                cambiarTexto(interno);
            }
        }
    }

    public JButton getBtnActualizar() {
        return btnActualizar;
    }

    public JPanel getPanelInteracciones() {
        return panelInteracciones;
    }
}