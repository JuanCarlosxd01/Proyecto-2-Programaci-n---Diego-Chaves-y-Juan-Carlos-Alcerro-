
package persistencia;

import insta.modelo.Seguimiento;


public class GestorSeguidoresBinario
        extends GestorListaInsta<Seguimiento> {

    public GestorSeguidoresBinario(
            RepositorioInsta repositorio,
            boolean seguidores
    ) {
        super(
                repositorio,
                seguidores ? "followers.ins" : "following.ins",
                Seguimiento.class
        );
    }
}