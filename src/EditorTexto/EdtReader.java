package EditorTexto;


import excepciones.ArchivoedtException;
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

public class EdtReader {

    private static final byte[] MAGIC =
            "EDT1".getBytes(StandardCharsets.US_ASCII);

    private static final byte[] EOF_MARK =
            "EOF1".getBytes(StandardCharsets.US_ASCII);

    public List<TextChunk> abrir(String rutaArchivo)
            throws ArchivoedtException {

        if (rutaArchivo.toLowerCase().endsWith(".txt")) {
            return DocumentosTexto.abrir(rutaArchivo);
        }

        if (!rutaArchivo.toLowerCase().endsWith(".edt")) {
            throw new ArchivoedtException(
                    "Extensión inválida: se esperaba un archivo .edt"
            );
        }

        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) {
            throw new ArchivoedtException(
                    "El archivo no existe: " + rutaArchivo
            );
        }

        if (archivo.length() == 0) {
            throw new ArchivoedtException(
                    "El archivo está vacío o corrupto (0 bytes): "
                    + rutaArchivo
            );
        }

        try (DataInputStream entrada = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(archivo)))) {

            leerYValidarMagic(entrada);

            int version = entrada.readInt();

            if (version != 1) {
                throw new ArchivoedtException(
                        "Versión de formato no soportada: " + version
                );
            }

            int cantidadFragmentos = entrada.readInt();

            if (cantidadFragmentos < 0) {
                throw new ArchivoedtException(
                        "Cabecera corrupta: cantidad de fragmentos inválida ("
                        + cantidadFragmentos + ")"
                );
            }

            List<TextChunk> fragmentos = new ArrayList<>();

            for (int i = 0; i < cantidadFragmentos; i++) {
                try {
                    fragmentos.add(leerFragmento(entrada));
                } catch (EOFException e) {
                    throw new ArchivoedtException(
                            "Archivo truncado: se esperaban "
                            + cantidadFragmentos
                            + " fragmentos pero el archivo termina "
                            + "en el fragmento " + (i + 1)
                            + " (índice " + i + ")",
                            e
                    );
                }
            }

            byte[] marcaFinal = new byte[4];
            int leidos;

            try {
                leidos = entrada.read(marcaFinal);
            } catch (IOException e) {
                throw new ArchivoedtException(
                        "Error leyendo la marca de fin del archivo",
                        e
                );
            }

            if (leidos != 4 || !Arrays.equals(marcaFinal, EOF_MARK)) {
                throw new ArchivoedtException(
                        "Archivo truncado o corrupto: "
                        + "no se encontró la marca de fin esperada"
                );
            }

            return fragmentos;
        } catch (EOFException e) {
            throw new ArchivoedtException(
                    "Archivo truncado: la cabecera está incompleta",
                    e
            );
        } catch (IOException e) {
            throw new ArchivoedtException(
                    "Error de lectura en '" + rutaArchivo
                    + "': " + e.getMessage(),
                    e
            );
        }
    }

    private void leerYValidarMagic(DataInputStream entrada)
            throws IOException, ArchivoedtException {

        byte[] magicLeido = new byte[4];
        entrada.readFully(magicLeido);

        if (!Arrays.equals(magicLeido, MAGIC)) {
            throw new ArchivoedtException(
                    "El archivo está corrupto o no es un archivo .edt válido "
                    + "(número mágico incorrecto)"
            );
        }
    }

    private TextChunk leerFragmento(DataInputStream entrada)
            throws IOException {

        int longitudTexto = entrada.readInt();
        validarLongitud(longitudTexto, "texto");

        byte[] textoBytes = new byte[longitudTexto];
        entrada.readFully(textoBytes);

        String texto = new String(
                textoBytes,
                StandardCharsets.UTF_8
        );

        int longitudFuente = entrada.readInt();
        validarLongitud(longitudFuente, "nombre de fuente");

        byte[] fuenteBytes = new byte[longitudFuente];
        entrada.readFully(fuenteBytes);

        String fuente = new String(
                fuenteBytes,
                StandardCharsets.UTF_8
        );

        int tamanio = entrada.readInt();
        byte estilo = entrada.readByte();
        int colorArgb = entrada.readInt();

        Formato formato = new Formato(
                fuente,
                tamanio,
                estilo,
                colorArgb
        );

        return new TextChunk(texto, formato);
    }

    private void validarLongitud(int longitud, String campo)
            throws IOException {

        final int LIMITE_RAZONABLE = 50_000_000;

        if (longitud < 0 || longitud > LIMITE_RAZONABLE) {
            throw new IOException(
                    "Longitud inválida para el campo '" + campo
                    + "': " + longitud
                    + " — el archivo probablemente está corrupto"
            );
        }
    }
}