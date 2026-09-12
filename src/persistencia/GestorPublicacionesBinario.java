
package persistencia;

import insta.modelo.Publicacion;

public class GestorPublicacionesBinario
        extends GestorListaInsta<Publicacion> {

    public GestorPublicacionesBinario(RepositorioInsta repositorio) {
        super(repositorio, "insta.ins", Publicacion.class);
    }
}
