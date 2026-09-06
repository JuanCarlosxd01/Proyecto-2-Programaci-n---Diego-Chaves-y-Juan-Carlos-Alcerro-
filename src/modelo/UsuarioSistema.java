
package modelo;

import java.io.Serializable;

public class UsuarioSistema implements Serializable{
    private String usuario;
    private String contrasena;
    private TipoUsuario tipo;
    private boolean activo;
    
    public UsuarioSistema(String usuario, String contrasena, TipoUsuario tipo){
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.tipo = tipo;
        this.activo = true;
    }
    
    public String getUsername(){
        return usuario;
    }
    
    public String getContrasena(){
        return contrasena;
    }
    
    public TipoUsuario getTipo(){
        return tipo;
    }
    
    public boolean estaActivo(){
        return activo;
    }
    
    public void setContrasena(String contrasena){
        this.contrasena = contrasena;
    }
    
    public void setActivo(boolean activo){
        this.activo = activo;
    }
    
    public boolean esAdministrador(){
        return tipo == TipoUsuario.ADMINISTRADOR;
    }
}
