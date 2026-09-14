package insta.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Comentario implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Tipo { TEXTO, STICKER }

    private final String id;
    private final String autor;
    private final String texto;
    private final LocalDateTime fecha;
    private Tipo tipo;
    private String rutaSticker;

    public Comentario(String autor, String texto) {
        this(autor, Tipo.TEXTO, texto, "");
    }

    private Comentario(String autor, Tipo tipo, String texto, String rutaSticker) {
        if (autor == null || autor.isBlank()) throw new IllegalArgumentException("El comentario debe tener autor.");
        this.id = UUID.randomUUID().toString();
        this.autor = autor;
        this.tipo = tipo;
        this.fecha = LocalDateTime.now();
        if (tipo == Tipo.STICKER) {
            String ruta = rutaSticker == null ? "" : rutaSticker.strip();
            if (ruta.isBlank()) throw new IllegalArgumentException("El sticker no puede estar vacío.");
            this.texto = "";
            this.rutaSticker = ruta;
        } else {
            String limpio = texto == null ? "" : texto.strip();
            if (limpio.isBlank()) throw new IllegalArgumentException("El comentario no puede estar vacío.");
            if (limpio.codePointCount(0, limpio.length()) > 220) throw new IllegalArgumentException("El comentario permite máximo 220 caracteres.");
            this.texto = limpio;
            this.rutaSticker = "";
        }
    }

    public static Comentario crearSticker(String autor, String rutaSticker) {
        return new Comentario(autor, Tipo.STICKER, "", rutaSticker);
    }

    public String getId() { return id; }
    public String getAutor() { return autor; }
    public String getTexto() { return texto == null ? "" : texto; }
    public LocalDateTime getFecha() { return fecha; }
    public Tipo getTipo() { return tipo == null ? Tipo.TEXTO : tipo; }
    public String getRutaSticker() { return rutaSticker == null ? "" : rutaSticker; }
    public boolean esSticker() { return getTipo() == Tipo.STICKER || !getRutaSticker().isBlank(); }
    public boolean esTexto() { return !esSticker(); }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof Comentario otro)) return false;
        return id.equals(otro.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
