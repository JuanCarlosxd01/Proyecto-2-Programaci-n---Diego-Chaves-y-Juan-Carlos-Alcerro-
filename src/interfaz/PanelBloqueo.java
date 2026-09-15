
package interfaz;

import java.awt.*;
import static java.awt.Component.CENTER_ALIGNMENT;
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
    
    private void crearBotonApagado() {
        btnApagar = new JButton("⏻") {
            private boolean mouseEncima = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        mouseEncima = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        mouseEncima = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                if (mouseEncima) {
                    g2.setColor(new Color(255, 255, 255, 45));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnApagar.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 28));
        btnApagar.setForeground(Color.WHITE);

        btnApagar.setPreferredSize(new Dimension(50, 50));
        btnApagar.setMinimumSize(new Dimension(50, 50));
        btnApagar.setMaximumSize(new Dimension(50, 50));

        btnApagar.setHorizontalAlignment(SwingConstants.CENTER);
        btnApagar.setVerticalAlignment(SwingConstants.CENTER);
        btnApagar.setMargin(new Insets(0, 0, 3, 0));

        btnApagar.setContentAreaFilled(false);
        btnApagar.setBorderPainted(false);
        btnApagar.setFocusPainted(false);
        btnApagar.setFocusable(false);
        btnApagar.setOpaque(false);

        btnApagar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnApagar.setToolTipText("Apagar");

        btnApagar.addActionListener(e -> {
            System.exit(0);
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
