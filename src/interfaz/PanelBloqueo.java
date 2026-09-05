
package interfaz;

import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;

public class PanelBloqueo extends JPanel{
    private PantallaInicioSesion pantalla;
    private JLabel hora;
    private JLabel fecha;
    private int posicionY = 0;
    private int inicioMouseY;
    private boolean arrastrando = false;
    private boolean huboArrastre = false;
    private JButton btnApagar;
    private ImageIcon iconoApagar = new ImageIcon(getClass().getResource("/Imagenes/botonApagar.png")); 
    
    public PanelBloqueo(PantallaInicioSesion pantalla){
        this.pantalla = pantalla;        
        setLayout(new BorderLayout());
        setOpaque(false);
        
        JPanel centro = new JPanel();
        centro.setOpaque(false);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        
        hora = new JLabel("", SwingConstants.CENTER);
        fecha = new JLabel("", SwingConstants.CENTER);
        
        hora.setForeground(Color.WHITE);
        fecha.setForeground(Color.WHITE);
        
        hora.setFont(new Font("Segoe UI", Font.PLAIN, 90));
        fecha.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        
        hora.setAlignmentX(CENTER_ALIGNMENT);
        fecha.setAlignmentX(CENTER_ALIGNMENT);
        
        centro.add(Box.createVerticalStrut(120));
        centro.add(hora);
        centro.add(fecha);
        centro.add(Box.createVerticalGlue());
        add(centro, BorderLayout.CENTER);
        
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 30));
        panelInferior.setOpaque(false);
        crearBotonApagado();
        panelInferior.add(btnApagar);
        add(panelInferior, BorderLayout.SOUTH);
        
        actualizarHora();
        Timer reloj = new Timer(1000, e -> actualizarHora());
        reloj.start();
        acciones();
        
        
    }
    
    private void crearBotonApagado(){
        Image imagenEscalada = iconoApagar.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        btnApagar = new JButton(new ImageIcon(imagenEscalada));
        btnApagar.setContentAreaFilled(false);
        btnApagar.setBorderPainted(false);
        btnApagar.setFocusPainted(false);
        btnApagar.setOpaque(false);
        
        btnApagar.addActionListener(e ->{
            System.exit(0);
        });
      
        btnApagar.setCursor(
            new Cursor(Cursor.HAND_CURSOR)
        );
        
        btnApagar.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent e){
                btnApagar.setContentAreaFilled(true);
                btnApagar.setBackground(new Color(255, 255, 255, 50));
            }
            @Override
            public void mouseExited(MouseEvent e){
                btnApagar.setContentAreaFilled(false);
            }
        });
    }
    
    private void actualizarHora(){
        Date ahora = new Date();
        hora.setText(new SimpleDateFormat("HH:mm").format(ahora));
        fecha.setText(new SimpleDateFormat("EEEE, d MMMM").format(ahora));     
    }
    
    private void acciones(){  
        addMouseListener(new MouseAdapter(){
           @Override
           public void mousePressed(MouseEvent e){
               inicioMouseY = e.getY();
               arrastrando = true;
               huboArrastre = false;
           }
           
           @Override 
           public void mouseReleased(MouseEvent e){
               arrastrando = false;
               if(!huboArrastre){
                   deslizarArriba();
                   return;
               }
               if(posicionY < -180){
                   deslizarCompleto();
               }
               else{
                   regresarAbajo();
               }
           }
        });
        
        addMouseMotionListener(new MouseMotionAdapter(){
            @Override
            public void mouseDragged(MouseEvent e){
                if(!arrastrando){
                    return;
                }
                int diferencia = e.getY() - inicioMouseY;
                if(diferencia < 0){
                    huboArrastre = true;
                    posicionY = diferencia;
                    if(posicionY <= -getHeight()){
                        pantalla.mostrarLogin();
                    }
                    else{
                        pantalla.ocultarLogin();
                    }
                    pantalla.actualizarAnimacion();
                }
            }
        });
    }
    
    public int getPosicionY(){
        return posicionY;
    }
    
    private void deslizarArriba(){
        Timer animacion = new Timer(8, null);
        animacion.addActionListener(e -> {
            posicionY -= 25;
            if(posicionY <= -getHeight()){
                pantalla.mostrarLogin();
            }
            if(posicionY <= -getHeight()){
                posicionY = -getHeight();
                animacion.stop();
            }
            pantalla.actualizarAnimacion();
        });
        animacion.start();
    }
    
    private void regresarAbajo(){
        Timer timer = new Timer(8, null);
        timer.addActionListener(e -> {
            posicionY += 20;
            if(posicionY >= 0){
                posicionY = 0;
                timer.stop();
                pantalla.ocultarLogin();
            }
            pantalla.actualizarAnimacion();
        });
        timer.start();
    }
    
    private void deslizarCompleto(){
        Timer timer = new Timer(8, null);
        timer.addActionListener(e -> {
            posicionY -= 20;
            if(posicionY <= -getHeight()){
                pantalla.mostrarLogin();
            }
            if(posicionY <= -getHeight()){
                posicionY = - getHeight();
                timer.stop();
            }
            pantalla.actualizarAnimacion();
        });
        timer.start();
    }
}
