
package pokemon.preparacion;
public class NombreInvalidoExcepcion extends Exception {
 
    public NombreInvalidoExcepcion(String mensaje) {
        super(mensaje);
    }
 
    public NombreInvalidoExcepcion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}