
package CMD;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;

public class ComandoCMD2 {

    private File carpetaActual;
    private final File carpetaRaiz;

    public ComandoCMD2(File carpetaRaiz) {
        this.carpetaRaiz = normalizar(carpetaRaiz);
        this.carpetaActual = this.carpetaRaiz;
    }

    public File getCarpetaActual() {
        return carpetaActual;
    }

    public String ap(String nombreArchivo, String texto) {
        File archivo = resolver(nombreArchivo);
        if (archivo == null) {
            return "Ruta no permitida.";
        }
        if (!archivo.exists()) {
            return "El archivo no existe.";
        }
        if (!archivo.isFile()) {
            return "El nombre indicado no corresponde a un archivo.";
        }

        try (FileWriter escritor = new FileWriter(archivo, true)) {
            escritor.write(texto);
            escritor.write(System.lineSeparator());
            return "Texto agregado correctamente.";
        } catch (IOException e) {
            return "Error al agregar texto al archivo.";
        }
    }

    public String ren(String actual, String nuevo) {
        File archivoActual = resolver(actual);
        File archivoNuevo = resolver(nuevo);

        if (archivoActual == null || archivoNuevo == null || archivoActual.equals(carpetaRaiz)) {
            return "Ruta no permitida.";
        }
        if (!archivoActual.exists()) {
            return "El archivo o carpeta no existe.";
        }
        if (archivoNuevo.exists()) {
            return "Ya existe un archivo o carpeta con ese nombre.";
        }

        return archivoActual.renameTo(archivoNuevo) ? "Renombrado correctamente." : "No se pudo renombrar.";
    }

    public String copy(String origen, String destino) {
        File archivoOrigen = resolver(origen);
        File archivoDestino = resolver(destino);

        if (archivoOrigen == null || archivoDestino == null) {
            return "Ruta no permitida.";
        }
        if (!archivoOrigen.exists()) {
            return "El archivo de origen no existe.";
        }
        if (!archivoOrigen.isFile()) {
            return "Solo se pueden copiar archivos.";
        }
        if (archivoDestino.exists()) {
            return "Ya existe un archivo con ese nombre.";
        }

        try {
            Files.copy(archivoOrigen.toPath(), archivoDestino.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
            return "Archivo copiado correctamente.";
        } catch (IOException e) {
            return "Error al copiar el archivo.";
        }
    }

    private String buscar(File carpeta, String nombre) {
        StringBuilder resultado = new StringBuilder();
        File[] archivos = carpeta.listFiles();

        if (archivos == null) {
            return "";
        }

        for (File archivo : archivos) {
            if (archivo.getName().toLowerCase().contains(nombre.toLowerCase())) {
                resultado.append(archivo.getAbsolutePath()).append("\n");
            }
            if (archivo.isDirectory()) {
                resultado.append(buscar(archivo, nombre));
            }
        }

        return resultado.toString();
    }

    public String find(String nombre) {
        String resultado = buscar(carpetaActual, nombre);
        return resultado.isEmpty() ? "No se encontraron archivos o carpetas." : resultado;
    }

    public String info(String nombre) {
        File archivo = resolver(nombre);
        if (archivo == null) {
            return "Ruta no permitida.";
        }
        if (!archivo.exists()) {
            return "El archivo o carpeta no existe.";
        }

        String tipo = archivo.isDirectory() ? "Carpeta" : "Archivo";
        return "Tipo: " + tipo + "\nRuta: " + archivo.getAbsolutePath() + "\nTamaño: " + archivo.length() + " bytes\nÚltima modificación: " + new Date(archivo.lastModified());
    }

    public String tree() {
        return carpetaActual.getName() + "\n" + mostrarArbol(carpetaActual, "");
    }

    private String mostrarArbol(File carpeta, String espacio) {
        StringBuilder resultado = new StringBuilder();
        File[] archivos = carpeta.listFiles();
        if (archivos == null) {
            return "";
        }
        for (File archivo : archivos) {
            resultado.append(espacio).append("|-- ").append(archivo.getName()).append("\n");
            if (archivo.isDirectory()) {
                resultado.append(mostrarArbol(archivo, espacio + "    "));
            }
        }
        return resultado.toString();
    }

    public String cls() {
        return "";
    }

    public String help() {
        return "Mkdir <nombre> - Crea una carpeta\n"
                + "Mfile <nombre.ext> - Crea un archivo\n"
                + "Rm <nombre> - Elimina archivo o carpeta\n"
                + "Cd <nombre> - Cambia de carpeta\n"
                + ".. - Regresa a la carpeta anterior\n"
                + "Dir - Lista archivos y carpetas\n"
                + "Date - Muestra la fecha actual\n"
                + "Time - Muestra la hora actual\n"
                + "Wr <archivo.ext> <texto> - Escribe en un archivo\n"
                + "Rd <archivo.ext> - Lee un archivo\n"
                + "Ap <archivo.ext> <texto> - Agrega texto a un archivo\n"
                + "Ren <actual> <nuevo> - Renombra archivo o carpeta\n"
                + "Copy <origen> <destino> - Copia un archivo\n"
                + "Find <nombre> - Busca archivos o carpetas\n"
                + "Info <nombre> - Muestra información\n"
                + "Tree - Muestra la estructura de carpetas\n"
                + "Cls - Limpia la consola\n"
                + "Help - Muestra los comandos\n"
                + "Exit - Cierra la aplicación";
    }

    public void setCarpetaActual(File carpetaActual) {
        File nueva = normalizar(carpetaActual);
        if (nueva != null && nueva.toPath().startsWith(carpetaRaiz.toPath())) {
            this.carpetaActual = nueva;
        }
    }

    private File resolver(String nombre) {
        File candidato = normalizar(new File(carpetaActual, nombre));
        if (candidato == null || !candidato.toPath().startsWith(carpetaRaiz.toPath())) {
            return null;
        }
        return candidato;
    }

    private File normalizar(File archivo) {
        if (archivo == null) {
            return null;
        }
        try {
            return archivo.getCanonicalFile();
        } catch (IOException e) {
            return archivo.getAbsoluteFile();
        }
    }
}
