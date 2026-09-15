package main;

import interfaz.VentanaPrincipal;
import red.Servidor;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MiniWindowsProyecto2 {

    public static void main(String[] args) {
        asegurarServidorCompartido();
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("No se pudo aplicar el estilo de Windows: " + e.getMessage());
            }
            new VentanaPrincipal();
        });
    }

    private static void asegurarServidorCompartido() {
        if (servidorDisponible()) {
            System.out.println("Servidor INSTA+ existente detectado. Esta instancia funcionará como cliente.");
            return;
        }

        try {
            String java = new File(System.getProperty("java.home"), "bin" + File.separator + "java").getAbsolutePath();
            String classpath = System.getProperty("java.class.path");
            ProcessBuilder proceso = new ProcessBuilder(java, "-cp", classpath, "red.ServidorPrincipal");
            proceso.directory(new File(System.getProperty("user.dir")));
            proceso.redirectOutput(ProcessBuilder.Redirect.appendTo(new File("servidor-insta.log")));
            proceso.redirectError(ProcessBuilder.Redirect.appendTo(new File("servidor-insta.log")));
            proceso.start();

            for (int intento = 0; intento < 20 && !servidorDisponible(); intento++) Thread.sleep(100);
            if (servidorDisponible()) {
                System.out.println("Servidor INSTA+ compartido iniciado correctamente.");
            } else {
                System.err.println("El servidor INSTA+ no respondió después de iniciarlo.");
            }
        } catch (Exception e) {
            System.err.println("No se pudo iniciar el servidor INSTA+ compartido: " + e.getMessage());
        }
    }

    private static boolean servidorDisponible() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", Servidor.PUERTO_PREDETERMINADO), 350);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
