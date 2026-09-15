package red;

public class ServidorPrincipal {

    public static void main(String[] args) {
        try (Servidor servidor = new Servidor()) {
            System.out.println("Servidor INSTA+ iniciado en puerto " + servidor.getPuerto());
            servidor.iniciar();
        } catch (Exception e) {
            System.err.println("No se pudo iniciar el servidor INSTA+: " + e.getMessage());
        }
    }
}
