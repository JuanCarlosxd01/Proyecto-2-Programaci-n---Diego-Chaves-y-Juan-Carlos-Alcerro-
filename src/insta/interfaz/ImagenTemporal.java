/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package insta.interfaz;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
/**
 *
 * @author diego
 */
public final class ImagenTemporal {

    private ImagenTemporal() {
    }

    public static void cargar(
            JLabel destino,
            Callable<byte[]> datos,
            int ancho,
            int alto
    ) {
        Object version = new Object();
        destino.putClientProperty("carga", version);

        new SwingWorker<BufferedImage, Void>() {

            @Override
            protected BufferedImage doInBackground() throws Exception {
                BufferedImage imagen = ImageIO.read(
                        new ByteArrayInputStream(datos.call())
                );

                if (imagen == null) {
                    throw new IOException("Imagen inválida.");
                }

                return imagen;
            }

            @Override
            protected void done() {
                if (destino.getClientProperty("carga") != version) {
                    return;
                }

                try {
                    BufferedImage imagen = get();
                    destino.setText("");

                    destino.setIcon(new Icon() {

                        private double escala() {
                            int disponible = destino.getWidth() > 0
                                    ? destino.getWidth()
                                    : ancho;

                            if (destino.getParent() != null
                                    && destino.getParent().getWidth() > 0) {
                                disponible = Math.min(
                                        disponible,
                                        Math.max(
                                                1,
                                                destino.getParent().getWidth() - 24
                                        )
                                );
                            }

                            return Math.min(
                                    (double) Math.min(ancho, disponible)
                                    / imagen.getWidth(),
                                    (double) alto / imagen.getHeight()
                            );
                        }

                        @Override
                        public int getIconWidth() {
                            return Math.max(
                                    1,
                                    (int) (imagen.getWidth() * escala())
                            );
                        }

                        @Override
                        public int getIconHeight() {
                            return Math.max(
                                    1,
                                    (int) (imagen.getHeight() * escala())
                            );
                        }

                        @Override
                        public void paintIcon(
                                Component componente,
                                Graphics graficos,
                                int x,
                                int y
                        ) {
                            Graphics2D copia = (Graphics2D) graficos.create();

                            copia.setRenderingHint(
                                    RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
                            );

                            copia.drawImage(
                                    imagen,
                                    x,
                                    y,
                                    getIconWidth(),
                                    getIconHeight(),
                                    null
                            );

                            copia.dispose();
                        }
                    });

                    destino.revalidate();
                    destino.repaint();
                } catch (Exception e) {
                    destino.setIcon(null);
                    destino.setText("Imagen no disponible");
                }
            }
        }.execute();
    }
}