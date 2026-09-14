/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package multimedia;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import java.io.File;
/**
 *
 * @author diego
 */
public class LectorMetadataMP3 {

    public static MetadataCancion leer(File archivo) {
        MetadataCancion metadata = new MetadataCancion();

        if (archivo == null || !archivo.exists()) {
            return metadata;
        }

        metadata.setTitulo(quitarExtension(archivo.getName()));

        try {
            Mp3File mp3 = new Mp3File(archivo.getAbsolutePath());

            if (mp3.hasId3v2Tag()) {
                ID3v2 tag = mp3.getId3v2Tag();

                metadata.setTitulo(tag.getTitle());
                metadata.setArtista(tag.getArtist());
                metadata.setAlbum(tag.getAlbum());
                metadata.setAnio(tag.getYear());

                byte[] imagen = tag.getAlbumImage();

                if (imagen != null && imagen.length > 0) {
                    metadata.setCaratula(imagen);
                }
            } else if (mp3.hasId3v1Tag()) {
                ID3v1 tag = mp3.getId3v1Tag();

                metadata.setTitulo(tag.getTitle());
                metadata.setArtista(tag.getArtist());
                metadata.setAlbum(tag.getAlbum());
                metadata.setAnio(tag.getYear());
            }
        } catch (Exception e) {
            System.err.println(
                    "No se pudieron leer los metadatos de "
                    + archivo.getName() + ": " + e.getMessage()
            );
        }

        return metadata;
    }

    private static String quitarExtension(String nombre) {
        int posicion = nombre.lastIndexOf(".");

        if (posicion > 0) {
            return nombre.substring(0, posicion);
        }

        return nombre;
    }
}
