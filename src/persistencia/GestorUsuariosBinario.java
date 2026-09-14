package persistencia;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import modelo.UsuarioSistema;

public class GestorUsuariosBinario {

    private static final String ARCHIVO_PRINCIPAL = "usuarios.sop";
    private static final String ARCHIVO_LEGACY = "usuarios.dat";

    public boolean guardarUsuarios(ArrayList<UsuarioSistema> usuarios) {
        if (usuarios == null) {
            return false;
        }

        File destino = new File(ARCHIVO_PRINCIPAL);
        File temporal = new File(ARCHIVO_PRINCIPAL + ".tmp");

        try (FileOutputStream archivo = new FileOutputStream(temporal); ObjectOutputStream salida = new ObjectOutputStream(archivo)) {
            salida.writeObject(usuarios);
            salida.flush();
            archivo.getFD().sync();
        } catch (IOException e) {
            temporal.delete();
            System.err.println("Error al guardar los usuarios: " + e.getMessage());
            return false;
        }

        try {
            try {
                Files.move(temporal.toPath(), destino.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            temporal.delete();
            System.err.println("No se pudo reemplazar " + ARCHIVO_PRINCIPAL + ": " + e.getMessage());
            return false;
        }
    }

    public ArrayList<UsuarioSistema> cargarUsuarios() {
        File archivo = localizarArchivo();

        if (archivo == null) {
            return new ArrayList<>();
        }

        ArrayList<UsuarioSistema> usuarios = leerUsuarios(archivo);

        if (usuarios != null && archivo.getName().equals(ARCHIVO_LEGACY)) {
            if (guardarUsuarios(usuarios)) {
                System.out.println("Usuarios migrados de " + ARCHIVO_LEGACY + " a " + ARCHIVO_PRINCIPAL + ".");
            }
        }

        return usuarios;
    }

    private File localizarArchivo() {
        File principal = new File(ARCHIVO_PRINCIPAL);
        if (principal.exists()) {
            return principal;
        }

        File legacy = new File(ARCHIVO_LEGACY);
        if (legacy.exists()) {
            return legacy;
        }

        return null;
    }

    private ArrayList<UsuarioSistema> leerUsuarios(File archivo) {
        if (!archivo.isFile() || archivo.length() == 0) {
            System.err.println(archivo.getName() + " no es un archivo válido.");
            return null;
        }

        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(archivo))) {
            Object objeto = entrada.readObject();

            if (!(objeto instanceof ArrayList<?> lista)) {
                System.err.println("El archivo " + archivo.getName() + " tiene un formato inválido.");
                return null;
            }

            ArrayList<UsuarioSistema> usuarios = new ArrayList<>();

            for (Object elemento : lista) {
                if (!(elemento instanceof UsuarioSistema usuario)) {
                    System.err.println("El archivo " + archivo.getName() + " contiene información inválida.");
                    return null;
                }
                usuarios.add(usuario);
            }

            return usuarios;
        } catch (InvalidClassException e) {
            System.err.println("El archivo " + archivo.getName() + " pertenece a una versión incompatible: " + e.getMessage());
            return null;
        } catch (EOFException e) {
            System.err.println("El archivo " + archivo.getName() + " está incompleto o corrupto.");
            return null;
        } catch (ClassNotFoundException e) {
            System.err.println("No se pudo reconocer la información guardada de usuarios.");
            return null;
        } catch (IOException e) {
            System.err.println("No se pudo leer " + archivo.getName() + ": " + e.getMessage());
            return null;
        }
    }

    public boolean respaldarArchivoCorrupto() {
        File archivo = localizarArchivo();
        if (archivo == null) {
            return true;
        }

        File respaldo = new File(archivo.getName() + ".corrupto-" + System.currentTimeMillis());
        try {
            Files.move(archivo.toPath(), respaldo.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.err.println("Se respaldó el archivo de usuarios dañado como: " + respaldo.getName());
            return true;
        } catch (IOException e) {
            System.err.println("No se pudo respaldar el archivo de usuarios dañado: " + e.getMessage());
            return false;
        }
    }

    public boolean existeArchivoUsuarios() {
        File archivo = localizarArchivo();
        return archivo != null && archivo.isFile();
    }
}
