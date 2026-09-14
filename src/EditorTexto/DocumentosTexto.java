/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EditorTexto;

import excepciones.ArchivoedtException;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import sistema.SeguridadArchivos;
/**
 *
 * @author diego
 */
public final class DocumentosTexto {

    private DocumentosTexto() {
    }

    public static List<TextChunk> abrir(String ruta)
            throws ArchivoedtException {

        try {
            String texto = Files.readString(
                    Path.of(ruta),
                    StandardCharsets.UTF_8
            );

            Path estilos = Path.of(ruta + ".formato.edt");

            if (Files.exists(estilos)) {
                try {
                    List<TextChunk> partes = new EdtReader().abrir(
                            estilos.toString()
                    );

                    StringBuilder contenido = new StringBuilder();

                    for (TextChunk parte : partes) {
                        contenido.append(parte.getTexto());
                    }

                    if (contenido.toString().equals(texto)) {
                        return partes;
                    }
                } catch (ArchivoedtException ignorada) {
                }
            }

            return List.of(new TextChunk(
                    texto,
                    Formato.desde(
                            "Segoe UI",
                            12,
                            false,
                            false,
                            false,
                            false,
                            Color.BLACK
                    )
            ));
        } catch (Exception e) {
            throw new ArchivoedtException(
                    "No se pudo abrir el documento: " + e.getMessage(),
                    e
            );
        }
    }

    public static void guardar(String ruta, List<TextChunk> partes)
            throws ArchivoedtException {

        Path temporal = null;
        Path formato = null;

        try {
            Path destino = Path.of(ruta).toAbsolutePath();

            temporal = Files.createTempFile(
                    destino.getParent(),
                    "texto-",
                    ".tmp"
            );

            formato = Files.createTempFile(
                    destino.getParent(),
                    "formato-",
                    ".edt"
            );

            StringBuilder texto = new StringBuilder();

            for (TextChunk parte : partes) {
                texto.append(parte.getTexto());
            }

            Files.writeString(
                    temporal,
                    texto,
                    StandardCharsets.UTF_8
            );

            new EdtWriter().guardar(formato.toString(), partes);

            mover(formato, Path.of(destino + ".formato.edt"));
            mover(temporal, destino);
        } catch (Exception e) {
            throw new ArchivoedtException(
                    "No se pudo guardar el documento: " + e.getMessage(),
                    e
            );
        } finally {
            try {
                if (temporal != null) {
                    Files.deleteIfExists(temporal);
                }
            } catch (Exception ignorada) {
            }

            try {
                if (formato != null) {
                    Files.deleteIfExists(formato);
                }
            } catch (Exception ignorada) {
            }
        }
    }

    public static void copiarFormato(File origen, File destino)
            throws IOException {

        if (!origen.getName().toLowerCase().endsWith(".txt")) {
            return;
        }

        Path formato = Path.of(origen + ".formato.edt");
        Path salida = Path.of(destino + ".formato.edt");

        if (Files.exists(formato)) {
            SeguridadArchivos.importar(formato.toFile());
            SeguridadArchivos.permitido(salida.toFile());

            Files.copy(
                    formato,
                    salida,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    public static void mover(File origen, File destino)
            throws IOException {

        SeguridadArchivos.permitido(origen);
        SeguridadArchivos.permitido(destino);

        Path formato = Path.of(origen + ".formato.edt");
        Path salida = Path.of(destino + ".formato.edt");

        boolean tiene = origen.getName().toLowerCase().endsWith(".txt")
                && Files.exists(formato);

        if (tiene) {
            SeguridadArchivos.permitido(formato.toFile());
            SeguridadArchivos.permitido(salida.toFile());
            Files.move(formato, salida);
        }

        try {
            Files.move(origen.toPath(), destino.toPath());
        } catch (IOException e) {
            if (tiene) {
                Files.move(salida, formato);
            }

            throw e;
        }
    }

    private static void mover(Path origen, Path destino)
            throws IOException {

        try {
            Files.move(
                    origen,
                    destino,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(
                    origen,
                    destino,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }
}
