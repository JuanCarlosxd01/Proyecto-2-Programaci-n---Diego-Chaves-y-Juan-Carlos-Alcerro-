/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pokemon.memoria;

/**
 *
 * @author diego
 */
public final class Catalogo {
    private Catalogo() {}

    private static Pokemon crear(
            String nombre,
            int nivel,
            int hp,
            Tipo tipo,
            String normal,
            int danoNormal,
            String fuerte,
            int danoFuerte,
            String especial,
            Tipo tipoEspecial,
            Estado.Efecto efecto
    ) {
        return new Pokemon(
                nombre,
                nivel,
                tipo,
                hp,
                new Ataque(normal, tipo, danoNormal, Estado.Efecto.NINGUNO),
                new Ataque(fuerte, tipo, danoFuerte, Estado.Efecto.NINGUNO),
                new Ataque(especial, tipoEspecial, 0, efecto)
        );
    }

    public static ListaEnlazada<Pokemon> todos() {
        ListaEnlazada<Pokemon> lista = new ListaEnlazada<>();

        lista.insertar(crear(
                "Pikachu", 55, 180, Tipo.ELECTRICO,
                "Impactrueno", 35,
                "Rayo", 65,
                "Onda Trueno", Tipo.ELECTRICO, Estado.Efecto.PARALISIS
        ));

        lista.insertar(crear(
                "Charmander", 40, 170, Tipo.FUEGO,
                "Ascuas", 25,
                "Lanzallamas", 50,
                "Fuego Fatuo", Tipo.FUEGO, Estado.Efecto.QUEMADURA
        ));

        lista.insertar(crear(
                "Squirtle", 60, 230, Tipo.AGUA,
                "Pistola Agua", 30,
                "Hidrobomba", 60,
                "Bostezo", Tipo.NORMAL, Estado.Efecto.BOSTEZO
        ));

        lista.insertar(crear(
                "Bulbasaur", 50, 210, Tipo.PLANTA,
                "Látigo Cepa", 40,
                "Rayo Solar", 70,
                "Polvo Veneno", Tipo.VENENO, Estado.Efecto.VENENO
        ));

        lista.insertar(crear(
                "Vulpix", 65, 175, Tipo.FUEGO,
                "Ascuas", 50,
                "Lanzallamas", 80,
                "Fuego Fatuo", Tipo.FUEGO, Estado.Efecto.QUEMADURA
        ));

        lista.insertar(crear(
                "Psyduck", 45, 200, Tipo.AGUA,
                "Pistola Agua", 45,
                "Hidrobomba", 75,
                "Hipnosis", Tipo.PSIQUICO, Estado.Efecto.SUENO
        ));

        lista.insertar(crear(
                "Chikorita", 40, 240, Tipo.PLANTA,
                "Hoja Afilada", 20,
                "Rayo Solar", 45,
                "Polvo Veneno", Tipo.VENENO, Estado.Efecto.VENENO
        ));

        lista.insertar(crear(
                "Mareep", 70, 220, Tipo.ELECTRICO,
                "Impactrueno", 55,
                "Trueno", 85,
                "Onda Trueno", Tipo.ELECTRICO, Estado.Efecto.PARALISIS
        ));

        lista.insertar(crear(
                "Geodude", 60, 260, Tipo.ROCA,
                "Lanzarrocas", 15,
                "Avalancha", 55,
                "Tóxico", Tipo.VENENO, Estado.Efecto.VENENO
        ));

        lista.insertar(crear(
                "Gastly", 75, 160, Tipo.FANTASMA,
                "Lengüetazo", 60,
                "Bola Sombra", 90,
                "Hipnosis", Tipo.PSIQUICO, Estado.Efecto.SUENO
        ));

        return lista;
    }

    public static Pokemon buscar(String nombre) {
        Pokemon pokemon = todos().buscar(
                p -> p.getNombre().equalsIgnoreCase(nombre)
        );

        if (pokemon == null) {
            throw new IllegalArgumentException(
                    "Pokémon desconocido: " + nombre
            );
        }

        return pokemon;
    }
}
