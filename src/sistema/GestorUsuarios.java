
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
        
        for (UsuarioSistema usuario : usuarios) {
            crearCarpetasUsuario(usuario);
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
        if (!carpetaUsuario.exists()) {
            carpetaUsuario.mkdirs();
        }
        File documentos = new File(carpetaUsuario, "Mis Documentos");
        File musica = new File(carpetaUsuario, "Música");
        File imagenes = new File(carpetaUsuario, "Mis Imágenes");
        
        if (!documentos.exists()) {
            documentos.mkdirs();
        }

        if (!musica.exists()) {
            musica.mkdirs();
        }

        if (!imagenes.exists()) {
            imagenes.mkdirs();
        }
    }

    public ArrayList<UsuarioSistema> getUsuarios() {
        return usuarios;
    }
} 