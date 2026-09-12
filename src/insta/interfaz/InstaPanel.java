
package insta.interfaz;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import red.Cliente;
import red.Respuesta;

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

    private JTextField txtBusquedaGeneral;

    private SwitchTema switchTema;

    private Runnable accionCerrar;
    private Cliente cliente;
    private String usuarioActual;
    
    public InstaPanel(Cliente cliente) {
        this.cliente = cliente;
        TemaInsta.cambiarTema(false);

        setLayout(new BorderLayout());

        crearMenu();
        crearBarraSuperior();
        crearContenido();
        configurarEventos();
        aplicarTema();
    }

    private void crearMenu() {
        panelMenu = new JPanel();
        panelMenu.setLayout(new BoxLayout(panelMenu, BoxLayout.Y_AXIS));
        panelMenu.setPreferredSize(new Dimension(240, 0));
        panelMenu.setBorder(new EmptyBorder(25, 15, 20, 15));
        
        panelMenu.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 0, 1, TemaInsta.BORDE), new EmptyBorder(25, 15, 20, 15)));
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
        boton.setFont(new Font("Arial", Font.PLAIN, 15));
        boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(10, 12, 10, 12));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setOpaque(true);

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
        panelSuperior.setBorder(new EmptyBorder(12, 25, 12, 25));
        panelSuperior.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TemaInsta.BORDE), new EmptyBorder(12, 25, 12, 25)));
        txtBusquedaGeneral = new JTextField();
        txtBusquedaGeneral.setFont(new Font("Arial", Font.PLAIN, 14));
        txtBusquedaGeneral.setBorder(new EmptyBorder(10, 15, 10, 15));
        txtBusquedaGeneral.setPreferredSize(new Dimension(350, 40));

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        derecha.setOpaque(false);

        JLabel lblSol = new JLabel("☀");
        lblSol.setFont(new Font("Arial", Font.PLAIN, 20));

        switchTema = new SwitchTema();

        JLabel lblLuna = new JLabel("☾");
        lblLuna.setFont(new Font("Arial", Font.PLAIN, 20));

        lblUsuario = new JLabel("@usuario");
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 14));

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
        perfilPanel = new PerfilPanel();
        publicarPanel = new PublicarPanel(cliente);
        interaccionesPanel = new InteraccionesPanel();
        buscarPanel = new BuscarPanel(cliente);
        hashtagPanel = new HashtagPanel();
        inboxPanel = new InboxPanel();
        editarPerfilPanel = new EditarPerfilPanel();
        
        publicarPanel.setAccionPublicacionCreada(() -> {
            timelinePanel.cargarTimeline();
            cargarPerfil(usuarioActual);
            mostrarPanel("TIMELINE");
        });
        
        buscarPanel.setAccionSeguimientoActualizado(() -> {
            timelinePanel.cargarTimeline();
            cargarPerfil(usuarioActual);
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
        btnPerfil.addActionListener(e -> mostrarPanel("PERFIL"));
        btnPublicar.addActionListener(e -> mostrarPanel("PUBLICAR"));
        btnInteracciones.addActionListener(e -> mostrarPanel("INTERACCIONES"));
        btnBuscar.addActionListener(e -> mostrarPanel("BUSCAR"));
        btnHashtag.addActionListener(e -> mostrarPanel("HASHTAG"));
        btnInbox.addActionListener(e -> mostrarPanel("INBOX"));
        btnEditarPerfil.addActionListener(e -> mostrarPanel("EDITAR"));

        btnCerrarSesion.addActionListener(e -> cerrarSesion());

        switchTema.addPropertyChangeListener("activado", e -> {
            boolean oscuro = switchTema.isActivado();
            TemaInsta.cambiarTema(oscuro);
            aplicarTema();
        });
    }

    private void aplicarTema() {
        setBackground(TemaInsta.FONDO);

        panelMenu.setBackground(TemaInsta.FONDO);
        panelSuperior.setBackground(TemaInsta.FONDO);
        panelContenido.setBackground(TemaInsta.FONDO);

        lblLogo.setForeground(TemaInsta.TEXTO);
        lblUsuario.setForeground(TemaInsta.TEXTO);

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
        }

        timelinePanel.aplicarTema();
        perfilPanel.aplicarTema();
        publicarPanel.aplicarTema();
        interaccionesPanel.aplicarTema();
        buscarPanel.aplicarTema();
        hashtagPanel.aplicarTema();
        inboxPanel.aplicarTema();
        editarPerfilPanel.aplicarTema();

        revalidate();
        repaint();
    }

    private void actualizarTemaComponentes(Container contenedor) {
        for (Component componente : contenedor.getComponents()) {

            if (componente instanceof JPanel panel) {
                panel.setBackground(TemaInsta.FONDO);
            }

            if (componente instanceof JLabel label) {
                label.setForeground(TemaInsta.TEXTO);
            }

            if (componente instanceof JTextField campo) {
                campo.setBackground(TemaInsta.INPUT);
                campo.setForeground(TemaInsta.TEXTO);
                campo.setCaretColor(TemaInsta.TEXTO);
            }

            if (componente instanceof JTextArea area) {
                area.setBackground(TemaInsta.INPUT);
                area.setForeground(TemaInsta.TEXTO);
                area.setCaretColor(TemaInsta.TEXTO);
            }

            if (componente instanceof JButton boton) {
                boton.setBackground(TemaInsta.FONDO_SECUNDARIO);
                boton.setForeground(TemaInsta.TEXTO);
            }

            if (componente instanceof JScrollPane scroll) {
                scroll.getViewport().setBackground(TemaInsta.FONDO);
            }

            if (componente instanceof Container interno) {
                actualizarTemaComponentes(interno);
            }
        }
    }

    public void mostrarPanel(String panel) {
        cardLayout.show(panelContenido, panel);
    }

    public void setUsuario(String username) {
        usuarioActual = username;
        lblUsuario.setText("@" + username);

        cargarPerfil(username);
        timelinePanel.cargarTimeline();
    }

    private void cerrarSesion() {
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Desea cerrar sesión de INSTA+?", "Cerrar sesión", JOptionPane.YES_NO_OPTION);

        if (respuesta == JOptionPane.YES_OPTION) {
            if (accionCerrar != null) {
                accionCerrar.run();
            }
        }
    }

    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }
    
    private void cargarPerfil(String username) {
        usuarioPerfilActual = username;

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

                        boolean esMiPerfil = usuario.getUsername().equalsIgnoreCase(usuarioActual);

                        perfilPanel.configurarComoPerfilPropio(esMiPerfil);

                    } else {
                        JOptionPane.showMessageDialog(
                                InstaPanel.this,
                                respuesta.getMensaje(),
                                "Perfil",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
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

}