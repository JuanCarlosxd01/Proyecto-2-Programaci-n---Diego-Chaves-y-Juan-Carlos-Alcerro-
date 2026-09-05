
package sistema;

import java.util.ArrayList;
import modelo.*;

public class GestorUsuarios {
    private ArrayList<UsuarioSistema> usuarios;
    private GestorArchivos gestorArchivos;
    
    public GestorUsuarios(GestorArchivos gestorArchivos){
        usuarios = new ArrayList<>();
        this.gestorArchivos = gestorArchivos;
    }
    
    public UsuarioSistema buscarUsuario(String nombre){
        for(UsuarioSistema usuario : usuarios){
            if(usuario.getUsuario().equalsIgnoreCase(nombre)){
                return usuario;
            }
        }
        return null;
    }
    
    public boolean crearUsuario(String nombre, String contrasena, TipoUsuario tipo){
        if(buscarUsuario(nombre) != null){
            return false;
        }
        
        UsuarioSistema nuevo = new UsuarioSistema(nombre, contrasena, tipo);
        usuarios.add(nuevo);
        gestorArchivos.crearEstructuraUsuario(nuevo);
        return true;
    }
    
    public UsuarioSistema autenticar(String nombre, String contrasena){
        UsuarioSistema usuario = buscarUsuario(nombre);
        if(usuario == null){
            return null;
        }
        if(!usuario.estaActivo()){
            return null;
        }
        if(!usuario.getContrasena().equals(contrasena)){
            return null;
        }
        return usuario;
    }
    
    public ArrayList<UsuarioSistema> getUsuarios(){
        return usuarios;
    } 
        
    
    
}
