


package insta.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Mensaje implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int MAX_TEXTO = 300;

    public enum Tipo {
        TEXTO,
        STICKER
    }

    private final String id;

    private final String emisor;
    private final String receptor;

    private final LocalDateTime fechaEnvio;
    private final Tipo tipo;

    private final String contenido;
    private final String rutaSticker;

    private boolean leido;

    private Mensaje(
            String emisor,
            String receptor,
            Tipo tipo,
            String contenido,
            String rutaSticker
    ) {
        validarParticipantes(emisor, receptor);

        Objects.requireNonNull(
                tipo,
                "El tipo de mensaje no puede ser null."
        );

        String texto = contenido == null
                ? ""
                : contenido.strip();

        String sticker = rutaSticker == null
                ? ""
                : rutaSticker.strip();

        validarContenido(tipo, texto, sticker);

        this.id = UUID.randomUUID().toString();

        this.emisor = emisor;
        this.receptor = receptor;

        this.fechaEnvio = LocalDateTime.now();
        this.tipo = tipo;

        this.contenido = texto;
        this.rutaSticker = sticker;

        this.leido = false;
    }

    public static Mensaje crearTexto(
            String emisor,
            String receptor,
            String contenido
    ) {
        return new Mensaje(
                emisor,
                receptor,
                Tipo.TEXTO,
                contenido,
                ""
        );
    }

    public static Mensaje crearSticker(
            String emisor,
            String receptor,
            String rutaSticker
    ) {
        return new Mensaje(
                emisor,
                receptor,
                Tipo.STICKER,
                "",
                rutaSticker
        );
    }

    private static void validarParticipantes(
            String emisor,
            String receptor
    ) {
        Objects.requireNonNull(
                emisor,
                "El emisor no puede ser null."
        );

        Objects.requireNonNull(
                receptor,
                "El receptor no puede ser null."
        );

        if (emisor.isBlank() || receptor.isBlank()) {
            throw new IllegalArgumentException(
                    "Debes indicar el emisor y el receptor."
            );
        }
    }

    private static void validarContenido(
            Tipo tipo,
            String contenido,
            String rutaSticker
    ) {
        if (tipo == Tipo.TEXTO) {
            if (contenido.isBlank()) {
                throw new IllegalArgumentException(
                        "El mensaje de texto no puede estar vacío."
                );
            }

            int cantidad = contenido.codePointCount(
                    0,
                    contenido.length()
            );

            if (cantidad > MAX_TEXTO) {
                throw new IllegalArgumentException(
                        "El mensaje permite como máximo "
                        + MAX_TEXTO + " caracteres."
                );
            }

            if (!rutaSticker.isEmpty()) {
                throw new IllegalArgumentException(
                        "Un mensaje de texto no debe "
                        + "contener una ruta de sticker."
                );
            }
        } else {
            if (rutaSticker.isEmpty()) {
                throw new IllegalArgumentException(
                        "Debes indicar la ruta del sticker."
                );
            }

            if (!contenido.isEmpty()) {
                throw new IllegalArgumentException(
                        "El sticker debe enviarse como "
                        + "un mensaje independiente."
                );
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getEmisor() {
        return emisor;
    }

    public String getReceptor() {
        return receptor;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getContenido() {
        return contenido;
    }

    public String getRutaSticker() {
        return rutaSticker;
    }

    public boolean esTexto() {
        return tipo == Tipo.TEXTO;
    }

    public boolean esSticker() {
        return tipo == Tipo.STICKER;
    }

    public boolean estaLeido() {
        return leido;
    }

    public void marcarComoLeido() {
        leido = true;
    }

    public boolean enviadoPor(String username) {
        return emisor.equals(username);
    }

    public boolean recibidoPor(String username) {
        return receptor.equals(username);
    }

    public boolean perteneceAConversacion(
            String primerUsuario,
            String segundoUsuario
    ) {
        boolean direccionOriginal =
                emisor.equals(primerUsuario)
                && receptor.equals(segundoUsuario);

        boolean direccionInversa =
                emisor.equals(segundoUsuario)
                && receptor.equals(primerUsuario);

        return direccionOriginal || direccionInversa;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Mensaje)) {
            return false;
        }

        Mensaje otro = (Mensaje) objeto;

        return id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        String resumen = esTexto()
                ? contenido
                : "[Sticker]";

        return "@" + emisor
                + " → @" + receptor
                + ": " + resumen;
    }
}
