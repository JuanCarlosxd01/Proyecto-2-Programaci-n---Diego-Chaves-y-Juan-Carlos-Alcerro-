
package insta.interfaz;

import interfaz.DialogosWindows;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;
import javax.swing.border.*;
import red.Cliente;
import red.Respuesta;
import hilos.HiloNotificaciones;
import insta.modelo.Mensaje;
import estructuras.ListaEnlazada;

public class InstaPanel extends JPanel {

    private CardLayout cardLayout;
    private JPanel panelContenido;
    private JPanel panelMenu;
    private JPanel panelSuperior;
    private String usuarioPerfilActual;

    private JButton btnInicio;
    private JButton btnPerfil;
    private JButton btnPublicar;
    private JButton btnInteracciones;
    private JButton btnBuscar;
    private JButton btnHashtag;
    private JButton btnInbox;
    private JButton btnEditarPerfil;
    private JButton btnCerrarSesion;

    private TimelinePanel timelinePanel;
    private PerfilPanel perfilPanel;
    private PublicarPanel publicarPanel;
    private InteraccionesPanel interaccionesPanel;
    private BuscarPanel buscarPanel;
    private HashtagPanel hashtagPanel;
    private InboxPanel inboxPanel;
    private EditarPerfilPanel editarPerfilPanel;

    private JLabel lblLogo;
    private JLabel lblUsuario;
    private JLabel lblNotificacion;
    private JLabel lblSol;
    private JLabel lblLuna;

    private JTextField txtBusquedaGeneral;

    private SwitchTema switchTema;

    private Runnable accionCerrar;
    private Cliente cliente;
    private String usuarioActual;
    private HiloNotificaciones hiloNotificaciones;
    private int mensajesPendientes;
    private int mencionesPendientes;
    private Popup popupNotificacion;
    private Timer timerPopupNotificacion;

    public InstaPanel(Cliente cliente) {
        this.cliente = cliente;

        TemaInsta.cambiarTema(false);

        setLayout(new BorderLayout());

        crearMenu();
        crearBarraSuperior();
        crearContenido();
        configurarEventos();
        aplicarTema();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                actualizarModoResponsive();
            }
        });
    }

    private void crearMenu() {
        panelMenu = new JPanel();
        panelMenu.setLayout(new BoxLayout(panelMenu, BoxLayout.Y_AXIS));
        panelMenu.setPreferredSize(new Dimension(240, 0));

        panelMenu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, TemaInsta.BORDE),
                new EmptyBorder(25, 15, 20, 15)
        ));

        lblLogo = new JLabel("INSTA+");
        lblLogo.setFont(new Font("Arial", Font.BOLD, 27));
        lblLogo.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelMenu.add(lblLogo);
        panelMenu.add(Box.createVerticalStrut(35));

        btnInicio = crearBotonMenu("⌂   Inicio");
        btnBuscar = crearBotonMenu("⌕   Buscar");
        btnHashtag = crearBotonMenu("#   Explorar");
        btnInbox = crearBotonMenu("✉   Mensajes");
        btnInteracciones = crearBotonMenu("♡   Notificaciones");
        btnPublicar = crearBotonMenu("⊞   Crear");
        btnPerfil = crearBotonMenu("◉   Perfil");
        btnEditarPerfil = crearBotonMenu("⚙   Configuración");

        panelMenu.add(btnInicio);
        panelMenu.add(Box.createVerticalStrut(5));

        panelMenu.add(btnBuscar);
        panelMenu.add(Box.createVerticalStrut(5));

        panelMenu.add(btnHashtag);
        panelMenu.add(Box.createVerticalStrut(5));

        panelMenu.add(btnInbox);
        panelMenu.add(Box.createVerticalStrut(5));

        panelMenu.add(btnInteracciones);
        panelMenu.add(Box.createVerticalStrut(5));

        panelMenu.add(btnPublicar);
        panelMenu.add(Box.createVerticalStrut(5));

        panelMenu.add(btnPerfil);
        panelMenu.add(Box.createVerticalStrut(5));

        panelMenu.add(btnEditarPerfil);

        panelMenu.add(Box.createVerticalGlue());

        btnCerrarSesion = crearBotonMenu("↪   Cerrar sesión");

        panelMenu.add(btnCerrarSesion);

        add(panelMenu, BorderLayout.WEST);
    }

    private JButton crearBotonMenu(String texto) {
        JButton boton = new JButton(texto);

        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        boton.setPreferredSize(new Dimension(210, 48));
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 15));
        boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(10, 12, 10, 12));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);

        boton.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (TemaInsta.oscuro) {
                    boton.setBackground(new Color(30, 30, 30));
                } else {
                    boton.setBackground(new Color(245, 245, 245));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                boton.setBackground(TemaInsta.FONDO);
            }
        });

        return boton;
    }

    private void crearBarraSuperior() {
        panelSuperior = new JPanel(new BorderLayout(20, 0));
        panelSuperior.setPreferredSize(new Dimension(0, 70));

        panelSuperior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, TemaInsta.BORDE),
                new EmptyBorder(12, 25, 12, 25)
        ));

        txtBusquedaGeneral = new JTextField();
        txtBusquedaGeneral.setFont(new Font("Arial", Font.PLAIN, 14));
        txtBusquedaGeneral.setBorder(new EmptyBorder(10, 15, 10, 15));
        txtBusquedaGeneral.setPreferredSize(new Dimension(350, 40));

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        derecha.setOpaque(false);

        lblSol = new JLabel("☀");
        lblSol.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));

        switchTema = new SwitchTema();

        lblLuna = new JLabel("☾");
        lblLuna.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));

        lblNotificacion = new JLabel("");
        lblNotificacion.setFont(new Font("Arial", Font.BOLD, 12));
        lblNotificacion.setForeground(new Color(220, 45, 70));

        lblUsuario = new JLabel("@usuario");
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 14));

        derecha.add(lblNotificacion);
        derecha.add(lblSol);
        derecha.add(switchTema);
        derecha.add(lblLuna);
        derecha.add(Box.createHorizontalStrut(10));
        derecha.add(lblUsuario);

        panelSuperior.add(txtBusquedaGeneral, BorderLayout.CENTER);
        panelSuperior.add(derecha, BorderLayout.EAST);

        add(panelSuperior, BorderLayout.NORTH);
    }

    private void crearContenido() {
        cardLayout = new CardLayout();

        panelContenido = new JPanel(cardLayout);

        timelinePanel = new TimelinePanel(cliente);
        perfilPanel = new PerfilPanel(cliente);
        timelinePanel.setAccionAbrirPerfil(username -> {
            cargarPerfil(username);
            mostrarPanel("PERFIL");
        });
        perfilPanel.setAccionAbrirPerfil(username -> {
            cargarPerfil(username);
            mostrarPanel("PERFIL");
        });
        publicarPanel = new PublicarPanel(cliente);
        interaccionesPanel = new InteraccionesPanel(cliente);
        buscarPanel = new BuscarPanel(cliente);
        hashtagPanel = new HashtagPanel(cliente);
        inboxPanel = new InboxPanel(cliente);
        editarPerfilPanel = new EditarPerfilPanel(cliente);
        editarPerfilPanel.setAccionPerfilActualizado(() -> {
            Respuesta.DatosUsuario actual = cliente.getUsuarioActual();
            if (actual != null) {
                inboxPanel.invalidarAvatar(actual.getUsername());
                timelinePanel.actualizarHistorias();
                cargarPerfil(actual.getUsername());
            }
        });

        publicarPanel.setAccionPublicacionCreada(() -> {
            timelinePanel.cargarTimeline();

            if (usuarioActual != null) {
                cargarPerfil(usuarioActual);
            }

            mostrarPanel("TIMELINE");
        });

        buscarPanel.setAccionSeguimientoActualizado(() -> {
            timelinePanel.cargarTimeline();

            if (usuarioActual != null) {
                cargarPerfil(usuarioActual);
            }
        });

        buscarPanel.setAccionAbrirPerfil(username -> {
            cargarPerfil(username);
            mostrarPanel("PERFIL");
        });

        panelContenido.add(timelinePanel, "TIMELINE");
        panelContenido.add(perfilPanel, "PERFIL");
        panelContenido.add(publicarPanel, "PUBLICAR");
        panelContenido.add(interaccionesPanel, "INTERACCIONES");
        panelContenido.add(buscarPanel, "BUSCAR");
        panelContenido.add(hashtagPanel, "HASHTAG");
        panelContenido.add(inboxPanel, "INBOX");
        panelContenido.add(editarPerfilPanel, "EDITAR");

        add(panelContenido, BorderLayout.CENTER);

        cardLayout.show(panelContenido, "TIMELINE");
    }

    private void configurarEventos() {
        btnInicio.addActionListener(e -> {
            mostrarPanel("TIMELINE");
            timelinePanel.cargarTimeline();
        });

        btnPerfil.addActionListener(e -> {
            if (usuarioActual != null) {
                cargarPerfil(usuarioActual);
            }

            mostrarPanel("PERFIL");
        });

        btnPublicar.addActionListener(e -> {
            publicarPanel.prepararPublicacion();
            mostrarPanel("PUBLICAR");
        });
        btnInteracciones.addActionListener(e -> {
            mencionesPendientes = 0;
            actualizarContadoresNotificacion();
            interaccionesPanel.cargarMenciones();
            mostrarPanel("INTERACCIONES");
        });
        btnBuscar.addActionListener(e -> mostrarPanel("BUSCAR"));
        btnHashtag.addActionListener(e -> mostrarPanel("HASHTAG"));
        btnInbox.addActionListener(e -> {
            mensajesPendientes = 0;
            actualizarContadoresNotificacion();
            lblNotificacion.setText("");
            ocultarPopupNotificacion();
            inboxPanel.cargarInbox();
            mostrarPanel("INBOX");
        });
        btnEditarPerfil.addActionListener(e -> {
            editarPerfilPanel.cargarDatos();
            mostrarPanel("EDITAR");
        });
        btnCerrarSesion.addActionListener(e -> cerrarSesion());

        txtBusquedaGeneral.addActionListener(e -> ejecutarBusquedaGeneral());

        perfilPanel.getBtnSeguir().addActionListener(e -> cambiarSeguimiento());

        switchTema.addPropertyChangeListener("activado", e -> {
            boolean oscuro = switchTema.isActivado();

            TemaInsta.cambiarTema(oscuro);

            aplicarTema();
        });
    }

    private void ejecutarBusquedaGeneral() {
        String texto = txtBusquedaGeneral.getText().trim();
        if (texto.isEmpty()) return;

        if (texto.startsWith("#")) {
            String hashtag = texto.substring(1).trim();
            if (hashtag.isEmpty()) return;
            hashtagPanel.getTxtHashtag().setText(hashtag);
            mostrarPanel("HASHTAG");
            hashtagPanel.getBtnBuscar().doClick();
        } else {
            String usuario = texto.startsWith("@") ? texto.substring(1).trim() : texto;
            if (usuario.isEmpty()) return;
            buscarPanel.getTxtBusqueda().setText(usuario);
            mostrarPanel("BUSCAR");
            buscarPanel.getBtnBuscar().doClick();
        }
    }

    private void cambiarSeguimiento() {
        if (usuarioPerfilActual == null) {
            return;
        }

        if (usuarioActual != null && usuarioPerfilActual.equalsIgnoreCase(usuarioActual)) {
            return;
        }

        JButton boton = perfilPanel.getBtnSeguir();

        boolean dejarDeSeguir = boton.getText().equalsIgnoreCase("Dejar de seguir");

        if (dejarDeSeguir) {
            int opcion = DialogosWindows.showConfirmDialog(this, "¿Dejar de seguir a @" + usuarioPerfilActual + "?", "Dejar de seguir", JOptionPane.YES_NO_OPTION);
            if (opcion != JOptionPane.YES_OPTION) {
                return;
            }
        }

        boton.setEnabled(false);

        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {

            @Override
            protected Respuesta doInBackground() throws Exception {
                if (dejarDeSeguir) {
                    return cliente.dejarDeSeguir(usuarioPerfilActual);
                }

                return cliente.seguir(usuarioPerfilActual);
            }

            @Override
            protected void done() {
                try {
                    Respuesta respuesta = get();

                    if (respuesta.esExitosa()) {
                        cargarPerfil(usuarioPerfilActual);
                        timelinePanel.cargarTimeline();
                    } else {
                        DialogosWindows.showMessageDialog(
                                InstaPanel.this,
                                respuesta.getMensaje(),
                                "Seguimiento",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }

                } catch (Exception e) {
                    DialogosWindows.showMessageDialog(
                            InstaPanel.this,
                            "No se pudo actualizar el seguimiento.\n" + e.getMessage(),
                            "Seguimiento",
                            JOptionPane.ERROR_MESSAGE
                    );
                }

                boton.setEnabled(true);
            }
        };

        trabajador.execute();
    }

    private void cargarPerfil(String username) {
        if (username == null || username.isBlank()) {
            return;
        }

        usuarioPerfilActual = username;

        perfilPanel.mostrarCargando();

        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {

            @Override
            protected Respuesta doInBackground() throws Exception {
                return cliente.perfil(username);
            }

            @Override
            protected void done() {
                try {
                    Respuesta respuesta = get();

                    if (respuesta.esExitosa() && respuesta.tieneUsuario()) {
                        Respuesta.DatosUsuario usuario = respuesta.getUsuario();

                        perfilPanel.mostrarUsuario(usuario);

                        boolean esMiPerfil = usuarioActual != null && usuario.getUsername().equalsIgnoreCase(usuarioActual);

                        perfilPanel.configurarComoPerfilPropio(esMiPerfil);

                        perfilPanel.cargarPublicaciones(username);

                    } else {
                        DialogosWindows.showMessageDialog(
                                InstaPanel.this,
                                respuesta.getMensaje(),
                                "Perfil",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }

                } catch (Exception e) {
                    DialogosWindows.showMessageDialog(
                            InstaPanel.this,
                            "No se pudo cargar el perfil.\n" + e.getMessage(),
                            "Perfil",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        trabajador.execute();
    }

    private void actualizarModoResponsive() {
        boolean movil = getWidth() > 0 && getWidth() < 900;
        TemaInsta.cambiarModoMobile(movil);
        panelMenu.setPreferredSize(new Dimension(movil ? 185 : 240, 0));
        txtBusquedaGeneral.setPreferredSize(new Dimension(movil ? 220 : 350, 40));
        revalidate();
    }

    private void aplicarTema() {
        setBackground(TemaInsta.FONDO);

        panelMenu.setBackground(TemaInsta.FONDO);
        panelSuperior.setBackground(TemaInsta.FONDO);
        panelContenido.setBackground(TemaInsta.FONDO);

        lblLogo.setForeground(TemaInsta.TEXTO);
        lblUsuario.setForeground(TemaInsta.TEXTO);
        lblSol.setForeground(TemaInsta.TEXTO);
        lblLuna.setForeground(TemaInsta.TEXTO);

        txtBusquedaGeneral.setBackground(TemaInsta.INPUT);
        txtBusquedaGeneral.setForeground(TemaInsta.TEXTO);
        txtBusquedaGeneral.setCaretColor(TemaInsta.TEXTO);

        JButton[] botones = {
            btnInicio,
            btnPerfil,
            btnPublicar,
            btnInteracciones,
            btnBuscar,
            btnHashtag,
            btnInbox,
            btnEditarPerfil,
            btnCerrarSesion
        };

        for (JButton boton : botones) {
            boton.setBackground(TemaInsta.FONDO);
            boton.setForeground(TemaInsta.TEXTO);
            boton.setOpaque(true);
            boton.setContentAreaFilled(true);
            boton.setBorder(new EmptyBorder(10, 12, 10, 12));
        }

        timelinePanel.aplicarTema();
        perfilPanel.aplicarTema();
        publicarPanel.aplicarTema();
        interaccionesPanel.aplicarTema();
        buscarPanel.aplicarTema();
        hashtagPanel.aplicarTema();
        inboxPanel.aplicarTema();
        editarPerfilPanel.aplicarTema();

        // Última pasada: Windows Look & Feel puede conservar fondos claros
        // aunque el texto ya haya cambiado a blanco.
        TemaComponentes.corregirContraste(this);

        revalidate();
        repaint();
    }

    public void mostrarPanel(String panel) {
        cardLayout.show(panelContenido, panel);
    }

    public void setUsuario(String username) {
        usuarioActual = username;

        lblUsuario.setText("@" + username);
        
        inboxPanel.setUsuarioActual(username);
        iniciarNotificaciones();
        cargarPerfil(username);
        timelinePanel.cargarTimeline();
    }

    private void cerrarSesion() {
        int respuesta = DialogosWindows.showConfirmDialog(
                this,
                "¿Desea cerrar sesión de INSTA+?",
                "Cerrar sesión",
                JOptionPane.YES_NO_OPTION
        );

        if (respuesta == JOptionPane.YES_OPTION) {
            cerrarNotificaciones();
            if (accionCerrar != null) {
                accionCerrar.run();
            }
        }
    }


    private void iniciarNotificaciones() {
        cerrarNotificaciones();

        mensajesPendientes = 0;
        mencionesPendientes = 0;

        actualizarContadoresNotificacion();

        hiloNotificaciones = new HiloNotificaciones(
                cliente,
                nuevos -> SwingUtilities.invokeLater(() -> mostrarNotificacionMensajes(nuevos)),
                nuevas -> SwingUtilities.invokeLater(() -> mostrarNotificacionMenciones(nuevas)),
                error -> {
                    if (cliente.tieneSesion()) {
                        System.err.println("Notificaciones INSTA+: " + error.getMessage());
                    }
                }
        );

        hiloNotificaciones.iniciar();
    }

    private void mostrarNotificacionMensajes(ListaEnlazada<Mensaje> nuevos) {
        if (nuevos == null || nuevos.isEmpty()) {
            return;
        }

        mensajesPendientes += nuevos.size();

        actualizarContadoresNotificacion();

        Mensaje ultimo = null;

        for (Mensaje mensaje : nuevos) {
            ultimo = mensaje;
        }

        if (ultimo == null) {
            return;
        }

        lblNotificacion.setText("✉ Nuevo mensaje de @" + ultimo.getEmisor());

        ocultarAvisoLuego();

        mostrarPopupMensaje(ultimo);
    }

    private void mostrarNotificacionMenciones(ListaEnlazada<insta.modelo.Publicacion> nuevas) {
        if (nuevas == null || nuevas.isEmpty()) {
            return;
        }

        mencionesPendientes += nuevas.size();

        actualizarContadoresNotificacion();

        insta.modelo.Publicacion ultima = null;

        for (insta.modelo.Publicacion publicacion : nuevas) {
            ultima = publicacion;
        }

        if (ultima == null) {
            return;
        }

        lblNotificacion.setText("@ Te mencionó @" + ultima.getAutor());

        ocultarAvisoLuego();

        mostrarPopupMencion(ultima);
    }

    private void mostrarPopupMensaje(Mensaje mensaje) {
        String contenido = mensaje.esTexto() ? mensaje.getContenido() : "Te envió un sticker";
        mostrarPopupNotificacion("Nuevo mensaje", mensaje.getEmisor(), contenido, () -> {
            mensajesPendientes = 0;
            actualizarContadoresNotificacion();
            mostrarPanel("INBOX");
            inboxPanel.abrirConversacionDesdeNotificacion(mensaje.getEmisor());
        });
    }

    private void mostrarPopupMencion(insta.modelo.Publicacion publicacion) {
        mostrarPopupPublicacion(publicacion, () -> {
            mencionesPendientes = 0;

            actualizarContadoresNotificacion();

            interaccionesPanel.cargarMenciones();

            mostrarPanel("INTERACCIONES");
        });
    }
    
    private void mostrarPopupPublicacion(insta.modelo.Publicacion publicacion, Runnable accion) {
        ocultarPopupNotificacion();

        if (!isShowing() || publicacion == null) {
            return;
        }

        int ancho = 390;

        JPanel tarjeta = new JPanel(new BorderLayout(10, 10));

        tarjeta.setPreferredSize(new Dimension(ancho, publicacion.tieneAdjunto() ? 330 : 145));

        tarjeta.setBackground(TemaInsta.TARJETA);

        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaInsta.BORDE),
                new EmptyBorder(12, 14, 12, 14)
        ));

        tarjeta.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel encabezado = new JPanel(new BorderLayout(10, 0));
        encabezado.setOpaque(false);

        String inicial = publicacion.getAutor() == null || publicacion.getAutor().isBlank() ? "?" : publicacion.getAutor().substring(0, 1).toUpperCase();

        JLabel avatar = new JLabel(inicial, SwingConstants.CENTER);

        avatar.setPreferredSize(new Dimension(46, 46));
        avatar.setOpaque(true);
        avatar.setBackground(TemaInsta.INPUT);
        avatar.setForeground(TemaInsta.TEXTO);
        avatar.setFont(new Font("Arial", Font.BOLD, 15));
        avatar.setBorder(BorderFactory.createLineBorder(TemaInsta.BORDE));

        JPanel datos = new JPanel();

        datos.setOpaque(false);
        datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel("Nueva mención · @" + publicacion.getAutor());

        lblTitulo.setForeground(TemaInsta.TEXTO);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 13));

        JLabel lblFecha = new JLabel(publicacion.getFechaPublicacion().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        lblFecha.setForeground(TemaInsta.TEXTO_SECUNDARIO);
        lblFecha.setFont(new Font("Arial", Font.PLAIN, 11));

        datos.add(lblTitulo);
        datos.add(Box.createVerticalStrut(3));
        datos.add(lblFecha);

        encabezado.add(avatar, BorderLayout.WEST);
        encabezado.add(datos, BorderLayout.CENTER);

        tarjeta.add(encabezado, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel();

        cuerpo.setOpaque(false);
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));

        if (publicacion.tieneAdjunto()) {
            int anchoImagen;
            int altoImagen;

            if (publicacion.esSticker()) {
                anchoImagen = 130;
                altoImagen = 130;
            } else {
                anchoImagen = 330;
                altoImagen = 180;
            }

            JLabel imagen = new JLabel(publicacion.esSticker() ? "Cargando sticker..." : "Cargando imagen...", SwingConstants.CENTER);

            imagen.setAlignmentX(Component.CENTER_ALIGNMENT);
            imagen.setPreferredSize(new Dimension(anchoImagen, altoImagen));
            imagen.setMaximumSize(new Dimension(anchoImagen, altoImagen));
            imagen.setMinimumSize(new Dimension(anchoImagen, altoImagen));
            imagen.setForeground(TemaInsta.TEXTO_SECUNDARIO);

            cuerpo.add(imagen);

            cargarAdjuntoPopup(publicacion.getRutaAdjunto(), imagen, anchoImagen, altoImagen);

            if (publicacion.getContenido() != null && !publicacion.getContenido().isBlank()) {
                cuerpo.add(Box.createVerticalStrut(6));
            }
        }

        if (publicacion.getContenido() != null && !publicacion.getContenido().isBlank()) {
            JLabel texto = new JLabel("<html><div style='width:340px;'>" + TarjetaPublicacionPanel.escaparHtml(recortar(publicacion.getContenido(), 180)) + "</div></html>");

            texto.setForeground(TemaInsta.TEXTO);
            texto.setFont(new Font("Arial", Font.PLAIN, 12));
            texto.setAlignmentX(Component.LEFT_ALIGNMENT);

            cuerpo.add(texto);
        }

        tarjeta.add(cuerpo, BorderLayout.CENTER);

        agregarAccionClick(tarjeta, accion);

        cargarAvatarPopup(publicacion.getAutor(), avatar);

        try {
            Point ubicacion = getLocationOnScreen();

            int x = ubicacion.x + getWidth() - ancho - 24;
            int y = ubicacion.y + 82;

            popupNotificacion = PopupFactory.getSharedInstance().getPopup(this, tarjeta, x, y);

            popupNotificacion.show();

            timerPopupNotificacion = new Timer(8000, e -> ocultarPopupNotificacion());

            timerPopupNotificacion.setRepeats(false);

            timerPopupNotificacion.start();

        } catch (IllegalComponentStateException ignored) {
        }
    }
    
    private void cargarAdjuntoPopup(String ruta, JLabel destino, int ancho, int alto) {
        if (ruta == null || ruta.isBlank()) {
            return;
        }

        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {

            @Override
            protected ImageIcon doInBackground() throws Exception {
                Respuesta respuesta = cliente.descargarImagen(ruta);

                if (!respuesta.esExitosa() || respuesta.getArchivo() == null) {
                    return null;
                }

                return ImagenUI.ajustar(respuesta.getArchivo(), ancho, alto);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icono = get();

                    if (icono != null) {
                        destino.setText("");
                        destino.setIcon(icono);
                    } else {
                        destino.setText("No se pudo cargar la imagen");
                    }

                } catch (Exception e) {
                    destino.setText("No se pudo cargar la imagen");
                }
            }
        };

        worker.execute();
    }

    private void mostrarPopupNotificacion(String titulo, String username, String detalle, Runnable accion) {
        ocultarPopupNotificacion();
        if (!isShowing()) return;

        JPanel tarjeta = new JPanel(new BorderLayout(12, 0));
        tarjeta.setPreferredSize(new Dimension(340, 86));
        tarjeta.setBackground(TemaInsta.TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaInsta.BORDE),
                new EmptyBorder(12, 14, 12, 14)
        ));
        tarjeta.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel avatar = new JLabel(username == null || username.isBlank() ? "?" : username.substring(0, 1).toUpperCase(), SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(46, 46));
        avatar.setOpaque(true);
        avatar.setBackground(TemaInsta.INPUT);
        avatar.setForeground(TemaInsta.TEXTO);
        avatar.setFont(new Font("Arial", Font.BOLD, 16));
        avatar.setBorder(BorderFactory.createLineBorder(TemaInsta.BORDE));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel lblTitulo = new JLabel(titulo + " · @" + username);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 13));
        lblTitulo.setForeground(TemaInsta.TEXTO);
        JLabel lblDetalle = new JLabel("<html><div style='width:235px;'>" + TarjetaPublicacionPanel.escaparHtml(recortar(detalle, 75)) + "</div></html>");
        lblDetalle.setFont(new Font("Arial", Font.PLAIN, 12));
        lblDetalle.setForeground(TemaInsta.TEXTO_SECUNDARIO);
        textos.add(lblTitulo);
        textos.add(Box.createVerticalStrut(5));
        textos.add(lblDetalle);

        tarjeta.add(avatar, BorderLayout.WEST);
        tarjeta.add(textos, BorderLayout.CENTER);
        agregarAccionClick(tarjeta, accion);
        cargarAvatarPopup(username, avatar);

        try {
            Point ubicacion = getLocationOnScreen();
            int x = ubicacion.x + getWidth() - tarjeta.getPreferredSize().width - 24;
            int y = ubicacion.y + 82;
            popupNotificacion = PopupFactory.getSharedInstance().getPopup(this, tarjeta, x, y);
            popupNotificacion.show();
            timerPopupNotificacion = new Timer(6500, e -> ocultarPopupNotificacion());
            timerPopupNotificacion.setRepeats(false);
            timerPopupNotificacion.start();
        } catch (IllegalComponentStateException ignored) {
        }
    }

    private void cargarAvatarPopup(String username, JLabel avatar) {
        if (username == null || username.isBlank()) return;
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                Respuesta perfil = cliente.perfil(username);
                if (!perfil.esExitosa() || !perfil.tieneUsuario()) return null;
                String ruta = perfil.getUsuario().getRutaFotoPerfil();
                if (ruta == null || ruta.isBlank()) return null;
                Respuesta imagen = cliente.descargarImagen(ruta);
                if (!imagen.esExitosa() || imagen.getArchivo() == null) return null;
                return ImagenUI.ajustar(imagen.getArchivo(), 46, 46);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icono = get();
                    if (icono != null) {
                        avatar.setText("");
                        avatar.setIcon(icono);
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private void agregarAccionClick(Component componente, Runnable accion) {
        java.awt.event.MouseAdapter listener = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                ocultarPopupNotificacion();
                accion.run();
            }
        };
        componente.addMouseListener(listener);
        if (componente instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) agregarAccionClick(hijo, accion);
        }
    }

    private String recortar(String texto, int maximo) {
        String valor = texto == null ? "" : texto.trim().replaceAll("\\s+", " ");
        return valor.length() <= maximo ? valor : valor.substring(0, maximo - 1) + "…";
    }

    private void ocultarPopupNotificacion() {
        if (timerPopupNotificacion != null) {
            timerPopupNotificacion.stop();
            timerPopupNotificacion = null;
        }
        if (popupNotificacion != null) {
            popupNotificacion.hide();
            popupNotificacion = null;
        }
    }

    private void actualizarContadoresNotificacion() {
        if (mensajesPendientes > 0) {
            btnInbox.setText("✉   Mensajes (" + mensajesPendientes + ")");
        } else {
            btnInbox.setText("✉   Mensajes");
        }

        if (mencionesPendientes > 0) {
            btnInteracciones.setText("♡   Notificaciones (" + mencionesPendientes + ")");
        } else {
            btnInteracciones.setText("♡   Notificaciones");
        }

        btnInbox.revalidate();
        btnInbox.repaint();

        btnInteracciones.revalidate();
        btnInteracciones.repaint();
    }

    private void ocultarAvisoLuego() {
        Timer timer = new Timer(5000, e -> lblNotificacion.setText(""));
        timer.setRepeats(false);
        timer.start();
    }

    private void cerrarNotificaciones() {
        ocultarPopupNotificacion();
        if (hiloNotificaciones != null) {
            hiloNotificaciones.close();
            hiloNotificaciones = null;
        }
    }

    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }
}