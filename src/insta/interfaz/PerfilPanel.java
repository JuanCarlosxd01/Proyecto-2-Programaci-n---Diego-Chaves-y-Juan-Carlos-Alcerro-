
package insta.interfaz;

import estructuras.ListaEnlazada;
import insta.modelo.Publicacion;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import red.Cliente;
import red.Respuesta;

public class PerfilPanel extends JPanel implements Tematizable {

    private JPanel panelSuperior;
    private JPanel panelDatos;
    private JPanel panelEstadisticas;
    private JPanel panelGridPublicaciones;
    private JPanel panelCentroPublicaciones;
    private CardLayout layoutPublicaciones;
    private JPanel panelDetallePublicacion;
    private JPanel panelContenedorPublicaciones;
    private JScrollPane scrollPublicaciones;
    private JLabel lblTituloPublicaciones;

    private JLabel lblFoto;
    private JLabel lblNombre;
    private JLabel lblBiografia;
    private JLabel lblUsername;
    private JLabel lblEdad;
    private JLabel lblGenero;
    private JLabel lblFecha;
    private JLabel lblEstado;

    private JLabel lblSeguidores;
    private JLabel lblSeguidos;
    private JLabel lblPublicaciones;

    private JButton btnSeguir;

    private Cliente cliente;

    public PerfilPanel(Cliente cliente) {
        this.cliente = cliente;

        setLayout(new BorderLayout());

        crearPerfil();
        crearPublicaciones();

        aplicarTema();
    }

    private void crearPerfil() {
        panelSuperior = new JPanel(new BorderLayout(30, 10));
        panelSuperior.setBorder(new EmptyBorder(30, 50, 25, 50));

        lblFoto = new JLabel("SIN FOTO", SwingConstants.CENTER);
        lblFoto.setPreferredSize(new Dimension(150, 150));
        lblFoto.setOpaque(true);

        panelDatos = new JPanel();
        panelDatos.setLayout(new BoxLayout(panelDatos, BoxLayout.Y_AXIS));

        JPanel encabezado = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        encabezado.setOpaque(false);

        lblUsername = new JLabel("@usuario");
        lblUsername.setFont(new Font("Arial", Font.BOLD, 23));

        btnSeguir = new JButton("Seguir");

        encabezado.add(lblUsername);
        encabezado.add(btnSeguir);

        lblNombre = new JLabel("Nombre completo");
        lblNombre.setFont(new Font("Arial", Font.BOLD, 16));
        lblBiografia = new JLabel("");

        lblEdad = new JLabel("Edad: --");
        lblGenero = new JLabel("Género: --");
        lblFecha = new JLabel("Fecha de registro: --");
        lblEstado = new JLabel("Estado: Activa");

        panelEstadisticas = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 10));

        lblPublicaciones = new JLabel("0 publicaciones");
        lblSeguidores = new JLabel("0 followers");
        lblSeguidos = new JLabel("0 following");

        panelEstadisticas.add(lblPublicaciones);
        panelEstadisticas.add(lblSeguidores);
        panelEstadisticas.add(lblSeguidos);

        panelDatos.add(encabezado);
        panelDatos.add(Box.createVerticalStrut(12));
        panelDatos.add(panelEstadisticas);
        panelDatos.add(Box.createVerticalStrut(10));
        panelDatos.add(lblNombre);
        panelDatos.add(Box.createVerticalStrut(5));
        panelDatos.add(lblBiografia);
        panelDatos.add(Box.createVerticalStrut(5));
        panelDatos.add(lblEdad);
        panelDatos.add(Box.createVerticalStrut(5));
        panelDatos.add(lblGenero);
        panelDatos.add(Box.createVerticalStrut(5));
        panelDatos.add(lblFecha);
        panelDatos.add(Box.createVerticalStrut(5));
        panelDatos.add(lblEstado);

        panelSuperior.add(lblFoto, BorderLayout.WEST);
        panelSuperior.add(panelDatos, BorderLayout.CENTER);

        add(panelSuperior, BorderLayout.NORTH);
    }

    private void crearPublicaciones() {
        layoutPublicaciones = new CardLayout();
        panelCentroPublicaciones = new JPanel(layoutPublicaciones);

        panelContenedorPublicaciones = new JPanel(new BorderLayout());
        panelContenedorPublicaciones.setBorder(new EmptyBorder(10, 40, 20, 40));

        lblTituloPublicaciones = new JLabel("PUBLICACIONES", SwingConstants.CENTER);
        lblTituloPublicaciones.setFont(new Font("Arial", Font.BOLD, 14));
        lblTituloPublicaciones.setBorder(new EmptyBorder(0, 0, 10, 0));

        panelGridPublicaciones = new JPanel(new GridLayout(0, 3, 10, 10));
        panelContenedorPublicaciones.add(lblTituloPublicaciones, BorderLayout.NORTH);
        panelContenedorPublicaciones.add(panelGridPublicaciones, BorderLayout.CENTER);

        scrollPublicaciones = new JScrollPane(panelContenedorPublicaciones);
        scrollPublicaciones.setBorder(null);
        scrollPublicaciones.getVerticalScrollBar().setUnitIncrement(18);
        panelCentroPublicaciones.add(scrollPublicaciones, "GRID");
        panelDetallePublicacion = new JPanel(new BorderLayout());
        panelCentroPublicaciones.add(panelDetallePublicacion, "DETALLE");
        add(panelCentroPublicaciones, BorderLayout.CENTER);
    }

    public void cargarPublicaciones(String username) {
        if (layoutPublicaciones != null) layoutPublicaciones.show(panelCentroPublicaciones, "GRID");
        panelGridPublicaciones.removeAll();

        JLabel cargando = new JLabel("Cargando publicaciones...", SwingConstants.CENTER);
        cargando.setForeground(TemaInsta.TEXTO);

        panelGridPublicaciones.add(cargando);

        panelGridPublicaciones.revalidate();
        panelGridPublicaciones.repaint();

        SwingWorker<ListaEnlazada<Publicacion>, Void> trabajador = new SwingWorker<>() {

            @Override
            protected ListaEnlazada<Publicacion> doInBackground() throws Exception {
                return PaginadorInsta.publicaciones(desde -> cliente.publicaciones(username, desde));
            }

            @Override
            protected void done() {
                try {
                    ListaEnlazada<Publicacion> publicaciones = get();
                    panelGridPublicaciones.removeAll();

                    if (publicaciones.isEmpty()) {
                        mostrarSinPublicaciones();
                    } else {
                        for (Publicacion publicacion : publicaciones) {
                            agregarPublicacion(publicacion);
                        }
                    }

                } catch (Exception e) {
                    panelGridPublicaciones.removeAll();

                    mostrarError("No se pudieron cargar las publicaciones.");
                }

                panelGridPublicaciones.revalidate();
                panelGridPublicaciones.repaint();
            }
        };

        trabajador.execute();
    }

    private void agregarPublicacion(Publicacion publicacion) {
        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setOpaque(true);
        tarjeta.setBackground(TemaInsta.TARJETA);
        tarjeta.setPreferredSize(new Dimension(220, 220));
        tarjeta.setBorder(BorderFactory.createLineBorder(TemaInsta.BORDE));

        if (publicacion.esTexto()) {
            JLabel texto = new JLabel(
                    "<html><div style='width:180px;text-align:center;'>"
                    + TarjetaPublicacionPanel.escaparHtml(publicacion.getContenido())
                    + "</div></html>",
                    SwingConstants.CENTER
            );

            texto.setFont(new Font("Arial", Font.PLAIN, 14));
            texto.setBorder(new EmptyBorder(15, 15, 15, 15));
            texto.setForeground(TemaInsta.TEXTO);

            tarjeta.add(texto, BorderLayout.CENTER);

        } else if (publicacion.esImagen()) {
            JLabel imagen = new JLabel("Cargando imagen...", SwingConstants.CENTER);

            imagen.setPreferredSize(new Dimension(210, 180));
            imagen.setOpaque(true);
            imagen.setBackground(TemaInsta.INPUT);

            tarjeta.add(imagen, BorderLayout.CENTER);

            cargarImagenPublicacion(publicacion, imagen);

            if (!publicacion.getContenido().isBlank()) {
                JLabel descripcion = new JLabel(
                        "<html><div style='width:190px;'>"
                        + TarjetaPublicacionPanel.escaparHtml(publicacion.getContenido())
                        + "</div></html>"
                );

                descripcion.setBorder(new EmptyBorder(5, 8, 5, 8));
                descripcion.setForeground(TemaInsta.TEXTO);

                tarjeta.add(descripcion, BorderLayout.SOUTH);
            }

        } else if (publicacion.esSticker()) {
            JLabel sticker = new JLabel("Cargando sticker...", SwingConstants.CENTER);

            sticker.setPreferredSize(new Dimension(200, 200));
            sticker.setOpaque(true);
            sticker.setBackground(TemaInsta.INPUT);

            tarjeta.add(sticker, BorderLayout.CENTER);

            cargarImagenPublicacion(publicacion, sticker);
        }

        tarjeta.setToolTipText("Haz clic para abrir la publicación");
        tarjeta.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) {
                    mostrarPublicacionCompleta(publicacion);
                }
            }
        });

        panelGridPublicaciones.add(tarjeta);
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

                        int ancho = label.getPreferredSize().width;
                        int alto = label.getPreferredSize().height;
                        ImageIcon ajustada = ImagenUI.ajustar(datos, ancho, alto);
                        label.setText("");
                        label.setIcon(ajustada);

                    } else {
                        label.setText("Imagen no disponible");
                    }

                } catch (Exception e) {
                    label.setText("Error al cargar imagen");
                }
            }
        };

        trabajador.execute();
    }

    private void mostrarPublicacionCompleta(Publicacion publicacion) {
        JPanel vista = new JPanel(new BorderLayout(0, 10));
        vista.setBorder(new EmptyBorder(12, 18, 18, 18));
        vista.setBackground(TemaInsta.FONDO);

        JButton volver = new JButton("← Volver a publicaciones");
        volver.setFocusPainted(false);
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        barra.setOpaque(false);
        barra.add(volver);
        vista.add(barra, BorderLayout.NORTH);

        TarjetaPublicacionPanel tarjeta = new TarjetaPublicacionPanel(cliente, publicacion, () -> {
            cargarPublicaciones(publicacion.getAutor());
            layoutPublicaciones.show(panelCentroPublicaciones, "GRID");
        });
        JPanel centro = new JPanel(new BorderLayout());
        centro.setBorder(new EmptyBorder(5, 40, 20, 40));
        centro.setBackground(TemaInsta.FONDO);
        centro.add(tarjeta, BorderLayout.NORTH);
        JScrollPane scrollDetalle = new JScrollPane(centro);
        scrollDetalle.setBorder(null);
        scrollDetalle.getVerticalScrollBar().setUnitIncrement(18);
        vista.add(scrollDetalle, BorderLayout.CENTER);

        volver.addActionListener(e -> layoutPublicaciones.show(panelCentroPublicaciones, "GRID"));
        panelDetallePublicacion.removeAll();
        panelDetallePublicacion.add(vista, BorderLayout.CENTER);
        layoutPublicaciones.show(panelCentroPublicaciones, "DETALLE");
        panelDetallePublicacion.revalidate();
        panelDetallePublicacion.repaint();
    }

    private void cargarFotoPerfil(String ruta) {
        lblFoto.setIcon(null);
        if (ruta == null || ruta.isBlank()) {
            lblFoto.setText("SIN FOTO");
            return;
        }

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
                        ImageIcon ajustada = ImagenUI.ajustar(respuesta.getArchivo(), 150, 150);
                        lblFoto.setText("");
                        lblFoto.setIcon(ajustada);
                    } else {
                        lblFoto.setText("SIN FOTO");
                    }
                } catch (Exception e) {
                    lblFoto.setText("SIN FOTO");
                }
            }
        };

        trabajador.execute();
    }

    private void mostrarSinPublicaciones() {
        JLabel mensaje = new JLabel("Este usuario todavía no tiene publicaciones.", SwingConstants.CENTER);

        mensaje.setForeground(TemaInsta.TEXTO);

        panelGridPublicaciones.add(mensaje);
    }

    private void mostrarError(String mensaje) {
        JLabel error = new JLabel(mensaje, SwingConstants.CENTER);

        error.setForeground(TemaInsta.TEXTO);

        panelGridPublicaciones.add(error);
    }

    public void mostrarCargando() {
        lblUsername.setText("@...");
        lblNombre.setText("Cargando perfil...");
    }

    public void mostrarUsuario(Respuesta.DatosUsuario usuario) {
        if (usuario == null) {
            return;
        }

        lblUsername.setText("@" + usuario.getUsername());
        lblNombre.setText(usuario.getNombreCompleto());
        lblBiografia.setText("<html>" + TarjetaPublicacionPanel.escaparHtml(usuario.getBiografia()) + "</html>");
        cargarFotoPerfil(usuario.getRutaFotoPerfil());

        lblEdad.setText("Edad: " + usuario.getEdad());
        lblGenero.setText("Género: " + usuario.getGenero());

        if (usuario.getFechaRegistro() != null) {
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            lblFecha.setText("Fecha de registro: " + usuario.getFechaRegistro().format(formato));
        } else {
            lblFecha.setText("Fecha de registro: --");
        }

        if (usuario.estaActiva()) {
            lblEstado.setText("Estado: Activa");
        } else {
            lblEstado.setText("Estado: Desactivada");
        }

        lblPublicaciones.setText(usuario.getPublicaciones() + " publicaciones");
        lblSeguidores.setText(usuario.getFollowers() + " followers");
        lblSeguidos.setText(usuario.getFollowing() + " following");

        if (usuario.loSigo()) {
            btnSeguir.setText("Dejar de seguir");
        } else {
            btnSeguir.setText("Seguir");
        }

        revalidate();
        repaint();
    }

    public void configurarComoPerfilPropio(boolean propio) {
        btnSeguir.setVisible(!propio);

        revalidate();
        repaint();
    }

    @Override
    public void aplicarTema() {
        setBackground(TemaInsta.FONDO);

        panelSuperior.setBackground(TemaInsta.FONDO);
        panelDatos.setBackground(TemaInsta.FONDO);
        panelEstadisticas.setBackground(TemaInsta.FONDO);
        panelGridPublicaciones.setBackground(TemaInsta.FONDO);

        lblFoto.setBackground(TemaInsta.INPUT);
        lblFoto.setForeground(TemaInsta.TEXTO_SECUNDARIO);

        btnSeguir.setBackground(TemaInsta.BOTON);
        btnSeguir.setForeground(TemaInsta.BOTON_TEXTO);

        cambiarTexto(this);
        aplicarTemaTarjetas(panelGridPublicaciones);
        aplicarTemaTarjetas(panelDetallePublicacion);
        TemaComponentes.corregirContraste(this);

        revalidate();
        repaint();
    }

    private void aplicarTemaTarjetas(Container contenedor) {
        if (contenedor == null) {
            return;
        }

        for (Component componente : contenedor.getComponents()) {
            if (componente instanceof TarjetaPublicacionPanel tarjetaPublicacion) {
                tarjetaPublicacion.aplicarTema();
                continue;
            }

            if (componente instanceof JPanel panel && panel.isOpaque()) {
                panel.setBackground(TemaInsta.TARJETA);
            }

            if (componente instanceof JLabel label) {
                label.setForeground(TemaInsta.TEXTO);

                if (label.isOpaque()) {
                    label.setBackground(TemaInsta.INPUT);
                }
            }

            if (componente instanceof Container interno) {
                aplicarTemaTarjetas(interno);
            }
        }
    }

    private void cambiarTexto(Container contenedor) {
        for (Component componente : contenedor.getComponents()) {

            if (componente instanceof JLabel label) {
                label.setForeground(TemaInsta.TEXTO);
            }

            if (componente instanceof JScrollPane scroll) {
                scroll.getViewport().setBackground(TemaInsta.FONDO);
            }

            if (componente instanceof Container interno) {
                cambiarTexto(interno);
            }
        }

        lblFoto.setForeground(TemaInsta.TEXTO_SECUNDARIO);
    }

    public JButton getBtnSeguir() {
        return btnSeguir;
    }

    public JPanel getPanelGridPublicaciones() {
        return panelGridPublicaciones;
    }
}