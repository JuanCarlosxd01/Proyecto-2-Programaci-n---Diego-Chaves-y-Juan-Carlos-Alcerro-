package insta.interfaz;

import estructuras.ListaEnlazada;
import insta.modelo.Sticker;
import interfaz.DialogosWindows;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.nio.file.Files;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import red.Cliente;
import red.Respuesta;
import sistema.RutasSistema;

public class PublicarPanel extends JPanel implements Tematizable {

    private JPanel panelEncabezado;
    private JPanel panelPrincipal;
    private JPanel tarjeta;
    private JPanel panelIzquierdo;
    private JPanel panelDerecho;
    private JLabel lblVistaImagen;
    private JLabel lblCaracteres;
    private JTextArea txtDescripcion;
    private JComboBox<String> cmbCarpetas;
    private JButton btnNuevaCarpeta;
    private JButton btnSeleccionarImagen;
    private JButton btnSeleccionarSticker;
    private JButton btnPublicar;
    private final Cliente cliente;
    private File imagenSeleccionada;
    private Sticker stickerSeleccionado;
    private Runnable accionPublicacionCreada;
    private JPopupMenu popupMenciones;
    private Timer timerMenciones;
    private long solicitudMencion;

    public PublicarPanel(Cliente cliente) {
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
        JLabel titulo = new JLabel("Crear nueva publicación");
        titulo.setFont(new Font("Arial", Font.BOLD, 25));
        panelEncabezado.add(titulo, BorderLayout.WEST);
        add(panelEncabezado, BorderLayout.NORTH);
    }

    private void crearFormulario() {
        panelPrincipal = new JPanel(new GridBagLayout());
        tarjeta = new JPanel(new BorderLayout(20, 20));
        tarjeta.setBorder(new EmptyBorder(25, 25, 25, 25));
        tarjeta.setPreferredSize(new Dimension(800, 520));

        panelIzquierdo = new JPanel(new BorderLayout(10, 10));
        lblVistaImagen = new JLabel("Vista previa de la imagen o sticker", SwingConstants.CENTER);
        lblVistaImagen.setOpaque(true);
        lblVistaImagen.setPreferredSize(new Dimension(350, 350));

        JPanel botonesAdjunto = new JPanel(new GridLayout(1, 2, 8, 0));
        botonesAdjunto.setOpaque(false);
        btnSeleccionarImagen = new JButton("Seleccionar imagen");
        btnSeleccionarSticker = new JButton("Seleccionar sticker");
        botonesAdjunto.add(btnSeleccionarImagen);
        botonesAdjunto.add(btnSeleccionarSticker);
        panelIzquierdo.add(lblVistaImagen, BorderLayout.CENTER);
        panelIzquierdo.add(botonesAdjunto, BorderLayout.SOUTH);

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

        JLabel lblCarpeta = new JLabel("Carpeta personal para imágenes");
        JPanel panelCarpeta = new JPanel(new BorderLayout(8, 0));
        panelCarpeta.setOpaque(false);
        panelCarpeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cmbCarpetas = new JComboBox<>();
        cmbCarpetas.addItem("Sin carpeta");
        btnNuevaCarpeta = new JButton("+");
        btnNuevaCarpeta.setToolTipText("Crear carpeta personal");
        panelCarpeta.add(cmbCarpetas, BorderLayout.CENTER);
        panelCarpeta.add(btnNuevaCarpeta, BorderLayout.EAST);

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
        panelDerecho.add(panelCarpeta);
        panelDerecho.add(Box.createVerticalStrut(15));
        panelDerecho.add(ayuda);
        panelDerecho.add(Box.createVerticalGlue());
        panelDerecho.add(btnPublicar);

        tarjeta.add(panelIzquierdo, BorderLayout.WEST);
        tarjeta.add(panelDerecho, BorderLayout.CENTER);
        panelPrincipal.add(tarjeta);
        add(panelPrincipal, BorderLayout.CENTER);
        configurarContador();
        configurarAutocompletadoMenciones();
    }

    private void configurarEventos() {
        btnSeleccionarImagen.addActionListener(e -> seleccionarImagen());
        btnSeleccionarSticker.addActionListener(e -> seleccionarSticker());
        btnNuevaCarpeta.addActionListener(e -> crearCarpeta());
        btnPublicar.addActionListener(e -> publicar());
    }

    public void prepararPublicacion() {
        cargarCarpetas();
    }

    private void cargarCarpetas() {
        String seleccion = cmbCarpetas.getSelectedItem() == null ? "Sin carpeta" : cmbCarpetas.getSelectedItem().toString();
        SwingWorker<ListaEnlazada<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected ListaEnlazada<String> doInBackground() throws Exception {
                return PaginadorInsta.carpetas(cliente);
            }

            @Override
            protected void done() {
                try {
                    ListaEnlazada<String> carpetas = get();
                    cmbCarpetas.removeAllItems();
                    cmbCarpetas.addItem("Sin carpeta");
                    for (String carpeta : carpetas) cmbCarpetas.addItem(carpeta);
                    cmbCarpetas.setSelectedItem(seleccion);
                    if (cmbCarpetas.getSelectedIndex() < 0) cmbCarpetas.setSelectedIndex(0);
                } catch (Exception e) {
                    cmbCarpetas.removeAllItems();
                    cmbCarpetas.addItem("Sin carpeta");
                }
            }
        };
        worker.execute();
    }

    private void crearCarpeta() {
        String nombre = DialogosWindows.showInputDialog(this, "Nombre de la nueva carpeta personal:");
        if (nombre == null || nombre.trim().isEmpty()) return;
        String carpeta = nombre.trim();
        btnNuevaCarpeta.setEnabled(false);
        SwingWorker<Respuesta, Void> worker = new SwingWorker<>() {
            @Override
            protected Respuesta doInBackground() throws Exception {
                return cliente.crearCarpeta(carpeta);
            }

            @Override
            protected void done() {
                btnNuevaCarpeta.setEnabled(true);
                try {
                    Respuesta respuesta = get();
                    if (respuesta.esExitosa()) {
                        cargarCarpetas();
                        SwingUtilities.invokeLater(() -> cmbCarpetas.setSelectedItem(carpeta));
                    } else {
                        DialogosWindows.showMessageDialog(PublicarPanel.this, respuesta.getMensaje(), "Carpeta personal", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    DialogosWindows.showMessageDialog(PublicarPanel.this, "No se pudo crear la carpeta personal.", "Carpeta personal", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void seleccionarImagen() {
        File carpetaInicial = RutasSistema.getImagenesUsuarioActual();
        JFileChooser selector = carpetaInicial != null && carpetaInicial.exists() ? new JFileChooser(carpetaInicial) : new JFileChooser();
        selector.setFileFilter(new FileNameExtensionFilter("Imágenes PNG, JPG y JPEG", "png", "jpg", "jpeg"));
        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        imagenSeleccionada = selector.getSelectedFile();
        stickerSeleccionado = null;
        mostrarVistaPrevia(new ImageIcon(imagenSeleccionada.getAbsolutePath()));
    }

    private void seleccionarSticker() {
        SwingWorker<ListaEnlazada<Sticker>, Void> worker = new SwingWorker<>() {
            @Override
            protected ListaEnlazada<Sticker> doInBackground() throws Exception {
                return PaginadorInsta.stickers(cliente);
            }

            @Override
            protected void done() {
                try {
                    ListaEnlazada<Sticker> stickers = get();
                    if (stickers.isEmpty()) {
                        DialogosWindows.showMessageDialog(PublicarPanel.this, "No hay stickers disponibles.");
                        return;
                    }
                    DefaultComboBoxModel<Sticker> modelo = new DefaultComboBoxModel<>();
                    for (Sticker sticker : stickers) modelo.addElement(sticker);
                    JComboBox<Sticker> combo = new JComboBox<>(modelo);
                    int opcion = DialogosWindows.showConfirmDialog(PublicarPanel.this, combo, "Seleccionar sticker", JOptionPane.OK_CANCEL_OPTION);
                    if (opcion == JOptionPane.OK_OPTION && combo.getSelectedItem() instanceof Sticker sticker) {
                        stickerSeleccionado = sticker;
                        imagenSeleccionada = null;
                        mostrarVistaPreviaSticker(sticker);
                    }
                } catch (Exception e) {
                    DialogosWindows.showMessageDialog(PublicarPanel.this, "No se pudieron cargar los stickers.", "Stickers", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }


    private void mostrarVistaPreviaSticker(Sticker sticker) {
        lblVistaImagen.setIcon(null);
        lblVistaImagen.setText("Cargando sticker...");
        SwingWorker<Respuesta, Void> worker = new SwingWorker<>() {
            @Override
            protected Respuesta doInBackground() throws Exception {
                return cliente.descargarImagen(sticker.getRutaImagen());
            }

            @Override
            protected void done() {
                if (stickerSeleccionado == null || !stickerSeleccionado.getId().equals(sticker.getId())) return;
                try {
                    Respuesta respuesta = get();
                    if (respuesta.esExitosa() && respuesta.getArchivo() != null) {
                        mostrarVistaPrevia(new ImageIcon(respuesta.getArchivo()));
                    } else {
                        lblVistaImagen.setIcon(null);
                        lblVistaImagen.setText("Sticker: " + sticker.getNombre());
                    }
                } catch (Exception e) {
                    lblVistaImagen.setIcon(null);
                    lblVistaImagen.setText("Sticker: " + sticker.getNombre());
                }
            }
        };
        worker.execute();
    }

    private void mostrarVistaPrevia(ImageIcon original) {
        int w = Math.max(1, original.getIconWidth());
        int h = Math.max(1, original.getIconHeight());
        double escala = Math.min(350.0 / w, 350.0 / h);
        int nw = Math.max(1, (int) Math.round(w * escala));
        int nh = Math.max(1, (int) Math.round(h * escala));
        Image imagen = original.getImage().getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
        lblVistaImagen.setText("");
        lblVistaImagen.setIcon(new ImageIcon(imagen));
    }

    private void publicar() {
        String descripcion = txtDescripcion.getText().trim();
        String carpeta = cmbCarpetas.getSelectedItem() == null || cmbCarpetas.getSelectedIndex() == 0 ? "" : cmbCarpetas.getSelectedItem().toString();
        if (imagenSeleccionada == null && stickerSeleccionado == null && descripcion.isEmpty()) {
            DialogosWindows.showMessageDialog(this, "Debes escribir algo o seleccionar una imagen/sticker.", "Publicación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (imagenSeleccionada == null && stickerSeleccionado == null && descripcion.length() > 140) {
            DialogosWindows.showMessageDialog(this, "Las publicaciones de texto permiten máximo 140 caracteres.", "Publicación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if ((imagenSeleccionada != null || stickerSeleccionado != null) && descripcion.length() > 220) {
            DialogosWindows.showMessageDialog(this, "La descripción permite máximo 220 caracteres.", "Publicación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File imagen = imagenSeleccionada;
        Sticker sticker = stickerSeleccionado;
        btnPublicar.setEnabled(false);
        btnPublicar.setText("Publicando...");
        SwingWorker<Respuesta, Void> worker = new SwingWorker<>() {
            @Override
            protected Respuesta doInBackground() throws Exception {
                if (sticker != null) return cliente.publicarSticker(descripcion, sticker.getId());
                if (imagen == null) return cliente.publicarTexto(descripcion);
                return cliente.publicarImagen(Files.readAllBytes(imagen.toPath()), descripcion, carpeta);
            }

            @Override
            protected void done() {
                btnPublicar.setEnabled(true);
                btnPublicar.setText("Publicar");
                try {
                    Respuesta respuesta = get();
                    if (respuesta.esExitosa()) {
                        DialogosWindows.showMessageDialog(PublicarPanel.this, "Publicación creada correctamente.", "INSTA+", JOptionPane.INFORMATION_MESSAGE);
                        limpiar();
                        if (accionPublicacionCreada != null) accionPublicacionCreada.run();
                    } else {
                        DialogosWindows.showMessageDialog(PublicarPanel.this, respuesta.getMensaje(), "Publicación", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    DialogosWindows.showMessageDialog(PublicarPanel.this, "No se pudo crear la publicación.", "Publicación", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }


    private void configurarAutocompletadoMenciones() {
        popupMenciones = new JPopupMenu();
        popupMenciones.setFocusable(false);
        timerMenciones = new Timer(280, e -> buscarSugerenciasMencion());
        timerMenciones.setRepeats(false);

        txtDescripcion.addCaretListener(e -> programarSugerenciasMencion());
        txtDescripcion.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { programarSugerenciasMencion(); }
            @Override public void removeUpdate(DocumentEvent e) { programarSugerenciasMencion(); }
            @Override public void changedUpdate(DocumentEvent e) { programarSugerenciasMencion(); }
        });
    }

    private void programarSugerenciasMencion() {
        if (timerMenciones == null) return;
        String prefijo = obtenerPrefijoMencion();
        if (prefijo == null) {
            popupMenciones.setVisible(false);
            timerMenciones.stop();
            return;
        }
        timerMenciones.restart();
    }

    private String obtenerPrefijoMencion() {
        int caret = txtDescripcion.getCaretPosition();
        String texto = txtDescripcion.getText();
        if (caret < 0 || caret > texto.length()) return null;
        int inicio = caret - 1;
        while (inicio >= 0) {
            char c = texto.charAt(inicio);
            if (c == '@') break;
            if (!(Character.isLetterOrDigit(c) || c == '_')) return null;
            inicio--;
        }
        if (inicio < 0 || texto.charAt(inicio) != '@') return null;
        if (inicio > 0) {
            char anterior = texto.charAt(inicio - 1);
            if (Character.isLetterOrDigit(anterior) || anterior == '_') return null;
        }
        return texto.substring(inicio + 1, caret).toLowerCase();
    }

    private void buscarSugerenciasMencion() {
        String prefijo = obtenerPrefijoMencion();
        if (prefijo == null) return;
        long numero = ++solicitudMencion;

        SwingWorker<ListaEnlazada<Respuesta.DatosUsuario>, Void> worker = new SwingWorker<>() {
            @Override
            protected ListaEnlazada<Respuesta.DatosUsuario> doInBackground() throws Exception {
                Respuesta respuesta = cliente.buscarPersonas(prefijo, 0);
                if (!respuesta.esExitosa()) return new ListaEnlazada<>();
                return respuesta.getUsuarios();
            }

            @Override
            protected void done() {
                if (numero != solicitudMencion) return;
                try {
                    String actual = obtenerPrefijoMencion();
                    if (actual == null || !actual.equals(prefijo)) return;
                    ListaEnlazada<Respuesta.DatosUsuario> usuarios = get();
                    mostrarSugerenciasMencion(usuarios);
                } catch (Exception e) {
                    popupMenciones.setVisible(false);
                }
            }
        };
        worker.execute();
    }

    private void mostrarSugerenciasMencion(ListaEnlazada<Respuesta.DatosUsuario> usuarios) {
        popupMenciones.removeAll();
        int agregados = 0;
        for (Respuesta.DatosUsuario usuario : usuarios) {
            Respuesta.DatosUsuario actual = cliente.getUsuarioActual();
            if (actual != null && actual.getUsername().equalsIgnoreCase(usuario.getUsername())) continue;
            JMenuItem item = new JMenuItem("@" + usuario.getUsername() + "  ·  " + usuario.getNombreCompleto());
            item.addActionListener(e -> insertarMencion(usuario.getUsername()));
            popupMenciones.add(item);
            if (++agregados >= 6) break;
        }
        if (agregados == 0) {
            popupMenciones.setVisible(false);
            return;
        }

        try {
            Rectangle2D r = txtDescripcion.modelToView2D(txtDescripcion.getCaretPosition());
            popupMenciones.show(txtDescripcion, (int) r.getX(), (int) (r.getY() + r.getHeight()));
            txtDescripcion.requestFocusInWindow();
        } catch (Exception e) {
            popupMenciones.show(txtDescripcion, 10, txtDescripcion.getHeight());
        }
    }

    private void insertarMencion(String username) {
        int caret = txtDescripcion.getCaretPosition();
        String texto = txtDescripcion.getText();
        int inicio = caret - 1;
        while (inicio >= 0 && texto.charAt(inicio) != '@' && (Character.isLetterOrDigit(texto.charAt(inicio)) || texto.charAt(inicio) == '_')) inicio--;
        if (inicio < 0 || texto.charAt(inicio) != '@') return;
        String nuevo = texto.substring(0, inicio) + "@" + username + " " + texto.substring(caret);
        txtDescripcion.setText(nuevo);
        txtDescripcion.setCaretPosition(Math.min(nuevo.length(), inicio + username.length() + 2));
        popupMenciones.setVisible(false);
        txtDescripcion.requestFocusInWindow();
    }

    private void configurarContador() {
        txtDescripcion.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { actualizar(); }
            @Override public void removeUpdate(DocumentEvent e) { actualizar(); }
            @Override public void changedUpdate(DocumentEvent e) { actualizar(); }
        });
    }

    private void actualizar() {
        int cantidad = txtDescripcion.getText().length();
        lblCaracteres.setText(cantidad + " / 220");
        lblCaracteres.setForeground(cantidad > 220 ? Color.RED : TemaInsta.TEXTO_SECUNDARIO);
    }

    private void limpiar() {
        txtDescripcion.setText("");
        imagenSeleccionada = null;
        stickerSeleccionado = null;
        lblVistaImagen.setIcon(null);
        lblVistaImagen.setText("Vista previa de la imagen o sticker");
        if (cmbCarpetas.getItemCount() > 0) cmbCarpetas.setSelectedIndex(0);
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
        cmbCarpetas.setBackground(TemaInsta.INPUT);
        cmbCarpetas.setForeground(TemaInsta.TEXTO);
        btnSeleccionarImagen.setBackground(TemaInsta.INPUT);
        btnSeleccionarSticker.setBackground(TemaInsta.INPUT);
        btnNuevaCarpeta.setBackground(TemaInsta.INPUT);
        btnSeleccionarImagen.setForeground(TemaInsta.TEXTO);
        btnSeleccionarSticker.setForeground(TemaInsta.TEXTO);
        btnNuevaCarpeta.setForeground(TemaInsta.TEXTO);
        btnPublicar.setBackground(TemaInsta.BOTON);
        btnPublicar.setForeground(TemaInsta.BOTON_TEXTO);
        cambiarTexto(this);
        revalidate();
        repaint();
    }

    private void cambiarTexto(Container contenedor) {
        for (Component componente : contenedor.getComponents()) {
            if (componente instanceof JLabel label && label != lblCaracteres) label.setForeground(TemaInsta.TEXTO);
            if (componente instanceof Container interno) cambiarTexto(interno);
        }
        actualizar();
    }

    public JTextArea getTxtDescripcion() { return txtDescripcion; }
    public void setAccionPublicacionCreada(Runnable accionPublicacionCreada) { this.accionPublicacionCreada = accionPublicacionCreada; }
    public JButton getBtnPublicar() { return btnPublicar; }
}
