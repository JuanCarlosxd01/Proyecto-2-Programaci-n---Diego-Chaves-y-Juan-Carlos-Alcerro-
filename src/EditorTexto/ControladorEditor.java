package EditorTexto;

import excepciones.ArchivoTxtException;
import interfaz.DialogosWindows;
import interfaz.EditorTextoPanel;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import sistema.RutasSistema;

public class ControladorEditor {

    private final EditorTextoPanel vista;
    private Color colorActual;
    private final TxtReader lector;
    private final TxtWriter escritor;
    private String rutaActual;

    public ControladorEditor(EditorTextoPanel vista) {
        this.vista = vista;
        colorActual = Color.BLACK;
        lector = new TxtReader();
        escritor = new TxtWriter();
        configurarEventos();
    }

    private void configurarEventos() {
        vista.getBtnNegrita().addActionListener(e -> aplicarFormato());
        vista.getBtnCursiva().addActionListener(e -> aplicarFormato());
        vista.getBtnSubrayado().addActionListener(e -> aplicarFormato());
        vista.getBtnTachado().addActionListener(e -> aplicarFormato());
        vista.getCmbFuente().addActionListener(e -> aplicarFormato());
        vista.getCmbTamano().addActionListener(e -> aplicarFormato());
        vista.getBtnColor().addActionListener(e -> cambiarColor());
        vista.getItemAbrir().addActionListener(e -> abrirArchivo());
        vista.getItemGuardar().addActionListener(e -> guardar());
        vista.getItemGuardarComo().addActionListener(e -> guardarComo());
        vista.getItemNuevo().addActionListener(e -> nuevoDocumento());
    }

    private void aplicarFormato() {
        String fuente = (String) vista.getCmbFuente().getSelectedItem();
        int tamano = (Integer) vista.getCmbTamano().getSelectedItem();

        Formato formato = Formato.desde(
                fuente,
                tamano,
                vista.getBtnNegrita().isSelected(),
                vista.getBtnCursiva().isSelected(),
                vista.getBtnSubrayado().isSelected(),
                vista.getBtnTachado().isSelected(),
                colorActual
        );

        SimpleAttributeSet atributos = atributos(formato);
        vista.getAreaTexto().setCharacterAttributes(atributos, false);
    }

    private SimpleAttributeSet atributos(Formato formato) {
        SimpleAttributeSet atributos = new SimpleAttributeSet();

        StyleConstants.setFontFamily(atributos, formato.getFuente());
        StyleConstants.setFontSize(atributos, formato.getTamano());
        StyleConstants.setForeground(atributos, formato.getColor());
        StyleConstants.setBold(atributos, formato.esNegrita());
        StyleConstants.setItalic(atributos, formato.esCursiva());
        StyleConstants.setUnderline(atributos, formato.esSubrayado());
        StyleConstants.setStrikeThrough(atributos, formato.esTachado());

        return atributos;
    }

    private void cambiarColor() {
        Color nuevo = JColorChooser.showDialog(vista, "Seleccionar color", colorActual);

        if (nuevo != null) {
            colorActual = nuevo;
            aplicarFormato();
        }
    }

    private void abrirArchivo() {
        JFileChooser chooser = crearSelector();

        if (chooser.showOpenDialog(vista) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = chooser.getSelectedFile();

        if (!estaDentroDeMisDocumentos(archivo)) {
            DialogosWindows.showMessageDialog(
                    vista,
                    "Solo puede abrir documentos ubicados dentro de la carpeta Mis Documentos del usuario.",
                    "Ubicación no permitida",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String ruta = archivo.getAbsolutePath();

        try {
            if (lector.tieneFormatoMiniWindows(archivo)) {
                cargarDocumento(lector.abrir(ruta));
            } else {
                cargarTextoPlano(Files.readString(archivo.toPath(), StandardCharsets.UTF_8));
            }

            rutaActual = ruta;

        } catch (ArchivoTxtException | IOException e) {
            DialogosWindows.showMessageDialog(vista, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void abrirArchivo(File archivo) {
        if (archivo == null || !archivo.exists() || !archivo.isFile()) {
            return;
        }

        try {
            archivo = sistema.SeguridadArchivos.permitido(archivo);

            String ruta = archivo.getAbsolutePath();

            if (lector.tieneFormatoMiniWindows(archivo)) {
                cargarDocumento(lector.abrir(ruta));
            } else {
                cargarTextoPlano(java.nio.file.Files.readString(archivo.toPath(), java.nio.charset.StandardCharsets.UTF_8));
            }

            rutaActual = ruta;

        } catch (Exception e) {
            DialogosWindows.showMessageDialog(vista, "No se pudo abrir el archivo:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JFileChooser crearSelector() {
        File carpetaDocumentos = RutasSistema.getDocumentosUsuarioActual();

        if (carpetaDocumentos != null && !carpetaDocumentos.exists()) {
            carpetaDocumentos.mkdirs();
        }

        JFileChooser chooser;

        if (carpetaDocumentos != null && carpetaDocumentos.exists()) {
            chooser = new JFileChooser(carpetaDocumentos);
        } else {
            chooser = new JFileChooser();
        }

        chooser.setFileFilter(new FileNameExtensionFilter("Documentos de texto (*.txt)", "txt"));

        return chooser;
    }

    private boolean estaDentroDeMisDocumentos(File archivo) {
        File carpetaDocumentos = RutasSistema.getDocumentosUsuarioActual();

        if (carpetaDocumentos == null || archivo == null) {
            return false;
        }

        try {
            File documentosCanonica = carpetaDocumentos.getCanonicalFile();
            File archivoCanonico = archivo.getCanonicalFile();

            return archivoCanonico.toPath().startsWith(documentosCanonica.toPath());

        } catch (IOException e) {
            return false;
        }
    }

    private void cargarTextoPlano(String texto) {
        vista.getAreaTexto().setText("");

        Formato formato = Formato.desde(
                "Segoe UI",
                12,
                false,
                false,
                false,
                false,
                Color.BLACK
        );

        try {
            vista.getAreaTexto().getStyledDocument().insertString(0, texto, atributos(formato));

        } catch (BadLocationException e) {
            DialogosWindows.showMessageDialog(vista, "No se pudo cargar el texto.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDocumento(List<TextChunk> fragmentos) {
        vista.getAreaTexto().setText("");

        StyledDocument documento = vista.getAreaTexto().getStyledDocument();

        for (TextChunk chunk : fragmentos) {
            try {
                documento.insertString(documento.getLength(), chunk.getTexto(), atributos(chunk.getFormato()));

            } catch (BadLocationException e) {
                DialogosWindows.showMessageDialog(vista, "Error cargando el texto.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private List<TextChunk> obtenerFragmentos() {
        List<TextChunk> fragmentos = new ArrayList<>();

        StyledDocument documento = vista.getAreaTexto().getStyledDocument();

        int posicion = 0;

        while (posicion < documento.getLength()) {
            Element elemento = documento.getCharacterElement(posicion);

            AttributeSet a = elemento.getAttributes();

            int inicio = elemento.getStartOffset();
            int fin = Math.min(elemento.getEndOffset(), documento.getLength());

            try {
                String texto = documento.getText(inicio, fin - inicio);

                Formato formato = Formato.desde(
                        StyleConstants.getFontFamily(a),
                        StyleConstants.getFontSize(a),
                        StyleConstants.isBold(a),
                        StyleConstants.isItalic(a),
                        StyleConstants.isUnderline(a),
                        StyleConstants.isStrikeThrough(a),
                        StyleConstants.getForeground(a)
                );

                fragmentos.add(new TextChunk(texto, formato));

            } catch (BadLocationException e) {
                throw new IllegalStateException("No se pudo leer el documento.", e);
            }

            posicion = fin;
        }

        return fragmentos;
    }

    private void guardarComo() {
        JFileChooser chooser = crearSelector();

        if (chooser.showSaveDialog(vista) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = chooser.getSelectedFile();

        if (!archivo.getName().toLowerCase().endsWith(".txt")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".txt");
        }

        if (!estaDentroDeMisDocumentos(archivo)) {
            DialogosWindows.showMessageDialog(
                    vista,
                    "El documento debe guardarse dentro de la carpeta Mis Documentos del usuario.",
                    "Ubicación no permitida",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String ruta = archivo.getAbsolutePath();

        if (guardarEn(ruta)) {
            rutaActual = ruta;
        }
    }

    private void guardar() {
        if (rutaActual == null) {
            guardarComo();
            return;
        }

        File archivo = new File(rutaActual);

        if (!estaDentroDeMisDocumentos(archivo)) {
            rutaActual = null;
            guardarComo();
            return;
        }

        guardarEn(rutaActual);
    }

    private boolean guardarEn(String ruta) {
        try {
            List<TextChunk> fragmentos = obtenerFragmentos();

            if (!ruta.toLowerCase().endsWith(".txt")) {
                ruta += ".txt";
            }

            escritor.guardar(ruta, fragmentos);

            DialogosWindows.showMessageDialog(
                    vista,
                    "Archivo guardado correctamente en Mis Documentos."
            );

            return true;

        } catch (ArchivoTxtException e) {
            DialogosWindows.showMessageDialog(vista, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void nuevoDocumento() {
        vista.getAreaTexto().setText("");
        rutaActual = null;
    }
}