
package interfaz;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import modelo.*;
import sistema.*;

public class LoginPanel extends JPanel {

    private JPanel contenedor;
    private CardLayout transicion;
    private GestorUsuarios gestorUsuarios;

    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JButton btnIngresar;
    private JButton btnApagar;
    private JCheckBox chkVerContrasena;

    private JPanel panelAviso;
    private JLabel lblAviso;

    public LoginPanel(JPanel contenedor, CardLayout transicion, GestorUsuarios gestorUsuarios) {
        this.contenedor = contenedor;
        this.transicion = transicion;
        this.gestorUsuarios = gestorUsuarios;

        setOpaque(false);
        setLayout(new GridBagLayout());

        crearBotonApagado();
        crearInterfaz();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setColor(new Color(0, 0, 0, 85));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.dispose();
        super.paintComponent(g);
    }

    private void crearInterfaz() {
        JPanel panelLogin = new JPanel();
        panelLogin.setOpaque(false);
        panelLogin.setLayout(new BoxLayout(panelLogin, BoxLayout.Y_AXIS));
        panelLogin.setPreferredSize(new Dimension(420, 430));

        JLabel lblIcono = crearIconoUsuario();

        JLabel titulo = new JLabel("Otro Usuario");
        titulo.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        crearCampoUsuario();
        crearCampoContrasena();

        JPanel panelContrasena = crearPanelContrasena();
        JPanel panelVer = crearPanelVerContrasena();

        crearPanelAviso();

        panelLogin.add(Box.createVerticalGlue());

        panelLogin.add(lblIcono);
        panelLogin.add(Box.createVerticalStrut(8));

        panelLogin.add(titulo);
        panelLogin.add(Box.createVerticalStrut(22));

        panelLogin.add(txtUsuario);
        panelLogin.add(Box.createVerticalStrut(8));

        panelLogin.add(panelContrasena);
        panelLogin.add(Box.createVerticalStrut(5));

        panelLogin.add(panelVer);
        panelLogin.add(Box.createVerticalStrut(8));

        panelLogin.add(panelAviso);

        panelLogin.add(Box.createVerticalGlue());

        GridBagConstraints gbcLogin = new GridBagConstraints();
        gbcLogin.gridx = 0;
        gbcLogin.gridy = 0;
        gbcLogin.weightx = 1;
        gbcLogin.weighty = 1;
        gbcLogin.anchor = GridBagConstraints.CENTER;
        gbcLogin.insets = new Insets(0, 0, 0, 0);

        add(panelLogin, gbcLogin);

        GridBagConstraints gbcApagar = new GridBagConstraints();
        gbcApagar.gridx = 0;
        gbcApagar.gridy = 0;
        gbcApagar.weightx = 1;
        gbcApagar.weighty = 1;
        gbcApagar.anchor = GridBagConstraints.SOUTHEAST;
        gbcApagar.insets = new Insets(0, 0, 25, 30);

        add(btnApagar, gbcApagar);

        acciones();
    }

    private JLabel crearIconoUsuario() {
        JLabel lblIcono = new JLabel("♙", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(95, 95, 95));
                g2.fillOval(0, 0, getWidth(), getHeight());

                g2.dispose();
                super.paintComponent(g);
            }
        };

        lblIcono.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 72));
        lblIcono.setForeground(Color.WHITE);

        lblIcono.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcono.setVerticalAlignment(SwingConstants.CENTER);

        lblIcono.setPreferredSize(new Dimension(120, 120));
        lblIcono.setMaximumSize(new Dimension(120, 120));
        lblIcono.setMinimumSize(new Dimension(120, 120));

        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblIcono.setOpaque(false);

        return lblIcono;
    }

    private void crearCampoUsuario() {
        txtUsuario = new JTextField();

        txtUsuario.setPreferredSize(new Dimension(310, 36));
        txtUsuario.setMaximumSize(new Dimension(310, 36));
        txtUsuario.setMinimumSize(new Dimension(310, 36));

        txtUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUsuario.setText("Nombre de usuario");
        txtUsuario.setForeground(Color.GRAY);
        txtUsuario.setBackground(Color.WHITE);

        txtUsuario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));

        txtUsuario.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if(txtUsuario.getText().equals("Nombre de usuario")) {
                    txtUsuario.setText("");
                    txtUsuario.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(txtUsuario.getText().isEmpty()) {
                    txtUsuario.setText("Nombre de usuario");
                    txtUsuario.setForeground(Color.GRAY);
                }
            }
        });
    }

    private void crearCampoContrasena() {
        txtContrasena = new JPasswordField();

        txtContrasena.setPreferredSize(new Dimension(259, 36));
        txtContrasena.setMaximumSize(new Dimension(259, 36));
        txtContrasena.setMinimumSize(new Dimension(259, 36));

        txtContrasena.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        txtContrasena.setBackground(new Color(65, 65, 65));
        txtContrasena.setForeground(new Color(200, 200, 200));
        txtContrasena.setCaretColor(Color.WHITE);

        txtContrasena.setText("Contraseña");
        txtContrasena.setEchoChar((char) 0);

        txtContrasena.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 120, 120)),
                BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));

        txtContrasena.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String texto = new String(txtContrasena.getPassword());

                if(texto.equals("Contraseña")) {
                    txtContrasena.setText("");
                    txtContrasena.setEchoChar(chkVerContrasena != null && chkVerContrasena.isSelected() ? (char) 0 : '•');
                    txtContrasena.setForeground(Color.WHITE);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(txtContrasena.getPassword().length == 0) {
                    txtContrasena.setText("Contraseña");
                    txtContrasena.setEchoChar((char) 0);
                    txtContrasena.setForeground(new Color(200, 200, 200));
                }
            }
        });
    }

    private JPanel crearPanelContrasena() {
        JPanel panelContrasena = new JPanel();
        panelContrasena.setOpaque(false);
        panelContrasena.setLayout(new BoxLayout(panelContrasena, BoxLayout.X_AXIS));

        panelContrasena.setPreferredSize(new Dimension(310, 36));
        panelContrasena.setMaximumSize(new Dimension(310, 36));
        panelContrasena.setMinimumSize(new Dimension(310, 36));

        panelContrasena.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnIngresar = new JButton("→");

        btnIngresar.setFont(new Font("Segoe UI", Font.PLAIN, 21));

        btnIngresar.setPreferredSize(new Dimension(45, 36));
        btnIngresar.setMaximumSize(new Dimension(45, 36));
        btnIngresar.setMinimumSize(new Dimension(45, 36));

        btnIngresar.setHorizontalAlignment(SwingConstants.CENTER);
        btnIngresar.setVerticalAlignment(SwingConstants.CENTER);
        btnIngresar.setMargin(new Insets(0, 0, 3, 0));

        btnIngresar.setBackground(new Color(245, 245, 245));
        btnIngresar.setForeground(new Color(20, 20, 20));

        btnIngresar.setContentAreaFilled(true);
        btnIngresar.setOpaque(true);

        btnIngresar.setFocusPainted(false);
        btnIngresar.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));

        btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnIngresar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnIngresar.setBackground(new Color(215, 230, 245));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnIngresar.setBackground(new Color(245, 245, 245));
            }
        });

        panelContrasena.add(txtContrasena);
        panelContrasena.add(Box.createHorizontalStrut(6));
        panelContrasena.add(btnIngresar);

        return panelContrasena;
    }

    private JPanel crearPanelVerContrasena() {
        chkVerContrasena = new JCheckBox("Mostrar contraseña");

        chkVerContrasena.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkVerContrasena.setForeground(Color.WHITE);
        chkVerContrasena.setOpaque(false);
        chkVerContrasena.setFocusPainted(false);

        chkVerContrasena.setCursor(new Cursor(Cursor.HAND_CURSOR));

        chkVerContrasena.addActionListener(e -> actualizarVisibilidadContrasena());

        JPanel panelVer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panelVer.setOpaque(false);

        panelVer.setPreferredSize(new Dimension(310, 24));
        panelVer.setMaximumSize(new Dimension(310, 24));
        panelVer.setMinimumSize(new Dimension(310, 24));

        panelVer.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelVer.add(chkVerContrasena);

        return panelVer;
    }

    private void crearPanelAviso() {
        panelAviso = new JPanel(new BorderLayout());

        panelAviso.setPreferredSize(new Dimension(310, 45));
        panelAviso.setMaximumSize(new Dimension(310, 45));
        panelAviso.setMinimumSize(new Dimension(310, 45));

        panelAviso.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelAviso.setOpaque(false);
        panelAviso.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        lblAviso = new JLabel(" ", SwingConstants.CENTER);
        lblAviso.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAviso.setForeground(Color.WHITE);

        panelAviso.add(lblAviso, BorderLayout.CENTER);
}

    private void crearBotonApagado() {
        btnApagar = new JButton("⏻") {

            private boolean mouseEncima = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        mouseEncima = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        mouseEncima = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                if(mouseEncima) {
                    g2.setColor(new Color(255, 255, 255, 45));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btnApagar.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 28));
        btnApagar.setForeground(Color.WHITE);

        btnApagar.setPreferredSize(new Dimension(50, 50));
        btnApagar.setMinimumSize(new Dimension(50, 50));
        btnApagar.setMaximumSize(new Dimension(50, 50));

        btnApagar.setHorizontalAlignment(SwingConstants.CENTER);
        btnApagar.setVerticalAlignment(SwingConstants.CENTER);

        btnApagar.setMargin(new Insets(0, 0, 3, 0));

        btnApagar.setContentAreaFilled(false);
        btnApagar.setBorderPainted(false);
        btnApagar.setFocusPainted(false);
        btnApagar.setOpaque(false);

        btnApagar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnApagar.setToolTipText("Apagar");

        btnApagar.addActionListener(e -> System.exit(0));
    }

    private void actualizarVisibilidadContrasena() {
        String texto = new String(txtContrasena.getPassword());

        if(texto.equals("Contraseña")) {
            txtContrasena.setEchoChar((char) 0);
            return;
        }

        txtContrasena.setEchoChar(chkVerContrasena.isSelected() ? (char) 0 : '•');
    }

    private void mostrarAviso(String mensaje) {
        panelAviso.setOpaque(true);
        panelAviso.setBackground(new Color(125, 35, 35, 215));

        panelAviso.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(235, 110, 110)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        lblAviso.setText(mensaje);

        panelAviso.repaint();
    }

    private void ocultarAviso() {
        lblAviso.setText(" ");

        panelAviso.setOpaque(false);
        panelAviso.setBackground(new Color(0, 0, 0, 0));

        panelAviso.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        panelAviso.repaint();
    }

    private void acciones() {
        btnIngresar.addActionListener(e -> iniciarSesion());

        txtContrasena.addActionListener(e -> iniciarSesion());

        txtUsuario.addActionListener(e -> {
            txtContrasena.requestFocusInWindow();
        });
    }

    private void iniciarSesion() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());

        if(usuario.equals("Nombre de usuario") || usuario.isEmpty()) {
            mostrarAviso("Ingrese su nombre de usuario.");
            txtUsuario.requestFocusInWindow();
            return;
        }

        if(contrasena.equals("Contraseña") || contrasena.isEmpty()) {
            mostrarAviso("Ingrese su contraseña.");
            txtContrasena.requestFocusInWindow();
            return;
        }

        UsuarioSistema encontrado = gestorUsuarios.iniciarSesion(usuario, contrasena);

        if(encontrado != null) {
            ocultarAviso();
            Sesion.iniciarSesion(encontrado);
            transicion.show(contenedor, "ESCRITORIO");
        }
        else {
            mostrarAviso("Usuario o contraseña incorrectos.");
        }
    }
    
    public void limpiarCampos() {
        txtUsuario.setText("Nombre de usuario");
        txtUsuario.setForeground(Color.GRAY);

        txtContrasena.setText("Contraseña");
        txtContrasena.setEchoChar((char) 0);
        txtContrasena.setForeground(new Color(200, 200, 200));

        chkVerContrasena.setSelected(false);

        ocultarAviso();

        revalidate();
        repaint();
    }
}