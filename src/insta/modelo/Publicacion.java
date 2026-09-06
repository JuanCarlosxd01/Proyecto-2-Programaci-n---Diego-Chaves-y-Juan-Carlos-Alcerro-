
package insta.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Publicacion implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int MAX_TEXTO = 140;
    public static final int MAX_DESCRIPCION_IMAGEN = 220;

    public enum Tipo {
        TEXTO,
        IMAGEN,
        STICKER
    }

    private final String id;
    private final String autor;
    private final LocalDateTime fechaPublicacion;

    private final Tipo tipo;
    private final String contenido;
    private final String rutaAdjunto;
    private final String carpetaPersonal;


    private Publicacion(
            String autor,
            String contenido,
            Tipo tipo,
            String rutaAdjunto,
            String carpetaPersonal
    ) {
        Objects.requireNonNull(
                autor,
                "El autor no puede ser null."
        );

        Objects.requireNonNull(
                tipo,
                "El tipo de publicación no puede ser null."
        );

        if (autor.isBlank()) {
            throw new IllegalArgumentException(
                    "La publicación debe tener un autor."
            );
        }

        String texto = contenido == null ? "" : contenido.strip();

        String adjunto = rutaAdjunto == null
                ? ""
                : rutaAdjunto.strip();

        String carpeta = carpetaPersonal == null
                ? ""
                : carpetaPersonal.strip();

        validarContenido(texto, tipo);
        validarAdjunto(adjunto, carpeta, tipo);

        this.id = UUID.randomUUID().toString();
        this.autor = autor;
        this.fechaPublicacion = LocalDateTime.now();

        this.tipo = tipo;
        this.contenido = texto;
        this.rutaAdjunto = adjunto;
        this.carpetaPersonal = carpeta;
    }

    public static Publicacion crearTexto(
            String autor,
            String contenido
    ) {
        return new Publicacion(
                autor,
                contenido,
                Tipo.TEXTO,
                "",
                ""
        );
    }

    public static Publicacion crearConImagen(
            String autor,
            String descripcion,
            String rutaImagen,
            String carpetaPersonal
    ) {
        return new Publicacion(
                autor,
                descripcion,
                Tipo.IMAGEN,
                rutaImagen,
                carpetaPersonal
        );
    }

    public static Publicacion crearConSticker(
            String autor,
            String contenido,
            String rutaSticker
    ) {
        return new Publicacion(
                autor,
                contenido,
                Tipo.STICKER,
                rutaSticker,
                ""
        );
    }

    private static void validarContenido(
            String contenido,
            Tipo tipo
    ) {
        int limite = tipo == Tipo.IMAGEN
                ? MAX_DESCRIPCION_IMAGEN
                : MAX_TEXTO;

        int cantidad = contenido.codePointCount(
                0,
                contenido.length()
        );

        if (cantidad > limite) {
            throw new IllegalArgumentException(
                    "Esta publicación permite como máximo "
                    + limite + " caracteres."
            );
        }

        if (tipo == Tipo.TEXTO && contenido.isBlank()) {
            throw new IllegalArgumentException(
                    "Una publicación de texto no puede estar vacía."
            );
        }
    }

    private static void validarAdjunto(
            String rutaAdjunto,
            String carpetaPersonal,
            Tipo tipo
    ) {
        if (tipo == Tipo.TEXTO && !rutaAdjunto.isEmpty()) {
            throw new IllegalArgumentException(
                    "Una publicación de texto no debe tener adjuntos."
            );
        }

        if (tipo != Tipo.TEXTO && rutaAdjunto.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debes indicar la ruta de la imagen o del sticker."
            );
        }

        if (tipo != Tipo.IMAGEN && !carpetaPersonal.isEmpty()) {
            throw new IllegalArgumentException(
                    "Solo las imágenes pueden asignarse "
                    + "a una carpeta personal."
            );
        }
    }

    public String getId() {
        return id;
    }

    public String getAutor() {
        return autor;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getContenido() {
        return contenido;
    }

    public String getRutaAdjunto() {
        return rutaAdjunto;
    }

    public String getCarpetaPersonal() {
        return carpetaPersonal;
    }

    public boolean esTexto() {
        return tipo == Tipo.TEXTO;
    }

    public boolean esImagen() {
        return tipo == Tipo.IMAGEN;
    }

    public boolean esSticker() {
        return tipo == Tipo.STICKER;
    }

    public boolean tieneAdjunto() {
        return !rutaAdjunto.isEmpty();
    }

    public boolean tieneCarpetaPersonal() {
        return !carpetaPersonal.isEmpty();
    }


    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Publicacion)) {
            return false;
        }

        Publicacion otra = (Publicacion) objeto;

        return id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return autor + " escribió: \"" + contenido + "\"";
    }
}