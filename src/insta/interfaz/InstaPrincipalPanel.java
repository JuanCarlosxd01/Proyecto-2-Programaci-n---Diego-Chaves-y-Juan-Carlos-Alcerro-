
package insta.interfaz;

import interfaz.DialogosWindows;

import java.awt.*;
import javax.swing.*;
import red.Cliente;
import red.Respuesta;

public class InstaPrincipalPanel extends JPanel {

    private CardLayout cardLayout;
    private JPanel panelContenido;

    private Cliente cliente;

    private LoginInstaPanel loginPanel;
    private CrearCuentaInstaPanel crearCuentaPanel;
    private InstaPanel instaPanel;

    private Runnable accionCerrar;

    public InstaPrincipalPanel() {
        cliente = new Cliente();

        setLayout(new BorderLayout());

        crearContenido();
        configurarEventos();
    }

    private void crearContenido() {
        cardLayout = new CardLayout();
        panelContenido = new JPanel(cardLayout);

        loginPanel = new LoginInstaPanel();
        crearCuentaPanel = new CrearCuentaInstaPanel();

        panelContenido.add(loginPanel, "LOGIN");
        panelContenido.add(crearCuentaPanel, "REGISTRO");

        add(panelContenido, BorderLayout.CENTER);

        cardLayout.show(panelContenido, "LOGIN");
    }

    private void configurarEventos() {
        loginPanel.getBtnCrearCuenta().addActionListener(e -> {
            crearCuentaPanel.limpiar();
            cardLayout.show(panelContenido, "REGISTRO");
        });

        crearCuentaPanel.getBtnVolver().addActionListener(e -> {
            loginPanel.getTxtPassword().setText("");
            cardLayout.show(panelContenido, "LOGIN");
        });

        loginPanel.getBtnIngresar().addActionListener(e -> {
            iniciarSesion();
        });

        loginPanel.getTxtPassword().addActionListener(e -> {
            iniciarSesion();
        });

        crearCuentaPanel.getBtnCrearCuenta().addActionListener(e -> {
            registrarUsuario();
        });
    }

    private void iniciarSesion() {
        String username = loginPanel.getTxtUsuario().getText().trim();
        String password = new String(loginPanel.getTxtPassword().getPassword());

        if (username.isEmpty()) {
            DialogosWindows.showMessageDialog(this, "Ingrese su username.", "INSTA+", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (password.isEmpty()) {
            DialogosWindows.showMessageDialog(this, "Ingrese su contraseña.", "INSTA+", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {

            @Override
            protected Respuesta doInBackground() throws Exception {
                return cliente.iniciarSesion(username, password);
            }

            @Override
            protected void done() {
                try {
                    Respuesta respuesta = get();

                    if (respuesta.esExitosa()) {
                        abrirInstagram();
                    } else {
                        manejarErrorLogin(respuesta);
                    }

                } catch (Exception e) {
                    DialogosWindows.showMessageDialog(
                            InstaPrincipalPanel.this,
                            "No se pudo conectar con el servidor de INSTA+.\n" + obtenerMensajeError(e),
                            "Error de conexión",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        trabajador.execute();
    }

    private void manejarErrorLogin(Respuesta respuesta) {
        switch (respuesta.getCodigo()) {

            case CREDENCIALES_INCORRECTAS:
                Object[] opciones = {"Intentar nuevamente", "Crear cuenta"};
                int opcion = DialogosWindows.showOptionDialog(this, "El username o la contraseña son incorrectos.\n¿Qué desea hacer?", "Inicio de sesión", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, opciones, opciones[0]);
                loginPanel.getTxtPassword().setText("");
                if (opcion == 1) {
                    crearCuentaPanel.limpiar();
                    cardLayout.show(panelContenido, "REGISTRO");
                } else {
                    loginPanel.getTxtPassword().requestFocusInWindow();
                }
                break;

            case CUENTA_DESACTIVADA:
                DialogosWindows.showMessageDialog(
                        this,
                        "Esta cuenta se encuentra desactivada.",
                        "Cuenta desactivada",
                        JOptionPane.WARNING_MESSAGE
                );
                break;

            case DATOS_INVALIDOS:
                DialogosWindows.showMessageDialog(
                        this,
                        respuesta.getMensaje(),
                        "Datos inválidos",
                        JOptionPane.WARNING_MESSAGE
                );
                break;

            default:
                DialogosWindows.showMessageDialog(
                        this,
                        respuesta.getMensaje(),
                        "INSTA+",
                        JOptionPane.ERROR_MESSAGE
                );
                break;
        }
    }

    private void registrarUsuario() {
        if (!crearCuentaPanel.datosValidos()) {
            return;
        }

        String nombre = crearCuentaPanel.getTxtNombre().getText().trim();
        String username = crearCuentaPanel.getTxtUsername().getText().trim();
        String password = new String(crearCuentaPanel.getTxtPassword().getPassword());

        char genero = crearCuentaPanel.getGeneroSeleccionado();
        int edad = (Integer) crearCuentaPanel.getSpnEdad().getValue();

        byte[] foto = crearCuentaPanel.getFotoSeleccionada();

        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {

            @Override
            protected Respuesta doInBackground() throws Exception {
                return cliente.registrarUsuario(
                        nombre,
                        genero,
                        username,
                        password,
                        edad,
                        foto
                );
            }

            @Override
            protected void done() {
                try {
                    Respuesta respuesta = get();

                    if (respuesta.esExitosa()) {
                        DialogosWindows.showMessageDialog(
                                InstaPrincipalPanel.this,
                                "Cuenta creada correctamente.\nAhora puedes iniciar sesión.",
                                "INSTA+",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        loginPanel.getTxtUsuario().setText(username);
                        loginPanel.getTxtPassword().setText("");

                        crearCuentaPanel.limpiar();

                        cardLayout.show(panelContenido, "LOGIN");

                    } else {
                        manejarErrorRegistro(respuesta);
                    }

                } catch (Exception e) {
                    DialogosWindows.showMessageDialog(
                            InstaPrincipalPanel.this,
                            "No se pudo crear la cuenta.\n" + obtenerMensajeError(e),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        trabajador.execute();
    }

    private void manejarErrorRegistro(Respuesta respuesta) {
        switch (respuesta.getCodigo()) {

            case USERNAME_DUPLICADO:
                DialogosWindows.showMessageDialog(
                        this,
                        "Ese username ya existe.\nElija otro username.",
                        "Username no disponible",
                        JOptionPane.WARNING_MESSAGE
                );
                break;

            case DATOS_INVALIDOS:
                DialogosWindows.showMessageDialog(
                        this,
                        respuesta.getMensaje(),
                        "Datos inválidos",
                        JOptionPane.WARNING_MESSAGE
                );
                break;

            default:
                DialogosWindows.showMessageDialog(
                        this,
                        respuesta.getMensaje(),
                        "INSTA+",
                        JOptionPane.ERROR_MESSAGE
                );
                break;
        }
    }

    private void abrirInstagram() {
        if (instaPanel != null) {
            panelContenido.remove(instaPanel);
        }

        instaPanel = new InstaPanel(cliente);

        Respuesta.DatosUsuario usuario = cliente.getUsuarioActual();

        if (usuario != null) {
            instaPanel.setUsuario(usuario.getUsername());
        }

        instaPanel.setAccionCerrar(() -> {
            cerrarSesion();
        });

        panelContenido.add(instaPanel, "INSTA");

        cardLayout.show(panelContenido, "INSTA");

        panelContenido.revalidate();
        panelContenido.repaint();

        loginPanel.getTxtPassword().setText("");
    }

    private void cerrarSesion() {
        SwingWorker<Respuesta, Void> trabajador = new SwingWorker<>() {

            @Override
            protected Respuesta doInBackground() throws Exception {
                return cliente.cerrarSesion();
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    System.out.println("No se pudo cerrar la sesión en el servidor: " + obtenerMensajeError(e));
                }

                volverAlLogin();
            }
        };

        trabajador.execute();
    }

    private void volverAlLogin() {
        if (instaPanel != null) {
            panelContenido.remove(instaPanel);
            instaPanel = null;
        }

        loginPanel.limpiar();

        cardLayout.show(panelContenido, "LOGIN");

        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private String obtenerMensajeError(Exception e) {
        Throwable causa = e;

        while (causa.getCause() != null) {
            causa = causa.getCause();
        }

        if (causa.getMessage() != null) {
            return causa.getMessage();
        }

        return "Error desconocido.";
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }

    public void cerrar() {
        try {
            if (cliente.tieneSesion()) {
                cliente.cerrarSesion();
            }

            cliente.close();

        } catch (Exception e) {
            System.out.println("Error cerrando INSTA+: " + e.getMessage());
        }

        if (accionCerrar != null) {
            accionCerrar.run();
        }
    }
}
