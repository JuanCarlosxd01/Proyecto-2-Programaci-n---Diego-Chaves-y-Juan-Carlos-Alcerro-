package interfaz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import CMD.*;
import sistema.*;

public class EscritorioPanel extends JPanel {

    private JDesktopPane escritorio;
    private JPanel barraTareas;
    private JPanel panelVentanas;
    private JLabel lblHora;
    private JLabel lblFecha;
    private JButton btnWindows;
    private JButton iconoSeleccionado = null;
    private JButton btnUsuarios;
    private GestorUsuarios gestorUsuarios;

    private int anchoCelda = 100;
    private int altoCelda = 100;

    public EscritorioPanel(JPanel contenedor, CardLayout transicion, GestorUsuarios gestorUsuarios){
        this.gestorUsuarios = gestorUsuarios;
        setLayout(new BorderLayout());
        crearEscritorio();
        crearBarraTareas();
        crearIconosEjemplo();
        addComponentListener(new ComponentAdapter(){
            @Override
            public void componentShown(ComponentEvent e){
                actualizarPermisos();
            }
        });
    }

    private void crearEscritorio(){
        escritorio = new JDesktopPane();
        escritorio.setBackground(new Color(20, 45, 75));
        escritorio.setLayout(null);
        add(escritorio, BorderLayout.CENTER);
    }

    private void crearBarraTareas(){
        barraTareas = new JPanel(new BorderLayout());
        barraTareas.setPreferredSize(new Dimension(0, 48));
        barraTareas.setBackground(new Color(25, 25, 25));

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 4));
        izquierda.setOpaque(false);

        btnWindows = new JButton("⊞");
        btnWindows.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 24));
        btnWindows.setForeground(Color.WHITE);
        btnWindows.setBackground(new Color(25, 25, 25));
        btnWindows.setFocusPainted(false);
        btnWindows.setBorderPainted(false);

        izquierda.add(btnWindows);

        panelVentanas = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 4));
        panelVentanas.setOpaque(false);
        izquierda.add(panelVentanas);

        barraTareas.add(izquierda, BorderLayout.WEST);

        JPanel derecha = new JPanel();
        derecha.setLayout(new BoxLayout(derecha, BoxLayout.Y_AXIS));
        derecha.setOpaque(false);
        derecha.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 15));

        lblHora = new JLabel();
        lblHora.setForeground(Color.WHITE);
        lblHora.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHora.setAlignmentX(Component.RIGHT_ALIGNMENT);

        lblFecha = new JLabel();
        lblFecha.setForeground(Color.WHITE);
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFecha.setAlignmentX(Component.RIGHT_ALIGNMENT);

        derecha.add(lblHora);
        derecha.add(lblFecha);

        barraTareas.add(derecha, BorderLayout.EAST);

        actualizarHora();
        Timer reloj = new Timer(1000, e -> actualizarHora());
        reloj.start();

        add(barraTareas, BorderLayout.SOUTH);
    }

    private void actualizarHora(){
        Date ahora = new Date();
        lblHora.setText(new SimpleDateFormat("HH:mm").format(ahora));
        lblFecha.setText(new SimpleDateFormat("dd/MM/yyyy").format(ahora));
    }

    private void crearIconosEjemplo(){
        crearIcono("Equipo", "💻", 0, 0);
        crearIcono("Archivos", "📁", 0, 1);
        crearIcono("Word", "📝", 0, 2);
        crearIcono("Música", "♫", 0, 3);
        crearIcono("CMD", "⌨", 1, 0);
        crearIcono("INSTA+","📷", 1, 1);
        btnUsuarios = crearIcono("Usuarios", "👥", 1, 2);
        btnUsuarios.setVisible(false);
    }

    private JButton crearIcono(String nombre, String simbolo, int columna, int fila){
        JButton icono = new JButton("<html><center><span style='font-size:30px'>" + simbolo + "</span><br>" + nombre + "</center></html>");

        icono.setForeground(Color.WHITE);
        icono.setOpaque(false);
        icono.setContentAreaFilled(false);
        icono.setBorderPainted(false);
        icono.setFocusPainted(false);

        int x = columna * anchoCelda;
        int y = fila * altoCelda;
        icono.setBounds(x, y, anchoCelda, altoCelda);

        permitirMover(icono);
        detectarClicks(icono, nombre);

        escritorio.add(icono, JLayeredPane.DEFAULT_LAYER);
        return icono;
    }

    private void detectarClicks(JButton icono, String nombre){
        icono.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                seleccionarIcono(icono);

                if(e.getClickCount() == 2){
                    abrirVentana(nombre);
                }
            }
        });
    }
    
    private void seleccionarIcono(JButton icono){
        if(iconoSeleccionado != null && iconoSeleccionado != icono){
            iconoSeleccionado.setContentAreaFilled(false);
            iconoSeleccionado.setBorderPainted(false);
            iconoSeleccionado.setOpaque(false);
        }

        iconoSeleccionado = icono;

        icono.setOpaque(true);
        icono.setContentAreaFilled(true);
        icono.setBackground(new Color(70, 120, 170));
        icono.setBorderPainted(true);
        icono.setBorder(BorderFactory.createLineBorder(new Color(150, 200, 255)));
    }

    private void abrirVentana(String nombre){
        JInternalFrame ventana = new JInternalFrame(nombre, true, true, true, true);
        ventana.setSize(500, 350);
        ventana.setLocation(150, 80);

        if(nombre.equals("CMD")){
            CMDPanel cmd = new CMDPanel();

            cmd.setAccionCerrar(() -> {
                ventana.dispose();
            });
            ventana.add(cmd);
        }
        else if(nombre.equals("Word")){
            EditorTextoPanel editor = new EditorTextoPanel();
            editor.setAccionCerrar(() ->{
                ventana.dispose();
            });
            ventana.add(editor);
        }
        else if(nombre.equals("Música")){
            ReproductorPanel musica = new ReproductorPanel();
            musica.setAccionCerrar(() ->{
                ventana.dispose();
            });
            ventana.add(musica);
        }
        else if(nombre.equals("Usuarios")){
            if(!Sesion.esAdministrador()){
                JOptionPane.showMessageDialog(this, "Solo el administrador puede administrar usuarios.", "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                ventana.dispose();
                return;
            }
            AdministrarUsuariosPanel usuarios =new AdministrarUsuariosPanel(gestorUsuarios);
            ventana.add(usuarios);
        }
        else{
            JPanel contenido = new JPanel(new BorderLayout());
            contenido.setBackground(Color.WHITE);

            JLabel titulo = new JLabel("Aplicación: " + nombre, SwingConstants.CENTER);
            titulo.setFont(new Font("Segoe UI", Font.PLAIN, 22));

            contenido.add(titulo, BorderLayout.CENTER);
            ventana.add(contenido);
        }

        escritorio.add(ventana, JLayeredPane.PALETTE_LAYER);
        crearBotonBarraTareas(ventana, nombre);
        activarAjusteVentana(ventana);

        ventana.setVisible(true);

        try{
            ventana.setSelected(true);
        }
        catch(java.beans.PropertyVetoException ex){
            ex.printStackTrace();
        }
    }

    private void crearBotonBarraTareas(JInternalFrame ventana, String nombre){
        JButton boton = new JButton(nombre);
        boton.setFocusPainted(false);

        panelVentanas.add(boton);
        panelVentanas.revalidate();
        panelVentanas.repaint();

        boton.addActionListener(e -> {
            try{
                if(ventana.isIcon()){
                    ventana.setIcon(false);
                }

                ventana.setVisible(true);
                ventana.setSelected(true);
                ventana.toFront();
            }
            catch(java.beans.PropertyVetoException ex){
                ex.printStackTrace();
            }
        });

        ventana.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter(){

            @Override
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent e){
                panelVentanas.remove(boton);
                panelVentanas.revalidate();
                panelVentanas.repaint();
            }

            @Override
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent e){
                boton.setText(nombre);
            }

            @Override
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent e){
                boton.setBackground(new Color(80, 80, 80));
            }

            @Override
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent e){
                boton.setBackground(null);
            }
        });
    }

    private void permitirMover(JButton icono){
        MouseAdapter mouse = new MouseAdapter(){
            int offsetX;
            int offsetY;

            @Override
            public void mousePressed(MouseEvent e){
                offsetX = e.getX();
                offsetY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e){
                int nuevoX = icono.getX() + e.getX() - offsetX;
                int nuevoY = icono.getY() + e.getY() - offsetY;

                icono.setLocation(nuevoX, nuevoY);
            }

            @Override
            public void mouseReleased(MouseEvent e){
                ajustarACuadricula(icono);
            }
        };

        icono.addMouseListener(mouse);
        icono.addMouseMotionListener(mouse);
    }

    private void ajustarACuadricula(JButton icono){
        int columna = Math.round((float) icono.getX() / anchoCelda);
        int fila = Math.round((float) icono.getY() / altoCelda);

        int x = columna * anchoCelda;
        int y = fila * altoCelda;

        if(x < 0) x = 0;
        if(y < 0) y = 0;

        if(x + anchoCelda > escritorio.getWidth()){
            x = escritorio.getWidth() - anchoCelda;
        }

        if(y + altoCelda > escritorio.getHeight()){
            y = escritorio.getHeight() - altoCelda;
        }

        icono.setLocation(x, y);
    }
    
    private void activarAjusteVentana(JInternalFrame ventana){
        final boolean[] ajustando = {false};

        Timer timer = new Timer(200, e -> {
            if(!ajustando[0]){
                ajustarVentanaAlBorde(ventana, ajustando);
            }
        });

        timer.setRepeats(false);

        ventana.addComponentListener(new java.awt.event.ComponentAdapter(){
            @Override
            public void componentMoved(java.awt.event.ComponentEvent e){
                if(!ajustando[0]){
                    timer.restart();
                }
            }
        });
    }

    private void ajustarVentanaAlBorde(JInternalFrame ventana, boolean[] ajustando){
        int margen = 15;
        int ancho = escritorio.getWidth();
        int alto = escritorio.getHeight();

        int x = ventana.getX();
        int derecha = x + ventana.getWidth();
        
        if(x <= margen){
            ajustando[0] = true;
            ventana.setBounds(0, 0, ancho / 2, alto);

            SwingUtilities.invokeLater(() -> {
                ajustando[0] = false;
            });
        }
        else if(derecha >= ancho - margen){
            ajustando[0] = true;
            ventana.setBounds(ancho / 2, 0, ancho / 2, alto);

            SwingUtilities.invokeLater(() -> {
                ajustando[0] = false;
            });
        }
    }
    
    public void actualizarPermisos(){
        if(btnUsuarios == null){
            return;
        }

        btnUsuarios.setVisible(
                Sesion.esAdministrador()
        );

        escritorio.repaint();
    }
}