
package insta.interfaz;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import estructuras.ListaEnlazada;
import insta.modelo.Publicacion;
import java.time.format.DateTimeFormatter;
import red.Cliente;
import red.Respuesta;

public class TimelinePanel extends JPanel implements Tematizable {

    private JPanel panelHistorias;
    private JPanel panelPublicaciones;
    private Cliente cliente;

    public TimelinePanel(Cliente cliente) {
        this.cliente = cliente;
        setLayout(new BorderLayout());

        crearHistorias();
        crearFeed();

        aplicarTema();
    }

    private void crearHistorias() {
        panelHistorias = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 10));
        panelHistorias.setBorder(new EmptyBorder(10, 30, 10, 30));

        agregarHistoria("Tu historia");
        agregarHistoria("maria");
        agregarHistoria("carlos");
        agregarHistoria("ana");
        agregarHistoria("david");

        JScrollPane scroll = new JScrollPane(panelHistorias);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(0, 110));

        add(scroll, BorderLayout.NORTH);
    }

    private void agregarHistoria(String username) {
        JPanel historia = new JPanel();
        historia.setLayout(new BoxLayout(historia, BoxLayout.Y_AXIS));
        historia.setOpaque(false);

        JLabel foto = new JLabel();
        foto.setPreferredSize(new Dimension(65, 65));
        foto.setMaximumSize(new Dimension(65, 65));
        foto.setMinimumSize(new Dimension(65, 65));
        foto.setOpaque(true);
        foto.setBackground(TemaInsta.INPUT);
        foto.setBorder(BorderFactory.createLineBorder(new Color(255, 70, 130), 3));
        foto.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nombre = new JLabel(username);
        nombre.setAlignmentX(Component.CENTER_ALIGNMENT);

        historia.add(foto);
        historia.add(Box.createVerticalStrut(4));
        historia.add(nombre);

        panelHistorias.add(historia);
    }
    
    private void crearFeed() {
        panelPublicaciones = new JPanel();
        panelPublicaciones.setLayout(new BoxLayout(panelPublicaciones, BoxLayout.Y_AXIS));
        panelPublicaciones.setBorder(new EmptyBorder(20, 100, 20, 100));

        JScrollPane scroll = new JScrollPane(panelPublicaciones);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        add(scroll, BorderLayout.CENTER);
    }

    private void agregarPublicacionEjemplo(String usuario, String descripcion, String likes) {
        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setMaximumSize(new Dimension(620, 700));
        tarjeta.setBorder(BorderFactory.createLineBorder(TemaInsta.BORDE));

        JPanel encabezado = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));

        JLabel fotoPerfil = new JLabel();
        fotoPerfil.setPreferredSize(new Dimension(40, 40));
        fotoPerfil.setOpaque(true);
        fotoPerfil.setBackground(TemaInsta.INPUT);

        JLabel lblUsuario = new JLabel(usuario);
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 14));

        encabezado.add(fotoPerfil);
        encabezado.add(lblUsuario);

        JLabel imagen = new JLabel("IMAGEN DE PUBLICACIÓN", SwingConstants.CENTER);
        imagen.setPreferredSize(new Dimension(620, 450));
        imagen.setOpaque(true);
        imagen.setBackground(TemaInsta.INPUT);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));

        JButton btnLike = crearBotonIcono("♡");
        JButton btnComentar = crearBotonIcono("○");
        JButton btnEnviar = crearBotonIcono("➤");
        JButton btnGuardar = crearBotonIcono("▱");

        acciones.add(btnLike);
        acciones.add(btnComentar);
        acciones.add(btnEnviar);
        acciones.add(Box.createHorizontalStrut(400));
        acciones.add(btnGuardar);

        JPanel informacion = new JPanel();
        informacion.setLayout(new BoxLayout(informacion, BoxLayout.Y_AXIS));
        informacion.setBorder(new EmptyBorder(0, 15, 15, 15));

        JLabel lblLikes = new JLabel(likes + " Me gusta");
        lblLikes.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel lblDescripcion = new JLabel("<html><b>" + usuario + "</b> " + descripcion + "</html>");

        informacion.add(lblLikes);
        informacion.add(Box.createVerticalStrut(6));
        informacion.add(lblDescripcion);

        JPanel inferior = new JPanel();
        inferior.setLayout(new BoxLayout(inferior, BoxLayout.Y_AXIS));

        inferior.add(acciones);
        inferior.add(informacion);

        tarjeta.add(encabezado, BorderLayout.NORTH);
        tarjeta.add(imagen, BorderLayout.CENTER);
        tarjeta.add(inferior, BorderLayout.SOUTH);

        panelPublicaciones.add(tarjeta);
        panelPublicaciones.add(Box.createVerticalStrut(25));
    }

    private JButton crearBotonIcono(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Arial", Font.PLAIN, 25));
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setContentAreaFilled(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return boton;
    }

    @Override
    public void aplicarTema() {
        aplicarTemaRecursivo(this);

        setBackground(TemaInsta.FONDO);
        panelHistorias.setBackground(TemaInsta.FONDO);
        panelPublicaciones.setBackground(TemaInsta.FONDO_SECUNDARIO);

        revalidate();
        repaint();
    }

    private void aplicarTemaRecursivo(Container contenedor) {
        for (Component componente : contenedor.getComponents()) {
            if (componente instanceof JPanel panel && panel.isOpaque()) {
                panel.setBackground(TemaInsta.FONDO);
            }

            if (componente instanceof JLabel label) {
                label.setForeground(TemaInsta.TEXTO);
            }

            if (componente instanceof JButton boton) {
                boton.setForeground(TemaInsta.TEXTO);
            }

            if (componente instanceof JScrollPane scroll) {
                scroll.getViewport().setBackground(TemaInsta.FONDO);
            }

            if (componente instanceof Container interno) {
                aplicarTemaRecursivo(interno);
            }
        }
    }
    
    public void cargarTimeline() {
        panelPublicaciones.removeAll();

        JLabel cargando = new JLabel("Cargando publicaciones...");
        cargando.setAlignmentX(Component.CENTER_ALIGNMENT);
        cargando.setForeground(TemaInsta.TEXTO);

        panelPublicaciones.add(cargando);
        panelPublicaciones.revalidate();
        panelPublicaciones.repaint();

        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {

            @Override
            protected Respuesta doInBackground() throws Exception {
                return cliente.timeline(0);
            }

            @Override
            protected void done() {
                try {
                    Respuesta respuesta = get();

                    panelPublicaciones.removeAll();

                    if (respuesta.esExitosa()) {
                        ListaEnlazada<Publicacion> publicaciones = respuesta.getPublicaciones();

                        if (publicaciones.isEmpty()) {
                            mostrarTimelineVacio();
                        } else {
                            for (Publicacion publicacion : publicaciones) {
                                agregarPublicacion(publicacion);
                            }
                        }
                    } else {
                        mostrarError(respuesta.getMensaje());
                    }

                    aplicarTema();

                    panelPublicaciones.revalidate();
                    panelPublicaciones.repaint();

                } catch (Exception e) {
                    panelPublicaciones.removeAll();
                    mostrarError("No se pudo cargar el timeline: " + e.getMessage());

                    panelPublicaciones.revalidate();
                    panelPublicaciones.repaint();
                }
            }
        };

        trabajador.execute();
    }
    
    private void agregarPublicacion(Publicacion publicacion) {
        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setMaximumSize(new Dimension(620, 650));
        tarjeta.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.setBorder(BorderFactory.createLineBorder(TemaInsta.BORDE));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel lblUsuario = new JLabel("@" + publicacion.getAutor());
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 14));

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        JLabel lblFecha = new JLabel(publicacion.getFechaPublicacion().format(formato));
        lblFecha.setFont(new Font("Arial", Font.PLAIN, 11));

        encabezado.add(lblUsuario, BorderLayout.WEST);
        encabezado.add(lblFecha, BorderLayout.EAST);

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(new EmptyBorder(15, 15, 15, 15));

        if (publicacion.esTexto()) {
            JLabel lblTexto = new JLabel("<html><div style='width:540px;'>" + publicacion.getContenido() + "</div></html>");
            lblTexto.setFont(new Font("Arial", Font.PLAIN, 16));
            lblTexto.setAlignmentX(Component.LEFT_ALIGNMENT);

            contenido.add(lblTexto);
        } else if (publicacion.esImagen()) {
            JLabel lblImagen = new JLabel("Cargando imagen...", SwingConstants.CENTER);
            lblImagen.setPreferredSize(new Dimension(550, 350));
            lblImagen.setMaximumSize(new Dimension(550, 350));
            lblImagen.setOpaque(true);
            lblImagen.setBackground(TemaInsta.INPUT);
            lblImagen.setAlignmentX(Component.CENTER_ALIGNMENT);

            contenido.add(lblImagen);

            if (!publicacion.getContenido().isBlank()) {
                contenido.add(Box.createVerticalStrut(10));

                JLabel lblDescripcion = new JLabel("<html><div style='width:540px;'>" + publicacion.getContenido() + "</div></html>");
                lblDescripcion.setAlignmentX(Component.LEFT_ALIGNMENT);

                contenido.add(lblDescripcion);
            }

            cargarImagenPublicacion(publicacion, lblImagen);
        } else if (publicacion.esSticker()) {
            JLabel lblSticker = new JLabel("Sticker", SwingConstants.CENTER);
            lblSticker.setPreferredSize(new Dimension(250, 250));
            lblSticker.setMaximumSize(new Dimension(250, 250));
            lblSticker.setOpaque(true);
            lblSticker.setBackground(TemaInsta.INPUT);
            lblSticker.setAlignmentX(Component.CENTER_ALIGNMENT);

            contenido.add(lblSticker);

            cargarImagenPublicacion(publicacion, lblSticker);

            if (!publicacion.getContenido().isBlank()) {
                contenido.add(Box.createVerticalStrut(10));

                JLabel lblTexto = new JLabel(publicacion.getContenido());
                lblTexto.setAlignmentX(Component.LEFT_ALIGNMENT);

                contenido.add(lblTexto);
            }
        }

        tarjeta.add(encabezado, BorderLayout.NORTH);
        tarjeta.add(contenido, BorderLayout.CENTER);

        panelPublicaciones.add(tarjeta);
        panelPublicaciones.add(Box.createVerticalStrut(25));
    }
    
    private void cargarImagenPublicacion(Publicacion publicacion, JLabel label) {
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
                        byte[] datos = respuesta.getArchivo();

                        ImageIcon iconoOriginal = new ImageIcon(datos);

                        int ancho = label.getPreferredSize().width;
                        int alto = label.getPreferredSize().height;

                        Image imagen = iconoOriginal.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);

                        label.setText("");
                        label.setIcon(new ImageIcon(imagen));
                    } else {
                        label.setText("No se pudo cargar la imagen");
                    }

                } catch (Exception e) {
                    label.setText("Error al cargar imagen");
                }
            }
        };

        trabajador.execute();
    }
    
    private void mostrarTimelineVacio() {
        JLabel mensaje = new JLabel("No hay publicaciones todavía.");
        mensaje.setFont(new Font("Arial", Font.PLAIN, 16));
        mensaje.setForeground(TemaInsta.TEXTO);
        mensaje.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelPublicaciones.add(Box.createVerticalStrut(50));
        panelPublicaciones.add(mensaje);
    }
    
    private void mostrarError(String texto) {
        JLabel mensaje = new JLabel(texto);
        mensaje.setForeground(TemaInsta.TEXTO);
        mensaje.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelPublicaciones.add(mensaje);
    }

    public JPanel getPanelPublicaciones() {
        return panelPublicaciones;
    }
}