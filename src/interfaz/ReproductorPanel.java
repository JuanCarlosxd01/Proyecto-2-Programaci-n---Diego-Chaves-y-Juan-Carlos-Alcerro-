
package interfaz;

import java.awt.*;
import javax.swing.*;

public class ReproductorPanel extends JPanel{
    private JLabel lblCaratula;
    private JLabel lblCancion;
    private JLabel lblDescripcion;

    private JButton btnPlay;
    private JButton btnPause;
    private JButton btnStop;

    private JSlider progreso;
    private Runnable accionCerrar;

    private JList<String> listaCanciones;
    private DefaultListModel<String> modeloCanciones;

    public ReproductorPanel() {

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(
                30, 50, 30, 50
        ));

        crearInformacionCancion();
        crearControles();
        crearListaCanciones();
    }

    private void crearInformacionCancion() {

        JPanel panel = new JPanel();
        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        lblCaratula = new JLabel("CARÁTULA");
        lblCaratula.setHorizontalAlignment(SwingConstants.CENTER);
        lblCaratula.setPreferredSize(new Dimension(250, 250));
        lblCaratula.setMaximumSize(new Dimension(250, 250));

        lblCaratula.setBorder(
                BorderFactory.createLineBorder(Color.GRAY)
        );

        lblCaratula.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblCancion = new JLabel("Ninguna canción seleccionada");
        lblCancion.setFont(
                new Font("Arial", Font.BOLD, 22)
        );
        lblCancion.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblDescripcion = new JLabel("Descripción");
        lblDescripcion.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(lblCaratula);
        panel.add(Box.createVerticalStrut(20));
        panel.add(lblCancion);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblDescripcion);

        add(panel, BorderLayout.CENTER);
    }

    private void crearControles() {

        JPanel panel = new JPanel();
        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        progreso = new JSlider();

        progreso.setMinimum(0);
        progreso.setMaximum(100);
        progreso.setValue(0);

        JPanel botones = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 20, 10)
        );

        btnPlay = new JButton("▶ Play");
        btnPause = new JButton("⏸ Pause");
        btnStop = new JButton("■ Stop");

        botones.add(btnPlay);
        botones.add(btnPause);
        botones.add(btnStop);

        panel.add(progreso);
        panel.add(botones);

        add(panel, BorderLayout.SOUTH);
    }

    private void crearListaCanciones() {

        JPanel panel = new JPanel(new BorderLayout());

        JLabel titulo = new JLabel("Lista de canciones");

        titulo.setFont(
                new Font("Arial", Font.BOLD, 18)
        );

        modeloCanciones = new DefaultListModel<>();

        listaCanciones = new JList<>(modeloCanciones);

        JScrollPane scroll =
                new JScrollPane(listaCanciones);

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        panel.setPreferredSize(
                new Dimension(300, 0)
        );

        add(panel, BorderLayout.EAST);
    }

    public JButton getBtnPlay() {
        return btnPlay;
    }

    public JButton getBtnPause() {
        return btnPause;
    }

    public JButton getBtnStop() {
        return btnStop;
    }

    public JList<String> getListaCanciones() {
        return listaCanciones;
    }

    public DefaultListModel<String> getModeloCanciones() {
        return modeloCanciones;
    }

    public JLabel getLblCaratula() {
        return lblCaratula;
    }

    public JLabel getLblCancion() {
        return lblCancion;
    }

    public JLabel getLblDescripcion() {
        return lblDescripcion;
    }

    public JSlider getProgreso() {
        return progreso;
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
