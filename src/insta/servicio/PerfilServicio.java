
package insta.servicio;

import insta.modelo.UsuarioInsta;
import persistencia.RepositorioInsta;
import red.*;
import java.util.Locale;
import java.util.UUID;

public class PerfilServicio {

    private final InstaServicio a;

    public PerfilServicio(InstaServicio a) {
        this.a = a;
    }

    Respuesta.DatosUsuario datos(
            String actor,
            String objetivo
    ) throws Exception {

        UsuarioInsta usuario = a.usuario(objetivo);

        return new Respuesta.DatosUsuario(
                usuario,
                a.seguimientoServicio.cantidad(objetivo, true),
                a.seguimientoServicio.cantidad(objetivo, false),
                a.publicaciones.cargar(objetivo).size(),
                !actor.equals(objetivo)
                        && a.seguimientoServicio.sigue(actor, objetivo)
        );
    }

    Respuesta ejecutar(String actor, Solicitud solicitud)
            throws Exception {

        if (solicitud.getOperacion()
                == Solicitud.Operacion.PERFIL) {

            String objetivo =
                    InstaServicio.normalizar(solicitud.arg(0));

            if (!actor.equals(objetivo)) {
                a.exigirCuentaActiva(objetivo);
            }

            return Respuesta.exito("Perfil.")
                    .conUsuario(datos(actor, objetivo));
        }

        UsuarioInsta usuario = a.usuario(actor);
        RepositorioInsta.Cambio foto = null;

        switch (solicitud.getOperacion()) {
            case DESACTIVAR ->
                usuario.desactivar();

            case REACTIVAR ->
                usuario.activar();

            case FOTO_PERFIL -> {
                String ruta = actor
                        + "/imagenes/perfil-"
                        + UUID.randomUUID()
                        + ".png";

                foto = new RepositorioInsta.Cambio(
                        ruta,
                        ImagenesInsta.validar(
                                solicitud.getArchivo()
                        )
                );

                usuario.setRutaFotoPerfil(ruta);
            }

            case EDITAR_PERFIL -> {
                String nombre = solicitud.arg(0).strip();

                String genero = solicitud.arg(1)
                        .toUpperCase(Locale.ROOT);

                int edad = Integer.parseInt(solicitud.arg(2));
                String nueva = solicitud.arg(4);
                String biografia = solicitud.arg(5);

                if (genero.length() != 1) {
                    throw new IllegalArgumentException(
                            "Género inválido."
                    );
                }

                InstaServicio.validarDatos(
                        nombre,
                        genero.charAt(0),
                        edad,
                        nueva.isEmpty() ? null : nueva
                );

                if (!SeguridadPassword.verificar(
                        solicitud.arg(3),
                        usuario.getPasswordHash(),
                        usuario.getPasswordSalt()
                )) {
                    throw new IllegalArgumentException(
                            "La contraseña actual no coincide."
                    );
                }

                usuario.setNombreCompleto(nombre);
                usuario.setGenero(genero.charAt(0));
                usuario.setEdad(edad);
                usuario.setBiografia(biografia);

                if (!nueva.isEmpty()) {
                    var credenciales =
                            SeguridadPassword.generarCredenciales(nueva);

                    usuario.actualizarCredenciales(
                            credenciales.getPasswordHash(),
                            credenciales.getPasswordSalt()
                    );
                }
            }

            default ->
                throw new IllegalArgumentException(
                        "Operación de perfil inválida."
                );
        }

        if (foto == null) {
            a.guardarUsuario(usuario);
        } else {
            a.guardarUsuario(usuario, foto);
        }

        return Respuesta.exito("Cuenta actualizada.")
                .conUsuario(datos(actor, actor));
    }
}