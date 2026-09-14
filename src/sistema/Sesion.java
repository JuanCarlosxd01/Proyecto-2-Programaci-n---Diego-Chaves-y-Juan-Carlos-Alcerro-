
package sistema;

import modelo.UsuarioSistema;

public class Sesion {

    private static UsuarioSistema usuarioActual;

    private Sesion() {

    }

    public static void iniciarSesion(UsuarioSistema usuario) {
        usuarioActual = usuario;
    }

    public static void cerrarSesion() {
        usuarioActual = null;
    }

    public static UsuarioSistema getUsuarioActual() {
        return usuarioActual;
    }

    public static boolean haySesion() {
        return usuarioActual != null;
    }

    public static boolean esAdministrador() {
        return usuarioActual != null && usuarioActual.esAdministrador();
    }

    public static String getNombreUsuario() {
        if (usuarioActual == null) {
            return "";
        }

        return usuarioActual.getUsername();
    }
}