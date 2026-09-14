
package interfaz;

import java.awt.*;
import javax.swing.*;
import sistema.*;

public class VentanaPrincipal extends JFrame {

    private JPanel contenedor;
    private CardLayout transicion;
    private GestorUsuarios gestorUsuarios;

    public VentanaPrincipal() {
        setTitle("Mini Windows");

        // Quita la barra superior de Windows para que no pueda moverse
        setUndecorated(true);

        // Hace que la ventana ocupe toda la pantalla
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(pantalla);
        setLocation(0, 0);

        // Impide cambiar el tamaño
        setResizable(false);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gestorUsuarios = new GestorUsuarios();
        transicion = new CardLayout();
        contenedor = new JPanel(transicion);

        crearPaneles();

        add(contenedor);
        transicion.show(contenedor, "LOGIN");

        setVisible(true);
    }

    private void crearPaneles() {
        PantallaInicioSesion pantalla = new PantallaInicioSesion(contenedor, transicion, gestorUsuarios);
        EscritorioPanel escritorio = new EscritorioPanel(contenedor, transicion, gestorUsuarios);

        contenedor.add(pantalla, "LOGIN");
        contenedor.add(escritorio, "ESCRITORIO");
    }
}