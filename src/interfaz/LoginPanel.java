
package interfaz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import sistema.*;
import modelo.*;

public class LoginPanel extends JPanel{
    private JPanel contenedor;
    private CardLayout transicion;
    private GestorUsuarios gestorUsuarios;
    
    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JButton btnIngresar;
    private JButton btnApagar;
    private ImageIcon iconoApagar = new ImageIcon(getClass().getResource("/Imagenes/botonApagar.png")); 
    
    public LoginPanel(JPanel contenedor, CardLayout transicion, GestorUsuarios gestorUsuarios){       
        this.contenedor = contenedor;
        this.transicion = transicion;
        this.gestorUsuarios = gestorUsuarios;
        setOpaque(false);
        setLayout(new GridBagLayout());

        crearBotonApagado();
        crearInterfaz();
    }
    
    @Override 
    protected void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(0, 0, 0 ,120));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
        g2.dispose();
        super.paintComponent(g);
    }
    
    private void crearInterfaz(){
        JPanel panelLogin = new JPanel();
        panelLogin.setOpaque(false);
        panelLogin.setPreferredSize(new Dimension(350, 430));
        panelLogin.setLayout(new BoxLayout(panelLogin, BoxLayout.Y_AXIS));
        
        ImageIcon iconoOriginal = new ImageIcon(getClass().getResource("/Imagenes/imagenPerfil.png"));
        Image imagenUsuario = iconoOriginal.getImage().getScaledInstance(125, 125, Image.SCALE_SMOOTH);
        
        JLabel lblIcono = new JLabel(new ImageIcon(imagenUsuario));
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titulo = new JLabel("Otro Usuario");
        titulo.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        txtUsuario = new JTextField();
        txtUsuario.setMaximumSize(new Dimension(245, 35));
        txtUsuario.setPreferredSize(new Dimension(245, 35));
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUsuario.setText("Nombre de usuario");
        txtUsuario.setForeground(Color.GRAY);
        txtUsuario.addFocusListener(new java.awt.event.FocusAdapter(){
            @Override
            public void focusGained(java.awt.event.FocusEvent e){
                if(txtUsuario.getText().equals("Nombre de usuario")){
                    txtUsuario.setText("");
                    txtUsuario.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e){
                if(txtUsuario.getText().length() == 0){
                    txtUsuario.setText("Nombre de usuario");
                    txtUsuario.setForeground(Color.GRAY);
                }
            }
        });
        
        JPanel panelContrasena = new JPanel();
        panelContrasena.setOpaque(false);
        panelContrasena.setLayout(new BoxLayout(panelContrasena, BoxLayout.X_AXIS));
        panelContrasena.setMaximumSize(new Dimension(245, 35));
        panelContrasena.setPreferredSize(new Dimension(245, 35));
        

        txtContrasena = new JPasswordField();
        txtContrasena.setMaximumSize(new Dimension(210, 35));
        txtContrasena.setPreferredSize(new Dimension(210, 35));
        txtContrasena.setBackground(new Color(70, 70, 70));
        txtContrasena.setForeground(Color.WHITE);
        txtContrasena.setCaretColor(Color.WHITE);
        txtContrasena.setText("Contraseña");
        txtContrasena.setEchoChar((char) 0);
        txtContrasena.setForeground(new Color(200, 200, 200));
        txtContrasena.addFocusListener(new java.awt.event.FocusAdapter(){
            @Override
            public void focusGained(java.awt.event.FocusEvent e){
                String texto = new String(txtContrasena.getPassword());
                if(texto.equals("Contraseña")){
                    txtContrasena.setText("");
                    txtContrasena.setEchoChar('•');
                    txtContrasena.setForeground(Color.WHITE);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e){
                if(txtContrasena.getPassword().length == 0){
                    txtContrasena.setText("Contraseña");
                    txtContrasena.setEchoChar((char) 0);
                    txtContrasena.setForeground(new Color(200, 200, 200));
                }
            }
        });
        
        btnIngresar = new JButton(("<html><div style='margin-bottom:4px;'>→</div></html>"));
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 30));
        btnIngresar.setHorizontalAlignment(SwingConstants.CENTER);
        btnIngresar.setVerticalAlignment(SwingConstants.CENTER);
        btnIngresar.setPreferredSize(new Dimension(45, 35));
        btnIngresar.setMaximumSize(new Dimension(45, 35));
        btnIngresar.setMargin(new Insets(0, 0, 3, 0));
        btnIngresar.setFocusPainted(false);
        btnIngresar.setBackground(new Color(90, 105, 115));
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setBorder(BorderFactory.createLineBorder(new Color(160, 160, 160)));
        
        panelContrasena.add(txtContrasena);
        panelContrasena.add(btnIngresar);
        
        
        
        panelLogin.add(Box.createVerticalStrut(20));
        panelLogin.add(lblIcono);
        panelLogin.add(Box.createVerticalStrut(8));
        panelLogin.add(titulo);
        panelLogin.add(Box.createVerticalStrut(25));
        panelLogin.add(txtUsuario);
        panelLogin.add(Box.createVerticalStrut(7));
        panelLogin.add(panelContrasena);
        panelLogin.add(Box.createVerticalStrut(12));
        
        GridBagConstraints gbcLogin = new GridBagConstraints();
        gbcLogin.gridx = 0;
        gbcLogin.gridy = 0;
        gbcLogin.weightx = 1;
        gbcLogin.weighty = 1;
        gbcLogin.anchor = GridBagConstraints.CENTER;
        add(panelLogin, gbcLogin);
        
        GridBagConstraints gbcApagar = new GridBagConstraints();
        gbcApagar.gridx = 0;
        gbcApagar.gridy = 1;
        gbcApagar.weightx = 1;
        gbcApagar.anchor = GridBagConstraints.SOUTHEAST;
        gbcApagar.insets =new Insets(0, 0, 25, 30);
        add(btnApagar, gbcApagar);

        acciones();
    }
    
    private void crearBotonApagado(){
        Image imagenEscalada = iconoApagar.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        btnApagar = new JButton(new ImageIcon(imagenEscalada));
        btnApagar.setPreferredSize(new Dimension(42, 42));
        btnApagar.setContentAreaFilled(false);
        btnApagar.setBorderPainted(false);
        btnApagar.setFocusPainted(false);
        btnApagar.setOpaque(false);
        
        btnApagar.setToolTipText("Apagar");
        
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
    
     private void acciones() {
        btnIngresar.addActionListener(e -> {
            String usuario = txtUsuario.getText();
            String contrasena = new String(txtContrasena.getPassword());
            UsuarioSistema encontrado = gestorUsuarios.iniciarSesion(usuario, contrasena);
            if(encontrado != null){
                Sesion.iniciarSesion(encontrado);
                transicion.show(contenedor, "ESCRITORIO");
            }
            else{
                JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.");
            }
        });
    }
    
}
