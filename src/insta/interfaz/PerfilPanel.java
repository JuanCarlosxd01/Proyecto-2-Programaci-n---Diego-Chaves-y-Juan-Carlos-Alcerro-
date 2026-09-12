
package insta.interfaz;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import red.Respuesta;
import java.time.format.DateTimeFormatter;

public class PerfilPanel extends JPanel implements Tematizable {

    private JPanel panelSuperior;
    private JPanel panelDatos;
    private JPanel panelEstadisticas;
    private JPanel panelGridPublicaciones;

    private JLabel lblFoto;
    private JLabel lblNombre;
    private JLabel lblUsername;
    private JLabel lblEdad;
    private JLabel lblGenero;
    private JLabel lblFecha;
    private JLabel lblEstado;

    private JLabel lblSeguidores;
    private JLabel lblSeguidos;
    private JLabel lblPublicaciones;

    private JButton btnSeguir;
    private JButton btnVerPublicaciones;

    public PerfilPanel() {
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
        btnVerPublicaciones = new JButton("Ver publicaciones");

        encabezado.add(lblUsername);
        encabezado.add(btnSeguir);
        encabezado.add(btnVerPublicaciones);

        lblNombre = new JLabel("Nombre completo");
        lblNombre.setFont(new Font("Arial", Font.BOLD, 16));

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
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBorder(new EmptyBorder(10, 40, 20, 40));

        JLabel titulo = new JLabel("PUBLICACIONES", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 14));

        panelGridPublicaciones = new JPanel(new GridLayout(0, 3, 5, 5));

        contenedor.add(titulo, BorderLayout.NORTH);
        contenedor.add(panelGridPublicaciones, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(contenedor);
        scroll.setBorder(null);

        add(scroll, BorderLayout.CENTER);
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

        btnVerPublicaciones.setBackground(TemaInsta.INPUT);
        btnVerPublicaciones.setForeground(TemaInsta.TEXTO);

        cambiarTexto(this);

        revalidate();
        repaint();
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
    
    public void mostrarUsuario(Respuesta.DatosUsuario usuario) {
        if (usuario == null) {
            return;
        }

        lblUsername.setText("@" + usuario.getUsername());
        lblNombre.setText(usuario.getNombreCompleto());
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
}