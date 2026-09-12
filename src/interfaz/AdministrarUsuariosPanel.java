
package interfaz;

import java.awt.*;
import javax.swing.*;
import sistema.GestorUsuarios;
import sistema.Sesion;

public class AdministrarUsuariosPanel extends JPanel {

    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JPasswordField txtConfirmarContrasena;

    private JCheckBox chkMostrarContrasena;

    private JButton btnCrearUsuario;

    private DefaultListModel<String> modeloUsuarios;
    private JList<String> listaUsuarios;

    private GestorUsuarios gestorUsuarios;

    public AdministrarUsuariosPanel(GestorUsuarios gestorUsuarios) {
        this.gestorUsuarios = gestorUsuarios;

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        crearFormulario();
        crearListaUsuarios();
        actualizarLista();
    }

    private void crearFormulario() {
        JPanel formulario = new JPanel();
        formulario.setLayout(new BoxLayout(formulario, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Crear nuevo usuario");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));

        txtUsuario = new JTextField();
        txtContrasena = new JPasswordField();
        txtConfirmarContrasena = new JPasswordField();

        chkMostrarContrasena = new JCheckBox("Mostrar contraseñas");

        btnCrearUsuario = new JButton("Crear usuario");

        formulario.add(titulo);

        formulario.add(Box.createVerticalStrut(20));

        formulario.add(new JLabel("Nombre de usuario:"));
        formulario.add(txtUsuario);

        formulario.add(Box.createVerticalStrut(10));

        formulario.add(new JLabel("Contraseña:"));
        formulario.add(txtContrasena);

        formulario.add(Box.createVerticalStrut(10));

        formulario.add(new JLabel("Confirmar contraseña:"));
        formulario.add(txtConfirmarContrasena);

        formulario.add(Box.createVerticalStrut(5));

        formulario.add(chkMostrarContrasena);

        formulario.add(Box.createVerticalStrut(10));

        JLabel lblRequisitos = new JLabel("<html>La contraseña debe contener:<br>• Mínimo 8 caracteres<br>• Una mayúscula<br>• Un número<br>• Un símbolo</html>");
        lblRequisitos.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        formulario.add(lblRequisitos);

        formulario.add(Box.createVerticalStrut(20));

        formulario.add(btnCrearUsuario);

        add(formulario, BorderLayout.WEST);

        btnCrearUsuario.addActionListener(e -> crearUsuario());
        chkMostrarContrasena.addActionListener(e -> mostrarOcultarContrasena());
    }

    private void crearListaUsuarios() {
        modeloUsuarios = new DefaultListModel<>();

        listaUsuarios = new JList<>(modeloUsuarios);

        JScrollPane scroll = new JScrollPane(listaUsuarios);
        scroll.setPreferredSize(new Dimension(250, 0));

        add(scroll, BorderLayout.CENTER);
    }

    private void crearUsuario() {
        if (!Sesion.esAdministrador()) {
            JOptionPane.showMessageDialog(this, "Solo el administrador puede crear usuarios.", "Acceso denegado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());
        String confirmarContrasena = new String(txtConfirmarContrasena.getPassword());

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un nombre de usuario.");
            return;
        }

        if (contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese una contraseña.");
            return;
        }

        if (confirmarContrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Confirme la contraseña.");
            return;
        }

        if (!contrasena.equals(confirmarContrasena)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.", "Contraseña incorrecta", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!contrasenaValida(contrasena)) {
            JOptionPane.showMessageDialog(
                this,
                "La contraseña debe tener:\n\n"
                + "- Mínimo 8 caracteres\n"
                + "- Al menos una letra mayúscula\n"
                + "- Al menos un número\n"
                + "- Al menos un símbolo",
                "Contraseña no válida",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        boolean creado = gestorUsuarios.crearUsuario(username, contrasena);

        if (creado) {
            JOptionPane.showMessageDialog(this, "Usuario creado correctamente.");

            txtUsuario.setText("");
            txtContrasena.setText("");
            txtConfirmarContrasena.setText("");

            actualizarLista();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo crear el usuario. Puede que el nombre ya exista.");
        }
    }

    private boolean contrasenaValida(String contrasena) {
        if (contrasena.length() < 8) {
            return false;
        }

        boolean tieneMayuscula = false;
        boolean tieneNumero = false;
        boolean tieneSimbolo = false;

        for (char caracter : contrasena.toCharArray()) {

            if (Character.isUpperCase(caracter)) {
                tieneMayuscula = true;
            }

            if (Character.isDigit(caracter)) {
                tieneNumero = true;
            }

            if (!Character.isLetterOrDigit(caracter)) {
                tieneSimbolo = true;
            }
        }

        return tieneMayuscula && tieneNumero && tieneSimbolo;
    }

    private void mostrarOcultarContrasena() {
        if (chkMostrarContrasena.isSelected()) {
            txtContrasena.setEchoChar((char) 0);
            txtConfirmarContrasena.setEchoChar((char) 0);
        } else {
            txtContrasena.setEchoChar('•');
            txtConfirmarContrasena.setEchoChar('•');
        }
    }

    private void actualizarLista() {
        modeloUsuarios.clear();

        gestorUsuarios.getUsuarios().forEach(usuario -> {
            modeloUsuarios.addElement(usuario.getUsername() + " - " + usuario.getTipo());
        });
    }
}