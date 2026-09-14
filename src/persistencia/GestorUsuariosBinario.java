

package persistencia;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import modelo.UsuarioSistema;
import sistema.SeguridadArchivos;

public class GestorUsuariosBinario {

    private final String ruta = "usuarios.dat";

    public synchronized void guardarUsuarios(
            ArrayList<UsuarioSistema> usuarios
    ) {
        Path destino = Path.of(ruta);
        Path temporal = Path.of(ruta + ".tmp");

        try {
            try (ObjectOutputStream salida = new ObjectOutputStream(
                    Files.newOutputStream(temporal))) {
                salida.writeObject(usuarios);
            }

            if (Files.exists(destino)) {
                Files.copy(
                        destino,
                        Path.of(ruta + ".bak"),
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            try {
                Files.move(
                        temporal,
                        destino,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(
                        temporal,
                        destino,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudieron guardar los usuarios.",
                    e
            );
        }
    }

    public synchronized ArrayList<UsuarioSistema> cargarUsuarios() {
        if (!new File(ruta).exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream entrada = new ObjectInputStream(
                new FileInputStream(ruta))) {

            Object datos = entrada.readObject();

            if (!(datos instanceof ArrayList<?> lista)) {
                throw new IOException("Formato de usuarios inválido.");
            }

            ArrayList<UsuarioSistema> usuarios = new ArrayList<>();

            for (Object dato : lista) {
                if (!(dato instanceof UsuarioSistema usuario)) {
                    throw new IOException("Usuario inválido.");
                }

                SeguridadArchivos.nombre(usuario.getUsername());
                usuarios.add(usuario);
            }

            return usuarios;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "usuarios.dat no se puede leer. "
                    + "Conserva el archivo y revisa usuarios.dat.bak.",
                    e
            );
        }
    }
}