
package interfaz;

import java.awt.*;
import javax.swing.*;
import sistema.*;

public class VentanaPrincipal extends JFrame{
    
    private JPanel contenedor;
    private CardLayout transicion;
    private GestorUsuarios gestorUsuarios;
    
    public VentanaPrincipal(){
        setTitle("Mini Windows");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        gestorUsuarios = new GestorUsuarios();
        transicion = new CardLayout();
        contenedor = new JPanel(transicion);  
        
        crearPaneles();
        add(contenedor);
        transicion.show(contenedor, "LOGIN");
        setVisible(true);
    }
    
    private void crearPaneles(){
        PantallaInicioSesion pantalla = new PantallaInicioSesion(contenedor, transicion, gestorUsuarios);
        EscritorioPanel escritorio = new EscritorioPanel(contenedor, transicion, gestorUsuarios);

        contenedor.add(pantalla, "LOGIN");
        contenedor.add(escritorio, "ESCRITORIO");
    }
}
