
package insta.interfaz;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class EditarPerfilPanel extends JPanel implements Tematizable {

    private JPanel panelEncabezado;
    private JPanel panelExterior;
    private JPanel panelFormulario;

    private JTextField txtNombre;
    private JComboBox<String> cmbGenero;
    private JSpinner spnEdad;

    private JPasswordField txtContrasenaActual;
    private JPasswordField txtContrasenaNueva;

    private JLabel lblFoto;

    private JButton btnCambiarFoto;
    private JButton btnGuardar;
    private JButton btnEstadoCuenta;

    public EditarPerfilPanel() {
        setLayout(new BorderLayout());

        crearEncabezado();
        crearFormulario();

        aplicarTema();
    }

    private void crearEncabezado() {
        panelEncabezado = new JPanel(new BorderLayout());
        panelEncabezado.setBorder(new EmptyBorder(20, 30, 15, 30));

        JLabel titulo = new JLabel("Editar perfil");
        titulo.setFont(new Font("Arial", Font.BOLD, 25));

        panelEncabezado.add(titulo, BorderLayout.WEST);

        add(panelEncabezado, BorderLayout.NORTH);
    }

    private void crearFormulario() {
        panelExterior = new JPanel(new GridBagLayout());

        panelFormulario = new JPanel();
        panelFormulario.setLayout(new BoxLayout(panelFormulario, BoxLayout.Y_AXIS));
        panelFormulario.setBorder(new EmptyBorder(30, 40, 30, 40));
        panelFormulario.setPreferredSize(new Dimension(500, 580));

        lblFoto = new JLabel("FOTO", SwingConstants.CENTER);
        lblFoto.setOpaque(true);
        lblFoto.setPreferredSize(new Dimension(100, 100));
        lblFoto.setMaximumSize(new Dimension(100, 100));
        lblFoto.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnCambiarFoto = new JButton("Cambiar foto");
        btnCambiarFoto.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtNombre = crearTexto();

        cmbGenero = new JComboBox<>(new String[]{"M", "F"});
        cmbGenero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        spnEdad = new JSpinner(new SpinnerNumberModel(18, 1, 120, 1));
        spnEdad.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        txtContrasenaActual = crearPassword();
        txtContrasenaNueva = crearPassword();

        btnGuardar = new JButton("Guardar cambios");
        btnGuardar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        btnEstadoCuenta = new JButton("Desactivar cuenta");
        btnEstadoCuenta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        panelFormulario.add(lblFoto);
        panelFormulario.add(Box.createVerticalStrut(10));
        panelFormulario.add(btnCambiarFoto);
        panelFormulario.add(Box.createVerticalStrut(25));

        agregarCampo("Nombre completo", txtNombre);
        agregarCampo("Género", cmbGenero);
        agregarCampo("Edad", spnEdad);
        agregarCampo("Contraseña actual", txtContrasenaActual);
        agregarCampo("Nueva contraseña", txtContrasenaNueva);

        panelFormulario.add(btnGuardar);
        panelFormulario.add(Box.createVerticalStrut(10));
        panelFormulario.add(btnEstadoCuenta);

        panelExterior.add(panelFormulario);

        add(panelExterior, BorderLayout.CENTER);
    }

    private JTextField crearTexto() {
        JTextField campo = new JTextField();
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        return campo;
    }

    private JPasswordField crearPassword() {
        JPasswordField campo = new JPasswordField();
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        return campo;
    }

    private void agregarCampo(String texto, JComponent componente) {
        JLabel label = new JLabel(texto);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        componente.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelFormulario.add(label);
        panelFormulario.add(Box.createVerticalStrut(5));
        panelFormulario.add(componente);
        panelFormulario.add(Box.createVerticalStrut(12));
    }

    @Override
    public void aplicarTema() {
        setBackground(TemaInsta.FONDO);

        panelEncabezado.setBackground(TemaInsta.FONDO);
        panelExterior.setBackground(TemaInsta.FONDO_SECUNDARIO);
        panelFormulario.setBackground(TemaInsta.TARJETA);

        lblFoto.setBackground(TemaInsta.INPUT);
        lblFoto.setForeground(TemaInsta.TEXTO_SECUNDARIO);

        txtNombre.setBackground(TemaInsta.INPUT);
        txtNombre.setForeground(TemaInsta.TEXTO);
        txtNombre.setCaretColor(TemaInsta.TEXTO);

        cmbGenero.setBackground(TemaInsta.INPUT);
        cmbGenero.setForeground(TemaInsta.TEXTO);

        JComponent editorEdad = spnEdad.getEditor();

        if (editorEdad instanceof JSpinner.DefaultEditor editor) {
            editor.getTextField().setBackground(TemaInsta.INPUT);
            editor.getTextField().setForeground(TemaInsta.TEXTO);
            editor.getTextField().setCaretColor(TemaInsta.TEXTO);
        }

        txtContrasenaActual.setBackground(TemaInsta.INPUT);
        txtContrasenaActual.setForeground(TemaInsta.TEXTO);
        txtContrasenaActual.setCaretColor(TemaInsta.TEXTO);

        txtContrasenaNueva.setBackground(TemaInsta.INPUT);
        txtContrasenaNueva.setForeground(TemaInsta.TEXTO);
        txtContrasenaNueva.setCaretColor(TemaInsta.TEXTO);

        btnCambiarFoto.setBackground(TemaInsta.INPUT);
        btnCambiarFoto.setForeground(TemaInsta.TEXTO);

        btnGuardar.setBackground(TemaInsta.BOTON);
        btnGuardar.setForeground(TemaInsta.BOTON_TEXTO);

        btnEstadoCuenta.setBackground(TemaInsta.INPUT);
        btnEstadoCuenta.setForeground(TemaInsta.TEXTO);

        cambiarTexto(this);

        revalidate();
        repaint();
    }

    private void cambiarTexto(Container contenedor) {
        for (Component componente : contenedor.getComponents()) {
            if (componente instanceof JLabel label && label != lblFoto) {
                label.setForeground(TemaInsta.TEXTO);
            }

            if (componente instanceof Container interno) {
                cambiarTexto(interno);
            }
        }
    }

    public JTextField getTxtNombre() {
        return txtNombre;
    }

    public JButton getBtnGuardar() {
        return btnGuardar;
    }

    public JButton getBtnEstadoCuenta() {
        return btnEstadoCuenta;
    }
}