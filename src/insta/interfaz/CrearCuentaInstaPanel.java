
package insta.interfaz;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class CrearCuentaInstaPanel extends JPanel implements Tematizable {

    private JPanel panelPrincipal;
    private JPanel panelFormulario;
    private JPanel panelPassword;
    private JPanel panelConfirmar;

    private JTextField txtNombre;
    private JTextField txtUsername;

    private JComboBox<String> cmbGenero;
    private JSpinner spnEdad;

    private JPasswordField txtPassword;
    private JPasswordField txtConfirmarPassword;

    private JButton btnVerPassword;
    private JButton btnVerConfirmar;
    private JButton btnSeleccionarFoto;
    private JButton btnCrearCuenta;
    private JButton btnVolver;

    private JLabel lblFoto;
    private JLabel lblRequisitos;

    private byte[] fotoSeleccionada;

    private char caracterPassword;
    private char caracterConfirmar;

    private final Dimension TAMANO_CAMPO = new Dimension(300, 38);

    public CrearCuentaInstaPanel() {
        setLayout(new BorderLayout());

        crearInterfaz();
        aplicarTema();
    }

    private void crearInterfaz() {
        panelPrincipal = new JPanel(new GridBagLayout());

        panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(new EmptyBorder(25, 45, 30, 45));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 6, 0);

        JLabel lblLogo = new JLabel("INSTA+", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        panelFormulario.add(lblLogo, gbc);

        gbc.gridy++;

        JLabel lblTitulo = new JLabel("Crear una cuenta", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panelFormulario.add(lblTitulo, gbc);

        gbc.gridy++;

        JLabel lblDescripcion = new JLabel("Regístrate para comenzar a compartir", SwingConstants.CENTER);
        panelFormulario.add(lblDescripcion, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(15, 0, 5, 0);

        lblFoto = new JLabel("Sin foto", SwingConstants.CENTER);
        lblFoto.setPreferredSize(new Dimension(90, 90));
        lblFoto.setMinimumSize(new Dimension(90, 90));
        lblFoto.setOpaque(true);

        JPanel contenedorFoto = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        contenedorFoto.setOpaque(false);
        contenedorFoto.add(lblFoto);

        panelFormulario.add(contenedorFoto, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 15, 0);

        btnSeleccionarFoto = new JButton("Seleccionar foto");
        btnSeleccionarFoto.setFocusPainted(false);
        btnSeleccionarFoto.addActionListener(e -> seleccionarFoto());

        JPanel contenedorBotonFoto = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        contenedorBotonFoto.setOpaque(false);
        contenedorBotonFoto.add(btnSeleccionarFoto);

        panelFormulario.add(contenedorBotonFoto, gbc);

        txtNombre = crearCampoTexto();
        txtUsername = crearCampoTexto();

        cmbGenero = new JComboBox<>(new String[]{"M", "F"});
        configurarTamano(cmbGenero);

        spnEdad = new JSpinner(new SpinnerNumberModel(18, 1, 120, 1));
        configurarTamano(spnEdad);

        gbc.gridy++;
        agregarCampo("Nombre completo", txtNombre, gbc);

        gbc.gridy += 2;
        agregarCampo("Username", txtUsername, gbc);

        gbc.gridy += 2;
        agregarCampo("Género", cmbGenero, gbc);

        gbc.gridy += 2;
        agregarCampo("Edad", spnEdad, gbc);

        txtPassword = new JPasswordField();
        txtPassword.setBorder(new EmptyBorder(8, 10, 8, 10));

        caracterPassword = txtPassword.getEchoChar();

        btnVerPassword = new JButton("◉");
        btnVerPassword.setPreferredSize(new Dimension(45, 38));
        btnVerPassword.setFocusPainted(false);
        btnVerPassword.setToolTipText("Mostrar u ocultar contraseña");
        btnVerPassword.addActionListener(e -> mostrarOcultarPassword());

        panelPassword = new JPanel(new BorderLayout());
        panelPassword.setPreferredSize(TAMANO_CAMPO);
        panelPassword.setMinimumSize(TAMANO_CAMPO);
        panelPassword.setMaximumSize(TAMANO_CAMPO);
        panelPassword.add(txtPassword, BorderLayout.CENTER);
        panelPassword.add(btnVerPassword, BorderLayout.EAST);

        gbc.gridy += 2;
        agregarCampo("Contraseña", panelPassword, gbc);

        gbc.gridy += 2;
        gbc.insets = new Insets(2, 0, 10, 0);

        lblRequisitos = new JLabel("<html>Debe contener mínimo 8 caracteres, una mayúscula,<br>un número y un símbolo.</html>");
        lblRequisitos.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        panelFormulario.add(lblRequisitos, gbc);

        txtConfirmarPassword = new JPasswordField();
        txtConfirmarPassword.setBorder(new EmptyBorder(8, 10, 8, 10));

        caracterConfirmar = txtConfirmarPassword.getEchoChar();

        btnVerConfirmar = new JButton("◉");
        btnVerConfirmar.setPreferredSize(new Dimension(45, 38));
        btnVerConfirmar.setFocusPainted(false);
        btnVerConfirmar.setToolTipText("Mostrar u ocultar contraseña");
        btnVerConfirmar.addActionListener(e -> mostrarOcultarConfirmar());

        panelConfirmar = new JPanel(new BorderLayout());
        panelConfirmar.setPreferredSize(TAMANO_CAMPO);
        panelConfirmar.setMinimumSize(TAMANO_CAMPO);
        panelConfirmar.setMaximumSize(TAMANO_CAMPO);
        panelConfirmar.add(txtConfirmarPassword, BorderLayout.CENTER);
        panelConfirmar.add(btnVerConfirmar, BorderLayout.EAST);

        gbc.gridy++;
        agregarCampo("Confirmar contraseña", panelConfirmar, gbc);

        gbc.gridy += 2;
        gbc.insets = new Insets(20, 0, 5, 0);

        btnCrearCuenta = new JButton("Crear cuenta");
        btnCrearCuenta.setPreferredSize(new Dimension(300, 42));
        btnCrearCuenta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCrearCuenta.setFocusPainted(false);

        panelFormulario.add(btnCrearCuenta, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(5, 0, 0, 0);

        btnVolver = new JButton("Ya tengo una cuenta");
        btnVolver.setPreferredSize(new Dimension(300, 42));
        btnVolver.setFocusPainted(false);

        panelFormulario.add(btnVolver, gbc);

        panelPrincipal.add(panelFormulario);

        JScrollPane scroll = new JScrollPane(panelPrincipal);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);
    }

    private JTextField crearCampoTexto() {
        JTextField campo = new JTextField();
        configurarTamano(campo);
        campo.setBorder(new EmptyBorder(8, 10, 8, 10));
        return campo;
    }

    private void configurarTamano(JComponent componente) {
        componente.setPreferredSize(TAMANO_CAMPO);
        componente.setMinimumSize(TAMANO_CAMPO);
        componente.setMaximumSize(TAMANO_CAMPO);
    }

    private void agregarCampo(String texto, JComponent componente, GridBagConstraints gbc) {
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));

        panelFormulario.add(label, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);

        panelFormulario.add(componente, gbc);
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

    private void mostrarOcultarConfirmar() {
        if (txtConfirmarPassword.getEchoChar() == 0) {
            txtConfirmarPassword.setEchoChar(caracterConfirmar);
            btnVerConfirmar.setText("◉");
        } else {
            txtConfirmarPassword.setEchoChar((char) 0);
            btnVerConfirmar.setText("⊘");
        }
    }

    private void seleccionarFoto() {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Seleccionar foto de perfil");

        FileNameExtensionFilter filtro = new FileNameExtensionFilter("Imágenes PNG o JPG", "png", "jpg", "jpeg");
        selector.setFileFilter(filtro);

        int resultado = selector.showOpenDialog(this);

        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = selector.getSelectedFile();

        try {
            BufferedImage imagen = ImageIO.read(archivo);

            if (imagen == null) {
                JOptionPane.showMessageDialog(this, "El archivo seleccionado no es una imagen válida.", "Foto de perfil", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            ImageIO.write(imagen, "png", salida);

            fotoSeleccionada = salida.toByteArray();

            Image escalada = imagen.getScaledInstance(90, 90, Image.SCALE_SMOOTH);

            lblFoto.setText("");
            lblFoto.setIcon(new ImageIcon(escalada));

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la imagen.", "Foto de perfil", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean datosValidos() {
        String nombre = txtNombre.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmar = new String(txtConfirmarPassword.getPassword());

        if (nombre.isEmpty()) {
            mostrarError("Ingrese su nombre completo.");
            return false;
        }

        if (username.isEmpty()) {
            mostrarError("Ingrese un username.");
            return false;
        }

        if (!username.matches("[a-zA-Z][a-zA-Z0-9_]{2,23}")) {
            mostrarError("El username debe comenzar con una letra y solo puede contener letras, números y guion bajo.");
            return false;
        }

        if (password.isEmpty()) {
            mostrarError("Ingrese una contraseña.");
            return false;
        }

        if (!passwordValida(password)) {
            mostrarError("La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un símbolo.");
            return false;
        }

        if (!password.equals(confirmar)) {
            mostrarError("Las contraseñas no coinciden.");
            return false;
        }

        return true;
    }

    private boolean passwordValida(String password) {
        if (password.length() < 8 || password.length() > 128) {
            return false;
        }

        boolean tieneMayuscula = false;
        boolean tieneNumero = false;
        boolean tieneSimbolo = false;

        for (char caracter : password.toCharArray()) {
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

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Crear cuenta", JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public void aplicarTema() {
        setBackground(TemaInsta.FONDO_SECUNDARIO);
        panelPrincipal.setBackground(TemaInsta.FONDO_SECUNDARIO);
        panelFormulario.setBackground(TemaInsta.TARJETA);

        cambiarTemaComponentes(this);

        txtNombre.setBackground(TemaInsta.INPUT);
        txtNombre.setForeground(TemaInsta.TEXTO);
        txtNombre.setCaretColor(TemaInsta.TEXTO);

        txtUsername.setBackground(TemaInsta.INPUT);
        txtUsername.setForeground(TemaInsta.TEXTO);
        txtUsername.setCaretColor(TemaInsta.TEXTO);

        txtPassword.setBackground(TemaInsta.INPUT);
        txtPassword.setForeground(TemaInsta.TEXTO);
        txtPassword.setCaretColor(TemaInsta.TEXTO);

        txtConfirmarPassword.setBackground(TemaInsta.INPUT);
        txtConfirmarPassword.setForeground(TemaInsta.TEXTO);
        txtConfirmarPassword.setCaretColor(TemaInsta.TEXTO);

        panelPassword.setBackground(TemaInsta.INPUT);
        panelConfirmar.setBackground(TemaInsta.INPUT);

        cmbGenero.setBackground(TemaInsta.INPUT);
        cmbGenero.setForeground(TemaInsta.TEXTO);

        lblFoto.setBackground(TemaInsta.INPUT);
        lblFoto.setForeground(TemaInsta.TEXTO_SECUNDARIO);

        lblRequisitos.setForeground(TemaInsta.TEXTO_SECUNDARIO);

        btnSeleccionarFoto.setBackground(TemaInsta.INPUT);
        btnSeleccionarFoto.setForeground(TemaInsta.TEXTO);

        btnVerPassword.setBackground(TemaInsta.INPUT);
        btnVerPassword.setForeground(TemaInsta.TEXTO);

        btnVerConfirmar.setBackground(TemaInsta.INPUT);
        btnVerConfirmar.setForeground(TemaInsta.TEXTO);

        btnCrearCuenta.setBackground(TemaInsta.BOTON);
        btnCrearCuenta.setForeground(TemaInsta.BOTON_TEXTO);

        btnVolver.setBackground(TemaInsta.INPUT);
        btnVolver.setForeground(TemaInsta.TEXTO);

        revalidate();
        repaint();
    }

    private void cambiarTemaComponentes(Container contenedor) {
        for (Component componente : contenedor.getComponents()) {
            if (componente instanceof JLabel label) {
                label.setForeground(TemaInsta.TEXTO);
            }

            if (componente instanceof JScrollPane scroll) {
                scroll.getViewport().setBackground(TemaInsta.FONDO_SECUNDARIO);
            }

            if (componente instanceof Container interno) {
                cambiarTemaComponentes(interno);
            }
        }
    }

    public JTextField getTxtNombre() {
        return txtNombre;
    }

    public JTextField getTxtUsername() {
        return txtUsername;
    }

    public JComboBox<String> getCmbGenero() {
        return cmbGenero;
    }

    public JSpinner getSpnEdad() {
        return spnEdad;
    }

    public JPasswordField getTxtPassword() {
        return txtPassword;
    }

    public JPasswordField getTxtConfirmarPassword() {
        return txtConfirmarPassword;
    }

    public JButton getBtnCrearCuenta() {
        return btnCrearCuenta;
    }

    public JButton getBtnVolver() {
        return btnVolver;
    }

    public byte[] getFotoSeleccionada() {
        return fotoSeleccionada;
    }

    public char getGeneroSeleccionado() {
        return cmbGenero.getSelectedItem().toString().charAt(0);
    }

    public void limpiar() {
        txtNombre.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        txtConfirmarPassword.setText("");

        cmbGenero.setSelectedIndex(0);
        spnEdad.setValue(18);

        fotoSeleccionada = null;

        lblFoto.setIcon(null);
        lblFoto.setText("Sin foto");

        txtPassword.setEchoChar(caracterPassword);
        txtConfirmarPassword.setEchoChar(caracterConfirmar);

        btnVerPassword.setText("◉");
        btnVerConfirmar.setText("◉");
    }
}