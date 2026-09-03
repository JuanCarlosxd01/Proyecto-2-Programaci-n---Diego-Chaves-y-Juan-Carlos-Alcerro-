
package interfaz;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel{
    private JPanel contenedor;
    private CardLayout transicion;
    
    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JButton btnIngresar;
    
    public LoginPanel(JPanel contenedor, CardLayout transicion){
        this.contenedor = contenedor;
        this.transicion = transicion;
        setLayout(new GridLayout());
        crearInterfaz();
    }
    
    private void crearInterfaz(){
        JPanel panelLogin = new JPanel();

        panelLogin.setPreferredSize(new Dimension(400, 420));

        panelLogin.setLayout(
                new BoxLayout(panelLogin, BoxLayout.Y_AXIS)
        );

        JLabel titulo = new JLabel("MINI WINDOWS");

        titulo.setFont(
                new Font("Arial", Font.BOLD, 30)
        );

        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblUsuario = new JLabel("Usuario");

        txtUsuario = new JTextField();

        txtUsuario.setMaximumSize(
                new Dimension(300, 35)
        );

        JLabel lblContrasena =
                new JLabel("Contraseña");

        txtContrasena =
                new JPasswordField();

        txtContrasena.setMaximumSize(
                new Dimension(300, 35)
        );

        btnIngresar =
                new JButton("INICIAR SESIÓN");

        btnIngresar.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        panelLogin.add(Box.createVerticalStrut(30));
        panelLogin.add(titulo);

        panelLogin.add(Box.createVerticalStrut(50));

        panelLogin.add(lblUsuario);
        panelLogin.add(txtUsuario);

        panelLogin.add(Box.createVerticalStrut(20));

        panelLogin.add(lblContrasena);
        panelLogin.add(txtContrasena);

        panelLogin.add(Box.createVerticalStrut(30));

        panelLogin.add(btnIngresar);

        add(panelLogin);

        acciones();
    }
    
     private void acciones() {

        btnIngresar.addActionListener(e -> {

            transicion.show(
                    contenedor,
                    "ESCRITORIO"
            );

        });
    }
    
    
    
    
}
