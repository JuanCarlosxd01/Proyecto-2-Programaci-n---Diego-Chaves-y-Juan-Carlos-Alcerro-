package insta.servicio;

import estructuras.ListaEnlazada;
import insta.modelo.Comentario;
import insta.modelo.Publicacion;
import insta.modelo.Sticker;
import insta.modelo.UsuarioInsta;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import persistencia.RepositorioInsta;
import red.Respuesta;
import red.Solicitud;

public class PublicacionServicio {

    private final InstaServicio a;

    public PublicacionServicio(InstaServicio a) {
        this.a = a;
    }

    ListaEnlazada<Publicacion> todas() throws Exception {
        ListaEnlazada<Publicacion> resultado = new ListaEnlazada<>();

        for (UsuarioInsta usuario : a.usuarios()) {
            if (usuario.estaActiva()) {
                for (Publicacion publicacion : a.publicaciones.cargar(usuario.getUsername())) {
                    resultado.agregarUnico(visibleParaCliente(publicacion));
                }
            }
        }

        return resultado;
    }

    static ListaEnlazada<Publicacion> ordenar(ListaEnlazada<Publicacion> lista) {
        ListaEnlazada<Publicacion> resultado = new ListaEnlazada<>();
        Comparator<Publicacion> orden = Comparator.comparing(Publicacion::getFechaPublicacion).reversed().thenComparing(Publicacion::getId);

        for (Publicacion publicacion : lista) {
            resultado.insertarOrdenado(publicacion, orden);
        }

        return resultado;
    }

    Respuesta ejecutar(String actor, Solicitud solicitud) throws Exception {
        return switch (solicitud.getOperacion()) {
            case PUBLICACIONES -> publicaciones(solicitud);
            case TIMELINE -> timeline(actor, solicitud);
            case PUBLICAR_TEXTO, PUBLICAR_IMAGEN, PUBLICAR_STICKER -> publicar(actor, solicitud);
            case LIKE_PUBLICACION -> alternarLike(actor, solicitud);
            case COMENTAR_PUBLICACION -> comentar(actor, solicitud);
            case COMENTAR_STICKER -> comentarSticker(actor, solicitud);
            case ELIMINAR_COMENTARIO -> eliminarComentario(actor, solicitud);
            case EDITAR_PUBLICACION -> editar(actor, solicitud);
            case ELIMINAR_PUBLICACION -> eliminar(actor, solicitud);
            default -> throw new IllegalArgumentException("Operación de publicaciones no soportada.");
        };
    }

    private Respuesta publicaciones(Solicitud solicitud) throws Exception {
        String objetivo = InstaServicio.normalizar(solicitud.arg(0));
        a.exigirCuentaActiva(objetivo);
        return a.pagina(ordenar(visiblesParaCliente(a.publicaciones.cargar(objetivo))), solicitud.arg(1));
    }

    private Respuesta timeline(String actor, Solicitud solicitud) throws Exception {
        ListaEnlazada<Publicacion> resultado = new ListaEnlazada<>();

        for (Publicacion publicacion : todas()) {
            if (publicacion.getAutor().equals(actor) || a.seguimientoServicio.sigue(actor, publicacion.getAutor())) {
                resultado.agregarUnico(publicacion);
            }
        }

        return a.pagina(ordenar(resultado), solicitud.arg(0));
    }

    private Respuesta publicar(String actor, Solicitud solicitud) throws Exception {
        Publicacion publicacion;
        RepositorioInsta.Cambio imagen = null;

        if (solicitud.getOperacion() == Solicitud.Operacion.PUBLICAR_TEXTO) {
            publicacion = Publicacion.crearTexto(actor, solicitud.arg(0));
        } else if (solicitud.getOperacion() == Solicitud.Operacion.PUBLICAR_STICKER) {
            Sticker sticker = a.stickerServicio.buscar(actor, solicitud.arg(1));
            publicacion = Publicacion.crearConSticker(actor, solicitud.arg(0), sticker.getRutaImagen());
        } else {
            String carpeta = solicitud.arg(1).strip();

            if (!carpeta.isEmpty()) {
                StickerServicio.validarCarpeta(carpeta);
                if (!a.repo.existe(actor + "/folders_personales/" + carpeta)) {
                    throw new IllegalArgumentException("La carpeta personal no existe.");
                }
            }

            String ruta = actor + "/imagenes/" + UUID.randomUUID() + ".png";
            publicacion = Publicacion.crearConImagen(actor, solicitud.arg(0), ruta, carpeta);
            imagen = new RepositorioInsta.Cambio(ruta, ImagenesInsta.validar(solicitud.getArchivo()));
        }

        ListaEnlazada<Publicacion> lista = a.publicaciones.cargar(actor);
        lista.agregar(publicacion);

        if (imagen == null) {
            a.publicaciones.guardar(actor, lista);
        } else {
            a.repo.guardar(a.publicaciones.cambio(actor, lista), imagen);
        }

        return respuestaConPublicacion("Publicación guardada.", publicacion);
    }

    private Respuesta alternarLike(String actor, Solicitud solicitud) throws Exception {
        String autor = InstaServicio.normalizar(solicitud.arg(0));
        PublicacionContexto contexto = buscar(autor, solicitud.arg(1));
        contexto.publicacion.alternarLike(actor);
        a.publicaciones.guardar(autor, contexto.lista);
        return respuestaConPublicacion("Me gusta actualizado.", contexto.publicacion);
    }

    private Respuesta comentar(String actor, Solicitud solicitud) throws Exception {
        String autor = InstaServicio.normalizar(solicitud.arg(0));
        PublicacionContexto contexto = buscar(autor, solicitud.arg(1));
        contexto.publicacion.agregarComentario(actor, solicitud.arg(2));
        a.publicaciones.guardar(autor, contexto.lista);
        return respuestaConPublicacion("Comentario agregado.", contexto.publicacion);
    }

    private Respuesta comentarSticker(String actor, Solicitud solicitud) throws Exception {
        String autor = InstaServicio.normalizar(solicitud.arg(0));
        PublicacionContexto contexto = buscar(autor, solicitud.arg(1));
        Sticker sticker = a.stickerServicio.buscar(actor, solicitud.arg(2));
        contexto.publicacion.agregarComentarioSticker(actor, sticker.getRutaImagen());
        a.publicaciones.guardar(autor, contexto.lista);
        return respuestaConPublicacion("Sticker agregado como comentario.", contexto.publicacion);
    }

    private Respuesta eliminarComentario(String actor, Solicitud solicitud) throws Exception {
        String autor = InstaServicio.normalizar(solicitud.arg(0));
        PublicacionContexto contexto = buscar(autor, solicitud.arg(1));

        if (!contexto.publicacion.eliminarComentario(solicitud.arg(2), actor)) {
            throw new IllegalArgumentException("El comentario no existe o no te pertenece.");
        }

        a.publicaciones.guardar(autor, contexto.lista);
        return respuestaConPublicacion("Comentario eliminado.", contexto.publicacion);
    }

    private Respuesta editar(String actor, Solicitud solicitud) throws Exception {
        PublicacionContexto contexto = buscar(actor, solicitud.arg(0));
        contexto.publicacion.editarContenido(solicitud.arg(1));
        a.publicaciones.guardar(actor, contexto.lista);
        return respuestaConPublicacion("Publicación actualizada.", contexto.publicacion);
    }

    private Respuesta eliminar(String actor, Solicitud solicitud) throws Exception {
        ListaEnlazada<Publicacion> lista = a.publicaciones.cargar(actor);
        Publicacion publicacion = lista.buscar(p -> p.getId().equals(solicitud.arg(0)));

        if (publicacion == null) {
            throw new IllegalArgumentException("La publicación no existe.");
        }

        lista.eliminar(publicacion);
        a.publicaciones.guardar(actor, lista);
        return Respuesta.exito("Publicación eliminada.");
    }

    private PublicacionContexto buscar(String autor, String id) throws Exception {
        a.exigirCuentaActiva(autor);
        ListaEnlazada<Publicacion> lista = a.publicaciones.cargar(autor);
        Publicacion publicacion = lista.buscar(p -> p.getId().equals(id));

        if (publicacion == null) {
            throw new IllegalArgumentException("La publicación no existe.");
        }

        return new PublicacionContexto(lista, publicacion);
    }

    private Respuesta respuestaConPublicacion(String mensaje, Publicacion publicacion) throws Exception {
        ListaEnlazada<Publicacion> resultado = new ListaEnlazada<>();
        resultado.agregar(visibleParaCliente(publicacion));
        return Respuesta.exito(mensaje).conLista(resultado);
    }

    private ListaEnlazada<Publicacion> visiblesParaCliente(ListaEnlazada<Publicacion> publicaciones) throws Exception {
        ListaEnlazada<Publicacion> resultado = new ListaEnlazada<>();
        for (Publicacion publicacion : publicaciones) {
            resultado.agregar(visibleParaCliente(publicacion));
        }
        return resultado;
    }

    private Publicacion visibleParaCliente(Publicacion publicacion) throws Exception {
        List<Comentario> visibles = new ArrayList<>();
        for (Comentario comentario : publicacion.getComentarios()) {
            UsuarioInsta autorComentario = a.buscarUsuario(comentario.getAutor());
            if (autorComentario != null && autorComentario.estaActiva()) {
                visibles.add(comentario);
            }
        }
        return publicacion.copiaConComentarios(visibles);
    }

    private static final class PublicacionContexto {
        private final ListaEnlazada<Publicacion> lista;
        private final Publicacion publicacion;

        private PublicacionContexto(ListaEnlazada<Publicacion> lista, Publicacion publicacion) {
            this.lista = lista;
            this.publicacion = publicacion;
        }
    }
}
