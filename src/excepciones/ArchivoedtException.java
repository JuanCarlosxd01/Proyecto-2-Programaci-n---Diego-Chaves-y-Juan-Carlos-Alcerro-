
package excepciones;

public class ArchivoedtException extends Exception{

    public ArchivoedtException(String mensaje) {
        super(mensaje);
    }
    
    public ArchivoedtException(String mensaje, Throwable causa){
    super(mensaje, causa);
    }
    
}
