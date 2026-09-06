
package excepciones;

public class ArchivoCorruptoException extends Exception {

    private static final long serialVersionUID = 1L;

    public ArchivoCorruptoException() {
        super(
                "El archivo está dañado o "
                + "no tiene el formato esperado."
        );
    }

    public ArchivoCorruptoException(String mensaje) {
        super(mensaje);
    }

    public ArchivoCorruptoException(
            String mensaje,
            Throwable causa
    ) {
        super(mensaje, causa);
    }
}