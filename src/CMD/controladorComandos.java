
package CMD;

import interfaz.CMDPanel;
import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;

public class controladorComandos {
    private CMDPanel consola;
    private GestorArchivos gestor;
    private boolean modoEscritura;
    private boolean append;
    private File archivoEscritura;
    private FileWriter escritor;
    private ComandoCMD2 cmd2;
    
    public controladorComandos(CMDPanel consola, GestorArchivos gestor){
        this.consola = consola;
        this.gestor = gestor;
        modoEscritura = false;
        cmd2 = new ComandoCMD2(gestor.getCarpetaActual());
    }
    
    public void ejecutarComando(String entrada){
        entrada = entrada.trim();
        if(entrada.isEmpty()){
            return;
        }
        String[] partes = entrada.split("\\s+");
        String comando = partes[0];
        switch(comando){
            case "Mkdir":
                mkdir(partes);
                break;
            case "Mfile":
                mFile(partes);
                break;
            case "Rm":
                rm(partes);
                break;
            case "Cd":
                cd(partes);
                break;
            case "..":
                regresar();
                break;
            case "Dir":
                dir();
                break;
            case "Date":
                date();
                break;
            case "Time":
                time();
                break;
            case "Wr":
                wr(partes);
                break;
            case "Rd":
                rd(partes);
                break;
            case "Ap":
                  if(partes.length < 3){
                    consola.imprimir("Uso: Ap <archivo.ext> <texto>");
                } else {
                    String texto = "";
                    for(int i = 2; i < partes.length; i++){
                        texto += partes[i];
                        if(i < partes.length - 1){
                            texto += " ";
                        }
                    }
                    consola.imprimir(
                        cmd2.ap(partes[1], texto)
                    );   
                  }
                  break;
            case "Ren":
                if(partes.length < 3){
                    consola.imprimir("Uso: Ren <actual> <nuevo>");
                } else {
                    consola.imprimir(cmd2.ren(partes[1], partes[2]));
                }
                break;
            case "Copy":
                if(partes.length < 3){
                    consola.imprimir("Uso: Copy <origen> <destino>");
                } else {
                    consola.imprimir(cmd2.copy(partes[1], partes[2]));
                }
                break;
            case "Find":
                if(partes.length < 2){
                    consola.imprimir("Uso: Find <nombre>");
                } else {
                    consola.imprimir(cmd2.find(partes[1]));
                }
                break;
            case "Info":
                if(partes.length < 2){
                    consola.imprimir("Uso: Info <nombre>");
                } else {
                    consola.imprimir(gestor.obtenerInformacion(partes[1]));
                }
                break;
            case "Tree":
                consola.imprimir(gestor.generarArbol());
                break;
            case "Cls":
                consola.limpiar();
                break;
            case "Help":
                consola.imprimir(cmd2.help());
                break;      
            case "Exit":
                consola.cerrar();
                break;
            default:
                consola.imprimir("'" + comando + "' no se reconoce como un comando interno o externo.");
        }
    }
    
    private void mkdir(String[] partes){
        if(partes.length < 2){
            consola.imprimir("Uso: Mkdir <nombre>");
            return;
        }
        consola.imprimir(gestor.crearDirectorio(partes[1]));
    }
    
    private void mFile(String[] partes){
        if(partes.length < 2){
            consola.imprimir("Uso: Mfile <nombre.ext>");
            return;
        }
        consola.imprimir(gestor.crearArchivo(partes[1]));
    }
    
    private void rm(String[] partes){
        if(partes.length < 2){
            consola.imprimir("Uso: Rm <nombre>");
            return;
        }
        consola.imprimir(gestor.eliminar(partes[1]));
    }
    
    private void cd(String[] partes){
        if(partes.length < 2){
            consola.imprimir("Uso: Cd <nombre carpete>");
            return;
        }
        String resultado = gestor.cambiarDirectorio(partes[1]);
        consola.imprimir(resultado);
        consola.setCarpetaActual(gestor.getCarpetaActual());
        cmd2.setCarpetaActual(gestor.getCarpetaActual());
    }
    
    private void regresar(){
        consola.imprimir(gestor.subirDirectorio());
        consola.setCarpetaActual(gestor.getCarpetaActual());
        cmd2.setCarpetaActual(gestor.getCarpetaActual());
    }
    
    private void dir(){
         consola.imprimir(gestor.listarDirectorio());
    }
    
    private void date(){
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        consola.imprimir("Fecha actual: " + formato.format(new Date()));
    }
    
    private void time(){
        SimpleDateFormat formato = new SimpleDateFormat("HH:mm:ss");
        consola.imprimir("Hora actual: " + formato.format(new Date()));
    }
    
    private void wr(String[] partes){
        if(partes.length < 3){
            consola.imprimir("Uso: Wr <archivo.ext> <texto>");
            return;
        }
        String nombreArchivo = partes[1];
        String texto = "";
        for(int i = 2; i < partes.length; i++){
            texto += partes[i];
            if(i < partes.length - 1){
                texto += " ";
            }
        }
        File archivo = new File(
                consola.getCarpetaActual(),
                nombreArchivo
        );
        if(!archivo.exists()){
            consola.imprimir("El archivo no existe.");
            return;
        }
        if(!archivo.isFile()){
            consola.imprimir(
                    "El nombre indicado no corresponde a un archivo."
            );
            return;
        }
        try{

            FileWriter escritor = new FileWriter(archivo);

            escritor.write(texto);
            escritor.write(System.lineSeparator());

            escritor.close();

            consola.imprimir(
                    "Texto escrito correctamente."
            );
        }catch(IOException e){
            consola.imprimir(
                    "Error al escribir en el archivo."
            );
        }
    }
    
    public void rd(String[] partes){
        if(partes.length < 2){
            consola.imprimir("Uso: Rd <archivo.ext>");
            return;
        }
        consola.imprimir(gestor.leerArchivo(partes[1]));
    }
       
}
