
package persistencia;

import insta.modelo.Sticker;


public class GestorStickersBinario
        extends GestorListaInsta<Sticker> {

    public GestorStickersBinario(RepositorioInsta repositorio) {
        super(repositorio, "stickers.ins", Sticker.class);
    }
}
