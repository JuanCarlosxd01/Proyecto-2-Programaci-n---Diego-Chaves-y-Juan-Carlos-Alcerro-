/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package insta.servicio;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
/**
 *
 * @author diego
 */
public final class ImagenesInsta {

    public static final int MAX_BYTES = 4 * 1024 * 1024;

    private ImagenesInsta() {
    }

    public static byte[] validar(byte[] bytes) throws IOException {
        if (bytes == null
                || bytes.length == 0
                || bytes.length > MAX_BYTES) {
            throw new IllegalArgumentException(
                    "La imagen debe ocupar entre 1 byte y 4 MB."
            );
        }

        try (ImageInputStream entrada =
                     ImageIO.createImageInputStream(
                             new ByteArrayInputStream(bytes)
                     )) {

            var lectores = ImageIO.getImageReaders(entrada);

            if (!lectores.hasNext()) {
                throw new IllegalArgumentException(
                        "El archivo no es una imagen."
                );
            }

            ImageReader lector = lectores.next();

            try {
                String formato = lector.getFormatName();

                if (!formato.equalsIgnoreCase("png")
                        && !formato.equalsIgnoreCase("jpeg")) {
                    throw new IllegalArgumentException(
                            "Usa una imagen PNG o JPG."
                    );
                }

                lector.setInput(entrada);

                int ancho = lector.getWidth(0);
                int alto = lector.getHeight(0);

                if (ancho < 1
                        || alto < 1
                        || ancho > 4096
                        || alto > 4096
                        || (long) ancho * alto > 16000000) {
                    throw new IllegalArgumentException(
                            "La imagen supera las dimensiones permitidas."
                    );
                }

                return png(lector.read(0));

            } finally {
                lector.dispose();
            }
        }
    }

    private static byte[] png(BufferedImage imagen)
            throws IOException {

        ByteArrayOutputStream salida = new ByteArrayOutputStream();

        if (!ImageIO.write(imagen, "png", salida)) {
            throw new IOException(
                    "No se pudo convertir la imagen."
            );
        }

        if (salida.size() > MAX_BYTES) {
            throw new IllegalArgumentException(
                    "La imagen convertida supera 4 MB."
            );
        }

        return salida.toByteArray();
    }

    public static byte[] dibujar(String texto, int color)
            throws IOException {

        BufferedImage imagen = new BufferedImage(
                256,
                256,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D graphics = imagen.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        graphics.setColor(new Color(color));
        graphics.fillRoundRect(8, 8, 240, 240, 55, 55);

        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 26));

        graphics.drawString(
                texto,
                (256 - graphics.getFontMetrics().stringWidth(texto)) / 2,
                139
        );

        graphics.dispose();

        return png(imagen);
    }
}