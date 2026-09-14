/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pokemon.memoria;

/**
 *
 * @author diego
 */
public enum Objeto {

    POCION(20, Estado.Efecto.NINGUNO),
    SUPERPOCION(50, Estado.Efecto.NINGUNO),
    REVIVIR(0, Estado.Efecto.NINGUNO),
    ANTIDOTO(0, Estado.Efecto.VENENO),
    ANTIPARALIZADOR(0, Estado.Efecto.PARALISIS);

    private final int curacion;
    private final Estado.Efecto cura;

    Objeto(int curacion, Estado.Efecto cura) {
        this.curacion = curacion;
        this.cura = cura;
    }

    public int getCuracion() {
        return curacion;
    }

    public Estado.Efecto getCura() {
        return cura;
    }

    public String getNombre() {
        switch (this) {
            case POCION:
                return "Poción";

            case SUPERPOCION:
                return "Superpoción";

            case REVIVIR:
                return "Revivir";

            case ANTIDOTO:
                return "Antídoto";

            case ANTIPARALIZADOR:
                return "Antiparalizador";

            default:
                return name();
        }
    }

    public String getDescripcion() {
        switch (this) {
            case POCION:
                return "Recupera 20 HP";

            case SUPERPOCION:
                return "Recupera 50 HP";

            case REVIVIR:
                return "Revive un Pokémon derrotado";

            case ANTIDOTO:
                return "Cura el veneno";

            case ANTIPARALIZADOR:
                return "Cura la parálisis";

            default:
                return "";
        }
    }

    public boolean sePuedeUsar(Pokemon pokemon) {
        if (pokemon == null) {
            return false;
        }

        if (this == REVIVIR) {
            return pokemon.estaDerrotado();
        }

        if (pokemon.estaDerrotado()) {
            return false;
        }

        if (curacion > 0) {
            return pokemon.getHp() < pokemon.getHpMaximo();
        }

        return pokemon.getEstado() == cura;
    }

    int aplicar(Pokemon pokemon) {
        if (!sePuedeUsar(pokemon)) {
            throw new IllegalArgumentException("El objeto no tiene efecto sobre este Pokémon");
        }

        if (this == REVIVIR) {
            return pokemon.revivir();
        }

        if (curacion > 0) {
            return pokemon.curar(curacion);
        }

        pokemon.curarEstado();

        return 0;
    }
}