

package sistema;

import EditorTexto.DocumentosTexto;
import estructuras.ListaEnlazada;
import java.io.File;
import java.io.IOException;

public class OrganizadorArchivos {

    public int organizar(File carpeta, File carpetaRaizUsuario) {
        if (carpeta == null
                || carpetaRaizUsuario == null
                || !carpeta.exists()
                || !carpeta.isDirectory()) {
            return 0;
        }

        if (!SeguridadArchivos.esPermitido(carpeta)
                || !SeguridadArchivos.esPermitido(carpetaRaizUsuario)) {
            return 0;
        }

        File carpetaImagenes = new File(
                carpetaRaizUsuario,
                RutasSistema.NOMBRE_IMAGENES
        );

        File carpetaDocumentos = new File(
                carpetaRaizUsuario,
                RutasSistema.NOMBRE_DOCUMENTOS
        );

        File carpetaMusica = new File(
                carpetaRaizUsuario,
                RutasSistema.NOMBRE_MUSICA
        );

        carpetaImagenes.mkdirs();
        carpetaDocumentos.mkdirs();
        carpetaMusica.mkdirs();

        if (!SeguridadArchivos.esPermitido(carpeta)
                || !SeguridadArchivos.esPermitido(carpetaRaizUsuario)) {
            throw new IllegalArgumentException("Carpeta no autorizada.");
        }

        ListaEnlazada<File> archivos = new ListaEnlazada<>();
        File[] encontrados = carpeta.listFiles();

        if (encontrados != null) {
            for (File archivo : encontrados) {
                if (SeguridadArchivos.esPermitido(archivo)) {
                    archivos.agregar(archivo);
                }
            }
        }

        int movidos = 0;

        for (File archivo : archivos) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }

            if (!archivo.isFile()) {
                continue;
            }

            String nombre = archivo.getName().toLowerCase();

            if (nombre.endsWith(".txt.formato.edt")) {
                continue;
            }

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
                System.err.println(
                        "No se pudo mover " + archivo.getName()
                        + ": " + e.getMessage()
                );
            }
        }

        return movidos;
    }

    private boolean esImagen(String nombre) {
        return nombre.endsWith(".png")
                || nombre.endsWith(".jpg")
                || nombre.endsWith(".jpeg")
                || nombre.endsWith(".gif")
                || nombre.endsWith(".bmp")
                || nombre.endsWith(".webp");
    }

    private boolean esDocumento(String nombre) {
        return nombre.endsWith(".txt")
                || nombre.endsWith(".pdf")
                || nombre.endsWith(".doc")
                || nombre.endsWith(".docx")
                || nombre.endsWith(".edt")
                || nombre.endsWith(".ppt")
                || nombre.endsWith(".pptx")
                || nombre.endsWith(".xls")
                || nombre.endsWith(".xlsx");
    }

    private boolean esMusica(String nombre) {
        return nombre.endsWith(".mp3")
                || nombre.endsWith(".wav")
                || nombre.endsWith(".wma")
                || nombre.endsWith(".au")
                || nombre.endsWith(".aiff")
                || nombre.endsWith(".aif");
    }

    private boolean moverArchivo(File archivo, File carpetaDestino)
            throws IOException {

        if (archivo.getParentFile().equals(carpetaDestino)) {
            return false;
        }

        File destino = new File(carpetaDestino, archivo.getName());

        if (destino.exists()) {
            destino = obtenerNombreDisponible(
                    carpetaDestino,
                    archivo.getName()
            );
        }

        DocumentosTexto.mover(archivo, destino);
        return true;
    }

    private File obtenerNombreDisponible(
            File carpeta,
            String nombreOriginal
    ) {
        int punto = nombreOriginal.lastIndexOf(".");

        String nombre = punto == -1
                ? nombreOriginal
                : nombreOriginal.substring(0, punto);

        String extension = punto == -1
                ? ""
                : nombreOriginal.substring(punto);

        int numero = 1;

        File destino = new File(
                carpeta,
                nombre + " (" + numero + ")" + extension
        );

        while (destino.exists()) {
            numero++;

            destino = new File(
                    carpeta,
                    nombre + " (" + numero + ")" + extension
            );
        }

        return destino;
    }
}