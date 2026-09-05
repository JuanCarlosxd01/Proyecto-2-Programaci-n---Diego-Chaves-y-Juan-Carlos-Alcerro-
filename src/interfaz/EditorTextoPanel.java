
package interfaz;

import EditorTexto.ControladorEditor;
import java.awt.*;
import javax.swing.*;

public class EditorTextoPanel extends JPanel{
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
    
    public EditorTextoPanel(){
        setLayout(new BorderLayout());
        panelSuperior = new JPanel();
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));
        crearMenu();
        crearBarraHerramientas();
        crearAreaTexto();
        add(panelSuperior, BorderLayout.NORTH);
        new ControladorEditor(this);
    }
    
    private void crearMenu(){
        JMenuBar menuBar = new JMenuBar();
        JMenu menuArchivo = new JMenu("Archivo");
        
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
        
        panelSuperior.add(menuBar);
    }
    
    private void crearBarraHerramientas(){
        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        String[] fuentes = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        cmbFuente = new JComboBox<>(fuentes);
        cmbFuente.setPreferredSize(new Dimension(150, 30));
        
        Integer[] tamanos = {8, 10, 12, 14, 16, 18, 20, 22, 24, 28, 32, 36 ,48};
        cmbTamano = new JComboBox<>(tamanos);
        cmbTamano.setSelectedItem(12);
        
        btnColor = new JButton("Color");
        btnNegrita = new JToggleButton("N");
        btnCursiva = new JToggleButton("C");
        btnSubrayado = new JToggleButton("S");
        btnTachado = new JToggleButton("T");
        
        btnNegrita.setFont(btnNegrita.getFont().deriveFont(Font.BOLD));
        btnCursiva.setFont(btnCursiva.getFont().deriveFont(Font.ITALIC));
        
        barra.add(new JLabel("Fuente: "));
        barra.add(cmbFuente);
        barra.addSeparator();
        
        barra.add(new JLabel("Tamaño: "));
        barra.add(cmbTamano);
        barra.addSeparator();
        
        barra.add(btnNegrita);
        barra.add(btnCursiva);
        barra.add(btnSubrayado);
        barra.add(btnTachado);
        barra.addSeparator();
        
        barra.add(btnColor);
        panelSuperior.add(barra);
    }
    
    private void crearAreaTexto(){
        areaTexto = new JTextPane();
        areaTexto.setFont(new Font("Arial", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(areaTexto);
        add(scroll, BorderLayout.CENTER);
    }

    public JTextPane getAreaTexto() {
        return areaTexto;
    }

    public JComboBox<String> getCmbFuente() {
        return cmbFuente;
    }

    public JComboBox<Integer> getCmbTamano() {
        return cmbTamano;
    }

    public JButton getBtnColor() {
        return btnColor;
    }

    public JToggleButton getBtnNegrita() {
        return btnNegrita;
    }

    public JToggleButton getBtnCursiva() {
        return btnCursiva;
    }

    public JToggleButton getBtnSubrayado() {
        return btnSubrayado;
    }

    public JToggleButton getBtnTachado() {
        return btnTachado;
    }

    public JMenuItem getItemNuevo() {
        return itemNuevo;
    }

    public JMenuItem getItemAbrir() {
        return itemAbrir;
    }

    public JMenuItem getItemGuardar() {
        return itemGuardar;
    }

    public JMenuItem getItemGuardarComo() {
        return itemGuardarComo;
    }
    
    public void setAccionCerrar(Runnable accionCerrar){
        this.accionCerrar = accionCerrar;
    }
    
    public void cerrar(){
        if(accionCerrar != null){
            accionCerrar.run();
        }
    }
    
    
}
