
package CMD;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import sistema.RutasSistema;
import sistema.Sesion;

public class GestorArchivos {

    private final File directorioRaiz;
    private File directorioActual;

    public GestorArchivos() {
        File raiz = RutasSistema.getRaizExplorador();
        if (raiz == null) {
            throw new IllegalStateException("No hay una sesión activa para abrir CMD.");
        }
        if (!raiz.exists()) {
            raiz.mkdirs();
        }
        this.directorioRaiz = normalizar(raiz);
        this.directorioActual = this.directorioRaiz;
    }

    public File getCarpetaActual() {
        return directorioActual;
    }

    public File getDirectorioActual() {
        return directorioActual;
    }

    public File getDirectorioRaiz() {
        return directorioRaiz;
    }

    public void setCarpetaActual(File carpetaActual) {
        File segura = normalizar(carpetaActual);
        if (segura != null && segura.exists() && segura.isDirectory() && estaPermitido(segura)) {
            this.directorioActual = segura;
        }
    }

    public File resolverSeguro(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        File candidato = normalizar(new File(directorioActual, nombre));
        return candidato != null && estaPermitido(candidato) ? candidato : null;
    }

    public String obtenerPrompt() {
        String relativa = directorioRaiz.toPath().relativize(directorioActual.toPath()).toString();
        StringBuilder base = new StringBuilder("Z:\\");

        if (!Sesion.esAdministrador() && Sesion.getUsuarioActual() != null) {
            base.append(Sesion.getUsuarioActual().getUsername());
        }

        if (!relativa.isEmpty()) {
            if (base.charAt(base.length() - 1) != '\\') {
                base.append('\\');
            }
            base.append(relativa.replace(File.separatorChar, '\\'));
        }

        return base + ">";
    }

    public String crearDirectorio(String nombre) {
        File f = resolverSeguro(nombre);
        if (f == null) {
            return "Ruta no permitida.\n";
        }
        if (f.exists()) {
            return "Ya existe un archivo o carpeta con ese nombre.\n";
        }
        return f.mkdir() ? "Carpeta creada correctamente.\n" : "No se pudo crear la carpeta.\n";
    }

    public String crearArchivo(String nombre) {
        File f = resolverSeguro(nombre);
        if (f == null) {
            return "Ruta no permitida.\n";
        }
        if (f.exists()) {
            return "El archivo ya existe.\n";
        }
        try {
            return f.createNewFile() ? "Archivo creado correctamente.\n" : "No se pudo crear el archivo.\n";
        } catch (IOException e) {
            return "Error al crear archivo: " + e.getMessage() + "\n";
        }
    }

    public String eliminar(String nombre) {
        File f = resolverSeguro(nombre);
        if (f == null || f.equals(directorioRaiz)) {
            return "Ruta no permitida.\n";
        }
        if (!f.exists()) {
            return "El archivo o carpeta no existe.\n";
        }
        return eliminarRecursivo(f) ? "Eliminado correctamente.\n" : "Error al intentar eliminar el elemento.\n";
    }

    private boolean eliminarRecursivo(File f) {
        if (f.isDirectory()) {
            File[] hijos = f.listFiles();
            if (hijos != null) {
                for (File hijo : hijos) {
                    if (!eliminarRecursivo(hijo)) {
                        return false;
                    }
                }
            }
        }
        return f.delete();
    }

    public String cambiarDirectorio(String nombre) {
        if ("..".equals(nombre)) {
            return subirDirectorio();
        }
        File destino = resolverSeguro(nombre);
        if (destino == null) {
            return "Ruta no permitida.\n";
        }
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
            return "Ya se encuentra en el directorio raíz permitido.\n";
        }
        File padre = normalizar(directorioActual.getParentFile());
        directorioActual = padre != null && estaPermitido(padre) ? padre : directorioRaiz;
        return "";
    }

    public String listarDirectorio() {
        StringBuilder sb = new StringBuilder();
        File[] lista = directorioActual.listFiles();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy  hh:mm a");
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
                    sb.append(String.format("%s    <DIR>          %s%n", fecha, f.getName()));
                } else {
                    totalArchivos++;
                    totalBytes += f.length();
                    sb.append(String.format("%s          %,10d %s%n", fecha, f.length(), f.getName()));
                }
            }
        }

        sb.append(String.format("              %d archivos  %,12d bytes%n", totalArchivos, totalBytes));
        sb.append(String.format("              %d carpetas  %,12d bytes libres%n", totalCarpetas, directorioActual.getFreeSpace()));
        return sb.toString();
    }

    public String leerArchivo(String nombre) {
        File f = resolverSeguro(nombre);
        if (f == null || !f.exists() || f.isDirectory()) {
            return "El archivo no existe o es una carpeta.\n";
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea).append("\n");
            }
        } catch (IOException e) {
            return "Error al leer el archivo: " + e.getMessage() + "\n";
        }
        return sb.toString();
    }

    public String obtenerInformacion(String nombre) {
        File f = resolverSeguro(nombre);
        if (f == null || !f.exists()) {
            return "El sistema no puede encontrar el elemento especificado.\n";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("--------------------------------------------------\n");
        sb.append("INFORMACIÓN DETALLADA\n");
        sb.append("--------------------------------------------------\n");
        sb.append("Nombre:              ").append(f.getName()).append("\n");
        sb.append("Tipo:                ").append(f.isDirectory() ? "Directorio / Carpeta" : "Archivo de datos").append("\n");
        sb.append("Ruta:                ").append(obtenerRutaVisible(f)).append("\n");
        sb.append("Tamaño:              ").append(f.isDirectory() ? calcularTamano(f) : f.length()).append(" bytes\n");
        sb.append("Última modificación: ").append(sdf.format(new Date(f.lastModified()))).append("\n");
        sb.append("--------------------------------------------------\n");
        return sb.toString();
    }

    private String obtenerRutaVisible(File archivo) {
        String relativa = directorioRaiz.toPath().relativize(normalizar(archivo).toPath()).toString();
        return "Z:\\" + relativa.replace(File.separatorChar, '\\');
    }

    private long calcularTamano(File dir) {
        long tam = 0;
        File[] hijos = dir.listFiles();
        if (hijos != null) {
            for (File f : hijos) {
                tam += f.isFile() ? f.length() : calcularTamano(f);
            }
        }
        return tam;
    }

    public String generarArbol() {
        StringBuilder sb = new StringBuilder();
        sb.append("Estructura de ").append(obtenerPrompt().replace(">", "")).append("\n.\n");
        arbolRecursivo(directorioActual, "", sb);
        return sb.toString();
    }

    private void arbolRecursivo(File carpeta, String prefijo, StringBuilder sb) {
        File[] hijos = carpeta.listFiles();
        if (hijos == null) {
            return;
        }
        Arrays.sort(hijos, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        for (int i = 0; i < hijos.length; i++) {
            boolean ultimo = i == hijos.length - 1;
            File hijo = hijos[i];
            sb.append(prefijo).append(ultimo ? "└── " : "├── ").append(hijo.getName()).append(hijo.isDirectory() ? "/" : "").append("\n");
            if (hijo.isDirectory()) {
                arbolRecursivo(hijo, prefijo + (ultimo ? "    " : "│   "), sb);
            }
        }
    }

    private boolean estaPermitido(File archivo) {
        if (archivo == null) {
            return false;
        }

        try {
            File archivoCanonico = archivo.getCanonicalFile();
            File raizCanonica = directorioRaiz.getCanonicalFile();

            return archivoCanonico.toPath().startsWith(raizCanonica.toPath());

        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    private File normalizar(File archivo) {
        if (archivo == null) {
            return null;
        }
        try {
            return archivo.getCanonicalFile();
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
}
