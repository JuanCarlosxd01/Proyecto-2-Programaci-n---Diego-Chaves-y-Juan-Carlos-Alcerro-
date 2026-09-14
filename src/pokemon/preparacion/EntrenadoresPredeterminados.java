
package pokemon.preparacion;

import pokemon.memoria.Entrenador;
import pokemon.memoria.ListaEnlazada;
import pokemon.memoria.Pokemon;
import pokemon.memoria.Catalogo;
import java.util.Random;

public final class EntrenadoresPredeterminados {
    private EntrenadoresPredeterminados() {}
    
    private static Entrenador crear(String nombre, String... pokemones) {   
        Entrenador entrenador = new Entrenador(nombre);

        for (String nombrePokemon : pokemones) {
            entrenador.agregar(Catalogo.buscar(nombrePokemon));
        }

        return entrenador;
    }
    
    public static ListaEnlazada<Entrenador> todos() {
        ListaEnlazada<Entrenador> lista = new ListaEnlazada<>();

        lista.insertar(crear("Leo", "Pikachu", "Charmander", "Squirtle", "Bulbasaur"));
        lista.insertar(crear("Damian", "Psyduck", "Squirtle", "Bulbasaur", "Vulpix"));
        lista.insertar(crear("Diego", "Geodude", "Bulbasaur", "Chikorita", "Mareep"));
        lista.insertar(crear("Jose", "Charmander", "Vulpix", "Mareep", "Gastly"));
        lista.insertar(crear("Juan", "Bulbasaur", "Chikorita", "Psyduck", "Pikachu"));
        lista.insertar(crear("Carlos", "Squirtle", "Psyduck", "Pikachu", "Chikorita"));
        lista.insertar(crear("Erick", "Gastly", "Geodude", "Mareep", "Charmander"));
        lista.insertar(crear("Abraham", "Vulpix", "Gastly", "Chikorita", "Squirtle"));
        lista.insertar(crear("Edgar", "Gastly", "Mareep", "Geodude", "Bulbasaur"));
        lista.insertar(crear("Romero", "Pikachu", "Charmander", "Bulbasaur", "Geodude"));


        return lista;
    }

    public static Entrenador aleatorio() {
        ListaEnlazada<Entrenador> lista = todos();
        Random azar = new Random();

        return lista.obtener(azar.nextInt(lista.contar()));
    }
}

