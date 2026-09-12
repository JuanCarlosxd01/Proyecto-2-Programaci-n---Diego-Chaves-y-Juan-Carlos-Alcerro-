package sistema;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class OrganizadorArchivos {

    public int organizar(File carpeta, File carpetaRaizUsuario) {
        if (carpeta == null || carpetaRaizUsuario == null || !carpeta.exists() || !carpeta.isDirectory()) {
            return 0;
        }

        File carpetaImagenes = new File(carpetaRaizUsuario, "Mis Imágenes");
        File carpetaDocumentos = new File(carpetaRaizUsuario, "Mis Documentos");
        File carpetaMusica = new File(carpetaRaizUsuario, "Música");

        carpetaImagenes.mkdirs();
        carpetaDocumentos.mkdirs();
        carpetaMusica.mkdirs();

        File[] archivos = carpeta.listFiles();

        if (archivos == null) {
            return 0;
        }

        int movidos = 0;

        for (File archivo : archivos) {
            if (!archivo.isFile()) {
                continue;
            }

            String nombre = archivo.getName().toLowerCase();

            try {
                if (esImagen(nombre)) {
                    if (moverArchivo(archivo, carpetaImagenes)) {
                        movidos++;
                    }
                } else if (esDocumento(nombre)) {
                    if (moverArchivo(archivo, carpetaDocumentos)) {
                        movidos++;
                    }
                } else if (esMusica(nombre)) {
                    if (moverArchivo(archivo, carpetaMusica)) {
                        movidos++;
                    }
                }
            } catch (IOException e) {
                System.out.println("No se pudo mover " + archivo.getName());
            }
        }

        return movidos;
    }

    private boolean esImagen(String nombre) {
        return nombre.endsWith(".png") || nombre.endsWith(".jpg") || nombre.endsWith(".jpeg") || nombre.endsWith(".gif") || nombre.endsWith(".bmp") || nombre.endsWith(".webp");
    }

    private boolean esDocumento(String nombre) {
        return nombre.endsWith(".txt") || nombre.endsWith(".pdf") || nombre.endsWith(".doc") || nombre.endsWith(".docx") || nombre.endsWith(".edt") || nombre.endsWith(".ppt") || nombre.endsWith(".pptx") || nombre.endsWith(".xls") || nombre.endsWith(".xlsx");
    }

    private boolean esMusica(String nombre) {
        return nombre.endsWith(".wav") || nombre.endsWith(".au") || nombre.endsWith(".aiff") || nombre.endsWith(".aif");
    }

    private boolean moverArchivo(File archivo, File carpetaDestino) throws IOException {
        if (archivo.getParentFile().equals(carpetaDestino)) {
            return false;
        }

        File destino = new File(carpetaDestino, archivo.getName());

        if (destino.exists()) {
            destino = obtenerNombreDisponible(carpetaDestino, archivo.getName());
        }

        Files.move(archivo.toPath(), destino.toPath());
        return true;
    }

    private File obtenerNombreDisponible(File carpeta, String nombreOriginal) {
        int punto = nombreOriginal.lastIndexOf(".");
        String nombre = punto == -1 ? nombreOriginal : nombreOriginal.substring(0, punto);
        String extension = punto == -1 ? "" : nombreOriginal.substring(punto);

        int numero = 1;
        File destino = new File(carpeta, nombre + " (" + numero + ")" + extension);

        while (destino.exists()) {
            numero++;
            destino = new File(carpeta, nombre + " (" + numero + ")" + extension);
        }

        return destino;
    }
}