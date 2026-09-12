/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package persistencia;

import estructuras.ListaEnlazada;
import excepciones.ArchivoCorruptoException;
import java.io.*;
import java.nio.channels.*;
import java.nio.file.*;
/**
 *
 * @author diego
 */
public final class RepositorioInsta implements AutoCloseable {

    public record Cambio(String ruta, Serializable valor)
            implements Serializable {
    }

    private final Path raiz;
    private final GestorBinario binario = new GestorBinario();
    private final FileChannel canal;
    private final FileLock bloqueo;

    public RepositorioInsta(String ruta)
            throws IOException, ArchivoCorruptoException {

        raiz = Path.of(ruta).toAbsolutePath().normalize();
        Files.createDirectories(raiz);

        var atributos = Files.readAttributes(
                raiz,
                java.nio.file.attribute.BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS
        );

        if (atributos.isSymbolicLink() || atributos.isOther()) {
            throw new IOException("La raíz no puede ser un enlace.");
        }

        canal = FileChannel.open(
                raiz.resolve("servidor.lock"),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
        );

        FileLock obtenido;

        try {
            obtenido = canal.tryLock();
        } catch (RuntimeException | IOException e) {
            canal.close();
            throw new IOException("Almacenamiento en uso.", e);
        }

        if (obtenido == null) {
            canal.close();
            throw new IOException(
                    "Ya hay un servidor usando esta carpeta."
            );
        }

        bloqueo = obtenido;

        try {
            recuperar();
        } catch (IOException | ArchivoCorruptoException e) {
            close();
            throw e;
        }
    }

    public Path ruta(String relativa) throws IOException {
        Path path = raiz.resolve(relativa).normalize();

        if (!path.startsWith(raiz)
                || Path.of(relativa).isAbsolute()) {
            throw new IOException("Ruta no permitida.");
        }

        Path actual = raiz;

        for (Path parte : raiz.relativize(path)) {
            actual = actual.resolve(parte);

            if (Files.exists(actual, LinkOption.NOFOLLOW_LINKS)) {
                var atributos = Files.readAttributes(
                        actual,
                        java.nio.file.attribute.BasicFileAttributes.class,
                        LinkOption.NOFOLLOW_LINKS
                );

                if (atributos.isSymbolicLink()
                        || atributos.isOther()) {
                    throw new IOException(
                            "No se permiten enlaces en INSTA_RAIZ."
                    );
                }
            }
        }

        return path;
    }

    public synchronized boolean existe(String relativa)
            throws IOException {
        return Files.exists(ruta(relativa));
    }

    public synchronized void carpeta(String relativa)
            throws IOException {
        Files.createDirectories(ruta(relativa));
    }

    public String getRutaRaiz() {
        return raiz.toString();
    }

    public synchronized <T> ListaEnlazada<T> leer(
            String relativa,
            Class<T> tipo
    ) throws IOException, ArchivoCorruptoException {

        recuperar();

        Object objeto = binario.leer(
                ruta(relativa).toString(),
                ListaEnlazada.class
        );

        ListaEnlazada<T> resultado = new ListaEnlazada<>();

        for (Object dato : (ListaEnlazada<?>) objeto) {
            if (!tipo.isInstance(dato)) {
                throw new ArchivoCorruptoException(
                        "Registro inválido en " + relativa
                );
            }

            resultado.agregar(tipo.cast(dato));
        }

        return resultado;
    }

    public synchronized void guardar(Cambio... cambios)
            throws IOException, ArchivoCorruptoException {

        recuperar();

        ListaEnlazada<Cambio> lote = new ListaEnlazada<>();

        for (Cambio cambio : cambios) {
            ruta(cambio.ruta());
            lote.agregar(cambio);
        }

        binario.guardar(
                raiz.resolve("pendiente.ins").toString(),
                lote
        );

        aplicar(lote);
    }

    private void recuperar()
            throws IOException, ArchivoCorruptoException {

        Path pendiente = raiz.resolve("pendiente.ins");

        if (!Files.exists(pendiente)) {
            return;
        }

        ListaEnlazada<?> lote = binario.leer(
                pendiente.toString(),
                ListaEnlazada.class
        );

        ListaEnlazada<Cambio> validado = new ListaEnlazada<>();

        for (Object objeto : lote) {
            if (!(objeto instanceof Cambio cambio)) {
                throw new ArchivoCorruptoException(
                        "Transacción incompleta inválida."
                );
            }

            ruta(cambio.ruta());
            validado.agregar(cambio);
        }

        aplicar(validado);
    }

    private void aplicar(ListaEnlazada<Cambio> lote)
            throws IOException {

        for (Cambio cambio : lote) {
            Path destino = ruta(cambio.ruta());

            if (Boolean.TRUE.equals(cambio.valor())) {
                Files.createDirectories(destino);

            } else if (cambio.valor() instanceof byte[] bytes) {
                Files.createDirectories(destino.getParent());

                Path temporal = Files.createTempFile(
                        destino.getParent(),
                        "imagen-",
                        ".tmp"
                );

                try {
                    try (FileOutputStream salida =
                                 new FileOutputStream(temporal.toFile())) {
                        salida.write(bytes);
                        salida.getFD().sync();
                    }

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
                } finally {
                    Files.deleteIfExists(temporal);
                }

            } else {
                binario.guardar(
                        destino.toString(),
                        cambio.valor()
                );
            }
        }

        Files.delete(raiz.resolve("pendiente.ins"));
    }

    @Override
    public void close() throws IOException {
        try {
            if (bloqueo.isValid()) {
                bloqueo.release();
            }
        } finally {
            canal.close();
        }
    }
}
