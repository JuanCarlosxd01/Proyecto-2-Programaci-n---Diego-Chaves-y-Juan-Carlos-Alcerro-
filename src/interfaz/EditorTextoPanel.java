package interfaz;

import EditorTexto.ControladorEditor;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.io.File;

public class EditorTextoPanel extends JPanel {
    private JTextPane areaTexto;
    private JComboBox<String> cmbFuente;
    private JComboBox<Integer> cmbTamano;
    private JButton btnColor;
    private JToggleButton btnNegrita;
    private JToggleButton btnCursiva;
    private JToggleButton btnSubrayado;
    private JToggleButton btnTachado;
    private JMenuItem itemNuevo;
    private JMenuItem itemAbrir;
    private JMenuItem itemGuardar;
    private JMenuItem itemGuardarComo;
    private JPanel panelSuperior;
    private Runnable accionCerrar;
    private ControladorEditor controlador;

    private static final Color AZUL_WORD = new Color(43, 87, 154);
    private static final Color HOVER = new Color(225, 235, 247);
    private static final Color SELECCION = new Color(207, 226, 250);

    public EditorTextoPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(235, 235, 235));
        panelSuperior = new JPanel();
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));
        panelSuperior.setBackground(Color.WHITE);
        crearMenu();
        crearBarraHerramientas();
        crearAreaTexto();
        add(panelSuperior, BorderLayout.NORTH);
        controlador = new ControladorEditor(this);
    }
    
    public void abrirArchivo(File archivo) {
        if (controlador != null) {
            controlador.abrirArchivo(archivo);
        }
    }

    private void crearMenu() {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(AZUL_WORD);
        cabecera.setBorder(new EmptyBorder(6, 12, 6, 12));
        JLabel nombre = new JLabel("Documento - Word");
        nombre.setForeground(Color.WHITE);
        nombre.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cabecera.add(nombre, BorderLayout.WEST);
        panelSuperior.add(cabecera);

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(215, 215, 215)));
        JMenu menuArchivo = new JMenu("Archivo");
        JMenu menuInicio = new JMenu("Inicio");
        JMenu menuInsertar = new JMenu("Insertar");
        JMenu menuDiseno = new JMenu("Diseño");
        JMenu menuVista = new JMenu("Vista");

        itemNuevo = new JMenuItem("Nuevo");
        itemAbrir = new JMenuItem("Abrir");
        itemGuardar = new JMenuItem("Guardar");
        itemGuardarComo = new JMenuItem("Guardar como");
        menuArchivo.add(itemNuevo);
        menuArchivo.add(itemAbrir);
        menuArchivo.addSeparator();
        menuArchivo.add(itemGuardar);
        menuArchivo.add(itemGuardarComo);

        menuBar.add(menuArchivo);
        menuBar.add(menuInicio);
        menuBar.add(menuInsertar);
        menuBar.add(menuDiseno);
        menuBar.add(menuVista);
        panelSuperior.add(menuBar);
    }

    private void crearBarraHerramientas() {
        JPanel cinta = new JPanel(new BorderLayout());
        cinta.setBackground(Color.WHITE);
        cinta.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 210, 210)), new EmptyBorder(8, 10, 8, 10)));

        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        barra.setOpaque(false);
        barra.setBorder(null);

        String[] fuentes = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        cmbFuente = new JComboBox<>(fuentes);
        cmbFuente.setPreferredSize(new Dimension(155, 30));
        cmbFuente.setMaximumSize(new Dimension(155, 30));
        cmbFuente.setSelectedItem("Segoe UI");

        Integer[] tamanos = {8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 28, 32, 36, 48, 72};
        cmbTamano = new JComboBox<>(tamanos);
        cmbTamano.setSelectedItem(12);
        cmbTamano.setPreferredSize(new Dimension(62, 30));
        cmbTamano.setMaximumSize(new Dimension(62, 30));

        btnColor = new JButton("A");
        btnColor.setToolTipText("Color de fuente");
        btnNegrita = new JToggleButton("N");
        btnCursiva = new JToggleButton("K");
        btnSubrayado = new JToggleButton("S");
        btnTachado = new JToggleButton("abc");
        btnNegrita.setToolTipText("Negrita");
        btnCursiva.setToolTipText("Cursiva");
        btnSubrayado.setToolTipText("Subrayado");
        btnTachado.setToolTipText("Tachado");
        btnNegrita.setFont(btnNegrita.getFont().deriveFont(Font.BOLD));
        btnCursiva.setFont(btnCursiva.getFont().deriveFont(Font.ITALIC));
        btnSubrayado.setFont(btnSubrayado.getFont().deriveFont(Font.PLAIN));

        estilizarBoton(btnColor);
        estilizarToggle(btnNegrita);
        estilizarToggle(btnCursiva);
        estilizarToggle(btnSubrayado);
        estilizarToggle(btnTachado);

        JLabel grupoFuente = new JLabel("Fuente");
        grupoFuente.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        grupoFuente.setForeground(new Color(95, 95, 95));

        barra.add(cmbFuente);
        barra.add(Box.createHorizontalStrut(5));
        barra.add(cmbTamano);
        barra.addSeparator(new Dimension(12, 28));
        barra.add(btnNegrita);
        barra.add(btnCursiva);
        barra.add(btnSubrayado);
        barra.add(btnTachado);
        barra.add(Box.createHorizontalStrut(5));
        barra.add(btnColor);
        barra.add(Box.createHorizontalStrut(10));
        barra.add(grupoFuente);

        cinta.add(barra, BorderLayout.WEST);
        panelSuperior.add(cinta);
    }

    private void estilizarBoton(AbstractButton boton) {
        boton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        boton.setPreferredSize(new Dimension(38, 30));
        boton.setMaximumSize(new Dimension(45, 30));
        boton.setFocusPainted(false);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        boton.setBackground(Color.WHITE);
        boton.setForeground(new Color(30, 30, 30));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!(boton instanceof JToggleButton toggle) || !toggle.isSelected()) boton.setBackground(HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (boton instanceof JToggleButton toggle && toggle.isSelected()) boton.setBackground(SELECCION);
                else boton.setBackground(Color.WHITE);
            }
        });
    }

    private void estilizarToggle(JToggleButton boton) {
        estilizarBoton(boton);
        boton.addItemListener(e -> {
            if (boton.isSelected()) {
                boton.setBackground(SELECCION);
                boton.setBorder(BorderFactory.createLineBorder(new Color(120, 170, 225)));
            } else {
                boton.setBackground(Color.WHITE);
                boton.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            }
        });
    }

    private void crearAreaTexto() {
        JPanel superficie = new JPanel(new GridBagLayout());
        superficie.setBackground(new Color(226, 226, 226));
        superficie.setBorder(new EmptyBorder(18, 18, 18, 18));

        areaTexto = new JTextPane();
        areaTexto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        areaTexto.setMargin(new Insets(42, 55, 42, 55));
        areaTexto.setBackground(Color.WHITE);
        areaTexto.setForeground(Color.BLACK);
        areaTexto.setCaretColor(Color.BLACK);

        JScrollPane pagina = new JScrollPane(areaTexto);
        pagina.setPreferredSize(new Dimension(720, 900));
        pagina.setMinimumSize(new Dimension(450, 350));
        pagina.setBorder(BorderFactory.createLineBorder(new Color(190, 190, 190)));
        pagina.getVerticalScrollBar().setUnitIncrement(16);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;
        superficie.add(pagina, gbc);
        add(superficie, BorderLayout.CENTER);
    }

    public JTextPane getAreaTexto() { return areaTexto; }
    public JComboBox<String> getCmbFuente() { return cmbFuente; }
    public JComboBox<Integer> getCmbTamano() { return cmbTamano; }
    public JButton getBtnColor() { return btnColor; }
    public JToggleButton getBtnNegrita() { return btnNegrita; }
    public JToggleButton getBtnCursiva() { return btnCursiva; }
    public JToggleButton getBtnSubrayado() { return btnSubrayado; }
    public JToggleButton getBtnTachado() { return btnTachado; }
    public JMenuItem getItemNuevo() { return itemNuevo; }
    public JMenuItem getItemAbrir() { return itemAbrir; }
    public JMenuItem getItemGuardar() { return itemGuardar; }
    public JMenuItem getItemGuardarComo() { return itemGuardarComo; }
    public void setAccionCerrar(Runnable accionCerrar) { this.accionCerrar = accionCerrar; }
    public void cerrar() { if (accionCerrar != null) accionCerrar.run(); }
}
