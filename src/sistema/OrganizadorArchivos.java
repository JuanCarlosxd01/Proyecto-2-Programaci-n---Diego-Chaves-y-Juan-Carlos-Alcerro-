package sistema;

import estructuras.ListaEnlazada;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class OrganizadorArchivos {

    public int organizar(File carpeta, File carpetaRaizUsuario) {
        if (carpeta == null || carpetaRaizUsuario == null || !carpeta.exists() || !carpeta.isDirectory()) {
            return 0;
        }

        File carpetaImagenes = new File(carpetaRaizUsuario, RutasSistema.NOMBRE_IMAGENES);
        File carpetaDocumentos = new File(carpetaRaizUsuario, RutasSistema.NOMBRE_DOCUMENTOS);
        File carpetaMusica = new File(carpetaRaizUsuario, RutasSistema.NOMBRE_MUSICA);

        carpetaImagenes.mkdirs();
        carpetaDocumentos.mkdirs();
        carpetaMusica.mkdirs();

        File[] archivos = carpeta.listFiles();
        if (archivos == null) {
            return 0;
        }

        ListaEnlazada<File> imagenes = new ListaEnlazada<>();
        ListaEnlazada<File> documentos = new ListaEnlazada<>();
        ListaEnlazada<File> musica = new ListaEnlazada<>();

        for (File archivo : archivos) {
            if (!archivo.isFile()) {
                continue;
            }

            String nombre = archivo.getName().toLowerCase();
            if (esImagen(nombre)) {
                imagenes.agregar(archivo);
            } else if (esDocumento(nombre)) {
                documentos.agregar(archivo);
            } else if (esMusica(nombre)) {
                musica.agregar(archivo);
            }
        }

        int movidos = 0;
        movidos += moverLista(imagenes, carpetaImagenes);
        movidos += moverLista(documentos, carpetaDocumentos);
        movidos += moverLista(musica, carpetaMusica);
        return movidos;
    }

    private int moverLista(ListaEnlazada<File> archivos, File destino) {
        int movidos = 0;

        for (File archivo : archivos) {
            try {
                if (moverArchivo(archivo, destino)) {
                    movidos++;
                }
            } catch (IOException e) {
                System.err.println("No se pudo mover " + archivo.getName() + ": " + e.getMessage());
            }
        }

        return movidos;
    }

    private boolean esImagen(String nombre) {
        return nombre.endsWith(".png") || nombre.endsWith(".jpg") || nombre.endsWith(".jpeg") || nombre.endsWith(".gif") || nombre.endsWith(".bmp") || nombre.endsWith(".webp");
    }

    private boolean esDocumento(String nombre) {
        return nombre.endsWith(".txt") || nombre.endsWith(".pdf") || nombre.endsWith(".doc") || nombre.endsWith(".docx") || nombre.endsWith(".ppt") || nombre.endsWith(".pptx") || nombre.endsWith(".xls") || nombre.endsWith(".xlsx");
    }

    private boolean esMusica(String nombre) {
        return nombre.endsWith(".mp3") || nombre.endsWith(".wav") || nombre.endsWith(".wma") || nombre.endsWith(".au") || nombre.endsWith(".aiff") || nombre.endsWith(".aif");
    }

    private boolean moverArchivo(File archivo, File carpetaDestino) throws IOException {
        if (archivo.getParentFile().getCanonicalFile().equals(carpetaDestino.getCanonicalFile())) {
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
