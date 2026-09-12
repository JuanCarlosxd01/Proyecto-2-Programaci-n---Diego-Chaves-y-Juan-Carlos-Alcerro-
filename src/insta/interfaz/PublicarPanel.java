
package insta.interfaz;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.swing.filechooser.FileNameExtensionFilter;
import red.Cliente;
import red.Respuesta;

public class PublicarPanel extends JPanel implements Tematizable {

    private JPanel panelEncabezado;
    private JPanel panelPrincipal;
    private JPanel tarjeta;
    private JPanel panelIzquierdo;
    private JPanel panelDerecho;

    private JLabel lblVistaImagen;
    private JLabel lblCaracteres;

    private JTextArea txtDescripcion;
    private JTextField txtCarpeta;

    private JButton btnSeleccionarImagen;
    private JButton btnPublicar;
    private Cliente cliente;
    private File imagenSeleccionada;
    private Runnable accionPublicacionCreada;

    public PublicarPanel(Cliente cliente) {
        this.cliente = cliente;
        setLayout(new BorderLayout());

        crearEncabezado();
        crearFormulario();

        aplicarTema();
    }

    private void crearEncabezado() {
        panelEncabezado = new JPanel(new BorderLayout());
        panelEncabezado.setBorder(new EmptyBorder(20, 30, 15, 30));

        JLabel titulo = new JLabel("Crear nueva publicación");
        titulo.setFont(new Font("Arial", Font.BOLD, 25));

        panelEncabezado.add(titulo, BorderLayout.WEST);

        add(panelEncabezado, BorderLayout.NORTH);
    }
    
    private void configurarEventos() {
        btnSeleccionarImagen.addActionListener(e -> seleccionarImagen());
        btnPublicar.addActionListener(e -> publicar());
    }

    private void crearFormulario() {
        panelPrincipal = new JPanel(new GridBagLayout());

        tarjeta = new JPanel(new BorderLayout(20, 20));
        tarjeta.setBorder(new EmptyBorder(25, 25, 25, 25));
        tarjeta.setPreferredSize(new Dimension(750, 480));

        panelIzquierdo = new JPanel(new BorderLayout(10, 10));

        lblVistaImagen = new JLabel("Vista previa de la imagen", SwingConstants.CENTER);
        lblVistaImagen.setOpaque(true);
        lblVistaImagen.setPreferredSize(new Dimension(350, 350));

        btnSeleccionarImagen = new JButton("Seleccionar imagen");

        panelIzquierdo.add(lblVistaImagen, BorderLayout.CENTER);
        panelIzquierdo.add(btnSeleccionarImagen, BorderLayout.SOUTH);

        panelDerecho = new JPanel();
        panelDerecho.setLayout(new BoxLayout(panelDerecho, BoxLayout.Y_AXIS));

        JLabel lblDescripcion = new JLabel("Descripción");
        lblDescripcion.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtDescripcion = new JTextArea();
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);

        JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);
        scrollDescripcion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        lblCaracteres = new JLabel("0 / 220");

        JLabel lblCarpeta = new JLabel("Carpeta personal");

        txtCarpeta = new JTextField();
        txtCarpeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JLabel ayuda = new JLabel("<html>Puedes utilizar <b>#hashtags</b> y <b>@username</b>.</html>");

        btnPublicar = new JButton("Publicar");
        btnPublicar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        panelDerecho.add(lblDescripcion);
        panelDerecho.add(Box.createVerticalStrut(8));
        panelDerecho.add(scrollDescripcion);
        panelDerecho.add(Box.createVerticalStrut(5));
        panelDerecho.add(lblCaracteres);
        panelDerecho.add(Box.createVerticalStrut(20));
        panelDerecho.add(lblCarpeta);
        panelDerecho.add(Box.createVerticalStrut(8));
        panelDerecho.add(txtCarpeta);
        panelDerecho.add(Box.createVerticalStrut(15));
        panelDerecho.add(ayuda);
        panelDerecho.add(Box.createVerticalGlue());
        panelDerecho.add(btnPublicar);

        tarjeta.add(panelIzquierdo, BorderLayout.WEST);
        tarjeta.add(panelDerecho, BorderLayout.CENTER);

        panelPrincipal.add(tarjeta);

        add(panelPrincipal, BorderLayout.CENTER);

        configurarContador();
    }

    private void configurarContador() {
        txtDescripcion.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizar();
            }
        });
    }

    private void actualizar() {
        int cantidad = txtDescripcion.getText().length();
        lblCaracteres.setText(cantidad + " / 220");

        if (cantidad > 220) {
            lblCaracteres.setForeground(Color.RED);
        } else {
            lblCaracteres.setForeground(TemaInsta.TEXTO_SECUNDARIO);
        }
    }

    @Override
    public void aplicarTema() {
        setBackground(TemaInsta.FONDO);
        panelEncabezado.setBackground(TemaInsta.FONDO);
        panelPrincipal.setBackground(TemaInsta.FONDO_SECUNDARIO);
        tarjeta.setBackground(TemaInsta.TARJETA);
        panelIzquierdo.setBackground(TemaInsta.TARJETA);
        panelDerecho.setBackground(TemaInsta.TARJETA);

        lblVistaImagen.setBackground(TemaInsta.INPUT);
        lblVistaImagen.setForeground(TemaInsta.TEXTO_SECUNDARIO);

        txtDescripcion.setBackground(TemaInsta.INPUT);
        txtDescripcion.setForeground(TemaInsta.TEXTO);
        txtDescripcion.setCaretColor(TemaInsta.TEXTO);

        txtCarpeta.setBackground(TemaInsta.INPUT);
        txtCarpeta.setForeground(TemaInsta.TEXTO);
        txtCarpeta.setCaretColor(TemaInsta.TEXTO);

        btnSeleccionarImagen.setBackground(TemaInsta.INPUT);
        btnSeleccionarImagen.setForeground(TemaInsta.TEXTO);

        btnPublicar.setBackground(TemaInsta.BOTON);
        btnPublicar.setForeground(TemaInsta.BOTON_TEXTO);

        cambiarTexto(this);

        revalidate();
        repaint();
    }

    private void cambiarTexto(Container contenedor) {
        for (Component componente : contenedor.getComponents()) {
            if (componente instanceof JLabel label && label != lblCaracteres) {
                label.setForeground(TemaInsta.TEXTO);
            }

            if (componente instanceof Container interno) {
                cambiarTexto(interno);
            }
        }

        actualizar();
    }
    
    private void seleccionarImagen() {
        JFileChooser selector = new JFileChooser();

        FileNameExtensionFilter filtro = new FileNameExtensionFilter(
                "Imágenes PNG, JPG y JPEG",
                "png",
                "jpg",
                "jpeg"
        );

        selector.setFileFilter(filtro);

        int resultado = selector.showOpenDialog(this);

        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        imagenSeleccionada = selector.getSelectedFile();

        ImageIcon iconoOriginal = new ImageIcon(imagenSeleccionada.getAbsolutePath());

        Image imagen = iconoOriginal.getImage().getScaledInstance(
                350,
                350,
                Image.SCALE_SMOOTH
        );

        lblVistaImagen.setText("");
        lblVistaImagen.setIcon(new ImageIcon(imagen));
    }
    
    private void publicar() {
        String descripcion = txtDescripcion.getText().trim();
        String carpeta = txtCarpeta.getText().trim();

        if (imagenSeleccionada == null && descripcion.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Debes escribir algo o seleccionar una imagen.",
                    "Publicación",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (imagenSeleccionada == null && descripcion.length() > 140) {
            JOptionPane.showMessageDialog(
                    this,
                    "Las publicaciones de texto permiten máximo 140 caracteres.",
                    "Publicación",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (imagenSeleccionada != null && descripcion.length() > 220) {
            JOptionPane.showMessageDialog(
                    this,
                    "La descripción de una imagen permite máximo 220 caracteres.",
                    "Publicación",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        btnPublicar.setEnabled(false);
        btnPublicar.setText("Publicando...");

        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {

            @Override
            protected Respuesta doInBackground() throws Exception {
                if (imagenSeleccionada == null) {
                    return cliente.publicarTexto(descripcion);
                }

                byte[] datosImagen = Files.readAllBytes(imagenSeleccionada.toPath());

                return cliente.publicarImagen(
                        datosImagen,
                        descripcion,
                        carpeta
                );
            }

            @Override
            protected void done() {
                btnPublicar.setEnabled(true);
                btnPublicar.setText("Publicar");

                try {
                    Respuesta respuesta = get();

                    if (respuesta.esExitosa()) {
                        JOptionPane.showMessageDialog(
                                PublicarPanel.this,
                                "Publicación creada correctamente.",
                                "INSTA+",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        limpiar();

                        if (accionPublicacionCreada != null) {
                            accionPublicacionCreada.run();
                        }

                    } else {
                        JOptionPane.showMessageDialog(
                                PublicarPanel.this,
                                respuesta.getMensaje(),
                                "No se pudo publicar",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            PublicarPanel.this,
                            "No se pudo crear la publicación.\n" + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        trabajador.execute();
    }
    
    private void limpiar() {
        txtDescripcion.setText("");
        txtCarpeta.setText("");

        imagenSeleccionada = null;

        lblVistaImagen.setIcon(null);
        lblVistaImagen.setText("Vista previa de la imagen");

        actualizar();
    }

    public JTextArea getTxtDescripcion() {
        return txtDescripcion;
    }
    
    public void setAccionPublicacionCreada(Runnable accionPublicacionCreada) {
        this.accionPublicacionCreada = accionPublicacionCreada;
    }

    public JButton getBtnPublicar() {
        return btnPublicar;
    }
}