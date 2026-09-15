package interfaz;

import java.awt.Component;
import java.awt.Color;
import java.awt.Font;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.Icon;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.HashMap;
import java.util.Map;

public final class DialogosWindows {

    private static JDesktopPane escritorioActual;

    private DialogosWindows() {
    }


    private static final String[] CLAVES_ESTILO = {
        "OptionPane.background", "Panel.background", "OptionPane.messageForeground", "OptionPane.messageFont",
        "Button.background", "Button.foreground", "Button.font", "Button.focus",
        "TextField.background", "TextField.foreground", "TextField.caretForeground",
        "ComboBox.background", "ComboBox.foreground"
    };

    private static Map<String, Object> guardarEstiloActual() {
        Map<String, Object> valores = new HashMap<>();
        for (String clave : CLAVES_ESTILO) valores.put(clave, UIManager.get(clave));
        return valores;
    }

    private static void restaurarEstilo(Map<String, Object> valores) {
        for (String clave : CLAVES_ESTILO) {
            Object valor = valores.get(clave);
            if (valor != null) UIManager.put(clave, valor);
            else UIManager.getDefaults().remove(clave);
        }
    }

    private static void aplicarEstiloMiniWindows() {
        Color fondo = Color.WHITE;
        Color texto = new Color(30, 30, 30);
        Color azul = new Color(0, 120, 215);
        UIManager.put("OptionPane.background", fondo);
        UIManager.put("Panel.background", fondo);
        UIManager.put("OptionPane.messageForeground", texto);
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Button.background", azul);
        UIManager.put("Button.foreground", Color.BLACK);
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("Button.focus", azul);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", texto);
        UIManager.put("TextField.caretForeground", texto);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.foreground", texto);
    }

    private static void prepararEstiloVisible() {
    }

    private static void estilizarRecursivo(Component componente) {
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
        Map<String, Object> estiloAnterior = guardarEstiloActual();
        try {
            aplicarEstiloMiniWindows();
            JDesktopPane desktop = buscarEscritorio(padre);
            if (desktop != null) JOptionPane.showInternalMessageDialog(desktop, mensaje, titulo, tipo);
            else JOptionPane.showMessageDialog(padre, mensaje, titulo, tipo);
        } finally {
            restaurarEstilo(estiloAnterior);
        }
    }

    public static int showConfirmDialog(Component padre, Object mensaje, String titulo, int tipoOpcion) {
        return showConfirmDialog(padre, mensaje, titulo, tipoOpcion, JOptionPane.QUESTION_MESSAGE);
    }

    public static int showConfirmDialog(Component padre, Object mensaje, String titulo, int tipoOpcion, int tipoMensaje) {
        Map<String, Object> estiloAnterior = guardarEstiloActual();
        try {
            aplicarEstiloMiniWindows();
            JDesktopPane desktop = buscarEscritorio(padre);
            if (desktop != null) return JOptionPane.showInternalConfirmDialog(desktop, mensaje, titulo, tipoOpcion, tipoMensaje);
            return JOptionPane.showConfirmDialog(padre, mensaje, titulo, tipoOpcion, tipoMensaje);
        } finally {
            restaurarEstilo(estiloAnterior);
        }
    }

    public static String showInputDialog(Component padre, Object mensaje) {
        Map<String, Object> estiloAnterior = guardarEstiloActual();
        try {
            aplicarEstiloMiniWindows();
            JDesktopPane desktop = buscarEscritorio(padre);
            if (desktop != null) return JOptionPane.showInternalInputDialog(desktop, mensaje);
            return JOptionPane.showInputDialog(padre, mensaje);
        } finally {
            restaurarEstilo(estiloAnterior);
        }
    }

    public static String showInputDialog(Component padre, Object mensaje, Object valorInicial) {
        Map<String, Object> estiloAnterior = guardarEstiloActual();
        try {
            aplicarEstiloMiniWindows();
            JDesktopPane desktop = buscarEscritorio(padre);
            Object resultado;
            if (desktop != null) resultado = JOptionPane.showInternalInputDialog(desktop, mensaje, "MiniWindows", JOptionPane.QUESTION_MESSAGE, null, null, valorInicial);
            else resultado = JOptionPane.showInputDialog(padre, mensaje, valorInicial);
            return resultado == null ? null : resultado.toString();
        } finally {
            restaurarEstilo(estiloAnterior);
        }
    }

    public static String showInputDialog(Component padre, Object mensaje, String titulo, int tipoMensaje) {
        Map<String, Object> estiloAnterior = guardarEstiloActual();
        try {
            aplicarEstiloMiniWindows();
            JDesktopPane desktop = buscarEscritorio(padre);
            Object resultado;
            if (desktop != null) resultado = JOptionPane.showInternalInputDialog(desktop, mensaje, titulo, tipoMensaje, null, null, null);
            else resultado = JOptionPane.showInputDialog(padre, mensaje, titulo, tipoMensaje);
            return resultado == null ? null : resultado.toString();
        } finally {
            restaurarEstilo(estiloAnterior);
        }
    }

    public static int showOptionDialog(Component padre, Object mensaje, String titulo, int tipoOpcion, int tipoMensaje, Icon icono, Object[] opciones, Object valorInicial) {
        Map<String, Object> estiloAnterior = guardarEstiloActual();
        try {
            aplicarEstiloMiniWindows();
            JDesktopPane desktop = buscarEscritorio(padre);
            if (desktop != null) return JOptionPane.showInternalOptionDialog(desktop, mensaje, titulo, tipoOpcion, tipoMensaje, icono, opciones, valorInicial);
            return JOptionPane.showOptionDialog(padre, mensaje, titulo, tipoOpcion, tipoMensaje, icono, opciones, valorInicial);
        } finally {
            restaurarEstilo(estiloAnterior);
        }
    }

}
