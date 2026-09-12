
package insta.interfaz;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import red.Cliente;
import red.Respuesta;
import java.util.function.Consumer;

public class BuscarPanel extends JPanel implements Tematizable {

    private JPanel panelSuperior;
    private JPanel panelBusqueda;
    private JPanel panelResultados;

    private JTextField txtBusqueda;
    private JButton btnBuscar;
    private Cliente cliente;
    private Runnable accionSeguimientoActualizado;
    private Consumer<String> accionAbrirPerfil;

    public BuscarPanel(Cliente cliente) {
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

        JLabel titulo = new JLabel("Buscar perfiles");
        titulo.setFont(new Font("Arial", Font.BOLD, 25));

        panelBusqueda = new JPanel(new BorderLayout(10, 0));

        txtBusqueda = new JTextField();
        btnBuscar = new JButton("Buscar");

        panelBusqueda.add(txtBusqueda, BorderLayout.CENTER);
        panelBusqueda.add(btnBuscar, BorderLayout.EAST);

        panelSuperior.add(titulo, BorderLayout.NORTH);
        panelSuperior.add(panelBusqueda, BorderLayout.SOUTH);

        add(panelSuperior, BorderLayout.NORTH);
    }
    
    private void configurarEventos() {
        btnBuscar.addActionListener(e -> buscarUsuarios());

        txtBusqueda.addActionListener(e -> buscarUsuarios());
    }
    
    private void buscarUsuarios() {
        String texto = txtBusqueda.getText().trim();

        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Escribe un nombre o username para buscar.",
                    "Buscar",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        panelResultados.removeAll();

        JLabel lblCargando = new JLabel("Buscando usuarios...");
        lblCargando.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelResultados.add(lblCargando);
        panelResultados.revalidate();
        panelResultados.repaint();

        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {

            @Override
            protected Respuesta doInBackground() throws Exception {
                return cliente.buscarPersonas(texto, 0);
            }

            @Override
            protected void done() {
                try {
                    Respuesta respuesta = get();

                    panelResultados.removeAll();

                    if (respuesta.esExitosa()) {
                        if (respuesta.getUsuarios().isEmpty()) {
                            mostrarMensaje("No se encontraron usuarios.");
                        } else {
                            for (Respuesta.DatosUsuario usuario : respuesta.getUsuarios()) {
                                agregarUsuario(usuario);
                            }
                        }
                    } else {
                        mostrarMensaje(respuesta.getMensaje());
                    }

                    aplicarTema();
                    panelResultados.revalidate();
                    panelResultados.repaint();

                } catch (Exception e) {
                    panelResultados.removeAll();
                    mostrarMensaje("No se pudo realizar la búsqueda.");
                    panelResultados.revalidate();
                    panelResultados.repaint();
                }
            }
        };

        trabajador.execute();
    }

    private void crearResultados() {
        panelResultados = new JPanel();
        panelResultados.setLayout(new BoxLayout(panelResultados, BoxLayout.Y_AXIS));
        panelResultados.setBorder(new EmptyBorder(20, 40, 20, 40));

        JLabel mensaje = new JLabel("Busca un usuario por su username.");
        mensaje.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelResultados.add(mensaje);

        JScrollPane scroll = new JScrollPane(panelResultados);
        scroll.setBorder(null);

        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void aplicarTema() {
        setBackground(TemaInsta.FONDO);

        panelSuperior.setBackground(TemaInsta.FONDO);
        panelBusqueda.setBackground(TemaInsta.FONDO);
        panelResultados.setBackground(TemaInsta.FONDO_SECUNDARIO);

        txtBusqueda.setBackground(TemaInsta.INPUT);
        txtBusqueda.setForeground(TemaInsta.TEXTO);
        txtBusqueda.setCaretColor(TemaInsta.TEXTO);

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
    
    private void agregarUsuario(Respuesta.DatosUsuario usuario) {
        JPanel tarjeta = new JPanel(new BorderLayout(15, 10));

        tarjeta.setBorder(
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        );

        tarjeta.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 85)
        );

        JPanel datos = new JPanel();

        datos.setLayout(
                new BoxLayout(datos, BoxLayout.Y_AXIS)
        );

        JLabel lblUsername =
                new JLabel("@" + usuario.getUsername());

        lblUsername.setFont(
                new Font("Arial", Font.BOLD, 15)
        );
        lblUsername.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblUsername.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (accionAbrirPerfil != null) {
                    accionAbrirPerfil.accept(usuario.getUsername());
                }
            }
        });

        JLabel lblNombre =
                new JLabel(usuario.getNombreCompleto());

        JLabel lblSeguidores =
                new JLabel(usuario.getFollowers() + " seguidores");

        datos.add(lblUsername);
        datos.add(lblNombre);
        datos.add(lblSeguidores);

        JButton btnSeguir = new JButton();

        if (usuario.loSigo()) {
            btnSeguir.setText("Dejar de seguir");
        } else {
            btnSeguir.setText("Seguir");
        }

        btnSeguir.addActionListener(e -> {
            cambiarSeguimiento(
                    usuario,
                    btnSeguir
            );
        });

        tarjeta.add(datos, BorderLayout.CENTER);
        tarjeta.add(btnSeguir, BorderLayout.EAST);

        panelResultados.add(tarjeta);
        panelResultados.add(Box.createVerticalStrut(8));
    }
    
    private void cambiarSeguimiento(Respuesta.DatosUsuario usuario, JButton boton){
        boton.setEnabled(false);

        SwingWorker<Respuesta, Void> trabajador =
                new SwingWorker<>() {

            @Override
            protected Respuesta doInBackground()
                    throws Exception {

                if (usuario.loSigo()) {
                    return cliente.dejarDeSeguir(
                            usuario.getUsername()
                    );
                } else {
                    return cliente.seguir(
                            usuario.getUsername()
                    );
                }
            }

            @Override
            protected void done() {
                try {
                    Respuesta respuesta = get();

                    if (respuesta.esExitosa()) {

                        if (accionSeguimientoActualizado != null) {
                            accionSeguimientoActualizado.run();
                        }

                        buscarUsuarios();

                    } else {
                        JOptionPane.showMessageDialog(
                                BuscarPanel.this,
                                respuesta.getMensaje(),
                                "Seguimiento",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            BuscarPanel.this,
                            "No se pudo actualizar el seguimiento.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }

                boton.setEnabled(true);
            }
        };

        trabajador.execute();
    }
    
    private void mostrarMensaje(String texto) {
        JLabel mensaje = new JLabel(texto);

        mensaje.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        panelResultados.add(
                Box.createVerticalStrut(30)
        );

        panelResultados.add(mensaje);
    }
    
    public void setAccionSeguimientoActualizado(Runnable accionSeguimientoActualizado) {
        this.accionSeguimientoActualizado =accionSeguimientoActualizado;
    }

    public JTextField getTxtBusqueda() {
        return txtBusqueda;
    }

    public JButton getBtnBuscar() {
        return btnBuscar;
    }

    public JPanel getPanelResultados() {
        return panelResultados;
    }
    
    public void setAccionAbrirPerfil(Consumer<String> accionAbrirPerfil) {
        this.accionAbrirPerfil = accionAbrirPerfil;
    }
}