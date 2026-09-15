package insta.interfaz;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JViewport;
import javax.swing.text.JTextComponent;
import javax.swing.plaf.basic.BasicButtonUI;

final class TemaComponentes {

    private TemaComponentes() {
    }

    static void corregirContraste(Component componente) {
        if (componente == null) {
            return;
        }

        if (componente instanceof JTextComponent texto) {
            texto.setOpaque(true);
            texto.setBackground(TemaInsta.INPUT);
            texto.setForeground(TemaInsta.TEXTO);
            texto.setCaretColor(TemaInsta.TEXTO);
            texto.setSelectionColor(TemaInsta.BOTON);
            texto.setSelectedTextColor(TemaInsta.BOTON_TEXTO);
        }

        if (componente instanceof JComboBox<?> combo) {
            combo.setOpaque(true);
            combo.setBackground(TemaInsta.INPUT);
            combo.setForeground(TemaInsta.TEXTO);
            combo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
                javax.swing.JLabel label = new javax.swing.JLabel(value == null ? "" : value.toString());
                label.setOpaque(true);
                label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                label.setBackground(isSelected ? TemaInsta.BOTON : TemaInsta.INPUT);
                label.setForeground(isSelected ? TemaInsta.BOTON_TEXTO : TemaInsta.TEXTO);
                return label;
            });

            for (Component hijo : combo.getComponents()) {
                if (hijo instanceof AbstractButton botonCombo) {
                    configurarBotonInterno(botonCombo);
                }
            }
        }

        if (componente instanceof JSpinner spinner) {
            spinner.setOpaque(true);
            spinner.setBackground(TemaInsta.INPUT);
            spinner.setBorder(BorderFactory.createLineBorder(TemaInsta.BORDE));

            if (spinner.getEditor() instanceof JSpinner.DefaultEditor editor) {
                editor.setBackground(TemaInsta.INPUT);
                editor.getTextField().setOpaque(true);
                editor.getTextField().setBackground(TemaInsta.INPUT);
                editor.getTextField().setForeground(TemaInsta.TEXTO);
                editor.getTextField().setCaretColor(TemaInsta.TEXTO);
            }

            corregirBotonesInternos(spinner);
        }

        if (componente instanceof JList<?> lista) {
            lista.setOpaque(true);
            lista.setBackground(TemaInsta.FONDO_SECUNDARIO);
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
            popup.setForeground(TemaInsta.TEXTO);
            popup.setBorder(BorderFactory.createLineBorder(TemaInsta.BORDE));
        }

        if (componente instanceof JScrollPane scroll) {
            scroll.setOpaque(true);
            Component vista = scroll.getViewport().getView();

            if (vista instanceof JTextComponent) {
                scroll.setBackground(TemaInsta.INPUT);
                scroll.getViewport().setBackground(TemaInsta.INPUT);
            } else if (vista instanceof JList<?>) {
                scroll.setBackground(TemaInsta.FONDO_SECUNDARIO);
                scroll.getViewport().setBackground(TemaInsta.FONDO_SECUNDARIO);
            } else {
                scroll.setBackground(TemaInsta.FONDO);
                scroll.getViewport().setBackground(TemaInsta.FONDO);
            }
        }

        if (componente instanceof JViewport viewport) {
            Component vista = viewport.getView();

            if (vista instanceof JTextComponent) {
                viewport.setBackground(TemaInsta.INPUT);
            } else if (vista instanceof JList<?>) {
                viewport.setBackground(TemaInsta.FONDO_SECUNDARIO);
            } else if (debeCorregirse(viewport.getBackground())) {
                viewport.setBackground(TemaInsta.FONDO);
            }
        }

        if (componente instanceof JPanel panel && panel.isOpaque() && debeCorregirse(panel.getBackground())) {
            panel.setBackground(TemaInsta.FONDO_SECUNDARIO);
        }

        if (componente instanceof AbstractButton boton) {
            boton.setOpaque(true);
            boton.setContentAreaFilled(true);
            boton.setFocusPainted(false);

            /*
             * El Look & Feel de Windows puede ignorar el background de un
             * JButton y seguir pintándolo blanco. BasicButtonUI respeta los
             * colores que cada panel ya asignó según TemaInsta.
             */
            if (boton instanceof JButton) {
                boton.setUI(new BasicButtonUI());
            }
        }

        if (componente instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) {
                corregirContraste(hijo);
            }
        }
    }

    private static void corregirBotonesInternos(Container contenedor) {
        for (Component hijo : contenedor.getComponents()) {
            if (hijo instanceof AbstractButton boton) {
                configurarBotonInterno(boton);
            }

            if (hijo instanceof Container interno) {
                corregirBotonesInternos(interno);
            }
        }
    }

    private static void configurarBotonInterno(AbstractButton boton) {
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setFocusPainted(false);

        if (boton instanceof JButton) {
            boton.setUI(new BasicButtonUI());
        }

        boton.setBackground(TemaInsta.INPUT);
        boton.setForeground(TemaInsta.TEXTO);
        boton.setBorder(BorderFactory.createLineBorder(TemaInsta.BORDE));
    }

    private static boolean debeCorregirse(Color color) {
        if (color == null) {
            return false;
        }

        int brillo = brillo(color);

        if (TemaInsta.oscuro) {
            return brillo > 210;
        }

        return brillo < 60;
    }

    private static int brillo(Color color) {
        return (color.getRed() * 299 + color.getGreen() * 587 + color.getBlue() * 114) / 1000;
    }
}
