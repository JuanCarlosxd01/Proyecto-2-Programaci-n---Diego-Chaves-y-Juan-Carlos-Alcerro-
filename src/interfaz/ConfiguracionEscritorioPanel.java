package interfaz;

import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import modelo.ConfiguracionSistema;
import modelo.UsuarioSistema;
import sistema.GestorUsuarios;
import sistema.Sesion;

public class ConfiguracionEscritorioPanel extends JPanel {

    public interface Listener {
        void seleccionarFondo(File archivo);
        void seleccionarColor(Color color);
        void restaurarFondo();
        void cuentaFinalizada();
    }

    private GestorUsuarios gestorUsuarios;
    private Listener listener;

    public ConfiguracionEscritorioPanel(GestorUsuarios gestorUsuarios, Listener listener) {
        this.gestorUsuarios = gestorUsuarios;
        this.listener = listener;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        JLabel titulo = new JLabel("Configuración", SwingConstants.LEFT);
        titulo.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        titulo.setBorder(BorderFactory.createEmptyBorder(25, 30, 10, 30));
        add(titulo, BorderLayout.NORTH);

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(BorderFactory.createEmptyBorder(15, 30, 30, 30));
        crearSeccionCuenta(contenido);
        contenido.add(Box.createVerticalStrut(30));
        crearSeccionPersonalizacion(contenido);

        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(245, 245, 245));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private void crearSeccionCuenta(JPanel contenido) {
        UsuarioSistema actual = Sesion.getUsuarioActual();
        JLabel cuenta = new JLabel("Cuenta");
        cuenta.setFont(new Font("Segoe UI", Font.BOLD, 19));
        cuenta.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(cuenta);
        contenido.add(Box.createVerticalStrut(8));

        String nombre = actual == null ? "Sin sesión" : actual.getUsername();
        JLabel usuario = new JLabel("Usuario actual: " + nombre);
        usuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usuario.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(usuario);
        contenido.add(Box.createVerticalStrut(15));

        JButton btnCambiar = crearBoton("Cambiar contraseña");
        JButton btnDesactivar = crearBoton("Desactivar mi cuenta");
        JButton btnEliminar = crearBoton("Eliminar mi cuenta");
        boolean principal = actual != null && actual.getUsername().equalsIgnoreCase(ConfiguracionSistema.ADMIN_USUARIO);
        btnDesactivar.setEnabled(!principal);
        btnEliminar.setEnabled(!principal);
        if (principal) {
            btnDesactivar.setToolTipText("El administrador principal no puede desactivarse.");
            btnEliminar.setToolTipText("El administrador principal no puede eliminarse.");
        }
        btnCambiar.addActionListener(e -> cambiarContrasena());
        btnDesactivar.addActionListener(e -> desactivarMiCuenta());
        btnEliminar.addActionListener(e -> eliminarMiCuenta());
        contenido.add(btnCambiar);
        contenido.add(Box.createVerticalStrut(8));
        contenido.add(btnDesactivar);
        contenido.add(Box.createVerticalStrut(8));
        contenido.add(btnEliminar);
    }

    private void crearSeccionPersonalizacion(JPanel contenido) {
        JLabel fondo = new JLabel("Personalización");
        fondo.setFont(new Font("Segoe UI", Font.BOLD, 19));
        fondo.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(fondo);
        contenido.add(Box.createVerticalStrut(8));

        JLabel subtitulo = new JLabel("Fondo del escritorio");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(subtitulo);
        contenido.add(Box.createVerticalStrut(15));

        JButton btnImagen = crearBoton("Examinar una imagen...");
        JButton btnColor = crearBoton("Elegir color sólido...");
        JButton btnPredeterminado = crearBoton("Restaurar fondo predeterminado");
        btnImagen.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "png", "jpg", "jpeg", "bmp", "gif"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) listener.seleccionarFondo(chooser.getSelectedFile());
        });
        btnColor.addActionListener(e -> {
            Color color = JColorChooser.showDialog(this, "Elegir color del escritorio", new Color(18, 56, 92));
            if (color != null) listener.seleccionarColor(color);
        });
        btnPredeterminado.addActionListener(e -> listener.restaurarFondo());
        contenido.add(btnImagen);
        contenido.add(Box.createVerticalStrut(8));
        contenido.add(btnColor);
        contenido.add(Box.createVerticalStrut(8));
        contenido.add(btnPredeterminado);
        contenido.add(Box.createVerticalStrut(18));
        JLabel nota = new JLabel("<html>El fondo seleccionado se guarda para el usuario actual y se vuelve a aplicar al iniciar sesión.</html>");
        nota.setForeground(new Color(90, 90, 90));
        nota.setMaximumSize(new Dimension(500, 80));
        nota.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(nota);
    }

    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setMaximumSize(new Dimension(300, 38));
        boton.setPreferredSize(new Dimension(300, 38));
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setFocusPainted(false);
        return boton;
    }

    private void cambiarContrasena() {
        UsuarioSistema actual = Sesion.getUsuarioActual();
        if (actual == null) return;
        JPasswordField actualField = new JPasswordField();
        JPasswordField nuevaField = new JPasswordField();
        JPasswordField confirmarField = new JPasswordField();
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Contraseña actual:"));
        panel.add(actualField);
        panel.add(new JLabel("Nueva contraseña:"));
        panel.add(nuevaField);
        panel.add(new JLabel("Confirmar nueva contraseña:"));
        panel.add(confirmarField);
        int opcion = DialogosWindows.showConfirmDialog(this, panel, "Cambiar contraseña", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) return;
        String claveActual = new String(actualField.getPassword());
        String nueva = new String(nuevaField.getPassword());
        String confirmar = new String(confirmarField.getPassword());
        if (!actual.getContrasena().equals(claveActual)) {
            DialogosWindows.showMessageDialog(this, "La contraseña actual es incorrecta.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!nueva.equals(confirmar)) {
            DialogosWindows.showMessageDialog(this, "Las nuevas contraseñas no coinciden.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!UsuarioSistema.contrasenaValida(nueva)) {
            DialogosWindows.showMessageDialog(this, "La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un símbolo.", "Contraseña no válida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmarCambio = DialogosWindows.showConfirmDialog(this, "¿Está seguro de cambiar su contraseña?", "Confirmar cambio", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmarCambio != JOptionPane.YES_OPTION) return;
        if (gestorUsuarios.cambiarContrasena(actual.getUsername(), nueva)) {
            DialogosWindows.showMessageDialog(this, "Contraseña cambiada correctamente.");
        } else {
            DialogosWindows.showMessageDialog(this, "No se pudo cambiar la contraseña.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void desactivarMiCuenta() {
        UsuarioSistema actual = Sesion.getUsuarioActual();
        if (actual == null) return;
        if (actual.getUsername().equalsIgnoreCase(ConfiguracionSistema.ADMIN_USUARIO)) {
            DialogosWindows.showMessageDialog(this, "El administrador principal no puede desactivar su cuenta.", "Acción no permitida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int opcion = DialogosWindows.showConfirmDialog(this, "¿Está seguro de desactivar su cuenta?\n\nSe cerrará la sesión y no podrá volver a entrar hasta que el administrador principal la active.", "Desactivar cuenta", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) return;
        if (gestorUsuarios.desactivarUsuario(actual.getUsername())) {
            DialogosWindows.showMessageDialog(this, "Su cuenta fue desactivada. Se cerrará la sesión.");
            listener.cuentaFinalizada();
        } else {
            DialogosWindows.showMessageDialog(this, "No se pudo desactivar la cuenta.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarMiCuenta() {
        UsuarioSistema actual = Sesion.getUsuarioActual();
        if (actual == null) return;
        if (actual.getUsername().equalsIgnoreCase(ConfiguracionSistema.ADMIN_USUARIO)) {
            DialogosWindows.showMessageDialog(this, "El administrador principal no puede eliminar su cuenta.", "Acción no permitida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int opcion = DialogosWindows.showConfirmDialog(this, "¿Está seguro de eliminar permanentemente su cuenta?\n\nTambién se eliminará su carpeta de archivos. Esta acción no se puede deshacer.", "Eliminar cuenta", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) return;
        int segundaConfirmacion = DialogosWindows.showConfirmDialog(this, "Esta es la última confirmación. ¿Eliminar definitivamente la cuenta " + actual.getUsername() + "?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
        if (segundaConfirmacion != JOptionPane.YES_OPTION) return;
        if (gestorUsuarios.eliminarUsuario(actual.getUsername())) {
            DialogosWindows.showMessageDialog(this, "La cuenta fue eliminada. Se cerrará la sesión.");
            listener.cuentaFinalizada();
        } else {
            DialogosWindows.showMessageDialog(this, "No se pudo eliminar la cuenta.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
