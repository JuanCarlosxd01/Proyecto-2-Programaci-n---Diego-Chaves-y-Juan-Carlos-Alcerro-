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

    private static final long serialVersionUID = 2L;

    public enum Operacion {
        REGISTRAR_USUARIO,
        INICIAR_SESION,
        CERRAR_SESION,
        PUBLICAR_TEXTO,
        PUBLICAR_IMAGEN,
        PUBLICAR_STICKER,
        PUBLICACIONES,
        TIMELINE,
        LIKE_PUBLICACION,
        COMENTAR_PUBLICACION,
        COMENTAR_STICKER,
        ELIMINAR_COMENTARIO,
        EDITAR_PUBLICACION,
        ELIMINAR_PUBLICACION,
        SEGUIR,
        DEJAR_SEGUIR,
        SEGUIDORES,
        SEGUIDOS,
        PERFIL,
        EDITAR_PERFIL,
        FOTO_PERFIL,
        DESACTIVAR,
        REACTIVAR,
        BUSCAR_PERSONAS,
        BUSCAR_HASHTAG,
        MENCIONES,
        ENVIAR_MENSAJE,
        ENVIAR_STICKER,
        CONVERSACION,
        LEER_CONVERSACION,
        ELIMINAR_CONVERSACION,
        NO_LEIDOS,
        STICKERS,
        IMPORTAR_STICKER,
        CREAR_CARPETA,
        CARPETAS,
        ARCHIVO
    }

    private final Operacion operacion;
    private final String tokenSesion;
    private final String[] argumentos;
    private final byte[] archivo;

    public Solicitud(
            Operacion operacion,
            String tokenSesion,
            byte[] archivo,
            String... argumentos
    ) {
        this.operacion = operacion;
        this.tokenSesion = tokenSesion;
        this.argumentos = argumentos.clone();
        this.archivo = archivo == null ? null : archivo.clone();
    }

    public static Solicitud registrarUsuario(
            String nombre,
            char genero,
            String username,
            String password,
            int edad
    ) {
        return new Solicitud(
                Operacion.REGISTRAR_USUARIO,
                "",
                null,
                nombre,
                String.valueOf(genero),
                username,
                password,
                String.valueOf(edad)
        );
    }

    public static Solicitud iniciarSesion(
            String username,
            String password
    ) {
        return new Solicitud(
                Operacion.INICIAR_SESION,
                "",
                null,
                username,
                password
        );
    }

    public static Solicitud cerrarSesion(String token) {
        return new Solicitud(
                Operacion.CERRAR_SESION,
                token,
                null
        );
    }

    public Operacion getOperacion() {
        return operacion;
    }

    public String getTokenSesion() {
        return tokenSesion;
    }

    public byte[] getArchivo() {
        return archivo;
    }

    public String arg(int indice) {
        if (argumentos == null
                || indice >= argumentos.length
                || argumentos[indice] == null) {
            throw new IllegalArgumentException("Faltan datos.");
        }

        return argumentos[indice];
    }
}