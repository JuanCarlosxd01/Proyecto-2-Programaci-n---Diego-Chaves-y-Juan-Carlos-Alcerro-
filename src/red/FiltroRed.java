/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package red;
import estructuras.ListaEnlazada;
import insta.modelo.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.Set;
/**
 *
 * @author diego
 */
final class FiltroRed {

    private static final long LIMITE = 6L * 1024 * 1024;

    private static final Set<Class<?>> CLASES = Set.of(
            Solicitud.class,
            Solicitud.Operacion.class,
            Respuesta.class,
            Respuesta.Codigo.class,
            Respuesta.DatosUsuario.class,
            ListaEnlazada.class,
            Publicacion.class,
            Publicacion.Tipo.class,
            Mensaje.class,
            Mensaje.Tipo.class,
            Sticker.class,
            String.class,
            Enum.class,
            LocalDateTime.class,
            byte[].class,
            String[].class
    );

    static ObjectInputStream entrada(InputStream origen)
            throws IOException {

        ObjectInputStream entrada = new ObjectInputStream(
                new FilterInputStream(origen) {

                    long restantes = LIMITE;

                    @Override
                    public int read() throws IOException {
                        if (restantes <= 0) {
                            throw new IOException(
                                    "Paquete demasiado grande."
                            );
                        }

                        int valor = in.read();

                        if (valor >= 0) {
                            restantes--;
                        }

                        return valor;
                    }

                    @Override
                    public int read(
                            byte[] buffer,
                            int offset,
                            int longitud
                    ) throws IOException {

                        if (longitud == 0) {
                            return 0;
                        }

                        if (restantes <= 0) {
                            throw new IOException(
                                    "Paquete demasiado grande."
                            );
                        }

                        int leidos = in.read(
                                buffer,
                                offset,
                                (int) Math.min(longitud, restantes)
                        );

                        if (leidos > 0) {
                            restantes -= leidos;
                        }

                        return leidos;
                    }

                    @Override
                    public long skip(long cantidad)
                            throws IOException {

                        long omitidos = in.skip(
                                Math.min(
                                        Math.max(0, cantidad),
                                        restantes
                                )
                        );

                        restantes -= omitidos;
                        return omitidos;
                    }
                }
        );

        entrada.setObjectInputFilter(info -> {
            if (info.depth() > 24
                    || info.references() > 20000
                    || info.streamBytes() > LIMITE
                    || info.arrayLength() > 4L * 1024 * 1024) {
                return ObjectInputFilter.Status.REJECTED;
            }

            Class<?> clase = info.serialClass();

            if (clase == null) {
                return ObjectInputFilter.Status.UNDECIDED;
            }

            if (clase == String[].class
                    && info.arrayLength() > 20) {
                return ObjectInputFilter.Status.REJECTED;
            }

            return CLASES.contains(clase)
                    || clase.isPrimitive()
                    || clase.getName().equals("java.time.Ser")
                    ? ObjectInputFilter.Status.ALLOWED
                    : ObjectInputFilter.Status.REJECTED;
        });

        return entrada;
    }
}