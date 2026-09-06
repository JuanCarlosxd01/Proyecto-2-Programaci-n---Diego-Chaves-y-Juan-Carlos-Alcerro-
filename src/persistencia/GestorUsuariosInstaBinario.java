/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package persistencia;

import estructuras.ListaEnlazada;
import excepciones.ArchivoCorruptoException;
import insta.modelo.UsuarioInsta;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
/**
 *
 * @author diego
 */
public class GestorUsuariosInstaBinario {

    private static final String NOMBRE_ARCHIVO = "users.ins";

    private final GestorBinario gestorBinario;
    private final String rutaArchivo;

    public GestorUsuariosInstaBinario() {
        this("INSTA_RAIZ");
    }

    public GestorUsuariosInstaBinario(String carpetaRaiz) {
        Objects.requireNonNull(
                carpetaRaiz,
                "La carpeta raíz no puede ser null."
        );

        if (carpetaRaiz.isBlank()) {
            throw new IllegalArgumentException(
                    "Debes indicar la carpeta raíz de INSTA+."
            );
        }

        this.gestorBinario = new GestorBinario();

        this.rutaArchivo = Path.of(carpetaRaiz)
                .toAbsolutePath()
                .normalize()
                .resolve(NOMBRE_ARCHIVO)
                .toString();
    }

    public synchronized void guardarUsuarios(
            ListaEnlazada<UsuarioInsta> usuarios
    ) throws IOException {

        Objects.requireNonNull(
                usuarios,
                "La lista de usuarios no puede ser null."
        );

        ListaEnlazada<UsuarioInsta> listaValidada;

        try {
            listaValidada = validarLista(usuarios);

        } catch (ArchivoCorruptoException e) {
            throw new IllegalArgumentException(
                    "No se puede guardar la lista de usuarios: "
                    + e.getMessage(),
                    e
            );
        }

        gestorBinario.guardar(
                rutaArchivo,
                listaValidada
        );
    }

    public synchronized ListaEnlazada<UsuarioInsta> cargarUsuarios()
            throws IOException, ArchivoCorruptoException {

     
        ListaEnlazada<?> registros = gestorBinario.leer(
                rutaArchivo,
                ListaEnlazada.class
        );

        return validarLista(registros);
    }

    private ListaEnlazada<UsuarioInsta> validarLista(
            ListaEnlazada<?> registros
    ) throws ArchivoCorruptoException {

        ListaEnlazada<UsuarioInsta> usuarios =
                new ListaEnlazada<>();

        ListaEnlazada<String> usernamesEncontrados =
                new ListaEnlazada<>();

        for (Object registro : registros) {

            if (!(registro instanceof UsuarioInsta)) {
                throw new ArchivoCorruptoException(
                        "La lista contiene un registro "
                        + "que no corresponde a un usuario de INSTA+."
                );
            }

            UsuarioInsta usuario = (UsuarioInsta) registro;

            String username = usuario.getUsername();

            if (username == null || username.isBlank()) {
                throw new ArchivoCorruptoException(
                        "Se encontró una cuenta sin username."
                );
            }

            String usernameNormalizado = username
                    .strip()
                    .toLowerCase(Locale.ROOT);

            if (usernamesEncontrados.contiene(usernameNormalizado)) {
                throw new ArchivoCorruptoException(
                        "Se encontraron cuentas duplicadas "
                        + "para el username: "
                        + usernameNormalizado
                );
            }

            usernamesEncontrados.agregar(usernameNormalizado);
            usuarios.agregar(usuario);
        }

        return usuarios;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }
}