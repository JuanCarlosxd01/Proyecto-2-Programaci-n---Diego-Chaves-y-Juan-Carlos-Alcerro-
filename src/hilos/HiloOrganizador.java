package hilos;

import java.io.File;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;
import sistema.OrganizadorArchivos;

public class HiloOrganizador extends Thread {

    private final File carpeta;
    private final File raizUsuario;
    private final Consumer<Integer> alTerminar;
    private final Consumer<Throwable> alError;

    public HiloOrganizador(File carpeta, File raizUsuario, Consumer<Integer> alTerminar, Consumer<Throwable> alError) {
        super("organizador-archivos");
        this.carpeta = carpeta;
        this.raizUsuario = raizUsuario;
        this.alTerminar = alTerminar;
        this.alError = alError;
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            int movidos = new OrganizadorArchivos().organizar(carpeta, raizUsuario);
            if (alTerminar != null) {
                SwingUtilities.invokeLater(() -> alTerminar.accept(movidos));
            }
        } catch (Throwable e) {
            if (alError != null) {
                SwingUtilities.invokeLater(() -> alError.accept(e));
            }
        }
    }
}
