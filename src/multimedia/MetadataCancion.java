package multimedia;

public class MetadataCancion {
    private String titulo = "Desconocido";
    private String artista = "Artista desconocido";
    private String album = "Álbum desconocido";
    private String anio = "";
    private String descripcion = "";
    private byte[] caratula;
    public String getTitulo() { return titulo; }
    public void setTitulo(String v) { if (v != null && !v.isBlank()) titulo = v; }
    public String getArtista() { return artista; }
    public void setArtista(String v) { if (v != null && !v.isBlank()) artista = v; }
    public String getAlbum() { return album; }
    public void setAlbum(String v) { if (v != null && !v.isBlank()) album = v; }
    public String getAnio() { return anio; }
    public void setAnio(String v) { if (v != null) anio = v; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { if (v != null) descripcion = v; }
    public byte[] getCaratula() { return caratula; }
    public void setCaratula(byte[] v) { caratula = v; }
}
