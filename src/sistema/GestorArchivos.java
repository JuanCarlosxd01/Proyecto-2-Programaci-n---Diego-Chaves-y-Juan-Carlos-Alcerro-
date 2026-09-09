
package sistema;

import java.io.*;

public class GestorArchivos {
    
    private File archivoCopiado;
    
    public boolean crearCarpeta(File carpetaPadre, String nombre){
        if(carpetaPadre == null || !carpetaPadre.isDirectory()){
            return false;
        }
        
        File nuevaCarpeta = new File(carpetaPadre, nombre);
        
        if(nuevaCarpeta.exists()){
            return false;
        }
        return nuevaCarpeta.mkdir();
    }
    
    public boolean crearArchivo(File carpetaPadre, String nombre){
        if(carpetaPadre == null || !carpetaPadre.isDirectory()){
            return false;
        }
        
        File nuevoArchivo = new File(carpetaPadre, nombre);
        
        if(nuevoArchivo.exists()){
            return false;
        }
        
        try{
            return nuevoArchivo.createNewFile();
        }catch(IOException e){
            return false;
        }
    }
    
    public boolean renombrar(File archivo, String nuevoNombre){
        if(archivo == null || !archivo.exists()){
            return false;
        }
        File nuevoArchivo = new File(archivo.getParentFile(), nuevoNombre);
        
        if(nuevoArchivo.exists()){
            return false;
        }
        return archivo.renameTo(nuevoArchivo);
    }
    
    public void copiar(File archivo){
        if(archivo != null && archivo.exists()){
            archivoCopiado = archivo;
        }
    }
    
    public boolean pegar(File carpetaDestino){
        if(archivoCopiado == null){
            return false;
        }
        
        if(carpetaDestino == null || !carpetaDestino.isDirectory()){
            return false;
        }
        
        File destino = new File(carpetaDestino, archivoCopiado.getName());
        if(destino.exists()){
            return false;
        }
        
        try{
            if(archivoCopiado.isDirectory()){
                copiarCarpeta(archivoCopiado, destino);
            }
            else{
                copiarArchivo(archivoCopiado, destino);
            }
            return true;
        }catch(IOException e){
            return false;
        }
    }
    
    private void copiarArchivo(File origen, File destino) throws IOException{
        FileInputStream entrada = new FileInputStream(origen);
        FileOutputStream salida = new FileOutputStream(destino);
        
        byte[] buffer = new byte[1024];
        int cantidad;
        
        while((cantidad = entrada.read(buffer)) != -1){
            salida.write(buffer, 0, cantidad);
        }
        entrada.close();
        salida.close();
    }
    
    private void copiarCarpeta(File origen, File destino) throws IOException{
        destino.mkdir();
        File[] archivos = origen.listFiles();
        
        if(archivos == null){
            return;
        }
        
        for(File archivo : archivos){
            File nuevoDestino = new File(destino, archivo.getName());
            
            if(archivo.isDirectory()){
                copiarCarpeta(archivo, nuevoDestino);
            }
            else{
                copiarArchivo(archivo, nuevoDestino);
            }
        }
    }
    
    public boolean hayArchivoCopiado(){
        return archivoCopiado != null;
    }
    
}
