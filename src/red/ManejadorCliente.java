package red;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ManejadorCliente implements Runnable {

    private final Socket socket;
    private final Servidor servidor;

    public ManejadorCliente(Socket socket, Servidor servidor) {
        this.socket = socket;
        this.servidor = servidor;
    }

    @Override
    public void run() {
        try (Socket conexion = socket) {
            conexion.setSoTimeout(30000);
            PushbackInputStream entradaBase = new PushbackInputStream(conexion.getInputStream(), 2);
            int primero = entradaBase.read();
            if (primero < 0) return;
            int segundo = entradaBase.read();
            if (segundo < 0) return;
            entradaBase.unread(new byte[]{(byte) primero, (byte) segundo});

            if (primero == 0xAC && segundo == 0xED) {
                procesarObjetos(conexion, entradaBase);
            } else {
                procesarTexto(conexion, entradaBase);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Conexión de Instagram terminada: " + e.getClass().getSimpleName());
        } finally {
            servidor.finalizarConexion(socket);
        }
    }

    private void procesarObjetos(Socket conexion, InputStream entradaBase) throws IOException, ClassNotFoundException {
        try (ObjectOutputStream salida = new ObjectOutputStream(conexion.getOutputStream()); ObjectInputStream entrada = FiltroRed.entrada(entradaBase)) {
            salida.flush();
            Object dato = entrada.readObject();
            Respuesta respuesta = dato instanceof Solicitud solicitud ? servidor.procesarSolicitud(solicitud) : Respuesta.error(Respuesta.Codigo.DATOS_INVALIDOS, "Solicitud inválida.");
            salida.writeObject(respuesta);
            salida.flush();
        }
    }

    private void procesarTexto(Socket conexion, InputStream entradaBase) throws IOException {
        BufferedReader entrada = new BufferedReader(new InputStreamReader(entradaBase, StandardCharsets.UTF_8));
        BufferedWriter salida = new BufferedWriter(new OutputStreamWriter(conexion.getOutputStream(), StandardCharsets.UTF_8));
        String comando = entrada.readLine();
        String respuesta = procesarComandoTexto(comando);
        salida.write(respuesta);
        salida.newLine();
        salida.flush();
    }

    private String procesarComandoTexto(String comando) {
        if (comando == null || comando.isBlank()) return "ERROR;Comando vacío";
        String[] partes = comando.split(";", -1);
        if (partes.length == 3 && partes[0].equalsIgnoreCase("LOGIN")) {
            Respuesta respuesta = servidor.procesarSolicitud(new Solicitud(Solicitud.Operacion.INICIAR_SESION, "", null, partes[1], partes[2]));
            return (respuesta.esExitosa() ? "OK;" : "ERROR;") + respuesta.getMensaje();
        }
        return "ERROR;Comando no reconocido";
    }
}
