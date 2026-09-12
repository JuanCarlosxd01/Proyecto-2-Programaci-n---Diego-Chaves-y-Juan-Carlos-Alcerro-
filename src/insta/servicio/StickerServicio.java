/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package insta.servicio;
import estructuras.ListaEnlazada;
import insta.modelo.*;
import persistencia.RepositorioInsta;
import red.*;
import java.nio.file.*;
import java.util.*;
/**
 *
 * @author diego
 */

public class StickerServicio {

    private final InstaServicio a;

    public StickerServicio(InstaServicio a) {
        this.a = a;
    }

    void inicializar() throws Exception {
        if (!a.repo.existe("globales.ins")) {
            String[] nombres = {
                "Feliz",
                "Triste",
                "Corazón",
                "Risa",
                "Aplauso"
            };

            int[] colores = {
                0xe4a62a,
                0x527daa,
                0xc64c68,
                0x689657,
                0x8d63a7
            };

            ListaEnlazada<Sticker> catalogo =
                    new ListaEnlazada<>();

            RepositorioInsta.Cambio[] cambios =
                    new RepositorioInsta.Cambio[6];

            for (int i = 0; i < 5; i++) {
                String ruta = "stickers_globales/" + i + ".png";

                catalogo.agregar(
                        Sticker.crearGlobal(nombres[i], ruta)
                );

                cambios[i] = new RepositorioInsta.Cambio(
                        ruta,
                        ImagenesInsta.dibujar(
                                nombres[i],
                                colores[i]
                        )
                );
            }

            cambios[5] = new RepositorioInsta.Cambio(
                    "globales.ins",
                    catalogo
            );

            a.repo.guardar(cambios);
        }

        ListaEnlazada<Sticker> globales =
                a.repo.leer("globales.ins", Sticker.class);

        for (UsuarioInsta usuario : a.usuarios()) {
            ListaEnlazada<Sticker> lista =
                    a.stickers.cargar(usuario.getUsername());

            boolean cambio = false;

            for (Sticker sticker : globales) {
                if (lista.agregarUnico(sticker)) {
                    cambio = true;
                }
            }

            if (cambio) {
                a.stickers.guardar(
                        usuario.getUsername(),
                        lista
                );
            }
        }
    }

    Sticker buscar(String actor, String id) throws Exception {
        Sticker sticker = a.stickers.cargar(actor).buscar(
                elemento -> elemento.getId().equals(id)
        );

        if (sticker == null
                || (!sticker.esGlobal()
                && !sticker.perteneceA(actor))) {
            throw new IllegalArgumentException(
                    "Sticker no disponible para esta cuenta."
            );
        }

        return sticker;
    }

    static void validarCarpeta(String nombre) {
        if (!nombre.matches("[\\p{L}\\p{N} _-]{1,40}")
                || nombre.isBlank()
                || nombre.endsWith(" ")
                || nombre.toLowerCase(Locale.ROOT).matches(
                        "con|prn|aux|nul|com[1-9]|lpt[1-9]"
                )) {
            throw new IllegalArgumentException(
                    "Nombre de carpeta inválido."
            );
        }
    }

    private boolean permitido(
            String actor,
            String referencia
    ) throws Exception {

        for (Sticker sticker : a.stickers.cargar(actor)) {
            if (sticker.getRutaImagen().equals(referencia)) {
                return true;
            }
        }

        for (UsuarioInsta usuario : a.usuarios()) {
            if (usuario.estaActiva()) {
                if (usuario.getRutaFotoPerfil().equals(referencia)) {
                    return true;
                }

                for (Publicacion publicacion :
                        a.publicaciones.cargar(usuario.getUsername())) {

                    if (publicacion.tieneAdjunto()
                            && publicacion.getRutaAdjunto()
                                    .equals(referencia)) {
                        return true;
                    }
                }
            }
        }

        for (Mensaje mensaje : a.inbox.cargar(actor)) {
            if (mensaje.esSticker()
                    && mensaje.getRutaSticker().equals(referencia)
                    && (mensaje.enviadoPor(actor)
                    || mensaje.recibidoPor(actor))
                    && a.visible(mensaje.getEmisor())
                    && a.visible(mensaje.getReceptor())) {
                return true;
            }
        }

        return false;
    }

    Respuesta ejecutar(String actor, Solicitud solicitud)
            throws Exception {

        switch (solicitud.getOperacion()) {
            case STICKERS:
                return a.pagina(
                        a.stickers.cargar(actor),
                        solicitud.arg(0)
                );

            case IMPORTAR_STICKER: {
                String nombre = solicitud.arg(0).strip();

                String archivo = solicitud.arg(1)
                        .toLowerCase(Locale.ROOT);

                if (nombre.isEmpty()
                        || nombre.length() > 40
                        || !(archivo.endsWith(".png")
                        || archivo.endsWith(".jpg"))) {
                    throw new IllegalArgumentException(
                            "Indica un nombre y un archivo PNG o JPG."
                    );
                }

                String ruta = actor
                        + "/stickers_personales/"
                        + UUID.randomUUID()
                        + ".png";

                Sticker sticker = Sticker.crearPersonal(
                        nombre,
                        ruta,
                        actor
                );

                ListaEnlazada<Sticker> lista =
                        a.stickers.cargar(actor);

                lista.agregar(sticker);

                a.repo.guardar(
                        a.stickers.cambio(actor, lista),
                        new RepositorioInsta.Cambio(
                                ruta,
                                ImagenesInsta.validar(
                                        solicitud.getArchivo()
                                )
                        )
                );

                ListaEnlazada<Sticker> creado =
                        new ListaEnlazada<>();

                creado.agregar(sticker);

                return Respuesta.exito("Sticker importado.")
                        .conLista(creado);
            }

            case CREAR_CARPETA: {
                String nombre = solicitud.arg(0).strip();

                validarCarpeta(nombre);

                Files.createDirectory(a.repo.ruta(
                        actor + "/folders_personales/" + nombre
                ));

                return Respuesta.exito("Carpeta creada.");
            }

            case CARPETAS: {
                ListaEnlazada<String> lista = new ListaEnlazada<>();

                try (DirectoryStream<Path> carpetas =
                             Files.newDirectoryStream(a.repo.ruta(
                                     actor + "/folders_personales"
                             ))) {

                    for (Path path : carpetas) {
                        a.repo.ruta(
                                actor + "/folders_personales/"
                                        + path.getFileName()
                        );

                        if (Files.isDirectory(path)) {
                            lista.insertarOrdenado(
                                    path.getFileName().toString(),
                                    String.CASE_INSENSITIVE_ORDER
                            );
                        }
                    }
                }

                return a.pagina(lista, solicitud.arg(0));
            }

            case ARCHIVO: {
                String referencia = solicitud.arg(0);

                if (referencia.isBlank()
                        || !permitido(actor, referencia)) {
                    throw new IllegalArgumentException(
                            "No tienes acceso a ese recurso."
                    );
                }

                Path ruta = a.repo.ruta(referencia);

                if (Files.size(ruta) > ImagenesInsta.MAX_BYTES) {
                    throw new IllegalArgumentException(
                            "Recurso demasiado grande."
                    );
                }

                return Respuesta.exito("Imagen PNG.")
                        .conArchivo(Files.readAllBytes(ruta));
            }

            default:
                throw new IllegalArgumentException(
                        "Operación inválida."
                );
        }
    }
}
