
package persistencia;

import insta.modelo.Mensaje;

public class GestorInboxBinario
        extends GestorListaInsta<Mensaje> {

    public GestorInboxBinario(RepositorioInsta repositorio) {
        super(repositorio, "inbox.ins", Mensaje.class);
    }
}