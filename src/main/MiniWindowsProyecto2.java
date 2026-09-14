
package main;

import interfaz.VentanaPrincipal;
import red.Servidor;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MiniWindowsProyecto2 {

    private static Servidor servidor;

    public static void main(String[] args) {

        iniciarServidor();

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("No se pudo aplicar el estilo de Windows: " + e.getMessage());
            }

            new VentanaPrincipal();
        });
    }

    private static void iniciarServidor() {
        try {
            servidor = new Servidor();

            Thread hiloServidor = new Thread(() -> {
                try {
                    servidor.iniciar();
                } catch (Exception e) {
                    System.err.println("Error en servidor INSTA+: " + e.getMessage());
                }
            });

            hiloServidor.setName("Servidor-INSTA");
            hiloServidor.setDaemon(true);
            hiloServidor.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if(servidor != null) {
                    servidor.close();
                }
            }));

            System.out.println("Servidor INSTA+ iniciado en puerto " + servidor.getPuerto());

        } catch (Exception e) {
            System.err.println("No se pudo iniciar el servidor INSTA+: " + e.getMessage());
        }
    }
}