package interfaz;

import java.awt.*;
import javax.swing.*;
import modelo.ConfiguracionSistema;
import modelo.TipoUsuario;
import modelo.UsuarioSistema;
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
    private JButton btnCambiarContrasena;
    private JButton btnActivarUsuario;
    private JButton btnDesactivarUsuario;
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
        JPanel panelLista = new JPanel(new BorderLayout(10, 10));
        JLabel tituloLista = new JLabel("Usuarios registrados");
        tituloLista.setFont(new Font("Segoe UI", Font.BOLD, 18));
        modeloUsuarios = new DefaultListModel<>();
        listaUsuarios = new JList<>(modeloUsuarios);
        listaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaUsuarios.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof UsuarioSistema usuario) {
                    String tipo = esAdminPrincipal(usuario) ? "ADMIN PRINCIPAL" : usuario.getTipo().toString();
                    String estado = usuario.estaActivo() ? "" : " - <font color='#D32F2F'><b>DESACTIVADA</b></font>";
                    label.setText("<html>" + usuario.getUsername() + " - " + tipo + estado + "</html>");
                }
                return label;
            }
        });
        JScrollPane scroll = new JScrollPane(listaUsuarios);
        btnCambiarContrasena = new JButton("Cambiar contraseña");
        btnActivarUsuario = new JButton("Activar cuenta");
        btnDesactivarUsuario = new JButton("Desactivar cuenta");
        btnEliminarUsuario = new JButton("Eliminar usuario");
        JPanel acciones = new JPanel(new GridLayout(2, 2, 8, 8));
        acciones.add(btnCambiarContrasena);
        acciones.add(btnActivarUsuario);
        acciones.add(btnDesactivarUsuario);
        acciones.add(btnEliminarUsuario);
        btnCambiarContrasena.addActionListener(e -> cambiarContrasena());
        btnActivarUsuario.addActionListener(e -> activarUsuario());
        btnDesactivarUsuario.addActionListener(e -> desactivarUsuario());
        btnEliminarUsuario.addActionListener(e -> eliminarUsuario());
        listaUsuarios.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) actualizarBotones();
        });
        panelLista.add(tituloLista, BorderLayout.NORTH);
        panelLista.add(scroll, BorderLayout.CENTER);
        panelLista.add(acciones, BorderLayout.SOUTH);
        panelLista.setPreferredSize(new Dimension(430, 0));
        add(panelLista, BorderLayout.CENTER);
        actualizarBotones();
    }

    private void crearUsuario() {
        if (!Sesion.esAdministrador()) {
            DialogosWindows.showMessageDialog(this, "Solo un administrador puede crear usuarios.", "Acceso denegado", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String username = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());
        String confirmarContrasena = new String(txtConfirmarContrasena.getPassword());
        if (username.isEmpty()) {
            DialogosWindows.showMessageDialog(this, "Ingrese un nombre de usuario.");
            return;
        }
        if (contrasena.isEmpty() || confirmarContrasena.isEmpty()) {
            DialogosWindows.showMessageDialog(this, "Ingrese y confirme la contraseña.");
            return;
        }
        if (!contrasena.equals(confirmarContrasena)) {
            DialogosWindows.showMessageDialog(this, "Las contraseñas no coinciden.", "Contraseña incorrecta", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!UsuarioSistema.contrasenaValida(contrasena)) {
            DialogosWindows.showMessageDialog(this, "La contraseña debe tener:\n\n- Mínimo 8 caracteres\n- Al menos una letra mayúscula\n- Al menos un número\n- Al menos un símbolo", "Contraseña no válida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        TipoUsuario tipo = cmbTipoUsuario.getSelectedItem().equals("Administrador") ? TipoUsuario.ADMINISTRADOR : TipoUsuario.ESTANDAR;
        boolean creado = gestorUsuarios.crearUsuario(username, contrasena, tipo);
        if (creado) {
            DialogosWindows.showMessageDialog(this, "Usuario creado correctamente.");
            txtUsuario.setText("");
            txtContrasena.setText("");
            txtConfirmarContrasena.setText("");
            cmbTipoUsuario.setSelectedIndex(0);
            actualizarLista();
        } else {
            DialogosWindows.showMessageDialog(this, "No se pudo crear el usuario. Puede que el nombre ya exista.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cambiarContrasena() {
        UsuarioSistema seleccionado = obtenerSeleccionado();
        if (seleccionado == null) return;
        UsuarioSistema actual = Sesion.getUsuarioActual();
        boolean principal = esAdminPrincipal(actual);
        boolean propia = actual != null && actual.getUsername().equalsIgnoreCase(seleccionado.getUsername());
        if (!principal && !propia) {
            DialogosWindows.showMessageDialog(this, "Los administradores secundarios solo pueden cambiar su propia contraseña.", "Acceso denegado", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JPasswordField nueva = new JPasswordField();
        JPasswordField confirmar = new JPasswordField();
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Nueva contraseña para " + seleccionado.getUsername() + ":"));
        panel.add(nueva);
        panel.add(new JLabel("Confirmar contraseña:"));
        panel.add(confirmar);
        int opcion = DialogosWindows.showConfirmDialog(this, panel, "Cambiar contraseña", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) return;
        String clave = new String(nueva.getPassword());
        String confirmacion = new String(confirmar.getPassword());
        if (!clave.equals(confirmacion)) {
            DialogosWindows.showMessageDialog(this, "Las contraseñas no coinciden.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!UsuarioSistema.contrasenaValida(clave)) {
            DialogosWindows.showMessageDialog(this, "La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un símbolo.", "Contraseña no válida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmarCambio = DialogosWindows.showConfirmDialog(this, "¿Confirma el cambio de contraseña de " + seleccionado.getUsername() + "?", "Confirmar cambio", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmarCambio != JOptionPane.YES_OPTION) return;
        if (gestorUsuarios.cambiarContrasena(seleccionado.getUsername(), clave)) {
            DialogosWindows.showMessageDialog(this, "Contraseña cambiada correctamente.");
        } else {
            DialogosWindows.showMessageDialog(this, "No se pudo cambiar la contraseña.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void activarUsuario() {
        UsuarioSistema seleccionado = obtenerSeleccionado();

        if (seleccionado == null) {
            return;
        }

        UsuarioSistema actual = Sesion.getUsuarioActual();

        if (actual == null || !actual.esAdministrador()) {
            DialogosWindows.showMessageDialog(this, "Solo un administrador puede activar cuentas.", "Acceso denegado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean principal = esAdminPrincipal(actual);

        if (!principal && seleccionado.esAdministrador()) {
            DialogosWindows.showMessageDialog(this, "Solo el administrador principal puede activar la cuenta de otro administrador.", "Acceso denegado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (seleccionado.estaActivo()) {
            DialogosWindows.showMessageDialog(this, "La cuenta ya está activa.");
            return;
        }

        int opcion = DialogosWindows.showConfirmDialog(this, "¿Desea activar la cuenta de " + seleccionado.getUsername() + "?", "Activar cuenta", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        if (gestorUsuarios.activarUsuario(seleccionado.getUsername())) {
            DialogosWindows.showMessageDialog(this, "Cuenta activada correctamente.");
            actualizarLista();
        } else {
            DialogosWindows.showMessageDialog(this, "No se pudo activar la cuenta.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void desactivarUsuario() {
        UsuarioSistema seleccionado = obtenerSeleccionado();

        if (seleccionado == null) {
            return;
        }

        UsuarioSistema actual = Sesion.getUsuarioActual();

        if (actual == null) {
            return;
        }

        if (esAdminPrincipal(seleccionado)) {
            DialogosWindows.showMessageDialog(this, "El administrador principal no puede desactivarse.", "Acción no permitida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean principal = esAdminPrincipal(actual);
        boolean administrador = actual.esAdministrador();
        boolean propia = actual.getUsername().equalsIgnoreCase(seleccionado.getUsername());
        boolean seleccionadoEsAdministrador = seleccionado.esAdministrador();

        if (!principal && seleccionadoEsAdministrador && !propia) {
            DialogosWindows.showMessageDialog(this, "Solo el administrador principal puede desactivar a otro administrador.", "Acceso denegado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!administrador && !propia) {
            DialogosWindows.showMessageDialog(this, "Solo puede desactivar su propia cuenta.", "Acceso denegado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!seleccionado.estaActivo()) {
            DialogosWindows.showMessageDialog(this, "La cuenta ya está desactivada.");
            return;
        }

        int opcion = DialogosWindows.showConfirmDialog(this, "¿Está seguro de desactivar la cuenta de " + seleccionado.getUsername() + "?\n\nNo podrá iniciar sesión hasta que vuelva a activarse.", "Desactivar cuenta", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        if (gestorUsuarios.desactivarUsuario(seleccionado.getUsername())) {
            DialogosWindows.showMessageDialog(this, "Cuenta desactivada correctamente.");
            actualizarLista();
        } else {
            DialogosWindows.showMessageDialog(this, "No se pudo desactivar la cuenta.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void eliminarUsuario() {
        UsuarioSistema seleccionado = obtenerSeleccionado();

        if (seleccionado == null) {
            return;
        }

        UsuarioSistema actual = Sesion.getUsuarioActual();

        if (actual == null) {
            return;
        }

        if (esAdminPrincipal(seleccionado)) {
            DialogosWindows.showMessageDialog(this, "El administrador principal no puede eliminarse.", "Acción no permitida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean principal = esAdminPrincipal(actual);
        boolean administrador = actual.esAdministrador();
        boolean propia = actual.getUsername().equalsIgnoreCase(seleccionado.getUsername());
        boolean seleccionadoEsAdministrador = seleccionado.esAdministrador();

        if (!principal && seleccionadoEsAdministrador && !propia) {
            DialogosWindows.showMessageDialog(this, "Solo el administrador principal puede eliminar a otro administrador.", "Acceso denegado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!administrador && !propia) {
            DialogosWindows.showMessageDialog(this, "Solo puede eliminar su propia cuenta.", "Acceso denegado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int opcion = DialogosWindows.showConfirmDialog(this, "¿Está seguro de eliminar al usuario " + seleccionado.getUsername() + "?\n\nTambién se eliminará su carpeta de archivos.", "Eliminar usuario", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        if (gestorUsuarios.eliminarUsuario(seleccionado.getUsername())) {
            DialogosWindows.showMessageDialog(this, "Usuario eliminado correctamente.");
            actualizarLista();
        } else {
            DialogosWindows.showMessageDialog(this, "No se pudo eliminar el usuario.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private UsuarioSistema obtenerSeleccionado() {
        UsuarioSistema seleccionado = listaUsuarios.getSelectedValue();
        if (seleccionado == null) DialogosWindows.showMessageDialog(this, "Seleccione un usuario de la lista.");
        return seleccionado;
    }

    private boolean esAdminPrincipal(UsuarioSistema usuario) {
        return usuario != null && usuario.getUsername().equalsIgnoreCase(ConfiguracionSistema.ADMIN_USUARIO);
    }

    private void mostrarOcultarContrasena() {
        char eco = chkMostrarContrasena.isSelected() ? (char) 0 : '•';
        txtContrasena.setEchoChar(eco);
        txtConfirmarContrasena.setEchoChar(eco);
    }

    private void actualizarLista() {
        String seleccionado = listaUsuarios.getSelectedValue() == null ? null : listaUsuarios.getSelectedValue().getUsername();
        modeloUsuarios.clear();
        for (UsuarioSistema usuario : gestorUsuarios.getUsuarios()) modeloUsuarios.addElement(usuario);
        if (seleccionado != null) {
            for (int i = 0; i < modeloUsuarios.size(); i++) {
                if (modeloUsuarios.get(i).getUsername().equalsIgnoreCase(seleccionado)) {
                    listaUsuarios.setSelectedIndex(i);
                    break;
                }
            }
        }
        actualizarBotones();
    }

    private void actualizarBotones() {
        UsuarioSistema seleccionado = listaUsuarios == null ? null : listaUsuarios.getSelectedValue();
        UsuarioSistema actual = Sesion.getUsuarioActual();

        if (seleccionado == null || actual == null) {
            btnCambiarContrasena.setEnabled(false);
            btnActivarUsuario.setEnabled(false);
            btnDesactivarUsuario.setEnabled(false);
            btnEliminarUsuario.setEnabled(false);
            return;
        }

        boolean principal = esAdminPrincipal(actual);
        boolean administrador = actual.esAdministrador();
        boolean propia = seleccionado.getUsername().equalsIgnoreCase(actual.getUsername());
        boolean seleccionadoPrincipal = esAdminPrincipal(seleccionado);
        boolean seleccionadoAdministrador = seleccionado.esAdministrador();

        boolean puedeAdministrarSeleccionado = principal || propia || (administrador && !seleccionadoAdministrador);

        btnCambiarContrasena.setEnabled(puedeAdministrarSeleccionado);
        btnActivarUsuario.setEnabled(administrador && !seleccionado.estaActivo() && (principal || !seleccionadoAdministrador));
        btnDesactivarUsuario.setEnabled(seleccionado.estaActivo() && !seleccionadoPrincipal && puedeAdministrarSeleccionado);
        btnEliminarUsuario.setEnabled(!seleccionadoPrincipal && puedeAdministrarSeleccionado);
    }
}
