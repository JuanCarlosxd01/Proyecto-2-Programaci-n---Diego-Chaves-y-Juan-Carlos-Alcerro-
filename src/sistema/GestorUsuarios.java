
package sistema;

import java.io.File;
import java.util.ArrayList;
import modelo.TipoUsuario;
import modelo.UsuarioSistema;
import persistencia.*;

public class GestorUsuarios {

    private ArrayList<UsuarioSistema> usuarios;
    private GestorUsuariosBinario gestorBinario;

    public GestorUsuarios() {
        gestorBinario = new GestorUsuariosBinario();
        usuarios = gestorBinario.cargarUsuarios();
        
        if(usuarios.isEmpty()){
            crearAdministradorInicial();
        }
    }

    private void crearAdministradorInicial() {
        UsuarioSistema admin = new UsuarioSistema("admin", "admin", TipoUsuario.ADMINISTRADOR);
        usuarios.add(admin);
        crearCarpetasUsuario(admin);
        gestorBinario.guardarUsuarios(usuarios);
    }

    public UsuarioSistema iniciarSesion(String username, String contrasena) {
        for (UsuarioSistema usuario : usuarios) {
            if (usuario.getUsername().equals(username) && usuario.getContrasena().equals(contrasena)) {
                return usuario;
            }
        }
        return null;
    }

    public boolean crearUsuario(String username, String contrasena) {
        if(buscarUsuario(username) != null){
            return false;
        }
        UsuarioSistema nuevo = new UsuarioSistema(username, contrasena, TipoUsuario.ESTANDAR);
        usuarios.add(nuevo);
        crearCarpetasUsuario(nuevo);
        gestorBinario.guardarUsuarios(usuarios);
        
        return true;
    }

    public UsuarioSistema buscarUsuario(String username) {
        for (UsuarioSistema usuario : usuarios) {
            if (usuario.getUsername().equals(username)) {
                return usuario;
            }
        }
        return null;
    }

    private void crearCarpetasUsuario(UsuarioSistema usuario) {
        File carpetaUsuario = new File("Z/" + usuario.getUsername());
        carpetaUsuario.mkdirs();
        new File(carpetaUsuario, "Mis Documentos").mkdir();
        new File(carpetaUsuario, "Música").mkdir();
        new File(carpetaUsuario, "Mis Imágenes").mkdir();
    }

    public ArrayList<UsuarioSistema> getUsuarios() {
        return usuarios;
    }
} 