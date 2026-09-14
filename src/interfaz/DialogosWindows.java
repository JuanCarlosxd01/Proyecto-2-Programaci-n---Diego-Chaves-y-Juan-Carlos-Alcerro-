package interfaz;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class DialogosWindows {

    private static JDesktopPane escritorioActual;

    private DialogosWindows() {
    }

    public static void registrarEscritorio(JDesktopPane escritorio) {
        escritorioActual = escritorio;
    }

    private static JDesktopPane buscarEscritorio(Component padre) {
        if (padre instanceof JDesktopPane desktop) {
            return desktop;
        }
        if (padre != null) {
            JDesktopPane encontrado = (JDesktopPane) SwingUtilities.getAncestorOfClass(JDesktopPane.class, padre);
            if (encontrado != null) {
                return encontrado;
            }
        }
        if (escritorioActual != null && escritorioActual.isShowing()) {
            return escritorioActual;
        }
        return null;
    }

    public static void showMessageDialog(Component padre, Object mensaje) {
        showMessageDialog(padre, mensaje, "MiniWindows", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showMessageDialog(Component padre, Object mensaje, String titulo, int tipo) {
        JDesktopPane desktop = buscarEscritorio(padre);
        if (desktop != null) {
            JOptionPane.showInternalMessageDialog(desktop, mensaje, titulo, tipo);
        } else {
            JOptionPane.showMessageDialog(padre, mensaje, titulo, tipo);
        }
    }

    public static int showConfirmDialog(Component padre, Object mensaje, String titulo, int tipoOpcion) {
        return showConfirmDialog(padre, mensaje, titulo, tipoOpcion, JOptionPane.QUESTION_MESSAGE);
    }

    public static int showConfirmDialog(Component padre, Object mensaje, String titulo, int tipoOpcion, int tipoMensaje) {
        JDesktopPane desktop = buscarEscritorio(padre);
        if (desktop != null) {
            return JOptionPane.showInternalConfirmDialog(desktop, mensaje, titulo, tipoOpcion, tipoMensaje);
        }
        return JOptionPane.showConfirmDialog(padre, mensaje, titulo, tipoOpcion, tipoMensaje);
    }

    public static String showInputDialog(Component padre, Object mensaje) {
        JDesktopPane desktop = buscarEscritorio(padre);
        if (desktop != null) {
            return JOptionPane.showInternalInputDialog(desktop, mensaje);
        }
        return JOptionPane.showInputDialog(padre, mensaje);
    }

    public static String showInputDialog(Component padre, Object mensaje, Object valorInicial) {
        JDesktopPane desktop = buscarEscritorio(padre);
        if (desktop != null) {
            Object resultado = JOptionPane.showInternalInputDialog(desktop, mensaje, "MiniWindows", JOptionPane.QUESTION_MESSAGE, null, null, valorInicial);
            return resultado == null ? null : resultado.toString();
        }
        Object resultado = JOptionPane.showInputDialog(padre, mensaje, valorInicial);
        return resultado == null ? null : resultado.toString();
    }

    public static String showInputDialog(Component padre, Object mensaje, String titulo, int tipoMensaje) {
        JDesktopPane desktop = buscarEscritorio(padre);
        if (desktop != null) {
            Object resultado = JOptionPane.showInternalInputDialog(desktop, mensaje, titulo, tipoMensaje, null, null, null);
            return resultado == null ? null : resultado.toString();
        }
        return JOptionPane.showInputDialog(padre, mensaje, titulo, tipoMensaje);
    }

    public static int showOptionDialog(Component padre, Object mensaje, String titulo, int tipoOpcion, int tipoMensaje, Icon icono, Object[] opciones, Object valorInicial) {
        JDesktopPane desktop = buscarEscritorio(padre);
        if (desktop != null) {
            return JOptionPane.showInternalOptionDialog(desktop, mensaje, titulo, tipoOpcion, tipoMensaje, icono, opciones, valorInicial);
        }
        return JOptionPane.showOptionDialog(padre, mensaje, titulo, tipoOpcion, tipoMensaje, icono, opciones, valorInicial);
    }
}
