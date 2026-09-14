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
public abstract class Estado {
    public enum Efecto {
        NINGUNO, VENENO, QUEMADURA, SUENO, BOSTEZO, PARALISIS
    }

    private final Efecto efecto;
    protected int turnos;

    private Estado(Efecto efecto, int turnos) {
        this.efecto = efecto;
        this.turnos = turnos;
    }

    public final Efecto getEfecto() {
        return efecto;
    }

    public final int getTurnos() {
        return turnos;
    }

    public abstract boolean permiteAtacar(Random azar);

    public abstract int danoPorTurno();

    final boolean avanzar() {
        if (turnos > 0) {
            turnos--;
        }

        return turnos == 0;
    }

    final Estado copiar() {
        Estado copia = crear(efecto);
        copia.turnos = turnos;
        return copia;
    }

    public static Estado crear(Efecto efecto) {
        if (efecto == null) {
            throw new IllegalArgumentException("Estado nulo");
        }

        return switch (efecto) {
            case NINGUNO -> new Sano();
            case VENENO -> new Envenenado();
            case QUEMADURA -> new Quemado();
            case SUENO -> new Dormido();
            case BOSTEZO -> new Somnoliento();
            case PARALISIS -> new Paralizado();
        };
    }

    private static final class Sano extends Estado {
        private Sano() {
            super(Efecto.NINGUNO, -1);
        }

        @Override
        public boolean permiteAtacar(Random azar) {
            return true;
        }

        @Override
        public int danoPorTurno() {
            return 0;
        }
    }

    private static final class Envenenado extends Estado {
        private Envenenado() {
            super(Efecto.VENENO, -1);
        }

        @Override
        public boolean permiteAtacar(Random azar) {
            return true;
        }

        @Override
        public int danoPorTurno() {
            return 10;
        }
    }

    private static final class Quemado extends Estado {
        private Quemado() {
            super(Efecto.QUEMADURA, 2);
        }

        @Override
        public boolean permiteAtacar(Random azar) {
            return true;
        }

        @Override
        public int danoPorTurno() {
            return 20;
        }
    }

    private static final class Dormido extends Estado {
        private Dormido() {
            super(Efecto.SUENO, 2);
        }

        @Override
        public boolean permiteAtacar(Random azar) {
            return false;
        }

        @Override
        public int danoPorTurno() {
            return 0;
        }
    }

    private static final class Somnoliento extends Estado {
        private Somnoliento() {
            super(Efecto.BOSTEZO, 3);
        }

        @Override
        public boolean permiteAtacar(Random azar) {
            return turnos == 3;
        }

        @Override
        public int danoPorTurno() {
            return 0;
        }
    }

    private static final class Paralizado extends Estado {
        private Paralizado() {
            super(Efecto.PARALISIS, -1);
        }

        @Override
        public boolean permiteAtacar(Random azar) {
            return azar.nextInt(100) >= 25;
        }

        @Override
        public int danoPorTurno() {
            return 0;
        }
    }
}
