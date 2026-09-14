/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pokemon.memoria;

/**
 *
 * @author diego
 */
public final class Pokemon {
    private final String nombre;
    private final int nivel;
    private final Tipo tipo;
    private final int hpMaximo;
    private int hp;
    private Estado estado = Estado.crear(Estado.Efecto.NINGUNO);

    private final ListaEnlazada<Ataque> ataques = new ListaEnlazada<>();

    public Pokemon(String nombre, int nivel, Tipo tipo, int hpMaximo, Ataque normal, Ataque fuerte, Ataque especial) {
        if (nombre == null || nombre.isBlank() || nivel < 1 || nivel > 100 || tipo == null || hpMaximo < 1 || normal == null || fuerte == null || especial == null || normal.efecto() != Estado.Efecto.NINGUNO || fuerte.efecto() != Estado.Efecto.NINGUNO || normal.dano() <= 0 || fuerte.dano() <= normal.dano() || especial.efecto() == Estado.Efecto.NINGUNO) {
            throw new IllegalArgumentException("Pokémon inválido");
        }

        this.nombre = nombre;
        this.nivel = nivel;
        this.tipo = tipo;
        this.hpMaximo = hpMaximo;
        this.hp = hpMaximo;

        ataques.insertar(normal);
        ataques.insertar(fuerte);
        ataques.insertar(especial);
    }

    public String getNombre() {
        return nombre;
    }

    public int getNivel() {
        return nivel;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public int getHp() {
        return hp;
    }

    public int getHpMaximo() {
        return hpMaximo;
    }

    public boolean estaDerrotado() {
        return hp == 0;
    }

    public Estado.Efecto getEstado() {
        return estado.getEfecto();
    }

    public int getTurnosEstado() {
        return estado.getTurnos();
    }

    public Ataque getAtaque(int indice) {
        return ataques.obtener(indice);
    }

    public int cantidadAtaques() {
        return ataques.contar();
    }

    Estado estado() {
        return estado;
    }

    void curarEstado() {
        estado = Estado.crear(Estado.Efecto.NINGUNO);
    }

    void aplicarEstado(Estado.Efecto efecto) {
        estado = Estado.crear(efecto);
    }

    int recibirDano(int dano) {
        if (dano < 0) {
            throw new IllegalArgumentException("Daño negativo");
        }

        int danoReal = Math.min(hp, dano);
        hp -= danoReal;

        return danoReal;
    }

    int curar(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("Curación negativa");
        }

        if (estaDerrotado()) {
            throw new IllegalArgumentException("Pokémon derrotado");
        }

        int curacionReal = Math.min(hpMaximo - hp, cantidad);
        hp += curacionReal;

        return curacionReal;
    }

    int revivir() {
        if (!estaDerrotado()) {
            throw new IllegalArgumentException("El Pokémon no está derrotado");
        }

        hp = Math.max(1, hpMaximo / 2);
        estado = Estado.crear(Estado.Efecto.NINGUNO);

        return hp;
    }

    Pokemon copiar() {
        Pokemon copia = nuevo();

        copia.hp = hp;
        copia.estado = estado.copiar();

        return copia;
    }

    Pokemon nuevo() {
        return new Pokemon(nombre, nivel, tipo, hpMaximo, getAtaque(0), getAtaque(1), getAtaque(2));
    }
}