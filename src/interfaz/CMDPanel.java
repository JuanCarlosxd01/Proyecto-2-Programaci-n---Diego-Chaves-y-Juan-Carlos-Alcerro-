
package interfaz;

import CMD.GestorArchivos;
import CMD.controladorComandos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class CMDPanel extends JPanel{

    private JTextArea areaSalida;
    private JTextField campoEntrada;
    private JLabel labelPrompt;
    private JScrollPane scrollSalida;

    private final GestorArchivos gestorArchivos;
    private final controladorComandos interprete;

    private Runnable accionCerrar;

    public enum ModoEntrada { NORMAL, WRITING }

    private ModoEntrada modoActual = ModoEntrada.NORMAL;
    private FileWriter escritorActivo = null;
    private String nombreArchivoActivo = "";

    public CMDPanel(){
        gestorArchivos = new GestorArchivos();
        interprete = new controladorComandos(this, gestorArchivos);

        configurarInterfaz();
        mostrarMensajeBienvenida();
    }


    private void configurarInterfaz(){
        setLayout(new BorderLayout());

        Color colorFondo = new Color(12, 12, 12);
        Color colorTexto = new Color(204, 204, 204);
        Color colorPrompt = new Color(0, 255, 128);
        Font fuenteConsola = new Font("Consolas", Font.PLAIN, 15);

        setBackground(colorFondo);

        areaSalida = new JTextArea();
        areaSalida.setEditable(false);
        areaSalida.setBackground(colorFondo);
        areaSalida.setForeground(colorTexto);
        areaSalida.setFont(fuenteConsola);
        areaSalida.setCaretColor(colorTexto);
        areaSalida.setBorder(new EmptyBorder(8, 8, 8, 8));
        areaSalida.setLineWrap(true);
        areaSalida.setWrapStyleWord(true);

        scrollSalida = new JScrollPane(areaSalida);
        scrollSalida.setBorder(null);
        scrollSalida.getViewport().setBackground(colorFondo);

        JPanel panelEntrada = new JPanel(new BorderLayout(5, 0));
        panelEntrada.setBackground(colorFondo);
        panelEntrada.setBorder(new EmptyBorder(4, 8, 8, 8));

        labelPrompt = new JLabel(gestorArchivos.obtenerPrompt());
        labelPrompt.setForeground(colorPrompt);
        labelPrompt.setFont(fuenteConsola);

        campoEntrada = new JTextField();
        campoEntrada.setBackground(colorFondo);
        campoEntrada.setForeground(colorTexto);
        campoEntrada.setFont(fuenteConsola);
        campoEntrada.setCaretColor(Color.WHITE);
        campoEntrada.setBorder(BorderFactory.createEmptyBorder());

        campoEntrada.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e){
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    procesarEntrada(campoEntrada.getText());
                    campoEntrada.setText("");
                }
            }
        });

        panelEntrada.add(labelPrompt, BorderLayout.WEST);
        panelEntrada.add(campoEntrada, BorderLayout.CENTER);

        add(scrollSalida, BorderLayout.CENTER);
        add(panelEntrada, BorderLayout.SOUTH);
    }


    private void mostrarMensajeBienvenida(){
        areaSalida.append("Microsoft Windows [Versión Simulada 10.0.19045]\n");
        areaSalida.append("(c) Consola de Comandos Simulada. Todos los derechos reservados.\n");
        areaSalida.append("Unidad activa: " + gestorArchivos.obtenerPrompt().replace(">", "") + "\n");
        areaSalida.append("Los archivos creados aquí son los mismos de la unidad del usuario en el Explorador.\n");
        areaSalida.append("Escriba 'Help' para ver la lista de comandos disponibles.\n\n");
    }

    private void procesarEntrada(String texto){
        if(modoActual == ModoEntrada.WRITING){
            areaSalida.append(labelPrompt.getText() + texto + "\n");

            if(texto.equals("EXIT")){
                try{
                    if(escritorActivo != null){
                        escritorActivo.flush();
                        escritorActivo.close();
                    }

                    areaSalida.append("Archivo '" + nombreArchivoActivo + "' guardado exitosamente.\n\n");
                }
                catch(IOException ex){
                    areaSalida.append("Error al guardar archivo: " + ex.getMessage() + "\n\n");
                }
                finally{
                    modoActual = ModoEntrada.NORMAL;
                    escritorActivo = null;
                    nombreArchivoActivo = "";
                }
            }
            else{
                try{
                    escritorActivo.write(texto + System.lineSeparator());
                }
                catch(IOException ex){
                    areaSalida.append("Error de escritura: " + ex.getMessage() + "\n");
                }
            }
        }
        else{
            areaSalida.append(gestorArchivos.obtenerPrompt() + texto + "\n");
            interprete.ejecutarComando(texto.trim());
        }

        actualizarInterfaz();
    }

    public void activarModoEscritura(FileWriter writer, String nombreArchivo, boolean append){
        escritorActivo = writer;
        nombreArchivoActivo = nombreArchivo;
        modoActual = ModoEntrada.WRITING;

        areaSalida.append("--- Modo " + (append ? "Anexar" : "Escritura") + " iniciado para '" + nombreArchivo + "' ---\n");
        areaSalida.append("Escriba las líneas de texto. Ingrese 'EXIT' para finalizar y guardar.\n\n");
    }

    public void imprimir(String texto){
        areaSalida.append(texto + "\n");
    }

    public void limpiar(){
        areaSalida.setText("");
    }

    public File getCarpetaActual(){
        return gestorArchivos.getDirectorioActual();
    }

    public void setCarpetaActual(File carpetaActual){
        gestorArchivos.setCarpetaActual(carpetaActual);
        actualizarInterfaz();
    }

    public void actualizarInterfaz(){
        if(modoActual == ModoEntrada.NORMAL){
            labelPrompt.setText(gestorArchivos.obtenerPrompt());
        }
        else{
            labelPrompt.setText("escribir> ");
        }

        SwingUtilities.invokeLater(() -> {
            areaSalida.setCaretPosition(areaSalida.getDocument().getLength());
            campoEntrada.requestFocusInWindow();
        });
    }

    public void setAccionCerrar(Runnable accionCerrar){
        this.accionCerrar = accionCerrar;
    }

    public void cerrar(){
        if(accionCerrar != null){
            accionCerrar.run();
        }
    }
}