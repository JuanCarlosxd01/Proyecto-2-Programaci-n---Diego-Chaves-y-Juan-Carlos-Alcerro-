

package hilos;

import java.io.File;
import java.util.function.Consumer;
import javax.swing.SwingWorker;
import sistema.OrganizadorArchivos;

public final class HiloOrganizador extends SwingWorker<Integer, Void> {

    private final File carpeta;
    private final File raiz;
    private final Consumer<Integer> exito;
    private final Consumer<Exception> error;

    public HiloOrganizador(
            File carpeta,
            File raiz,
            Consumer<Integer> exito,
            Consumer<Exception> error
    ) {
        this.carpeta = carpeta;
        this.raiz = raiz;
        this.exito = exito;
        this.error = error;
    }

    @Override
    protected Integer doInBackground() throws Exception {
        return new OrganizadorArchivos().organizar(carpeta, raiz);
    }

    @Override
    protected void done() {
        if (isCancelled()) {
            return;
        }

        try {
            exito.accept(get());
        } catch (Exception e) {
            error.accept(e);
        }
    }
}