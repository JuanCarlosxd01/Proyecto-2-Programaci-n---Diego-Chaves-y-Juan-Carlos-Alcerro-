
package excepciones;

public class UsernameDuplicadoException extends Exception {

    private static final long serialVersionUID = 1L;

    public UsernameDuplicadoException() {
        super("Ese nombre de usuario ya está registrado.");
    }

    public UsernameDuplicadoException(String mensaje) {
        super(mensaje);
    }

    public UsernameDuplicadoException(
            String mensaje,
            Throwable causa
    ) {
        super(mensaje, causa);
    }
}