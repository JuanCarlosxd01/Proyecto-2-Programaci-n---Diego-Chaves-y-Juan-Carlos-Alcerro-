/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pokemon.memoria;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
/**
 *
 * @author diego
 */
public final class ListaEnlazada<T> implements Iterable<T> {
    private static final class Nodo<T> {
        private T valor;
        private Nodo<T> siguiente;

        private Nodo(T valor) {
            this.valor = valor;
        }
    }

    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int cantidad;

    public void insertar(T valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Valor nulo");
        }

        Nodo<T> nuevo = new Nodo<>(valor);

        if (cola == null) {
            cabeza = nuevo;
        } else {
            cola.siguiente = nuevo;
        }

        cola = nuevo;
        cantidad++;
    }

    public int contar() {
        return cantidad;
    }

    private Nodo<T> nodo(int indice) {
        if (indice < 0 || indice >= cantidad) {
            throw new IllegalArgumentException("Índice inválido: " + indice);
        }

        Nodo<T> actual = cabeza;

        for (int i = 0; i < indice; i++) {
            actual = actual.siguiente;
        }

        return actual;
    }

    public T obtener(int indice) {
        return nodo(indice).valor;
    }

    public void modificar(int indice, T valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Valor nulo");
        }

        nodo(indice).valor = valor;
    }

    public T buscar(Predicate<T> criterio) {
        for (T valor : this) {
            if (criterio.test(valor)) {
                return valor;
            }
        }

        return null;
    }

    public T eliminar(int indice) {
        Nodo<T> eliminado = nodo(indice);

        if (indice == 0) {
            cabeza = eliminado.siguiente;
        } else {
            nodo(indice - 1).siguiente = eliminado.siguiente;
        }

        cantidad--;

        if (cantidad == 0) {
            cola = null;
        } else if (eliminado == cola) {
            cola = nodo(cantidad - 1);
        }

        return eliminado.valor;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = cabeza;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (actual == null) {
                    throw new NoSuchElementException();
                }

                T valor = actual.valor;
                actual = actual.siguiente;
                return valor;
            }
        };
    }
}
