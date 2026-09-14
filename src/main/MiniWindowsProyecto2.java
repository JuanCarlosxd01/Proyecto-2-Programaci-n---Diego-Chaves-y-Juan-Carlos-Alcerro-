
package main;

import interfaz.VentanaPrincipal;
import java.net.BindException;
import java.util.Arrays;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import red.Servidor;

public class MiniWindowsProyecto2 {

    public static void main(String[] args) {
        boolean soloServidor = Arrays.asList(args).contains("--servidor");
        boolean soloCliente = Arrays.asList(args).contains("--cliente");

        if (!soloCliente) {
            try {
                Servidor servidor = new Servidor(
                        Integer.getInteger("insta.puerto", 5050),
                        System.getProperty("insta.raiz", "INSTA_RAIZ")
                );

                Runtime.getRuntime().addShutdownHook(
                        new Thread(servidor::close)
                );

                Thread hilo = new Thread(() -> {
                    try {
                        servidor.iniciar();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, "servidor-insta");

                hilo.setDaemon(!soloServidor);
                hilo.start();
            } catch (BindException e) {
                if (soloServidor) {
                    System.err.println("El puerto ya está ocupado.");
                    return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        null,
                        "No se pudo iniciar el servidor: " + e.getMessage()
                );
                return;
            }
        }

        if (!soloServidor) {
            SwingUtilities.invokeLater(() -> new VentanaPrincipal());
        }
    }
}