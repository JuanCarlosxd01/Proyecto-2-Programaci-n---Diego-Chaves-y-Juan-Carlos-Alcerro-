package insta.interfaz;

import estructuras.ListaEnlazada;
import java.awt.geom.Rectangle2D;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import red.Cliente;
import red.Respuesta;

final class AutocompletarMenciones {

    private final Cliente cliente;
    private final JTextComponent campo;
    private final JPopupMenu popup = new JPopupMenu();
    private final Timer timer;
    private long solicitud;

    AutocompletarMenciones(Cliente cliente, JTextComponent campo) {
        this.cliente = cliente;
        this.campo = campo;
        popup.setFocusable(false);
        timer = new Timer(280, e -> buscar());
        timer.setRepeats(false);

        campo.addCaretListener(e -> programar());
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { programar(); }
            @Override public void removeUpdate(DocumentEvent e) { programar(); }
            @Override public void changedUpdate(DocumentEvent e) { programar(); }
        });
    }

    private void programar() {
        if (prefijo() == null) {
            popup.setVisible(false);
            timer.stop();
            return;
        }
        timer.restart();
    }

    private String prefijo() {
        int caret = campo.getCaretPosition();
        String texto = campo.getText();
        if (caret < 0 || caret > texto.length()) return null;
        int inicio = caret - 1;
        while (inicio >= 0) {
            char c = texto.charAt(inicio);
            if (c == '@') break;
            if (!(Character.isLetterOrDigit(c) || c == '_')) return null;
            inicio--;
        }
        if (inicio < 0 || texto.charAt(inicio) != '@') return null;
        if (inicio > 0) {
            char anterior = texto.charAt(inicio - 1);
            if (Character.isLetterOrDigit(anterior) || anterior == '_') return null;
        }
        return texto.substring(inicio + 1, caret).toLowerCase();
    }

    private void buscar() {
        String prefijo = prefijo();
        if (prefijo == null) return;
        long numero = ++solicitud;

        SwingWorker<ListaEnlazada<Respuesta.DatosUsuario>, Void> worker = new SwingWorker<>() {
            @Override
            protected ListaEnlazada<Respuesta.DatosUsuario> doInBackground() throws Exception {
                Respuesta respuesta = cliente.buscarPersonas(prefijo, 0);
                if (!respuesta.esExitosa()) return new ListaEnlazada<>();
                return respuesta.getUsuarios();
            }

            @Override
            protected void done() {
                if (numero != solicitud) return;
                try {
                    String actual = prefijo();
                    if (actual == null || !actual.equals(prefijo)) return;
                    mostrar(get());
                } catch (Exception e) {
                    popup.setVisible(false);
                }
            }
        };
        worker.execute();
    }

    private void mostrar(ListaEnlazada<Respuesta.DatosUsuario> usuarios) {
        popup.removeAll();
        int agregados = 0;
        Respuesta.DatosUsuario actual = cliente.getUsuarioActual();

        for (Respuesta.DatosUsuario usuario : usuarios) {
            if (actual != null && actual.getUsername().equalsIgnoreCase(usuario.getUsername())) continue;
            JMenuItem item = new JMenuItem("@" + usuario.getUsername() + "  ·  " + usuario.getNombreCompleto());
            item.setOpaque(true);
            item.setBackground(TemaInsta.TARJETA);
            item.setForeground(TemaInsta.TEXTO);
            item.addActionListener(e -> insertar(usuario.getUsername()));
            popup.add(item);
            if (++agregados >= 6) break;
        }

        popup.setBackground(TemaInsta.TARJETA);
        popup.setBorder(javax.swing.BorderFactory.createLineBorder(TemaInsta.BORDE));

        if (agregados == 0) {
            popup.setVisible(false);
            return;
        }

        try {
            Rectangle2D r = campo.modelToView2D(campo.getCaretPosition());
            popup.show(campo, (int) r.getX(), (int) (r.getY() + r.getHeight()));
            campo.requestFocusInWindow();
        } catch (Exception e) {
            popup.show(campo, 10, campo.getHeight());
        }
    }

    private void insertar(String username) {
        int caret = campo.getCaretPosition();
        String texto = campo.getText();
        int inicio = caret - 1;
        while (inicio >= 0 && texto.charAt(inicio) != '@' && (Character.isLetterOrDigit(texto.charAt(inicio)) || texto.charAt(inicio) == '_')) inicio--;
        if (inicio < 0 || texto.charAt(inicio) != '@') return;
        String nuevo = texto.substring(0, inicio) + "@" + username + " " + texto.substring(caret);
        campo.setText(nuevo);
        campo.setCaretPosition(Math.min(nuevo.length(), inicio + username.length() + 2));
        popup.setVisible(false);
        campo.requestFocusInWindow();
    }
}
