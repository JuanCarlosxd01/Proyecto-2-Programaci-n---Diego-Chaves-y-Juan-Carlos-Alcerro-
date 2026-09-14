/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pokemon.memoria;

/**
 *
 * @author diego
 */
public enum Tipo {
    NORMAL, FUEGO, AGUA, ELECTRICO, PLANTA, HIELO, LUCHA, VENENO,
    TIERRA, VOLADOR, PSIQUICO, BICHO, ROCA, FANTASMA, DRAGON,
    SINIESTRO, ACERO;

    public int efectividadContra(Tipo defensor) {
        return switch (this) {
            case NORMAL -> switch (defensor) {
                case FANTASMA -> 0;
                case ROCA, ACERO -> 1;
                default -> 2;
            };

            case FUEGO -> switch (defensor) {
                case PLANTA, HIELO, BICHO, ACERO -> 4;
                case FUEGO, AGUA, ROCA, DRAGON -> 1;
                default -> 2;
            };

            case AGUA -> switch (defensor) {
                case FUEGO, TIERRA, ROCA -> 4;
                case AGUA, PLANTA, DRAGON -> 1;
                default -> 2;
            };

            case ELECTRICO -> switch (defensor) {
                case TIERRA -> 0;
                case AGUA, VOLADOR -> 4;
                case ELECTRICO, PLANTA, DRAGON -> 1;
                default -> 2;
            };

            case PLANTA -> switch (defensor) {
                case AGUA, TIERRA, ROCA -> 4;
                case FUEGO, PLANTA, VENENO, VOLADOR, BICHO,
                     DRAGON, ACERO -> 1;
                default -> 2;
            };

            case HIELO -> switch (defensor) {
                case PLANTA, TIERRA, VOLADOR, DRAGON -> 4;
                case FUEGO, AGUA, HIELO, ACERO -> 1;
                default -> 2;
            };

            case LUCHA -> switch (defensor) {
                case FANTASMA -> 0;
                case NORMAL, HIELO, ROCA, SINIESTRO, ACERO -> 4;
                case VENENO, VOLADOR, PSIQUICO, BICHO -> 1;
                default -> 2;
            };

            case VENENO -> switch (defensor) {
                case ACERO -> 0;
                case PLANTA -> 4;
                case VENENO, TIERRA, ROCA, FANTASMA -> 1;
                default -> 2;
            };

            case TIERRA -> switch (defensor) {
                case VOLADOR -> 0;
                case FUEGO, ELECTRICO, VENENO, ROCA, ACERO -> 4;
                case PLANTA, BICHO -> 1;
                default -> 2;
            };

            case VOLADOR -> switch (defensor) {
                case PLANTA, LUCHA, BICHO -> 4;
                case ELECTRICO, ROCA, ACERO -> 1;
                default -> 2;
            };

            case PSIQUICO -> switch (defensor) {
                case SINIESTRO -> 0;
                case LUCHA, VENENO -> 4;
                case PSIQUICO, ACERO -> 1;
                default -> 2;
            };

            case BICHO -> switch (defensor) {
                case PLANTA, PSIQUICO, SINIESTRO -> 4;
                case FUEGO, LUCHA, VENENO, VOLADOR,
                     FANTASMA, ACERO -> 1;
                default -> 2;
            };

            case ROCA -> switch (defensor) {
                case FUEGO, HIELO, VOLADOR, BICHO -> 4;
                case LUCHA, TIERRA, ACERO -> 1;
                default -> 2;
            };

            case FANTASMA -> switch (defensor) {
                case NORMAL -> 0;
                case PSIQUICO, FANTASMA -> 4;
                case SINIESTRO, ACERO -> 1;
                default -> 2;
            };

            case DRAGON -> switch (defensor) {
                case DRAGON -> 4;
                case ACERO -> 1;
                default -> 2;
            };

            case SINIESTRO -> switch (defensor) {
                case PSIQUICO, FANTASMA -> 4;
                case LUCHA, SINIESTRO, ACERO -> 1;
                default -> 2;
            };

            case ACERO -> switch (defensor) {
                case HIELO, ROCA -> 4;
                case FUEGO, AGUA, ELECTRICO, ACERO -> 1;
                default -> 2;
            };
        };
    }
}
