
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
import sistema.SeguridadArchivos;
import sistema.Sesion;

public class GestorArchivos {

    private final File directorioRaiz;
    private File directorioActual;

    public GestorArchivos() {
        File raiz = RutasSistema.getRaizExplorador();
        if (raiz == null) raiz = RutasSistema.getRaizSistema();
        if (!raiz.exists()) raiz.mkdirs();

        directorioRaiz = normalizar(raiz);
        directorioActual = directorioRaiz;
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
        if (segura != null && segura.exists()
                && segura.isDirectory() && estaPermitido(segura)) {
            directorioActual = segura;
        }
    }

    public File resolverSeguro(String nombre) {
        if (nombre == null || nombre.isBlank()) return null;
        File candidato = normalizar(new File(directorioActual, nombre));
        return candidato != null && estaPermitido(candidato)
                ? candidato : null;
    }

    public String obtenerPrompt() {
        String relativa = directorioRaiz.toPath()
                .relativize(directorioActual.toPath()).toString();
        StringBuilder base = new StringBuilder("Z:\\");

        if (!Sesion.esAdministrador() && Sesion.getUsuarioActual() != null) {
            base.append(Sesion.getUsuarioActual().getUsername());
        }

        if (!relativa.isEmpty()) {
            if (base.charAt(base.length() - 1) != '\\') base.append('\\');
            base.append(relativa.replace(File.separatorChar, '\\'));
        }
        return base + ">";
    }

    public String crearDirectorio(String nombre) {
        File archivo = resolverSeguro(nombre);
        if (archivo == null) return "Ruta no permitida.\n";
        if (archivo.exists()) {
            return "Ya existe un archivo o carpeta con ese nombre.\n";
        }
        return archivo.mkdir()
                ? "Carpeta creada correctamente.\n"
                : "No se pudo crear la carpeta.\n";
    }

    public String crearArchivo(String nombre) {
        File archivo = resolverSeguro(nombre);
        if (archivo == null) return "Ruta no permitida.\n";
        if (archivo.exists()) return "El archivo ya existe.\n";

        try {
            return archivo.createNewFile()
                    ? "Archivo creado correctamente.\n"
                    : "No se pudo crear el archivo.\n";
        } catch (IOException e) {
            return "Error al crear archivo: " + e.getMessage() + "\n";
        }
    }

    public String eliminar(String nombre) {
        File archivo = resolverSeguro(nombre);
        if (archivo == null || archivo.equals(directorioRaiz)) {
            return "Ruta no permitida.\n";
        }
        if (!archivo.exists()) return "El archivo o carpeta no existe.\n";

        return eliminarRecursivo(archivo)
                ? "Eliminado correctamente.\n"
                : "Error al intentar eliminar el elemento.\n";
    }

    private boolean eliminarRecursivo(File archivo) {
        if (!SeguridadArchivos.esPermitido(archivo)) return false;

        if (archivo.isDirectory()) {
            File[] hijos = archivo.listFiles();
            if (hijos != null) {
                for (File hijo : hijos) {
                    if (!eliminarRecursivo(hijo)) return false;
                }
            }
        }
        return archivo.delete();
    }

    public String cambiarDirectorio(String nombre) {
        if ("..".equals(nombre)) return subirDirectorio();

        File destino = resolverSeguro(nombre);
        if (destino == null) return "Ruta no permitida.\n";
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
        directorioActual = padre != null && estaPermitido(padre)
                ? padre : directorioRaiz;
        return "";
    }

    public String listarDirectorio() {
        if (!estaPermitido(directorioActual)) return "Acceso denegado.";

        StringBuilder resultado = new StringBuilder();
        File[] lista = directorioActual.listFiles();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy  hh:mm a");

        resultado.append(" Directorio de ")
                .append(obtenerPrompt().replace(">", "")).append("\n\n");

        long totalBytes = 0;
        int totalArchivos = 0;
        int totalCarpetas = 0;

        if (lista != null) {
            Arrays.sort(lista, (a, b) -> {
                if (a.isDirectory() && !b.isDirectory()) return -1;
                if (!a.isDirectory() && b.isDirectory()) return 1;
                return a.getName().compareToIgnoreCase(b.getName());
            });

            for (File archivo : lista) {
                if (!estaPermitido(archivo)) continue;
                String fecha = formato.format(new Date(archivo.lastModified()));

                if (archivo.isDirectory()) {
                    totalCarpetas++;
                    resultado.append(String.format(
                            "%s    <DIR>          %s%n",
                            fecha, archivo.getName()
                    ));
                } else {
                    totalArchivos++;
                    totalBytes += archivo.length();
                    resultado.append(String.format(
                            "%s          %,10d %s%n",
                            fecha, archivo.length(), archivo.getName()
                    ));
                }
            }
        }

        resultado.append(String.format(
                "              %d archivos  %,12d bytes%n",
                totalArchivos, totalBytes
        ));
        resultado.append(String.format(
                "              %d carpetas  %,12d bytes libres%n",
                totalCarpetas, directorioActual.getFreeSpace()
        ));
        return resultado.toString();
    }

    public String leerArchivo(String nombre) {
        File archivo = resolverSeguro(nombre);
        if (archivo == null || !archivo.exists() || archivo.isDirectory()) {
            return "El archivo no existe o es una carpeta.\n";
        }

        StringBuilder resultado = new StringBuilder();
        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                resultado.append(linea).append("\n");
            }
        } catch (IOException e) {
            return "Error al leer el archivo: " + e.getMessage() + "\n";
        }
        return resultado.toString();
    }

    public String obtenerInformacion(String nombre) {
        File archivo = resolverSeguro(nombre);
        if (archivo == null || !archivo.exists()) {
            return "El sistema no puede encontrar el elemento especificado.\n";
        }

        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        StringBuilder resultado = new StringBuilder();

        resultado.append("--------------------------------------------------\n");
        resultado.append("INFORMACIÓN DETALLADA\n");
        resultado.append("--------------------------------------------------\n");
        resultado.append("Nombre:              ").append(archivo.getName()).append("\n");
        resultado.append("Tipo:                ")
                .append(archivo.isDirectory() ? "Directorio / Carpeta" : "Archivo de datos")
                .append("\n");
        resultado.append("Ruta:                ")
                .append(obtenerRutaVisible(archivo)).append("\n");
        resultado.append("Tamaño:              ")
                .append(archivo.isDirectory() ? calcularTamano(archivo) : archivo.length())
                .append(" bytes\n");
        resultado.append("Última modificación: ")
                .append(formato.format(new Date(archivo.lastModified()))).append("\n");
        resultado.append("--------------------------------------------------\n");

        return resultado.toString();
    }

    private String obtenerRutaVisible(File archivo) {
        String relativa = directorioRaiz.toPath()
                .relativize(normalizar(archivo).toPath()).toString();
        return "Z:\\" + relativa.replace(File.separatorChar, '\\');
    }

    private long calcularTamano(File directorio) {
        long tamano = 0;
        File[] hijos = directorio.listFiles();
        if (hijos != null) {
            for (File archivo : hijos) {
                tamano += archivo.isFile()
                        ? archivo.length() : calcularTamano(archivo);
            }
        }
        return tamano;
    }

    public String generarArbol() {
        StringBuilder resultado = new StringBuilder();
        resultado.append("Estructura de ")
                .append(obtenerPrompt().replace(">", "")).append("\n.\n");
        arbolRecursivo(directorioActual, "", resultado);
        return resultado.toString();
    }

    private void arbolRecursivo(
            File carpeta,
            String prefijo,
            StringBuilder resultado
    ) {
        File[] hijos = carpeta.listFiles();
        if (hijos == null) return;

        Arrays.sort(hijos, Comparator.comparing(
                File::getName, String.CASE_INSENSITIVE_ORDER
        ));

        for (int i = 0; i < hijos.length; i++) {
            boolean ultimo = i == hijos.length - 1;
            File hijo = hijos[i];
            if (!SeguridadArchivos.esPermitido(hijo)) continue;

            resultado.append(prefijo).append(ultimo ? "└── " : "├── ")
                    .append(hijo.getName())
                    .append(hijo.isDirectory() ? "/" : "").append("\n");

            if (hijo.isDirectory()) {
                arbolRecursivo(
                        hijo,
                        prefijo + (ultimo ? "    " : "│   "),
                        resultado
                );
            }
        }
    }

    private boolean estaPermitido(File archivo) {
        return archivo != null
                && archivo.toPath().startsWith(directorioRaiz.toPath())
                && SeguridadArchivos.esPermitido(archivo);
    }

    private File normalizar(File archivo) {
        if (archivo == null) return null;
        try {
            return archivo.getCanonicalFile();
        } catch (IOException e) {
            return archivo.getAbsoluteFile();
        }
    }
}