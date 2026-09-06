
package red;


import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;
import java.net.SocketTimeoutException;

public class ManejadorCliente implements Runnable {

    private static final int TIEMPO_ESPERA_MS = 15_000;


    private static final long MAX_BYTES_SOLICITUD =
            64 * 1024;

    private final Socket socket;
    private final Servidor servidor;

    public ManejadorCliente(
            Socket socket,
            Servidor servidor
    ) {
        this.socket = socket;
        this.servidor = servidor;
    }

    @Override
    public void run() {
        try (
                Socket conexion = socket;

                ObjectOutputStream salida =
                        new ObjectOutputStream(
                                conexion.getOutputStream()
                        )
        ) {
            conexion.setSoTimeout(TIEMPO_ESPERA_MS);

   
            salida.flush();

            InputStream entradaLimitada =
                    new EntradaLimitada(
                            conexion.getInputStream(),
                            MAX_BYTES_SOLICITUD
                    );

            ObjectInputStream entrada =
                    new ObjectInputStream(entradaLimitada);

            entrada.setObjectInputFilter(
                    ManejadorCliente::filtrarObjeto
            );

            Object recibido = entrada.readObject();

            Respuesta respuesta;

            if (recibido instanceof Solicitud) {
                respuesta = servidor.procesarSolicitud(
                        (Solicitud) recibido
                );

            } else {
                respuesta = Respuesta.error(
                        Respuesta.Codigo.DATOS_INVALIDOS,
                        "Se esperaba una solicitud de INSTA+."
                );
            }

            salida.writeObject(respuesta);
            salida.flush();

       

        } catch (SocketTimeoutException e) {
            System.err.println(
                    "Conexión cerrada por tiempo de espera."
            );

        } catch (ClassNotFoundException e) {
            System.err.println(
                    "Se recibió una clase desconocida."
            );

        } catch (IOException e) {
            System.err.println(
                    "La conexión terminó o la solicitud "
                    + "no pudo procesarse: "
                    + e.getClass().getSimpleName()
            );

        } finally {
            servidor.finalizarConexion(socket);
        }
    }


    private static ObjectInputFilter.Status filtrarObjeto(
            ObjectInputFilter.FilterInfo informacion
    ) {
        if (informacion.depth() > 10
                || informacion.references() > 100
                || informacion.streamBytes() > MAX_BYTES_SOLICITUD) {

            return ObjectInputFilter.Status.REJECTED;
        }

        Class<?> clase = informacion.serialClass();

        if (clase == null) {
            return ObjectInputFilter.Status.UNDECIDED;
        }

        boolean permitida =
                clase == Solicitud.class
                || clase == Solicitud.Operacion.class
                || clase == String.class
                || clase == Enum.class
                || clase.isPrimitive();

        return permitida
                ? ObjectInputFilter.Status.ALLOWED
                : ObjectInputFilter.Status.REJECTED;
    }


    private static final class EntradaLimitada
            extends FilterInputStream {

        private long restantes;

        private EntradaLimitada(
                InputStream entrada,
                long limite
        ) {
            super(entrada);
            this.restantes = limite;
        }

        @Override
        public int read() throws IOException {
            comprobarLimite();

            int valor = in.read();

            if (valor != -1) {
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

            comprobarLimite();

            int permitido = (int) Math.min(
                    longitud,
                    restantes
            );

            int leidos = in.read(
                    buffer,
                    offset,
                    permitido
            );

            if (leidos > 0) {
                restantes -= leidos;
            }

            return leidos;
        }

        @Override
        public long skip(long cantidad) throws IOException {
            if (cantidad <= 0) {
                return 0;
            }

            comprobarLimite();

            long omitidos = in.skip(
                    Math.min(cantidad, restantes)
            );

            restantes -= omitidos;

            return omitidos;
        }

        private void comprobarLimite() throws IOException {
            if (restantes <= 0) {
                throw new IOException(
                        "La solicitud supera el tamaño permitido."
                );
            }
        }
    }
}
