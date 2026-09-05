
package EditorTexto;
import excepciones.ArchivoedtException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EdtWriter {
    private static final byte[] MAGIC = "EDT1".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] EOF_MARK = "EOF1".getBytes(StandardCharsets.US_ASCII);
    private static final int VERSION_ACTUAL = 1;
    
    public void guardar(String rutaArchivo, List<TextChunk> fragmentos) throws ArchivoedtException{
        if(fragmentos == null){
            throw new ArchivoedtException("La lista de fragmentos no puede ser null");
        }
        if (!rutaArchivo.toLowerCase().endsWith(".edt")) {
            throw new ArchivoedtException("El archivo debe tener extension .edt (se recibio: "+rutaArchivo + ")");
            
        }
        
        try(DataOutputStream out = new DataOutputStream
        (new BufferedOutputStream(new FileOutputStream(rutaArchivo)))){
        out.write(MAGIC);
        out.writeInt(VERSION_ACTUAL);
        out.writeInt(fragmentos.size());
        
        for(TextChunk frag : fragmentos){
        escribirFragmento(out, frag);
        }
        
        out.write(EOF_MARK);
        } catch (IOException e){
        throw new ArchivoedtException("No se pudo escribir el archivo "+rutaArchivo + ": "+e.getMessage(), e);
        }
    }
    
    private void escribirFragmento(DataOutputStream out, TextChunk frag) throws IOException {
    byte[] textoBytes = frag.getTexto().getBytes(StandardCharsets.UTF_8);
        byte[] fuenteBytes = frag.getFormato().getFuente().getBytes(StandardCharsets.UTF_8);
 
        out.writeInt(textoBytes.length);
        out.write(textoBytes);
 
        out.writeInt(fuenteBytes.length);
        out.write(fuenteBytes);
 
        out.writeInt(frag.getFormato().getTamano());
        out.writeByte(frag.getFormato().getEstilo());
        out.writeInt(frag.getFormato().getColorArgb());

    }
}
