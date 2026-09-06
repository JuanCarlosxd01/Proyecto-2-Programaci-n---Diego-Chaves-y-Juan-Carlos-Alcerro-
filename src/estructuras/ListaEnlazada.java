
package estructuras;


import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;


public class ListaEnlazada<T> implements Iterable<T>, Serializable {

    private static final long serialVersionUID = 1L;

    private transient Nodo<T> primero;
    private transient Nodo<T> ultimo;
    private transient int cantidad;

    public ListaEnlazada() {
        primero = null;
        ultimo = null;
        cantidad = 0;
    }

    public int size() {
        return cantidad;
    }

    public boolean isEmpty() {
        return cantidad == 0;
    }

    public void agregar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);

        if (isEmpty()) {
            primero = nuevo;
            ultimo = nuevo;
        } else {
            ultimo.setSiguiente(nuevo);
            ultimo = nuevo;
        }

        cantidad++;
    }

    public void agregarAlInicio(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);

        nuevo.setSiguiente(primero);
        primero = nuevo;

        if (ultimo == null) {
            ultimo = nuevo;
        }

        cantidad++;
    }

    public T obtener(int indice) {
        validarIndice(indice);

        Nodo<T> actual = primero;

        for (int i = 0; i < indice; i++) {
            actual = actual.getSiguiente();
        }

        return actual.getDato();
    }

    public boolean contiene(T dato) {
        Nodo<T> actual = primero;

        while (actual != null) {
            if (Objects.equals(actual.getDato(), dato)) {
                return true;
            }

            actual = actual.getSiguiente();
        }

        return false;
    }

    public boolean agregarUnico(T dato) {
        if (contiene(dato)) {
            return false;
        }

        agregar(dato);
        return true;
    }

    public T buscar(Predicate<T> condicion) {
        Objects.requireNonNull(
                condicion,
                "La condición de búsqueda no puede estar vacia"
        );

        Nodo<T> actual = primero;

        while (actual != null) {
            if (condicion.test(actual.getDato())) {
                return actual.getDato();
            }

            actual = actual.getSiguiente();
        }

        return null;
    }

    public ListaEnlazada<T> filtrar(Predicate<T> condicion) {
        Objects.requireNonNull(
                condicion,
                "La condición del filtro no puede estar vacia"
        );

        ListaEnlazada<T> resultado = new ListaEnlazada<>();

        for (T dato : this) {
            if (condicion.test(dato)) {
                resultado.agregar(dato);
            }
        }

        return resultado;
    }

    public boolean eliminar(T dato) {
        Nodo<T> actual = primero;
        Nodo<T> anterior = null;

        while (actual != null) {
            if (Objects.equals(actual.getDato(), dato)) {
                desvincular(anterior, actual);
                return true;
            }

            anterior = actual;
            actual = actual.getSiguiente();
        }

        return false;
    }

    public int eliminarSi(Predicate<T> condicion) {
        Objects.requireNonNull(
                condicion,
                "La condición de eliminación no puede estar vacia"
        );

        Nodo<T> actual = primero;
        Nodo<T> anterior = null;

        int eliminados = 0;

        while (actual != null) {
            Nodo<T> siguiente = actual.getSiguiente();

            if (condicion.test(actual.getDato())) {
                desvincular(anterior, actual);
                eliminados++;
            } else {
                anterior = actual;
            }

            actual = siguiente;
        }

        return eliminados;
    }

    private void desvincular(
            Nodo<T> anterior,
            Nodo<T> actual
    ) {
        if (anterior == null) {
            primero = actual.getSiguiente();
        } else {
            anterior.setSiguiente(actual.getSiguiente());
        }

        if (actual == ultimo) {
            ultimo = anterior;
        }

        cantidad--;
    }

    public void insertarOrdenado(
            T dato,
            Comparator<? super T> comparador
    ) {
        Objects.requireNonNull(
                comparador,
                "El comparador no puede estar vacio"
        );

        Nodo<T> nuevo = new Nodo<>(dato);
        Nodo<T> actual = primero;
        Nodo<T> anterior = null;

        while (actual != null
                && comparador.compare(actual.getDato(), dato) <= 0) {

            anterior = actual;
            actual = actual.getSiguiente();
        }

        nuevo.setSiguiente(actual);

        if (anterior == null) {
            primero = nuevo;
        } else {
            anterior.setSiguiente(nuevo);
        }

        if (actual == null) {
            ultimo = nuevo;
        }

        cantidad++;
    }

    public void limpiar() {
        primero = null;
        ultimo = null;
        cantidad = 0;
    }

    private void validarIndice(int indice) {
        if (indice < 0 || indice >= cantidad) {
            throw new IndexOutOfBoundsException(
                    "Índice fuera de rango: " + indice
                    + ". Cantidad de elementos: " + cantidad
            );
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private Nodo<T> actual = primero;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException(
                            "No hay más elementos en la lista."
                    );
                }

                T dato = actual.getDato();
                actual = actual.getSiguiente();

                return dato;
            }
        };
    }

    private void writeObject(ObjectOutputStream salida)
            throws IOException {

        salida.writeInt(cantidad);

        for (T dato : this) {
            salida.writeObject(dato);
        }
    }


    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream entrada)
            throws IOException, ClassNotFoundException {

        primero = null;
        ultimo = null;
        cantidad = 0;

        int total = entrada.readInt();

        if (total < 0 || total > 100_000) {
            throw new InvalidObjectException(
                    "Cantidad de elementos inválida: " + total
            );
        }

        for (int i = 0; i < total; i++) {
            T dato = (T) entrada.readObject();
            agregar(dato);
        }
    }
}