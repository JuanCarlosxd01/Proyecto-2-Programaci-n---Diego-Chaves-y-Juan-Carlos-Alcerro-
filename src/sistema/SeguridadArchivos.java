/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistema;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;

/**
 *
 * @author diego
 */
public final class SeguridadArchivos {

    private SeguridadArchivos() {
    }

    public static void nombre(String nombre) {
        if (nombre == null
                || nombre.isBlank()
                || nombre.length() > 120
                || nombre.equals(".")
                || nombre.equals("..")
                || nombre.endsWith(".")
                || nombre.endsWith(" ")
                || nombre.matches(".*[\\\\/:*?\"<>|\\p{Cntrl}].*")) {
            throw new IllegalArgumentException(
                    "Nombre de archivo o carpeta inválido."
            );
        }

        String base = nombre.split("\\.", 2)[0]
                .toLowerCase(Locale.ROOT);

        if (base.matches("con|prn|aux|nul|com[1-9]|lpt[1-9]")) {
            throw new IllegalArgumentException(
                    "Nombre reservado por Windows."
            );
        }
    }

    public static Path dentro(File archivo, File raiz) throws IOException {
        if (archivo == null || raiz == null) {
            throw new IOException("No hay una ruta autorizada.");
        }

        Path rutaRaiz = raiz.toPath().toAbsolutePath().normalize();
        Path rutaArchivo = archivo.toPath().toAbsolutePath().normalize();

        if (!rutaArchivo.startsWith(rutaRaiz)) {
            throw new IOException("La ruta está fuera de tu cuenta.");
        }

        sinEnlaces(rutaArchivo);
        return rutaArchivo;
    }

    public static void sinEnlaces(Path ruta) throws IOException {
        Path normalizada = ruta.toAbsolutePath().normalize();
        Path actual = normalizada.getRoot();

        for (Path parte : normalizada) {
            actual = actual.resolve(parte);

            if (Files.exists(actual, LinkOption.NOFOLLOW_LINKS)) {
                BasicFileAttributes atributos = Files.readAttributes(
                        actual,
                        BasicFileAttributes.class,
                        LinkOption.NOFOLLOW_LINKS
                );

                if (atributos.isSymbolicLink() || atributos.isOther()) {
                    throw new IOException(
                            "No se permiten enlaces de carpetas."
                    );
                }
            }
        }
    }

    public static File permitido(File archivo) throws IOException {
        if (!Sesion.haySesion()) {
            throw new IOException("Inicia sesión.");
        }

        return dentro(
                archivo,
                RutasSistema.getRaizExplorador()
        ).toFile();
    }

    public static void importar(File archivo) throws IOException {
        Path ruta = archivo.toPath().toAbsolutePath().normalize();
        sinEnlaces(ruta);

        Path raizSistema = RutasSistema.getRaizSistema()
                .toPath().toAbsolutePath().normalize();

        if (ruta.startsWith(raizSistema)) {
            permitido(archivo);
        }
    }

    public static boolean esPermitido(File archivo) {
        try {
            permitido(archivo);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}