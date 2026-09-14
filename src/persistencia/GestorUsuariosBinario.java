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

    private static final String ARCHIVO_USUARIOS = "usuarios.sop";

    public boolean guardarUsuarios(ArrayList<UsuarioSistema> usuarios) {
        if (usuarios == null) {
            return false;
        }

        File destino = new File(ARCHIVO_USUARIOS);
        File temporal = new File(ARCHIVO_USUARIOS + ".tmp");

        try (FileOutputStream archivo = new FileOutputStream(temporal);
             ObjectOutputStream salida = new ObjectOutputStream(archivo)) {

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
                Files.move(
                        temporal.toPath(),
                        destino.toPath(),
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                );

            } catch (AtomicMoveNotSupportedException e) {
                Files.move(
                        temporal.toPath(),
                        destino.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            return true;

        } catch (IOException e) {
            temporal.delete();

            System.err.println("No se pudo reemplazar " + ARCHIVO_USUARIOS + ": " + e.getMessage());

            return false;
        }
    }

    public ArrayList<UsuarioSistema> cargarUsuarios() {
        File archivo = new File(ARCHIVO_USUARIOS);

        if (!archivo.exists()) {
            return new ArrayList<>();
        }

        return leerUsuarios(archivo);
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
                    System.err.println("El archivo contiene información de usuarios inválida.");
                    return null;
                }

                usuarios.add(usuario);
            }

            return usuarios;

        } catch (InvalidClassException e) {
            System.err.println("El archivo de usuarios pertenece a una versión incompatible: " + e.getMessage());
            return null;

        } catch (EOFException e) {
            System.err.println("El archivo de usuarios está incompleto o corrupto.");
            return null;

        } catch (ClassNotFoundException e) {
            System.err.println("No se pudo reconocer la información guardada de usuarios.");
            return null;

        } catch (IOException e) {
            System.err.println("No se pudo leer " + ARCHIVO_USUARIOS + ": " + e.getMessage());
            return null;
        }
    }

    public boolean respaldarArchivoCorrupto() {
        File archivo = new File(ARCHIVO_USUARIOS);

        if (!archivo.exists()) {
            return true;
        }

        File respaldo = new File(ARCHIVO_USUARIOS + ".corrupto-" + System.currentTimeMillis());

        try {
            Files.move(
                    archivo.toPath(),
                    respaldo.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );

            System.err.println("Archivo dañado respaldado como: " + respaldo.getName());

            return true;

        } catch (IOException e) {
            System.err.println("No se pudo respaldar el archivo de usuarios dañado: " + e.getMessage());

            return false;
        }
    }

    public boolean existeArchivoUsuarios() {
        File archivo = new File(ARCHIVO_USUARIOS);

        return archivo.exists() && archivo.isFile();
    }
}