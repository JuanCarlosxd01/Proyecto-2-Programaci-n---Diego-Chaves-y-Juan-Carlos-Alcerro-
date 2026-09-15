package interfaz;

import java.awt.*;
import javax.swing.*;
import modelo.UsuarioSistema;
import modelo.TipoUsuario;
import sistema.GestorUsuarios;
import sistema.Sesion;

public class AdministrarUsuariosPanel extends JPanel {

    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JPasswordField txtConfirmarContrasena;

    private JCheckBox chkMostrarContrasena;
    private JComboBox<String> cmbTipoUsuario;

    private JButton btnCrearUsuario;
    private JButton btnEliminarUsuario;

    private DefaultListModel<UsuarioSistema> modeloUsuarios;
    private JList<UsuarioSistema> listaUsuarios;

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

        JLabel titulo = new JLabel("Administrar usuarios");

        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));

        txtUsuario = new JTextField();

        txtContrasena = new JPasswordField();

        txtConfirmarContrasena = new JPasswordField();

        chkMostrarContrasena = new JCheckBox("Mostrar contraseñas");
        cmbTipoUsuario = new JComboBox<>(new String[]{"Estándar", "Administrador"});

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
        formulario.add(new JLabel("Tipo de usuario:"));
        formulario.add(cmbTipoUsuario);

        formulario.add(Box.createVerticalStrut(10));

        JLabel lblRequisitos = new JLabel(
                "<html>"
                + "La contraseña debe contener:<br>"
                + "• Mínimo 8 caracteres<br>"
                + "• Una mayúscula<br>"
                + "• Un número<br>"
                + "• Un símbolo"
                + "</html>"
        );

        lblRequisitos.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        formulario.add(lblRequisitos);

        formulario.add(Box.createVerticalStrut(20));

        formulario.add(btnCrearUsuario);

        add(formulario, BorderLayout.WEST);

        btnCrearUsuario.addActionListener(e -> crearUsuario());

        chkMostrarContrasena.addActionListener(e -> mostrarOcultarContrasena());
    }

    private void crearListaUsuarios() {
        JPanel panelLista = new JPanel(new BorderLayout(10, 10));

        JLabel tituloLista = new JLabel("Usuarios registrados");

        tituloLista.setFont(new Font("Segoe UI", Font.BOLD, 18));

        modeloUsuarios = new DefaultListModel<>();

        listaUsuarios = new JList<>(modeloUsuarios);

        listaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        listaUsuarios.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list,
                        value,
                        index,
                        isSelected,
                        cellHasFocus
                );

                if (value instanceof UsuarioSistema usuario) {
                    label.setText(usuario.getUsername() + " - " + usuario.getTipo());
                }

                return label;
            }
        });

        JScrollPane scroll = new JScrollPane(listaUsuarios);

        btnEliminarUsuario = new JButton("Eliminar usuario seleccionado");

        btnEliminarUsuario.addActionListener(e -> eliminarUsuario());

        panelLista.add(tituloLista, BorderLayout.NORTH);
        panelLista.add(scroll, BorderLayout.CENTER);
        panelLista.add(btnEliminarUsuario, BorderLayout.SOUTH);

        panelLista.setPreferredSize(new Dimension(330, 0));

        add(panelLista, BorderLayout.CENTER);
    }

    private void crearUsuario() {
        if (!Sesion.esAdministrador()) {
            DialogosWindows.showMessageDialog(
                    this,
                    "Solo el administrador puede crear usuarios.",
                    "Acceso denegado",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        String username = txtUsuario.getText().trim();

        String contrasena = new String(txtContrasena.getPassword());

        String confirmarContrasena = new String(txtConfirmarContrasena.getPassword());

        if (username.isEmpty()) {
            DialogosWindows.showMessageDialog(this, "Ingrese un nombre de usuario.");

            return;
        }

        if (contrasena.isEmpty()) {
            DialogosWindows.showMessageDialog(this, "Ingrese una contraseña.");

            return;
        }

        if (confirmarContrasena.isEmpty()) {
            DialogosWindows.showMessageDialog(this, "Confirme la contraseña.");

            return;
        }

        if (!contrasena.equals(confirmarContrasena)) {
            DialogosWindows.showMessageDialog(
                    this,
                    "Las contraseñas no coinciden.",
                    "Contraseña incorrecta",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        if (!contrasenaValida(contrasena)) {
            DialogosWindows.showMessageDialog(
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

        TipoUsuario tipo = cmbTipoUsuario.getSelectedItem().equals("Administrador") ? TipoUsuario.ADMINISTRADOR : TipoUsuario.ESTANDAR;
        boolean creado = gestorUsuarios.crearUsuario(username, contrasena, tipo);

        if (creado) {
            DialogosWindows.showMessageDialog(
                    this,
                    "Usuario creado correctamente."
            );

            txtUsuario.setText("");
            txtContrasena.setText("");
            txtConfirmarContrasena.setText("");
            cmbTipoUsuario.setSelectedIndex(0);

            actualizarLista();

        } else {
            DialogosWindows.showMessageDialog(
                    this,
                    "No se pudo crear el usuario. Puede que el nombre ya exista.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void eliminarUsuario() {
        if (!Sesion.esAdministrador()) {
            DialogosWindows.showMessageDialog(
                    this,
                    "Solo el administrador puede eliminar usuarios.",
                    "Acceso denegado",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        UsuarioSistema seleccionado = listaUsuarios.getSelectedValue();

        if (seleccionado == null) {
            DialogosWindows.showMessageDialog(
                    this,
                    "Seleccione un usuario de la lista."
            );

            return;
        }

        if (seleccionado.esAdministrador()) {
            DialogosWindows.showMessageDialog(
                    this,
                    "El usuario administrador principal no puede eliminarse.",
                    "Acción no permitida",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int opcion = DialogosWindows.showConfirmDialog(this, "¿Está seguro de eliminar al usuario " + seleccionado.getUsername() + "?\n\nTambién se eliminará su carpeta de archivos.", "Eliminar usuario", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        boolean eliminado = gestorUsuarios.eliminarUsuario(
                seleccionado.getUsername()
        );

        if (eliminado) {
            DialogosWindows.showMessageDialog(
                    this,
                    "Usuario eliminado correctamente."
            );

            actualizarLista();

        } else {
            DialogosWindows.showMessageDialog(
                    this,
                    "No se pudo eliminar el usuario.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private boolean contrasenaValida(String contrasena) {
        return UsuarioSistema.contrasenaValida(contrasena);
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

        for (UsuarioSistema usuario : gestorUsuarios.getUsuarios()) {
            modeloUsuarios.addElement(usuario);
        }
    }
}