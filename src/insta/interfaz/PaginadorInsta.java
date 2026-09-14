package insta.interfaz;

import estructuras.ListaEnlazada;
import insta.modelo.Publicacion;
import insta.modelo.Sticker;
import java.io.IOException;
import red.Cliente;
import red.Respuesta;

final class PaginadorInsta {

    private PaginadorInsta() {
    }

    interface Consulta {
        Respuesta ejecutar(int desde) throws IOException;
    }

    static ListaEnlazada<Publicacion> publicaciones(Consulta consulta) throws IOException {
        ListaEnlazada<Publicacion> resultado = new ListaEnlazada<>();
        int desde = 0;
        int total;
        do {
            Respuesta respuesta = consulta.ejecutar(desde);
            validar(respuesta);
            ListaEnlazada<Publicacion> pagina = respuesta.getPublicaciones();
            for (Publicacion publicacion : pagina) resultado.agregarUnico(publicacion);
            total = respuesta.getTotal();
            desde += pagina.size();
            if (pagina.isEmpty()) break;
        } while (desde < total);
        return resultado;
    }

    static ListaEnlazada<Sticker> stickers(Cliente cliente) throws IOException {
        ListaEnlazada<Sticker> resultado = new ListaEnlazada<>();
        int desde = 0;
        int total;
        do {
            Respuesta respuesta = cliente.stickers(desde);
            validar(respuesta);
            ListaEnlazada<Sticker> pagina = respuesta.getStickers();
            for (Sticker sticker : pagina) resultado.agregarUnico(sticker);
            total = respuesta.getTotal();
            desde += pagina.size();
            if (pagina.isEmpty()) break;
        } while (desde < total);
        return resultado;
    }

    static ListaEnlazada<String> carpetas(Cliente cliente) throws IOException {
        ListaEnlazada<String> resultado = new ListaEnlazada<>();
        int desde = 0;
        int total;
        do {
            Respuesta respuesta = cliente.carpetas(desde);
            validar(respuesta);
            ListaEnlazada<String> pagina = respuesta.getCarpetas();
            for (String carpeta : pagina) resultado.agregarUnico(carpeta);
            total = respuesta.getTotal();
            desde += pagina.size();
            if (pagina.isEmpty()) break;
        } while (desde < total);
        return resultado;
    }


    static ListaEnlazada<Respuesta.DatosUsuario> usuarios(Consulta consulta) throws IOException {
        ListaEnlazada<Respuesta.DatosUsuario> resultado = new ListaEnlazada<>();
        int desde = 0;
        int total;
        do {
            Respuesta respuesta = consulta.ejecutar(desde);
            validar(respuesta);
            ListaEnlazada<Respuesta.DatosUsuario> pagina = respuesta.getUsuarios();
            for (Respuesta.DatosUsuario usuario : pagina) resultado.agregarUnico(usuario);
            total = respuesta.getTotal();
            desde += pagina.size();
            if (pagina.isEmpty()) break;
        } while (desde < total);
        return resultado;
    }
    static void validar(Respuesta respuesta) throws IOException {
        if (respuesta == null) throw new IOException("Respuesta vacía del servidor.");
        if (!respuesta.esExitosa()) throw new IOException(respuesta.getMensaje());
    }
}
