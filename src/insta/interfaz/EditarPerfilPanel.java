
package insta.interfaz;

import interfaz.DialogosWindows;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import red.Cliente;
import red.Respuesta;

public class EditarPerfilPanel extends JPanel implements Tematizable {

    private JPanel panelEncabezado;
    private JPanel panelExterior;
    private JPanel panelFormulario;

    private JTextField txtNombre;
    private JTextArea txtBiografia;
    private JComboBox<String> cmbGenero;
    private JSpinner spnEdad;

    private JPasswordField txtContrasenaActual;
    private JPasswordField txtContrasenaNueva;

    private JLabel lblFoto;

    private JButton btnCambiarFoto;
    private JButton btnGuardar;
    private JButton btnEstadoCuenta;

    private Cliente cliente;
    private boolean cuentaActiva = true;
    private Runnable accionPerfilActualizado;

    public EditarPerfilPanel(Cliente cliente) {
        this.cliente = cliente;

        setLayout(new BorderLayout());

        crearEncabezado();
        crearFormulario();
        configurarEventos();

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
        panelFormulario.setPreferredSize(new Dimension(500, 660));

        lblFoto = new JLabel("FOTO", SwingConstants.CENTER);
        lblFoto.setOpaque(true);
        lblFoto.setPreferredSize(new Dimension(100, 100));
        lblFoto.setMaximumSize(new Dimension(100, 100));
        lblFoto.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnCambiarFoto = new JButton("Cambiar foto");
        btnCambiarFoto.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtNombre = crearTexto();
        txtBiografia = new JTextArea(3, 20);
        txtBiografia.setLineWrap(true);
        txtBiografia.setWrapStyleWord(true);

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
        agregarCampo("Biografía (máximo 160 caracteres)", new JScrollPane(txtBiografia));
        agregarCampo("Género", cmbGenero);
        agregarCampo("Edad", spnEdad);
        agregarCampo("Contraseña actual", txtContrasenaActual);
        agregarCampo("Nueva contraseña (opcional)", txtContrasenaNueva);

        panelFormulario.add(btnGuardar);
        panelFormulario.add(Box.createVerticalStrut(10));
        panelFormulario.add(btnEstadoCuenta);

        panelExterior.add(panelFormulario);

        add(panelExterior, BorderLayout.CENTER);
    }

    private void configurarEventos() {
        btnGuardar.addActionListener(e -> guardarCambios());
        btnCambiarFoto.addActionListener(e -> cambiarFoto());
        btnEstadoCuenta.addActionListener(e -> cambiarEstadoCuenta());
    }

    public void cargarDatos() {
        Respuesta.DatosUsuario usuario = cliente.getUsuarioActual();

        if (usuario == null) {
            return;
        }

        txtNombre.setText(usuario.getNombreCompleto());
        txtBiografia.setText(usuario.getBiografia());
        cmbGenero.setSelectedItem(String.valueOf(usuario.getGenero()));
        spnEdad.setValue(usuario.getEdad());

        cuentaActiva = usuario.estaActiva();

        actualizarBotonEstado();

        if (usuario.getRutaFotoPerfil() != null && !usuario.getRutaFotoPerfil().isBlank()) {
            cargarFoto(usuario.getRutaFotoPerfil());
        }
    }

    private void guardarCambios() {
        String nombre = txtNombre.getText().trim();
        char genero = cmbGenero.getSelectedItem().toString().charAt(0);
        int edad = (Integer) spnEdad.getValue();

        String actual = new String(txtContrasenaActual.getPassword());
        String nueva = new String(txtContrasenaNueva.getPassword());
        String biografia = txtBiografia.getText().trim();

        if (nombre.isBlank()) {
            DialogosWindows.showMessageDialog(this, "Ingrese su nombre completo.", "Editar perfil", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (biografia.codePointCount(0, biografia.length()) > 160) {
            DialogosWindows.showMessageDialog(this, "La biografía permite máximo 160 caracteres.", "Editar perfil", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (actual.isBlank()) {
            DialogosWindows.showMessageDialog(this, "Debe ingresar su contraseña actual para guardar los cambios.", "Editar perfil", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnGuardar.setEnabled(false);

        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {

            @Override
            protected Respuesta doInBackground() throws Exception {
                return cliente.editarPerfil(nombre, genero, edad, actual, nueva, biografia);
            }

            @Override
            protected void done() {
                btnGuardar.setEnabled(true);

                try {
                    Respuesta respuesta = get();

                    if (respuesta.esExitosa()) {
                        DialogosWindows.showMessageDialog(EditarPerfilPanel.this, "Perfil actualizado correctamente.", "INSTA+", JOptionPane.INFORMATION_MESSAGE);

                        txtContrasenaActual.setText("");
                        txtContrasenaNueva.setText("");

                        cargarDatos();
                        if (accionPerfilActualizado != null) accionPerfilActualizado.run();
                    } else {
                        DialogosWindows.showMessageDialog(EditarPerfilPanel.this, respuesta.getMensaje(), "Editar perfil", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (Exception e) {
                    DialogosWindows.showMessageDialog(EditarPerfilPanel.this, "No se pudieron guardar los cambios.\n" + obtenerMensaje(e), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        trabajador.execute();
    }

    private void cambiarFoto() {
        JFileChooser selector = new JFileChooser();

        selector.setDialogTitle("Seleccionar foto de perfil");
        selector.setFileFilter(new FileNameExtensionFilter("Imágenes", "png", "jpg", "jpeg"));

        int opcion = selector.showOpenDialog(this);

        if (opcion != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = selector.getSelectedFile();

        try {
            byte[] bytes = Files.readAllBytes(archivo.toPath());

            if (bytes.length > 4 * 1024 * 1024) {
                DialogosWindows.showMessageDialog(this, "La imagen no puede superar 4 MB.", "Foto de perfil", JOptionPane.WARNING_MESSAGE);
                return;
            }

            btnCambiarFoto.setEnabled(false);

            SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {

                @Override
                protected Respuesta doInBackground() throws Exception {
                    return cliente.cambiarFoto(bytes);
                }

                @Override
                protected void done() {
                    btnCambiarFoto.setEnabled(true);

                    try {
                        Respuesta respuesta = get();

                        if (respuesta.esExitosa()) {
                            mostrarFoto(bytes);
                            if (accionPerfilActualizado != null) accionPerfilActualizado.run();

                            DialogosWindows.showMessageDialog(EditarPerfilPanel.this, "Foto de perfil actualizada.", "INSTA+", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            DialogosWindows.showMessageDialog(EditarPerfilPanel.this, respuesta.getMensaje(), "Foto de perfil", JOptionPane.ERROR_MESSAGE);
                        }

                    } catch (Exception e) {
                        DialogosWindows.showMessageDialog(EditarPerfilPanel.this, "No se pudo cambiar la foto.\n" + obtenerMensaje(e), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            trabajador.execute();

        } catch (Exception e) {
            DialogosWindows.showMessageDialog(this, "No se pudo leer la imagen.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cambiarEstadoCuenta() {
        if (cuentaActiva) {
            int opcion = DialogosWindows.showConfirmDialog(this, "¿Está seguro de que desea desactivar su cuenta?", "Estado de cuenta", JOptionPane.YES_NO_OPTION);
            if (opcion != JOptionPane.YES_OPTION) {
                return;
            }
        }

        btnEstadoCuenta.setEnabled(false);

        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {

            @Override
            protected Respuesta doInBackground() throws Exception {
                if (cuentaActiva) {
                    return cliente.desactivarCuenta();
                }

                return cliente.reactivarCuenta();
            }

            @Override
            protected void done() {
                btnEstadoCuenta.setEnabled(true);

                try {
                    Respuesta respuesta = get();

                    if (respuesta.esExitosa()) {
                        cuentaActiva = !cuentaActiva;

                        actualizarBotonEstado();

                        DialogosWindows.showMessageDialog(EditarPerfilPanel.this, respuesta.getMensaje(), "INSTA+", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        DialogosWindows.showMessageDialog(EditarPerfilPanel.this, respuesta.getMensaje(), "INSTA+", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (Exception e) {
                    DialogosWindows.showMessageDialog(EditarPerfilPanel.this, "No se pudo cambiar el estado de la cuenta.\n" + obtenerMensaje(e), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        trabajador.execute();
    }

    private void actualizarBotonEstado() {
        if (cuentaActiva) {
            btnEstadoCuenta.setText("Desactivar cuenta");
        } else {
            btnEstadoCuenta.setText("Reactivar cuenta");
        }
    }

    private void cargarFoto(String ruta) {
        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {

            @Override
            protected Respuesta doInBackground() throws Exception {
                return cliente.descargarImagen(ruta);
            }

            @Override
            protected void done() {
                try {
                    Respuesta respuesta = get();

                    if (respuesta.esExitosa() && respuesta.getArchivo() != null) {
                        mostrarFoto(respuesta.getArchivo());
                    }

                } catch (Exception e) {
                    lblFoto.setText("FOTO");
                }
            }
        };

        trabajador.execute();
    }

    private void mostrarFoto(byte[] bytes) {
        ImageIcon original = new ImageIcon(bytes);
        Image imagen = original.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);

        lblFoto.setText("");
        lblFoto.setIcon(new ImageIcon(imagen));
    }

    private String obtenerMensaje(Exception e) {
        Throwable causa = e;

        while (causa.getCause() != null) {
            causa = causa.getCause();
        }

        return causa.getMessage() == null ? "Error desconocido." : causa.getMessage();
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

        txtBiografia.setBackground(TemaInsta.INPUT);
        txtBiografia.setForeground(TemaInsta.TEXTO);
        txtBiografia.setCaretColor(TemaInsta.TEXTO);

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
        TemaComponentes.corregirContraste(this);
        lblFoto.setForeground(TemaInsta.TEXTO_SECUNDARIO);

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

    public void setAccionPerfilActualizado(Runnable accionPerfilActualizado) {
        this.accionPerfilActualizado = accionPerfilActualizado;
    }
}