package EditorTexto;

import excepciones.ArchivoTxtException;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TxtWriter {

    private static final byte[] MAGIC = "MWT1".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] EOF_MARK = "EOF1".getBytes(StandardCharsets.US_ASCII);
    private static final int VERSION_ACTUAL = 1;

    public void guardar(String rutaArchivo, List<TextChunk> fragmentos) throws ArchivoTxtException {
        if (fragmentos == null) {
            throw new ArchivoTxtException("La lista de fragmentos no puede ser null.");
        }
        if (rutaArchivo == null || !rutaArchivo.toLowerCase().endsWith(".txt")) {
            throw new ArchivoTxtException("El archivo del editor debe tener extensión .txt.");
        }

        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(rutaArchivo)))) {
            out.write(MAGIC);
            out.writeInt(VERSION_ACTUAL);
            out.writeInt(fragmentos.size());

            for (TextChunk fragmento : fragmentos) {
                escribirFragmento(out, fragmento);
            }

            out.write(EOF_MARK);
        } catch (IOException e) {
            throw new ArchivoTxtException("No se pudo escribir el archivo " + rutaArchivo + ": " + e.getMessage(), e);
        }
    }

    private void escribirFragmento(DataOutputStream out, TextChunk fragmento) throws IOException {
        byte[] textoBytes = fragmento.getTexto().getBytes(StandardCharsets.UTF_8);
        byte[] fuenteBytes = fragmento.getFormato().getFuente().getBytes(StandardCharsets.UTF_8);

        out.writeInt(textoBytes.length);
        out.write(textoBytes);
        out.writeInt(fuenteBytes.length);
        out.write(fuenteBytes);
        out.writeInt(fragmento.getFormato().getTamano());
        out.writeByte(fragmento.getFormato().getEstilo());
        out.writeInt(fragmento.getFormato().getColorArgb());
    }
}
