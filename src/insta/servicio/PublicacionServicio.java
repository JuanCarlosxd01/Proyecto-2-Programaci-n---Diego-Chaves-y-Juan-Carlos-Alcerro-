
package insta.servicio;


import estructuras.ListaEnlazada;
import insta.modelo.*;
import persistencia.RepositorioInsta;
import red.*;
import java.util.*;


public class PublicacionServicio {

    private final InstaServicio a;

    public PublicacionServicio(InstaServicio a) {
        this.a = a;
    }

    ListaEnlazada<Publicacion> todas() throws Exception {
        ListaEnlazada<Publicacion> resultado = new ListaEnlazada<>();

        for (UsuarioInsta usuario : a.usuarios()) {
            if (usuario.estaActiva()) {
                for (Publicacion publicacion :
                        a.publicaciones.cargar(usuario.getUsername())) {
                    resultado.agregarUnico(publicacion);
                }
            }
        }

        return resultado;
    }

    static ListaEnlazada<Publicacion> ordenar(
            ListaEnlazada<Publicacion> lista
    ) {
        ListaEnlazada<Publicacion> resultado = new ListaEnlazada<>();

        var orden = Comparator.comparing(
                Publicacion::getFechaPublicacion
        ).reversed().thenComparing(Publicacion::getId);

        for (Publicacion publicacion : lista) {
            resultado.insertarOrdenado(publicacion, orden);
        }

        return resultado;
    }

    Respuesta ejecutar(String actor, Solicitud solicitud)
            throws Exception {

        if (solicitud.getOperacion()
                == Solicitud.Operacion.PUBLICACIONES) {

            String objetivo =
                    InstaServicio.normalizar(solicitud.arg(0));

            a.exigirCuentaActiva(objetivo);

            return a.pagina(
                    ordenar(a.publicaciones.cargar(objetivo)),
                    solicitud.arg(1)
            );
        }

        if (solicitud.getOperacion()
                == Solicitud.Operacion.TIMELINE) {

            ListaEnlazada<Publicacion> resultado =
                    new ListaEnlazada<>();

            for (Publicacion publicacion : todas()) {
                if (publicacion.getAutor().equals(actor)
                        || a.seguimientoServicio.sigue(
                                actor,
                                publicacion.getAutor()
                        )) {
                    resultado.agregarUnico(publicacion);
                }
            }

            return a.pagina(
                    ordenar(resultado),
                    solicitud.arg(0)
            );
        }

        Publicacion publicacion;
        RepositorioInsta.Cambio imagen = null;

        if (solicitud.getOperacion()
                == Solicitud.Operacion.PUBLICAR_TEXTO) {

            publicacion = Publicacion.crearTexto(
                    actor,
                    solicitud.arg(0)
            );

        } else if (solicitud.getOperacion()
                == Solicitud.Operacion.PUBLICAR_STICKER) {

            Sticker sticker = a.stickerServicio.buscar(
                    actor,
                    solicitud.arg(1)
            );

            publicacion = Publicacion.crearConSticker(
                    actor,
                    solicitud.arg(0),
                    sticker.getRutaImagen()
            );

        } else {
            String carpeta = solicitud.arg(1).strip();

            if (!carpeta.isEmpty()) {
                StickerServicio.validarCarpeta(carpeta);

                if (!a.repo.existe(
                        actor + "/folders_personales/" + carpeta
                )) {
                    throw new IllegalArgumentException(
                            "La carpeta personal no existe."
                    );
                }
            }

            String ruta = actor
                    + "/imagenes/"
                    + UUID.randomUUID()
                    + ".png";

            publicacion = Publicacion.crearConImagen(
                    actor,
                    solicitud.arg(0),
                    ruta,
                    carpeta
            );

            imagen = new RepositorioInsta.Cambio(
                    ruta,
                    ImagenesInsta.validar(solicitud.getArchivo())
            );
        }

        ListaEnlazada<Publicacion> lista =
                a.publicaciones.cargar(actor);

        lista.agregar(publicacion);

        if (imagen == null) {
            a.publicaciones.guardar(actor, lista);
        } else {
            a.repo.guardar(
                    a.publicaciones.cambio(actor, lista),
                    imagen
            );
        }

        ListaEnlazada<Publicacion> creada = new ListaEnlazada<>();
        creada.agregar(publicacion);

        return Respuesta.exito("Publicación guardada.")
                .conLista(creada);
    }
}