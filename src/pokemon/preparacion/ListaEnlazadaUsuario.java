
package pokemon.preparacion;

public class ListaEnlazadaUsuario {

    private NodoUser cabeza;
    private int cuenta;

    public ListaEnlazadaUsuario() {
        cabeza = null;
        cuenta = 0;
    }

    public void insertar(User nuevoUsuario) throws NombreInvalidoExcepcion {
        if (nuevoUsuario == null) {
            throw new NombreInvalidoExcepcion("El usuario no puede ser nulo.");
        }

        if (nuevoUsuario.getUsuario() == null || nuevoUsuario.getUsuario().trim().isEmpty()) {
            throw new NombreInvalidoExcepcion("El nombre de usuario no puede estar vacío.");
        }

        if (existeUser(nuevoUsuario.getUsuario())) {
            throw new NombreInvalidoExcepcion("Ya existe un usuario con ese nombre.");
        }

        NodoUser nuevoNodo = new NodoUser(nuevoUsuario);

        if (cabeza == null) {
            cabeza = nuevoNodo;
        } else {
            NodoUser actual = cabeza;

            while (actual.getSiguiente() != null) {
                actual = actual.getSiguiente();
            }

            actual.setSiguiente(nuevoNodo);
        }

        cuenta++;
    }

    public User buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return null;
        }

        NodoUser actual = cabeza;

        while (actual != null) {
            if (actual.getDato().getUsuario().equalsIgnoreCase(nombre.trim())) {
                return actual.getDato();
            }

            actual = actual.getSiguiente();
        }

        return null;
    }

    public boolean existeUser(String nombre) {
        return buscarPorNombre(nombre) != null;
    }

    public User autenticar(String nombreUsuario, String password) {
        if (nombreUsuario == null || password == null) {
            return null;
        }

        User usuario = buscarPorNombre(nombreUsuario);

        if (usuario == null) {
            return null;
        }

        if (!usuario.validarContrasena(password)) {
            return null;
        }

        return usuario;
    }

    public boolean eliminar(String nombreUsuario) {
        if (cabeza == null || nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            return false;
        }

        if (cabeza.getDato().getUsuario().equalsIgnoreCase(nombreUsuario.trim())) {
            cabeza = cabeza.getSiguiente();
            cuenta--;
            return true;
        }

        NodoUser anterior = cabeza;
        NodoUser actual = cabeza.getSiguiente();

        while (actual != null) {
            if (actual.getDato().getUsuario().equalsIgnoreCase(nombreUsuario.trim())) {
                anterior.setSiguiente(actual.getSiguiente());
                cuenta--;
                return true;
            }

            anterior = actual;
            actual = actual.getSiguiente();
        }

        return false;
    }

    public User[] listarTodos() {
        User[] usuarios = new User[cuenta];

        NodoUser actual = cabeza;
        int posicion = 0;

        while (actual != null) {
            usuarios[posicion] = actual.getDato();
            actual = actual.getSiguiente();
            posicion++;
        }

        return usuarios;
    }

    public User obtener(int indice) {
        if (indice < 0 || indice >= cuenta) {
            throw new IndexOutOfBoundsException("Índice de usuario inválido.");
        }

        NodoUser actual = cabeza;
        int posicion = 0;

        while (posicion < indice) {
            actual = actual.getSiguiente();
            posicion++;
        }

        return actual.getDato();
    }

    public int contar() {
        return cuenta;
    }

    public boolean estaVacia() {
        return cabeza == null;
    }
}