/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pokemon.memoria;

/**
 *
 * @author diego
 */
public final class Entrenador {

    public record Existencia(Objeto objeto, int cantidad) {}

    private final String nombre;

    private final ListaEnlazada<Pokemon> equipo = new ListaEnlazada<>();
    private final ListaEnlazada<Existencia> inventario = new ListaEnlazada<>();

    private int activo = -1;

    public Entrenador(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre vacío");
        }

        this.nombre = nombre;

        inventario.insertar(new Existencia(Objeto.POCION, 2));
        inventario.insertar(new Existencia(Objeto.SUPERPOCION, 2));
        inventario.insertar(new Existencia(Objeto.REVIVIR, 1));
        inventario.insertar(new Existencia(Objeto.ANTIDOTO, 2));
        inventario.insertar(new Existencia(Objeto.ANTIPARALIZADOR, 2));
    }

    public String getNombre() {
        return nombre;
    }

    public int getIndiceActivo() {
        return activo;
    }

    public int contar() {
        return equipo.contar();
    }

    public Pokemon getPokemon(int indice) {
        return equipo.obtener(indice).copiar();
    }

    public Pokemon getActivo() {
        if (activo < 0 || activo >= equipo.contar()) {
            return null;
        }

        return getPokemon(activo);
    }

    public void agregar(Pokemon pokemon) {
        if (pokemon == null) {
            throw new IllegalArgumentException("Pokémon nulo");
        }
        
        if (equipo.contar() >= 4) {
            throw new IllegalArgumentException("El equipo no puede tener más de 4 Pokémon");
        }
        equipo.insertar(pokemon.copiar());

        if (activo < 0 && !pokemon.estaDerrotado()) {
            activo = equipo.contar() - 1;
        }
    }

    public Pokemon buscar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }

        Pokemon encontrado = equipo.buscar(pokemon -> pokemon.getNombre().equalsIgnoreCase(nombre));

        if (encontrado == null) {
            return null;
        }

        return encontrado.copiar();
    }

    public int buscarIndice(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return -1;
        }

        for (int i = 0; i < equipo.contar(); i++) {
            if (equipo.obtener(i).getNombre().equalsIgnoreCase(nombre)) {
                return i;
            }
        }

        return -1;
    }

    public boolean contieneEnEquipo(String nombre) {
        return buscarIndice(nombre) != -1;
    }

    public void eliminar(int indice) {
        if (indice < 0 || indice >= equipo.contar()) {
            throw new IllegalArgumentException("Índice inválido");
        }

        equipo.eliminar(indice);

        if (equipo.contar() == 0) {
            activo = -1;
            return;
        }

        if (indice < activo) {
            activo--;
        } else if (indice == activo) {
            activo = -1;

            for (int i = 0; i < equipo.contar(); i++) {
                if (!interno(i).estaDerrotado()) {
                    activo = i;
                    break;
                }
            }
        }
    }

    public void modificar(int indice, Pokemon pokemon) {
        if (pokemon == null) {
            throw new IllegalArgumentException("Pokémon nulo");
        }

        if (indice < 0 || indice >= equipo.contar()) {
            throw new IllegalArgumentException("Índice inválido");
        }

        equipo.modificar(indice, pokemon.copiar());

        if (activo < 0 || activo >= equipo.contar() || interno(activo).estaDerrotado()) {
            activo = primerDisponible();
        }
    }

    public int disponibles() {
        int total = 0;

        for (Pokemon pokemon : equipo) {
            if (!pokemon.estaDerrotado()) {
                total++;
            }
        }

        return total;
    }

    public int primerDisponible() {
        for (int i = 0; i < equipo.contar(); i++) {
            if (!interno(i).estaDerrotado()) {
                return i;
            }
        }

        return -1;
    }

    public int siguienteDisponible() {
        if (equipo.contar() == 0) {
            return -1;
        }

        if (activo < 0) {
            return primerDisponible();
        }

        for (int i = 1; i <= equipo.contar(); i++) {
            int indice = (activo + i) % equipo.contar();

            if (!interno(indice).estaDerrotado()) {
                return indice;
            }
        }

        return -1;
    }

    public ListaEnlazada<Pokemon> getEquipo() {
        ListaEnlazada<Pokemon> copia = new ListaEnlazada<>();

        for (Pokemon pokemon : equipo) {
            copia.insertar(pokemon.copiar());
        }

        return copia;
    }

    public ListaEnlazada<Existencia> getInventario() {
        ListaEnlazada<Existencia> copia = new ListaEnlazada<>();

        for (Existencia existencia : inventario) {
            copia.insertar(existencia);
        }

        return copia;
    }

    public int cantidad(Objeto objeto) {
        if (objeto == null) {
            return 0;
        }

        Existencia existencia = inventario.buscar(e -> e.objeto() == objeto);

        if (existencia == null) {
            return 0;
        }

        return existencia.cantidad();
    }

    public boolean tieneObjeto(Objeto objeto) {
        return cantidad(objeto) > 0;
    }

    Pokemon interno(int indice) {
        return equipo.obtener(indice);
    }

    Pokemon activoInterno() {
        if (activo < 0 || activo >= equipo.contar()) {
            throw new IllegalStateException("No hay Pokémon activo");
        }

        return interno(activo);
    }

    void cambiar(int indice) {
        if (indice < 0 || indice >= equipo.contar()) {
            throw new IllegalArgumentException("Índice inválido");
        }

        if (interno(indice).estaDerrotado()) {
            throw new IllegalArgumentException("Pokémon derrotado");
        }

        activo = indice;
    }

    void consumir(Objeto objeto) {
        if (objeto == null) {
            throw new IllegalArgumentException("Objeto nulo");
        }

        for (int i = 0; i < inventario.contar(); i++) {
            Existencia existencia = inventario.obtener(i);

            if (existencia.objeto() == objeto) {
                if (existencia.cantidad() <= 0) {
                    throw new IllegalArgumentException("Objeto agotado");
                }

                inventario.modificar(i, new Existencia(objeto, existencia.cantidad() - 1));
                return;
            }
        }

        throw new IllegalArgumentException("Objeto no encontrado");
    }

    Entrenador copiar(boolean reiniciar) {
        Entrenador copia = new Entrenador(nombre);

        for (Pokemon pokemon : equipo) {
            if (reiniciar) {
                copia.agregar(pokemon.nuevo());
            } else {
                copia.agregar(pokemon);
            }
        }

        copia.activo = activo;

        if (!reiniciar) {
            for (int i = 0; i < inventario.contar(); i++) {
                copia.inventario.modificar(i, inventario.obtener(i));
            }
        }

        return copia;
    }
}