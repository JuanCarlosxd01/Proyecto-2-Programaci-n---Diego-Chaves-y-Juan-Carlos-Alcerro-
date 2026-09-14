
package insta.interfaz;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginInstaPanel extends JPanel implements Tematizable {

    private JPanel panelCentral;
    private JPanel panelPassword;

    private JLabel lblLogo;
    private JLabel lblSubtitulo;

    private JTextField txtUsuario;
    private JPasswordField txtPassword;

    private JButton btnIngresar;
    private JButton btnCrearCuenta;
    private JButton btnVerPassword;

    private char caracterPassword;

    public LoginInstaPanel() {
        setLayout(new GridBagLayout());

        crearInterfaz();
        aplicarTema();
    }

    private void crearInterfaz() {
        panelCentral = new JPanel();
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.Y_AXIS));
        panelCentral.setPreferredSize(new Dimension(440, 600));
        panelCentral.setBorder(new EmptyBorder(35, 45, 35, 45));

        lblLogo = new JLabel("INSTA+");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 38));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblSubtitulo = new JLabel("Inicia sesión para continuar");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblUsuario = new JLabel("Username");
        lblUsuario.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsuario = new JTextField();
        txtUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsuario.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel lblPassword = new JLabel("Contraseña");
        lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelPassword = new JPanel(new BorderLayout());

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setBorder(new EmptyBorder(10, 12, 10, 12));

        caracterPassword = txtPassword.getEchoChar();

        btnVerPassword = new JButton("◉");
        btnVerPassword.setPreferredSize(new Dimension(45, 42));
        btnVerPassword.setFocusPainted(false);
        btnVerPassword.setToolTipText("Mostrar u ocultar contraseña");

        btnVerPassword.addActionListener(e -> mostrarOcultarPassword());

        panelPassword.add(txtPassword, BorderLayout.CENTER);
        panelPassword.add(btnVerPassword, BorderLayout.EAST);
        panelPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        btnIngresar = new JButton("Iniciar sesión");
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnIngresar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnIngresar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnIngresar.setFocusPainted(false);
        btnIngresar.setOpaque(true);
        btnIngresar.setContentAreaFilled(true);
        btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIngresar.setHorizontalAlignment(SwingConstants.CENTER);
        btnIngresar.setUI(new BasicButtonUI());
        instalarHoverPrincipal(btnIngresar);

        JLabel lblSeparador = new JLabel("──────────  O  ──────────");
        lblSeparador.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnCrearCuenta = new JButton("Crear cuenta");
        btnCrearCuenta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCrearCuenta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnCrearCuenta.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCrearCuenta.setFocusPainted(false);
        btnCrearCuenta.setOpaque(true);
        btnCrearCuenta.setContentAreaFilled(true);
        btnCrearCuenta.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCrearCuenta.setHorizontalAlignment(SwingConstants.CENTER);
        btnCrearCuenta.setUI(new BasicButtonUI());
        instalarHoverPrincipal(btnCrearCuenta);

        panelCentral.add(lblLogo);
        panelCentral.add(Box.createVerticalStrut(8));
        panelCentral.add(lblSubtitulo);

        panelCentral.add(Box.createVerticalStrut(35));

        panelCentral.add(lblUsuario);
        panelCentral.add(Box.createVerticalStrut(6));
        panelCentral.add(txtUsuario);

        panelCentral.add(Box.createVerticalStrut(16));

        panelCentral.add(lblPassword);
        panelCentral.add(Box.createVerticalStrut(6));
        panelCentral.add(panelPassword);

        panelCentral.add(Box.createVerticalStrut(25));
        panelCentral.add(btnIngresar);

        panelCentral.add(Box.createVerticalStrut(25));
        panelCentral.add(lblSeparador);
        panelCentral.add(Box.createVerticalStrut(20));

        panelCentral.add(btnCrearCuenta);
        panelCentral.add(Box.createVerticalStrut(22));

        JLabel demo = new JLabel(
        "<html><div style='text-align:center;'>"
        + "<b>Cuentas de prueba</b><br>"
        + "maria_demo · carlos_demo · sofia_demo<br>"
        + "Contraseña: <b>Demo1234!</b>"
        + "</div></html>",
        SwingConstants.CENTER
        );

        demo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        demo.setForeground(TemaInsta.TEXTO_SECUNDARIO);

        demo.setHorizontalAlignment(SwingConstants.CENTER);
        demo.setVerticalAlignment(SwingConstants.CENTER);

        demo.setAlignmentX(Component.CENTER_ALIGNMENT);
        demo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        demo.setPreferredSize(new Dimension(350, 65));

        panelCentral.add(demo);

        add(panelCentral);
    }

    private void mostrarOcultarPassword() {
        if (txtPassword.getEchoChar() == 0) {
            txtPassword.setEchoChar(caracterPassword);
            btnVerPassword.setText("◉");
        } else {
            txtPassword.setEchoChar((char) 0);
            btnVerPassword.setText("⊘");
        }
    }

    @Override
    public void aplicarTema() {
        setBackground(TemaInsta.FONDO_SECUNDARIO);

        panelCentral.setBackground(TemaInsta.TARJETA);
        panelCentral.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaInsta.BORDE),
                new EmptyBorder(35, 45, 35, 45)
        ));

        panelPassword.setBackground(TemaInsta.INPUT);

        lblLogo.setForeground(TemaInsta.TEXTO);
        lblSubtitulo.setForeground(TemaInsta.TEXTO_SECUNDARIO);

        cambiarLabels(this);

        txtUsuario.setBackground(TemaInsta.INPUT);
        txtUsuario.setForeground(TemaInsta.TEXTO);
        txtUsuario.setCaretColor(TemaInsta.TEXTO);

        txtPassword.setBackground(TemaInsta.INPUT);
        txtPassword.setForeground(TemaInsta.TEXTO);
        txtPassword.setCaretColor(TemaInsta.TEXTO);

        btnVerPassword.setBackground(TemaInsta.INPUT);
        btnVerPassword.setForeground(TemaInsta.TEXTO);

        btnIngresar.setBackground(TemaInsta.BOTON);
        btnIngresar.setForeground(TemaInsta.BOTON_TEXTO);
        btnIngresar.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));

        btnCrearCuenta.setBackground(TemaInsta.BOTON);
        btnCrearCuenta.setForeground(Color.WHITE);
        btnCrearCuenta.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));

        revalidate();
        repaint();
    }

    private void instalarHoverPrincipal(JButton boton) {
        boton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { boton.setBackground(new Color(0, 125, 210)); }
            @Override public void mouseExited(MouseEvent e) { boton.setBackground(TemaInsta.BOTON); }
        });
    }

    private void cambiarLabels(Container contenedor) {
        for (Component componente : contenedor.getComponents()) {
            if (componente instanceof JLabel label) {
                label.setForeground(TemaInsta.TEXTO);
            }

            if (componente instanceof Container interno) {
                cambiarLabels(interno);
            }
        }

        lblSubtitulo.setForeground(TemaInsta.TEXTO_SECUNDARIO);
    }

    public JTextField getTxtUsuario() {
        return txtUsuario;
    }

    public JPasswordField getTxtPassword() {
        return txtPassword;
    }

    public JButton getBtnIngresar() {
        return btnIngresar;
    }

    public JButton getBtnCrearCuenta() {
        return btnCrearCuenta;
    }

    public void limpiar() {
        txtUsuario.setText("");
        txtPassword.setText("");
        txtPassword.setEchoChar(caracterPassword);
    }
}
