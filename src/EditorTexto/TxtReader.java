package EditorTexto;

import excepciones.ArchivoTxtException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TxtReader {

    private static final byte[] MAGIC = "MWT1".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] EOF_MARK = "EOF1".getBytes(StandardCharsets.US_ASCII);

    public boolean tieneFormatoMiniWindows(File archivo) {
        if (archivo == null || !archivo.isFile() || archivo.length() < MAGIC.length) {
            return false;
        }

        try (FileInputStream in = new FileInputStream(archivo)) {
            byte[] cabecera = new byte[MAGIC.length];
            return in.read(cabecera) == MAGIC.length && Arrays.equals(cabecera, MAGIC);
        } catch (IOException e) {
            return false;
        }
    }

    public List<TextChunk> abrir(String rutaArchivo) throws ArchivoTxtException {
        if (rutaArchivo == null || !rutaArchivo.toLowerCase().endsWith(".txt")) {
            throw new ArchivoTxtException("Extensión inválida: se esperaba un archivo .txt.");
        }

        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            throw new ArchivoTxtException("El archivo no existe: " + rutaArchivo);
        }
        if (archivo.length() == 0) {
            return new ArrayList<>();
        }

        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(archivo)))) {
            leerYValidarMagic(in);
            int version = in.readInt();
            if (version != 1) {
                throw new ArchivoTxtException("Versión de formato no soportada: " + version);
            }

            int cantidadFragmentos = in.readInt();
            if (cantidadFragmentos < 0) {
                throw new ArchivoTxtException("Cabecera corrupta: cantidad de fragmentos inválida.");
            }

            List<TextChunk> fragmentos = new ArrayList<>();
            for (int i = 0; i < cantidadFragmentos; i++) {
                try {
                    fragmentos.add(leerFragmento(in));
                } catch (EOFException e) {
                    throw new ArchivoTxtException("Archivo .txt truncado al leer el fragmento " + (i + 1) + ".", e);
                }
            }

            byte[] marcaFinal = new byte[EOF_MARK.length];
            in.readFully(marcaFinal);
            if (!Arrays.equals(marcaFinal, EOF_MARK)) {
                throw new ArchivoTxtException("Archivo .txt corrupto: no se encontró la marca de fin.");
            }

            return fragmentos;
        } catch (EOFException e) {
            throw new ArchivoTxtException("Archivo .txt truncado o incompleto.", e);
        } catch (IOException e) {
            throw new ArchivoTxtException("Error leyendo '" + rutaArchivo + "': " + e.getMessage(), e);
        }
    }

    private void leerYValidarMagic(DataInputStream in) throws IOException, ArchivoTxtException {
        byte[] magicLeido = new byte[MAGIC.length];
        in.readFully(magicLeido);
        if (!Arrays.equals(magicLeido, MAGIC)) {
            throw new ArchivoTxtException("El archivo no contiene el formato enriquecido de MiniWindows.");
        }
    }

    private TextChunk leerFragmento(DataInputStream in) throws IOException {
        int longTexto = in.readInt();
        validarLongitud(longTexto, "texto");
        byte[] textoBytes = new byte[longTexto];
        in.readFully(textoBytes);
        String texto = new String(textoBytes, StandardCharsets.UTF_8);

        int longFuente = in.readInt();
        validarLongitud(longFuente, "nombre de fuente");
        byte[] fuenteBytes = new byte[longFuente];
        in.readFully(fuenteBytes);
        String fuente = new String(fuenteBytes, StandardCharsets.UTF_8);

        int tamanio = in.readInt();
        byte estilo = in.readByte();
        int colorArgb = in.readInt();
        return new TextChunk(texto, new Formato(fuente, tamanio, estilo, colorArgb));
    }

    private void validarLongitud(int longitud, String campo) throws IOException {
        final int LIMITE_RAZONABLE = 50_000_000;
        if (longitud < 0 || longitud > LIMITE_RAZONABLE) {
            throw new IOException("Longitud inválida para el campo '" + campo + "': " + longitud);
        }
    }
}
