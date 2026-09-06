/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package red;
import java.io.Serializable;
import java.util.Objects;
/**
 *
 * @author diego
 */
public class Solicitud implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Operacion {
        REGISTRAR_USUARIO,
        INICIAR_SESION,
        CERRAR_SESION
    }

    private final Operacion operacion;

    private final String nombreCompleto;
    private final char genero;
    private final String username;
    private final String password;
    private final int edad;

    private final String tokenSesion;

    private Solicitud(
            Operacion operacion,
            String nombreCompleto,
            char genero,
            String username,
            String password,
            int edad,
            String tokenSesion
    ) {
        this.operacion = Objects.requireNonNull(
                operacion,
                "Debes indicar la operación."
        );

        this.nombreCompleto = nombreCompleto;
        this.genero = genero;
        this.username = username;
        this.password = password;
        this.edad = edad;
        this.tokenSesion = tokenSesion;
    }

    public static Solicitud registrarUsuario(
            String nombreCompleto,
            char genero,
            String username,
            String password,
            int edad
    ) {
        return new Solicitud(
                Operacion.REGISTRAR_USUARIO,
                nombreCompleto,
                genero,
                username,
                password,
                edad,
                ""
        );
    }

    public static Solicitud iniciarSesion(
            String username,
            String password
    ) {
        return new Solicitud(
                Operacion.INICIAR_SESION,
                "",
                '\0',
                username,
                password,
                0,
                ""
        );
    }

    public static Solicitud cerrarSesion(String tokenSesion) {
        return new Solicitud(
                Operacion.CERRAR_SESION,
                "",
                '\0',
                "",
                "",
                0,
                tokenSesion
        );
    }

    public Operacion getOperacion() {
        return operacion;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public char getGenero() {
        return genero;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getEdad() {
        return edad;
    }

    public String getTokenSesion() {
        return tokenSesion;
    }


    @Override
    public String toString() {
        return "Solicitud{operacion=" + operacion + "}";
    }
}
