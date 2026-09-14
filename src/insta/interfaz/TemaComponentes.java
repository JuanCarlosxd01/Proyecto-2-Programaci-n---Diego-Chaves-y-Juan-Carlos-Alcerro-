package insta.interfaz;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.text.JTextComponent;
import javax.swing.plaf.basic.BasicButtonUI;

/** Ajustes comunes para evitar controles ilegibles al alternar el tema. */
final class TemaComponentes {

    private TemaComponentes() { }

    static void corregirContraste(Component componente) {
        if (componente == null) return;

        if (componente instanceof AbstractButton boton) {
            boton.setUI(new BasicButtonUI());
            boton.setOpaque(true);
            boton.setContentAreaFilled(true);
            boton.setBorderPainted(true);
            boton.setFocusPainted(false);
            if (esClaro(boton.getBackground()) && esClaro(boton.getForeground())) {
                boton.setForeground(TemaInsta.oscuro ? TemaInsta.TEXTO : new Color(20, 20, 20));
            }
        }

        if (componente instanceof JTextComponent texto) {
            if (TemaInsta.oscuro && esClaro(texto.getBackground())) texto.setBackground(TemaInsta.INPUT);
            texto.setForeground(TemaInsta.TEXTO);
            texto.setCaretColor(TemaInsta.TEXTO);
        }

        if (componente instanceof JComboBox<?> combo) {
            combo.setBackground(TemaInsta.INPUT);
            combo.setForeground(TemaInsta.TEXTO);
        }

        if (componente instanceof JList<?> lista) {
            if (TemaInsta.oscuro && esClaro(lista.getBackground())) lista.setBackground(TemaInsta.FONDO_SECUNDARIO);
            lista.setForeground(TemaInsta.TEXTO);
            lista.setSelectionBackground(TemaInsta.INPUT);
            lista.setSelectionForeground(TemaInsta.TEXTO);
        }

        if (componente instanceof JMenuItem item) {
            item.setOpaque(true);
            item.setBackground(TemaInsta.TARJETA);
            item.setForeground(TemaInsta.TEXTO);
        }

        if (componente instanceof JPopupMenu popup) {
            popup.setOpaque(true);
            popup.setBackground(TemaInsta.TARJETA);
            popup.setBorder(BorderFactory.createLineBorder(TemaInsta.BORDE));
        }

        if (componente instanceof JScrollPane scroll) {
            scroll.getViewport().setBackground(TemaInsta.FONDO_SECUNDARIO);
        }

        if (componente instanceof JViewport viewport && TemaInsta.oscuro && esClaro(viewport.getBackground())) {
            viewport.setBackground(TemaInsta.FONDO_SECUNDARIO);
        }


        if (componente instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) corregirContraste(hijo);
        }
    }

    private static boolean esClaro(Color color) {
        if (color == null) return false;
        int brillo = (color.getRed() * 299 + color.getGreen() * 587 + color.getBlue() * 114) / 1000;
        return brillo > 205;
    }
}
