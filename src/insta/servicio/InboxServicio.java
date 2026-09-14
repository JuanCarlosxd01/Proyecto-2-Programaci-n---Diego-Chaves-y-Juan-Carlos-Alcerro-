
package insta.servicio;

import estructuras.ListaEnlazada;
import insta.modelo.Mensaje;
import java.util.Comparator;
import red.Respuesta;
import red.Solicitud;

public class InboxServicio {

    private final InstaServicio a;

    public InboxServicio(InstaServicio a) {
        this.a = a;
    }

    Respuesta ejecutar(String actor, Solicitud solicitud) throws Exception {
        ListaEnlazada<Mensaje> propios = a.inbox.cargar(actor);

        if (solicitud.getOperacion() == Solicitud.Operacion.NO_LEIDOS) {
            ListaEnlazada<Mensaje> resultado = new ListaEnlazada<>();

            for (Mensaje mensaje : propios) {
                if (mensaje.recibidoPor(actor) && !mensaje.estaLeido()
                        && a.visible(mensaje.getEmisor())) {
                    resultado.agregar(mensaje);
                }
            }
            return a.pagina(resultado, solicitud.arg(0));
        }

        if (solicitud.getOperacion() == Solicitud.Operacion.CONVERSACIONES) {
            ListaEnlazada<String> resultado = new ListaEnlazada<>();

            for (Mensaje mensaje : propios) {
                String contacto = mensaje.enviadoPor(actor)
                        ? mensaje.getReceptor() : mensaje.getEmisor();
                if (a.visible(contacto)) resultado.agregarUnico(contacto);
            }
            return a.pagina(resultado, solicitud.arg(0));
        }

        String otro = InstaServicio.normalizar(solicitud.arg(0));

        if (actor.equals(otro)) {
            throw new IllegalArgumentException("Selecciona otra cuenta.");
        }

        if (solicitud.getOperacion() == Solicitud.Operacion.ELIMINAR_CONVERSACION) {
            a.usuario(otro);
            propios.eliminarSi(
                    mensaje -> mensaje.perteneceAConversacion(actor, otro)
            );
            a.inbox.guardar(actor, propios);
            return Respuesta.exito("Conversación eliminada de tu historial.");
        }

        a.exigirCuentaActiva(otro);

        if (solicitud.getOperacion() == Solicitud.Operacion.CONVERSACION) {
            ListaEnlazada<Mensaje> resultado = new ListaEnlazada<>();
            var orden = Comparator.comparing(Mensaje::getFechaEnvio)
                    .thenComparing(Mensaje::getId);

            for (Mensaje mensaje : propios) {
                if (mensaje.perteneceAConversacion(actor, otro)) {
                    resultado.insertarOrdenado(mensaje, orden);
                }
            }
            return a.pagina(resultado, solicitud.arg(1));
        }

        ListaEnlazada<Mensaje> ajenos = a.inbox.cargar(otro);

        if (solicitud.getOperacion() == Solicitud.Operacion.LEER_CONVERSACION) {
            for (Mensaje mensaje : propios) {
                if (mensaje.recibidoPor(actor) && mensaje.enviadoPor(otro)) {
                    mensaje.marcarComoLeido();

                    Mensaje espejo = ajenos.buscar(
                            elemento -> elemento.getId().equals(mensaje.getId())
                    );
                    if (espejo != null) espejo.marcarComoLeido();
                }
            }

            a.repo.guardar(
                    a.inbox.cambio(actor, propios),
                    a.inbox.cambio(otro, ajenos)
            );
            return Respuesta.exito("Mensajes marcados como leídos.");
        }

        Mensaje nuevo = solicitud.getOperacion() == Solicitud.Operacion.ENVIAR_MENSAJE
                ? Mensaje.crearTexto(actor, otro, solicitud.arg(1))
                : Mensaje.crearSticker(
                        actor, otro,
                        a.stickerServicio.buscar(actor, solicitud.arg(1))
                                .getRutaImagen()
                );

        propios.agregar(nuevo);
        ajenos.agregar(nuevo);

        a.repo.guardar(
                a.inbox.cambio(actor, propios),
                a.inbox.cambio(otro, ajenos)
        );

        ListaEnlazada<Mensaje> resultado = new ListaEnlazada<>();
        resultado.agregar(nuevo);

        return Respuesta.exito("Mensaje enviado.").conLista(resultado);
    }
}