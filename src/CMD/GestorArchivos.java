
package CMD;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class GestorArchivos {
    private final File directorioRaiz;
    private File directorioActual;

    public GestorArchivos() {
        this.directorioRaiz = new File(System.getProperty("user.dir"), "ConsolaSimulada");
        if (!directorioRaiz.exists()) {
            directorioRaiz.mkdirs();
        }
        this.directorioActual = directorioRaiz;
    }

    public File getCarpetaActual() {
        return directorioActual;
    }

    public File getDirectorioActual() {
        return directorioActual;
    }

    public void setCarpetaActual(File carpetaActual) {
        if (carpetaActual != null && carpetaActual.exists() && carpetaActual.isDirectory()) {
            if (carpetaActual.getAbsolutePath().startsWith(directorioRaiz.getAbsolutePath())) {
                this.directorioActual = carpetaActual;
            }
        }
    }

    public String obtenerPrompt() {
        String relativa = directorioActual.getAbsolutePath().substring(directorioRaiz.getAbsolutePath().length());
        if (relativa.startsWith(File.separator)) {
            relativa = relativa.substring(1);
        }
        return "Z\\" + (relativa.isEmpty() ? "" : "\\" + relativa) + ">";
    }

    public String crearDirectorio(String nombre) {
        File f = new File(directorioActual, nombre);
        if (f.exists()) {
            return "Ya existe un archivo o carpeta con ese nombre.\n";
        }
        if (f.mkdir()) {
            return "Carpeta creada correctamente.\n";
        }
        return "No se pudo crear la carpeta.\n";
    }

    public String crearArchivo(String nombre) {
        File f = new File(directorioActual, nombre);
        if (f.exists()) {
            return "El archivo ya existe.\n";
        }
        try {
            if (f.createNewFile()) {
                return "Archivo creado correctamente.\n";
            }
            return "No se pudo crear el archivo.\n";
        } catch (IOException e) {
            return "Error al crear archivo: " + e.getMessage() + "\n";
        }
    }

    public String eliminar(String nombre) {
        File f = new File(directorioActual, nombre);
        if (!f.exists()) {
            return "El archivo o carpeta no existe.\n";
        }
        if (eliminarRecursivo(f)) {
            return "Eliminado correctamente.\n";
        }
        return "Error al intentar eliminar el elemento.\n";
    }

    private boolean eliminarRecursivo(File f) {
        if (f.isDirectory()) {
            File[] hijos = f.listFiles();
            if (hijos != null) {
                for (File hijo : hijos) {
                    eliminarRecursivo(hijo);
                }
            }
        }
        return f.delete();
    }

    public String cambiarDirectorio(String nombre) {
        File destino = new File(directorioActual, nombre);
        if (!destino.exists()) {
            return "El sistema no puede encontrar la ruta especificada.\n";
        }
        if (!destino.isDirectory()) {
            return "El nombre de directorio no es válido.\n";
        }
        directorioActual = destino;
        return "";
    }

    public String subirDirectorio() {
        if (directorioActual.equals(directorioRaiz)) {
            return "Ya se encuentra en el directorio raíz simulado.\n";
        }
        File padre = directorioActual.getParentFile();
        if (padre != null && padre.getAbsolutePath().startsWith(directorioRaiz.getAbsolutePath())) {
            directorioActual = padre;
        } else {
            directorioActual = directorioRaiz;
        }
        return "";
    }

    public String listarDirectorio() {
        StringBuilder sb = new StringBuilder();
        File[] lista = directorioActual.listFiles();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy  hh:mm a");

        sb.append(" El volumen de la unidad simulada es CONSOLA\n");
        sb.append(" El número de serie del volumen es SIM-2024\n\n");
        sb.append(" Directorio de ").append(obtenerPrompt().replace(">", "")).append("\n\n");

        long totalBytes = 0;
        int totalArchivos = 0;
        int totalCarpetas = 0;

        if (lista != null) {
            Arrays.sort(lista, (a, b) -> {
                if (a.isDirectory() && !b.isDirectory()) return -1;
                if (!a.isDirectory() && b.isDirectory()) return 1;
                return a.getName().compareToIgnoreCase(b.getName());
            });

            for (File f : lista) {
                String fecha = sdf.format(new Date(f.lastModified()));
                if (f.isDirectory()) {
                    totalCarpetas++;
                    sb.append(String.format("%s    <DIR>          %s\n", fecha, f.getName()));
                } else {
                    totalArchivos++;
                    totalBytes += f.length();
                    sb.append(String.format("%s          %,10d %s\n", fecha, f.length(), f.getName()));
                }
            }
        }

        sb.append(String.format("              %d archivos  %,12d bytes\n", totalArchivos, totalBytes));
        sb.append(String.format("              %d carpetas  %,12d bytes libres\n", totalCarpetas, directorioActual.getFreeSpace()));
        return sb.toString();
    }

    public String leerArchivo(String nombre) {
        File f = new File(directorioActual, nombre);
        if (!f.exists() || f.isDirectory()) {
            return "El archivo no existe o es una carpeta.\n";
        }

        StringBuilder sb = new StringBuilder();
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(f);
            br = new BufferedReader(fr);
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea).append("\n");
            }
        } catch (IOException e) {
            return "Error al leer el archivo: " + e.getMessage() + "\n";
        } finally {
            try {
                if (br != null) br.close();
                if (fr != null) fr.close();
            } catch (IOException ignored) {}
        }
        return sb.toString();
    }

    public String obtenerInformacion(String nombre) {
        File f = new File(directorioActual, nombre);
        if (!f.exists()) {
            return "El sistema no puede encontrar el elemento especificado.\n";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("--------------------------------------------------\n");
        sb.append("INFORMACIÓN DETALLADA\n");
        sb.append("--------------------------------------------------\n");
        sb.append("Nombre:              ").append(f.getName()).append("\n");
        sb.append("Tipo:                ").append(f.isDirectory() ? "Directorio / Carpeta" : "Archivo de datos").append("\n");
        sb.append("Ruta absoluta:       ").append(f.getAbsolutePath()).append("\n");
        sb.append("Tamaño:              ").append(f.isDirectory() ? calcularTamano(f) : f.length()).append(" bytes\n");
        sb.append("Última modificación: ").append(sdf.format(new Date(f.lastModified()))).append("\n");
        sb.append("Lectura permitida:   ").append(f.canRead() ? "Sí" : "No").append("\n");
        sb.append("Escritura permitida: ").append(f.canWrite() ? "Sí" : "No").append("\n");
        sb.append("--------------------------------------------------\n");
        return sb.toString();
    }

    private long calcularTamano(File dir) {
        long tam = 0;
        File[] hijos = dir.listFiles();
        if (hijos != null) {
            for (File f : hijos) {
                if (f.isFile()) tam += f.length();
                else tam += calcularTamano(f);
            }
        }
        return tam;
    }

    public String generarArbol() {
        StringBuilder sb = new StringBuilder();
        sb.append("Estructura de carpetas de ").append(obtenerPrompt().replace(">", "")).append("\n.\n");
        arbolRecursivo(directorioActual, "", sb);
        return sb.toString();
    }

    private void arbolRecursivo(File carpeta, String prefijo, StringBuilder sb) {
        File[] hijos = carpeta.listFiles();
        if (hijos != null) {
            Arrays.sort(hijos, Comparator.comparing(File::getName));
            for (int i = 0; i < hijos.length; i++) {
                boolean esUltimo = (i == hijos.length - 1);
                File hijo = hijos[i];
                sb.append(prefijo).append(esUltimo ? "└── " : "├── ").append(hijo.getName())
                        .append(hijo.isDirectory() ? "/" : "").append("\n");
                if (hijo.isDirectory()) {
                    arbolRecursivo(hijo, prefijo + (esUltimo ? "    " : "│   "), sb);
                }
            }
        }
    }
}
