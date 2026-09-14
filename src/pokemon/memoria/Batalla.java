/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pokemon.memoria;
import java.util.Random;

/**
 *
 * @author diego
 */
public final class Batalla {
    public enum Resultado {
        EN_CURSO, VICTORIA, DERROTA, EMPATE
    }

    public record Evento(int ronda, String mensaje) {}

    public record Estadisticas(
            int rondas,
            int danoJugador,
            int danoRival,
            int objetosJugador,
            int objetosRival,
            int derrotadosJugador,
            int derrotadosRival
    ) {}

    private final Entrenador inicialJugador;
    private final Entrenador inicialRival;
    private final Random azar;

    private Entrenador jugador;
    private Entrenador rival;
    private ListaEnlazada<Evento> historial;
    private Resultado resultado;
    private boolean turnoRivalPendiente;

    private int ronda;
    private int danoJugador;
    private int danoRival;
    private int objetosJugador;
    private int objetosRival;

    public Batalla(Entrenador jugador, Entrenador rival) {
        this(jugador, rival, new Random());
    }

    public Batalla(Entrenador jugador, Entrenador rival, Random azar) {
        if (jugador == null || rival == null || azar == null
                || jugador.contar() == 0 || rival.contar() == 0) {
            throw new IllegalArgumentException(
                    "Se necesitan dos equipos y un generador aleatorio"
            );
        }

        inicialJugador = jugador.copiar(true);
        inicialRival = rival.copiar(true);
        this.azar = azar;

        reiniciar();
    }

    public synchronized void reiniciar() {
        jugador = inicialJugador.copiar(true);
        rival = inicialRival.copiar(true);

        jugador.cambiar(0);
        rival.cambiar(0);

        historial = new ListaEnlazada<>();
        resultado = Resultado.EN_CURSO;
        turnoRivalPendiente = false;

        ronda = 0;
        danoJugador = 0;
        danoRival = 0;
        objetosJugador = 0;
        objetosRival = 0;

        registrar(
                "Comienza la batalla entre "
                + jugador.getNombre() + " y " + rival.getNombre()
        );
    }

    public synchronized Entrenador getJugador() {
        return jugador.copiar(false);
    }

    public synchronized Entrenador getRival() {
        return rival.copiar(false);
    }

    public synchronized Resultado getResultado() {
        return resultado;
    }

    public synchronized boolean isTurnoRivalPendiente() {
        return turnoRivalPendiente;
    }

    public synchronized ListaEnlazada<Evento> getHistorial() {
        ListaEnlazada<Evento> copia = new ListaEnlazada<>();

        for (Evento evento : historial) {
            copia.insertar(evento);
        }

        return copia;
    }

    public synchronized Estadisticas getEstadisticas() {
        return new Estadisticas(
                ronda,
                danoJugador,
                danoRival,
                objetosJugador,
                objetosRival,
                jugador.contar() - jugador.disponibles(),
                rival.contar() - rival.disponibles()
        );
    }

    public synchronized void atacar(int indiceAtaque) {
        comprobarEnCurso();

        Ataque ataque = jugador.activoInterno().getAtaque(indiceAtaque);

        ejecutar(() -> atacarInterno(jugador, rival, ataque));
    }

    public synchronized void cambiarPokemon(int indice) {
        comprobarEnCurso();

        if (jugador.interno(indice).estaDerrotado()
                || indice == jugador.getIndiceActivo()) {
            throw new IllegalArgumentException(
                    "Selecciona otro Pokémon disponible"
            );
        }

        ejecutar(() -> {
            jugador.cambiar(indice);

            registrar(
                    jugador.getNombre() + " cambia a "
                    + jugador.activoInterno().getNombre()
            );
        });
    }

    public synchronized void usarObjeto(Objeto objeto, int indicePokemon) {
        comprobarEnCurso();

        Pokemon objetivo = jugador.interno(indicePokemon);

        if (objeto == null
                || jugador.cantidad(objeto) == 0
                || !objeto.sePuedeUsar(objetivo)) {
            throw new IllegalArgumentException(
                    "Objeto agotado o sin efecto sobre ese Pokémon"
            );
        }

        ejecutar(() -> usarInterno(jugador, objeto, objetivo));
    }

    private void comprobarEnCurso() {
        if (resultado != Resultado.EN_CURSO) {
            throw new IllegalStateException("La batalla terminó");
        }
        if (turnoRivalPendiente) {
            throw new IllegalStateException("Espera el turno del rival");
        }
    }

    private void ejecutar(Runnable accion) {
        ronda++;

        Pokemon participante = jugador.activoInterno();

        accion.run();
        terminarTurno(jugador, participante);
        resolver();

        turnoRivalPendiente = resultado == Resultado.EN_CURSO;
    }

    public synchronized void ejecutarTurnoRival() {
        if (!turnoRivalPendiente || resultado != Resultado.EN_CURSO) {
            return;
        }

        turnoRivalPendiente = false;
        Pokemon oponente = rival.activoInterno();
        turnoRival();
        terminarTurno(rival, oponente);
        resolver();
    }

    private void atacarInterno(
            Entrenador atacante,
            Entrenador defensor,
            Ataque ataque
    ) {
        Pokemon origen = atacante.activoInterno();
        Pokemon destino = defensor.activoInterno();

        if (!origen.estado().permiteAtacar(azar)) {
            registrar(
                    origen.getNombre() + " no puede atacar por "
                    + origen.getEstado()
            );
            return;
        }

        registrar(origen.getNombre() + " usa " + ataque.nombre());

        if (ataque.efecto() == Estado.Efecto.NINGUNO) {
            int efectividad = ataque.tipo().efectividadContra(
                    destino.getTipo()
            );

            if (efectividad == 4) {
                registrar("Debilidad: +10 de daño");
            }

            if (efectividad == 1) {
                registrar("Resistencia: -10 de daño, mínimo 0");
            }

            if (efectividad == 0) {
                registrar("El objetivo es inmune al daño de este tipo");
            }

            danar(
                    defensor,
                    destino,
                    ataque.calcularDano(destino.getTipo())
            );

        } else if (destino.getEstado() != Estado.Efecto.NINGUNO) {
            registrar(
                    destino.getNombre()
                    + " ya tiene un estado; no se reemplaza ni se prolonga"
            );

        } else if (ataque.efecto() == Estado.Efecto.PARALISIS
                && azar.nextInt(100) >= 80) {
            registrar("La parálisis falló");

        } else {
            destino.aplicarEstado(ataque.efecto());

            registrar(
                    destino.getNombre() + " recibe " + destino.getEstado()
            );
        }
    }

    private void terminarTurno(Entrenador entrenador, Pokemon pokemon) {
        if (pokemon.estaDerrotado()) {
            return;
        }

        Estado estado = pokemon.estado();

        if (estado.danoPorTurno() > 0) {
            registrar(
                    pokemon.getNombre() + " sufre daño por "
                    + pokemon.getEstado()
            );

            danar(entrenador, pokemon, estado.danoPorTurno());
        }

        if (estado.avanzar()) {
            pokemon.curarEstado();

            registrar(
                    pokemon.getNombre() + " se recupera de "
                    + estado.getEfecto()
            );

        } else if (estado.getEfecto() == Estado.Efecto.BOSTEZO
                && estado.getTurnos() == 2) {
            registrar(
                    pokemon.getNombre()
                    + " se duerme durante sus siguientes dos turnos"
            );
        }
    }

    private void danar(
            Entrenador receptor,
            Pokemon pokemon,
            int cantidad
    ) {
        int real = pokemon.recibirDano(cantidad);

        if (receptor == rival) {
            danoJugador += real;
        } else {
            danoRival += real;
        }

        registrar(
                pokemon.getNombre() + " recibe " + real
                + " de daño. PS: " + pokemon.getHp()
                + "/" + pokemon.getHpMaximo()
        );

        if (pokemon.estaDerrotado()) {
            registrar(pokemon.getNombre() + " fue derrotado");
        }
    }

    private void usarInterno(
            Entrenador entrenador,
            Objeto objeto,
            Pokemon pokemon
    ) {
        int recuperado = objeto.aplicar(pokemon);
        entrenador.consumir(objeto);

        if (entrenador == jugador) {
            objetosJugador++;
        } else {
            objetosRival++;
        }

        registrar(
                entrenador.getNombre() + " usa " + objeto
                + " en " + pokemon.getNombre()
                + ". PS recuperados: " + recuperado
                + ". Estado: " + pokemon.getEstado()
        );
    }

    private void turnoRival() {
        Pokemon pokemon = rival.activoInterno();

        Objeto cura = switch (pokemon.getEstado()) {
            case VENENO -> Objeto.ANTIDOTO;
            case PARALISIS -> Objeto.ANTIPARALIZADOR;
            default -> null;
        };

        if (cura != null && rival.cantidad(cura) > 0) {
            usarInterno(rival, cura, pokemon);

        } else if (pokemon.getHp() <= pokemon.getHpMaximo() / 2
                && rival.cantidad(Objeto.SUPERPOCION) > 0) {
            usarInterno(rival, Objeto.SUPERPOCION, pokemon);

        } else if (pokemon.getHp() <= pokemon.getHpMaximo() / 2
                && rival.cantidad(Objeto.POCION) > 0) {
            usarInterno(rival, Objeto.POCION, pokemon);

        } else {
            int opciones = jugador.activoInterno().getEstado()
                    == Estado.Efecto.NINGUNO ? 3 : 2;

            atacarInterno(
                    rival,
                    jugador,
                    pokemon.getAtaque(azar.nextInt(opciones))
            );
        }
    }

    private void resolver() {
        boolean sinJugador = jugador.disponibles() == 0;
        boolean sinRival = rival.disponibles() == 0;

        if (sinJugador || sinRival) {
            resultado = sinJugador && sinRival
                    ? Resultado.EMPATE
                    : sinRival ? Resultado.VICTORIA : Resultado.DERROTA;

            registrar("Resultado: " + resultado);
            return;
        }

        reemplazarDerrotado(jugador);
        reemplazarDerrotado(rival);
    }

    private void reemplazarDerrotado(Entrenador entrenador) {
        if (entrenador.activoInterno().estaDerrotado()) {
            entrenador.cambiar(entrenador.siguienteDisponible());

            registrar(
                    entrenador.getNombre() + " envía a "
                    + entrenador.activoInterno().getNombre()
            );
        }
    }

    private void registrar(String mensaje) {
        historial.insertar(new Evento(ronda, mensaje));
    }
}
