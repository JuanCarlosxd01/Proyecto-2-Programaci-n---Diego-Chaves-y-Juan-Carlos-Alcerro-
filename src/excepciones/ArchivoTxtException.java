package excepciones;

public class ArchivoTxtException extends Exception {

    public ArchivoTxtException(String mensaje) {
        super(mensaje);
    }

    public ArchivoTxtException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
