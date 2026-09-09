
package sistema;

import java.io.File;

public class OrganizadorArchivos {
    
    public void organizar(File carpeta){
        if(carpeta == null || ! carpeta.isDirectory()){
            return;
        }
        
        File carpetaImagenes = new File(carpeta, "Imágenes");
        File carpetaDocumentos = new File(carpeta, "Documentos");
        File carpetaMusica = new File(carpeta, "Música");
        
        File[] archivos = carpeta.listFiles();
        if(archivos == null){
            return;
        }
        
        for(File archivo : archivos){
            if(archivo.isFile()){
                String nombre = archivo.getName().toLowerCase();
                if(esImagen(nombre)){
                    if(!carpetaImagenes.exists()){
                        carpetaImagenes.mkdir();
                    }
                    moverArchivo(archivo, carpetaImagenes);
                }
                else if(esDocumento(nombre)){
                    if(!carpetaDocumentos.exists()){
                        carpetaDocumentos.mkdir();
                    }
                    moverArchivo(archivo, carpetaDocumentos);
                }
                else if(esMusica(nombre)){
                    if(!carpetaMusica.exists()){
                        carpetaMusica.mkdir();
                    }
                    moverArchivo(archivo, carpetaMusica);
                }
            }
        }
    }
    
    private boolean esImagen(String nombre){
        return nombre.endsWith(".png") || nombre.endsWith(".jpg") || nombre.endsWith(".jpeg") || nombre.endsWith(".gif");
    }
    
    private boolean esDocumento(String nombre){
        return nombre.endsWith(".txt") || nombre.endsWith(".pdf") || nombre.endsWith(".doc") || nombre.endsWith(".docx") || nombre.endsWith(".edt"); 
    }
    
    private boolean esMusica(String nombre){
        return nombre.endsWith(".mp3") || nombre.endsWith(".wav") || nombre.endsWith(".wma");
    }
    
    private void moverArchivo(File archivo, File carpetaDestino){
        File destino = new File(carpetaDestino, archivo.getName());
        archivo.renameTo(destino);
    }
        
}
