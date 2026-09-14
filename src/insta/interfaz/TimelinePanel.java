
package insta.interfaz;

import estructuras.ListaEnlazada;
import insta.modelo.Publicacion;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import red.Cliente;
import red.Respuesta;

public class TimelinePanel extends JPanel implements Tematizable {

    private JPanel panelHistorias;
    private JPanel panelPublicaciones;
    private final Cliente cliente;

    public TimelinePanel(Cliente cliente) {
        this.cliente = cliente;
        setLayout(new BorderLayout());

        crearHistorias();
        crearFeed();

        aplicarTema();
        cargarHistorias();
    }

    private void crearHistorias() {
        panelHistorias = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 10));
        panelHistorias.setBorder(new EmptyBorder(10, 30, 10, 30));

        JScrollPane scroll = new JScrollPane(panelHistorias);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(0, 110));

        add(scroll, BorderLayout.NORTH);
    }

    private void cargarHistorias() {
        panelHistorias.removeAll();

        JLabel cargando = new JLabel("Cargando historias...");
        cargando.setForeground(TemaInsta.TEXTO);

        panelHistorias.add(cargando);
        panelHistorias.revalidate();
        panelHistorias.repaint();

        Respuesta.DatosUsuario usuarioActual = cliente.getUsuarioActual();

        if (usuarioActual == null) {
            panelHistorias.removeAll();
            agregarHistoria("Tu historia", "");
            panelHistorias.revalidate();
            panelHistorias.repaint();
            return;
        }

        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {

            @Override
            protected Respuesta doInBackground() throws Exception {
                return cliente.seguidos(usuarioActual.getUsername(), 0);
            }

            @Override
            protected void done() {
                panelHistorias.removeAll();

                agregarHistoria("Tu historia", usuarioActual.getRutaFotoPerfil());

                try {
                    Respuesta respuesta = get();

                    if (respuesta.esExitosa()) {
                        ListaEnlazada<Respuesta.DatosUsuario> seguidos = respuesta.getUsuarios();

                        for (Respuesta.DatosUsuario usuario : seguidos) {
                            if (usuario.estaActiva()) {
                                agregarHistoria(usuario.getUsername(), usuario.getRutaFotoPerfil());
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("No se pudieron cargar las historias: " + e.getMessage());
                }

                panelHistorias.revalidate();
                panelHistorias.repaint();
            }
        };

        trabajador.execute();
    }

    private void agregarHistoria(String username, String rutaFotoPerfil) {
        JPanel historia = new JPanel();
        historia.setLayout(new BoxLayout(historia, BoxLayout.Y_AXIS));
        historia.setOpaque(false);

        JLabel foto = new JLabel("👤", SwingConstants.CENTER);
        foto.setPreferredSize(new Dimension(65, 65));
        foto.setMaximumSize(new Dimension(65, 65));
        foto.setMinimumSize(new Dimension(65, 65));
        foto.setOpaque(true);
        foto.setBackground(TemaInsta.INPUT);
        foto.setBorder(BorderFactory.createLineBorder(new Color(255, 70, 130), 3));
        foto.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nombre = new JLabel(username);
        nombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        nombre.setForeground(TemaInsta.TEXTO);

        historia.add(foto);
        historia.add(Box.createVerticalStrut(4));
        historia.add(nombre);

        panelHistorias.add(historia);

        if (rutaFotoPerfil != null && !rutaFotoPerfil.isBlank()) {
            cargarImagen(rutaFotoPerfil, foto, 59, 59);
        }
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

    public void cargarTimeline() {
        panelPublicaciones.removeAll();

        JLabel cargando = new JLabel("Cargando publicaciones...");
        cargando.setAlignmentX(Component.CENTER_ALIGNMENT);
        cargando.setForeground(TemaInsta.TEXTO);

        panelPublicaciones.add(cargando);
        panelPublicaciones.revalidate();
        panelPublicaciones.repaint();

        SwingWorker<ListaEnlazada<Publicacion>, Void> trabajador = new SwingWorker<>() {

            @Override
            protected ListaEnlazada<Publicacion> doInBackground() throws Exception {
                return PaginadorInsta.publicaciones(desde -> cliente.timeline(desde));
            }

            @Override
            protected void done() {
                try {
                    ListaEnlazada<Publicacion> publicaciones = get();
                    panelPublicaciones.removeAll();

                    if (publicaciones.isEmpty()) {
                        mostrarTimelineVacio();
                    } else {
                        for (Publicacion publicacion : publicaciones) {
                            agregarPublicacion(publicacion);
                        }
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
        TarjetaPublicacionPanel tarjeta = new TarjetaPublicacionPanel(cliente, publicacion, this::cargarTimeline);
        panelPublicaciones.add(tarjeta);
        panelPublicaciones.add(Box.createVerticalStrut(25));
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
                        label.setText("👤");
                    }
                } catch (Exception e) {
                    label.setText("👤");
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
            if (componente instanceof TarjetaPublicacionPanel tarjeta) {
                tarjeta.aplicarTema();
            } else if (componente instanceof JPanel panel && panel.isOpaque()) {
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

    public JPanel getPanelPublicaciones() {
        return panelPublicaciones;
    }
}