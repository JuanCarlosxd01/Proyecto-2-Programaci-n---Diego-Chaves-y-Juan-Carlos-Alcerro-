
package sistema;

import modelo.*;

public class Sesion {
    private UsuarioSistema usuarioActual;
    
    public void Sesion(UsuarioSistema usuarioActual){
        this.usuarioActual = usuarioActual;
    }
    
    public void cerrar(){
        
    }
    
    public UsuarioSistema getUsuarioActual(){
        return usuarioActual;
    }
    
    public boolean haySesion(){
        return usuarioActual != null;
    }
    
    public boolean esAdministrador(){
        return usuarioActual != null && usuarioActual.esAdministrador();
    }
}
