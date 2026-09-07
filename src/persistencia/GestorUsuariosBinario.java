
package persistencia;

import java.io.*;
import java.util.ArrayList;
import modelo.*;

public class GestorUsuariosBinario {
    private final String ruta = "usuarios.dat";
    
    public void guardarUsuarios(ArrayList<UsuarioSistema> usuarios){
        try{
            ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(ruta));
            salida.writeObject(usuarios);
            salida.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public ArrayList<UsuarioSistema> cargarUsuarios(){
        File archivo = new File(ruta);
        
        if(!archivo.exists()){
            return new ArrayList<>();
        }
        
        try{
            ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(ruta));
            ArrayList<UsuarioSistema> usuarios = (ArrayList<UsuarioSistema>) entrada.readObject();
            entrada.close();
            return usuarios;
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
