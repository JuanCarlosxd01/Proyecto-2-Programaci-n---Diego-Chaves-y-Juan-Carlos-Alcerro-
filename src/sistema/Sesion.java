
package sistema;

import modelo.*;

public class Sesion {
    private static UsuarioSistema usuarioActual;
    
    public static void iniciarSesion(UsuarioSistema usuario){
        usuarioActual = usuario;
    }
    
    public static void cerrarSesion() {
        usuarioActual = null;
    }
    
    public UsuarioSistema getUsuarioActual(){
        return usuarioActual;
    }
    
    public boolean haySesion(){
        return usuarioActual != null;
    }
    
    public static boolean esAdministrador(){
        return usuarioActual != null && usuarioActual.esAdministrador();
    }
}
