
package pokemon.preparacion;

import pokemon.memoria.Batalla;
import pokemon.memoria.Entrenador;

public final class Sesion {

    private static final Sesion instancia = new Sesion();

    private final ListaEnlazadaUsuario usuarios;

    private User usuarioActual;
    private Entrenador entrenadorActual;
    private Entrenador rivalActual;
    private Batalla batallaActual;

    private Sesion() {
        usuarios = new ListaEnlazadaUsuario();
    }

    public static Sesion getInstancia() {
        return instancia;
    }

    public ListaEnlazadaUsuario getUsuarios() {
        return usuarios;
    }

    public User getUsuarioActual() {
        return usuarioActual;
    }

    public Entrenador getEntrenadorActual() {
        return entrenadorActual;
    }

    public Entrenador getRivalActual() {
        return rivalActual;
    }

    public Batalla getBatallaActual() {
        return batallaActual;
    }

    public boolean hayUsuarioActivo() {
        return usuarioActual != null;
    }

    public void iniciarSesion(User usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario inválido");
        }

        usuarioActual = usuario;
        entrenadorActual = usuario.getEntrenador();

        rivalActual = null;
        batallaActual = null;
    }

    public void setRivalActual(Entrenador rival) {
        if (rival == null) {
            throw new IllegalArgumentException("Rival inválido");
        }

        rivalActual = rival;
    }

    public void setBatallaActual(Batalla batalla) {
        if (batalla == null) {
            throw new IllegalArgumentException("Batalla inválida");
        }

        batallaActual = batalla;
    }

    public void prepararBatalla(Entrenador rival) {
        if (entrenadorActual == null) {
            throw new IllegalStateException("No hay un entrenador activo");
        }

        if (rival == null) {
            throw new IllegalArgumentException("Rival inválido");
        }

        rivalActual = rival;
        batallaActual = new Batalla(entrenadorActual, rivalActual);
    }

    public void limpiarBatalla() {
        rivalActual = null;
        batallaActual = null;
    }

    public void cerrarSesion() {
        usuarioActual = null;
        entrenadorActual = null;
        rivalActual = null;
        batallaActual = null;
    }
}