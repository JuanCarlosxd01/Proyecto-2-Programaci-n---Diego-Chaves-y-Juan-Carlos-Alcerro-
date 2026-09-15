package interfaz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import sistema.*;
import insta.interfaz.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import pokemon.interfaz.VentanaPokemon;

public class EscritorioPanel extends JPanel {

    private JDesktopPane escritorio;
    private JPanel barraTareas;
    private JPanel panelVentanas;
    private JPanel panelAppsTaskbar;
    private static final int MAX_INSTANCIAS_POR_APP = 5;
    private final Map<String, JButton> botonesTaskbar = new LinkedHashMap<>();
    private final Map<String, List<JInternalFrame>> ventanasPorApp = new LinkedHashMap<>();
    private JLabel lblHora;
    private JButton btnWindows;
    private JButton iconoSeleccionado = null;
    private JButton btnUsuarios;
    private GestorUsuarios gestorUsuarios;
    private JPanel menuInicio;
    private JLabel lblUsuarioMenu;
    private JPanel contenedor;
    private CardLayout transicion;

    private int anchoCelda = 100;
    private int altoCelda = 100;

    public EscritorioPanel(JPanel contenedor, CardLayout transicion, GestorUsuarios gestorUsuarios){
        this.contenedor = contenedor;
        this.transicion = transicion;
        this.gestorUsuarios = gestorUsuarios;
        setLayout(new BorderLayout());
        crearEscritorio();
        crearBarraTareas();
        crearIconosEjemplo();
        crearMenuInicio();
        addComponentListener(new ComponentAdapter(){
            @Override
            public void componentShown(ComponentEvent e){
                actualizarPermisos();
                cargarFondoGuardado();
            }
        });
    }

    private void crearEscritorio(){
        EscritorioWindowsPane pane = new EscritorioWindowsPane();
        escritorio = pane;
        DialogosWindows.registrarEscritorio(escritorio);
        escritorio.setLayout(null);
        escritorio.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (menuInicio != null && menuInicio.isVisible()) ocultarMenuInicio();
            }
        });
        cargarFondoGuardado();

        JPopupMenu menuContextual = new JPopupMenu();
        JMenuItem personalizar = new JMenuItem("Personalizar fondo...");
        JMenuItem actualizar = new JMenuItem("Actualizar");
        personalizar.addActionListener(e -> abrirVentana("Configuración"));
        actualizar.addActionListener(e -> escritorio.repaint());
        menuContextual.add(actualizar);
        menuContextual.addSeparator();
        menuContextual.add(personalizar);
        escritorio.setComponentPopupMenu(menuContextual);

        add(escritorio, BorderLayout.CENTER);
    }

    private void crearBarraTareas(){
        barraTareas = new JPanel(new BorderLayout());
        barraTareas.setPreferredSize(new Dimension(0, 46));
        barraTareas.setBackground(new Color(24, 24, 24));
        barraTareas.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(55, 55, 55)));

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        izquierda.setOpaque(false);
        panelAppsTaskbar = izquierda;

        btnWindows = new JButton(IconosWindows.crear("windows", 24));
        btnWindows.setForeground(Color.WHITE);
        btnWindows.setPreferredSize(new Dimension(48, 40));
        btnWindows.setBackground(new Color(24, 24, 24));
        btnWindows.setFocusPainted(false);
        btnWindows.setBorderPainted(false);
        btnWindows.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnWindows.addMouseListener(new MouseAdapter(){
            @Override public void mouseEntered(MouseEvent e){ btnWindows.setBackground(new Color(48, 48, 48)); }
            @Override public void mouseExited(MouseEvent e){ btnWindows.setBackground(menuInicio != null && menuInicio.isVisible() ? new Color(55, 55, 55) : new Color(24, 24, 24)); }
        });
        btnWindows.addActionListener(e -> mostrarOcultarMenuInicio());
        izquierda.add(btnWindows);

        agregarAccesoTaskbar(izquierda, "Archivos", "archivos");
        agregarAccesoTaskbar(izquierda, "Word", "word");
        agregarAccesoTaskbar(izquierda, "Música", "musica");
        agregarAccesoTaskbar(izquierda, "CMD", "cmd");
        agregarAccesoTaskbar(izquierda, "Imágenes", "imagenes");
        agregarAccesoTaskbar(izquierda, "INSTA+", "insta");

        barraTareas.add(izquierda, BorderLayout.WEST);

        panelVentanas = new JPanel();
        panelVentanas.setOpaque(false);

        JPanel derecha = new JPanel(new GridBagLayout());
        derecha.setOpaque(false);
        derecha.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 12));
        lblHora = new JLabel();
        lblHora.setForeground(Color.WHITE);
        lblHora.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHora.setHorizontalAlignment(SwingConstants.RIGHT);
        derecha.add(lblHora);
        barraTareas.add(derecha, BorderLayout.EAST);

        actualizarHora();
        new Timer(1000, e -> actualizarHora()).start();
        add(barraTareas, BorderLayout.SOUTH);
    }


    private void agregarAccesoTaskbar(JPanel panel, String nombre, String icono) {
        JButton boton = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = getModel().isRollover();
                boolean activa = hayVentanaActiva(nombre);
                if (hover) {
                    g2.setColor(new Color(62, 62, 62));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } else if (activa) {
                    g2.setColor(new Color(45, 45, 45));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                javax.swing.Icon app = IconosWindows.crear(icono, 24);
                int x = (getWidth() - app.getIconWidth()) / 2;
                int y = (getHeight() - app.getIconHeight()) / 2 - 1;
                int cantidad = cantidadInstancias(nombre);
                app.paintIcon(this, g2, x, y);
                if (cantidad > 1) {
                    String numero = String.valueOf(cantidad);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    FontMetrics fm = g2.getFontMetrics();
                    int diametro = Math.max(15, fm.stringWidth(numero) + 8);
                    int bx = getWidth() - diametro - 3;
                    int by = 2;
                    g2.setColor(new Color(0, 120, 215));
                    g2.fillOval(bx, by, diametro, 15);
                    g2.setColor(Color.WHITE);
                    g2.drawString(numero, bx + (diametro - fm.stringWidth(numero)) / 2, by + 11);
                }
                if (cantidad > 0) {
                    g2.setColor(activa ? new Color(0, 120, 215) : new Color(125, 125, 125));
                    g2.fillRect(8, getHeight() - 3, getWidth() - 16, 3);
                }
                g2.dispose();
            }
        };
        boton.setToolTipText(nombre);
        boton.setPreferredSize(new Dimension(44, 38));
        boton.setMinimumSize(new Dimension(44, 38));
        boton.setMaximumSize(new Dimension(44, 38));
        boton.setOpaque(false);
        boton.setContentAreaFilled(false);
        boton.setRolloverEnabled(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setFocusable(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.addActionListener(e -> manejarClickTaskbar(nombre));
        boton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                boton.repaint();
                if (cantidadInstancias(nombre) > 0) mostrarMiniaturasTaskbar(nombre, boton);
            }
            @Override public void mouseExited(MouseEvent e) { boton.repaint(); }
        });
        botonesTaskbar.put(nombre, boton);
        panel.add(boton);
    }

    private boolean esAccesoFijoTaskbar(String nombre) {
        return nombre.equals("Archivos") || nombre.equals("Word") || nombre.equals("Música") || nombre.equals("CMD") || nombre.equals("Imágenes") || nombre.equals("INSTA+");
    }

    private void asegurarIconoTaskbar(String nombre) {
        if (botonesTaskbar.containsKey(nombre) || panelAppsTaskbar == null) return;
        agregarAccesoTaskbar(panelAppsTaskbar, nombre, tipoIconoPara(nombre));
        panelAppsTaskbar.revalidate();
        panelAppsTaskbar.repaint();
    }

    private void retirarIconoDinamicoSiCorresponde(String nombre) {
        if (esAccesoFijoTaskbar(nombre) || cantidadInstancias(nombre) > 0) return;
        JButton boton = botonesTaskbar.remove(nombre);
        if (boton != null && panelAppsTaskbar != null) {
            panelAppsTaskbar.remove(boton);
            panelAppsTaskbar.revalidate();
            panelAppsTaskbar.repaint();
        }
    }

    private int cantidadInstancias(String nombre) {
        List<JInternalFrame> lista = ventanasPorApp.get(nombre);
        if (lista == null) return 0;
        lista.removeIf(v -> v == null || v.isClosed());
        return lista.size();
    }

    private boolean puedeAbrirOtraVentana(String nombre) {
        if (cantidadInstancias(nombre) < MAX_INSTANCIAS_POR_APP) return true;
        DialogosWindows.showMessageDialog(this, "Se alcanzó el límite de " + MAX_INSTANCIAS_POR_APP + " instancias de " + nombre + ". Cierre una para abrir otra.", "Límite de instancias", JOptionPane.WARNING_MESSAGE);
        return false;
    }

    private boolean puedeAbrirOtraVentana() {
        return true;
    }

    private void registrarVentanaTaskbar(JInternalFrame ventana, String nombre) {
        asegurarIconoTaskbar(nombre);
        ventanasPorApp.computeIfAbsent(nombre, k -> new ArrayList<>()).add(ventana);
        JButton boton = botonesTaskbar.get(nombre);
        if (boton != null) boton.repaint();
        ventana.addInternalFrameListener(new InternalFrameAdapter() {
            @Override public void internalFrameActivated(InternalFrameEvent e) { actualizarEstadoTaskbar(nombre); }
            @Override public void internalFrameDeactivated(InternalFrameEvent e) { actualizarEstadoTaskbar(nombre); }
            @Override public void internalFrameIconified(InternalFrameEvent e) { ventana.getDesktopIcon().setVisible(false); actualizarEstadoTaskbar(nombre); }
            @Override public void internalFrameDeiconified(InternalFrameEvent e) { ventana.getDesktopIcon().setVisible(false); actualizarEstadoTaskbar(nombre); }
            @Override public void internalFrameClosed(InternalFrameEvent e) {
                List<JInternalFrame> lista = ventanasPorApp.get(nombre);
                if (lista != null) lista.remove(ventana);
                actualizarEstadoTaskbar(nombre);
                retirarIconoDinamicoSiCorresponde(nombre);
            }
        });
        actualizarEstadoTaskbar(nombre);
    }

    private boolean hayVentanaActiva(String nombre) {
        List<JInternalFrame> lista = ventanasPorApp.get(nombre);
        if (lista == null) return false;
        for (JInternalFrame v : lista) if (!v.isClosed() && v.isSelected() && !v.isIcon()) return true;
        return false;
    }

    private void actualizarEstadoTaskbar(String nombre) {
        JButton boton = botonesTaskbar.get(nombre);
        if (boton == null) return;
        boolean abierta = cantidadInstancias(nombre) > 0;
        boolean activa = hayVentanaActiva(nombre);
        boton.repaint();
    }

    private void manejarClickTaskbar(String nombre) {
        List<JInternalFrame> lista = ventanasPorApp.get(nombre);
        if (lista == null || lista.isEmpty()) {
            abrirVentana(nombre);
            return;
        }
        lista.removeIf(v -> v == null || v.isClosed());
        if (lista.size() == 1) {
            JInternalFrame ventana = lista.get(0);
            if (ventana.isSelected() && !ventana.isIcon()) {
                try { ventana.setIcon(true); } catch (java.beans.PropertyVetoException ignored) {}
            } else activarVentana(ventana);
        } else mostrarMiniaturasTaskbar(nombre, botonesTaskbar.get(nombre));
    }

    private ImageIcon crearVistaPrevia(JInternalFrame ventana, int ancho, int alto) {
        try {
            int w = Math.max(1, ventana.getWidth());
            int h = Math.max(1, ventana.getHeight());
            java.awt.image.BufferedImage captura = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = captura.createGraphics();
            ventana.printAll(g2);
            g2.dispose();
            double escala = Math.min((double) ancho / w, (double) alto / h);
            int nw = Math.max(1, (int) (w * escala));
            int nh = Math.max(1, (int) (h * escala));
            Image img = captura.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
    }

    private void mostrarMiniaturasTaskbar(String nombre, JButton origen) {
        List<JInternalFrame> lista = ventanasPorApp.get(nombre);
        if (lista == null || lista.isEmpty() || origen == null) return;
        lista.removeIf(v -> v == null || v.isClosed());
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        JPanel miniaturas = new JPanel(new GridLayout(1, lista.size(), 6, 0));
        miniaturas.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        miniaturas.setBackground(new Color(24, 24, 24));
        for (int i = 0; i < lista.size(); i++) {
            JInternalFrame ventana = lista.get(i);
            ImageIcon vista = crearVistaPrevia(ventana, 118, 66);
            JButton mini = new JButton("<html><center>" + nombre + " · Ventana " + (i + 1) + "</center></html>", vista);
            mini.setVerticalTextPosition(SwingConstants.BOTTOM);
            mini.setHorizontalTextPosition(SwingConstants.CENTER);
            mini.setForeground(Color.BLACK);
            mini.setBackground(new Color(235, 235, 235));
            mini.setPreferredSize(new Dimension(134, 96));
            mini.setOpaque(true);
            mini.setContentAreaFilled(true);
            mini.setBorder(BorderFactory.createLineBorder(new Color(75, 75, 75)));
            mini.setFocusPainted(false);
            mini.setCursor(new Cursor(Cursor.HAND_CURSOR));
            mini.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { mini.setBackground(new Color(215, 225, 235)); }
                @Override public void mouseExited(MouseEvent e) { mini.setBackground(new Color(235, 235, 235)); }
            });
            mini.addActionListener(e -> { popup.setVisible(false); activarVentana(ventana); });
            miniaturas.add(mini);
        }
        popup.add(miniaturas);
        Dimension tamPopup = popup.getPreferredSize();
        int xCentrado = (origen.getWidth() - tamPopup.width) / 2;
        popup.show(origen, xCentrado, -tamPopup.height);
        Timer cierreHover = new Timer(220, e -> {
            try {
                Point mouse = MouseInfo.getPointerInfo().getLocation();
                Point botonPantalla = origen.getLocationOnScreen();
                Rectangle zonaBoton = new Rectangle(botonPantalla.x, botonPantalla.y, origen.getWidth(), origen.getHeight());
                Point popupPantalla = popup.getLocationOnScreen();
                Rectangle zonaPopup = new Rectangle(popupPantalla.x, popupPantalla.y, popup.getWidth(), popup.getHeight());
                if (!zonaBoton.contains(mouse) && !zonaPopup.contains(mouse)) popup.setVisible(false);
            } catch (Exception ex) {
                popup.setVisible(false);
            }
        });
        cierreHover.setRepeats(true);
        cierreHover.start();
        popup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {}
            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) { cierreHover.stop(); }
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) { cierreHover.stop(); }
        });
    }

    private JInternalFrame buscarVentanaAbierta(String nombre) {
        for (JInternalFrame ventana : escritorio.getAllFrames()) {
            if (!ventana.isClosed() && nombre.equals(ventana.getTitle())) return ventana;
        }
        return null;
    }

    private void activarVentana(JInternalFrame ventana) {
        try {
            if (ventana.isIcon()) ventana.setIcon(false);
            ventana.setVisible(true);
            ventana.setSelected(true);
            ventana.toFront();
        } catch (java.beans.PropertyVetoException e) {
        }
    }

    private void actualizarHora(){
        Date ahora = new Date();
        String hora = new SimpleDateFormat("HH:mm").format(ahora);
        String fecha = new SimpleDateFormat("dd/MM/yyyy").format(ahora);
        lblHora.setText("<html><div style='text-align:right'>" + hora + "<br>" + fecha + "</div></html>");
    }

    private void crearIconosEjemplo(){
        crearIcono("Equipo", "equipo", 0, 0);
        crearIcono("Archivos", "archivos", 0, 1);
        crearIcono("Word", "word", 0, 2);
        crearIcono("Música", "musica", 0, 3);
        crearIcono("CMD", "cmd", 1, 0);
        crearIcono("INSTA+", "insta", 1, 1);
        crearIcono("Imágenes", "imagenes", 1, 2);
        crearIcono("Configuración", "configuracion", 1, 3);
        crearIcono("Pokémon", "pokemon", 2, 1);
        btnUsuarios = crearIcono("Usuarios", "usuarios", 2, 0);
        btnUsuarios.setVisible(false);
    }

    private JButton crearIcono(String nombre, String tipoIcono, int columna, int fila){
        JButton icono = new JButton(nombre, IconosWindows.crear(tipoIcono, 46));
        icono.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        icono.setForeground(Color.WHITE);
        icono.setVerticalTextPosition(SwingConstants.BOTTOM);
        icono.setHorizontalTextPosition(SwingConstants.CENTER);
        icono.setIconTextGap(5);
        icono.setOpaque(false);
        icono.setContentAreaFilled(false);
        icono.setBorderPainted(false);
        icono.setFocusPainted(false);
        icono.setCursor(new Cursor(Cursor.HAND_CURSOR));
        icono.addMouseListener(new MouseAdapter(){
            @Override public void mouseEntered(MouseEvent e){
                if(icono != iconoSeleccionado){
                    icono.setOpaque(true);
                    icono.setContentAreaFilled(true);
                    icono.setBackground(new Color(255, 255, 255, 38));
                    icono.setForeground(Color.BLACK);
                    icono.setBorderPainted(true);
                    icono.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 65), 1));
                }
            }
            @Override public void mouseExited(MouseEvent e){
                if(icono != iconoSeleccionado){
                    icono.setOpaque(false);
                    icono.setContentAreaFilled(false);
                    icono.setBorderPainted(false);
                    icono.setForeground(Color.WHITE);
                }
            }
        });

        int x = columna * anchoCelda;
        int y = fila * altoCelda;
        icono.setBounds(x, y, anchoCelda, altoCelda);

        permitirMover(icono);
        detectarClicks(icono, nombre);

        escritorio.add(icono, JLayeredPane.DEFAULT_LAYER);
        return icono;
    }

    private void detectarClicks(JButton icono, String nombre){
        icono.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                seleccionarIcono(icono);

                if(e.getClickCount() == 2){
                    abrirVentana(nombre);
                }
            }
        });
    }
    
    private void seleccionarIcono(JButton icono) {
        if(iconoSeleccionado != null && iconoSeleccionado != icono){
            iconoSeleccionado.setContentAreaFilled(false);
            iconoSeleccionado.setBorderPainted(false);
            iconoSeleccionado.setOpaque(false);
            iconoSeleccionado.setForeground(Color.WHITE);
        }

        iconoSeleccionado = icono;

        icono.setOpaque(true);
        icono.setContentAreaFilled(true);

        // Selección clara
        icono.setBackground(new Color(190, 220, 245, 210));

        // Texto oscuro para que siempre sea legible
        icono.setForeground(new Color(20, 45, 65));

        icono.setBorderPainted(true);
        icono.setBorder(BorderFactory.createLineBorder(new Color(80, 160, 220), 1));

        icono.repaint();
    }

    private void abrirArchivoDesdeExplorador(File archivo) {
        if (archivo == null || !archivo.exists() || archivo.isDirectory()) {
            return;
        }

        String nombre = archivo.getName().toLowerCase();

        if (nombre.endsWith(".txt") || nombre.endsWith(".edt")) {
            abrirEditorConArchivo(archivo);
            return;
        }

        if (nombre.endsWith(".png") || nombre.endsWith(".jpg") || nombre.endsWith(".jpeg") || nombre.endsWith(".gif") || nombre.endsWith(".bmp")) {
            abrirVisorConArchivo(archivo);
            return;
        }

        if (nombre.endsWith(".wav") || nombre.endsWith(".mp3") || nombre.endsWith(".au") || nombre.endsWith(".aiff") || nombre.endsWith(".aif")) {
            abrirReproductorConArchivo(archivo);
            return;
        }

        DialogosWindows.showMessageDialog(this, "No hay una aplicación asociada para este tipo de archivo.", "Abrir archivo", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void abrirEditorConArchivo(File archivo) {
        if (!puedeAbrirOtraVentana("Word")) return;
        JInternalFrame ventana = new JInternalFrame("Word - " + archivo.getName(), true, true, true, true);

        ventana.setFrameIcon(IconosWindows.crear("word", 16));
        ventana.setSize(800, 600);
        ubicarVentanaCentrada(ventana);

        EditorTextoPanel editor = new EditorTextoPanel();

        editor.setAccionCerrar(() -> {
            ventana.dispose();
        });

        ventana.add(editor);

        escritorio.add(ventana, JLayeredPane.PALETTE_LAYER);

        registrarVentanaTaskbar(ventana, "Word");
        activarAjusteVentana(ventana);

        ventana.setVisible(true);

        editor.abrirArchivo(archivo);

        try {
            ventana.setSelected(true);
        } catch (java.beans.PropertyVetoException ignored) {
        }
    }
    
    private void abrirVisorConArchivo(File archivo) {
        if (!puedeAbrirOtraVentana("Imágenes")) return;
        JInternalFrame ventana = new JInternalFrame("Imágenes - " + archivo.getName(), true, true, true, true);

        ventana.setFrameIcon(IconosWindows.crear("imagenes", 16));
        ventana.setSize(980, 650);
        ubicarVentanaCentrada(ventana);

        VisorImagenPanel visor = new VisorImagenPanel(archivo.getParentFile());

        visor.setAccionCerrar(() -> {
            ventana.dispose();
        });

        ventana.add(visor);

        escritorio.add(ventana, JLayeredPane.PALETTE_LAYER);

        registrarVentanaTaskbar(ventana, "Imágenes");
        activarAjusteVentana(ventana);

        ventana.setVisible(true);

        visor.seleccionarImagen(archivo);

        try {
            ventana.setSelected(true);
        } catch (java.beans.PropertyVetoException ignored) {
        }
    }
    
    private void abrirReproductorConArchivo(File archivo) {
        if (!puedeAbrirOtraVentana("Música")) return;
        JInternalFrame ventana = new JInternalFrame("Música - " + archivo.getName(), true, true, true, true);

        ventana.setFrameIcon(IconosWindows.crear("musica", 16));
        ventana.setSize(1000, 650);
        ubicarVentanaCentrada(ventana);

        ReproductorPanel musica = new ReproductorPanel();

        musica.setAccionCerrar(() -> {
            ventana.dispose();
        });

        ventana.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

        ventana.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                musica.detenerReproductor();
            }

            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                musica.detenerReproductor();
            }
        });

        ventana.add(musica);

        escritorio.add(ventana, JLayeredPane.PALETTE_LAYER);

        registrarVentanaTaskbar(ventana, "Música");
        activarAjusteVentana(ventana);

        ventana.setVisible(true);

        musica.reproducirArchivo(archivo);

        try {
            ventana.setSelected(true);
        } catch (java.beans.PropertyVetoException ignored) {
        }
    }


    private void abrirVentana(String nombre){
        if (!puedeAbrirOtraVentana(nombre)) return;

        JInternalFrame ventana = new JInternalFrame(nombre, true, true, true, true);
        ventana.setFrameIcon(IconosWindows.crear(tipoIconoPara(nombre), 16));
        ventana.setBorder(BorderFactory.createLineBorder(new Color(170, 170, 170), 1));
        Dimension tamano = tamanoPredeterminado(nombre);
        ventana.setSize(tamano);
        ubicarVentanaCentrada(ventana);

        if(nombre.equals("CMD")){
            CMDPanel cmd = new CMDPanel();

            cmd.setAccionCerrar(() -> {
                ventana.dispose();
            });
            ventana.add(cmd);
        }
        else if(nombre.equals("Word")){
            EditorTextoPanel editor = new EditorTextoPanel();
            editor.setAccionCerrar(() ->{
                ventana.dispose();
            });
            ventana.add(editor);
        }
        else if(nombre.equals("Música")){
            ReproductorPanel musica = new ReproductorPanel();

            musica.setAccionCerrar(() -> {
                ventana.dispose();
            });

            ventana.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

            ventana.addInternalFrameListener(new InternalFrameAdapter() {
                @Override
                public void internalFrameClosing(InternalFrameEvent e) {
                    musica.detenerReproductor();
                }

                @Override
                public void internalFrameClosed(InternalFrameEvent e) {
                    musica.detenerReproductor();
                }
            });

            ventana.add(musica);
        }
        else if(nombre.equals("Pokémon")){
            VentanaPokemon pokemon = new VentanaPokemon();
            ventana.add(pokemon);
        }
        else if(nombre.equals("Usuarios")){
            if(!Sesion.esAdministrador()){
                DialogosWindows.showMessageDialog(this, "Solo el administrador puede administrar usuarios.", "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                ventana.dispose();
                return;
            }
            AdministrarUsuariosPanel usuarios =new AdministrarUsuariosPanel(gestorUsuarios);
            ventana.add(usuarios);
        }
        else if(nombre.equals("INSTA+")){
            InstaPrincipalPanel insta = new InstaPrincipalPanel();

            insta.setAccionCerrar(() -> {
                ventana.dispose();
            });

            ventana.add(insta);
        }
        else if(nombre.equals("Equipo") || nombre.equals("Archivos")){
            ExploradorPanel explorador = new ExploradorPanel();
            explorador.actualizarExplorador();
            explorador.setAccionCerrar(() -> {
                ventana.dispose();
            });
            explorador.setAccionAbrirArchivo(this::abrirArchivoDesdeExplorador);
            ventana.add(explorador);
        }
        else if(nombre.equals("Configuración")){
            ConfiguracionEscritorioPanel config = new ConfiguracionEscritorioPanel(gestorUsuarios, new ConfiguracionEscritorioPanel.Listener() {
                @Override
                public void seleccionarFondo(File archivo) {
                    aplicarFondoImagen(archivo);
                }

                @Override
                public void seleccionarColor(Color color) {
                    aplicarColorFondo(color);
                }

                @Override
                public void restaurarFondo() {
                    EscritorioPanel.this.restaurarFondo();
                }

                @Override
                public void cuentaFinalizada() {
                    cerrarSesion();
                }
            });
            ventana.add(config);
        }
        else if(nombre.equals("Imágenes")){
            File carpeta = RutasSistema.getImagenesUsuarioActual();
            if (carpeta == null) {
                DialogosWindows.showMessageDialog(this, "No hay una sesión iniciada.", "Imágenes", JOptionPane.WARNING_MESSAGE);
                ventana.dispose();
                return;
            }
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }
            VisorImagenPanel visor = new VisorImagenPanel(carpeta);
            visor.setAccionCerrar(() ->{
                ventana.dispose();
            });
            ventana.add(visor);
        }
        else{
            JPanel contenido = new JPanel(new BorderLayout());
            contenido.setBackground(Color.WHITE);

            JLabel titulo = new JLabel("Aplicación: " + nombre, SwingConstants.CENTER);
            titulo.setFont(new Font("Segoe UI", Font.PLAIN, 22));

            contenido.add(titulo, BorderLayout.CENTER);
            ventana.add(contenido);
        }

        escritorio.add(ventana, JLayeredPane.PALETTE_LAYER);
        registrarVentanaTaskbar(ventana, nombre);
        activarAjusteVentana(ventana);

        ventana.setVisible(true);

        try{
            ventana.setSelected(true);
        }
        catch(java.beans.PropertyVetoException ex){
            ex.printStackTrace();
        }
    }

    private void crearBotonBarraTareas(JInternalFrame ventana, String nombre){
        String tipo = tipoIconoPara(nombre);
        JButton boton = new JButton(IconosWindows.crear(tipo, 26));
        boton.setToolTipText(nombre);
        boton.setPreferredSize(new Dimension(44, 38));
        boton.setMinimumSize(new Dimension(28, 38));
        boton.setMaximumSize(new Dimension(110, 38));
        boton.setFocusPainted(false);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBackground(new Color(24, 24, 24));
        boton.setBorder(BorderFactory.createEmptyBorder(0, 4, 3, 4));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.putClientProperty("estadoActivo", Boolean.FALSE);
        boton.putClientProperty("estadoMinimizado", Boolean.FALSE);
        boton.addMouseListener(new MouseAdapter(){
            @Override public void mouseEntered(MouseEvent e){
                boolean activo = Boolean.TRUE.equals(boton.getClientProperty("estadoActivo"));
                if(!activo) boton.setBackground(new Color(48, 48, 48));
            }
            @Override public void mouseExited(MouseEvent e){
                boolean activo = Boolean.TRUE.equals(boton.getClientProperty("estadoActivo"));
                boolean minimizado = Boolean.TRUE.equals(boton.getClientProperty("estadoMinimizado"));
                marcarBotonTarea(boton, activo, minimizado);
            }
        });
        panelVentanas.add(boton);
        panelVentanas.revalidate();
        panelVentanas.repaint();

        boton.addActionListener(e -> {
            try{
                if(ventana.isIcon()) ventana.setIcon(false);
                ventana.setVisible(true);
                ventana.setSelected(true);
                ventana.toFront();
            } catch(java.beans.PropertyVetoException ex){
                ex.printStackTrace();
            }
        });

        ventana.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter(){
            @Override public void internalFrameIconified(javax.swing.event.InternalFrameEvent e){
                ventana.getDesktopIcon().setVisible(false);
                marcarBotonTarea(boton, false, true);
                escritorio.revalidate();
                escritorio.repaint();
            }
            @Override public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent e){
                ventana.getDesktopIcon().setVisible(false);
                marcarBotonTarea(boton, true, false);
                escritorio.revalidate();
                escritorio.repaint();
            }
            @Override public void internalFrameClosed(javax.swing.event.InternalFrameEvent e){
                panelVentanas.remove(boton);
                panelVentanas.revalidate();
                panelVentanas.repaint();
            }
            @Override public void internalFrameActivated(javax.swing.event.InternalFrameEvent e){
                marcarBotonTarea(boton, true, false);
            }
            @Override public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent e){
                marcarBotonTarea(boton, false, false);
            }
        });
    }

    private void marcarBotonTarea(JButton boton, boolean activo, boolean minimizado){
        boton.putClientProperty("estadoActivo", activo);
        boton.putClientProperty("estadoMinimizado", minimizado);
        if(activo){
            boton.setBackground(new Color(55, 55, 55));
            boton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(0, 120, 215)),
                    BorderFactory.createEmptyBorder(0, 4, 0, 4)
            ));
        } else if(minimizado){
            boton.setBackground(new Color(35, 35, 35));
            boton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(110, 110, 110)),
                    BorderFactory.createEmptyBorder(0, 4, 1, 4)
            ));
        } else {
            boton.setBackground(new Color(24, 24, 24));
            boton.setBorder(BorderFactory.createEmptyBorder(0, 4, 3, 4));
        }
        boton.repaint();
    }

    private Dimension tamanoPredeterminado(String nombre){
        int ancho;
        int alto;
        switch(nombre){
            case "Equipo", "Archivos" -> { ancho = 1050; alto = 650; }
            case "Word" -> { ancho = 900; alto = 650; }
            case "Música" -> { ancho = 1000; alto = 650; }
            case "CMD" -> { ancho = 860; alto = 520; }
            case "INSTA+" -> { ancho = 1180; alto = 720; }
            case "Imágenes" -> { ancho = 980; alto = 650; }
            case "Usuarios" -> { ancho = 780; alto = 540; }
            case "Configuración" -> { ancho = 760; alto = 520; }
            case "Pokémon" -> { ancho = 1100; alto = 700; }
            default -> { ancho = 800; alto = 550; }
        }
        int maxAncho = Math.max(500, escritorio.getWidth() - 50);
        int maxAlto = Math.max(350, escritorio.getHeight() - 50);
        return new Dimension(Math.min(ancho, maxAncho), Math.min(alto, maxAlto));
    }

    private void ubicarVentanaCentrada(JInternalFrame ventana){
        int x = Math.max(10, (escritorio.getWidth() - ventana.getWidth()) / 2);
        int y = Math.max(10, (escritorio.getHeight() - ventana.getHeight()) / 2);
        ventana.setLocation(x, y);
    }

    private String tipoIconoPara(String nombre){
        return switch(nombre){
            case "Equipo" -> "equipo";
            case "Archivos" -> "archivos";
            case "Word" -> "word";
            case "Música" -> "musica";
            case "CMD" -> "cmd";
            case "INSTA+" -> "insta";
            case "Imágenes" -> "imagenes";
            case "Usuarios" -> "usuarios";
            case "Configuración" -> "configuracion";
            case "Pokémon" -> "pokemon";
            default -> "app";
        };
    }

    private void permitirMover(JButton icono){
        MouseAdapter mouse = new MouseAdapter(){
            int offsetX;
            int offsetY;

            @Override
            public void mousePressed(MouseEvent e){
                seleccionarIcono(icono);
                offsetX = e.getX();
                offsetY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e){
                int nuevoX = icono.getX() + e.getX() - offsetX;
                int nuevoY = icono.getY() + e.getY() - offsetY;

                icono.setLocation(nuevoX, nuevoY);
            }

            @Override
            public void mouseReleased(MouseEvent e){
                ajustarACuadricula(icono);
            }
        };

        icono.addMouseListener(mouse);
        icono.addMouseMotionListener(mouse);
    }

    private void ajustarACuadricula(JButton icono){
        int columna = Math.round((float) icono.getX() / anchoCelda);
        int fila = Math.round((float) icono.getY() / altoCelda);

        int x = columna * anchoCelda;
        int y = fila * altoCelda;

        if(x < 0) x = 0;
        if(y < 0) y = 0;

        if(x + anchoCelda > escritorio.getWidth()){
            x = escritorio.getWidth() - anchoCelda;
        }

        if(y + altoCelda > escritorio.getHeight()){
            y = escritorio.getHeight() - altoCelda;
        }

        icono.setLocation(x, y);
    }
    
    private void activarAjusteVentana(JInternalFrame ventana){
        final boolean[] ajustando = {false};

        Timer timer = new Timer(200, e -> {
            if(!ajustando[0]){
                ajustarVentanaAlBorde(ventana, ajustando);
            }
        });

        timer.setRepeats(false);

        ventana.addComponentListener(new java.awt.event.ComponentAdapter(){
            @Override
            public void componentMoved(java.awt.event.ComponentEvent e){
                if(!ajustando[0]){
                    try { if(ventana.isMaximum()) return; } catch(Exception ignored) {}
                    timer.restart();
                }
            }
        });
    }

    private void ajustarVentanaAlBorde(JInternalFrame ventana, boolean[] ajustando){
        try {
            if(ventana.isMaximum()) return;
        } catch(Exception ignored) {
        }
        if(ventana.getWidth() >= escritorio.getWidth() - 40 || ventana.getHeight() >= escritorio.getHeight() - 40) return;
        int margen = 15;
        int ancho = escritorio.getWidth();
        int alto = escritorio.getHeight();

        int x = ventana.getX();
        int derecha = x + ventana.getWidth();
        
        if(x <= margen){
            ajustando[0] = true;
            ventana.setBounds(0, 0, ancho / 2, alto);

            SwingUtilities.invokeLater(() -> {
                ajustando[0] = false;
            });
        }
        else if(derecha >= ancho - margen){
            ajustando[0] = true;
            ventana.setBounds(ancho / 2, 0, ancho / 2, alto);

            SwingUtilities.invokeLater(() -> {
                ajustando[0] = false;
            });
        }
    }
    
    public void actualizarPermisos(){
        if(btnUsuarios == null){
            return;
        }
        btnUsuarios.setVisible(Sesion.esAdministrador());
        escritorio.repaint();
    }
    
    private void crearMenuInicio(){
        menuInicio = new JPanel(new BorderLayout());
        menuInicio.setBackground(new Color(32, 32, 32));
        menuInicio.setBorder(BorderFactory.createLineBorder(new Color(75, 75, 75)));
        menuInicio.setBounds(5, escritorio.getHeight() - 465, 330, 465);

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(new Color(28, 28, 28));
        cabecera.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));
        lblUsuarioMenu = new JLabel(Sesion.haySesion() ? Sesion.getNombreUsuario() : "Usuario", IconosWindows.crear("usuarios", 24), SwingConstants.LEFT);
        lblUsuarioMenu.setForeground(Color.WHITE);
        lblUsuarioMenu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUsuarioMenu.setIconTextGap(10);
        cabecera.add(lblUsuarioMenu, BorderLayout.WEST);
        menuInicio.add(cabecera, BorderLayout.NORTH);

        JPanel aplicaciones = new JPanel();
        aplicaciones.setLayout(new BoxLayout(aplicaciones, BoxLayout.Y_AXIS));
        aplicaciones.setBackground(new Color(32, 32, 32));

        JButton btnExplorador = crearBotonMenu("Explorador de archivos", "archivos");
        JButton btnWord = crearBotonMenu("Word", "word");
        JButton btnCMD = crearBotonMenu("Símbolo del sistema", "cmd");
        JButton btnMusica = crearBotonMenu("Música", "musica");
        JButton btnInsta = crearBotonMenu("INSTA+", "insta");
        JButton btnPokemon = crearBotonMenu("Pokémon", "pokemon");
        JButton btnConfig = crearBotonMenu("Configuración", "configuracion");
        for(JButton b : new JButton[]{btnExplorador, btnWord, btnCMD, btnMusica, btnInsta, btnPokemon, btnConfig}) aplicaciones.add(b);
        menuInicio.add(aplicaciones, BorderLayout.CENTER);

        JPanel inferior = new JPanel(new GridLayout(1, 2, 4, 0));
        inferior.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        inferior.setBackground(new Color(28, 28, 28));
        JButton btnCerrarSesion = crearBotonMenu("Cerrar sesión", "usuarios");
        JButton btnApagar = crearBotonMenu("Apagar", "app");
        inferior.add(btnCerrarSesion);
        inferior.add(btnApagar);
        menuInicio.add(inferior, BorderLayout.SOUTH);

        btnCerrarSesion.addActionListener(e -> mostrarConfirmacionCerrarSesion());
        btnApagar.addActionListener(e -> mostrarConfirmacionApagar());
        btnExplorador.addActionListener(e -> { abrirVentana("Archivos"); ocultarMenuInicio(); });
        btnWord.addActionListener(e -> { abrirVentana("Word"); ocultarMenuInicio(); });
        btnCMD.addActionListener(e -> { abrirVentana("CMD"); ocultarMenuInicio(); });
        btnMusica.addActionListener(e -> { abrirVentana("Música"); ocultarMenuInicio(); });
        btnInsta.addActionListener(e -> { abrirVentana("INSTA+"); ocultarMenuInicio(); });
        btnPokemon.addActionListener(e -> { abrirVentana("Pokémon"); ocultarMenuInicio(); });
        btnConfig.addActionListener(e -> { abrirVentana("Configuración"); ocultarMenuInicio(); });
        menuInicio.setVisible(false);
        escritorio.add(menuInicio, JLayeredPane.POPUP_LAYER);
    }

    private JButton crearBotonMenu(String texto, String tipo){
        JButton boton = new JButton(texto, IconosWindows.crear(tipo, 22));
        boton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        boton.setForeground(Color.WHITE);
        boton.setBackground(new Color(32, 32, 32));
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setIconTextGap(12);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.addMouseListener(new MouseAdapter(){
            @Override public void mouseEntered(MouseEvent e){ boton.setBackground(new Color(58, 58, 58)); }
            @Override public void mouseExited(MouseEvent e){ boton.setBackground(new Color(32, 32, 32)); }
        });
        return boton;
    }

    private void mostrarOcultarMenuInicio(){
        if(menuInicio == null){
            return;
        }
        if (lblUsuarioMenu != null) {
            lblUsuarioMenu.setText(Sesion.haySesion() ? Sesion.getNombreUsuario() : "Usuario");
        }
        int x = 5;
        int y = escritorio.getHeight() - menuInicio.getHeight();
        
        menuInicio.setLocation(x, y);
        menuInicio.setVisible(!menuInicio.isVisible());
        btnWindows.setBackground(menuInicio.isVisible() ? new Color(55, 55, 55) : new Color(24, 24, 24));

        if(menuInicio.isVisible()){
            escritorio.moveToFront(menuInicio);
        }
    }
    
    private void ocultarMenuInicio(){
        if(menuInicio != null){
            menuInicio.setVisible(false);
            if(btnWindows != null) btnWindows.setBackground(new Color(24, 24, 24));
        }
    }
    
    private void mostrarConfirmacionApagar(){
        ocultarMenuInicio();
        JPanel fondo = new JPanel(null);
        fondo.setBackground(new Color(0, 0, 0, 170));
        fondo.setBounds(0, 0, escritorio.getWidth(), escritorio.getHeight());

        JPanel ventana = new JPanel();
        ventana.setLayout(new BorderLayout(10, 20));
        ventana.setBackground(new Color(35, 35, 35));
        ventana.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));

        int ancho = 400;
        int alto = 180;
        int x = (escritorio.getWidth() - ancho) / 2;
        int y = (escritorio.getHeight() - alto) / 2;
        ventana.setBounds(x, y, ancho, alto);

        JLabel mensaje = new JLabel("¿Desea apagar el sistema?", SwingConstants.CENTER);
        mensaje.setForeground(Color.WHITE);
        mensaje.setFont(new Font("Segoe UI", Font.PLAIN, 19));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        botones.setOpaque(false);

        JButton btnCancelar = new JButton("Cancelar");
        JButton btnAceptar = new JButton("Apagar");

        botones.add(btnCancelar);
        botones.add(btnAceptar);

        ventana.add(mensaje, BorderLayout.CENTER);
        ventana.add(botones, BorderLayout.SOUTH);
        fondo.add(ventana);

        escritorio.add(fondo, JLayeredPane.DRAG_LAYER);
        escritorio.moveToFront(fondo);
        fondo.setVisible(true);

        btnCancelar.addActionListener(e -> {
            escritorio.remove(fondo);
            escritorio.repaint();
        });

        btnAceptar.addActionListener(e -> {
            System.exit(0);
        });
    }
    
    private void mostrarConfirmacionCerrarSesion(){
        ocultarMenuInicio();
        JPanel fondo = new JPanel(null);
        fondo.setBackground(new Color(0, 0, 0, 170));
        fondo.setBounds(0, 0, escritorio.getWidth(), escritorio.getHeight());

        JPanel ventana = new JPanel(new BorderLayout(10, 20));
        ventana.setBackground(new Color(35, 35, 35));
        ventana.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));

        int ancho = 400;
        int alto = 180;
        int x = (escritorio.getWidth() - ancho) / 2;
        int y = (escritorio.getHeight() - alto) / 2;
        ventana.setBounds(x, y, ancho, alto);

        JLabel mensaje = new JLabel("¿Desea cerrar la sesión?", SwingConstants.CENTER);
        mensaje.setForeground(Color.WHITE);
        mensaje.setFont(new Font("Segoe UI", Font.PLAIN, 19));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        botones.setOpaque(false);

        JButton btnCancelar = new JButton("Cancelar");
        JButton btnAceptar = new JButton("Cerrar sesión");

        botones.add(btnCancelar);
        botones.add(btnAceptar);

        ventana.add(mensaje, BorderLayout.CENTER);
        ventana.add(botones, BorderLayout.SOUTH);
        fondo.add(ventana);

        escritorio.add(fondo, JLayeredPane.DRAG_LAYER);
        escritorio.moveToFront(fondo);

        btnCancelar.addActionListener(e -> {
            escritorio.remove(fondo);
            escritorio.repaint();
        });

        btnAceptar.addActionListener(e -> {
            escritorio.remove(fondo);
            cerrarSesion();
        });
    }
    
    private File archivoConfiguracionFondo() {
        File carpeta = RutasSistema.getCarpetaUsuarioActual();
        if (carpeta == null) return null;
        if (!carpeta.exists()) carpeta.mkdirs();
        return new File(carpeta, ".escritorio.properties");
    }

    private void cargarFondoGuardado() {
        if (!(escritorio instanceof EscritorioWindowsPane pane)) return;
        File config = archivoConfiguracionFondo();
        if (config == null || !config.exists()) {
            pane.setColorFondo(new Color(18, 56, 92));
            return;
        }
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(config)) {
            props.load(in);
            String tipo = props.getProperty("tipo", "color");
            if ("imagen".equals(tipo)) {
                File imagen = new File(props.getProperty("ruta", ""));
                if (imagen.exists()) {
                    pane.setFondoArchivo(imagen);
                    return;
                }
            }
            pane.setColorFondo(new Color(Integer.parseInt(props.getProperty("color", String.valueOf(new Color(18, 56, 92).getRGB()))), true));
        } catch (Exception ex) {
            pane.setColorFondo(new Color(18, 56, 92));
        }
    }

    private void guardarPropiedadesFondo(Properties props) {
        File config = archivoConfiguracionFondo();
        if (config == null) return;
        try (FileOutputStream out = new FileOutputStream(config)) {
            props.store(out, "Personalización MiniWindows");
        } catch (Exception ex) {
            DialogosWindows.showMessageDialog(this, "No se pudo guardar la personalización: " + ex.getMessage(), "Personalización", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void aplicarFondoImagen(File archivo) {
        if (archivo == null || !(escritorio instanceof EscritorioWindowsPane pane)) {
            return;
        }

        try {
            File carpetaImagenes = RutasSistema.getImagenesUsuarioActual();

            if (carpetaImagenes == null) {
                return;
            }

            if (!carpetaImagenes.exists()) {
                carpetaImagenes.mkdirs();
            }

            String ext = archivo.getName().contains(".")
                    ? archivo.getName().substring(archivo.getName().lastIndexOf('.'))
                    : ".jpg";

            String nombreFondo = "FondoEscritorio_" + System.currentTimeMillis() + ext;

            File copia = new File(carpetaImagenes, nombreFondo);

            Files.copy(
                    archivo.toPath(),
                    copia.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );

            Properties props = new Properties();
            props.setProperty("tipo", "imagen");
            props.setProperty("ruta", copia.getAbsolutePath());

            guardarPropiedadesFondo(props);

            pane.setFondoArchivo(copia);

            pane.revalidate();
            pane.repaint();

            escritorio.revalidate();
            escritorio.repaint();

        } catch (Exception ex) {
            DialogosWindows.showMessageDialog(
                    this,
                    "No se pudo establecer el fondo: " + ex.getMessage(),
                    "Personalización",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void aplicarColorFondo(Color color) {
        if (color == null || !(escritorio instanceof EscritorioWindowsPane pane)) return;
        Properties props = new Properties();
        props.setProperty("tipo", "color");
        props.setProperty("color", String.valueOf(color.getRGB()));
        guardarPropiedadesFondo(props);
        pane.setColorFondo(color);
    }

    private void restaurarFondo() {
        File config = archivoConfiguracionFondo();
        if (config != null && config.exists()) config.delete();
        if (escritorio instanceof EscritorioWindowsPane pane) pane.setColorFondo(new Color(18, 56, 92));
    }

    private void cerrarSesion() {
        for (JInternalFrame ventana : escritorio.getAllFrames()) {
            ventana.dispose();
        }
        panelVentanas.removeAll();
        panelVentanas.revalidate();
        panelVentanas.repaint();
        if (menuInicio != null) menuInicio.setVisible(false);

        LoginPanel loginPanel = buscarLoginPanel(contenedor);

        if (loginPanel != null) {
            loginPanel.limpiarCampos();
        }

        Sesion.cerrarSesion();

        transicion.show(contenedor, "LOGIN");
    }
    
    private LoginPanel buscarLoginPanel(Container contenedor) {
        for (Component componente : contenedor.getComponents()) {
            if (componente instanceof LoginPanel loginPanel) {
                return loginPanel;
            }

            if (componente instanceof Container interno) {
                LoginPanel encontrado = buscarLoginPanel(interno);

                if (encontrado != null) {
                    return encontrado;
                }
            }
        }

        return null;
    }
}