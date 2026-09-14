package interfaz;

import java.awt.*;
import javax.swing.*;

public final class IconosWindows {

    private IconosWindows() {
    }

    public static Icon crear(String tipo, int tamano) {
        return new Icon() {
            @Override
            public int getIconWidth() {
                return tamano;
            }

            @Override
            public int getIconHeight() {
                return tamano;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int s = tamano;
                switch (tipo.toLowerCase()) {
                    case "equipo" -> dibujarEquipo(g2, x, y, s);
                    case "archivos" -> dibujarCarpeta(g2, x, y, s);
                    case "word" -> dibujarWord(g2, x, y, s);
                    case "musica" -> dibujarMusica(g2, x, y, s);
                    case "cmd" -> dibujarCmd(g2, x, y, s);
                    case "insta" -> dibujarInsta(g2, x, y, s);
                    case "imagenes" -> dibujarImagen(g2, x, y, s);
                    case "usuarios" -> dibujarUsuarios(g2, x, y, s);
                    case "configuracion" -> dibujarConfiguracion(g2, x, y, s);
                    case "pokemon" -> dibujarPokemon(g2, x, y, s);
                    case "windows" -> dibujarWindows(g2, x, y, s);
                    default -> dibujarApp(g2, x, y, s);
                }
                g2.dispose();
            }
        };
    }

    private static void dibujarEquipo(Graphics2D g, int x, int y, int s) {
        g.setColor(new Color(50, 135, 230));
        g.fillRoundRect(x + 4, y + 6, s - 8, (int) (s * .62), 5, 5);
        g.setColor(new Color(220, 235, 250));
        g.fillRect(x + 8, y + 10, s - 16, (int) (s * .48));
        g.setColor(new Color(180, 185, 190));
        g.fillRect(x + s / 2 - 3, y + (int) (s * .69), 6, 8);
        g.fillRoundRect(x + s / 2 - 12, y + (int) (s * .82), 24, 4, 4, 4);
    }

    private static void dibujarCarpeta(Graphics2D g, int x, int y, int s) {
        g.setColor(new Color(252, 193, 55));
        g.fillRoundRect(x + 3, y + 12, s - 6, s - 18, 5, 5);
        g.setColor(new Color(255, 215, 95));
        g.fillRoundRect(x + 5, y + 7, s / 2, 12, 4, 4);
        g.fillRoundRect(x + 3, y + 17, s - 6, s - 23, 5, 5);
    }

    private static void dibujarWord(Graphics2D g, int x, int y, int s) {
        g.setColor(new Color(28, 95, 178));
        g.fillRoundRect(x + 4, y + 4, s - 8, s - 8, 6, 6);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, (int) (s * .58)));
        FontMetrics fm = g.getFontMetrics();
        String t = "W";
        g.drawString(t, x + (s - fm.stringWidth(t)) / 2, y + (s + fm.getAscent() - fm.getDescent()) / 2);
    }

    private static void dibujarMusica(Graphics2D g, int x, int y, int s) {
        int margen = Math.max(3, s / 14);
        g.setColor(new Color(29, 185, 84));
        g.fillOval(x + margen, y + margen, s - margen * 2, s - margen * 2);

        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(Math.max(2f, s / 12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int stemIzqX = x + (int) (s * 0.43);
        int stemDerX = x + (int) (s * 0.67);
        int arribaY = y + (int) (s * 0.28);
        int abajoY = y + (int) (s * 0.67);
        int cabezaW = Math.max(9, (int) (s * 0.22));
        int cabezaH = Math.max(7, (int) (s * 0.16));

        g.drawLine(stemIzqX, arribaY + 4, stemIzqX, abajoY);
        g.drawLine(stemDerX, arribaY, stemDerX, abajoY - 4);
        g.drawLine(stemIzqX, arribaY + 4, stemDerX, arribaY);

        g.fillOval(stemIzqX - cabezaW + 2, abajoY - cabezaH / 2, cabezaW, cabezaH);
        g.fillOval(stemDerX - cabezaW + 2, abajoY - cabezaH / 2 - 4, cabezaW, cabezaH);
    }

    private static void dibujarCmd(Graphics2D g, int x, int y, int s) {
        g.setColor(new Color(25, 25, 25));
        g.fillRoundRect(x + 3, y + 5, s - 6, s - 10, 5, 5);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, Math.max(12, s / 3)));
        g.drawString(">_", x + 8, y + s / 2 + 6);
    }

    private static void dibujarInsta(Graphics2D g, int x, int y, int s) {
        GradientPaint gp = new GradientPaint(x, y, new Color(125, 54, 220), x + s, y + s, new Color(250, 80, 100));
        g.setPaint(gp);
        g.fillRoundRect(x + 4, y + 4, s - 8, s - 8, 12, 12);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(x + 11, y + 11, s - 22, s - 22, 9, 9);
        g.drawOval(x + s / 2 - 7, y + s / 2 - 7, 14, 14);
        g.fillOval(x + s - 19, y + 13, 5, 5);
    }

    private static void dibujarImagen(Graphics2D g, int x, int y, int s) {
        g.setColor(new Color(245, 245, 245));
        g.fillRoundRect(x + 3, y + 5, s - 6, s - 10, 5, 5);
        g.setColor(new Color(76, 175, 80));
        Polygon p = new Polygon();
        p.addPoint(x + 7, y + s - 10);
        p.addPoint(x + s / 2, y + s / 2);
        p.addPoint(x + s - 7, y + s - 10);
        g.fillPolygon(p);
        g.setColor(new Color(255, 193, 7));
        g.fillOval(x + s - 18, y + 11, 8, 8);
        g.setColor(new Color(180, 180, 180));
        g.drawRoundRect(x + 3, y + 5, s - 6, s - 10, 5, 5);
    }

    private static void dibujarUsuarios(Graphics2D g, int x, int y, int s) {
        g.setColor(new Color(65, 145, 245));
        g.fillOval(x + s / 2 - 8, y + 6, 16, 16);
        g.fillRoundRect(x + s / 2 - 15, y + 24, 30, 20, 12, 12);
        g.setColor(new Color(130, 185, 250));
        g.fillOval(x + 5, y + 12, 12, 12);
        g.fillRoundRect(x + 2, y + 27, 19, 15, 9, 9);
    }

    private static void dibujarConfiguracion(Graphics2D g, int x, int y, int s) {
        g.setColor(new Color(120, 125, 130));
        g.fillOval(x + 5, y + 5, s - 10, s - 10);
        g.setColor(new Color(225, 225, 225));
        g.fillOval(x + s / 2 - 8, y + s / 2 - 8, 16, 16);
        g.setColor(new Color(90, 95, 100));
        g.fillOval(x + s / 2 - 4, y + s / 2 - 4, 8, 8);
    }


    private static void dibujarWindows(Graphics2D g, int x, int y, int s) {
        g.setColor(new Color(0, 120, 215));
        int margen = Math.max(2, s / 10);
        int gap = Math.max(1, s / 18);
        int w = (s - margen * 2 - gap) / 2;
        int h = (s - margen * 2 - gap) / 2;
        g.fillRect(x + margen, y + margen, w, h);
        g.fillRect(x + margen + w + gap, y + margen, w, h);
        g.fillRect(x + margen, y + margen + h + gap, w, h);
        g.fillRect(x + margen + w + gap, y + margen + h + gap, w, h);
    }

    private static void dibujarPokemon(Graphics2D g, int x, int y, int s) {
        int d = s - 8;
        int ox = x + 4;
        int oy = y + 4;
        g.setColor(new Color(220, 45, 45));
        g.fillArc(ox, oy, d, d, 0, 180);
        g.setColor(Color.WHITE);
        g.fillArc(ox, oy, d, d, 180, 180);
        g.setColor(new Color(45, 45, 45));
        g.setStroke(new BasicStroke(Math.max(2f, s / 12f)));
        g.drawOval(ox, oy, d, d);
        g.drawLine(ox + 1, oy + d / 2, ox + d - 1, oy + d / 2);
        int c = Math.max(10, s / 4);
        g.setColor(Color.WHITE);
        g.fillOval(x + s / 2 - c / 2, y + s / 2 - c / 2, c, c);
        g.setColor(new Color(45, 45, 45));
        g.drawOval(x + s / 2 - c / 2, y + s / 2 - c / 2, c, c);
    }

    private static void dibujarApp(Graphics2D g, int x, int y, int s) {
        g.setColor(new Color(0, 120, 215));
        g.fillRoundRect(x + 5, y + 5, s - 10, s - 10, 8, 8);
    }
}
