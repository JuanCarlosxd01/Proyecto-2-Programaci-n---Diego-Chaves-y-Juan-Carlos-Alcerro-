
package interfaz;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.*;
import javax.swing.tree.*;
import sistema.RutasSistema;
import sistema.SeguridadArchivos;
import sistema.Sesion;

public class ExploradorPanel extends JPanel {

    private boolean organizando;
    private hilos.HiloOrganizador trabajoOrganizador;
    private JTree arbol;
    private DefaultTreeModel modeloArbol;
    private File carpetaRaiz;
    private File archivoCopiado;
    private JComboBox<String> cmbOrdenar;
    private JTextField txtRuta;
    private JLabel lblEstado;
    private JSplitPane panelDividido;
    private java.util.function.Consumer<File> abrirMusica;
    private Runnable accionCerrar;

    public ExploradorPanel() {
        setLayout(new BorderLayout());
        crearRaizUsuario();
        crearBarraHerramientas();
        cargarArbol();
    }

    public void detener() {
        if (trabajoOrganizador != null) trabajoOrganizador.cancel(true);
    }

    public void setAbrirMusica(java.util.function.Consumer<File> accion) {
        abrirMusica = accion;
    }

    private void crearRaizUsuario() {
        carpetaRaiz = RutasSistema.getRaizExplorador();
        if (carpetaRaiz == null) carpetaRaiz = RutasSistema.getRaizSistema();
        if (!carpetaRaiz.exists()) carpetaRaiz.mkdirs();
    }

    private void crearBarraHerramientas() {
        JPanel superior = new JPanel();
        superior.setLayout(new BoxLayout(superior, BoxLayout.Y_AXIS));
        superior.setBackground(new Color(250, 250, 250));
        superior.setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 0, new Color(215, 215, 215)
        ));

        JPanel navegacion = new JPanel(new BorderLayout(8, 0));
        navegacion.setOpaque(false);
        navegacion.setBorder(BorderFactory.createEmptyBorder(8, 10, 6, 10));

        JPanel botonesNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        botonesNav.setOpaque(false);
        JButton btnAtras = new JButton("←");
        JButton btnArriba = new JButton("↑");
        JButton btnActualizar = new JButton("↻");

        for (JButton boton : new JButton[]{btnAtras, btnArriba, btnActualizar}) {
            boton.setFocusPainted(false);
            boton.setPreferredSize(new Dimension(42, 32));
            botonesNav.add(boton);
        }

        txtRuta = new JTextField();
        txtRuta.setEditable(false);
        txtRuta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtRuta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 190, 190)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        actualizarRutaVisible(carpetaRaiz);

        navegacion.add(botonesNav, BorderLayout.WEST);
        navegacion.add(txtRuta, BorderLayout.CENTER);

        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        barra.setBorder(BorderFactory.createEmptyBorder(4, 10, 7, 10));
        barra.setBackground(new Color(250, 250, 250));

        JButton btnNuevaCarpeta = new JButton("Nueva carpeta");
        JButton btnNuevoArchivo = new JButton("Nuevo archivo");
        JButton btnImportarArchivo = new JButton("Importar archivo");
        JButton btnImportarCarpeta = new JButton("Importar carpeta");
        JButton btnRenombrar = new JButton("Renombrar");
        JButton btnCopiar = new JButton("Copiar");
        JButton btnPegar = new JButton("Pegar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnOrganizar = new JButton("Organizar");

        for (JButton boton : new JButton[]{
            btnNuevaCarpeta, btnNuevoArchivo, btnImportarArchivo,
            btnImportarCarpeta, btnRenombrar, btnCopiar, btnPegar,
            btnEliminar, btnOrganizar
        }) {
            boton.setFocusPainted(false);
            barra.add(boton);
            barra.addSeparator(new Dimension(4, 0));
        }

        cmbOrdenar = new JComboBox<>(new String[]{"Nombre", "Fecha", "Tipo", "Tamaño"});
        barra.add(Box.createHorizontalGlue());
        barra.add(new JLabel("Ordenar por: "));
        barra.add(cmbOrdenar);

        superior.add(navegacion);
        superior.add(barra);
        add(superior, BorderLayout.NORTH);

        lblEstado = new JLabel(" ");
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEstado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        add(lblEstado, BorderLayout.SOUTH);

        btnNuevaCarpeta.addActionListener(e -> crearCarpeta());
        btnNuevoArchivo.addActionListener(e -> crearArchivo());
        btnImportarArchivo.addActionListener(e -> importarArchivo());
        btnImportarCarpeta.addActionListener(e -> importarCarpeta());
        btnRenombrar.addActionListener(e -> renombrar());
        btnCopiar.addActionListener(e -> copiar());
        btnPegar.addActionListener(e -> pegar());
        btnEliminar.addActionListener(e -> eliminar());
        btnOrganizar.addActionListener(e -> organizar());
        btnActualizar.addActionListener(e -> cargarArbol());
        btnAtras.addActionListener(e -> subirNivel());
        btnArriba.addActionListener(e -> subirNivel());
        cmbOrdenar.addActionListener(e -> cargarArbol());
    }

    private void subirNivel() {
        File seleccionado = obtenerSeleccionado();
        File actual = seleccionado == null
                ? carpetaRaiz
                : seleccionado.isDirectory() ? seleccionado : seleccionado.getParentFile();

        if (actual == null || actual.equals(carpetaRaiz)) {
            actualizarRutaVisible(carpetaRaiz);
            return;
        }

        File padre = actual.getParentFile();
        if (padre != null && padre.toPath().normalize()
                .startsWith(carpetaRaiz.toPath().normalize())) {
            seleccionarArchivoEnArbol(padre);
        }
    }

    private void seleccionarArchivoEnArbol(File archivo) {
        if (arbol == null || archivo == null) return;
        DefaultMutableTreeNode raiz = (DefaultMutableTreeNode) modeloArbol.getRoot();
        TreePath ruta = buscarRutaNodo(raiz, archivo);

        if (ruta != null) {
            arbol.setSelectionPath(ruta);
            arbol.scrollPathToVisible(ruta);
        }
    }

    private TreePath buscarRutaNodo(DefaultMutableTreeNode nodo, File archivo) {
        Object objeto = nodo.getUserObject();
        if (objeto instanceof File actual && actual.equals(archivo)) {
            return new TreePath(nodo.getPath());
        }

        for (int i = 0; i < nodo.getChildCount(); i++) {
            TreePath encontrada = buscarRutaNodo(
                    (DefaultMutableTreeNode) nodo.getChildAt(i), archivo
            );
            if (encontrada != null) return encontrada;
        }
        return null;
    }

    private void actualizarRutaVisible(File archivo) {
        if (txtRuta == null || archivo == null) return;

        try {
            File raizSistema = RutasSistema.getRaizSistema().getCanonicalFile();
            File actual = archivo.getCanonicalFile();
            String relativa = raizSistema.toPath().relativize(actual.toPath())
                    .toString().replace(File.separatorChar, '\\');
            txtRuta.setText(relativa.isEmpty() ? "Z:\\" : "Z:\\" + relativa);
        } catch (IOException e) {
            txtRuta.setText(archivo.getPath());
        }

        if (lblEstado != null) {
            if (archivo.isDirectory()) {
                File[] hijos = archivo.listFiles();
                lblEstado.setText((hijos == null ? 0 : hijos.length) + " elementos");
            } else {
                lblEstado.setText(archivo.getName() + " · " + archivo.length() + " bytes");
            }
        }
    }

    private void importarArchivo() {
        File destino = obtenerSeleccionado();
        if (destino == null) destino = carpetaRaiz;
        if (destino.isFile()) destino = destino.getParentFile();

        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Seleccione un archivo de su carpeta");
        selector.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File archivoExterno = selector.getSelectedFile();
        File archivoDestino = new File(destino, archivoExterno.getName());

        if (archivoDestino.exists()) {
            int opcion = JOptionPane.showConfirmDialog(
                    this, "Ya existe un archivo con ese nombre.\n¿Desea reemplazarlo?",
                    "Archivo existente", JOptionPane.YES_NO_OPTION
            );
            if (opcion != JOptionPane.YES_OPTION) return;
        }

        try {
            SeguridadArchivos.importar(archivoExterno);
            SeguridadArchivos.permitido(archivoDestino);
            Files.copy(
                    archivoExterno.toPath(), archivoDestino.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
            JOptionPane.showMessageDialog(this, "Archivo importado correctamente.");
            cargarArbol();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this, "No se pudo importar el archivo:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void cargarArbol() {
        DefaultMutableTreeNode raiz = crearNodo(carpetaRaiz);
        modeloArbol = new DefaultTreeModel(raiz);

        if (arbol == null) {
            arbol = new JTree(modeloArbol);
            arbol.setRootVisible(true);
            arbol.setShowsRootHandles(true);

            arbol.setCellRenderer(new DefaultTreeCellRenderer() {
                @Override
                public Component getTreeCellRendererComponent(
                        JTree tree, Object value, boolean sel, boolean expanded,
                        boolean leaf, int row, boolean hasFocus
                ) {
                    super.getTreeCellRendererComponent(
                            tree, value, sel, expanded, leaf, row, hasFocus
                    );

                    DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) value;
                    if (nodo.getUserObject() instanceof File archivo) {
                        String nombre = archivo.getName();
                        if (nombre.isEmpty()) nombre = archivo.getPath();
                        setText(nombre);

                        if (archivo.isDirectory()) {
                            setIcon(expanded ? getOpenIcon() : getClosedIcon());
                        } else {
                            setIcon(getLeafIcon());
                        }
                    }
                    return this;
                }
            });

            arbol.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    File archivo = obtenerSeleccionado();
                    if (e.getClickCount() == 2 && archivo != null
                            && archivo.isFile() && abrirMusica != null
                            && archivo.getName().toLowerCase()
                                    .matches(".*\\.(mp3|wav|au|aiff|aif)$")) {
                        abrirMusica.accept(archivo);
                    }
                }
            });

            arbol.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            arbol.setRowHeight(26);
            arbol.addTreeSelectionListener(e -> {
                File seleccionado = obtenerSeleccionado();
                if (seleccionado != null) actualizarRutaVisible(seleccionado);
            });

            JScrollPane scroll = new JScrollPane(arbol);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            crearAreaWindows(scroll);
        } else {
            arbol.setModel(modeloArbol);
        }

        actualizarRutaVisible(carpetaRaiz);
        revalidate();
        repaint();
    }

    private void crearAreaWindows(JScrollPane contenido) {
        JPanel accesos = new JPanel();
        accesos.setLayout(new BoxLayout(accesos, BoxLayout.Y_AXIS));
        accesos.setBackground(new Color(246, 246, 246));
        accesos.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        accesos.setPreferredSize(new Dimension(190, 0));

        JLabel titulo = new JLabel("Acceso rápido");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titulo.setBorder(BorderFactory.createEmptyBorder(4, 10, 8, 0));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        accesos.add(titulo);

        Icon icono = UIManager.getIcon("FileView.directoryIcon");
        agregarAcceso(accesos, "Este equipo", icono, carpetaRaiz);

        File carpetaUsuario = RutasSistema.getCarpetaUsuarioActual();
        if (carpetaUsuario != null) {
            agregarAcceso(accesos, "Inicio", icono, carpetaUsuario);
            agregarAcceso(accesos, "Documentos", icono,
                    RutasSistema.getDocumentosUsuarioActual());
            agregarAcceso(accesos, "Imágenes", icono,
                    RutasSistema.getImagenesUsuarioActual());
            agregarAcceso(accesos, "Música", icono,
                    RutasSistema.getMusicaUsuarioActual());
        }

        accesos.add(Box.createVerticalGlue());
        JLabel unidad = new JLabel("  Unidad Z:");
        unidad.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        unidad.setForeground(new Color(90, 90, 90));
        unidad.setAlignmentX(Component.LEFT_ALIGNMENT);
        accesos.add(unidad);

        panelDividido = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, accesos, contenido);
        panelDividido.setDividerLocation(190);
        panelDividido.setDividerSize(1);
        panelDividido.setBorder(BorderFactory.createEmptyBorder());
        panelDividido.setContinuousLayout(true);
        add(panelDividido, BorderLayout.CENTER);
    }

    private void agregarAcceso(JPanel panel, String texto, Icon icono, File destino) {
        if (destino == null) return;

        JButton boton = new JButton(texto, icono);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        boton.setIconTextGap(10);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);

        boton.addActionListener(e -> {
            if (!destino.exists()) destino.mkdirs();
            seleccionarArchivoEnArbol(destino);
            actualizarRutaVisible(destino);
        });
        panel.add(boton);
    }

    private void eliminar() {
        File seleccionado = obtenerSeleccionado();
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo o carpeta.");
            return;
        }
        if (seleccionado.equals(carpetaRaiz)) {
            JOptionPane.showMessageDialog(this, "No puede eliminar la carpeta raíz del usuario.");
            return;
        }

        File padre = seleccionado.getParentFile();
        if (padre != null && padre.equals(carpetaRaiz)) {
            String nombre = seleccionado.getName();
            if (nombre.equals("Mis Imágenes") || nombre.equals("Mis Documentos")
                    || nombre.equals("Música")) {
                JOptionPane.showMessageDialog(
                        this, "No puede eliminar una carpeta principal del sistema."
                );
                return;
            }
        }

        if (JOptionPane.showConfirmDialog(
                this, "¿Está seguro que desea eliminar " + seleccionado.getName() + "?",
                "Eliminar", JOptionPane.YES_NO_OPTION
        ) != JOptionPane.YES_OPTION) return;

        JOptionPane.showMessageDialog(
                this, eliminarArchivo(seleccionado)
                        ? "Elemento eliminado correctamente."
                        : "No se pudo eliminar el elemento."
        );
        cargarArbol();
    }

    private boolean eliminarArchivo(File archivo) {
        if (!SeguridadArchivos.esPermitido(archivo)) return false;

        if (archivo.isDirectory()) {
            File[] archivos = archivo.listFiles();
            if (archivos != null) {
                for (File hijo : archivos) {
                    if (!eliminarArchivo(hijo)) return false;
                }
            }
        }

        try {
            return Files.deleteIfExists(archivo.toPath());
        } catch (IOException e) {
            return false;
        }
    }

    private DefaultMutableTreeNode crearNodo(File archivo) {
        DefaultMutableTreeNode nodo = new DefaultMutableTreeNode(archivo);
        if (!SeguridadArchivos.esPermitido(archivo)) return nodo;

        if (archivo.isDirectory()) {
            File[] archivos = archivo.listFiles();
            if (archivos != null) {
                ordenarArchivos(archivos);
                for (File hijo : archivos) {
                    if (SeguridadArchivos.esPermitido(hijo)) nodo.add(crearNodo(hijo));
                }
            }
        }
        return nodo;
    }

    private File obtenerSeleccionado() {
        TreePath seleccion = arbol.getSelectionPath();
        if (seleccion == null) return null;

        DefaultMutableTreeNode nodo =
                (DefaultMutableTreeNode) seleccion.getLastPathComponent();
        File archivo = (File) nodo.getUserObject();
        return SeguridadArchivos.esPermitido(archivo) ? archivo : null;
    }

    private void crearCarpeta() {
        File seleccionado = obtenerSeleccionado();
        if (seleccionado == null) seleccionado = carpetaRaiz;
        if (seleccionado.isFile()) seleccionado = seleccionado.getParentFile();

        String nombre = JOptionPane.showInputDialog(this, "Nombre de la carpeta:");
        if (nombre == null) return;
        nombre = nombre.trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe escribir un nombre.");
            return;
        }

        if (nombre.contains("/") || nombre.contains("\\") || nombre.contains(":")
                || nombre.contains("*") || nombre.contains("?") || nombre.contains("\"")
                || nombre.contains("<") || nombre.contains(">") || nombre.contains("|")) {
            JOptionPane.showMessageDialog(this, "El nombre contiene caracteres no permitidos.");
            return;
        }

        File nuevaCarpeta = new File(seleccionado, nombre);
        if (nuevaCarpeta.exists()) {
            JOptionPane.showMessageDialog(this, "Ya existe un elemento con ese nombre.");
            return;
        }

        JOptionPane.showMessageDialog(
                this, nuevaCarpeta.mkdirs()
                        ? "Carpeta creada correctamente."
                        : "No se pudo crear la carpeta."
        );
        cargarArbol();
    }

    private void crearArchivo() {
        File seleccionado = obtenerSeleccionado();
        if (seleccionado == null) seleccionado = carpetaRaiz;
        if (seleccionado.isFile()) seleccionado = seleccionado.getParentFile();

        String nombre = JOptionPane.showInputDialog(this, "Nombre del archivo:");
        if (nombre == null) return;
        nombre = nombre.trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe escribir un nombre.");
            return;
        }

        if (nombre.contains("/") || nombre.contains("\\") || nombre.contains(":")
                || nombre.contains("*") || nombre.contains("?") || nombre.contains("\"")
                || nombre.contains("<") || nombre.contains(">") || nombre.contains("|")) {
            JOptionPane.showMessageDialog(this, "El nombre contiene caracteres no permitidos.");
            return;
        }

        File nuevoArchivo = new File(seleccionado, nombre);
        if (nuevoArchivo.exists()) {
            JOptionPane.showMessageDialog(this, "Ya existe un elemento con ese nombre.");
            return;
        }

        try {
            JOptionPane.showMessageDialog(
                    this, nuevoArchivo.createNewFile()
                            ? "Archivo creado correctamente."
                            : "No se pudo crear el archivo."
            );
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al crear el archivo:\n" + e.getMessage());
        }
        cargarArbol();
    }

    private void renombrar() {
        File seleccionado = obtenerSeleccionado();
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo o carpeta.");
            return;
        }
        if (seleccionado.equals(carpetaRaiz)) {
            JOptionPane.showMessageDialog(this, "No puede renombrar la carpeta raíz.");
            return;
        }

        String nombreActual = seleccionado.getName();
        if (nombreActual.equals("Mis Imágenes") || nombreActual.equals("Mis Documentos")
                || nombreActual.equals("Música")) {
            JOptionPane.showMessageDialog(
                    this, "No puede renombrar una carpeta principal del sistema."
            );
            return;
        }

        String nuevoNombre = JOptionPane.showInputDialog(
                this, "Nuevo nombre:", seleccionado.getName()
        );
        if (nuevoNombre == null) return;
        nuevoNombre = nuevoNombre.trim();
        if (nuevoNombre.isEmpty()) return;

        try {
            SeguridadArchivos.nombre(nuevoNombre);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            return;
        }

        File nuevoArchivo = new File(seleccionado.getParentFile(), nuevoNombre);
        if (!SeguridadArchivos.esPermitido(nuevoArchivo)) return;
        if (nuevoArchivo.exists()) {
            JOptionPane.showMessageDialog(this, "Ya existe un elemento con ese nombre.");
            return;
        }

        try {
            EditorTexto.DocumentosTexto.mover(seleccionado, nuevoArchivo);
            JOptionPane.showMessageDialog(this, "Elemento renombrado correctamente.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo renombrar.\n" + e.getMessage());
        }
        cargarArbol();
    }

    private void copiar() {
        File seleccionado = obtenerSeleccionado();
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo o carpeta.");
            return;
        }

        archivoCopiado = seleccionado;
        JOptionPane.showMessageDialog(this, "Elemento copiado.");
    }

    private void pegar() {
        if (archivoCopiado == null) {
            JOptionPane.showMessageDialog(this, "No ha copiado ningún elemento.");
            return;
        }

        File destino = obtenerSeleccionado();
        if (destino == null) {
            JOptionPane.showMessageDialog(this, "Seleccione dónde desea pegar.");
            return;
        }
        if (destino.isFile()) destino = destino.getParentFile();

        try {
            String rutaOrigen = archivoCopiado.getCanonicalPath();
            String rutaDestino = destino.getCanonicalPath();

            if (archivoCopiado.isDirectory()
                    && (rutaDestino.equals(rutaOrigen)
                    || rutaDestino.startsWith(rutaOrigen + File.separator))) {
                JOptionPane.showMessageDialog(
                        this, "No puede pegar una carpeta dentro de sí misma."
                );
                return;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo comprobar la ruta.");
            return;
        }

        File nuevo = new File(destino, archivoCopiado.getName());
        if (nuevo.exists()) {
            nuevo = obtenerNombreDisponible(destino, archivoCopiado.getName());
        }

        try {
            if (archivoCopiado.isDirectory()) {
                copiarCarpeta(archivoCopiado, nuevo);
            } else {
                copiarArchivo(archivoCopiado, nuevo);
            }
            JOptionPane.showMessageDialog(this, "Elemento pegado correctamente.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this, "No se pudo pegar el elemento.\n" + e.getMessage()
            );
        }
        cargarArbol();
    }

    private void copiarArchivo(File origen, File destino) throws IOException {
        SeguridadArchivos.importar(origen);
        SeguridadArchivos.permitido(destino);
        Files.copy(origen.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
        EditorTexto.DocumentosTexto.copiarFormato(origen, destino);
    }

    private void copiarCarpeta(File origen, File destino) throws IOException {
        SeguridadArchivos.importar(origen);
        SeguridadArchivos.permitido(destino);

        if (!destino.exists() && !destino.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta " + destino.getName());
        }

        File[] archivos = origen.listFiles();
        if (archivos == null) return;

        for (File archivo : archivos) {
            File nuevoDestino = new File(destino, archivo.getName());
            if (archivo.isDirectory()) {
                copiarCarpeta(archivo, nuevoDestino);
            } else {
                copiarArchivo(archivo, nuevoDestino);
            }
        }
    }

    private void organizar() {
        File seleccionado = obtenerSeleccionado();
        if (seleccionado == null || !seleccionado.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Seleccione una carpeta para organizar.");
            return;
        }

        File raizUsuario = determinarRaizUsuario(seleccionado);
        if (raizUsuario == null) {
            JOptionPane.showMessageDialog(
                    this, "No se pudo determinar la carpeta del usuario.",
                    "Organizar", JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (organizando) return;
        organizando = true;
        lblEstado.setText("Organizando...");

        trabajoOrganizador = new hilos.HiloOrganizador(
                seleccionado, seleccionado,
                movidos -> {
                    organizando = false;
                    lblEstado.setText("Archivos organizados: " + movidos);
                    cargarArbol();
                },
                error -> {
                    organizando = false;
                    JOptionPane.showMessageDialog(this, error.getMessage());
                }
        );
        trabajoOrganizador.execute();
    }

    private File determinarRaizUsuario(File seleccionado) {
        if (!Sesion.esAdministrador()) {
            return RutasSistema.getCarpetaUsuarioActual();
        }

        try {
            File raizSistema = RutasSistema.getRaizSistema().getCanonicalFile();
            File actual = seleccionado.getCanonicalFile();

            if (actual.equals(raizSistema)) {
                return RutasSistema.getCarpetaUsuarioActual();
            }

            while (actual.getParentFile() != null
                    && !actual.getParentFile().getCanonicalFile().equals(raizSistema)) {
                actual = actual.getParentFile().getCanonicalFile();
            }

            return actual.getParentFile() != null
                    && actual.getParentFile().getCanonicalFile().equals(raizSistema)
                    ? actual : RutasSistema.getCarpetaUsuarioActual();
        } catch (IOException e) {
            return RutasSistema.getCarpetaUsuarioActual();
        }
    }

    private void ordenarArchivos(File[] archivos) {
        String opcion = cmbOrdenar == null
                ? "Nombre" : cmbOrdenar.getSelectedItem().toString();

        Comparator<File> comparadorCarpetas = (a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            return 0;
        };

        Comparator<File> comparador;
        if (opcion.equals("Fecha")) {
            comparador = Comparator.comparingLong(File::lastModified);
        } else if (opcion.equals("Tipo")) {
            comparador = Comparator.comparing(
                    this::obtenerExtension, String.CASE_INSENSITIVE_ORDER
            );
        } else if (opcion.equals("Tamaño")) {
            comparador = Comparator.comparingLong(File::length);
        } else {
            comparador = Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER);
        }

        Arrays.sort(archivos, comparadorCarpetas.thenComparing(comparador));
    }

    private void importarCarpeta() {
        File destino = obtenerSeleccionado();
        if (destino == null) destino = carpetaRaiz;
        if (destino.isFile()) destino = destino.getParentFile();

        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Seleccione una carpeta de su computadora");
        selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File carpetaExterna = selector.getSelectedFile();
        File carpetaDestino = new File(destino, carpetaExterna.getName());

        if (carpetaDestino.exists()) {
            JOptionPane.showMessageDialog(this, "Ya existe una carpeta con ese nombre.");
            return;
        }

        try {
            copiarCarpeta(carpetaExterna, carpetaDestino);
            JOptionPane.showMessageDialog(this, "Carpeta importada correctamente.");
            cargarArbol();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this, "No se pudo importar la carpeta:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String obtenerExtension(File archivo) {
        if (archivo.isDirectory()) return "";
        String nombre = archivo.getName();
        int punto = nombre.lastIndexOf(".");
        return punto == -1 ? "" : nombre.substring(punto + 1).toLowerCase();
    }

    private File obtenerNombreDisponible(File carpeta, String nombreOriginal) {
        int punto = nombreOriginal.lastIndexOf('.');
        String base = punto > 0 ? nombreOriginal.substring(0, punto) : nombreOriginal;
        String extension = punto > 0 ? nombreOriginal.substring(punto) : "";

        int contador = 1;
        File resultado = new File(carpeta, nombreOriginal);

        while (resultado.exists()) {
            resultado = new File(carpeta, base + " (" + contador + ")" + extension);
            contador++;
        }
        return resultado;
    }

    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }

    public void cerrar() {
        if (accionCerrar != null) accionCerrar.run();
    }
}