
package insta.interfaz;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class InstaPanel extends JPanel{
    private CardLayout transicion;
    private JPanel panelContenido;
    
    private JButton btnTimeline;
    private JButton btnPerfil;
    private JButton btnPublicar;
    private JButton btnInteracciones;
    private JButton btnBuscar;
    private JButton btnHashtag;
    private JButton btnInbox;
    private JButton btnEditarPerfil;
    private JButton btnCerrarSesion;
    private Runnable accionCerrar;
    
    private JLabel lblUsuario;
    
    public InstaPanel(){
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        crearEncabezado();
        crearMenuLateral();
        crearContenido();
    }
    
    private void crearEncabezado(){
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Color.WHITE);
        encabezado.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JLabel titulo = new JLabel("INSTA+");
        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        
        lblUsuario = new JLabel("@usuario");
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 14));
        
        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(lblUsuario, BorderLayout.EAST);
        
        add(encabezado, BorderLayout.NORTH);
    }
    
    private void crearMenuLateral(){
        JPanel menu = new JPanel();
        menu.setPreferredSize(new Dimension(200, 0));
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(new Color(250, 250, 250));
        menu.setBorder(new EmptyBorder(20, 15, 20, 15));
        
        btnTimeline = crearBoton("Inicio");
        btnPerfil = crearBoton("Perfil");
        btnPublicar = crearBoton("Nueva publicación");
        btnInteracciones = crearBoton("Interacciones");
        btnBuscar = crearBoton("Buscar perfil");
        btnHashtag = crearBoton("Buscar hashtag");
        btnInbox = crearBoton("Inbox");
        btnEditarPerfil = crearBoton("Editar perfil");
        btnCerrarSesion = crearBoton("Cerrar sesión");
        
        menu.add(btnTimeline);
        menu.add(Box.createVerticalStrut(8));
        menu.add(btnPerfil);
        menu.add(Box.createVerticalStrut(8));
        menu.add(btnPublicar);
        menu.add(Box.createVerticalStrut(8));
        menu.add(btnInteracciones);
        menu.add(Box.createVerticalStrut(8));
        menu.add(btnBuscar);
        menu.add(Box.createVerticalStrut(8));
        menu.add(btnHashtag);
        menu.add(Box.createVerticalStrut(8));
        menu.add(btnInbox);
        menu.add(Box.createVerticalStrut(8));
        menu.add(btnEditarPerfil);
        menu.add(Box.createVerticalGlue());
        menu.add(btnCerrarSesion);

        add(menu, BorderLayout.WEST);
    }
    
    private JButton crearBoton(String texto){
        JButton boton = new JButton(texto);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        boton.setPreferredSize(new Dimension(170, 40));
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setFont(new Font("Arial", Font.PLAIN, 15));
        boton.setFocusPainted(false);
        boton.setBackground(Color.WHITE);
        return boton;
    }
    
    private void crearContenido(){
        transicion = new CardLayout();
        panelContenido = new JPanel(transicion);
        panelContenido.setBackground(Color.WHITE);
        
        TimelinePanel timeline = new TimelinePanel();
        PerfilPanel perfil = new PerfilPanel();
        BuscarPanel buscar = new BuscarPanel();
        InboxPanel inbox = new InboxPanel();
        EditarPerfilPanel editar = new EditarPerfilPanel();
        
        panelContenido.add(timeline, "TIMELINE");
        panelContenido.add(perfil, "PERFIL");
        panelContenido.add(buscar, "BUSCAR");
        panelContenido.add(inbox, "INBOX");
        panelContenido.add(editar, "EDITAR");
        
        JPanel publicar  = crearPanelTemporal("Crer nueva publicación");
        JPanel interacciones = crearPanelTemporal("Interacciones");
        JPanel hashtag = crearPanelTemporal("Buscar Hashtag");
        
        configurarEventos();
        add(panelContenido, BorderLayout.CENTER);
        transicion.show(panelContenido, "TIMELINE");
    }
    
    private JPanel crearPanelTemporal(String titulo){
        JPanel panel = new JPanel(new BorderLayout());
        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(lblTitulo, BorderLayout.NORTH);
        return panel;
    }
    
     private void configurarEventos() {

        btnTimeline.addActionListener(e ->
            transicion.show(panelContenido, "TIMELINE")
        );

        btnPerfil.addActionListener(e ->
            transicion.show(panelContenido, "PERFIL")
        );

        btnPublicar.addActionListener(e ->
            transicion.show(panelContenido, "PUBLICAR")
        );

        btnInteracciones.addActionListener(e ->
            transicion.show(panelContenido, "INTERACCIONES")
        );

        btnBuscar.addActionListener(e ->
            transicion.show(panelContenido, "BUSCAR")
        );

        btnHashtag.addActionListener(e ->
            transicion.show(panelContenido, "HASHTAG")
        );

        btnInbox.addActionListener(e ->
            transicion.show(panelContenido, "INBOX")
        );

        btnEditarPerfil.addActionListener(e ->
            transicion.show(panelContenido, "EDITAR")
        );
        
        btnCerrarSesion.addActionListener(e ->{
            int respuesta = JOptionPane.showConfirmDialog(this, "¿Desea cerrar sesión?",  "Cerrar sesión", JOptionPane.YES_NO_OPTION);
            if(respuesta == JOptionPane.YES_OPTION){
                JOptionPane.showMessageDialog(this, "Sesión cerrada.");
            }
        });
     }
     
     public void setUsuario(String username){
         lblUsuario.setText("@" + username);
     }
     
     public JButton getBtnCerrarSesion(){
         return btnCerrarSesion;
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
