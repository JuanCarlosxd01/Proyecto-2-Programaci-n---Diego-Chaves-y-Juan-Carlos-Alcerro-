
package insta.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class UsuarioInsta implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String username;
    private final LocalDateTime fechaRegistro;

    private String nombreCompleto;
    private char genero;
    private int edad;

    private String passwordHash;
    private String passwordSalt;

    private boolean activa;
    private String rutaFotoPerfil;
    private String biografia;

    public UsuarioInsta( String nombreCompleto,char genero,String username,String passwordHash,String passwordSalt,int edad,String rutaFotoPerfil ) {
        this.username = Objects.requireNonNull(
                username,
                "El username no puede estar vacio"
        );

        this.nombreCompleto = Objects.requireNonNull(
                nombreCompleto,
                "El nombre completo no puede estar vacio"
        );

        this.genero = genero;
        this.edad = edad;

        actualizarCredenciales(passwordHash, passwordSalt);

        this.fechaRegistro = LocalDateTime.now();
        this.activa = true;
        this.biografia = "";

        setRutaFotoPerfil(rutaFotoPerfil);
    }

    public String getUsername() {
        return username;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = Objects.requireNonNull(
                nombreCompleto,
                "El nombre completo no puede estar vacio"
        );
    }

    public char getGenero() {
        return genero;
    }

    public void setGenero(char genero) {
        this.genero = genero;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void actualizarCredenciales(
            String passwordHash,
            String passwordSalt
    ) {
        Objects.requireNonNull(
                passwordHash,
                "El hash de la contraseña no puede estar vacia"
        );

        Objects.requireNonNull(
                passwordSalt,
                "La sal de la contraseña no puede ser nula"
        );

        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
    }

    public boolean estaActiva() {
        return activa;
    }

    public void activar() {
        activa = true;
    }

    public void desactivar() {
        activa = false;
    }

    public String getRutaFotoPerfil() {
        return rutaFotoPerfil;
    }

    public void setRutaFotoPerfil(String rutaFotoPerfil) {
        this.rutaFotoPerfil = rutaFotoPerfil == null
                ? ""
                : rutaFotoPerfil;
    }

    public String getBiografia() {
        return biografia == null ? "" : biografia;
    }

    public void setBiografia(String biografia) {
        String texto = biografia == null ? "" : biografia.strip();
        if (texto.codePointCount(0, texto.length()) > 160) {
            throw new IllegalArgumentException("La biografía permite máximo 160 caracteres.");
        }
        this.biografia = texto;
    }


    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof UsuarioInsta)) {
            return false;
        }

        UsuarioInsta otro = (UsuarioInsta) objeto;

        return username.equals(otro.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public String toString() {
        return "@" + username + " - " + nombreCompleto;
    }
}