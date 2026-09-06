
package insta.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class Sticker implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String nombre;
    private final String rutaImagen;

    private final String propietario;
    private final boolean global;

    private final LocalDateTime fechaRegistro;

    private Sticker(
            String nombre,
            String rutaImagen,
            String propietario,
            boolean global
    ) {
        Objects.requireNonNull(
                nombre,
                "El nombre del sticker no puede ser null."
        );

        Objects.requireNonNull(
                rutaImagen,
                "La ruta de la imagen no puede ser null."
        );

        String nombreValidado = nombre.strip();
        String rutaValidada = rutaImagen.strip();

        if (nombreValidado.isEmpty()) {
            throw new IllegalArgumentException(
                    "El sticker debe tener un nombre."
            );
        }

        validarExtension(rutaValidada);

        if (!global) {
            Objects.requireNonNull(
                    propietario,
                    "Un sticker personal debe tener propietario."
            );

            if (propietario.isBlank()) {
                throw new IllegalArgumentException(
                        "Debes indicar el username del propietario."
                );
            }
        }


        if (global) {
            this.id = "global:"
                    + nombreValidado.toLowerCase(Locale.ROOT);
        } else {
            this.id = UUID.randomUUID().toString();
        }

        this.nombre = nombreValidado;
        this.rutaImagen = rutaValidada;

        this.propietario = global ? "" : propietario;
        this.global = global;

        this.fechaRegistro = LocalDateTime.now();
    }

    public static Sticker crearGlobal(
            String nombre,
            String rutaImagen
    ) {
        return new Sticker(
                nombre,
                rutaImagen,
                "",
                true
        );
    }

    public static Sticker crearPersonal(
            String nombre,
            String rutaImagen,
            String propietario
    ) {
        return new Sticker(
                nombre,
                rutaImagen,
                propietario,
                false
        );
    }

    private static void validarExtension(String rutaImagen) {
        if (rutaImagen.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debes indicar la ruta de la imagen."
            );
        }

        String rutaMinusculas =
                rutaImagen.toLowerCase(Locale.ROOT);

        boolean extensionValida =
                rutaMinusculas.endsWith(".png")
                || rutaMinusculas.endsWith(".jpg");

        if (!extensionValida) {
            throw new IllegalArgumentException(
                    "Los stickers deben ser imágenes PNG o JPG."
            );
        }
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public String getPropietario() {
        return propietario;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public boolean esGlobal() {
        return global;
    }

    public boolean esPersonal() {
        return !global;
    }

    public boolean perteneceA(String username) {
        return !global && propietario.equals(username);
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Sticker)) {
            return false;
        }

        Sticker otro = (Sticker) objeto;

        return id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return nombre;
    }
}