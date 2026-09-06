
package excepciones;

public class CuentaDesactivadaException extends Exception {

    private static final long serialVersionUID = 1L;

    public CuentaDesactivadaException() {
        super(
                "Esta cuenta está desactivada. "
                + "Debes reactivarla para realizar esta acción."
        );
    }

    public CuentaDesactivadaException(String mensaje) {
        super(mensaje);
    }

    public CuentaDesactivadaException(
            String mensaje,
            Throwable causa
    ) {
        super(mensaje, causa);
    }
}