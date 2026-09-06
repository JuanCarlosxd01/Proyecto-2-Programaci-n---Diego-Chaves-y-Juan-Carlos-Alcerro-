
package interfaz;

import java.awt.*;
import javax.swing.*;
import sistema.GestorUsuarios;
import sistema.Sesion;

public class AdministrarUsuariosPanel extends JPanel {
    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
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
        btnCrearUsuario = new JButton("Crear usuario");

        formulario.add(titulo);
        formulario.add(Box.createVerticalStrut(20));
        formulario.add(new JLabel("Nombre de usuario:"));
        formulario.add(txtUsuario);
        formulario.add(Box.createVerticalStrut(10));
        formulario.add(new JLabel("Contraseña:"));
        formulario.add(txtContrasena);
        formulario.add(Box.createVerticalStrut(20));
        formulario.add(btnCrearUsuario);

        add(formulario, BorderLayout.WEST);
        btnCrearUsuario.addActionListener(e -> crearUsuario());
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

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un nombre de usuario.");
            return;
        }

        if (contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese una contraseña.");
            return;
        }

        boolean creado = gestorUsuarios.crearUsuario(username, contrasena);

        if (creado) {
            JOptionPane.showMessageDialog(this, "Usuario creado correctamente.");
            txtUsuario.setText("");
            txtContrasena.setText("");
            actualizarLista();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo crear el usuario. Puede que el nombre ya exista.");
        }
    }

    private void actualizarLista() {
        modeloUsuarios.clear();
        gestorUsuarios.getUsuarios().forEach(usuario -> {
            modeloUsuarios.addElement(usuario.getUsername() + " - " + usuario.getTipo());
        });
    }
}