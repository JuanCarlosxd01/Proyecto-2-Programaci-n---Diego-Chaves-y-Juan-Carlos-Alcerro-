
package insta.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class Seguimiento implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String username;
    private final LocalDateTime fechaRegistro;

    private boolean activo;

    public Seguimiento(String username) {
        Objects.requireNonNull(
                username,
                "El username no puede estar vacio"
        );

        if (username.isBlank()) {
            throw new IllegalArgumentException(
                    "Debes indicar el username de la cuenta."
            );
        }

        this.username = username;
        this.fechaRegistro = LocalDateTime.now();
        this.activo = true;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public boolean estaActivo() {
        return activo;
    }

    public void activar() {
        activo = true;
    }

    public void desactivar() {
        activo = false;
    }

 
    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Seguimiento)) {
            return false;
        }

        Seguimiento otro = (Seguimiento) objeto;

        return username.equals(otro.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public String toString() {
        return "@" + username
                + (activo ? " - Activo" : " - Inactivo");
    }
}