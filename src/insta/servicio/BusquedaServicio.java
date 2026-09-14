
package insta.servicio;

import estructuras.ListaEnlazada;
import insta.modelo.*;
import red.*;
import java.util.Locale;
import java.util.regex.Pattern;

public class BusquedaServicio {

    private final InstaServicio a;

    private static final Pattern TOKEN = Pattern.compile(
            "(?<![\\p{L}\\p{N}_])([@#])([\\p{L}\\p{N}_]+)"
    );

    public BusquedaServicio(InstaServicio a) {
        this.a = a;
    }

    private boolean contiene(
            String texto,
            char prefijo,
            String valor
    ) {
        var coincidencias = TOKEN.matcher(texto);

        while (coincidencias.find()) {
            if (coincidencias.group(1).charAt(0) == prefijo
                    && coincidencias.group(2)
                            .equalsIgnoreCase(valor)) {
                return true;
            }
        }

        return false;
    }

    Respuesta ejecutar(String actor, Solicitud solicitud)
            throws Exception {

        if (solicitud.getOperacion()
                == Solicitud.Operacion.BUSCAR_PERSONAS) {

            String texto = solicitud.arg(0)
                    .strip()
                    .toLowerCase(Locale.ROOT);

            if (texto.length() > 24) {
                throw new IllegalArgumentException(
                        "Búsqueda demasiado larga."
                );
            }

            ListaEnlazada<Respuesta.DatosUsuario> resultado =
                    new ListaEnlazada<>();

            for (UsuarioInsta usuario : a.usuarios()) {
                String username = usuario.getUsername() == null ? "" : usuario.getUsername().toLowerCase(Locale.ROOT);
                String nombre = usuario.getNombreCompleto() == null ? "" : usuario.getNombreCompleto().toLowerCase(Locale.ROOT);
                if (usuario.estaActiva() && (username.contains(texto) || nombre.contains(texto))) {
                    resultado.agregar(a.perfilServicio.datos(actor, usuario.getUsername()));
                }
            }

            return a.pagina(resultado, solicitud.arg(1));
        }

        boolean menciones = solicitud.getOperacion()
                == Solicitud.Operacion.MENCIONES;

        String valor = menciones
                ? actor
                : solicitud.arg(0).strip().replaceFirst("^#", "");

        if (!valor.matches("[\\p{L}\\p{N}_]+")) {
            throw new IllegalArgumentException(
                    "Hashtag inválido."
            );
        }

        ListaEnlazada<Publicacion> resultado = new ListaEnlazada<>();

        for (Publicacion publicacion :
                a.publicacionServicio.todas()) {

            boolean coincidePublicacion = contiene(publicacion.getContenido(), menciones ? '@' : '#', valor);
            boolean coincideComentario = false;
            if (menciones) {
                for (Comentario comentario : publicacion.getComentarios()) {
                    if (!comentario.getAutor().equals(actor) && comentario.esTexto() && contiene(comentario.getTexto(), '@', valor)) {
                        coincideComentario = true;
                        break;
                    }
                }
            }
            if ((!menciones || !publicacion.getAutor().equals(actor) || coincideComentario) && (coincidePublicacion || coincideComentario)) {
                resultado.agregarUnico(publicacion);
            }
        }

        return a.pagina(
                PublicacionServicio.ordenar(resultado),
                solicitud.arg(menciones ? 0 : 1)
        );
    }
}
