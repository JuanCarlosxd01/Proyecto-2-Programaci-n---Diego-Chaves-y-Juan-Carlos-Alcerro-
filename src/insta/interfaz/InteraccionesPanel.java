
package insta.interfaz;

import estructuras.ListaEnlazada;
import insta.modelo.Publicacion;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import red.Cliente;
import red.Respuesta;

public class InteraccionesPanel extends JPanel implements Tematizable {

    private JPanel panelEncabezado;
    private JPanel panelInteracciones;
    private JButton btnActualizar;
    private Cliente cliente;

    public InteraccionesPanel(Cliente cliente) {
        this.cliente = cliente;

        setLayout(new BorderLayout());

        crearEncabezado();
        crearContenido();
        configurarEventos();

        aplicarTema();
    }

    private void crearEncabezado() {
        panelEncabezado = new JPanel(new BorderLayout());
        panelEncabezado.setBorder(new EmptyBorder(20, 30, 15, 30));

        JLabel titulo = new JLabel("Menciones");
        titulo.setFont(new Font("Arial", Font.BOLD, 25));

        btnActualizar = new JButton("Actualizar");

        panelEncabezado.add(titulo, BorderLayout.WEST);
        panelEncabezado.add(btnActualizar, BorderLayout.EAST);

        add(panelEncabezado, BorderLayout.NORTH);
    }

    private void crearContenido() {
        panelInteracciones = new JPanel();
        panelInteracciones.setLayout(new BoxLayout(panelInteracciones, BoxLayout.Y_AXIS));
        panelInteracciones.setBorder(new EmptyBorder(20, 50, 20, 50));

        JScrollPane scroll = new JScrollPane(panelInteracciones);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        add(scroll, BorderLayout.CENTER);
    }

    private void configurarEventos() {
        btnActualizar.addActionListener(e -> cargarMenciones());
    }

    public void cargarMenciones() {
        panelInteracciones.removeAll();

        JLabel cargando = new JLabel("Cargando menciones...");
        cargando.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelInteracciones.add(cargando);

        panelInteracciones.revalidate();
        panelInteracciones.repaint();

        SwingWorker<ListaEnlazada<Publicacion>, Void> trabajador = new SwingWorker<>() {

            @Override
            protected ListaEnlazada<Publicacion> doInBackground() throws Exception {
                return PaginadorInsta.publicaciones(desde -> cliente.menciones(desde));
            }

            @Override
            protected void done() {
                panelInteracciones.removeAll();

                try {
                    ListaEnlazada<Publicacion> publicaciones = get();

                    if (publicaciones.isEmpty()) {
                        mostrarMensaje("Todavía nadie te ha mencionado.");
                        return;
                    }

                    for (Publicacion publicacion : publicaciones) {
                        agregarPublicacion(publicacion);
                    }

                } catch (Exception e) {
                    mostrarMensaje("No se pudieron cargar las menciones.");
                }

                aplicarTema();
                panelInteracciones.revalidate();
                panelInteracciones.repaint();
            }
        };

        trabajador.execute();
    }

    private void agregarPublicacion(Publicacion publicacion) {
        TarjetaPublicacionPanel tarjeta = new TarjetaPublicacionPanel(cliente, publicacion, this::cargarMenciones);
        panelInteracciones.add(tarjeta);
        panelInteracciones.add(Box.createVerticalStrut(15));
    }

    private void mostrarMensaje(String texto) {
        JLabel mensaje = new JLabel(texto);
        mensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        mensaje.setForeground(TemaInsta.TEXTO);

        panelInteracciones.add(Box.createVerticalStrut(40));
        panelInteracciones.add(mensaje);

        panelInteracciones.revalidate();
        panelInteracciones.repaint();
    }

    @Override
    public void aplicarTema() {
        setBackground(TemaInsta.FONDO);
        panelEncabezado.setBackground(TemaInsta.FONDO);
        panelInteracciones.setBackground(TemaInsta.FONDO_SECUNDARIO);

        btnActualizar.setBackground(TemaInsta.INPUT);
        btnActualizar.setForeground(TemaInsta.TEXTO);

        cambiarTexto(this);

        TemaComponentes.corregirContraste(this);

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

    public JButton getBtnActualizar() {
        return btnActualizar;
    }

    public JPanel getPanelInteracciones() {
        return panelInteracciones;
    }
}