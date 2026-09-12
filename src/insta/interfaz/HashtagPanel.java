
package insta.interfaz;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class HashtagPanel extends JPanel implements Tematizable {

    private JPanel panelSuperior;
    private JPanel panelBusqueda;
    private JPanel panelResultados;

    private JTextField txtHashtag;
    private JButton btnBuscar;

    public HashtagPanel() {
        setLayout(new BorderLayout());

        crearEncabezado();
        crearResultados();

        aplicarTema();
    }

    private void crearEncabezado() {
        panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel titulo = new JLabel("Buscar hashtag");
        titulo.setFont(new Font("Arial", Font.BOLD, 25));

        panelBusqueda = new JPanel(new BorderLayout(10, 0));

        JLabel hashtag = new JLabel("#");
        hashtag.setFont(new Font("Arial", Font.BOLD, 20));

        txtHashtag = new JTextField();
        btnBuscar = new JButton("Buscar");

        panelBusqueda.add(hashtag, BorderLayout.WEST);
        panelBusqueda.add(txtHashtag, BorderLayout.CENTER);
        panelBusqueda.add(btnBuscar, BorderLayout.EAST);

        panelSuperior.add(titulo, BorderLayout.NORTH);
        panelSuperior.add(panelBusqueda, BorderLayout.SOUTH);

        add(panelSuperior, BorderLayout.NORTH);
    }

    private void crearResultados() {
        panelResultados = new JPanel();
        panelResultados.setLayout(new BoxLayout(panelResultados, BoxLayout.Y_AXIS));
        panelResultados.setBorder(new EmptyBorder(20, 40, 20, 40));

        JLabel mensaje = new JLabel("Las publicaciones con el hashtag aparecerán aquí.");
        mensaje.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelResultados.add(mensaje);

        JScrollPane scroll = new JScrollPane(panelResultados);
        scroll.setBorder(null);

        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void aplicarTema() {
        setBackground(TemaInsta.FONDO);

        panelSuperior.setBackground(TemaInsta.FONDO);
        panelBusqueda.setBackground(TemaInsta.FONDO);
        panelResultados.setBackground(TemaInsta.FONDO_SECUNDARIO);

        txtHashtag.setBackground(TemaInsta.INPUT);
        txtHashtag.setForeground(TemaInsta.TEXTO);
        txtHashtag.setCaretColor(TemaInsta.TEXTO);

        btnBuscar.setBackground(TemaInsta.BOTON);
        btnBuscar.setForeground(TemaInsta.BOTON_TEXTO);

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

    public JTextField getTxtHashtag() {
        return txtHashtag;
    }

    public JButton getBtnBuscar() {
        return btnBuscar;
    }

    public JPanel getPanelResultados() {
        return panelResultados;
    }
}