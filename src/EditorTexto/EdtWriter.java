

package EditorTexto;

import excepciones.ArchivoedtException;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EdtWriter {

    private static final byte[] MAGIC =
            "EDT1".getBytes(StandardCharsets.US_ASCII);

    private static final byte[] EOF_MARK =
            "EOF1".getBytes(StandardCharsets.US_ASCII);

    private static final int VERSION_ACTUAL = 1;

    public void guardar(
            String rutaArchivo,
            List<TextChunk> fragmentos
    ) throws ArchivoedtException {

        if (fragmentos == null) {
            throw new ArchivoedtException(
                    "La lista de fragmentos no puede ser null"
            );
        }

        if (rutaArchivo.toLowerCase().endsWith(".txt")) {
            DocumentosTexto.guardar(rutaArchivo, fragmentos);
            return;
        }

        if (!rutaArchivo.toLowerCase().endsWith(".edt")) {
            throw new ArchivoedtException(
                    "El archivo debe tener extension .edt (se recibio: "
                    + rutaArchivo + ")"
            );
        }

        try (DataOutputStream salida = new DataOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(rutaArchivo)))) {

            salida.write(MAGIC);
            salida.writeInt(VERSION_ACTUAL);
            salida.writeInt(fragmentos.size());

            for (TextChunk fragmento : fragmentos) {
                escribirFragmento(salida, fragmento);
            }

            salida.write(EOF_MARK);
        } catch (IOException e) {
            throw new ArchivoedtException(
                    "No se pudo escribir el archivo " + rutaArchivo
                    + ": " + e.getMessage(),
                    e
            );
        }
    }

    private void escribirFragmento(
            DataOutputStream salida,
            TextChunk fragmento
    ) throws IOException {

        byte[] textoBytes = fragmento.getTexto()
                .getBytes(StandardCharsets.UTF_8);

        byte[] fuenteBytes = fragmento.getFormato()
                .getFuente().getBytes(StandardCharsets.UTF_8);

        salida.writeInt(textoBytes.length);
        salida.write(textoBytes);

        salida.writeInt(fuenteBytes.length);
        salida.write(fuenteBytes);

        salida.writeInt(fragmento.getFormato().getTamano());
        salida.writeByte(fragmento.getFormato().getEstilo());
        salida.writeInt(fragmento.getFormato().getColorArgb());
    }
}