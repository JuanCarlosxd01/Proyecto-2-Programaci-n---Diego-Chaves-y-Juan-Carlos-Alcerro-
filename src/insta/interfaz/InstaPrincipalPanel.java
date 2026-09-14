

package insta.interfaz;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import estructuras.ListaEnlazada;
import insta.modelo.*;
import red.*;

public class InstaPrincipalPanel extends JPanel {

    private Cliente cliente;
    private final JPanel contenido = new JPanel(new BorderLayout(12, 12));
    private final JPanel menu = new JPanel();
    private final JLabel estado = new JLabel("INSTA+ · Tu espacio, tus conexiones");
    private final DateTimeFormatter fecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private hilos.HiloNotificaciones notificaciones;
    private volatile long vista;
    private volatile boolean cerrado;
    private boolean activa = true;
    public boolean MODO_MOBILE;

    private String usuario = "";
    private String contacto = "";
    private JPanel chat;
    private JTextField mensaje;
    private JButton btnInbox;
    private DefaultListModel<String> contactos;
    private JList<String> listaContactos;
    private String host = System.getProperty("insta.host", "127.0.0.1");
    private int puerto = Integer.getInteger("insta.puerto", 5050);

    public InstaPrincipalPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setBackground(new Color(237, 242, 248));
        contenido.setBackground(Color.WHITE);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setOpaque(false);

        add(menu, BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                MODO_MOBILE = getWidth() < 850;
                menu.setPreferredSize(new Dimension(MODO_MOBILE ? 115 : 170, 0));
                revalidate();
            }
        });
        login();
    }

    private JButton boton(String texto, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.setFocusPainted(false);
        boton.addActionListener(e -> accion.run());
        return boton;
    }

    private static class Columna extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(650, 500);
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
            return 20;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
            return Math.max(20, r.height - 30);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private JPanel columna() {
        JPanel panel = new Columna();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private void mostrar(String nombre, JComponent panel) {
        vista++;
        contenido.removeAll();

        JLabel titulo = new JLabel(nombre);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));

        contenido.add(titulo, BorderLayout.NORTH);
        contenido.add(panel, BorderLayout.CENTER);
        contenido.revalidate();
        contenido.repaint();
        aviso("INSTA+ · " + nombre);
    }

    private JPanel formulario() {
        JPanel panel = new Columna();
        panel.setLayout(new GridLayout(0, 2, 8, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        return panel;
    }

    private void campo(JPanel panel, String nombre, JComponent valor) {
        panel.add(new JLabel(nombre));
        panel.add(valor);
    }

    private void aviso(String texto) {
        estado.setText(texto);
    }

    private <T> void tarea(Callable<T> trabajo, Consumer<T> terminado) {
        long actual = vista;

        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                if (cerrado || actual != vista) {
                    throw new java.util.concurrent.CancellationException();
                }
                return trabajo.call();
            }

            @Override
            protected void done() {
                if (cerrado || actual != vista) return;
                try {
                    terminado.accept(get());
                } catch (Exception e) {
                    Throwable causa = e;
                    while (causa.getCause() != null) causa = causa.getCause();
                    aviso("No se pudo completar: " + causa.getMessage());
                }
            }
        }.execute();
    }

    private Respuesta exigir(Respuesta respuesta) throws IOException {
        if (!respuesta.esExitosa()) throw new IOException(respuesta.getMensaje());
        return respuesta;
    }

    private interface Pagina {
        Respuesta leer(int desde) throws Exception;
    }

    private void consultar(Pagina pagina, Consumer<Respuesta> terminado) {
        long paginaVista = vista;

        tarea(() -> {
            ListaEnlazada<Object> todos = new ListaEnlazada<>();
            int desde = 0;
            Respuesta respuesta;

            do {
                if (cerrado || paginaVista != vista) {
                    throw new java.util.concurrent.CancellationException();
                }

                respuesta = exigir(pagina.leer(desde));
                var elementos = respuesta.getElementos(Object.class);

                for (Object elemento : elementos) todos.agregar(elemento);
                desde += elementos.size();
                if (elementos.isEmpty()) break;
            } while (desde < respuesta.getTotal());

            return respuesta.conLista(todos).conTotal(todos.size());
        }, terminado);
    }

    private void imagen(JLabel label, String ruta, int ancho, int alto) {
        Cliente actual = cliente;
        ImagenTemporal.cargar(
                label,
                () -> exigir(actual.descargarImagen(ruta)).getArchivo(),
                ancho, alto
        );
    }

    private void elegirFoto(Consumer<byte[]> recibir) {
        JFileChooser selector = new JFileChooser();
        selector.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter(
                        "PNG o JPG", "png", "jpg"
                )
        );
        selector.setAcceptAllFileFilterUsed(false);

        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File archivo = selector.getSelectedFile();

        if (!archivo.getName().toLowerCase(java.util.Locale.ROOT)
                .matches(".*\\.(png|jpg)$")) {
            aviso("Selecciona un archivo .png o .jpg.");
            return;
        }

        tarea(() -> {
            sistema.SeguridadArchivos.importar(archivo);
            if (Files.size(archivo.toPath()) > 4 * 1024 * 1024) {
                throw new IOException("La imagen supera 4 MB.");
            }
            return insta.servicio.ImagenesInsta.validar(
                    Files.readAllBytes(archivo.toPath())
            );
        }, recibir);
    }

    private void login() {
        detenerNotificaciones();
        menu.removeAll();
        usuario = "";

        JPanel panel = formulario();
        JTextField user = new JTextField();
        JTextField server = new JTextField(host);
        JTextField port = new JTextField(String.valueOf(puerto));
        JPasswordField pass = new JPasswordField();

        campo(panel, "Username", user);
        campo(panel, "Contraseña", pass);
        campo(panel, "Servidor", server);
        campo(panel, "Puerto", port);

        JButton ingresar = new JButton("Ingresar");
        panel.add(ingresar);
        panel.add(boton("Crear cuenta", this::registro));
        mostrar("Bienvenido a INSTA+", new JScrollPane(panel));

        ingresar.addActionListener(e -> {
            try {
                host = server.getText().strip();
                puerto = Integer.parseInt(port.getText());
                if (puerto < 1 || puerto > 65535 || host.isBlank()) {
                    throw new IllegalArgumentException();
                }
            } catch (Exception error) {
                aviso("Indica un servidor y puerto válidos.");
                return;
            }

            String username = user.getText().strip();
            String clave = new String(pass.getPassword());
            if (!ingresar.isEnabled()) return;

            cliente = new Cliente(host, puerto);
            Cliente actual = cliente;
            ingresar.setEnabled(false);

            tarea(() -> {
                try {
                    return actual.iniciarSesion(username, clave);
                } finally {
                    SwingUtilities.invokeLater(() -> ingresar.setEnabled(true));
                }
            }, respuesta -> {
                if (respuesta.esExitosa()) {
                    usuario = respuesta.getUsuario().getUsername();
                    activa = respuesta.getUsuario().estaActiva();
                    navegacion();

                    if (activa) {
                        iniciarNotificaciones();
                        timeline();
                    } else {
                        editar();
                    }
                } else {
                    Object[] opciones = {"Reintentar", "Crear cuenta"};
                    int opcion = JOptionPane.showOptionDialog(
                            this, respuesta.getMensaje(), "Inicio de sesión",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                            null, opciones, opciones[0]
                    );
                    if (opcion == 1) registro();
                }
            });
        });

        pass.addActionListener(e -> ingresar.doClick());
    }

    private void registro() {
        JPanel panel = formulario();
        JTextField nombre = new JTextField();
        JTextField user = new JTextField();
        JPasswordField pass = new JPasswordField();
        JComboBox<String> genero = new JComboBox<>(new String[]{"M", "F"});
        JSpinner edad = new JSpinner(new SpinnerNumberModel(18, 1, 120, 1));
        byte[][] foto = {null};
        JLabel preview = new JLabel("Foto de perfil");

        campo(panel, "Nombre completo", nombre);
        campo(panel, "Username", user);
        campo(panel, "Contraseña", pass);
        campo(panel, "Género", genero);
        campo(panel, "Edad", edad);

        panel.add(preview);
        panel.add(boton("Seleccionar foto", () -> elegirFoto(bytes -> {
            foto[0] = bytes;
            ImagenTemporal.cargar(preview, () -> bytes, 100, 100);
        })));

        JButton crear = new JButton("Crear cuenta");
        panel.add(crear);
        panel.add(boton("Volver", this::login));
        mostrar("Crear cuenta", new JScrollPane(panel));

        crear.addActionListener(e -> {
            String n = nombre.getText();
            String u = user.getText();
            String pw = new String(pass.getPassword());
            char g = genero.getSelectedItem().toString().charAt(0);
            int a = (Integer) edad.getValue();
            byte[] f = foto[0];
            crear.setEnabled(false);

            tarea(() -> {
                try (Cliente actual = new Cliente(host, puerto)) {
                    return actual.registrarUsuario(n, g, u, pw, a, f);
                } finally {
                    SwingUtilities.invokeLater(() -> crear.setEnabled(true));
                }
            }, respuesta -> {
                if (respuesta.esExitosa()) {
                    login();
                    aviso("Cuenta creada. Ya puedes ingresar.");
                } else {
                    aviso(respuesta.getMensaje());
                }
            });
        });
    }

    private void navegacion() {
        menu.removeAll();
        JLabel logo = new JLabel("INSTA+");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        menu.add(logo);
        menu.add(new JLabel("@" + usuario));

        String[] nombres = {
            "Perfil", "Cargar imágenes", "Timeline", "Interacciones",
            "Buscar Profile", "Buscar Hashtag", "Inbox",
            "Editar perfil", "Cerrar sesión"
        };

        Runnable[] acciones = {
            () -> perfil(usuario),
            this::publicar,
            this::timeline,
            () -> feed("Interacciones", d -> cliente.menciones(d)),
            () -> buscar(false),
            () -> buscar(true),
            this::inbox,
            this::editar,
            this::salir
        };

        for (int i = 0; i < nombres.length; i++) {
            final int indice = i;
            JButton boton = boton(nombres[i], () -> {
                if (!activa && indice != 7 && indice != 8 && indice != 0) {
                    aviso("Reactiva tu cuenta desde Editar perfil.");
                    return;
                }
                acciones[indice].run();
            });

            boton.setMaximumSize(new Dimension(180, 42));
            menu.add(Box.createVerticalStrut(8));
            menu.add(boton);
            if (i == 6) btnInbox = boton;
        }

        menu.revalidate();
        menu.repaint();
    }

    private void iniciarNotificaciones() {
        detenerNotificaciones();
        Cliente actual = cliente;
        String cuenta = usuario;

        notificaciones = new hilos.HiloNotificaciones(
                actual,
                nuevos -> SwingUtilities.invokeLater(() -> {
                    if (cerrado || !cuenta.equals(usuario) || !activa) return;
                    btnInbox.setText("Inbox · nuevos");
                    aviso("Recibiste " + nuevos.size()
                            + " mensaje(s). Abre Inbox para leerlos.");
                }),
                error -> SwingUtilities.invokeLater(() -> {
                    if (!cerrado && cuenta.equals(usuario)) {
                        aviso("No se pudieron revisar mensajes: " + error.getMessage());
                    }
                })
        );
        notificaciones.iniciar();
    }

    private void detenerNotificaciones() {
        if (notificaciones != null) {
            notificaciones.close();
            notificaciones = null;
        }
    }

    private void timeline() {
        feed("Timeline", d -> cliente.timeline(d));
    }

    private void feed(String titulo, Pagina consulta) {
        JPanel panel = columna();
        mostrar(titulo, new JScrollPane(panel));
        consultar(consulta, respuesta -> pintarPublicaciones(panel, respuesta));
    }

    private void pintarPublicaciones(JPanel panel, Respuesta respuesta) {
        panel.removeAll();
        for (Publicacion publicacion : respuesta.getPublicaciones()) {
            panel.add(tarjeta(publicacion));
        }
        if (respuesta.getPublicaciones().isEmpty()) {
            panel.add(new JLabel("No hay publicaciones."));
        }
        panel.revalidate();
        panel.repaint();
    }

    private JPanel tarjeta(Publicacion publicacion) {
        JPanel panel = columna();
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(212, 222, 232)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        panel.add(boton(
                publicacion.getAutor() + " escribió:",
                () -> perfil(publicacion.getAutor())
        ));

        JTextArea texto = new JTextArea(publicacion.getContenido());
        texto.setLineWrap(true);
        texto.setWrapStyleWord(true);
        texto.setEditable(false);
        texto.setOpaque(false);
        panel.add(texto);

        if (publicacion.tieneAdjunto()) {
            JLabel foto = new JLabel("Cargando...", SwingConstants.CENTER);
            foto.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(foto);
            imagen(
                    foto, publicacion.getRutaAdjunto(),
                    600, publicacion.esSticker() ? 250 : 750
            );
        }

        panel.add(new JLabel(fecha.format(publicacion.getFechaPublicacion())));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1100));
        return panel;
    }

    private void perfil(String nombre) {
        JPanel panel = columna();
        mostrar("Perfil · @" + nombre, new JScrollPane(panel));
        Cliente actual = cliente;

        tarea(() -> exigir(actual.perfil(nombre)), respuesta -> {
            var u = respuesta.getUsuario();
            JLabel foto = new JLabel("Foto de perfil");
            foto.setPreferredSize(new Dimension(150, 150));
            panel.add(foto);

            if (u.estaActiva()) imagen(foto, u.getRutaFotoPerfil(), 150, 150);

            panel.add(new JLabel(u.getNombreCompleto() + " · @" + u.getUsername()));
            panel.add(new JLabel("Edad: " + u.getEdad() + " · Género: " + u.getGenero()));
            panel.add(new JLabel("Registro: " + fecha.format(u.getFechaRegistro())));
            panel.add(new JLabel("Estado: " + (u.estaActiva() ? "Activa" : "Inactiva")));

            JPanel contadores = new JPanel();
            contadores.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            contadores.add(boton(
                    u.getFollowers() + " followers",
                    () -> personas("Followers", d -> actual.seguidores(nombre, d))
            ));
            contadores.add(boton(
                    u.getFollowing() + " following",
                    () -> personas("Following", d -> actual.seguidos(nombre, d))
            ));
            contadores.add(new JLabel(u.getPublicaciones() + " publicaciones"));
            panel.add(contadores);

            if (!nombre.equals(usuario)) {
                panel.add(boton(
                        u.loSigo() ? "Dejar de seguir" : "Seguir",
                        () -> seguir(u, () -> perfil(nombre))
                ));
            }

            if (u.estaActiva()) {
                panel.add(boton(
                        "Ver sus publicaciones",
                        () -> feed(
                                "Publicaciones · @" + nombre,
                                d -> actual.publicaciones(nombre, d)
                        )
                ));

                JPanel grid = new JPanel(new GridLayout(0, 3, 8, 8));
                panel.add(grid);

                consultar(d -> actual.publicaciones(nombre, d), resultado -> {
                    for (Publicacion publicacion : resultado.getPublicaciones()) {
                        JPanel celda = columna();

                        if (publicacion.tieneAdjunto()) {
                            JLabel img = new JLabel("Imagen");
                            celda.add(img);
                            imagen(img, publicacion.getRutaAdjunto(), 220, 275);
                        }

                        celda.add(boton(
                                publicacion.esTexto()
                                        ? publicacion.getContenido()
                                        : "Abrir publicación",
                                () -> {
                                    JPanel detalle = columna();
                                    detalle.add(boton(
                                            "Volver al perfil", () -> perfil(nombre)
                                    ));
                                    detalle.add(tarjeta(publicacion));
                                    mostrar("Publicación", new JScrollPane(detalle));
                                }
                        ));
                        grid.add(celda);
                    }
                    panel.revalidate();
                    panel.repaint();
                });
            }

            panel.revalidate();
            panel.repaint();
        });
    }

    private void seguir(Respuesta.DatosUsuario u, Runnable refrescar) {
        if (u.loSigo() && JOptionPane.showConfirmDialog(
                this, "¿Dejar de seguir a @" + u.getUsername() + "?",
                "Seguimiento", JOptionPane.YES_NO_OPTION
        ) != JOptionPane.YES_OPTION) return;

        Cliente actual = cliente;
        tarea(() -> exigir(
                u.loSigo()
                        ? actual.dejarDeSeguir(u.getUsername())
                        : actual.seguir(u.getUsername())
        ), respuesta -> refrescar.run());
    }

    private void personas(String titulo, Pagina consulta) {
        JPanel panel = columna();
        mostrar(titulo, new JScrollPane(panel));
        consultar(consulta, respuesta -> pintarPersonas(
                panel, respuesta, () -> personas(titulo, consulta)
        ));
    }

    private void pintarPersonas(
            JPanel panel, Respuesta respuesta, Runnable refrescar
    ) {
        panel.removeAll();

        for (var u : respuesta.getUsuarios()) {
            JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT));
            fila.add(boton("@" + u.getUsername(), () -> perfil(u.getUsername())));
            fila.add(new JLabel(u.loSigo() ? "Lo sigo" : "No lo sigues"));

            if (!u.getUsername().equals(usuario)) {
                fila.add(boton(
                        u.loSigo() ? "Dejar de seguir" : "Seguir",
                        () -> seguir(u, refrescar)
                ));
            }
            panel.add(fila);
        }

        if (respuesta.getUsuarios().isEmpty()) panel.add(new JLabel("Sin resultados."));
        panel.revalidate();
        panel.repaint();
    }

    private void buscar(boolean hashtag) {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel fila = new JPanel(new BorderLayout());
        JPanel resultados = columna();
        JTextField texto = new JTextField();

        fila.add(texto, BorderLayout.CENTER);

        Runnable accion = () -> {
            String valor = texto.getText().strip();
            consultar(
                    d -> hashtag
                            ? cliente.buscarHashtag(valor, d)
                            : cliente.buscarPersonas(valor, d),
                    respuesta -> {
                        if (hashtag) {
                            pintarPublicaciones(resultados, respuesta);
                        } else {
                            pintarPersonas(
                                    resultados, respuesta, texto::postActionEvent
                            );
                        }
                    }
            );
        };

        fila.add(boton("Buscar", accion), BorderLayout.EAST);
        texto.addActionListener(e -> accion.run());
        panel.add(fila, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultados), BorderLayout.CENTER);

        mostrar(hashtag ? "Buscar Hashtag" : "Buscar Profile", panel);
        if (!hashtag) accion.run();
    }

    private void publicar() {
        JPanel panel = columna();
        JTextArea texto = new JTextArea(5, 30);
        texto.setLineWrap(true);
        texto.setWrapStyleWord(true);

        JComboBox<String> carpetas = new JComboBox<>(new String[]{""});
        byte[][] foto = {null};
        JLabel preview = new JLabel("Texto o imagen");

        panel.add(new JLabel("Texto: 140 caracteres · Descripción de imagen: 220"));
        panel.add(new JScrollPane(texto));
        panel.add(preview);

        JPanel acciones = new JPanel();
        acciones.add(boton("Elegir imagen", () -> elegirFoto(bytes -> {
            foto[0] = bytes;
            ImagenTemporal.cargar(preview, () -> bytes, 450, 560);
        })));

        acciones.add(boton("Quitar imagen", () -> {
            foto[0] = null;
            preview.putClientProperty("carga", new Object());
            preview.setIcon(null);
            preview.setText("Texto");
        }));

        panel.add(acciones);
        panel.add(carpetas);

        JTextField nueva = new JTextField(16);
        JPanel folders = new JPanel();
        folders.add(nueva);

        folders.add(boton("Crear carpeta personal", () -> {
            String nombre = nueva.getText().strip();
            tarea(() -> exigir(cliente.crearCarpeta(nombre)), respuesta -> {
                carpetas.addItem(nombre);
                carpetas.setSelectedItem(nombre);
                nueva.setText("");
                aviso(respuesta.getMensaje());
            });
        }));
        panel.add(folders);

        JButton enviar = new JButton("Publicar");
        panel.add(enviar);
        enviar.addActionListener(e -> {
            String contenidoTexto = texto.getText().strip();
            String carpeta = String.valueOf(carpetas.getSelectedItem());
            byte[] bytes = foto[0];
            enviar.setEnabled(false);

            tarea(() -> {
                try {
                    return exigir(bytes == null
                            ? cliente.publicarTexto(contenidoTexto)
                            : cliente.publicarImagen(bytes, contenidoTexto, carpeta));
                } finally {
                    SwingUtilities.invokeLater(() -> enviar.setEnabled(true));
                }
            }, respuesta -> timeline());
        });

        panel.add(boton("Publicar un sticker", () -> galeria(null)));
        panel.add(boton("Importar sticker personal", this::importarSticker));
        mostrar("Cargar imágenes", new JScrollPane(panel));

        consultar(d -> cliente.carpetas(d), respuesta -> {
            for (String carpeta : respuesta.getCarpetas()) carpetas.addItem(carpeta);
        });
    }

    private void importarSticker() {
        JPanel panel = formulario();
        JTextField nombre = new JTextField();
        byte[][] bytes = {null};
        JLabel foto = new JLabel("PNG o JPG");

        campo(panel, "Nombre del sticker", nombre);
        panel.add(foto);
        panel.add(boton("Elegir imagen", () -> elegirFoto(imagen -> {
            bytes[0] = imagen;
            ImagenTemporal.cargar(foto, () -> imagen, 160, 160);
        })));

        panel.add(boton("Guardar sticker", () -> {
            if (bytes[0] == null) {
                aviso("Selecciona una imagen.");
                return;
            }

            String n = nombre.getText().strip();
            byte[] imagen = bytes[0];

            tarea(
                    () -> exigir(cliente.importarSticker(n, "sticker.png", imagen)),
                    respuesta -> {
                        publicar();
                        aviso("Sticker importado.");
                    }
            );
        }));

        panel.add(boton("Volver", this::publicar));
        mostrar("Importar sticker", new JScrollPane(panel));
    }

    private void galeria(String destinatario) {
        JPanel panel = columna();
        JTextField descripcion = new JTextField();

        if (destinatario == null) {
            panel.add(new JLabel("Texto de la publicación"));
            panel.add(descripcion);
        }

        panel.add(boton("Volver", () -> {
            if (destinatario == null) {
                publicar();
            } else {
                inbox();
                abrirChat(destinatario);
            }
        }));

        JPanel grid = new JPanel(new GridLayout(0, 3, 8, 8));
        panel.add(grid);
        mostrar("Galería de stickers", new JScrollPane(panel));

        consultar(d -> cliente.stickers(d), respuesta -> {
            for (Sticker sticker : respuesta.getStickers()) {
                JPanel celda = columna();
                JLabel im = new JLabel(sticker.getNombre());
                celda.add(im);
                imagen(im, sticker.getRutaImagen(), 180, 180);

                celda.add(boton(sticker.getNombre(), () -> {
                    String texto = descripcion.getText();

                    tarea(() -> exigir(destinatario == null
                            ? cliente.publicarSticker(texto, sticker.getId())
                            : cliente.enviarSticker(destinatario, sticker.getId())
                    ), resultado -> {
                        if (destinatario == null) {
                            timeline();
                        } else {
                            inbox();
                            abrirChat(destinatario);
                        }
                    });
                }));
                grid.add(celda);
            }

            panel.revalidate();
            panel.repaint();
        });
    }

    private void inbox() {
        contacto = "";
        contactos = new DefaultListModel<>();
        listaContactos = new JList<>(contactos);

        JPanel izquierda = new JPanel(new BorderLayout());
        JTextField destinatario = new JTextField();
        izquierda.add(destinatario, BorderLayout.NORTH);
        izquierda.add(new JScrollPane(listaContactos), BorderLayout.CENTER);
        izquierda.add(boton(
                "Abrir conversación",
                () -> abrirChat(
                        destinatario.getText().strip().toLowerCase(java.util.Locale.ROOT)
                )
        ), BorderLayout.SOUTH);

        JPanel derecha = new JPanel(new BorderLayout());
        chat = columna();
        derecha.add(new JScrollPane(chat), BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout());
        mensaje = new JTextField();
        pie.add(mensaje, BorderLayout.CENTER);

        JPanel botones = new JPanel();
        JButton enviar = boton("Enviar", this::enviarMensaje);
        botones.add(enviar);
        botones.add(boton("Sticker", () -> {
            if (!contacto.isBlank()) galeria(contacto);
        }));
        botones.add(boton("Actualizar", this::cargarChat));

        botones.add(boton("Eliminar", () -> {
            String otro = contacto;
            if (otro.isBlank()) return;

            if (JOptionPane.showConfirmDialog(
                    this, "¿Eliminar esta conversación de tu historial?",
                    "Inbox", JOptionPane.YES_NO_OPTION
            ) == JOptionPane.YES_OPTION) {
                tarea(
                        () -> exigir(cliente.eliminarConversacion(otro)),
                        respuesta -> inbox()
                );
            }
        }));

        pie.add(botones, BorderLayout.SOUTH);
        derecha.add(pie, BorderLayout.SOUTH);
        mensaje.addActionListener(e -> enviar.doClick());

        JSplitPane divisor = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, izquierda, derecha
        );
        divisor.setResizeWeight(.25);
        divisor.setDividerLocation(MODO_MOBILE ? 140 : 220);

        mostrar("Inbox", divisor);
        btnInbox.setText("Inbox");

        listaContactos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaContactos.getSelectedValue() != null) {
                contacto = listaContactos.getSelectedValue();
                mensaje.setText("");
                cargarChat();
            }
        });

        consultar(d -> cliente.conversaciones(d), respuesta -> {
            for (String nombre : respuesta.getElementos(String.class)) {
                if (!contactos.contains(nombre)) contactos.addElement(nombre);
            }
        });
    }

    private void abrirChat(String otro) {
        if (otro.isBlank() || otro.equals(usuario)) {
            aviso("Indica el username de otra cuenta.");
            return;
        }

        if (!contactos.contains(otro)) contactos.addElement(otro);
        listaContactos.setSelectedValue(otro, true);
    }

    private void cargarChat() {
        String otro = contacto;
        if (otro.isBlank()) return;
        Cliente actual = cliente;

        consultar(d -> actual.conversacion(otro, d), respuesta -> {
            if (!otro.equals(contacto)) return;

            chat.removeAll();
            chat.add(new JLabel("Conversación con @" + otro));

            for (Mensaje recibido : respuesta.getMensajes()) {
                JPanel burbuja = columna();
                burbuja.setBorder(new EmptyBorder(8, 8, 8, 8));
                burbuja.add(new JLabel(
                        "@" + recibido.getEmisor() + " → @" + recibido.getReceptor()
                        + " · " + fecha.format(recibido.getFechaEnvio())
                        + " · " + (recibido.estaLeido() ? "Leído" : "No leído")
                ));

                if (recibido.esSticker()) {
                    JLabel im = new JLabel("Sticker");
                    burbuja.add(im);
                    imagen(im, recibido.getRutaSticker(), 160, 160);
                } else {
                    JTextArea texto = new JTextArea(recibido.getContenido());
                    texto.setEditable(false);
                    texto.setLineWrap(true);
                    texto.setWrapStyleWord(true);
                    burbuja.add(texto);
                }
                chat.add(burbuja);
            }

            chat.revalidate();
            chat.repaint();

            tarea(() -> exigir(actual.marcarLeidos(otro)), resultado -> {
                if (otro.equals(contacto)) {
                    aviso("Conversación con @" + otro + " · mensajes recibidos leídos");
                }
            });
        });
    }

    private void enviarMensaje() {
        String otro = contacto;
        String texto = mensaje.getText().strip();

        if (otro.isBlank() || texto.isBlank()) {
            aviso("Selecciona una conversación y escribe un mensaje.");
            return;
        }
        if (texto.length() > 300) {
            aviso("Máximo 300 caracteres.");
            return;
        }

        JTextField campo = mensaje;
        if (!campo.isEnabled()) return;
        campo.setEnabled(false);

        tarea(() -> {
            try {
                return exigir(cliente.enviarMensaje(otro, texto));
            } finally {
                SwingUtilities.invokeLater(() -> campo.setEnabled(true));
            }
        }, respuesta -> {
            if (otro.equals(contacto)) {
                campo.setText("");
                cargarChat();
            }
        });
    }

    private void editar() {
        JPanel panel = formulario();
        mostrar("Editar perfil", new JScrollPane(panel));

        tarea(() -> exigir(cliente.perfil(usuario)), respuesta -> {
            var u = respuesta.getUsuario();
            JTextField nombre = new JTextField(u.getNombreCompleto());
            JComboBox<String> genero = new JComboBox<>(new String[]{"M", "F"});
            genero.setSelectedItem(String.valueOf(u.getGenero()));

            JSpinner edad = new JSpinner(
                    new SpinnerNumberModel(u.getEdad(), 1, 120, 1)
            );
            JPasswordField actual = new JPasswordField();
            JPasswordField nueva = new JPasswordField();

            campo(panel, "Nombre completo", nombre);
            campo(panel, "Género", genero);
            campo(panel, "Edad", edad);
            campo(panel, "Contraseña actual", actual);
            campo(panel, "Nueva contraseña (opcional)", nueva);

            JButton guardar = boton("Guardar cambios", () -> {
                String n = nombre.getText();
                String a = new String(actual.getPassword());
                String b = new String(nueva.getPassword());
                char g = genero.getSelectedItem().toString().charAt(0);
                int ed = (Integer) edad.getValue();

                tarea(() -> exigir(cliente.editarPerfil(n, g, ed, a, b)), resultado -> {
                    editar();
                    aviso("Perfil actualizado.");
                });
            });
            guardar.setEnabled(u.estaActiva());
            panel.add(guardar);

            JButton foto = boton("Cambiar foto", () -> elegirFoto(bytes ->
                    tarea(() -> exigir(cliente.cambiarFoto(bytes)), resultado -> {
                        editar();
                        aviso("Foto actualizada.");
                    })
            ));
            foto.setEnabled(u.estaActiva());
            panel.add(foto);

            panel.add(boton(
                    u.estaActiva() ? "Desactivar cuenta" : "Reactivar cuenta",
                    () -> {
                        if (u.estaActiva() && JOptionPane.showConfirmDialog(
                                this,
                                "¿Desactivar tu cuenta? Dejará de aparecer para los demás.",
                                "Cuenta", JOptionPane.YES_NO_OPTION
                        ) != JOptionPane.YES_OPTION) return;

                        tarea(() -> exigir(
                                u.estaActiva()
                                        ? cliente.desactivarCuenta()
                                        : cliente.reactivarCuenta()
                        ), resultado -> {
                            activa = resultado.getUsuario().estaActiva();
                            if (activa) iniciarNotificaciones();
                            else detenerNotificaciones();
                            editar();
                            aviso(activa ? "Cuenta reactivada." : "Cuenta desactivada.");
                        });
                    }
            ));

            panel.revalidate();
            panel.repaint();
        });
    }

    private void salir() {
        if (JOptionPane.showConfirmDialog(
                this, "¿Cerrar sesión?", "INSTA+", JOptionPane.YES_NO_OPTION
        ) != JOptionPane.YES_OPTION) return;

        detenerNotificaciones();
        Cliente actual = cliente;
        vista++;
        usuario = "";
        login();

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                actual.close();
                return null;
            }
        }.execute();
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setAccionCerrar(Runnable accion) {
    }

    public void cerrar() {
        if (cerrado) return;
        cerrado = true;
        vista++;
        usuario = "";
        detenerNotificaciones();

        Cliente actual = cliente;
        if (actual != null) {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    actual.close();
                    return null;
                }
            }.execute();
        }
    }
}