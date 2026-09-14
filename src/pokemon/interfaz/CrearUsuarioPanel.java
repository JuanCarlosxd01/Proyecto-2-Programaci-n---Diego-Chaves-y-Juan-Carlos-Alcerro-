
package pokemon.interfaz;

import interfaz.DialogosWindows;

import pokemon.preparacion.NombreInvalidoExcepcion;
import pokemon.preparacion.Sesion;
import pokemon.preparacion.User;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class CrearUsuarioPanel extends FondoPokemonPanel {

    private VentanaPokemon ventana;

    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JPasswordField txtConfirmar;

    public CrearUsuarioPanel(VentanaPokemon ventana) {
        this.ventana = ventana;

        setLayout(new GridBagLayout());

        crearContenido();
    }

    private void crearContenido() {
        JPanel tarjeta = new JPanel();

        tarjeta.setPreferredSize(new Dimension(470, 520));
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(new Color(248, 250, 246));

        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(35, 75, 62), 5),
                new EmptyBorder(35, 45, 35, 45)
        ));

        JLabel titulo = new JLabel("NUEVO ENTRENADOR");

        titulo.setFont(new Font("Arial", Font.BOLD, 27));
        titulo.setForeground(new Color(43, 91, 77));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel pokeball = new JLabel("\u25C9");

        pokeball.setFont(new Font("Segoe UI Symbol", Font.BOLD, 50));
        pokeball.setForeground(new Color(213, 59, 55));
        pokeball.setHorizontalAlignment(SwingConstants.CENTER);
        pokeball.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtUsuario = crearCampo();
        txtContrasena = crearPassword();
        txtConfirmar = crearPassword();

        JButton btnCrear = crearBoton("CREAR ENTRENADOR");
        JButton btnVolver = crearBoton("VOLVER");

        btnCrear.addActionListener(e -> crearUsuario());

        btnVolver.addActionListener(e -> {
            limpiarCampos();
            ventana.mostrarLogin();
        });

        tarjeta.add(titulo);
        tarjeta.add(Box.createVerticalStrut(5));
        tarjeta.add(pokeball);
        tarjeta.add(Box.createVerticalStrut(20));

        agregarCampo(tarjeta, "USUARIO", txtUsuario);
        agregarCampo(tarjeta, "CONTRASEÑA", txtContrasena);
        agregarCampo(tarjeta, "CONFIRMAR CONTRASEÑA", txtConfirmar);

        tarjeta.add(Box.createVerticalStrut(20));
        tarjeta.add(btnCrear);
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(btnVolver);

        add(tarjeta);
    }

    private JTextField crearCampo() {
        JTextField campo = new JTextField();

        configurarCampo(campo);

        return campo;
    }

    private JPasswordField crearPassword() {
        JPasswordField campo = new JPasswordField();

        configurarCampo(campo);

        return campo;
    }

    private void configurarCampo(JTextField campo) {
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        campo.setFont(new Font("Arial", Font.PLAIN, 16));

        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 103, 91), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void agregarCampo(JPanel panel, String texto, JTextField campo) {
        JLabel label = new JLabel(texto);

        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(campo);
        panel.add(Box.createVerticalStrut(15));
    }

    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color color;

                if(getModel().isPressed()) {
                    color = new Color(25, 85, 60);
                }
                else if(getModel().isRollover()) {
                    color = new Color(75, 170, 120);
                }
                else {
                    color = new Color(35, 120, 80);
                }

                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2.dispose();

                super.paintComponent(g);
            }
        };

        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        boton.setPreferredSize(new Dimension(380, 45));
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);

        boton.setFont(new Font("Arial", Font.BOLD, 15));
        boton.setForeground(Color.WHITE);

        boton.setOpaque(false);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);

        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return boton;
    }

    private void crearUsuario() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());
        String confirmar = new String(txtConfirmar.getPassword());

        if (usuario.isEmpty() || contrasena.isEmpty() || confirmar.isEmpty()) {
            DialogosWindows.showMessageDialog(
                    this,
                    "Complete todos los campos.",
                    "Campos incompletos",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (usuario.length() < 3) {
            DialogosWindows.showMessageDialog(
                    this,
                    "El nombre de usuario debe tener al menos 3 caracteres.",
                    "Usuario inválido",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (!contrasena.equals(confirmar)) {
            DialogosWindows.showMessageDialog(
                    this,
                    "Las contraseñas no coinciden.",
                    "Contraseña inválida",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {
            User nuevoUsuario = new User(usuario, contrasena);

            Sesion.getInstancia().getUsuarios().insertar(nuevoUsuario);

            DialogosWindows.showMessageDialog(
                    this,
                    "Entrenador creado correctamente.",
                    "Usuario creado",
                    JOptionPane.INFORMATION_MESSAGE
            );

            limpiarCampos();

            ventana.mostrarLogin();

        } catch (NombreInvalidoExcepcion e) {

            DialogosWindows.showMessageDialog(
                    this,
                    e.getMessage(),
                    "No se pudo crear el usuario",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void limpiarCampos() {
        txtUsuario.setText("");
        txtContrasena.setText("");
        txtConfirmar.setText("");
    }
}