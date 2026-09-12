
package insta.servicio;

import estructuras.ListaEnlazada;
import insta.modelo.Seguimiento;
import red.*;

public class SeguimientoServicio {

    private final InstaServicio a;

    public SeguimientoServicio(InstaServicio a) {
        this.a = a;
    }

    boolean sigue(String actor, String objetivo)
            throws Exception {

        return a.seguidos.cargar(actor).buscar(
                seguimiento -> seguimiento.estaActivo()
                        && seguimiento.getUsername().equals(objetivo)
        ) != null;
    }

    int cantidad(String usuario, boolean seguidores)
            throws Exception {

        int cantidad = 0;

        for (Seguimiento seguimiento :
                (seguidores ? a.seguidores : a.seguidos)
                        .cargar(usuario)) {

            if (seguimiento.estaActivo()
                    && a.visible(seguimiento.getUsername())) {
                cantidad++;
            }
        }

        return cantidad;
    }

    private void establecer(
            ListaEnlazada<Seguimiento> lista,
            String usuario,
            boolean activo
    ) {
        Seguimiento seguimiento = lista.buscar(
                elemento -> elemento.getUsername().equals(usuario)
        );

        if (seguimiento == null) {
            seguimiento = new Seguimiento(usuario);
            lista.agregar(seguimiento);
        }

        if (activo) {
            seguimiento.activar();
        } else {
            seguimiento.desactivar();
        }
    }

    Respuesta ejecutar(String actor, Solicitud solicitud)
            throws Exception {

        String objetivo =
                InstaServicio.normalizar(solicitud.arg(0));

        a.exigirCuentaActiva(objetivo);

        if (solicitud.getOperacion()
                == Solicitud.Operacion.SEGUIDORES
                || solicitud.getOperacion()
                == Solicitud.Operacion.SEGUIDOS) {

            ListaEnlazada<Respuesta.DatosUsuario> resultado =
                    new ListaEnlazada<>();

            var gestor = solicitud.getOperacion()
                    == Solicitud.Operacion.SEGUIDORES
                    ? a.seguidores
                    : a.seguidos;

            for (Seguimiento seguimiento : gestor.cargar(objetivo)) {
                if (seguimiento.estaActivo()
                        && a.visible(seguimiento.getUsername())) {
                    resultado.agregar(a.perfilServicio.datos(
                            actor,
                            seguimiento.getUsername()
                    ));
                }
            }

            return a.pagina(resultado, solicitud.arg(1));
        }

        if (actor.equals(objetivo)) {
            throw new IllegalArgumentException(
                    "No puedes seguirte a ti mismo."
            );
        }

        boolean activo = solicitud.getOperacion()
                == Solicitud.Operacion.SEGUIR;

        ListaEnlazada<Seguimiento> propios =
                a.seguidos.cargar(actor);

        ListaEnlazada<Seguimiento> ajenos =
                a.seguidores.cargar(objetivo);

        establecer(propios, objetivo, activo);
        establecer(ajenos, actor, activo);

        a.repo.guardar(
                a.seguidos.cambio(actor, propios),
                a.seguidores.cambio(objetivo, ajenos)
        );

        return Respuesta.exito(
                activo
                        ? "Seguimiento activo."
                        : "Dejaste de seguir esta cuenta."
        );
    }
}