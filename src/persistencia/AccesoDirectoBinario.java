package persistencia;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public final class AccesoDirectoBinario {

    private AccesoDirectoBinario() {
    }

    public static void escribirEntero(File archivo, long posicion, int valor) throws IOException {
        if (archivo == null || posicion < 0) throw new IllegalArgumentException("Archivo o posición inválida.");
        try (RandomAccessFile acceso = new RandomAccessFile(archivo, "rw")) {
            acceso.seek(posicion);
            acceso.writeInt(valor);
        }
    }

    public static int leerEntero(File archivo, long posicion) throws IOException {
        if (archivo == null || posicion < 0) throw new IllegalArgumentException("Archivo o posición inválida.");
        try (RandomAccessFile acceso = new RandomAccessFile(archivo, "r")) {
            acceso.seek(posicion);
            return acceso.readInt();
        }
    }

    public static void escribirRegistroFijo(File archivo, int indice, int tamanoRegistro, byte[] datos) throws IOException {
        if (archivo == null || indice < 0 || tamanoRegistro <= 0 || datos == null || datos.length > tamanoRegistro) throw new IllegalArgumentException("Registro inválido.");
        try (RandomAccessFile acceso = new RandomAccessFile(archivo, "rw")) {
            acceso.seek((long) indice * tamanoRegistro);
            acceso.write(datos);
            for (int i = datos.length; i < tamanoRegistro; i++) acceso.writeByte(0);
        }
    }
}
