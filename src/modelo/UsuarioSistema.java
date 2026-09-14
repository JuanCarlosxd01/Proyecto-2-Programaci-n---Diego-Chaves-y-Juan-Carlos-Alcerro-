
package modelo;

import java.io.Serializable;

public class UsuarioSistema implements Serializable {

    private static final long serialVersionUID = 1L;

    private String usuario;
    private String contrasena;
    private TipoUsuario tipo;
    private boolean activo;

    public UsuarioSistema(String usuario, String contrasena, TipoUsuario tipo) {
        if (usuario == null || usuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío.");
        }

        if (!contrasenaValida(contrasena)) {
            throw new IllegalArgumentException("La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un símbolo.");
        }

        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de usuario no puede ser nulo.");
        }

        this.usuario = usuario.trim();
        this.contrasena = contrasena;
        this.tipo = tipo;
        this.activo = true;
    }

    public String getUsername() {
        return usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public boolean estaActivo() {
        return activo;
    }

    public boolean esAdministrador() {
        return tipo == TipoUsuario.ADMINISTRADOR;
    }

    public boolean esUsuarioEstandar() {
        return tipo == TipoUsuario.ESTANDAR;
    }

    public void setContrasena(String nuevaContrasena) {
        if (!contrasenaValida(nuevaContrasena)) {
            throw new IllegalArgumentException("La contraseña no cumple los requisitos de seguridad.");
        }

        this.contrasena = nuevaContrasena;
    }

    public static boolean contrasenaValida(String contrasena) {
        if (contrasena == null || contrasena.length() < 8) {
            return false;
        }

        boolean mayuscula = false;
        boolean numero = false;
        boolean simbolo = false;

        for (char caracter : contrasena.toCharArray()) {
            if (Character.isUpperCase(caracter)) {
                mayuscula = true;
            } else if (Character.isDigit(caracter)) {
                numero = true;
            } else if (!Character.isLetterOrDigit(caracter)) {
                simbolo = true;
            }
        }

        return mayuscula && numero && simbolo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return usuario;
    }
}