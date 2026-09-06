
package persistencia;

import excepciones.ArchivoCorruptoException;

import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.util.Objects;

public class GestorBinario {


    public synchronized void guardar(
            String rutaArchivo,
            Serializable objeto
    ) throws IOException {

        Objects.requireNonNull(
                objeto,
                "El objeto que se guardará no puede ser null."
        );

        Path destino = obtenerRuta(rutaArchivo);
        Path carpeta = destino.getParent();

        Files.createDirectories(carpeta);

        Path temporal = Files.createTempFile(
                carpeta,
                "insta-",
                ".tmp"
        );

        try {
            escribirTemporal(temporal, objeto);
            reemplazarArchivo(temporal, destino);

        } catch (IOException | RuntimeException error) {

        
            try {
                Files.deleteIfExists(temporal);

            } catch (IOException errorLimpieza) {
                error.addSuppressed(errorLimpieza);
            }

            throw error;
        }
    }

    private void escribirTemporal(
            Path temporal,
            Serializable objeto
    ) throws IOException {

        try (
                FileOutputStream archivo =
                        new FileOutputStream(temporal.toFile());

                ObjectOutputStream salida =
                        new ObjectOutputStream(archivo)
        ) {
            salida.writeObject(objeto);
            salida.flush();

        
            archivo.getFD().sync();
        }
    }

    private void reemplazarArchivo(
            Path temporal,
            Path destino
    ) throws IOException {

        try {
            Files.move(
                    temporal,
                    destino,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (AtomicMoveNotSupportedException e) {

     
            Files.move(
                    temporal,
                    destino,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }


    public synchronized <T> T leer(
            String rutaArchivo,
            Class<T> tipoEsperado
    ) throws IOException, ArchivoCorruptoException {

        Objects.requireNonNull(
                tipoEsperado,
                "Debes indicar el tipo de objeto esperado."
        );

        Path archivo = obtenerRuta(rutaArchivo);

        try (
                ObjectInputStream entrada =
                        new ObjectInputStream(
                                Files.newInputStream(archivo)
                        )
        ) {
            Object objeto = entrada.readObject();

            if (!tipoEsperado.isInstance(objeto)) {
                throw new ArchivoCorruptoException(
                        "El archivo "
                        + archivo.getFileName()
                        + " no contiene un objeto de tipo "
                        + tipoEsperado.getSimpleName()
                        + "."
                );
            }

            return tipoEsperado.cast(objeto);

        } catch (EOFException e) {
            throw new ArchivoCorruptoException(
                    "El archivo "
                    + archivo.getFileName()
                    + " está vacío o incompleto.",
                    e
            );

        } catch (ObjectStreamException e) {
            throw new ArchivoCorruptoException(
                    "El archivo "
                    + archivo.getFileName()
                    + " está dañado o tiene un formato incompatible.",
                    e
            );

        } catch (ClassNotFoundException e) {
            throw new ArchivoCorruptoException(
                    "No se reconoce una clase almacenada en "
                    + archivo.getFileName()
                    + ".",
                    e
            );
        }
    }

    private Path obtenerRuta(String rutaArchivo) {
        Objects.requireNonNull(
                rutaArchivo,
                "La ruta del archivo no puede ser null."
        );

        if (rutaArchivo.isBlank()) {
            throw new IllegalArgumentException(
                    "La ruta del archivo no puede estar vacía."
            );
        }

        return Path.of(rutaArchivo)
                .toAbsolutePath()
                .normalize();
    }
}