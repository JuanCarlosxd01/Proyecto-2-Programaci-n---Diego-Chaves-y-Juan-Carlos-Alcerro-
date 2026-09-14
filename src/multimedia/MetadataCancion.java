/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package multimedia;

/**
 *
 * @author diego
 */
public class MetadataCancion {

    private String titulo;
    private String artista;
    private String album;
    private String anio;
    private byte[] caratula;

    public MetadataCancion() {
        titulo = "Desconocido";
        artista = "Artista desconocido";
        album = "Álbum desconocido";
        anio = "";
        caratula = null;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        if (titulo != null && !titulo.isBlank()) {
            this.titulo = titulo;
        }
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        if (artista != null && !artista.isBlank()) {
            this.artista = artista;
        }
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        if (album != null && !album.isBlank()) {
            this.album = album;
        }
    }

    public String getAnio() {
        return anio;
    }

    public void setAnio(String anio) {
        if (anio != null) {
            this.anio = anio;
        }
    }

    public byte[] getCaratula() {
        return caratula;
    }

    public void setCaratula(byte[] caratula) {
        this.caratula = caratula;
    }
}
