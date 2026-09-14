package insta.interfaz;

import interfaz.DialogosWindows;

import insta.modelo.Comentario;
import insta.modelo.Publicacion;
import insta.modelo.Sticker;
import estructuras.ListaEnlazada;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import javax.swing.border.EmptyBorder;
import red.Cliente;
import red.Respuesta;

public class TarjetaPublicacionPanel extends JPanel implements Tematizable {

    private final Cliente cliente;
    private final Publicacion publicacion;
    private final Runnable accionActualizar;

    private JPanel encabezado;
    private JPanel contenido;
    private JPanel acciones;
    private JButton btnLike;
    private JButton btnComentarios;

    public TarjetaPublicacionPanel(Cliente cliente, Publicacion publicacion, Runnable accionActualizar) {
        this.cliente = cliente;
        this.publicacion = publicacion;
        this.accionActualizar = accionActualizar;

        setLayout(new BorderLayout());
        setMaximumSize(new Dimension(620, 700));
        setAlignmentX(Component.CENTER_ALIGNMENT);
        setBorder(BorderFactory.createLineBorder(TemaInsta.BORDE));

        crearEncabezado();
        crearContenido();
        crearAcciones();
        aplicarTema();
    }

    private void crearEncabezado() {
        encabezado = new JPanel(new BorderLayout());
        encabezado.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel lblUsuario = new JLabel("@" + publicacion.getAutor() + " escribió:");
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 14));

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        JLabel lblFecha = new JLabel("— " + publicacion.getFechaPublicacion().format(formato));
        lblFecha.setFont(new Font("Arial", Font.PLAIN, 11));

        encabezado.add(lblUsuario, BorderLayout.WEST);
        encabezado.add(lblFecha, BorderLayout.EAST);
        add(encabezado, BorderLayout.NORTH);
    }

    private void crearContenido() {
        contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(new EmptyBorder(15, 15, 15, 15));

        if (publicacion.esTexto()) {
            JLabel texto = new JLabel("<html><div style='width:540px;'>" + escaparHtml(publicacion.getContenido()) + "</div></html>");
            texto.setFont(new Font("Arial", Font.PLAIN, 16));
            texto.setAlignmentX(Component.LEFT_ALIGNMENT);
            contenido.add(texto);
        } else {
            boolean sticker = publicacion.esSticker();
            int ancho = sticker ? 250 : 550;
            int alto = sticker ? 250 : 350;

            JLabel imagen = new JLabel(sticker ? "Cargando sticker..." : "Cargando imagen...", SwingConstants.CENTER);
            imagen.setPreferredSize(new Dimension(ancho, alto));
            imagen.setMaximumSize(new Dimension(ancho, alto));
            imagen.setOpaque(true);
            imagen.setAlignmentX(Component.CENTER_ALIGNMENT);
            contenido.add(imagen);
            cargarImagen(imagen, ancho, alto);

            if (!publicacion.getContenido().isBlank()) {
                contenido.add(Box.createVerticalStrut(10));
                JLabel descripcion = new JLabel("<html><div style='width:540px;'>" + escaparHtml(publicacion.getContenido()) + "</div></html>");
                descripcion.setAlignmentX(Component.LEFT_ALIGNMENT);
                contenido.add(descripcion);
            }
        }

        add(contenido, BorderLayout.CENTER);
    }

    private void crearAcciones() {
        acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        btnLike = new JButton(textoLike());
        btnComentarios = new JButton("💬 " + publicacion.getCantidadComentarios());

        btnLike.addActionListener(e -> alternarLike());
        btnComentarios.addActionListener(e -> mostrarComentarios());

        acciones.add(btnLike);
        acciones.add(btnComentarios);

        Respuesta.DatosUsuario actual = cliente.getUsuarioActual();
        if (actual != null && actual.getUsername().equals(publicacion.getAutor())) {
            JButton btnEditar = new JButton("Editar");
            JButton btnEliminar = new JButton("Eliminar publicación");
            btnEditar.addActionListener(e -> editarPublicacion());
            btnEliminar.addActionListener(e -> eliminarPublicacion());
            acciones.add(btnEditar);
            acciones.add(btnEliminar);
        }

        add(acciones, BorderLayout.SOUTH);
    }

    private String textoLike() {
        Respuesta.DatosUsuario actual = cliente.getUsuarioActual();
        boolean marcado = actual != null && publicacion.tieneLike(actual.getUsername());
        return (marcado ? "♥ " : "♡ ") + publicacion.getCantidadLikes();
    }

    private void alternarLike() {
        ejecutar(() -> cliente.alternarLike(publicacion.getAutor(), publicacion.getId()));
    }

    private void editarPublicacion() {
        String nuevo = DialogosWindows.showInputDialog(this, "Editar publicación:", publicacion.getContenido());
        if (nuevo == null) {
            return;
        }
        ejecutar(() -> cliente.editarPublicacion(publicacion.getId(), nuevo));
    }

    private void eliminarPublicacion() {
        int opcion = DialogosWindows.showConfirmDialog(this, "¿Eliminar esta publicación?", "INSTA+", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }
        ejecutar(() -> cliente.eliminarPublicacion(publicacion.getId()));
    }

    private void mostrarComentarios() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        DefaultListModel<Comentario> modelo = new DefaultListModel<>();
        List<Comentario> comentarios = publicacion.getComentarios();
        for (Comentario comentario : comentarios) modelo.addElement(comentario);

        Map<String, ImageIcon> iconosSticker = new HashMap<>();
        JList<Comentario> lista = new JList<>(modelo);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setFixedCellHeight(82);
        lista.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JPanel fila = new JPanel(new BorderLayout(8, 0));
            fila.setOpaque(true);
            fila.setBorder(new EmptyBorder(6, 8, 6, 8));
            fila.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            JLabel autor = new JLabel("@" + value.getAutor());
            autor.setFont(new Font("Arial", Font.BOLD, 12));
            fila.add(autor, BorderLayout.WEST);
            if (value.esSticker()) {
                JLabel sticker = new JLabel("Sticker", SwingConstants.LEFT);
                ImageIcon icono = iconosSticker.get(value.getRutaSticker());
                if (icono != null) {
                    sticker.setText("");
                    sticker.setIcon(icono);
                } else {
                    cargarStickerComentario(value.getRutaSticker(), iconosSticker, lista);
                }
                fila.add(sticker, BorderLayout.CENTER);
            } else {
                JLabel texto = new JLabel("<html><div style='width:300px;'>" + escaparHtml(value.getTexto()) + "</div></html>");
                fila.add(texto, BorderLayout.CENTER);
            }
            return fila;
        });

        panel.add(new JScrollPane(lista), BorderLayout.CENTER);

        JTextArea txtComentario = new JTextArea(3, 30);
        txtComentario.setLineWrap(true);
        txtComentario.setWrapStyleWord(true);
        new AutocompletarMenciones(cliente, txtComentario);

        JPanel inferior = new JPanel(new BorderLayout(6, 6));
        inferior.add(new JScrollPane(txtComentario), BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnStickerComentario = new JButton("☺ Sticker");
        JButton btnAgregar = new JButton("Comentar");
        JButton btnBorrar = new JButton("Eliminar mío");
        botones.add(btnStickerComentario);
        botones.add(btnBorrar);
        botones.add(btnAgregar);
        inferior.add(botones, BorderLayout.SOUTH);
        panel.add(inferior, BorderLayout.SOUTH);

        lista.setBackground(TemaInsta.FONDO_SECUNDARIO);
        lista.setForeground(TemaInsta.TEXTO);
        lista.setSelectionBackground(TemaInsta.INPUT);
        lista.setSelectionForeground(TemaInsta.TEXTO);
        txtComentario.setBackground(TemaInsta.INPUT);
        txtComentario.setForeground(TemaInsta.TEXTO);
        txtComentario.setCaretColor(TemaInsta.TEXTO);
        panel.setBackground(TemaInsta.FONDO);
        inferior.setBackground(TemaInsta.FONDO);
        botones.setBackground(TemaInsta.FONDO);
        btnStickerComentario.setBackground(TemaInsta.INPUT);
        btnStickerComentario.setForeground(TemaInsta.TEXTO);
        btnBorrar.setBackground(TemaInsta.INPUT);
        btnBorrar.setForeground(TemaInsta.TEXTO);
        btnAgregar.setBackground(TemaInsta.BOTON);
        btnAgregar.setForeground(TemaInsta.BOTON_TEXTO);
        TemaComponentes.corregirContraste(panel);

        JDialogSimple dialogo = new JDialogSimple(panel, "Comentarios", new Dimension(540, 460));

        btnAgregar.addActionListener(e -> {
            String texto = txtComentario.getText().trim();
            if (texto.isEmpty()) return;
            btnAgregar.setEnabled(false);
            ejecutarComentarioEnDialogo(() -> cliente.comentarPublicacion(publicacion.getAutor(), publicacion.getId(), texto), modelo, lista, iconosSticker, () -> {
                txtComentario.setText("");
                btnAgregar.setEnabled(true);
            });
        });

        btnStickerComentario.addActionListener(e -> seleccionarStickerComentario(dialogo, stickerId -> {
            btnStickerComentario.setEnabled(false);
            ejecutarComentarioEnDialogo(() -> cliente.comentarSticker(publicacion.getAutor(), publicacion.getId(), stickerId), modelo, lista, iconosSticker, () -> btnStickerComentario.setEnabled(true));
        }));

        btnBorrar.addActionListener(e -> {
            Comentario seleccionado = lista.getSelectedValue();
            Respuesta.DatosUsuario actual = cliente.getUsuarioActual();
            if (seleccionado == null || actual == null || !seleccionado.getAutor().equals(actual.getUsername())) {
                DialogosWindows.showMessageDialog(dialogo, "Selecciona uno de tus comentarios.");
                return;
            }
            btnBorrar.setEnabled(false);
            ejecutarComentarioEnDialogo(() -> cliente.eliminarComentario(publicacion.getAutor(), publicacion.getId(), seleccionado.getId()), modelo, lista, iconosSticker, () -> btnBorrar.setEnabled(true));
        });

        dialogo.mostrar(this);
    }

    private void ejecutarComentarioEnDialogo(AccionRemota accion, DefaultListModel<Comentario> modelo, JList<Comentario> lista, Map<String, ImageIcon> cache, Runnable alFinalizar) {
        SwingWorker<Respuesta, Void> worker = new SwingWorker<>() {
            @Override
            protected Respuesta doInBackground() throws Exception { return accion.ejecutar(); }

            @Override
            protected void done() {
                try {
                    Respuesta respuesta = get();
                    if (!respuesta.esExitosa()) {
                        DialogosWindows.showMessageDialog(TarjetaPublicacionPanel.this, respuesta.getMensaje(), "INSTA+", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    ListaEnlazada<Publicacion> actualizadas = respuesta.getPublicaciones();
                    if (!actualizadas.isEmpty()) {
                        Publicacion actualizada = actualizadas.obtener(0);
                        modelo.clear();
                        cache.clear();
                        for (Comentario comentario : actualizada.getComentarios()) modelo.addElement(comentario);
                        btnComentarios.setText("💬 " + actualizada.getCantidadComentarios());
                        if (!modelo.isEmpty()) lista.ensureIndexIsVisible(modelo.size() - 1);
                    }
                } catch (Exception e) {
                    DialogosWindows.showMessageDialog(TarjetaPublicacionPanel.this, mensaje(e), "INSTA+", JOptionPane.ERROR_MESSAGE);
                } finally {
                    if (alFinalizar != null) alFinalizar.run();
                }
            }
        };
        worker.execute();
    }

    private void cargarStickerComentario(String ruta, Map<String, ImageIcon> cache, JList<Comentario> lista) {
        if (ruta == null || ruta.isBlank() || cache.containsKey(ruta)) return;
        cache.put(ruta, null);
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                Respuesta respuesta = cliente.descargarImagen(ruta);
                if (!respuesta.esExitosa() || respuesta.getArchivo() == null) return null;
                return ImagenUI.ajustar(respuesta.getArchivo(), 64, 64);
            }
            @Override
            protected void done() {
                try {
                    ImageIcon icono = get();
                    if (icono != null) cache.put(ruta, icono); else cache.remove(ruta);
                    lista.repaint();
                } catch (Exception e) { cache.remove(ruta); }
            }
        };
        worker.execute();
    }

    private void seleccionarStickerComentario(Component padre, java.util.function.Consumer<String> alSeleccionar) {
        SwingWorker<ArrayList<StickerItem>, Void> worker = new SwingWorker<>() {
            @Override
            protected ArrayList<StickerItem> doInBackground() throws Exception {
                ListaEnlazada<Sticker> stickers = PaginadorInsta.stickers(cliente);
                ArrayList<StickerItem> items = new ArrayList<>();
                for (Sticker sticker : stickers) {
                    ImageIcon icono = null;
                    try {
                        Respuesta respuesta = cliente.descargarImagen(sticker.getRutaImagen());
                        if (respuesta.esExitosa() && respuesta.getArchivo() != null) icono = ImagenUI.ajustar(respuesta.getArchivo(), 72, 72);
                    } catch (Exception ignored) { }
                    items.add(new StickerItem(sticker, icono));
                }
                return items;
            }
            @Override
            protected void done() {
                try {
                    ArrayList<StickerItem> items = get();
                    if (items.isEmpty()) {
                        DialogosWindows.showMessageDialog(padre, "No tienes stickers disponibles.", "Stickers", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    DefaultListModel<StickerItem> modeloStickers = new DefaultListModel<>();
                    for (StickerItem item : items) modeloStickers.addElement(item);
                    JList<StickerItem> galeria = new JList<>(modeloStickers);
                    galeria.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    galeria.setLayoutOrientation(JList.HORIZONTAL_WRAP);
                    galeria.setVisibleRowCount(0);
                    galeria.setFixedCellWidth(120);
                    galeria.setFixedCellHeight(105);
                    galeria.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
                        JLabel label = new JLabel(value.sticker.getNombre(), value.icono, SwingConstants.CENTER);
                        label.setHorizontalTextPosition(SwingConstants.CENTER);
                        label.setVerticalTextPosition(SwingConstants.BOTTOM);
                        label.setOpaque(true);
                        label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
                        label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                        return label;
                    });
                    JScrollPane scroll = new JScrollPane(galeria);
                    scroll.setPreferredSize(new Dimension(430, 260));
                    mostrarSelectorStickerEncima(padre, galeria, scroll, alSeleccionar);
                } catch (Exception e) {
                    DialogosWindows.showMessageDialog(padre, "No se pudieron cargar los stickers.", "Stickers", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }


    private void mostrarSelectorStickerEncima(Component padre, JList<StickerItem> galeria, JScrollPane scroll, java.util.function.Consumer<String> alSeleccionar) {
        java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(padre);
        javax.swing.JDialog selector = new javax.swing.JDialog(owner, "Sticker como comentario", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        selector.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel contenidoSelector = new JPanel(new BorderLayout(8, 8));
        contenidoSelector.setBorder(new EmptyBorder(12, 12, 12, 12));
        contenidoSelector.add(scroll, BorderLayout.CENTER);

        JPanel botonesSelector = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelar = new JButton("Cancelar");
        JButton enviar = new JButton("Usar sticker");
        enviar.setEnabled(false);
        botonesSelector.add(cancelar);
        botonesSelector.add(enviar);
        contenidoSelector.add(botonesSelector, BorderLayout.SOUTH);

        galeria.addListSelectionListener(e -> enviar.setEnabled(galeria.getSelectedValue() != null));
        cancelar.addActionListener(e -> selector.dispose());
        enviar.addActionListener(e -> {
            StickerItem seleccionado = galeria.getSelectedValue();
            if (seleccionado == null) return;
            selector.dispose();
            if (alSeleccionar != null) alSeleccionar.accept(seleccionado.sticker.getId());
        });
        galeria.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && galeria.getSelectedValue() != null) enviar.doClick();
            }
        });

        contenidoSelector.setBackground(TemaInsta.FONDO);
        botonesSelector.setBackground(TemaInsta.FONDO);
        galeria.setBackground(TemaInsta.FONDO_SECUNDARIO);
        galeria.setForeground(TemaInsta.TEXTO);
        galeria.setSelectionBackground(TemaInsta.INPUT);
        galeria.setSelectionForeground(TemaInsta.TEXTO);
        cancelar.setBackground(TemaInsta.INPUT);
        cancelar.setForeground(TemaInsta.TEXTO);
        enviar.setBackground(TemaInsta.BOTON);
        enviar.setForeground(TemaInsta.BOTON_TEXTO);
        TemaComponentes.corregirContraste(contenidoSelector);

        selector.setContentPane(contenidoSelector);
        selector.pack();
        selector.setMinimumSize(new Dimension(470, 340));
        selector.setLocationRelativeTo(padre);
        selector.setAlwaysOnTop(true);
        selector.setVisible(true);
    }

    private static final class StickerItem {
        private final Sticker sticker;
        private final ImageIcon icono;
        private StickerItem(Sticker sticker, ImageIcon icono) { this.sticker = sticker; this.icono = icono; }
    }

    private Dimension dimensionesImagen(byte[] datos, int anchoPredeterminado, int altoPredeterminado) {
        if (!TemaInsta.MODO_MOBILE || datos == null || datos.length == 0) {
            return new Dimension(anchoPredeterminado, altoPredeterminado);
        }

        ImageIcon original = new ImageIcon(datos);
        if (original.getIconWidth() <= 0 || original.getIconHeight() <= 0) {
            return new Dimension(anchoPredeterminado, altoPredeterminado);
        }

        double proporcion = (double) original.getIconWidth() / original.getIconHeight();
        int alto;

        if (proporcion > 1.20) {
            alto = TemaInsta.MOVIL_ALTO_HORIZONTAL;
        } else if (proporcion < 0.90) {
            alto = TemaInsta.MOVIL_ALTO_VERTICAL;
        } else {
            alto = TemaInsta.MOVIL_ALTO_CUADRADA;
        }

        return new Dimension(TemaInsta.MOVIL_ANCHO_IMAGEN, alto);
    }

    private void cargarImagen(JLabel label, int ancho, int alto) {
        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {
            @Override
            protected Respuesta doInBackground() throws Exception {
                return cliente.descargarImagen(publicacion.getRutaAdjunto());
            }

            @Override
            protected void done() {
                try {
                    Respuesta respuesta = get();
                    if (respuesta.esExitosa() && respuesta.getArchivo() != null) {
                        Dimension dimensiones = dimensionesImagen(respuesta.getArchivo(), ancho, alto);
                        ImageIcon ajustada = ImagenUI.ajustar(respuesta.getArchivo(), dimensiones.width, dimensiones.height);
                        label.setPreferredSize(dimensiones);
                        label.setMaximumSize(dimensiones);
                        label.setText("");
                        label.setIcon(ajustada);
                        label.revalidate();
                        label.repaint();
                    } else {
                        label.setText("No disponible");
                    }
                } catch (Exception e) {
                    label.setText("No disponible");
                }
            }
        };
        trabajador.execute();
    }

    private void ejecutar(AccionRemota accion) {
        setEnabled(false);
        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {
            @Override
            protected Respuesta doInBackground() throws Exception {
                return accion.ejecutar();
            }

            @Override
            protected void done() {
                setEnabled(true);
                try {
                    Respuesta respuesta = get();
                    if (!respuesta.esExitosa()) {
                        DialogosWindows.showMessageDialog(TarjetaPublicacionPanel.this, respuesta.getMensaje(), "INSTA+", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (accionActualizar != null) {
                        accionActualizar.run();
                    }
                } catch (Exception e) {
                    DialogosWindows.showMessageDialog(TarjetaPublicacionPanel.this, mensaje(e), "INSTA+", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        trabajador.execute();
    }

    private String mensaje(Exception e) {
        Throwable causa = e;
        while (causa.getCause() != null) {
            causa = causa.getCause();
        }
        return causa.getMessage() == null ? "No se pudo completar la operación." : causa.getMessage();
    }

    public static String escaparHtml(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    @Override
    public void aplicarTema() {
        setBackground(TemaInsta.TARJETA);
        encabezado.setBackground(TemaInsta.TARJETA);
        contenido.setBackground(TemaInsta.TARJETA);
        acciones.setBackground(TemaInsta.TARJETA);
        for (Component componente : acciones.getComponents()) {
            if (componente instanceof JButton boton) {
                boton.setBackground(TemaInsta.INPUT);
                boton.setForeground(TemaInsta.TEXTO);
            }
        }
        TemaComponentes.corregirContraste(this);
    }

    @FunctionalInterface
    private interface AccionRemota {
        Respuesta ejecutar() throws Exception;
    }

    private static final class JDialogSimple extends javax.swing.JDialog {
        private JDialogSimple(JPanel contenido, String titulo, Dimension tamano) {
            setTitle(titulo);
            setModal(true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setContentPane(contenido);
            setSize(tamano);
        }

        private void mostrar(Component padre) {
            setLocationRelativeTo(padre);
            setVisible(true);
        }
    }
}
