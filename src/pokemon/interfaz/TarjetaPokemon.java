
package pokemon.interfaz;

import java.awt.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class TarjetaPokemon extends JPanel {

    private int id;
    private String nombre;
    private String tipo;
    private boolean seleccionado;
    private Runnable accionSeleccion;

    private JLabel lblImagen;
    private JLabel lblNombre;
    private JLabel lblTipo;
    private JButton btnAgregar;

    public TarjetaPokemon(int id, String nombre, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.seleccionado = false;

        configurarPanel();
        crearContenido();
    }

    private void configurarPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(170, 210));
        setBackground(Color.WHITE);
        setBorder(new LineBorder(new Color(50, 90, 70), 3));
    }

    private void crearContenido() {
        lblImagen = new JLabel();
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagen.setVerticalAlignment(SwingConstants.CENTER);

        cargarImagen();

        lblNombre = new JLabel(nombre.toUpperCase(), SwingConstants.CENTER);
        lblNombre.setFont(new Font("Arial", Font.BOLD, 15));

        lblTipo = new JLabel(tipo, SwingConstants.CENTER);
        lblTipo.setFont(new Font("Arial", Font.PLAIN, 13));

        btnAgregar = new JButton("AGREGAR");
        btnAgregar.setFocusPainted(false);
        btnAgregar.addActionListener(e -> cambiarSeleccion());

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTipo.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAgregar.setAlignmentX(Component.CENTER_ALIGNMENT);

        info.add(lblNombre);
        info.add(lblTipo);
        info.add(Box.createVerticalStrut(5));
        info.add(btnAgregar);
        info.add(Box.createVerticalStrut(5));

        add(lblImagen, BorderLayout.CENTER);
        add(info, BorderLayout.SOUTH);
    }

    private void cargarImagen() {
        String ruta = "/Imagenes/" + id + ".png";
        URL recurso = TarjetaPokemon.class.getResource(ruta);

        System.out.println("Buscando imagen: " + ruta);
        System.out.println("Resultado: " + recurso);

        if (recurso == null) {
            lblImagen.setText("No encontrada");
            lblImagen.setForeground(Color.RED);
            return;
        }

        ImageIcon iconoOriginal = new ImageIcon(recurso);
        Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(120, 120, Image.SCALE_FAST);
        ImageIcon iconoEscalado = new ImageIcon(imagenEscalada);

        lblImagen.setText("");
        lblImagen.setIcon(iconoEscalado);
    }

    private void cambiarSeleccion() {
        seleccionado = !seleccionado;

        actualizarApariencia();

        if (accionSeleccion != null) {
            accionSeleccion.run();
        }
    }

    private void actualizarApariencia() {
        if (seleccionado) {
            setBackground(new Color(255, 244, 190));
            setBorder(new LineBorder(new Color(230, 170, 30), 5));
            btnAgregar.setText("QUITAR");
        } else {
            setBackground(Color.WHITE);
            setBorder(new LineBorder(new Color(50, 90, 70), 3));
            btnAgregar.setText("AGREGAR");
        }
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
        actualizarApariencia();
    }

    public void setAccionSeleccion(Runnable accionSeleccion) {
        this.accionSeleccion = accionSeleccion;
    }

    public boolean isSeleccionado() {
        return seleccionado;
    }

    public String getNombrePokemon() {
        return nombre;
    }

    public int getIdPokemon() {
        return id;
    }
}