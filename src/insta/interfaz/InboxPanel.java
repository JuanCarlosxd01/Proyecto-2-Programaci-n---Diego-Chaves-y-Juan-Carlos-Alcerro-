
package insta.interfaz;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class InboxPanel extends JPanel implements Tematizable {

    private JPanel panelIzquierdo;
    private JPanel panelDerecho;
    private JPanel panelEncabezadoChat;
    private JPanel panelEnviar;
    private JPanel panelMensajes;

    private JSplitPane divisor;

    private DefaultListModel<String> modeloConversaciones;
    private JList<String> listaConversaciones;

    private JLabel lblUsuarioChat;

    private JTextField txtMensaje;

    private JButton btnEnviar;
    private JButton btnSticker;
    private JButton btnEliminar;

    public InboxPanel() {
        setLayout(new BorderLayout());

        crearInbox();

        aplicarTema();
    }

    private void crearInbox() {
        divisor = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        divisor.setDividerLocation(260);
        divisor.setResizeWeight(0.30);
        divisor.setBorder(null);

        panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblConversaciones = new JLabel("Mensajes");
        lblConversaciones.setFont(new Font("Arial", Font.BOLD, 22));

        modeloConversaciones = new DefaultListModel<>();

        listaConversaciones = new JList<>(modeloConversaciones);
        listaConversaciones.setFixedCellHeight(55);

        panelIzquierdo.add(lblConversaciones, BorderLayout.NORTH);
        panelIzquierdo.add(new JScrollPane(listaConversaciones), BorderLayout.CENTER);

        panelDerecho = new JPanel(new BorderLayout());

        panelEncabezadoChat = new JPanel(new BorderLayout());
        panelEncabezadoChat.setBorder(new EmptyBorder(15, 20, 15, 20));

        lblUsuarioChat = new JLabel("Selecciona una conversación");
        lblUsuarioChat.setFont(new Font("Arial", Font.BOLD, 16));

        btnEliminar = new JButton("Eliminar");

        panelEncabezadoChat.add(lblUsuarioChat, BorderLayout.WEST);
        panelEncabezadoChat.add(btnEliminar, BorderLayout.EAST);

        panelMensajes = new JPanel();
        panelMensajes.setLayout(new BoxLayout(panelMensajes, BoxLayout.Y_AXIS));
        panelMensajes.setBorder(new EmptyBorder(15, 15, 15, 15));

        JScrollPane scrollMensajes = new JScrollPane(panelMensajes);
        scrollMensajes.setBorder(null);

        panelEnviar = new JPanel(new BorderLayout(8, 0));
        panelEnviar.setBorder(new EmptyBorder(10, 10, 10, 10));

        btnSticker = new JButton("☺");
        txtMensaje = new JTextField();
        btnEnviar = new JButton("Enviar");

        panelEnviar.add(btnSticker, BorderLayout.WEST);
        panelEnviar.add(txtMensaje, BorderLayout.CENTER);
        panelEnviar.add(btnEnviar, BorderLayout.EAST);

        panelDerecho.add(panelEncabezadoChat, BorderLayout.NORTH);
        panelDerecho.add(scrollMensajes, BorderLayout.CENTER);
        panelDerecho.add(panelEnviar, BorderLayout.SOUTH);

        divisor.setLeftComponent(panelIzquierdo);
        divisor.setRightComponent(panelDerecho);

        add(divisor, BorderLayout.CENTER);
    }

    @Override
    public void aplicarTema() {
        setBackground(TemaInsta.FONDO);

        panelIzquierdo.setBackground(TemaInsta.FONDO);
        panelDerecho.setBackground(TemaInsta.FONDO);
        panelEncabezadoChat.setBackground(TemaInsta.FONDO);
        panelEnviar.setBackground(TemaInsta.FONDO);
        panelMensajes.setBackground(TemaInsta.FONDO_SECUNDARIO);

        divisor.setBackground(TemaInsta.BORDE);

        listaConversaciones.setBackground(TemaInsta.FONDO);
        listaConversaciones.setForeground(TemaInsta.TEXTO);
        listaConversaciones.setSelectionBackground(TemaInsta.INPUT);
        listaConversaciones.setSelectionForeground(TemaInsta.TEXTO);

        lblUsuarioChat.setForeground(TemaInsta.TEXTO);

        txtMensaje.setBackground(TemaInsta.INPUT);
        txtMensaje.setForeground(TemaInsta.TEXTO);
        txtMensaje.setCaretColor(TemaInsta.TEXTO);

        btnEnviar.setBackground(TemaInsta.BOTON);
        btnEnviar.setForeground(TemaInsta.BOTON_TEXTO);

        btnSticker.setBackground(TemaInsta.INPUT);
        btnSticker.setForeground(TemaInsta.TEXTO);

        btnEliminar.setBackground(TemaInsta.INPUT);
        btnEliminar.setForeground(TemaInsta.TEXTO);

        cambiarTexto(this);

        revalidate();
        repaint();
    }

    private void cambiarTexto(Container contenedor) {
        for (Component componente : contenedor.getComponents()) {
            if (componente instanceof JLabel label) {
                label.setForeground(TemaInsta.TEXTO);
            }

            if (componente instanceof Container interno) {
                cambiarTexto(interno);
            }
        }
    }

    public DefaultListModel<String> getModeloConversaciones() {
        return modeloConversaciones;
    }

    public JList<String> getListaConversaciones() {
        return listaConversaciones;
    }

    public JTextField getTxtMensaje() {
        return txtMensaje;
    }

    public JButton getBtnEnviar() {
        return btnEnviar;
    }

    public JButton getBtnSticker() {
        return btnSticker;
    }

    public JButton getBtnEliminar() {
        return btnEliminar;
    }

    public JPanel getPanelMensajes() {
        return panelMensajes;
    }
    
    
}