/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pokemon.memoria;

/**
 *
 * @author diego
 */
public record Ataque(
        String nombre,
        Tipo tipo,
        int dano,
        Estado.Efecto efecto
) {
    public Ataque {
        if (nombre == null || nombre.isBlank()
                || tipo == null || efecto == null
                || dano < 0
                || (efecto != Estado.Efecto.NINGUNO && dano != 0)) {
            throw new IllegalArgumentException("Ataque inválido");
        }
    }

    public int calcularDano(Tipo defensor) {
        if (efecto != Estado.Efecto.NINGUNO || dano == 0) {
            return 0;
        }

        return switch (tipo.efectividadContra(defensor)) {
            case 0 -> 0;
            case 1 -> Math.max(0, dano - 10);
            case 4 -> dano + 10;
            default -> dano;
        };
    }
}
