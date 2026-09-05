
package EditorTexto;

import EditorTexto.Formato;

public class TextChunk {
    private String texto;
    private Formato formato;

    public TextChunk(String texto, Formato formato) {
        this.texto = texto;
        this.formato = formato;
    }

    public String getTexto() {
        return texto;
    }

    public Formato getFormato() {
        return formato;
    }

    
    
    
}
