
package sistema;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import modelo.UsuarioSistema;

public final class RutasSistema {

    public static final String NOMBRE_RAIZ = "Z";
    public static final String NOMBRE_DOCUMENTOS = "Mis Documentos";
    public static final String NOMBRE_MUSICA = "Música";
    public static final String NOMBRE_IMAGENES = "Mis Imágenes";

    private RutasSistema() {
    }

    public static File getRaizSistema() {
        return new File(NOMBRE_RAIZ);
    }

    public static File getCarpetaUsuario(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        return new File(getRaizSistema(), username.trim());
    }

    public static File getCarpetaUsuario(UsuarioSistema usuario) {
        if (usuario == null) {
            return null;
        }

        return getCarpetaUsuario(usuario.getUsername());
    }

    public static File getCarpetaUsuarioActual() {
        return getCarpetaUsuario(Sesion.getUsuarioActual());
    }

    public static File getDocumentosUsuarioActual() {
        File carpetaUsuario = getCarpetaUsuarioActual();

        if (carpetaUsuario == null) {
            return null;
        }

        return new File(carpetaUsuario, NOMBRE_DOCUMENTOS);
    }

    public static File getMusicaUsuarioActual() {
        File carpetaUsuario = getCarpetaUsuarioActual();

        if (carpetaUsuario == null) {
            return null;
        }

        return new File(carpetaUsuario, NOMBRE_MUSICA);
    }

    public static File getImagenesUsuarioActual() {
        File carpetaUsuario = getCarpetaUsuarioActual();

        if (carpetaUsuario == null) {
            return null;
        }

        return new File(carpetaUsuario, NOMBRE_IMAGENES);
    }

    public static File getDocumentos(UsuarioSistema usuario) {
        File carpetaUsuario = getCarpetaUsuario(usuario);

        if (carpetaUsuario == null) {
            return null;
        }

        return new File(carpetaUsuario, NOMBRE_DOCUMENTOS);
    }

    public static File getMusica(UsuarioSistema usuario) {
        File carpetaUsuario = getCarpetaUsuario(usuario);

        if (carpetaUsuario == null) {
            return null;
        }

        return new File(carpetaUsuario, NOMBRE_MUSICA);
    }

    public static File getImagenes(UsuarioSistema usuario) {
        File carpetaUsuario = getCarpetaUsuario(usuario);

        if (carpetaUsuario == null) {
            return null;
        }

        return new File(carpetaUsuario, NOMBRE_IMAGENES);
    }

    public static File getRaizExplorador() {
        UsuarioSistema usuario = Sesion.getUsuarioActual();

        if (usuario == null) {
            return getRaizSistema();
        }

        if (Sesion.esAdministrador()) {
            return getRaizSistema();
        }

        return getCarpetaUsuario(usuario);
    }

    public static void crearEstructuraUsuario(UsuarioSistema usuario) {
        File carpetaUsuario = getCarpetaUsuario(usuario);

        if (carpetaUsuario == null) {
            return;
        }

        File raiz = getRaizSistema();

        if (!raiz.exists()) {
            raiz.mkdirs();
        }

        if (!carpetaUsuario.exists()) {
            carpetaUsuario.mkdirs();
        }

        normalizarCarpetasPrincipales(carpetaUsuario);

        File documentos = getDocumentos(usuario);
        File musica = getMusica(usuario);
        File imagenes = getImagenes(usuario);

        if (!documentos.exists()) {
            documentos.mkdirs();
        }

        if (!musica.exists()) {
            musica.mkdirs();
        }

        if (!imagenes.exists()) {
            imagenes.mkdirs();
        }
    }

    public static boolean esCarpetaPrincipal(File archivo) {
        if (archivo == null || !archivo.isDirectory()) {
            return false;
        }

        try {
            File padre = archivo.getParentFile();
            if (padre == null || !padre.getParentFile().getCanonicalFile().equals(getRaizSistema().getCanonicalFile())) {
                return false;
            }

            String nombre = archivo.getName();
            return nombre.equals(NOMBRE_DOCUMENTOS) || nombre.equals(NOMBRE_MUSICA) || nombre.equals(NOMBRE_IMAGENES);
        } catch (IOException | NullPointerException e) {
            return false;
        }
    }

    private static void normalizarCarpetasPrincipales(File carpetaUsuario) {
        File[] carpetas = carpetaUsuario.listFiles(File::isDirectory);
        if (carpetas == null) {
            return;
        }

        for (File carpeta : carpetas) {
            String nombreCorrecto = nombrePrincipalEquivalente(carpeta.getName());
            if (nombreCorrecto == null || carpeta.getName().equals(nombreCorrecto)) {
                continue;
            }

            File destino = new File(carpetaUsuario, nombreCorrecto);
            try {
                fusionarCarpeta(carpeta, destino);
            } catch (IOException e) {
                System.err.println("No se pudo normalizar la carpeta " + carpeta.getName() + ": " + e.getMessage());
            }
        }
    }

    private static String nombrePrincipalEquivalente(String nombre) {
        if (nombre == null) {
            return null;
        }

        String simple = Normalizer.normalize(nombre, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").toLowerCase().trim();
        String compacto = simple.replaceAll("[^a-z0-9]", "");

        if (compacto.equals("misdocumentos")) {
            return NOMBRE_DOCUMENTOS;
        }
        if (compacto.equals("musica") || (compacto.startsWith("m") && compacto.endsWith("sica") && compacto.length() <= 10)) {
            return NOMBRE_MUSICA;
        }
        if (compacto.equals("misimagenes") || (compacto.startsWith("misim") && compacto.endsWith("genes"))) {
            return NOMBRE_IMAGENES;
        }
        return null;
    }

    private static void fusionarCarpeta(File origen, File destino) throws IOException {
        if (!destino.exists() && Files.move(origen.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING) != null) {
            return;
        }

        if (!destino.exists() && !destino.mkdirs()) {
            throw new IOException("No se pudo crear " + destino.getName());
        }

        File[] hijos = origen.listFiles();
        if (hijos != null) {
            for (File hijo : hijos) {
                File nuevoDestino = new File(destino, hijo.getName());
                if (hijo.isDirectory()) {
                    fusionarCarpeta(hijo, nuevoDestino);
                } else if (!nuevoDestino.exists()) {
                    Files.move(hijo.toPath(), nuevoDestino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    File alterno = nombreDisponible(destino, hijo.getName());
                    Files.move(hijo.toPath(), alterno.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        Files.deleteIfExists(origen.toPath());
    }

    private static File nombreDisponible(File carpeta, String nombreOriginal) {
        String base = nombreOriginal;
        String extension = "";
        int punto = nombreOriginal.lastIndexOf('.');
        if (punto > 0) {
            base = nombreOriginal.substring(0, punto);
            extension = nombreOriginal.substring(punto);
        }

        int numero = 1;
        File candidato;
        do {
            candidato = new File(carpeta, base + " (migrado " + numero + ")" + extension);
            numero++;
        } while (candidato.exists());
        return candidato;
    }
    
    public static boolean esCarpetaDeUsuario(File archivo) {
        if (archivo == null || !archivo.isDirectory()) {
            return false;
        }

        try {
            File padre = archivo.getParentFile();

            if (padre == null) {
                return false;
            }

            return padre.getCanonicalFile().equals(getRaizSistema().getCanonicalFile());
        } catch (IOException e) {
            return false;
        }
    }
}
