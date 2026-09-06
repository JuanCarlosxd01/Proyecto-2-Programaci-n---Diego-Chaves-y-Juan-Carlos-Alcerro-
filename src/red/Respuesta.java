/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package red;
import insta.modelo.UsuarioInsta;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
/**
 *
 * @author diego
 */
public class Respuesta implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Codigo {
        OK,
        DATOS_INVALIDOS,
        USERNAME_DUPLICADO,
        CREDENCIALES_INCORRECTAS,
        CUENTA_DESACTIVADA,
        SESION_INVALIDA,
        ERROR_ALMACENAMIENTO,
        ERROR_INTERNO,
        OPERACION_NO_SOPORTADA
    }

    private final Codigo codigo;
    private final String mensaje;

    private final DatosUsuario usuario;
    private final String tokenSesion;

    private Respuesta(
            Codigo codigo,
            String mensaje,
            DatosUsuario usuario,
            String tokenSesion
    ) {
        this.codigo = Objects.requireNonNull(
                codigo,
                "Debes indicar el código de respuesta."
        );

        this.mensaje = Objects.requireNonNull(
                mensaje,
                "El mensaje de respuesta no puede ser null."
        );

        this.usuario = usuario;
        this.tokenSesion = tokenSesion == null
                ? ""
                : tokenSesion;
    }

    public static Respuesta exito(String mensaje) {
        return new Respuesta(
                Codigo.OK,
                mensaje,
                null,
                ""
        );
    }

    public static Respuesta cuentaCreada(UsuarioInsta usuario) {
        return new Respuesta(
                Codigo.OK,
                "Cuenta creada correctamente. Ya puedes iniciar sesión.",
                new DatosUsuario(usuario),
                ""
        );
    }

    public static Respuesta sesionIniciada(
            UsuarioInsta usuario,
            String tokenSesion
    ) {
        Objects.requireNonNull(
                tokenSesion,
                "El token de sesión no puede ser null."
        );

        if (tokenSesion.isBlank()) {
            throw new IllegalArgumentException(
                    "El token de sesión no puede estar vacío."
            );
        }

        return new Respuesta(
                Codigo.OK,
                "Sesión iniciada correctamente.",
                new DatosUsuario(usuario),
                tokenSesion
        );
    }

    public static Respuesta error(
            Codigo codigo,
            String mensaje
    ) {
        if (codigo == Codigo.OK) {
            throw new IllegalArgumentException(
                    "Una respuesta de error no puede usar el código OK."
            );
        }

        return new Respuesta(
                codigo,
                mensaje,
                null,
                ""
        );
    }

    public Codigo getCodigo() {
        return codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public DatosUsuario getUsuario() {
        return usuario;
    }

    public String getTokenSesion() {
        return tokenSesion;
    }

    public boolean esExitosa() {
        return codigo == Codigo.OK;
    }

    public boolean tieneUsuario() {
        return usuario != null;
    }

    @Override
    public String toString() {
        return "Respuesta{codigo=" + codigo + "}";
    }


    public static final class DatosUsuario
            implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String username;
        private final String nombreCompleto;

        private final char genero;
        private final int edad;

        private final LocalDateTime fechaRegistro;

        private final boolean activa;
        private final String rutaFotoPerfil;

        private DatosUsuario(UsuarioInsta usuario) {
            Objects.requireNonNull(
                    usuario,
                    "El usuario no puede estar vacio"
            );

            this.username = usuario.getUsername();
            this.nombreCompleto = usuario.getNombreCompleto();

            this.genero = usuario.getGenero();
            this.edad = usuario.getEdad();

            this.fechaRegistro = usuario.getFechaRegistro();

            this.activa = usuario.estaActiva();
            this.rutaFotoPerfil = usuario.getRutaFotoPerfil();
        }

        public String getUsername() {
            return username;
        }

        public String getNombreCompleto() {
            return nombreCompleto;
        }

        public char getGenero() {
            return genero;
        }

        public int getEdad() {
            return edad;
        }

        public LocalDateTime getFechaRegistro() {
            return fechaRegistro;
        }

        public boolean estaActiva() {
            return activa;
        }

        public String getRutaFotoPerfil() {
            return rutaFotoPerfil;
        }

        @Override
        public String toString() {
            return "@" + username + " - " + nombreCompleto;
        }
    }
}
