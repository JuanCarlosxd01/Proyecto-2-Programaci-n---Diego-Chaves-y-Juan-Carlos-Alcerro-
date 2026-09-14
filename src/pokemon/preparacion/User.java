
package pokemon.preparacion;

import pokemon.memoria.Entrenador;

public class User {

    private String usuario;
    private String contrasena;
    private Entrenador entrenador;

    public User(String usuario, String contrasena) {
        if (usuario == null || usuario.isBlank()) {
            throw new IllegalArgumentException("El usuario no puede estar vacío");
        }

        if (contrasena == null || contrasena.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }

        this.usuario = usuario;
        this.contrasena = contrasena;
        this.entrenador = new Entrenador(usuario);
    }

    public String getUsuario() {
        return usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public Entrenador getEntrenador() {
        return entrenador;
    }

    public boolean validarContrasena(String contrasenaIngresada) {
        if (contrasenaIngresada == null) {
            return false;
        }

        return contrasena.equals(contrasenaIngresada);
    }

    @Override
    public String toString() {
        return usuario;
    }
}