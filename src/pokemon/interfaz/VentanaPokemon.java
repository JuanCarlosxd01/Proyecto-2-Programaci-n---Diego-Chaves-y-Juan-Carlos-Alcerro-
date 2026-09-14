package pokemon.interfaz;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import javax.swing.JPanel;

/** Aplicación Pokémon embebible dentro de MiniWindows. */
public class VentanaPokemon extends JPanel {

    private final CardLayout cardLayout;
    private final JPanel panelPrincipal;
    private final LoginPanel loginPanel;
    private final CrearUsuarioPanel crearUsuarioPanel;
    private final SeleccionEquipoPanel seleccionEquipoPanel;
    private final BatallaPanel batallaPanel;

    public VentanaPokemon() {
        setLayout(new BorderLayout());
        cardLayout = new CardLayout();
        panelPrincipal = new JPanel(cardLayout);

        loginPanel = new LoginPanel(this);
        crearUsuarioPanel = new CrearUsuarioPanel(this);
        seleccionEquipoPanel = new SeleccionEquipoPanel(this);
        batallaPanel = new BatallaPanel(this);

        panelPrincipal.add(loginPanel, "LOGIN");
        panelPrincipal.add(crearUsuarioPanel, "CREAR_USUARIO");
        panelPrincipal.add(seleccionEquipoPanel, "SELECCION");
        panelPrincipal.add(batallaPanel, "BATALLA");
        add(panelPrincipal, BorderLayout.CENTER);
        mostrarLogin();
    }

    public void mostrarLogin() {
        cardLayout.show(panelPrincipal, "LOGIN");
    }

    public void mostrarCrearUsuario() {
        cardLayout.show(panelPrincipal, "CREAR_USUARIO");
    }

    public void mostrarSeleccion() {
        seleccionEquipoPanel.reiniciarSeleccion();
        cardLayout.show(panelPrincipal, "SELECCION");
    }

    public void mostrarBatalla() {
        batallaPanel.prepararBatalla();
        cardLayout.show(panelPrincipal, "BATALLA");
    }

    public LoginPanel getLoginPanel() { return loginPanel; }
    public CrearUsuarioPanel getCrearUsuarioPanel() { return crearUsuarioPanel; }
    public SeleccionEquipoPanel getSeleccionEquipoPanel() { return seleccionEquipoPanel; }
    public BatallaPanel getBatallaPanel() { return batallaPanel; }
}
