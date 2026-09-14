package insta.interfaz;

import estructuras.ListaEnlazada;
import interfaz.DialogosWindows;
import java.awt.*;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import red.Cliente;
import red.Respuesta;

public class BuscarPanel extends JPanel implements Tematizable {

    private JPanel panelSuperior;
    private JPanel panelBusqueda;
    private JPanel panelResultados;
    private JTextField txtBusqueda;
    private JButton btnBuscar;
    private final Cliente cliente;
    private Runnable accionSeguimientoActualizado;
    private Consumer<String> accionAbrirPerfil;
    private final Timer temporizadorBusqueda = new Timer(280, e -> buscarUsuarios(false));
    private int secuenciaBusqueda = 0;

    public BuscarPanel(Cliente cliente) {
        this.cliente = cliente;
        setLayout(new BorderLayout());
        crearEncabezado();
        crearResultados();
        temporizadorBusqueda.setRepeats(false);
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
        btnBuscar.addActionListener(e -> buscarUsuarios(true));
        txtBusqueda.addActionListener(e -> buscarUsuarios(true));
        txtBusqueda.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void cambio() {
                temporizadorBusqueda.restart();
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { cambio(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { cambio(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { cambio(); }
        });
    }

    private void buscarUsuarios(boolean mostrarAviso) {
        String texto = txtBusqueda.getText().trim().replaceFirst("^@", "");
        if (texto.isEmpty()) {
            secuenciaBusqueda++;
            panelResultados.removeAll();
            mostrarMensaje("Empieza a escribir para ver usuarios parecidos.");
            panelResultados.revalidate();
            panelResultados.repaint();
            if (mostrarAviso) txtBusqueda.requestFocusInWindow();
            return;
        }
        final int miSecuencia = ++secuenciaBusqueda;

        panelResultados.removeAll();
        JLabel lblCargando = new JLabel("Buscando usuarios...");
        lblCargando.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelResultados.add(lblCargando);
        panelResultados.revalidate();
        panelResultados.repaint();
        btnBuscar.setEnabled(false);

        SwingWorker<ListaEnlazada<Respuesta.DatosUsuario>, Void> trabajador = new SwingWorker<>() {
            @Override
            protected ListaEnlazada<Respuesta.DatosUsuario> doInBackground() throws Exception {
                ListaEnlazada<Respuesta.DatosUsuario> todos = new ListaEnlazada<>();
                int desde = 0;
                int total;
                do {
                    Respuesta respuesta = cliente.buscarPersonas(texto, desde);
                    if (!respuesta.esExitosa()) {
                        throw new IllegalStateException(respuesta.getMensaje());
                    }
                    ListaEnlazada<Respuesta.DatosUsuario> pagina = respuesta.getUsuarios();
                    for (Respuesta.DatosUsuario usuario : pagina) {
                        todos.agregar(usuario);
                    }
                    total = respuesta.getTotal();
                    desde += pagina.size();
                    if (pagina.isEmpty()) break;
                } while (desde < total);
                return todos;
            }

            @Override
            protected void done() {
                if (miSecuencia != secuenciaBusqueda) return;
                btnBuscar.setEnabled(true);
                panelResultados.removeAll();
                try {
                    ListaEnlazada<Respuesta.DatosUsuario> usuarios = get();
                    if (usuarios.isEmpty()) {
                        mostrarMensaje("No se encontraron usuarios.");
                    } else {
                        for (Respuesta.DatosUsuario usuario : usuarios) {
                            agregarUsuario(usuario);
                        }
                    }
                } catch (Exception e) {
                    mostrarMensaje("No se pudo realizar la búsqueda: " + mensaje(e));
                }
                aplicarTema();
                panelResultados.revalidate();
                panelResultados.repaint();
            }
        };
        trabajador.execute();
    }

    private void crearResultados() {
        panelResultados = new JPanel();
        panelResultados.setLayout(new BoxLayout(panelResultados, BoxLayout.Y_AXIS));
        panelResultados.setBorder(new EmptyBorder(20, 40, 20, 40));
        JLabel mensaje = new JLabel("Empieza a escribir un nombre o @username.");
        mensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelResultados.add(mensaje);
        JScrollPane scroll = new JScrollPane(panelResultados);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        add(scroll, BorderLayout.CENTER);
    }

    private void agregarUsuario(Respuesta.DatosUsuario usuario) {
        JPanel tarjeta = new JPanel(new BorderLayout(15, 10));
        tarjeta.setOpaque(true);
        tarjeta.setBackground(TemaInsta.TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(TemaInsta.BORDE), BorderFactory.createEmptyBorder(12, 15, 12, 15)));
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

        JPanel datos = new JPanel();
        datos.setOpaque(false);
        datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS));
        JLabel lblUsername = new JLabel("@" + usuario.getUsername());
        lblUsername.setFont(new Font("Arial", Font.BOLD, 15));
        lblUsername.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblUsername.setToolTipText("Abrir perfil");
        lblUsername.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (accionAbrirPerfil != null) accionAbrirPerfil.accept(usuario.getUsername());
            }
        });
        JLabel lblNombre = new JLabel(usuario.getNombreCompleto());
        JLabel lblSeguidores = new JLabel(usuario.getFollowers() + " seguidores");
        JLabel lblRelacion = new JLabel(usuario.loSigo() ? "Lo sigo" : "No lo sigues");
        lblRelacion.setFont(new Font("Arial", Font.ITALIC, 12));
        datos.add(lblUsername);
        datos.add(lblNombre);
        datos.add(lblSeguidores);
        datos.add(lblRelacion);

        JButton btnSeguir = new JButton(usuario.loSigo() ? "Dejar de seguir" : "Seguir");
        btnSeguir.setOpaque(true);
        btnSeguir.setContentAreaFilled(true);
        btnSeguir.setFocusPainted(false);
        btnSeguir.setBackground(usuario.loSigo() ? TemaInsta.INPUT : TemaInsta.BOTON);
        btnSeguir.setForeground(usuario.loSigo() ? TemaInsta.TEXTO : TemaInsta.BOTON_TEXTO);
        btnSeguir.addActionListener(e -> cambiarSeguimiento(usuario, btnSeguir));
        tarjeta.add(datos, BorderLayout.CENTER);
        tarjeta.add(btnSeguir, BorderLayout.EAST);
        panelResultados.add(tarjeta);
        panelResultados.add(Box.createVerticalStrut(8));
    }

    private void cambiarSeguimiento(Respuesta.DatosUsuario usuario, JButton boton) {
        if (usuario.loSigo()) {
            int opcion = DialogosWindows.showConfirmDialog(this, "¿Dejar de seguir a @" + usuario.getUsername() + "?", "Dejar de seguir", JOptionPane.YES_NO_OPTION);
            if (opcion != JOptionPane.YES_OPTION) return;
        }

        boton.setEnabled(false);
        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {
            @Override
            protected Respuesta doInBackground() throws Exception {
                return usuario.loSigo() ? cliente.dejarDeSeguir(usuario.getUsername()) : cliente.seguir(usuario.getUsername());
            }

            @Override
            protected void done() {
                try {
                    Respuesta respuesta = get();
                    if (respuesta.esExitosa()) {
                        if (accionSeguimientoActualizado != null) accionSeguimientoActualizado.run();
                        buscarUsuarios(false);
                    } else {
                        DialogosWindows.showMessageDialog(BuscarPanel.this, respuesta.getMensaje(), "Seguimiento", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    DialogosWindows.showMessageDialog(BuscarPanel.this, "No se pudo actualizar el seguimiento.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                boton.setEnabled(true);
            }
        };
        trabajador.execute();
    }

    private String mensaje(Exception e) {
        Throwable t = e;
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() == null ? "Error desconocido" : t.getMessage();
    }

    private void mostrarMensaje(String texto) {
        JLabel mensaje = new JLabel(texto);
        mensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelResultados.add(Box.createVerticalStrut(30));
        panelResultados.add(mensaje);
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
        TemaComponentes.corregirContraste(this);
        revalidate();
        repaint();
    }

    private void cambiarTexto(Container contenedor) {
        for (Component componente : contenedor.getComponents()) {
            if (componente instanceof JLabel label) label.setForeground(TemaInsta.TEXTO);
            if (componente instanceof JScrollPane scroll) scroll.getViewport().setBackground(TemaInsta.FONDO_SECUNDARIO);
            if (componente instanceof Container interno) cambiarTexto(interno);
        }
    }

    public void setAccionSeguimientoActualizado(Runnable accionSeguimientoActualizado) { this.accionSeguimientoActualizado = accionSeguimientoActualizado; }
    public JTextField getTxtBusqueda() { return txtBusqueda; }
    public JButton getBtnBuscar() { return btnBuscar; }
    public JPanel getPanelResultados() { return panelResultados; }
    public void setAccionAbrirPerfil(Consumer<String> accionAbrirPerfil) { this.accionAbrirPerfil = accionAbrirPerfil; }
}
