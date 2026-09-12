/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package red;
import estructuras.ListaEnlazada;
import insta.modelo.*;
import java.io.Serializable;
import java.time.LocalDateTime;
/**
 *
 * @author diego
 */
public class Respuesta implements Serializable {

    private static final long serialVersionUID = 2L;

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

    private DatosUsuario usuario;
    private String tokenSesion = "";
    private ListaEnlazada<?> elementos = new ListaEnlazada<>();
    private byte[] archivo;
    private int total;

    private Respuesta(Codigo codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

    public static Respuesta exito(String mensaje) {
        return new Respuesta(Codigo.OK, mensaje);
    }

    public static Respuesta error(Codigo codigo, String mensaje) {
        return new Respuesta(codigo, mensaje);
    }

    public static Respuesta cuentaCreada(UsuarioInsta usuario) {
        return exito("Cuenta creada.").conUsuario(
                new DatosUsuario(usuario, 0, 0, 0, false)
        );
    }

    public static Respuesta sesionIniciada(
            UsuarioInsta usuario,
            String token
    ) {
        Respuesta respuesta = exito("Sesión iniciada.")
                .conUsuario(
                        new DatosUsuario(usuario, 0, 0, 0, false)
                );

        respuesta.tokenSesion = token;
        return respuesta;
    }

    public Respuesta conUsuario(DatosUsuario usuario) {
        this.usuario = usuario;
        return this;
    }

    public Respuesta conLista(ListaEnlazada<?> elementos) {
        this.elementos = elementos;
        total = elementos.size();
        return this;
    }

    public Respuesta conTotal(int total) {
        this.total = total;
        return this;
    }

    public Respuesta conArchivo(byte[] archivo) {
        this.archivo = archivo;
        return this;
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

    public int getTotal() {
        return total;
    }

    public boolean esExitosa() {
        return codigo == Codigo.OK;
    }

    public boolean tieneUsuario() {
        return usuario != null;
    }

    public byte[] getArchivo() {
        return archivo == null ? null : archivo.clone();
    }

    public <T> ListaEnlazada<T> getElementos(Class<T> tipo) {
        ListaEnlazada<T> resultado = new ListaEnlazada<>();

        for (Object elemento : elementos) {
            resultado.agregar(tipo.cast(elemento));
        }

        return resultado;
    }

    public ListaEnlazada<Publicacion> getPublicaciones() {
        return getElementos(Publicacion.class);
    }

    public ListaEnlazada<Mensaje> getMensajes() {
        return getElementos(Mensaje.class);
    }

    public ListaEnlazada<Sticker> getStickers() {
        return getElementos(Sticker.class);
    }

    public ListaEnlazada<DatosUsuario> getUsuarios() {
        return getElementos(DatosUsuario.class);
    }

    public ListaEnlazada<String> getCarpetas() {
        return getElementos(String.class);
    }

    public static final class DatosUsuario implements Serializable {

        private static final long serialVersionUID = 2L;

        private final String username;
        private final String nombreCompleto;
        private final String rutaFotoPerfil;

        private final char genero;
        private final int edad;
        private final int followers;
        private final int following;
        private final int publicaciones;

        private final boolean activa;
        private final boolean loSigo;
        private final LocalDateTime fechaRegistro;

        public DatosUsuario(
                UsuarioInsta usuario,
                int followers,
                int following,
                int publicaciones,
                boolean loSigo
        ) {
            username = usuario.getUsername();
            nombreCompleto = usuario.getNombreCompleto();
            genero = usuario.getGenero();
            edad = usuario.getEdad();
            rutaFotoPerfil = usuario.getRutaFotoPerfil();
            activa = usuario.estaActiva();
            fechaRegistro = usuario.getFechaRegistro();

            this.followers = followers;
            this.following = following;
            this.publicaciones = publicaciones;
            this.loSigo = loSigo;
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

        public int getFollowers() {
            return followers;
        }

        public int getFollowing() {
            return following;
        }

        public int getPublicaciones() {
            return publicaciones;
        }

        public boolean loSigo() {
            return loSigo;
        }
    }
}