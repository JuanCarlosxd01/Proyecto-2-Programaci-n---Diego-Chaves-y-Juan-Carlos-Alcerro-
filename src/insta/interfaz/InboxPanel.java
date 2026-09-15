
package insta.interfaz;

import interfaz.DialogosWindows;

import estructuras.ListaEnlazada;
import insta.modelo.Mensaje;
import insta.modelo.Sticker;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import red.Cliente;
import red.Respuesta;

public class InboxPanel extends JPanel implements Tematizable {

    private JPanel panelIzquierdo;
    private JPanel panelDerecho;
    private JPanel panelEncabezadoChat;
    private JPanel panelEnviar;
    private JPanel panelMensajes;

    private JSplitPane divisor;

    private DefaultListModel<String> modeloConversaciones;
    private JList<String> listaConversaciones;

    private JLabel lblUsuarioChat;
    private JLabel lblConversaciones;

    private JTextField txtMensaje;

    private JButton btnEnviar;
    private JButton btnSticker;
    private JButton btnEliminar;
    private JButton btnNuevoChat;
    private JButton btnActualizar;
    private JButton btnImportarSticker;
    private JTextField txtBuscarChat;
    private JButton btnBuscarChat;

    private Cliente cliente;
    private String usuarioActual;
    private String usuarioSeleccionado;
    private final Map<String, Integer> noLeidosPorUsuario = new HashMap<>();
    private final Map<String, ImageIcon> avatares = new HashMap<>();
    private final Set<String> avataresCargando = new HashSet<>();

    public InboxPanel(Cliente cliente) {
        this.cliente = cliente;

        setLayout(new BorderLayout());

        crearInbox();
        configurarEventos();

        aplicarTema();
    }

    private void crearInbox() {
        divisor = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        divisor.setDividerLocation(260);
        divisor.setResizeWeight(0.30);
        divisor.setBorder(null);

        panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel encabezadoIzquierdo = new JPanel();
        encabezadoIzquierdo.setLayout(new BoxLayout(encabezadoIzquierdo, BoxLayout.Y_AXIS));
        encabezadoIzquierdo.setOpaque(false);

        lblConversaciones = new JLabel("Mensajes");
        lblConversaciones.setFont(new Font("Arial", Font.BOLD, 22));
        lblConversaciones.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblConversaciones.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        botones.setOpaque(false);
        botones.setAlignmentX(Component.CENTER_ALIGNMENT);
        botones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        btnNuevoChat = new JButton("+");
        btnActualizar = new JButton("Act.");
        btnImportarSticker = new JButton("+ Sticker");
        btnImportarSticker.setPreferredSize(new Dimension(82, 30));
        btnNuevoChat.setPreferredSize(new Dimension(42, 30));
        btnActualizar.setPreferredSize(new Dimension(50, 30));
        btnImportarSticker.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnNuevoChat.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnActualizar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnImportarSticker.setToolTipText("Importar sticker PNG/JPG");
        btnNuevoChat.setToolTipText("Nuevo chat");
        btnActualizar.setToolTipText("Actualizar conversaciones");

        botones.add(btnImportarSticker);
        botones.add(btnNuevoChat);
        botones.add(btnActualizar);

        encabezadoIzquierdo.add(lblConversaciones);
        encabezadoIzquierdo.add(Box.createVerticalStrut(10));
        encabezadoIzquierdo.add(botones);

        modeloConversaciones = new DefaultListModel<>();

        listaConversaciones = new JList<>(modeloConversaciones);
        listaConversaciones.setFixedCellHeight(55);
        listaConversaciones.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            int cantidad = noLeidosPorUsuario.getOrDefault(value.toLowerCase(), 0);
            String texto = "@" + value + (cantidad > 0 ? "   • " + cantidad + (cantidad == 1 ? " nuevo" : " nuevos") : "");
            JLabel label = new JLabel(texto);
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(8, 10, 8, 10));
            label.setFont(new Font("Arial", cantidad > 0 ? Font.BOLD : Font.PLAIN, 14));
            label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            return label;
        });

        JPanel superiorChats = new JPanel();
        superiorChats.setLayout(new BoxLayout(superiorChats, BoxLayout.Y_AXIS));
        superiorChats.setOpaque(false);
        superiorChats.add(encabezadoIzquierdo);
        superiorChats.add(Box.createVerticalStrut(8));
        JPanel buscadorChat = new JPanel(new BorderLayout(6, 0));
        buscadorChat.setOpaque(false);
        txtBuscarChat = new JTextField();
        txtBuscarChat.setToolTipText("Buscar usuario para iniciar un chat");
        btnBuscarChat = new JButton("Buscar");
        buscadorChat.add(txtBuscarChat, BorderLayout.CENTER);
        buscadorChat.add(btnBuscarChat, BorderLayout.EAST);
        superiorChats.add(buscadorChat);

        panelIzquierdo.add(superiorChats, BorderLayout.NORTH);
        panelIzquierdo.add(new JScrollPane(listaConversaciones), BorderLayout.CENTER);

        panelDerecho = new JPanel(new BorderLayout());

        panelEncabezadoChat = new JPanel(new BorderLayout());
        panelEncabezadoChat.setBorder(new EmptyBorder(15, 20, 15, 20));

        lblUsuarioChat = new JLabel("Selecciona una conversación");
        lblUsuarioChat.setFont(new Font("Arial", Font.BOLD, 16));

        btnEliminar = new JButton("Eliminar chat");
        btnEliminar.setToolTipText("Elimina todo el historial de esta conversación de tu Inbox");
        btnEliminar.setEnabled(false);

        panelEncabezadoChat.add(lblUsuarioChat, BorderLayout.WEST);
        panelEncabezadoChat.add(btnEliminar, BorderLayout.EAST);

        panelMensajes = new JPanel();
        panelMensajes.setLayout(new BoxLayout(panelMensajes, BoxLayout.Y_AXIS));
        panelMensajes.setBorder(new EmptyBorder(15, 15, 15, 15));

        JScrollPane scrollMensajes = new JScrollPane(panelMensajes);
        scrollMensajes.setBorder(null);
        scrollMensajes.getVerticalScrollBar().setUnitIncrement(18);

        panelEnviar = new JPanel(new BorderLayout(8, 0));
        panelEnviar.setBorder(new EmptyBorder(10, 10, 10, 10));

        btnSticker = new JButton("☺");
        txtMensaje = new JTextField();
        btnEnviar = new JButton("Enviar");

        panelEnviar.add(btnSticker, BorderLayout.WEST);
        panelEnviar.add(txtMensaje, BorderLayout.CENTER);
        panelEnviar.add(btnEnviar, BorderLayout.EAST);

        panelDerecho.add(panelEncabezadoChat, BorderLayout.NORTH);
        panelDerecho.add(scrollMensajes, BorderLayout.CENTER);
        panelDerecho.add(panelEnviar, BorderLayout.SOUTH);

        divisor.setLeftComponent(panelIzquierdo);
        divisor.setRightComponent(panelDerecho);

        add(divisor, BorderLayout.CENTER);
    }

    private void configurarEventos() {
        listaConversaciones.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String seleccionado = listaConversaciones.getSelectedValue();

                if (seleccionado != null) {
                    abrirConversacion(seleccionado);
                }
            }
        });

        btnNuevoChat.addActionListener(e -> nuevoChat());
        btnActualizar.addActionListener(e -> cargarNoLeidos());
        btnImportarSticker.addActionListener(e -> importarSticker());
        btnEnviar.addActionListener(e -> enviarMensaje());
        txtMensaje.addActionListener(e -> enviarMensaje());
        btnSticker.addActionListener(e -> seleccionarSticker());
        btnEliminar.addActionListener(e -> eliminarConversacion());
        btnBuscarChat.addActionListener(e -> buscarChat());
        txtBuscarChat.addActionListener(e -> buscarChat());
        instalarHover(btnImportarSticker);
        instalarHover(btnNuevoChat);
        instalarHover(btnActualizar);
        instalarHover(btnBuscarChat);
        instalarHover(btnSticker);
        instalarHover(btnEnviar);
    }

    private void instalarHover(JButton boton) {
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            private Color normal;
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                normal = boton.getBackground();
                if (boton.isEnabled()) boton.setBackground(TemaInsta.oscuro ? new Color(78, 78, 78) : new Color(220, 220, 220));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (normal != null) boton.setBackground(normal);
            }
        });
    }

    public void setUsuarioActual(String usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    public void cargarInbox() {
        cargarSeguidos();
        cargarNoLeidos();
        if (usuarioSeleccionado != null) cargarConversacion();
    }

    public void refrescarEnTiempoReal(ListaEnlazada<Mensaje> nuevos) {
        if (nuevos == null || nuevos.isEmpty()) return;
        boolean afectaConversacionActual = false;

        for (Mensaje mensaje : nuevos) {
            String otro = mensaje.enviadoPor(usuarioActual) ? mensaje.getReceptor() : mensaje.getEmisor();
            agregarConversacion(otro);
            if (usuarioSeleccionado != null && otro.equalsIgnoreCase(usuarioSeleccionado)) {
                afectaConversacionActual = true;
            } else {
                String clave = otro.toLowerCase();
                noLeidosPorUsuario.put(clave, noLeidosPorUsuario.getOrDefault(clave, 0) + 1);
            }
        }

        listaConversaciones.repaint();
        if (afectaConversacionActual) cargarConversacion();
    }


    private void cargarSeguidos() {
        if (usuarioActual == null || usuarioActual.isBlank()) return;
        SwingWorker<ListaEnlazada<Respuesta.DatosUsuario>, Void> trabajador = new SwingWorker<>() {
            @Override
            protected ListaEnlazada<Respuesta.DatosUsuario> doInBackground() throws Exception {
                return PaginadorInsta.usuarios(desde -> cliente.seguidos(usuarioActual, desde));
            }

            @Override
            protected void done() {
                try {
                    for (Respuesta.DatosUsuario usuario : get()) agregarConversacion(usuario.getUsername());
                } catch (Exception e) {
                    System.out.println("No se pudieron cargar los usuarios seguidos: " + e.getMessage());
                }
            }
        };
        trabajador.execute();
    }

    private void buscarChat() {
        String texto = txtBuscarChat.getText().trim().replaceFirst("^@", "");
        if (texto.isBlank()) return;
        btnBuscarChat.setEnabled(false);
        SwingWorker<ListaEnlazada<Respuesta.DatosUsuario>, Void> trabajador = new SwingWorker<>() {
            @Override
            protected ListaEnlazada<Respuesta.DatosUsuario> doInBackground() throws Exception {
                return PaginadorInsta.usuarios(desde -> cliente.buscarPersonas(texto, desde));
            }

            @Override
            protected void done() {
                btnBuscarChat.setEnabled(true);
                try {
                    ListaEnlazada<Respuesta.DatosUsuario> resultados = get();
                    if (resultados.isEmpty()) {
                        DialogosWindows.showMessageDialog(InboxPanel.this, "No se encontraron usuarios.", "Buscar chat", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    DefaultListModel<Respuesta.DatosUsuario> modelo = new DefaultListModel<>();
                    for (Respuesta.DatosUsuario usuario : resultados) {
                        if (usuarioActual == null || !usuario.getUsername().equalsIgnoreCase(usuarioActual)) modelo.addElement(usuario);
                    }
                    JList<Respuesta.DatosUsuario> lista = new JList<>(modelo);
                    lista.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
                        JLabel label = new JLabel("@" + value.getUsername() + "  ·  " + value.getNombreCompleto());
                        label.setOpaque(true);
                        label.setBorder(new EmptyBorder(8, 10, 8, 10));
                        label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
                        label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                        return label;
                    });
                    JScrollPane scroll = new JScrollPane(lista);
                    scroll.setPreferredSize(new Dimension(360, 220));
                    int opcion = DialogosWindows.showConfirmDialog(InboxPanel.this, scroll, "Selecciona un usuario", JOptionPane.OK_CANCEL_OPTION);
                    if (opcion == JOptionPane.OK_OPTION && lista.getSelectedValue() != null) {
                        String username = lista.getSelectedValue().getUsername();
                        agregarConversacion(username);
                        listaConversaciones.setSelectedValue(username, true);
                        txtBuscarChat.setText("");
                    }
                } catch (Exception e) {
                    DialogosWindows.showMessageDialog(InboxPanel.this, "No se pudo buscar usuarios.", "Buscar chat", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        trabajador.execute();
    }
    private void nuevoChat() {
        String username = DialogosWindows.showInputDialog(this, "Username de la persona:", "Nueva conversación", JOptionPane.PLAIN_MESSAGE);

        if (username == null) {
            return;
        }

        username = username.trim().toLowerCase();

        if (username.isBlank()) {
            return;
        }

        if (usuarioActual != null && username.equalsIgnoreCase(usuarioActual)) {
            DialogosWindows.showMessageDialog(this, "No puedes enviarte mensajes a ti mismo.", "INSTA+", JOptionPane.WARNING_MESSAGE);
            return;
        }

        agregarConversacion(username);
        listaConversaciones.setSelectedValue(username, true);
    }

    private void cargarNoLeidos() {
        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {

            @Override
            protected Respuesta doInBackground() throws Exception {
                return cliente.noLeidos(0);
            }

            @Override
            protected void done() {
                try {
                    Respuesta respuesta = get();

                    if (!respuesta.esExitosa()) {
                        return;
                    }

                    ListaEnlazada<Mensaje> mensajes = respuesta.getMensajes();

                    for (Mensaje mensaje : mensajes) {
                        agregarConversacion(mensaje.getEmisor());
                    }

                } catch (Exception e) {
                    System.out.println("No se pudieron cargar mensajes no leídos: " + e.getMessage());
                }
            }
        };

        trabajador.execute();
    }

    private void agregarConversacion(String usuario) {
        if (usuario == null || usuario.isBlank()) {
            return;
        }

        for (int i = 0; i < modeloConversaciones.size(); i++) {
            if (modeloConversaciones.get(i).equalsIgnoreCase(usuario)) {
                return;
            }
        }

        modeloConversaciones.addElement(usuario);
    }

    private void abrirConversacion(String usuario) {
        usuarioSeleccionado = usuario;
        noLeidosPorUsuario.remove(usuario.toLowerCase());
        listaConversaciones.repaint();

        lblUsuarioChat.setText("@" + usuario);
        btnEliminar.setEnabled(true);

        cargarConversacion();
    }

    private void cargarConversacion() {
        if (usuarioSeleccionado == null) {
            return;
        }

        panelMensajes.removeAll();

        JLabel cargando = new JLabel("Cargando conversación...");
        cargando.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelMensajes.add(cargando);

        panelMensajes.revalidate();
        panelMensajes.repaint();

        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {

            @Override
            protected Respuesta doInBackground() throws Exception {
                return cliente.conversacion(usuarioSeleccionado, 0);
            }

            @Override
            protected void done() {
                panelMensajes.removeAll();

                try {
                    Respuesta respuesta = get();

                    if (!respuesta.esExitosa()) {
                        mostrarMensajeSistema(respuesta.getMensaje());
                        return;
                    }

                    ListaEnlazada<Mensaje> mensajes = respuesta.getMensajes();

                    if (mensajes.isEmpty()) {
                        mostrarMensajeSistema("Aún no hay mensajes.");
                    } else {
                        for (Mensaje mensaje : mensajes) {
                            agregarMensaje(mensaje);
                        }
                    }

                    marcarComoLeidos();

                } catch (Exception e) {
                    mostrarMensajeSistema("No se pudo cargar la conversación.");
                }

                panelMensajes.revalidate();
                panelMensajes.repaint();
            }
        };

        trabajador.execute();
    }

    private void agregarMensaje(Mensaje mensaje) {
        boolean mio = mensaje.enviadoPor(usuarioActual);
        String autor = mio ? usuarioActual : mensaje.getEmisor();

        JPanel contenedor = new JPanel(new FlowLayout(mio ? FlowLayout.RIGHT : FlowLayout.LEFT, 8, 2));
        contenedor.setOpaque(false);
        contenedor.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel avatar = crearAvatar(autor);
        JPanel burbuja = new JPanel();
        burbuja.setLayout(new BoxLayout(burbuja, BoxLayout.Y_AXIS));
        burbuja.setBorder(new EmptyBorder(8, 12, 8, 12));
        burbuja.setBackground(mio ? TemaInsta.BOTON : TemaInsta.INPUT);

        JLabel autorLabel = new JLabel(mio ? "Tú" : "@" + autor);
        autorLabel.setFont(new Font("Arial", Font.BOLD, 10));
        autorLabel.setForeground(mio ? TemaInsta.BOTON_TEXTO : TemaInsta.BOTON);
        burbuja.add(autorLabel);
        burbuja.add(Box.createVerticalStrut(2));

        if (mensaje.esTexto()) {
            JLabel texto = new JLabel("<html><div style='width:250px;'>" + TarjetaPublicacionPanel.escaparHtml(mensaje.getContenido()) + "</div></html>");
            texto.setForeground(mio ? TemaInsta.BOTON_TEXTO : TemaInsta.TEXTO);
            burbuja.add(texto);
        } else {
            JLabel sticker = new JLabel("Cargando sticker...", SwingConstants.CENTER);
            sticker.setPreferredSize(new Dimension(100, 100));
            sticker.setMaximumSize(new Dimension(100, 100));
            sticker.setForeground(mio ? TemaInsta.BOTON_TEXTO : TemaInsta.TEXTO);
            burbuja.add(sticker);
            cargarImagen(mensaje.getRutaSticker(), sticker, 100, 100);
        }

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("HH:mm");
        JLabel hora = new JLabel(mensaje.getFechaEnvio().format(formato));
        hora.setFont(new Font("Arial", Font.PLAIN, 9));
        hora.setForeground(mio ? new Color(230, 245, 255) : TemaInsta.TEXTO_SECUNDARIO);

        burbuja.add(Box.createVerticalStrut(3));
        burbuja.add(hora);

        contenedor.add(avatar);
        contenedor.add(burbuja);

        Dimension preferida = contenedor.getPreferredSize();
        contenedor.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferida.height));
        contenedor.setPreferredSize(new Dimension(preferida.width, preferida.height));
        panelMensajes.add(contenedor);
        panelMensajes.add(Box.createVerticalStrut(4));
    }

    private JLabel crearAvatar(String username) {
        JLabel avatar = new JLabel(inicial(username), SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(38, 38));
        avatar.setMinimumSize(new Dimension(38, 38));
        avatar.setMaximumSize(new Dimension(38, 38));
        avatar.setOpaque(true);
        avatar.setBackground(TemaInsta.BORDE);
        avatar.setForeground(TemaInsta.TEXTO);
        avatar.setFont(new Font("Arial", Font.BOLD, 14));
        avatar.setBorder(BorderFactory.createLineBorder(TemaInsta.BORDE));
        cargarAvatar(username, avatar);
        return avatar;
    }

    private String inicial(String username) {
        if (username == null || username.isBlank()) return "?";
        return username.substring(0, 1).toUpperCase();
    }

    private void cargarAvatar(String username, JLabel destino) {
        if (username == null || username.isBlank()) return;
        String clave = username.toLowerCase();
        ImageIcon cache = avatares.get(clave);
        if (cache != null) {
            destino.setText("");
            destino.setIcon(cache);
            return;
        }
        if (!avataresCargando.add(clave)) return;

        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                Respuesta perfil = cliente.perfil(username);
                if (!perfil.esExitosa() || !perfil.tieneUsuario()) return null;
                String ruta = perfil.getUsuario().getRutaFotoPerfil();
                if (ruta == null || ruta.isBlank()) return null;
                Respuesta imagen = cliente.descargarImagen(ruta);
                if (!imagen.esExitosa() || imagen.getArchivo() == null) return null;
                return ImagenUI.ajustar(imagen.getArchivo(), 38, 38);
            }

            @Override
            protected void done() {
                avataresCargando.remove(clave);
                try {
                    ImageIcon icono = get();
                    if (icono != null) {
                        avatares.put(clave, icono);
                        destino.setText("");
                        destino.setIcon(icono);
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    public void invalidarAvatar(String username) {
        if (username == null || username.isBlank()) return;
        String clave = username.toLowerCase();
        avatares.remove(clave);
        avataresCargando.remove(clave);
        if (usuarioSeleccionado != null) cargarConversacion();
        listaConversaciones.repaint();
    }

    public void abrirConversacionDesdeNotificacion(String usuario) {
        if (usuario == null || usuario.isBlank()) return;
        agregarConversacion(usuario);
        listaConversaciones.setSelectedValue(usuario, true);
        abrirConversacion(usuario);
    }

    private void enviarMensaje() {
        if (usuarioSeleccionado == null) {
            DialogosWindows.showMessageDialog(this, "Seleccione una conversación primero.", "INSTA+", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String texto = txtMensaje.getText().trim();

        if (texto.isBlank()) {
            return;
        }

        btnEnviar.setEnabled(false);

        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {

            @Override
            protected Respuesta doInBackground() throws Exception {
                return cliente.enviarMensaje(usuarioSeleccionado, texto);
            }

            @Override
            protected void done() {
                btnEnviar.setEnabled(true);

                try {
                    Respuesta respuesta = get();

                    if (respuesta.esExitosa()) {
                        txtMensaje.setText("");
                        cargarConversacion();
                    } else {
                        DialogosWindows.showMessageDialog(InboxPanel.this, respuesta.getMensaje(), "Mensaje", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (Exception e) {
                    DialogosWindows.showMessageDialog(InboxPanel.this, "No se pudo enviar el mensaje.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        trabajador.execute();
    }

    private void seleccionarSticker() {
        if (usuarioSeleccionado == null) {
            DialogosWindows.showMessageDialog(this, "Seleccione una conversación primero.", "INSTA+", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnSticker.setEnabled(false);
        SwingWorker<ArrayList<StickerGaleria>, Void> trabajador = new SwingWorker<>() {
            @Override
            protected ArrayList<StickerGaleria> doInBackground() throws Exception {
                ListaEnlazada<Sticker> stickers = PaginadorInsta.stickers(cliente);
                ArrayList<StickerGaleria> galeria = new ArrayList<>();
                for (Sticker sticker : stickers) {
                    ImageIcon icono = null;
                    try {
                        Respuesta imagen = cliente.descargarImagen(sticker.getRutaImagen());
                        if (imagen.esExitosa() && imagen.getArchivo() != null) {
                            ImageIcon original = new ImageIcon(imagen.getArchivo());
                            int w = Math.max(1, original.getIconWidth());
                            int h = Math.max(1, original.getIconHeight());
                            double escala = Math.min(72.0 / w, 72.0 / h);
                            Image ajustada = original.getImage().getScaledInstance(Math.max(1, (int)(w * escala)), Math.max(1, (int)(h * escala)), Image.SCALE_SMOOTH);
                            icono = new ImageIcon(ajustada);
                        }
                    } catch (Exception ignored) {
                    }
                    galeria.add(new StickerGaleria(sticker, icono));
                }
                return galeria;
            }

            @Override
            protected void done() {
                btnSticker.setEnabled(true);
                try {
                    ArrayList<StickerGaleria> galeria = get();
                    if (galeria.isEmpty()) {
                        DialogosWindows.showMessageDialog(InboxPanel.this, "No tienes stickers disponibles.", "Stickers", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    DefaultListModel<StickerGaleria> modelo = new DefaultListModel<>();
                    for (StickerGaleria item : galeria) modelo.addElement(item);
                    JList<StickerGaleria> lista = new JList<>(modelo);
                    lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    lista.setLayoutOrientation(JList.HORIZONTAL_WRAP);
                    lista.setVisibleRowCount(0);
                    lista.setFixedCellWidth(125);
                    lista.setFixedCellHeight(110);
                    lista.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
                        JLabel label = new JLabel(value.sticker.getNombre(), value.icono, SwingConstants.CENTER);
                        label.setHorizontalTextPosition(SwingConstants.CENTER);
                        label.setVerticalTextPosition(SwingConstants.BOTTOM);
                        label.setOpaque(true);
                        label.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
                        label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
                        label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                        return label;
                    });
                    JScrollPane scroll = new JScrollPane(lista);
                    scroll.setPreferredSize(new Dimension(430, 260));
                    int opcion = DialogosWindows.showConfirmDialog(InboxPanel.this, scroll, "Galería de stickers", JOptionPane.OK_CANCEL_OPTION);
                    if (opcion == JOptionPane.OK_OPTION && lista.getSelectedValue() != null) {
                        enviarSticker(lista.getSelectedValue().sticker);
                    }
                } catch (Exception e) {
                    DialogosWindows.showMessageDialog(InboxPanel.this, "No se pudieron cargar los stickers.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        trabajador.execute();
    }

    private void importarSticker() {
        File archivo = SelectorArchivosZ.seleccionarSticker(this, "Importar sticker");
        if (archivo == null) return;
        ImageIcon original = new ImageIcon(archivo.getAbsolutePath());
        ImageIcon previa = original;
        if (original.getIconWidth() > 0 && original.getIconHeight() > 0) {
            double escala = Math.min(140.0 / original.getIconWidth(), 140.0 / original.getIconHeight());
            Image imagen = original.getImage().getScaledInstance(Math.max(1, (int)(original.getIconWidth() * escala)), Math.max(1, (int)(original.getIconHeight() * escala)), Image.SCALE_SMOOTH);
            previa = new ImageIcon(imagen);
        }
        JLabel vista = new JLabel("<html><center>¿Deseas añadir este sticker?<br><br></center></html>", previa, SwingConstants.CENTER);
        vista.setHorizontalTextPosition(SwingConstants.CENTER);
        vista.setVerticalTextPosition(SwingConstants.TOP);
        int confirmar = DialogosWindows.showConfirmDialog(this, vista, "Previsualizar sticker", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirmar != JOptionPane.OK_OPTION) return;
        String nombre = DialogosWindows.showInputDialog(this, "Nombre del sticker:", quitarExtension(archivo.getName()));
        if (nombre == null || nombre.trim().isEmpty()) return;

        btnImportarSticker.setEnabled(false);
        SwingWorker<Respuesta, Void> worker = new SwingWorker<>() {
            @Override
            protected Respuesta doInBackground() throws Exception {
                return cliente.importarSticker(nombre.trim(), archivo.getName(), Files.readAllBytes(archivo.toPath()));
            }

            @Override
            protected void done() {
                btnImportarSticker.setEnabled(true);
                try {
                    Respuesta respuesta = get();
                    if (respuesta.esExitosa()) {
                        DialogosWindows.showMessageDialog(InboxPanel.this, "Sticker importado correctamente.", "Stickers", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        DialogosWindows.showMessageDialog(InboxPanel.this, respuesta.getMensaje(), "Stickers", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    DialogosWindows.showMessageDialog(InboxPanel.this, "No se pudo importar el sticker.", "Stickers", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private String quitarExtension(String nombre) {
        int punto = nombre.lastIndexOf('.');
        return punto > 0 ? nombre.substring(0, punto) : nombre;
    }

    private static final class StickerGaleria {
        private final Sticker sticker;
        private final ImageIcon icono;

        private StickerGaleria(Sticker sticker, ImageIcon icono) {
            this.sticker = sticker;
            this.icono = icono;
        }

        @Override
        public String toString() {
            return sticker.getNombre();
        }
    }

    private void enviarSticker(Sticker sticker) {
        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {

            @Override
            protected Respuesta doInBackground() throws Exception {
                return cliente.enviarSticker(usuarioSeleccionado, sticker.getId());
            }

            @Override
            protected void done() {
                try {
                    Respuesta respuesta = get();

                    if (respuesta.esExitosa()) {
                        cargarConversacion();
                    } else {
                        DialogosWindows.showMessageDialog(InboxPanel.this, respuesta.getMensaje(), "Sticker", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (Exception e) {
                    DialogosWindows.showMessageDialog(InboxPanel.this, "No se pudo enviar el sticker.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        trabajador.execute();
    }

    private void marcarComoLeidos() {
        if (usuarioSeleccionado == null) {
            return;
        }

        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {

            @Override
            protected Respuesta doInBackground() throws Exception {
                return cliente.marcarLeidos(usuarioSeleccionado);
            }
        };

        trabajador.execute();
    }

    private void eliminarConversacion() {
        if (usuarioSeleccionado == null) {
            return;
        }

        int opcion = DialogosWindows.showConfirmDialog(this, "¿Eliminar todo el historial con @" + usuarioSeleccionado + "?\n\nEsta acción quitará los mensajes de tu Inbox, pero no dejarás de seguir a la persona.", "Eliminar conversación", JOptionPane.YES_NO_OPTION);

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        String usuarioEliminar = usuarioSeleccionado;

        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {

            @Override
            protected Respuesta doInBackground() throws Exception {
                return cliente.eliminarConversacion(usuarioEliminar);
            }

            @Override
            protected void done() {
                try {
                    Respuesta respuesta = get();

                    if (respuesta.esExitosa()) {
                        modeloConversaciones.removeElement(usuarioEliminar);
                        noLeidosPorUsuario.remove(usuarioEliminar.toLowerCase());
                        usuarioSeleccionado = null;
                        listaConversaciones.clearSelection();
                        lblUsuarioChat.setText("Selecciona una conversación");
                        btnEliminar.setEnabled(false);
                        txtMensaje.setText("");
                        panelMensajes.removeAll();
                        mostrarMensajeSistema("Conversación eliminada de tu historial.");
                        panelMensajes.revalidate();
                        panelMensajes.repaint();
                    } else {
                        DialogosWindows.showMessageDialog(InboxPanel.this, respuesta.getMensaje(), "Eliminar conversación", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (Exception e) {
                    DialogosWindows.showMessageDialog(InboxPanel.this, "No se pudo eliminar la conversación.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        trabajador.execute();
    }

    private void cargarImagen(String ruta, JLabel label, int ancho, int alto) {
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
                        ImageIcon ajustada = ImagenUI.ajustar(respuesta.getArchivo(), ancho, alto);
                        label.setText("");
                        label.setIcon(ajustada);
                    } else {
                        label.setText("Sticker");
                    }

                } catch (Exception e) {
                    label.setText("Sticker");
                }
            }
        };

        trabajador.execute();
    }

    private void mostrarMensajeSistema(String mensaje) {
        JLabel label = new JLabel(mensaje);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelMensajes.add(label);
    }

    @Override
    public void aplicarTema() {
        setBackground(TemaInsta.FONDO);

        panelIzquierdo.setBackground(TemaInsta.FONDO);
        panelDerecho.setBackground(TemaInsta.FONDO);
        panelEncabezadoChat.setBackground(TemaInsta.FONDO);
        panelEnviar.setBackground(TemaInsta.FONDO);
        panelMensajes.setBackground(TemaInsta.FONDO_SECUNDARIO);

        divisor.setBackground(TemaInsta.BORDE);

        listaConversaciones.setBackground(TemaInsta.FONDO);
        listaConversaciones.setForeground(TemaInsta.TEXTO);
        listaConversaciones.setSelectionBackground(TemaInsta.INPUT);
        listaConversaciones.setSelectionForeground(TemaInsta.TEXTO);

        lblUsuarioChat.setForeground(TemaInsta.TEXTO);
        lblConversaciones.setForeground(TemaInsta.TEXTO);

        txtMensaje.setBackground(TemaInsta.INPUT);
        txtMensaje.setForeground(TemaInsta.TEXTO);
        txtMensaje.setCaretColor(TemaInsta.TEXTO);

        btnEnviar.setBackground(TemaInsta.BOTON);
        btnEnviar.setForeground(TemaInsta.BOTON_TEXTO);

        btnSticker.setBackground(TemaInsta.INPUT);
        btnSticker.setForeground(TemaInsta.TEXTO);

        btnEliminar.setBackground(new Color(190, 45, 55));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFocusPainted(false);
        btnEliminar.setOpaque(true);
        btnEliminar.setContentAreaFilled(true);

        btnNuevoChat.setBackground(TemaInsta.INPUT);
        btnNuevoChat.setForeground(TemaInsta.TEXTO);

        btnActualizar.setBackground(TemaInsta.INPUT);
        btnActualizar.setForeground(TemaInsta.TEXTO);
        btnImportarSticker.setBackground(TemaInsta.INPUT);
        btnImportarSticker.setForeground(TemaInsta.TEXTO);

        cambiarTexto(this);

        TemaComponentes.corregirContraste(this);

        revalidate();
        repaint();
    }

    private void cambiarTexto(Container contenedor) {
        for (Component componente : contenedor.getComponents()) {
            if (componente instanceof Container interno) {
                cambiarTexto(interno);
            }
        }
    }

    public DefaultListModel<String> getModeloConversaciones() {
        return modeloConversaciones;
    }

    public JList<String> getListaConversaciones() {
        return listaConversaciones;
    }

    public JTextField getTxtMensaje() {
        return txtMensaje;
    }

    public JButton getBtnEnviar() {
        return btnEnviar;
    }

    public JButton getBtnSticker() {
        return btnSticker;
    }

    public JButton getBtnEliminar() {
        return btnEliminar;
    }

    public JPanel getPanelMensajes() {
        return panelMensajes;
    }
}