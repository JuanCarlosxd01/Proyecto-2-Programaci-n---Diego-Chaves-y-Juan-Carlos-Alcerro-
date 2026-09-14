
package pokemon.interfaz;

import interfaz.DialogosWindows;

import pokemon.preparacion.Sesion;
import java.awt.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.Border;
import pokemon.memoria.Ataque;
import pokemon.memoria.Batalla;
import pokemon.memoria.Entrenador;
import pokemon.memoria.ListaEnlazada;
import pokemon.memoria.Objeto;
import pokemon.memoria.Pokemon;

public class BatallaPanel extends JPanel {

    private VentanaPokemon ventana;
    private JPanel campo;

    private JLabel lblPokemonJugador;
    private JLabel lblPokemonRival;
    private JLabel texto1;
    private JLabel texto2;

    private JLabel lblNombreJugador;
    private JLabel lblVidaJugador;
    private JLabel lblNivelJugador;

    private JLabel lblNombreRival;
    private JLabel lblVidaRival;
    private JLabel lblNivelRival;

    private JLabel lblMensaje;

    private JProgressBar barraJugador;
    private JProgressBar barraRival;

    private Batalla batalla;
    private static final int DELAY_TURNO_MS = 4000;
    private Timer temporizadorTurno;
    private JPanel panelOpciones;
    private boolean mostrandoTurno;

    public BatallaPanel(VentanaPokemon ventana) {
        this.ventana = ventana;
        setLayout(new BorderLayout());
        crearCampoBatalla();
    }

    public void prepararBatalla() {
        cancelarTemporizador();
        habilitarAcciones(true);
        batalla = Sesion.getInstancia().getBatallaActual();

        if (batalla == null) {
            DialogosWindows.showMessageDialog(this, "No existe una batalla preparada.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        actualizarBatalla();
    }

    private Font fuente(float tamaño) {
        return FuentePokemon.obtenerFuente(tamaño);
    }

    private void crearCampoBatalla() {
        campo = new FondoPokemonPanel();
        campo.setLayout(null);

        crearCajasInformacion();
        crearPokemon();
        crearInformacionPokemon();
        crearBotonHistorial();
        crearMenuInferior();

        add(campo, BorderLayout.CENTER);
    }

    private void crearCajasInformacion() {
        texto1 = cargarImagen("/Imagenes/texto1.png", 360, 115);
        texto2 = cargarImagen("/Imagenes/texto2.png", 360, 115);

        texto1.setBounds(25, 30, 360, 115);
        texto2.setBounds(650, 330, 360, 115);

        campo.add(texto1);
        campo.add(texto2);
    }

    private void crearPokemon() {
        lblPokemonRival = new JLabel();
        lblPokemonRival.setHorizontalAlignment(SwingConstants.CENTER);
        lblPokemonRival.setVerticalAlignment(SwingConstants.CENTER);
        lblPokemonRival.setBounds(680, 75, 280, 250);

        lblPokemonJugador = new JLabel();
        lblPokemonJugador.setHorizontalAlignment(SwingConstants.CENTER);
        lblPokemonJugador.setVerticalAlignment(SwingConstants.CENTER);
        lblPokemonJugador.setBounds(75, 285, 330, 280);

        campo.add(lblPokemonRival);
        campo.add(lblPokemonJugador);
    }

    private void crearInformacionPokemon() {
        lblNombreRival = new JLabel("POKÉMON");
        lblNombreRival.setFont(fuente(18f));
        lblNombreRival.setForeground(new Color(45, 45, 45));
        lblNombreRival.setBounds(80, 60, 200, 25);

        lblNivelRival = new JLabel("Lv. 0");
        lblNivelRival.setFont(fuente(14f));
        lblNivelRival.setForeground(new Color(45, 45, 45));
        lblNivelRival.setBounds(315, 60, 70, 25);

        JLabel hpRival = new JLabel("HP");
        hpRival.setFont(fuente(11f));
        hpRival.setForeground(new Color(45, 45, 45));
        hpRival.setBounds(100, 100, 40, 20);

        barraRival = crearBarraVida();
        barraRival.setBounds(145, 102, 215, 14);

        lblVidaRival = new JLabel("0 / 0");
        lblVidaRival.setFont(fuente(11f));
        lblVidaRival.setForeground(new Color(45, 45, 45));
        lblVidaRival.setBounds(275, 124, 90, 20);

        lblNombreJugador = new JLabel("POKÉMON");
        lblNombreJugador.setFont(fuente(18f));
        lblNombreJugador.setForeground(new Color(45, 45, 45));
        lblNombreJugador.setBounds(685, 365, 200, 25);

        lblNivelJugador = new JLabel("Lv. 0");
        lblNivelJugador.setFont(fuente(14f));
        lblNivelJugador.setForeground(new Color(45, 45, 45));
        lblNivelJugador.setBounds(920, 365, 70, 25);

        JLabel hpJugador = new JLabel("HP");
        hpJugador.setFont(fuente(11f));
        hpJugador.setForeground(new Color(45, 45, 45));
        hpJugador.setBounds(705, 405, 40, 20);

        barraJugador = crearBarraVida();
        barraJugador.setBounds(750, 407, 215, 14);

        lblVidaJugador = new JLabel("0 / 0");
        lblVidaJugador.setFont(fuente(11f));
        lblVidaJugador.setForeground(new Color(45, 45, 45));
        lblVidaJugador.setBounds(875, 428, 100, 20);

        campo.add(lblNombreRival);
        campo.add(lblNivelRival);
        campo.add(hpRival);
        campo.add(barraRival);
        campo.add(lblVidaRival);

        campo.add(lblNombreJugador);
        campo.add(lblNivelJugador);
        campo.add(hpJugador);
        campo.add(barraJugador);
        campo.add(lblVidaJugador);

        campo.setComponentZOrder(lblNombreRival, 0);
        campo.setComponentZOrder(lblNivelRival, 0);
        campo.setComponentZOrder(hpRival, 0);
        campo.setComponentZOrder(barraRival, 0);
        campo.setComponentZOrder(lblVidaRival, 0);

        campo.setComponentZOrder(lblNombreJugador, 0);
        campo.setComponentZOrder(lblNivelJugador, 0);
        campo.setComponentZOrder(hpJugador, 0);
        campo.setComponentZOrder(barraJugador, 0);
        campo.setComponentZOrder(lblVidaJugador, 0);
    }

    private JProgressBar crearBarraVida() {
        JProgressBar barra = new JProgressBar();
        barra.setBorderPainted(false);
        barra.setStringPainted(false);
        barra.setBackground(new Color(65, 75, 70));
        barra.setForeground(new Color(55, 190, 90));
        return barra;
    }

    private void crearBotonHistorial() {
        JButton btnHistorial = new JButton("HISTORIAL");
        btnHistorial.setFont(fuente(13f));
        btnHistorial.setFocusPainted(false);
        btnHistorial.setBackground(new Color(248, 246, 232));
        btnHistorial.setForeground(new Color(45, 45, 45));
        btnHistorial.setBounds(890, 20, 150, 35);
        btnHistorial.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHistorial.addActionListener(e -> mostrarHistorial());

        campo.add(btnHistorial);
        campo.setComponentZOrder(btnHistorial, 0);
    }

    private void crearMenuInferior() {
        JPanel panelInferior = new JPanel(null);
        panelInferior.setBounds(0, 515, 1085, 165);
        panelInferior.setBackground(new Color(65, 67, 78));

        JPanel panelMensaje = new JPanel(new BorderLayout());
        panelMensaje.setBounds(12, 10, 600, 145);
        panelMensaje.setBackground(new Color(105, 175, 180));

        Border bordeMensaje = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(195, 70, 65), 8), BorderFactory.createLineBorder(new Color(225, 225, 225), 4));
        panelMensaje.setBorder(bordeMensaje);

        lblMensaje = new JLabel("<html>SELECCIONA<br>TU ACCIÓN</html>");
        lblMensaje.setFont(fuente(21f));
        lblMensaje.setForeground(Color.WHITE);
        lblMensaje.setBorder(BorderFactory.createEmptyBorder(15, 25, 10, 10));

        panelMensaje.add(lblMensaje, BorderLayout.CENTER);

        panelOpciones = new JPanel(null);
        panelOpciones.setBounds(620, 10, 450, 145);
        panelOpciones.setBackground(new Color(248, 246, 232));

        Border bordeOpciones = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(75, 70, 95), 7), BorderFactory.createLineBorder(new Color(220, 220, 220), 4));
        panelOpciones.setBorder(bordeOpciones);

        JButton btnAtaque = crearBotonMenu("ATAQUE");
        JButton btnObjetos = crearBotonMenu("OBJETOS");
        JButton btnCambiar = crearBotonMenu("CAMBIAR");
        JButton btnEquipo = crearBotonMenu("MI EQUIPO");

        btnAtaque.setBounds(25, 20, 185, 45);
        btnObjetos.setBounds(235, 20, 185, 45);
        btnCambiar.setBounds(25, 80, 185, 45);
        btnEquipo.setBounds(235, 80, 185, 45);

        btnAtaque.addActionListener(e -> mostrarAtaques());
        btnObjetos.addActionListener(e -> mostrarObjetos());
        btnCambiar.addActionListener(e -> mostrarCambiarPokemon());
        btnEquipo.addActionListener(e -> mostrarEquipo());

        panelOpciones.add(btnAtaque);
        panelOpciones.add(btnObjetos);
        panelOpciones.add(btnCambiar);
        panelOpciones.add(btnEquipo);

        panelInferior.add(panelMensaje);
        panelInferior.add(panelOpciones);

        campo.add(panelInferior);
        campo.setComponentZOrder(panelInferior, 0);
    }

    private JButton crearBotonMenu(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(fuente(17f));
        boton.setForeground(new Color(45, 45, 45));
        boton.setBackground(new Color(248, 246, 232));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return boton;
    }

    private void actualizarBatalla() {
        if (batalla == null) {
            return;
        }

        Entrenador jugador = batalla.getJugador();
        Entrenador rival = batalla.getRival();

        Pokemon pokemonJugador = jugador.getActivo();
        Pokemon pokemonRival = rival.getActivo();

        if (pokemonJugador != null) {
            lblNombreJugador.setText(pokemonJugador.getNombre().toUpperCase());
            lblNivelJugador.setText("Lv. " + pokemonJugador.getNivel());
            lblVidaJugador.setText(pokemonJugador.getHp() + " / " + pokemonJugador.getHpMaximo());
            actualizarBarra(barraJugador, pokemonJugador);
            ponerSprite(lblPokemonJugador, pokemonJugador.getNombre(), true);
            lblMensaje.setFont(fuente(21f));
            lblMensaje.setText("<html>¿QUÉ HARÁ<br>" + pokemonJugador.getNombre().toUpperCase() + "?</html>");
        }

        if (pokemonRival != null) {
            lblNombreRival.setText(pokemonRival.getNombre().toUpperCase());
            lblNivelRival.setText("Lv. " + pokemonRival.getNivel());
            lblVidaRival.setText(pokemonRival.getHp() + " / " + pokemonRival.getHpMaximo());
            actualizarBarra(barraRival, pokemonRival);
            ponerSprite(lblPokemonRival, pokemonRival.getNombre(), false);
        }

        repaint();
        revalidate();
    }

    private void actualizarBarra(JProgressBar barra, Pokemon pokemon) {
        barra.setMinimum(0);
        barra.setMaximum(pokemon.getHpMaximo());
        barra.setValue(pokemon.getHp());

        double porcentaje = (double) pokemon.getHp() / pokemon.getHpMaximo();

        if (porcentaje <= 0.20) {
            barra.setForeground(new Color(210, 60, 55));
        } else if (porcentaje <= 0.50) {
            barra.setForeground(new Color(225, 180, 45));
        } else {
            barra.setForeground(new Color(55, 190, 90));
        }
    }

    private void mostrarAtaques() {
        if (!batallaDisponible()) {
            return;
        }

        Pokemon activo = batalla.getJugador().getActivo();
        JDialog dialogo = crearDialogo("ATAQUES", 520, 300);

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        principal.setBackground(new Color(248, 246, 232));

        JLabel titulo = new JLabel("ATAQUES DE " + activo.getNombre().toUpperCase(), SwingConstants.CENTER);
        titulo.setFont(fuente(18f));

        JPanel panelAtaques = new JPanel(new GridLayout(0, 1, 8, 8));
        panelAtaques.setBackground(new Color(248, 246, 232));

        for (int i = 0; i < activo.cantidadAtaques(); i++) {
            Ataque ataque = activo.getAtaque(i);
            int indice = i;
            String detalle;

            if (ataque.dano() > 0) {
                detalle = "   DAÑO: " + ataque.dano();
            } else {
                detalle = "   EFECTO: " + ataque.efecto();
            }

            JButton boton = crearBotonDialogo(ataque.nombre().toUpperCase() + detalle);

            boton.addActionListener(e -> {
                try {
                    int inicio = batalla.getHistorial().contar();
                    batalla.atacar(indice);
                    dialogo.dispose();
                    despuesDeAccion(inicio);
                } catch (Exception ex) {
                    DialogosWindows.showMessageDialog(dialogo, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            panelAtaques.add(boton);
        }

        principal.add(titulo, BorderLayout.NORTH);
        principal.add(panelAtaques, BorderLayout.CENTER);
        dialogo.add(principal);
        dialogo.setVisible(true);
    }

    private void mostrarCambiarPokemon() {
        if (!batallaDisponible()) {
            return;
        }

        Entrenador jugador = batalla.getJugador();
        JDialog dialogo = crearDialogo("SELECCIONAR POKÉMON", 520, 460);

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        principal.setBackground(new Color(248, 246, 232));

        JLabel titulo = new JLabel("SELECCIONAR POKÉMON", SwingConstants.CENTER);
        titulo.setFont(fuente(18f));

        JPanel listaPokemon = new JPanel();
        listaPokemon.setLayout(new BoxLayout(listaPokemon, BoxLayout.Y_AXIS));
        listaPokemon.setBackground(new Color(248, 246, 232));

        ButtonGroup grupo = new ButtonGroup();
        JRadioButton[] opciones = new JRadioButton[jugador.contar()];

        for (int i = 0; i < jugador.contar(); i++) {
            Pokemon pokemon = jugador.getPokemon(i);
            opciones[i] = crearOpcionPokemon(pokemon);
            opciones[i].setEnabled(!pokemon.estaDerrotado() && i != jugador.getIndiceActivo());
            grupo.add(opciones[i]);
            listaPokemon.add(opciones[i]);
            listaPokemon.add(Box.createVerticalStrut(6));
        }

        JButton btnCambiar = crearBotonDialogo("CAMBIAR");

        btnCambiar.addActionListener(e -> {
            int seleccionado = indiceSeleccionado(opciones);

            if (seleccionado < 0) {
                DialogosWindows.showMessageDialog(dialogo, "Selecciona otro Pokémon disponible.", "Cambio de Pokémon", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int inicio = batalla.getHistorial().contar();
                batalla.cambiarPokemon(seleccionado);
                dialogo.dispose();
                despuesDeAccion(inicio);
            } catch (Exception ex) {
                DialogosWindows.showMessageDialog(dialogo, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel panelBoton = new JPanel();
        panelBoton.setBackground(new Color(248, 246, 232));
        panelBoton.add(btnCambiar);

        JScrollPane scroll = new JScrollPane(listaPokemon);
        scroll.setBorder(null);

        principal.add(titulo, BorderLayout.NORTH);
        principal.add(scroll, BorderLayout.CENTER);
        principal.add(panelBoton, BorderLayout.SOUTH);

        dialogo.add(principal);
        dialogo.setVisible(true);
    }

    private int indiceSeleccionado(JRadioButton[] opciones) {
        for (int i = 0; i < opciones.length; i++) {
            if (opciones[i].isSelected()) {
                return i;
            }
        }

        return -1;
    }

    private JRadioButton crearOpcionPokemon(Pokemon pokemon) {
        String texto = pokemon.getNombre().toUpperCase() + "   Lv." + pokemon.getNivel() + "   " + pokemon.getHp() + "/" + pokemon.getHpMaximo();

        if (pokemon.estaDerrotado()) {
            texto += "   DERROTADO";
        }

        JRadioButton opcion = new JRadioButton(texto);
        opcion.setFont(fuente(14f));
        opcion.setBackground(new Color(248, 246, 232));
        opcion.setForeground(new Color(45, 45, 45));
        opcion.setIconTextGap(15);

        asignarIconoPokemon(opcion, pokemon.getNombre(), 55, 55);

        return opcion;
    }

    private void mostrarObjetos() {
        if (!batallaDisponible()) {
            return;
        }

        Entrenador jugador = batalla.getJugador();
        JDialog dialogo = crearDialogo("OBJETOS", 560, 450);

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        principal.setBackground(new Color(248, 246, 232));

        JLabel titulo = new JLabel("OBJETOS", SwingConstants.CENTER);
        titulo.setFont(fuente(18f));

        JPanel centro = new JPanel(new GridLayout(1, 2, 15, 0));
        centro.setBackground(new Color(248, 246, 232));

        JPanel panelObjetos = new JPanel();
        panelObjetos.setLayout(new BoxLayout(panelObjetos, BoxLayout.Y_AXIS));
        panelObjetos.setBackground(new Color(248, 246, 232));
        panelObjetos.setBorder(BorderFactory.createTitledBorder("Objeto"));

        JRadioButton pocion = crearOpcionObjeto("POCIÓN x" + jugador.cantidad(Objeto.POCION) + "   +20 HP");
        JRadioButton superPocion = crearOpcionObjeto("SUPERPOCIÓN x" + jugador.cantidad(Objeto.SUPERPOCION) + "   +50 HP");
        JRadioButton revivir = crearOpcionObjeto("REVIVIR x" + jugador.cantidad(Objeto.REVIVIR) + "   REVIVE AL 50%");

        ButtonGroup grupoObjetos = new ButtonGroup();
        grupoObjetos.add(pocion);
        grupoObjetos.add(superPocion);
        grupoObjetos.add(revivir);

        pocion.setEnabled(jugador.cantidad(Objeto.POCION) > 0);
        superPocion.setEnabled(jugador.cantidad(Objeto.SUPERPOCION) > 0);
        revivir.setEnabled(jugador.cantidad(Objeto.REVIVIR) > 0);

        panelObjetos.add(pocion);
        panelObjetos.add(Box.createVerticalStrut(15));
        panelObjetos.add(superPocion);
        panelObjetos.add(Box.createVerticalStrut(15));
        panelObjetos.add(revivir);

        JPanel panelPokemon = new JPanel();
        panelPokemon.setLayout(new BoxLayout(panelPokemon, BoxLayout.Y_AXIS));
        panelPokemon.setBackground(new Color(248, 246, 232));
        panelPokemon.setBorder(BorderFactory.createTitledBorder("Pokémon"));

        JRadioButton[] opcionesPokemon = new JRadioButton[jugador.contar()];
        ButtonGroup grupoPokemon = new ButtonGroup();

        for (int i = 0; i < jugador.contar(); i++) {
            Pokemon pokemon = jugador.getPokemon(i);
            String texto = pokemon.getNombre().toUpperCase() + " " + pokemon.getHp() + "/" + pokemon.getHpMaximo();

            if (pokemon.estaDerrotado()) {
                texto += " DERROTADO";
            }

            opcionesPokemon[i] = new JRadioButton(texto);
            opcionesPokemon[i].setFont(fuente(12f));
            opcionesPokemon[i].setBackground(new Color(248, 246, 232));

            grupoPokemon.add(opcionesPokemon[i]);
            panelPokemon.add(opcionesPokemon[i]);
        }

        centro.add(panelObjetos);
        centro.add(panelPokemon);

        JButton btnUtilizar = crearBotonDialogo("UTILIZAR");
        JButton btnCerrar = crearBotonDialogo("CERRAR");

        btnUtilizar.addActionListener(e -> {
            Objeto objeto = null;

            if (pocion.isSelected()) {
                objeto = Objeto.POCION;
            } else if (superPocion.isSelected()) {
                objeto = Objeto.SUPERPOCION;
            } else if (revivir.isSelected()) {
                objeto = Objeto.REVIVIR;
            }

            int indicePokemon = indiceSeleccionado(opcionesPokemon);

            if (objeto == null || indicePokemon < 0) {
                DialogosWindows.showMessageDialog(dialogo, "Selecciona un objeto y un Pokémon.", "Objeto", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int inicio = batalla.getHistorial().contar();
                batalla.usarObjeto(objeto, indicePokemon);
                dialogo.dispose();
                despuesDeAccion(inicio);
            } catch (Exception ex) {
                DialogosWindows.showMessageDialog(dialogo, ex.getMessage(), "No se puede utilizar el objeto", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCerrar.addActionListener(e -> dialogo.dispose());

        JPanel botones = new JPanel();
        botones.setBackground(new Color(248, 246, 232));
        botones.add(btnUtilizar);
        botones.add(btnCerrar);

        principal.add(titulo, BorderLayout.NORTH);
        principal.add(centro, BorderLayout.CENTER);
        principal.add(botones, BorderLayout.SOUTH);

        dialogo.add(principal);
        dialogo.setVisible(true);
    }

    private JRadioButton crearOpcionObjeto(String texto) {
        JRadioButton opcion = new JRadioButton(texto);
        opcion.setFont(fuente(13f));
        opcion.setBackground(new Color(248, 246, 232));
        opcion.setForeground(new Color(45, 45, 45));
        return opcion;
    }

    private void mostrarEquipo() {
        if (batalla == null) {
            DialogosWindows.showMessageDialog(this, "No hay una batalla activa.");
            return;
        }

        Entrenador jugador = batalla.getJugador();
        JDialog dialogo = crearDialogo("MI EQUIPO", 580, 470);

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        principal.setBackground(new Color(248, 246, 232));

        JLabel titulo = new JLabel("MI EQUIPO", SwingConstants.CENTER);
        titulo.setFont(fuente(20f));

        JPanel lista = new JPanel();
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setBackground(new Color(248, 246, 232));

        for (int i = 0; i < jugador.contar(); i++) {
            Pokemon pokemon = jugador.getPokemon(i);
            lista.add(crearFilaEquipo(pokemon, i == jugador.getIndiceActivo()));
            lista.add(Box.createVerticalStrut(6));
        }

        JLabel disponibles = new JLabel("POKÉMON DISPONIBLES: " + jugador.disponibles());
        disponibles.setFont(fuente(14f));

        JButton cerrar = crearBotonDialogo("CERRAR");
        cerrar.addActionListener(e -> dialogo.dispose());

        JPanel inferior = new JPanel(new BorderLayout());
        inferior.setBackground(new Color(248, 246, 232));
        inferior.add(disponibles, BorderLayout.WEST);
        inferior.add(cerrar, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(lista);
        scroll.setBorder(null);

        principal.add(titulo, BorderLayout.NORTH);
        principal.add(scroll, BorderLayout.CENTER);
        principal.add(inferior, BorderLayout.SOUTH);

        dialogo.add(principal);
        dialogo.setVisible(true);
    }

    private JPanel crearFilaEquipo(Pokemon pokemon, boolean activo) {
        JPanel fila = new JPanel(new BorderLayout(10, 5));
        fila.setBackground(new Color(248, 246, 232));

        JLabel imagen = new JLabel();
        asignarIconoPokemon(imagen, pokemon.getNombre(), 55, 55);

        String texto = pokemon.getNombre().toUpperCase() + "   Lv." + pokemon.getNivel() + "   " + pokemon.getTipo() + "   " + pokemon.getHp() + "/" + pokemon.getHpMaximo();

        if (pokemon.estaDerrotado()) {
            texto += "   DERROTADO";
        } else if (activo) {
            texto += "   ACTIVO";
        }

        JLabel informacion = new JLabel(texto);
        informacion.setFont(fuente(13f));

        fila.add(imagen, BorderLayout.WEST);
        fila.add(informacion, BorderLayout.CENTER);

        return fila;
    }

    private void mostrarHistorial() {
        if (batalla == null) {
            DialogosWindows.showMessageDialog(this, "Todavía no hay una batalla activa.");
            return;
        }

        JDialog dialogo = crearDialogo("HISTORIAL DE BATALLA", 560, 430);

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        principal.setBackground(new Color(248, 246, 232));

        JLabel titulo = new JLabel("HISTORIAL DE BATALLA", SwingConstants.CENTER);
        titulo.setFont(fuente(18f));

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(fuente(13f));
        area.setBackground(new Color(248, 246, 232));
        area.setForeground(new Color(45, 45, 45));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        ListaEnlazada<Batalla.Evento> historial = batalla.getHistorial();
        StringBuilder texto = new StringBuilder();

        for (Batalla.Evento evento : historial) {
            texto.append("Turno ");
            texto.append(evento.ronda());
            texto.append("\n");
            texto.append(evento.mensaje());
            texto.append("\n\n");
        }

        if (texto.length() == 0) {
            texto.append("Todavía no se han realizado acciones.");
        }

        area.setText(texto.toString());
        area.setCaretPosition(0);

        JButton cerrar = crearBotonDialogo("CERRAR");
        cerrar.addActionListener(e -> dialogo.dispose());

        JPanel inferior = new JPanel();
        inferior.setBackground(new Color(248, 246, 232));
        inferior.add(cerrar);

        principal.add(titulo, BorderLayout.NORTH);
        principal.add(new JScrollPane(area), BorderLayout.CENTER);
        principal.add(inferior, BorderLayout.SOUTH);

        dialogo.add(principal);
        dialogo.setVisible(true);
    }

    private void despuesDeAccion(int inicio) {
        actualizarBatalla();
        mostrarAccion(inicio, "JUGADOR");
        habilitarAcciones(false);

        Batalla actual = batalla;
        programarPaso(() -> {
            if (batalla != actual) {
                return;
            }
            if (!actual.isTurnoRivalPendiente()) {
                finalizarPresentacion();
                return;
            }

            int inicioRival = actual.getHistorial().contar();
            actual.ejecutarTurnoRival();
            actualizarBatalla();
            mostrarAccion(inicioRival, "CPU");
            programarPaso(this::finalizarPresentacion);
        });
    }

    private void programarPaso(Runnable paso) {
        cancelarTemporizador();
        temporizadorTurno = new Timer(DELAY_TURNO_MS, evento -> {
            if (evento.getSource() != temporizadorTurno) {
                return;
            }
            temporizadorTurno = null;
            paso.run();
        });
        temporizadorTurno.setRepeats(false);
        temporizadorTurno.start();
    }

    private void cancelarTemporizador() {
        if (temporizadorTurno != null) {
            temporizadorTurno.stop();
            temporizadorTurno = null;
        }
    }

    private void habilitarAcciones(boolean habilitar) {
        mostrandoTurno = !habilitar;
        for (Component componente : panelOpciones.getComponents()) {
            componente.setEnabled(habilitar);
        }
    }

    private void mostrarAccion(int inicio, String lado) {
        ListaEnlazada<Batalla.Evento> eventos = batalla.getHistorial();
        if (inicio >= eventos.contar()) {
            return;
        }

        String mensaje = eventos.obtener(inicio).mensaje();
        for (int i = inicio + 1; i < eventos.contar(); i++) {
            String detalle = eventos.obtener(i).mensaje();
            if (detalle.contains(" recibe ")) {
                mensaje += " " + detalle;
                break;
            }
        }

        mensaje = mensaje.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        lblMensaje.setFont(fuente(14f));
        lblMensaje.setText("<html>" + lado + "<br>" + mensaje + "</html>");
    }

    private void finalizarPresentacion() {
        habilitarAcciones(batalla.getResultado() == Batalla.Resultado.EN_CURSO);
        mostrarResultadoSiTermino();
    }

    private void mostrarResultadoSiTermino() {
        actualizarBatalla();

        if (batalla.getResultado() == Batalla.Resultado.EN_CURSO) {
            return;
        }

        Batalla.Estadisticas estadisticas = batalla.getEstadisticas();
        String resultado;

        if (batalla.getResultado() == Batalla.Resultado.VICTORIA) {
            resultado = "¡GANASTE LA BATALLA!";
        } else {
            resultado = "HAS PERDIDO LA BATALLA";
        }

        String mensaje = resultado + "\n\nRondas: " + estadisticas.rondas() + "\nDaño causado: " + estadisticas.danoJugador() + "\nDaño recibido: " + estadisticas.danoRival() + "\nObjetos usados: " + estadisticas.objetosJugador() + "\nPokémon rivales derrotados: " + estadisticas.derrotadosRival();

        Object[] opciones = {"REINICIAR", "NUEVO EQUIPO", "CERRAR"};

        int opcion = DialogosWindows.showOptionDialog(this, mensaje, "FIN DE LA BATALLA", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opciones, opciones[0]);

        if (opcion == 0) {
            batalla.reiniciar();
            habilitarAcciones(true);
            actualizarBatalla();
        } else if (opcion == 1) {
            Sesion.getInstancia().limpiarBatalla();
            ventana.mostrarSeleccion();
        }
    }

    private boolean batallaDisponible() {
        if (mostrandoTurno) {
            return false;
        }
        if (batalla == null) {
            DialogosWindows.showMessageDialog(this, "No hay una batalla activa.");
            return false;
        }

        if (batalla.getResultado() != Batalla.Resultado.EN_CURSO) {
            DialogosWindows.showMessageDialog(this, "La batalla ya terminó.");
            return false;
        }

        return true;
    }

    private JDialog crearDialogo(String titulo, int ancho, int alto) {
        Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
        JDialog dialogo = new JDialog(ventanaPadre, titulo, Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setSize(ancho, alto);
        dialogo.setLocationRelativeTo(this);
        dialogo.setResizable(false);
        return dialogo;
    }

    private JButton crearBotonDialogo(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(fuente(14f));
        boton.setBackground(new Color(248, 246, 232));
        boton.setForeground(new Color(45, 45, 45));
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return boton;
    }

    private JLabel cargarImagen(String ruta, int ancho, int alto) {
        JLabel label = new JLabel();
        URL recurso = BatallaPanel.class.getResource(ruta);

        if (recurso == null) {
            System.out.println("No se encontró: " + ruta);
            return label;
        }

        ImageIcon icono = new ImageIcon(recurso);
        Image imagen = icono.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        label.setIcon(new ImageIcon(imagen));

        return label;
    }

    private void ponerSprite(JLabel label, String nombre, boolean espalda) {
        int id = idPokemon(nombre);
        String ruta;

        if (espalda) {
            ruta = "/Imagenes/" + id + " back.png";
        } else {
            ruta = "/Imagenes/" + id + ".png";
        }

        URL recurso = BatallaPanel.class.getResource(ruta);

        if (recurso == null) {
            label.setIcon(null);
            label.setText("NO ENCONTRADA");
            return;
        }

        label.setText("");

        ImageIcon icono = new ImageIcon(recurso);
        Image imagen = icono.getImage().getScaledInstance(240, 240, Image.SCALE_FAST);
        label.setIcon(new ImageIcon(imagen));
    }

    private void asignarIconoPokemon(AbstractButton componente, String nombre, int ancho, int alto) {
        URL recurso = BatallaPanel.class.getResource("/Imagenes/" + idPokemon(nombre) + ".png");

        if (recurso != null) {
            ImageIcon icono = new ImageIcon(recurso);
            Image imagen = icono.getImage().getScaledInstance(ancho, alto, Image.SCALE_FAST);
            componente.setIcon(new ImageIcon(imagen));
        }
    }

    private void asignarIconoPokemon(JLabel componente, String nombre, int ancho, int alto) {
        URL recurso = BatallaPanel.class.getResource("/Imagenes/" + idPokemon(nombre) + ".png");

        if (recurso != null) {
            ImageIcon icono = new ImageIcon(recurso);
            Image imagen = icono.getImage().getScaledInstance(ancho, alto, Image.SCALE_FAST);
            componente.setIcon(new ImageIcon(imagen));
        }
    }

    private int idPokemon(String nombre) {
        switch (nombre.toLowerCase()) {
            case "bulbasaur":
                return 1;
            case "charmander":
                return 4;
            case "squirtle":
                return 7;
            case "pikachu":
                return 25;
            case "vulpix":
                return 37;
            case "psyduck":
                return 54;
            case "geodude":
                return 74;
            case "gastly":
                return 92;
            case "chikorita":
                return 152;
            case "mareep":
                return 179;
            default:
                return 25;
        }
    }
}
