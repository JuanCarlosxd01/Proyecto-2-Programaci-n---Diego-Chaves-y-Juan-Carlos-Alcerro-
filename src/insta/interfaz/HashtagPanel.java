
package insta.interfaz;

import interfaz.DialogosWindows;

import estructuras.ListaEnlazada;
import insta.modelo.Publicacion;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import red.Cliente;
import red.Respuesta;

public class HashtagPanel extends JPanel implements Tematizable {

    private JPanel panelSuperior;
    private JPanel panelBusqueda;
    private JPanel panelResultados;

    private JTextField txtHashtag;
    private JButton btnBuscar;

    private Cliente cliente;

    public HashtagPanel(Cliente cliente) {
        this.cliente = cliente;

        setLayout(new BorderLayout());

        crearEncabezado();
        crearResultados();
        configurarEventos();

        aplicarTema();
    }

    private void crearEncabezado() {
        panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel titulo = new JLabel("Buscar hashtag");
        titulo.setFont(new Font("Arial", Font.BOLD, 25));

        panelBusqueda = new JPanel(new BorderLayout(10, 0));

        JLabel hashtag = new JLabel("#");
        hashtag.setFont(new Font("Arial", Font.BOLD, 20));

        txtHashtag = new JTextField();

        btnBuscar = new JButton("Buscar");

        panelBusqueda.add(hashtag, BorderLayout.WEST);
        panelBusqueda.add(txtHashtag, BorderLayout.CENTER);
        panelBusqueda.add(btnBuscar, BorderLayout.EAST);

        panelSuperior.add(titulo, BorderLayout.NORTH);
        panelSuperior.add(panelBusqueda, BorderLayout.SOUTH);

        add(panelSuperior, BorderLayout.NORTH);
    }

    private void crearResultados() {
        panelResultados = new JPanel();

        panelResultados.setLayout(new BoxLayout(panelResultados, BoxLayout.Y_AXIS));
        panelResultados.setBorder(new EmptyBorder(20, 40, 20, 40));

        JLabel mensaje = new JLabel("Las publicaciones con el hashtag aparecerán aquí.");

        mensaje.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelResultados.add(mensaje);

        JScrollPane scroll = new JScrollPane(panelResultados);

        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        add(scroll, BorderLayout.CENTER);
    }

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> buscar());

        txtHashtag.addActionListener(e -> buscar());
    }

    private void buscar() {
        String texto = txtHashtag.getText().trim();

        if (texto.startsWith("#")) {
            texto = texto.substring(1);
        }

        if (texto.isBlank()) {
            DialogosWindows.showMessageDialog(
                    this,
                    "Ingrese un hashtag.",
                    "Buscar hashtag",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        String hashtagBuscado = texto;

        panelResultados.removeAll();

        JLabel cargando = new JLabel("Buscando #" + hashtagBuscado + "...");

        cargando.setAlignmentX(Component.CENTER_ALIGNMENT);
        cargando.setForeground(TemaInsta.TEXTO);

        panelResultados.add(cargando);

        panelResultados.revalidate();
        panelResultados.repaint();

        SwingWorker<ListaEnlazada<Publicacion>, Void> trabajador = new SwingWorker<>() {

            @Override
            protected ListaEnlazada<Publicacion> doInBackground() throws Exception {
                return PaginadorInsta.publicaciones(desde -> cliente.buscarHashtag(hashtagBuscado, desde));
            }

            @Override
            protected void done() {
                try {
                    ListaEnlazada<Publicacion> publicaciones = get();
                    panelResultados.removeAll();

                    if (publicaciones.isEmpty()) {
                        mostrarSinResultados(hashtagBuscado);
                    } else {
                        for (Publicacion publicacion : publicaciones) {
                            agregarPublicacion(publicacion);
                        }
                    }

                } catch (Exception e) {
                    panelResultados.removeAll();

                    mostrarError("No se pudo realizar la búsqueda.");
                }

                aplicarTema();

                panelResultados.revalidate();
                panelResultados.repaint();
            }
        };

        trabajador.execute();
    }

    private void agregarPublicacion(Publicacion publicacion) {
        TarjetaPublicacionPanel tarjeta = new TarjetaPublicacionPanel(cliente, publicacion, this::buscar);
        panelResultados.add(tarjeta);
        panelResultados.add(Box.createVerticalStrut(15));
    }

    private void mostrarSinResultados(String hashtag) {
        JLabel mensaje = new JLabel("No se encontraron publicaciones con #" + hashtag);

        mensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        mensaje.setForeground(TemaInsta.TEXTO);

        panelResultados.add(mensaje);
    }

    private void mostrarError(String texto) {
        JLabel mensaje = new JLabel(texto);

        mensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        mensaje.setForeground(TemaInsta.TEXTO);

        panelResultados.add(mensaje);
    }

    @Override
    public void aplicarTema() {
        setBackground(TemaInsta.FONDO);

        panelSuperior.setBackground(TemaInsta.FONDO);
        panelBusqueda.setBackground(TemaInsta.FONDO);
        panelResultados.setBackground(TemaInsta.FONDO_SECUNDARIO);

        txtHashtag.setBackground(TemaInsta.INPUT);
        txtHashtag.setForeground(TemaInsta.TEXTO);
        txtHashtag.setCaretColor(TemaInsta.TEXTO);

        btnBuscar.setBackground(TemaInsta.BOTON);
        btnBuscar.setForeground(TemaInsta.BOTON_TEXTO);

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
                scroll.getViewport().setBackground(TemaInsta.FONDO_SECUNDARIO);
            }

            if (componente instanceof Container interno) {
                cambiarTexto(interno);
            }
        }
    }

    public JTextField getTxtHashtag() {
        return txtHashtag;
    }

    public JButton getBtnBuscar() {
        return btnBuscar;
    }

    public JPanel getPanelResultados() {
        return panelResultados;
    }
}