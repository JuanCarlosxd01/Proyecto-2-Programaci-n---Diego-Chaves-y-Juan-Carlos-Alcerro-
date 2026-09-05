
package EditorTexto;

import interfaz.EditorTextoPanel;
import excepciones.ArchivoedtException;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class ControladorEditor {
    
    private EditorTextoPanel vista;
    private Color colorActual;
    private EdtReader lector;
    private EdtWriter escritor;
    private String rutaActual;
    
    public ControladorEditor(EditorTextoPanel vista){
        this.vista = vista;
        this.colorActual = Color.BLACK;
        this.lector = new EdtReader();
        this.escritor = new EdtWriter();
        this.rutaActual = null;
        configurarEventos();
    }
    
    private void configurarEventos(){
        vista.getBtnNegrita().addActionListener(e ->{
            aplicarFormato();
        });
        vista.getBtnCursiva().addActionListener(e -> {
            aplicarFormato();
        });
        vista.getBtnSubrayado().addActionListener(e ->{
            aplicarFormato();
        });
        vista.getBtnTachado().addActionListener(e ->{
            aplicarFormato();
        });
        
        vista.getCmbFuente().addActionListener(e ->{
            aplicarFormato();
        });
        vista.getCmbTamano().addActionListener(e ->{
            aplicarFormato();
        });
        vista.getBtnColor().addActionListener(e ->{
            cambiarColor();
        });
        
        vista.getItemAbrir().addActionListener(e ->{
            abrirArchivo();
        });
        
        vista.getItemGuardar().addActionListener(e -> {
            guardar();
        });
        vista.getItemGuardarComo().addActionListener(e ->{
            guardarComo();
        });
        vista.getItemNuevo().addActionListener(e -> {
            nuevoDocumento();
        });
    }
    
    private void aplicarFormato(){
        String fuente = (String) vista.getCmbFuente().getSelectedItem();
        int tamano = (Integer) vista.getCmbTamano().getSelectedItem();
        boolean negrita = vista.getBtnNegrita().isSelected();
        boolean cursiva = vista.getBtnCursiva().isSelected();
        boolean subrayado = vista.getBtnSubrayado().isSelected();
        boolean tachado = vista.getBtnTachado().isSelected();
        
        Formato formato = Formato.desde(fuente, tamano, negrita, cursiva, subrayado, tachado, colorActual);
        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setFontFamily(atributos, formato.getFuente());
        StyleConstants.setFontSize(atributos, formato.getTamano());
        StyleConstants.setForeground(atributos, formato.getColor());
        StyleConstants.setBold(atributos, formato.esNegrita());
        StyleConstants.setItalic(atributos, formato.esCursiva());
        StyleConstants.setUnderline(atributos, formato.esSubrayado());
        StyleConstants.setStrikeThrough(atributos, formato.esTachado());
        vista.getAreaTexto().setCharacterAttributes(atributos, false);
    }
    
    private void cambiarColor(){
        Color nColor = JColorChooser.showDialog(vista, "Seleccionar color", colorActual);
        if(nColor != null){
            colorActual = nColor;
            aplicarFormato();
        }
    }
    
    private void abrirArchivo(){
        JFileChooser chooser = new JFileChooser();
        int resultado = chooser.showOpenDialog(vista);
        if(resultado != JFileChooser.APPROVE_OPTION){
            return;
        }
        File archivo = chooser.getSelectedFile();
        
        try{
            List<TextChunk> fragmentos = lector.abrir(archivo.getAbsolutePath());
            cargarDocumento(fragmentos);
            rutaActual = archivo.getAbsolutePath();
        }catch(ArchivoedtException e){
            JOptionPane.showMessageDialog(vista, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cargarDocumento(List<TextChunk> fragmentos){
        JTextPane area = vista.getAreaTexto();
        area.setText("");
        StyledDocument documento = area.getStyledDocument();
        
        for(TextChunk chunk : fragmentos){
            Formato formato = chunk.getFormato();
            SimpleAttributeSet atributos = new SimpleAttributeSet();
            StyleConstants.setFontFamily(atributos, formato.getFuente());
            StyleConstants.setFontSize(atributos, formato.getTamano());
            StyleConstants.setForeground(atributos, formato.getColor());
            StyleConstants.setBold(atributos, formato.esNegrita());
            StyleConstants.setItalic(atributos, formato.esCursiva());
            StyleConstants.setUnderline(atributos, formato.esSubrayado());
            StyleConstants.setStrikeThrough(atributos, formato.esTachado());
            
            try{
                documento.insertString(documento.getLength(), chunk.getTexto(), atributos);
            }catch (BadLocationException e){
                JOptionPane.showMessageDialog(vista, "Error cargando el texto");
            }
        }
    }
    
    private List<TextChunk> obtenerFragmentos(){
        List<TextChunk> fragmentos = new ArrayList<>();
        StyledDocument documento = vista.getAreaTexto().getStyledDocument();
        int posicion = 0;
        
        while(posicion < documento.getLength()){
            Element elemento = documento.getCharacterElement(posicion);
            AttributeSet atributos = elemento.getAttributes();
            int inicio = elemento.getStartOffset();
            int fin = Math.min(elemento.getEndOffset(), documento.getLength());
            
            try{
                String texto = documento.getText(inicio, fin - inicio);
                String fuente = StyleConstants.getFontFamily(atributos);
                int tamano = StyleConstants.getFontSize(atributos);
                boolean negrita = StyleConstants.isBold(atributos);
                boolean cursiva = StyleConstants.isItalic(atributos); 
                boolean subrayado = StyleConstants.isUnderline(atributos);
                boolean tachado = StyleConstants.isStrikeThrough(atributos);
                Color color = StyleConstants.getForeground(atributos);
                
                Formato formato = Formato.desde(fuente, tamano, negrita, cursiva, subrayado, tachado, color);
                TextChunk fragmento = new TextChunk(texto, formato);
                fragmentos.add(fragmento);
            } catch(BadLocationException e){
                System.out.println("Error leyendo el documento");
            }
            posicion = fin;
        }
        return fragmentos;
    }
    
    private void guardarComo(){
        JFileChooser chooser = new JFileChooser();
        int resultado = chooser.showSaveDialog(vista);
        if(resultado != JFileChooser.APPROVE_OPTION){
            return;
        }
        
        File archivo = chooser.getSelectedFile();
        String ruta = archivo.getAbsolutePath();
        
        if(!ruta.toLowerCase().endsWith(".edt")){
            ruta += ".edt";
        }
        
        try{
            List<TextChunk> fragmentos = obtenerFragmentos();
            escritor.guardar(ruta, fragmentos);
            rutaActual = ruta;
            JOptionPane.showMessageDialog(vista, "Archivo guardado correctamente");
        } catch(ArchivoedtException e){
            JOptionPane.showMessageDialog(vista, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void guardar(){
        if(rutaActual == null){
            guardarComo();
            return;
        }
        try{
            List<TextChunk> fragmentos = obtenerFragmentos();
            escritor.guardar(rutaActual, fragmentos);
            JOptionPane.showMessageDialog(vista, "Archivo guardado correctamente");
        }catch(ArchivoedtException e){
            JOptionPane.showMessageDialog(vista, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void nuevoDocumento(){
        vista.getAreaTexto().setText("");
        rutaActual = null;
    } 
}
