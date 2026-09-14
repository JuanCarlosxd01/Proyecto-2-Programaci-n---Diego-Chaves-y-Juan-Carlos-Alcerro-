/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistema;

import java.io.File;
import modelo.UsuarioSistema;
/**
 *
 * @author diego
 */
public final class RutasSistema {

    public static final String NOMBRE_RAIZ = "Z";
    public static final String NOMBRE_DOCUMENTOS = "Mis Documentos";
    public static final String NOMBRE_MUSICA = "Música";
    public static final String NOMBRE_IMAGENES = "Mis Imágenes";

    private RutasSistema() {
    }

    public static File getRaizSistema() {
        return new File(NOMBRE_RAIZ);
    }

    public static File getCarpetaUsuario(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        SeguridadArchivos.nombre(username.trim());
        return new File(getRaizSistema(), username.trim());
    }

    public static File getCarpetaUsuario(UsuarioSistema usuario) {
        if (usuario == null) {
            return null;
        }

        return getCarpetaUsuario(usuario.getUsername());
    }

    public static File getCarpetaUsuarioActual() {
        return getCarpetaUsuario(Sesion.getUsuarioActual());
    }

    public static File getDocumentosUsuarioActual() {
        File carpetaUsuario = getCarpetaUsuarioActual();

        if (carpetaUsuario == null) {
            return null;
        }

        return new File(carpetaUsuario, NOMBRE_DOCUMENTOS);
    }

    public static File getMusicaUsuarioActual() {
        File carpetaUsuario = getCarpetaUsuarioActual();

        if (carpetaUsuario == null) {
            return null;
        }

        return new File(carpetaUsuario, NOMBRE_MUSICA);
    }

    public static File getImagenesUsuarioActual() {
        File carpetaUsuario = getCarpetaUsuarioActual();

        if (carpetaUsuario == null) {
            return null;
        }

        return new File(carpetaUsuario, NOMBRE_IMAGENES);
    }

    public static File getDocumentos(UsuarioSistema usuario) {
        File carpetaUsuario = getCarpetaUsuario(usuario);

        if (carpetaUsuario == null) {
            return null;
        }

        return new File(carpetaUsuario, NOMBRE_DOCUMENTOS);
    }

    public static File getMusica(UsuarioSistema usuario) {
        File carpetaUsuario = getCarpetaUsuario(usuario);

        if (carpetaUsuario == null) {
            return null;
        }

        return new File(carpetaUsuario, NOMBRE_MUSICA);
    }

    public static File getImagenes(UsuarioSistema usuario) {
        File carpetaUsuario = getCarpetaUsuario(usuario);

        if (carpetaUsuario == null) {
            return null;
        }

        return new File(carpetaUsuario, NOMBRE_IMAGENES);
    }

    public static File getRaizExplorador() {
        UsuarioSistema usuario = Sesion.getUsuarioActual();

        if (usuario == null) {
            return getRaizSistema();
        }

        if (usuario.esAdministrador()) {
            return getRaizSistema();
        }

        return getCarpetaUsuario(usuario);
    }

    public static void crearEstructuraUsuario(UsuarioSistema usuario) {
        File carpetaUsuario = getCarpetaUsuario(usuario);

        if (carpetaUsuario == null) {
            return;
        }

        File raiz = getRaizSistema();

        if (!raiz.exists()) {
            raiz.mkdirs();
        }

        if (!carpetaUsuario.exists()) {
            carpetaUsuario.mkdirs();
        }

        File documentos = getDocumentos(usuario);
        File musica = getMusica(usuario);
        File imagenes = getImagenes(usuario);

        if (!documentos.exists()) {
            documentos.mkdirs();
        }

        if (!musica.exists()) {
            musica.mkdirs();
        }

        if (!imagenes.exists()) {
            imagenes.mkdirs();
        }
    }
}