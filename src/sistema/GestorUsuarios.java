
package sistema;

import java.io.File;
import java.util.ArrayList;
import modelo.TipoUsuario;
import modelo.UsuarioSistema;

public class GestorUsuarios {

    private ArrayList<UsuarioSistema> usuarios;

    public GestorUsuarios() {
        usuarios = new ArrayList<>();
        crearAdministradorInicial();
    }

    private void crearAdministradorInicial() {
        UsuarioSistema admin = new UsuarioSistema("admin", "admin", TipoUsuario.ADMINISTRADOR);
        usuarios.add(admin);
        crearCarpetasUsuario(admin);
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
        if (!Sesion.esAdministrador()) {
            return false;
        }

        if (buscarUsuario(username) != null) {
            return false;
        }
        UsuarioSistema nuevo =new UsuarioSistema(username, contrasena, TipoUsuario.ESTANDAR);
        usuarios.add(nuevo);
        crearCarpetasUsuario(nuevo);
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