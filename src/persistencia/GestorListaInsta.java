/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package persistencia;
import estructuras.ListaEnlazada;
import excepciones.ArchivoCorruptoException;
import java.io.IOException;
/**
 *
 * @author diego
 */
public class GestorListaInsta<T> {

    protected final RepositorioInsta repositorio;
    private final String archivo;
    private final Class<T> tipo;

    public GestorListaInsta(
            RepositorioInsta repositorio,
            String archivo,
            Class<T> tipo
    ) {
        this.repositorio = repositorio;
        this.archivo = archivo;
        this.tipo = tipo;
    }

    private String ruta(String usuario) {
        if (!usuario.matches("[a-z][a-z0-9_]{2,23}")) {
            throw new IllegalArgumentException("Usuario inválido.");
        }

        return usuario + "/" + archivo;
    }

    public ListaEnlazada<T> cargar(String usuario)
            throws IOException, ArchivoCorruptoException {

        return repositorio.leer(ruta(usuario), tipo);
    }

    public RepositorioInsta.Cambio cambio(
            String usuario,
            ListaEnlazada<T> datos
    ) {
        if (datos.size() > 100000) {
            throw new IllegalArgumentException(
                    "Límite de registros alcanzado."
            );
        }

        for (Object dato : datos) {
            if (!tipo.isInstance(dato)) {
                throw new IllegalArgumentException(
                        "Registro inválido."
                );
            }
        }

        return new RepositorioInsta.Cambio(
                ruta(usuario),
                datos
        );
    }

    public void guardar(
            String usuario,
            ListaEnlazada<T> datos
    ) throws IOException, ArchivoCorruptoException {

        repositorio.guardar(cambio(usuario, datos));
    }
}
