package sistema;

import java.io.File;
import java.util.ArrayList;
import modelo.ConfiguracionSistema;
import modelo.TipoUsuario;
import modelo.UsuarioSistema;
import persistencia.GestorUsuariosBinario;

public class GestorUsuarios {

    private ArrayList<UsuarioSistema> usuarios;
    private GestorUsuariosBinario gestorBinario;

    public GestorUsuarios() {
        gestorBinario = new GestorUsuariosBinario();

        usuarios = gestorBinario.cargarUsuarios();

        if (usuarios == null) {
            System.err.println("No se pudieron cargar los usuarios guardados.");

            gestorBinario.respaldarArchivoCorrupto();

            usuarios = new ArrayList<>();
        }

        if (usuarios.isEmpty()) {
            crearAdministradorInicial();
        }

        for (UsuarioSistema usuario : usuarios) {
            crearCarpetasUsuario(usuario);
        }
    }

    private void crearAdministradorInicial() {
        if (buscarUsuario(ConfiguracionSistema.ADMIN_USUARIO) != null) {
            return;
        }

        UsuarioSistema admin = new UsuarioSistema(
                ConfiguracionSistema.ADMIN_USUARIO,
                ConfiguracionSistema.ADMIN_CONTRASENA,
                TipoUsuario.ADMINISTRADOR
        );

        usuarios.add(admin);

        crearCarpetasUsuario(admin);

        gestorBinario.guardarUsuarios(usuarios);
    }

    public UsuarioSistema iniciarSesion(String username, String contrasena) {
        if (username == null || contrasena == null) {
            return null;
        }

        username = username.trim();

        if (username.isEmpty() || contrasena.isEmpty()) {
            return null;
        }

        for (UsuarioSistema usuario : usuarios) {
            boolean mismoUsuario = usuario.getUsername().equalsIgnoreCase(username);
            boolean mismaContrasena = usuario.getContrasena().equals(contrasena);
            boolean usuarioActivo = usuario.estaActivo();

            if (mismoUsuario && mismaContrasena && usuarioActivo) {
                return usuario;
            }
        }

        return null;
    }

    public boolean crearUsuario(String username, String contrasena) {
        return crearUsuario(username, contrasena, TipoUsuario.ESTANDAR);
    }

    public boolean crearUsuario(String username, String contrasena, TipoUsuario tipo) {
        if (!Sesion.esAdministrador()) {
            return false;
        }

        if (username == null || contrasena == null || tipo == null) {
            return false;
        }

        username = username.trim();

        if (username.isEmpty()) {
            return false;
        }

        if (!UsuarioSistema.contrasenaValida(contrasena)) {
            return false;
        }

        if (buscarUsuario(username) != null) {
            return false;
        }

        UsuarioSistema nuevo = new UsuarioSistema(username, contrasena, tipo);

        usuarios.add(nuevo);

        crearCarpetasUsuario(nuevo);

        if (!gestorBinario.guardarUsuarios(usuarios)) {
            usuarios.remove(nuevo);

            return false;
        }

        return true;
    }

    public boolean eliminarUsuario(String username) {
        if (!Sesion.esAdministrador()) {
            return false;
        }

        if (username == null || username.isBlank()) {
            return false;
        }

        UsuarioSistema usuario = buscarUsuario(username);

        if (usuario == null) {
            return false;
        }

        if (usuario.esAdministrador()) {
            return false;
        }

        usuarios.remove(usuario);

        if (!gestorBinario.guardarUsuarios(usuarios)) {
            usuarios.add(usuario);

            return false;
        }

       File carpetaUsuario = new File("Z", usuario.getUsername());

        eliminarRecursivamente(carpetaUsuario);

        return true;
    }

    private boolean eliminarRecursivamente(File archivo) {
        if (archivo == null || !archivo.exists()) {
            return true;
        }

        if (archivo.isDirectory()) {
            File[] hijos = archivo.listFiles();

            if (hijos != null) {
                for (File hijo : hijos) {
                    if (!eliminarRecursivamente(hijo)) {
                        return false;
                    }
                }
            }
        }

        return archivo.delete();
    }

    public UsuarioSistema buscarUsuario(String username) {
        if (username == null) {
            return null;
        }

        for (UsuarioSistema usuario : usuarios) {
            if (usuario.getUsername().equalsIgnoreCase(username.trim())) {
                return usuario;
            }
        }

        return null;
    }

    public boolean desactivarUsuario(String username) {
        if (!Sesion.esAdministrador()) {
            return false;
        }

        UsuarioSistema usuario = buscarUsuario(username);

        if (usuario == null) {
            return false;
        }

        if (usuario.esAdministrador()) {
            return false;
        }

        usuario.setActivo(false);

        gestorBinario.guardarUsuarios(usuarios);

        return true;
    }

    public boolean activarUsuario(String username) {
        if (!Sesion.esAdministrador()) {
            return false;
        }

        UsuarioSistema usuario = buscarUsuario(username);

        if (usuario == null) {
            return false;
        }

        usuario.setActivo(true);

        gestorBinario.guardarUsuarios(usuarios);

        return true;
    }

    public boolean cambiarContrasena(String username, String nuevaContrasena) {
        if (username == null || nuevaContrasena == null) {
            return false;
        }

        UsuarioSistema usuario = buscarUsuario(username);

        if (usuario == null || !UsuarioSistema.contrasenaValida(nuevaContrasena)) {
            return false;
        }

        if (!Sesion.esAdministrador()) {
            UsuarioSistema usuarioActual = Sesion.getUsuarioActual();

            if (usuarioActual == null || !usuarioActual.getUsername().equalsIgnoreCase(username)) {
                return false;
            }
        }

        usuario.setContrasena(nuevaContrasena);

        gestorBinario.guardarUsuarios(usuarios);

        return true;
    }

    private void crearCarpetasUsuario(UsuarioSistema usuario) {
        RutasSistema.crearEstructuraUsuario(usuario);
    }

    public void guardarUsuarios() {
        gestorBinario.guardarUsuarios(usuarios);
    }

    public ArrayList<UsuarioSistema> getUsuarios() {
        return new ArrayList<>(usuarios);
    }
}