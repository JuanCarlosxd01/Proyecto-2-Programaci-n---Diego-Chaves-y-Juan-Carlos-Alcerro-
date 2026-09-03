
package interfaz;

import java.awt.*;
import javax.swing.*;

public class VentanaPrincipal extends JFrame{
    
    private JPanel contenedor;
    private CardLayout transicion;
    
    public VentanaPrincipal(){
        setTitle("Mini Windows");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        transicion = new CardLayout();
        contenedor = new JPanel(transicion);  
        
        crearPaneles();
        add(contenedor);
        transicion.show(contenedor, "LOGIN");
        setVisible(true);
    }
    
    private void crearPaneles(){
        LoginPanel login = new LoginPanel(contenedor, transicion);
        /*EscritorioPanel escritorio = new EscritorioPanel(contenedor, transicion);
        ExploradorPanel explorador = new ExploradorPanel(contenedor, transicion);
        EditorTextoPanel editor = new EditorTextoPanel(contenedor, transicion);
        ReproductorPanel reproductor = new ReproductorPanel(contenedor, transicion);
        VisorImagenPanel visor = new VisorImagenPanel(contenedor, transicion);*/
        
        contenedor.add(login, "LOGIN");
        /*contenedor.add(escritorio, "ESCRITORIO");
        contenedor.add(explorador, "EXPLORADOR");
        contenedor.add(editor, "EDITOR");
        contenedor.add(reproductor, "REPRODUCTOR");
        contenedor.add(visor, "VISOR");*/
    }
}
