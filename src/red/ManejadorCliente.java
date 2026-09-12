
package red;


import java.io.*;
import java.net.Socket;


public class ManejadorCliente implements Runnable {

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
            conexion.setSoTimeout(30000);
            salida.flush();

            try (ObjectInputStream entrada =
                         FiltroRed.entrada(conexion.getInputStream())) {

                Object dato = entrada.readObject();

                Respuesta respuesta =
                        dato instanceof Solicitud solicitud
                                ? servidor.procesarSolicitud(solicitud)
                                : Respuesta.error(
                                        Respuesta.Codigo.DATOS_INVALIDOS,
                                        "Solicitud inválida."
                                );

                salida.writeObject(respuesta);
                salida.flush();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println(
                    "Conexión de Instagram terminada: "
                            + e.getClass().getSimpleName()
            );

        } finally {
            servidor.finalizarConexion(socket);
        }
    }
}