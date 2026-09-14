
package CMD;

import interfaz.CMDPanel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class controladorComandos {

    private CMDPanel consola;
    private GestorArchivos gestor;
    private ComandoCMD2 cmd2;

    public controladorComandos(CMDPanel consola, GestorArchivos gestor) {
        this.consola = consola;
        this.gestor = gestor;
        cmd2 = new ComandoCMD2(gestor.getCarpetaActual());
    }

    public void ejecutarComando(String entrada) {
        entrada = entrada.trim();
        if (entrada.isEmpty()) return;

        String[] partes = separar(entrada);
        String comandoOriginal = partes[0];
        String comando = comandoOriginal.toLowerCase();

        switch (comando) {
            case "mkdir":
                mkdir(partes);
                break;
            case "mfile":
                mFile(partes);
                break;
            case "rm":
                rm(partes);
                break;
            case "cd":
                cd(partes);
                break;
            case "cd..":
            case "..":
                regresar();
                break;
            case "dir":
                dir();
                break;
            case "date":
                date();
                break;
            case "time":
                time();
                break;
            case "wr":
                wr(partes);
                break;
            case "rd":
                rd(partes);
                break;
            case "ap":
                if (partes.length < 3) {
                    consola.imprimir("Uso: Ap <archivo.ext> <texto>");
                } else {
                    String texto = "";
                    for (int i = 2; i < partes.length; i++) {
                        texto += partes[i];
                        if (i < partes.length - 1) texto += " ";
                    }
                    consola.imprimir(cmd2.ap(partes[1], texto));
                }
                break;
            case "ren":
                if (partes.length < 3) {
                    consola.imprimir("Uso: Ren <actual> <nuevo>");
                } else {
                    consola.imprimir(cmd2.ren(partes[1], partes[2]));
                }
                break;
            case "copy":
                if (partes.length < 3) {
                    consola.imprimir("Uso: Copy <origen> <destino>");
                } else {
                    consola.imprimir(cmd2.copy(partes[1], partes[2]));
                }
                break;
            case "find":
                if (partes.length < 2) {
                    consola.imprimir("Uso: Find <nombre>");
                } else {
                    consola.imprimir(cmd2.find(partes[1]));
                }
                break;
            case "info":
                if (partes.length < 2) {
                    consola.imprimir("Uso: Info <nombre>");
                } else {
                    consola.imprimir(gestor.obtenerInformacion(partes[1]));
                }
                break;
            case "tree":
                consola.imprimir(gestor.generarArbol());
                break;
            case "cls":
                consola.limpiar();
                break;
            case "help":
                consola.imprimir(cmd2.help());
                break;
            case "exit":
                consola.cerrar();
                break;
            default:
                consola.imprimir("'" + comandoOriginal
                        + "' no se reconoce como un comando interno o externo.");
        }
    }

    private String[] separar(String entrada) {
        int espacio = entrada.indexOf(' ');
        String comando = espacio < 0
                ? entrada : entrada.substring(0, espacio);
        String resto = espacio < 0
                ? "" : entrada.substring(espacio + 1).trim();

        if (Set.of("cd", "mkdir", "rm", "mfile", "rd", "info", "find")
                .contains(comando.toLowerCase())) {
            if (resto.startsWith("\"") && resto.endsWith("\"")
                    && resto.length() > 1) {
                resto = resto.substring(1, resto.length() - 1);
            }
            return resto.isEmpty()
                    ? new String[]{comando}
                    : new String[]{comando, resto};
        }

        List<String> tokens = new ArrayList<>();
        Matcher matcher = Pattern.compile("\"([^\"]*)\"|(\\S+)")
                .matcher(entrada);

        while (matcher.find()) {
            tokens.add(matcher.group(1) == null
                    ? matcher.group(2) : matcher.group(1));
        }
        return tokens.toArray(String[]::new);
    }

    private void mkdir(String[] partes) {
        if (partes.length < 2) {
            consola.imprimir("Uso: Mkdir <nombre>");
            return;
        }
        consola.imprimir(gestor.crearDirectorio(partes[1]));
    }

    private void mFile(String[] partes) {
        if (partes.length < 2) {
            consola.imprimir("Uso: Mfile <nombre.ext>");
            return;
        }
        consola.imprimir(gestor.crearArchivo(partes[1]));
    }

    private void rm(String[] partes) {
        if (partes.length < 2) {
            consola.imprimir("Uso: Rm <nombre>");
            return;
        }
        consola.imprimir(gestor.eliminar(partes[1]));
    }

    private void cd(String[] partes) {
        if (partes.length < 2) {
            consola.imprimir("Uso: Cd <nombre carpeta>");
            return;
        }
        consola.imprimir(gestor.cambiarDirectorio(partes[1]));
        consola.setCarpetaActual(gestor.getCarpetaActual());
        cmd2.setCarpetaActual(gestor.getCarpetaActual());
    }

    private void regresar() {
        consola.imprimir(gestor.subirDirectorio());
        consola.setCarpetaActual(gestor.getCarpetaActual());
        cmd2.setCarpetaActual(gestor.getCarpetaActual());
    }

    private void dir() {
        consola.imprimir(gestor.listarDirectorio());
    }

    private void date() {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        consola.imprimir("Fecha actual: " + formato.format(new Date()));
    }

    private void time() {
        SimpleDateFormat formato = new SimpleDateFormat("HH:mm:ss");
        consola.imprimir("Hora actual: " + formato.format(new Date()));
    }

    private void wr(String[] partes) {
        if (partes.length < 3) {
            consola.imprimir("Uso: Wr <archivo.ext> <texto>");
            return;
        }

        String texto = "";
        for (int i = 2; i < partes.length; i++) {
            texto += partes[i];
            if (i < partes.length - 1) texto += " ";
        }

        File archivo = gestor.resolverSeguro(partes[1]);
        if (archivo == null) {
            consola.imprimir("Ruta no permitida.");
            return;
        }
        if (!archivo.exists()) {
            consola.imprimir("El archivo no existe.");
            return;
        }
        if (!archivo.isFile()) {
            consola.imprimir("El nombre indicado no corresponde a un archivo.");
            return;
        }

        try (FileWriter escritor = new FileWriter(archivo)) {
            escritor.write(texto);
            escritor.write(System.lineSeparator());
            consola.imprimir("Texto escrito correctamente.");
        } catch (IOException e) {
            consola.imprimir("Error al escribir en el archivo.");
        }
    }

    public void rd(String[] partes) {
        if (partes.length < 2) {
            consola.imprimir("Uso: Rd <archivo.ext>");
            return;
        }
        consola.imprimir(gestor.leerArchivo(partes[1]));
    }
}