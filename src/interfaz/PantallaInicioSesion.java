
package interfaz;

import java.awt.*;
import javax.swing.*;
import sistema.*;

public class PantallaInicioSesion extends JPanel{
    private LoginPanel login;
    private PanelBloqueo bloqueo;
    private Image imagenFondo;
    private GestorUsuarios gestorUsuarios;
    
    public PantallaInicioSesion(JPanel contenedor, CardLayout transicion, GestorUsuarios gestorUsuarios){
        this.gestorUsuarios = gestorUsuarios;
        setLayout(null);
        imagenFondo = new ImageIcon(getClass().getResource("/Imagenes/FondoLogin.png")).getImage();
        bloqueo = new PanelBloqueo(this);
        login = new LoginPanel(contenedor, transicion, gestorUsuarios);
        
        login.setVisible(false);
        
        add(login);   
        add(bloqueo); 
                
        setComponentZOrder(bloqueo, 0);
        setComponentZOrder(login, 1);
    }
    
    @Override 
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
    }
    
    @Override
    public void doLayout(){
        int w = getWidth();
        int h = getHeight();
        login.setBounds(0, 0, w, h);
        bloqueo.setBounds(0, bloqueo.getPosicionY(), w, h);
    }
    
    public void actualizarAnimacion(){
        doLayout();
        repaint();
    }
    
    public void mostrarLogin(){
        login.setVisible(true);
    }
    
    public void ocultarLogin(){
        login.setVisible(false);
    }
}
