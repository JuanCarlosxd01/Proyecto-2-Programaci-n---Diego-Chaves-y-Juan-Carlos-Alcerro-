package insta.interfaz;

import interfaz.DialogosWindows;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import sistema.RutasSistema;

public final class SelectorArchivosZ {

    private SelectorArchivosZ() {
    }

    public static File seleccionarImagen(Component padre, String titulo) {
        return seleccionar(padre, titulo, new FileNameExtensionFilter("Imágenes PNG, JPG y JPEG", "png", "jpg", "jpeg"));
    }

    public static File seleccionarSticker(Component padre, String titulo) {
        return seleccionar(padre, titulo, new FileNameExtensionFilter("Sticker PNG o JPG", "png", "jpg"));
    }

    private static File seleccionar(Component padre, String titulo, FileNameExtensionFilter filtro) {
        File raiz = RutasSistema.getRaizExplorador();
        if (raiz == null || !raiz.exists()) {
            DialogosWindows.showMessageDialog(padre, "No hay un disco Z: disponible para la sesión actual.", titulo, JOptionPane.WARNING_MESSAGE);
            return null;
        }

        JFileChooser selector = new JFileChooser(raiz);
        selector.setDialogTitle(titulo + " - Disco Z:");
        selector.setFileFilter(filtro);

        while (selector.showOpenDialog(padre) == JFileChooser.APPROVE_OPTION) {
            File archivo = selector.getSelectedFile();
            if (estaDentroDeRaiz(archivo, raiz)) return archivo;
            DialogosWindows.showMessageDialog(padre, "INSTA+ solo puede utilizar archivos almacenados dentro del disco Z:.", "Ubicación no permitida", JOptionPane.WARNING_MESSAGE);
            selector.setCurrentDirectory(raiz);
        }

        return null;
    }

    private static boolean estaDentroDeRaiz(File archivo, File raiz) {
        if (archivo == null || raiz == null) return false;
        try {
            return archivo.getCanonicalFile().toPath().startsWith(raiz.getCanonicalFile().toPath());
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }
}
