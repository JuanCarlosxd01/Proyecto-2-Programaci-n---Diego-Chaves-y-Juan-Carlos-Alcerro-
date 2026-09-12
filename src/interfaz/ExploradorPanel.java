package interfaz;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.*;
import javax.swing.tree.*;
import sistema.Sesion;

public class ExploradorPanel extends JPanel {
    private JTree arbol;
    private DefaultTreeModel modeloArbol;
    private File carpetaRaiz;
    private File archivoCopiado;
    private JComboBox<String> cmbOrdenar;
    
    private Runnable accionCerrar;

    public ExploradorPanel() {
        setLayout(new BorderLayout());
        crearRaizUsuario();
        crearBarraHerramientas();
        cargarArbol();
    }

    private void crearRaizUsuario() {
        if (Sesion.getUsuarioActual() != null) {
            String username = Sesion.getUsuarioActual().getUsername();
            carpetaRaiz = new File("Z/" + username);
        } else {
            carpetaRaiz = new File("Z");
        }

        if (!carpetaRaiz.exists()) {
            carpetaRaiz.mkdirs();
        }
    }

    private void crearBarraHerramientas() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnNuevaCarpeta = new JButton("Nueva carpeta");
        JButton btnNuevoArchivo = new JButton("Nuevo archivo");
        JButton btnImportarArchivo = new JButton("Importar archivo");
        JButton btnImportarCarpeta = new JButton("Importar carpeta");
        JButton btnRenombrar = new JButton("Renombrar");
        JButton btnCopiar = new JButton("Copiar");
        JButton btnPegar = new JButton("Pegar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnOrganizar = new JButton("Organizar");
        JButton btnActualizar = new JButton("Actualizar");

        cmbOrdenar = new JComboBox<>();
        cmbOrdenar.addItem("Nombre");
        cmbOrdenar.addItem("Fecha");
        cmbOrdenar.addItem("Tipo");
        cmbOrdenar.addItem("Tamaño");

        barra.add(btnNuevaCarpeta);
        barra.add(btnNuevoArchivo);
        barra.add(btnImportarArchivo);
        barra.add(btnImportarCarpeta);
        barra.add(btnRenombrar);
        barra.add(btnCopiar);
        barra.add(btnPegar);
        barra.add(btnEliminar);
        barra.add(btnOrganizar);
        barra.add(new JLabel("Ordenar por:"));
        barra.add(cmbOrdenar);
        barra.add(btnActualizar);

        add(barra, BorderLayout.NORTH);

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
        cmbOrdenar.addActionListener(e -> cargarArbol());
    }
    
    private void importarArchivo(){
        File destino = obtenerSeleccionado();
        if(destino == null){
            destino = carpetaRaiz;
        }
        if(destino.isFile()){
            destino = destino.getParentFile();
        }
        JFileChooser selector = new JFileChooser();
        
        selector.setDialogTitle("Seleccione un archivo de su carpeta");
        selector.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int resultado = selector.showOpenDialog(this);
        
        if(resultado != JFileChooser.APPROVE_OPTION){
            return;
        }
        File archivoExterno = selector.getSelectedFile();
        File archivoDestino = new File(destino, archivoExterno.getName());
        
        if(archivoDestino.exists()){
            int opcion = JOptionPane.showConfirmDialog(this, "Ya existe un archivo con ese nombre.\n¿Desea reemplazarlo?", "Archivo existente", JOptionPane.YES_NO_OPTION);
            
            if(opcion != JOptionPane.YES_OPTION){
                return;
            }
        }
        try{
            Files.copy(archivoExterno.toPath(), archivoDestino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            JOptionPane.showMessageDialog(this, "Archivo importado correctamente.");
            cargarArbol();
        } catch(IOException e){
            JOptionPane.showMessageDialog(this, "No se pudo importar el archivo:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

                    DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) value;
                    Object objeto = nodo.getUserObject();

                    if (objeto instanceof File) {
                        File archivo = (File) objeto;
                        String nombre = archivo.getName();

                        if (nombre.isEmpty()) {
                            nombre = archivo.getPath();
                        }

                        setText(nombre);

                        if (archivo.isDirectory()) {
                            if (expanded) {
                                setIcon(getOpenIcon());
                            } else {
                                setIcon(getClosedIcon());
                            }
                        } else {
                            setIcon(getLeafIcon());
                        }
                    }

                    return this;
                }
            });

            JScrollPane scroll = new JScrollPane(arbol);
            add(scroll, BorderLayout.CENTER);
        } else {
            arbol.setModel(modeloArbol);
        }

        revalidate();
        repaint();
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

            if (nombre.equals("Mis Imágenes") || nombre.equals("Mis Documentos") || nombre.equals("Música")) {
                JOptionPane.showMessageDialog(this, "No puede eliminar una carpeta principal del sistema.");
                return;
            }
        }

        int opcion = JOptionPane.showConfirmDialog(this, "¿Está seguro que desea eliminar " + seleccionado.getName() + "?", "Eliminar", JOptionPane.YES_NO_OPTION);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        if (eliminarArchivo(seleccionado)) {
            JOptionPane.showMessageDialog(this, "Elemento eliminado correctamente.");
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar el elemento.");
        }

        cargarArbol();
    }
    
    private boolean eliminarArchivo(File archivo) {
        if (archivo.isDirectory()) {
            File[] archivos = archivo.listFiles();

            if (archivos != null) {
                for (File hijo : archivos) {
                    if (!eliminarArchivo(hijo)) {
                        return false;
                    }
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

        if (archivo.isDirectory()) {
            File[] archivos = archivo.listFiles();

            if (archivos != null) {
                ordenarArchivos(archivos);

                for (File hijo : archivos) {
                    nodo.add(crearNodo(hijo));
                }
            }
        }

        return nodo;
    }

    private File obtenerSeleccionado() {
        TreePath seleccion = arbol.getSelectionPath();

        if (seleccion == null) {
            return null;
        }

        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) seleccion.getLastPathComponent();
        return (File) nodo.getUserObject();
    }

    private void crearCarpeta() {
        File seleccionado = obtenerSeleccionado();

        if (seleccionado == null) {
            seleccionado = carpetaRaiz;
        }

        if (seleccionado.isFile()) {
            seleccionado = seleccionado.getParentFile();
        }

        String nombre = JOptionPane.showInputDialog(this, "Nombre de la carpeta:");

        if (nombre == null) {
            return;
        }

        nombre = nombre.trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe escribir un nombre.");
            return;
        }

        if (nombre.contains("/") || nombre.contains("\\") || nombre.contains(":") || nombre.contains("*") || nombre.contains("?") || nombre.contains("\"") || nombre.contains("<") || nombre.contains(">") || nombre.contains("|")) {
            JOptionPane.showMessageDialog(this, "El nombre contiene caracteres no permitidos.");
            return;
        }

        File nuevaCarpeta = new File(seleccionado, nombre);

        if (nuevaCarpeta.exists()) {
            JOptionPane.showMessageDialog(this, "Ya existe un elemento con ese nombre.");
            return;
        }

        if (nuevaCarpeta.mkdirs()) {
            JOptionPane.showMessageDialog(this, "Carpeta creada correctamente.");
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo crear la carpeta.");
        }

        cargarArbol();
    }

    private void crearArchivo() {
        File seleccionado = obtenerSeleccionado();
        if (seleccionado == null) {
            seleccionado = carpetaRaiz;
        }

        if (seleccionado.isFile()) {
            seleccionado = seleccionado.getParentFile();
        }

        String nombre = JOptionPane.showInputDialog(this, "Nombre del archivo:");

        if (nombre == null) {
            return;
        }

        nombre = nombre.trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe escribir un nombre.");
            return;
        }

        if (nombre.contains("/") || nombre.contains("\\") || nombre.contains(":") || nombre.contains("*") || nombre.contains("?") || nombre.contains("\"") || nombre.contains("<") || nombre.contains(">") || nombre.contains("|")) {
            JOptionPane.showMessageDialog(this, "El nombre contiene caracteres no permitidos.");
            return;
        }

        File nuevoArchivo = new File(seleccionado, nombre);

        if (nuevoArchivo.exists()) {
            JOptionPane.showMessageDialog(this, "Ya existe un elemento con ese nombre.");
            return;
        }

        try {
            if (nuevoArchivo.createNewFile()) {
                JOptionPane.showMessageDialog(this, "Archivo creado correctamente.");
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo crear el archivo.");
            }
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

        if (nombreActual.equals("Mis Imágenes") || nombreActual.equals("Mis Documentos") || nombreActual.equals("Música")) {
            JOptionPane.showMessageDialog(this, "No puede renombrar una carpeta principal del sistema.");
            return;
        }

        String nuevoNombre = JOptionPane.showInputDialog(this, "Nuevo nombre:", seleccionado.getName());

        if (nuevoNombre == null) {
            return;
        }

        nuevoNombre = nuevoNombre.trim();

        if (nuevoNombre.isEmpty()) {
            return;
        }

        File nuevoArchivo = new File(seleccionado.getParentFile(), nuevoNombre);

        if (nuevoArchivo.exists()) {
            JOptionPane.showMessageDialog(this, "Ya existe un elemento con ese nombre.");
            return;
        }

        try {
            Files.move(seleccionado.toPath(), nuevoArchivo.toPath());
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

        if (destino.isFile()) {
            destino = destino.getParentFile();
        }

        try {
            String rutaOrigen = archivoCopiado.getCanonicalPath();
            String rutaDestino = destino.getCanonicalPath();

            if (archivoCopiado.isDirectory() && (rutaDestino.equals(rutaOrigen) || rutaDestino.startsWith(rutaOrigen + File.separator))) {
                JOptionPane.showMessageDialog(this, "No puede pegar una carpeta dentro de sí misma.");
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
            JOptionPane.showMessageDialog(this, "No se pudo pegar el elemento.\n" + e.getMessage());
        }

        cargarArbol();
    }

    private void copiarArchivo(File origen, File destino) throws IOException {
        Files.copy(origen.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void copiarCarpeta(File origen, File destino) throws IOException {
        if(!destino.exists()){
            if(!destino.mkdirs()){
                throw new IOException("No se pudo crear la carpeta " + destino.getName());
            }
        }
        File[] archivos = origen.listFiles();

        if (archivos == null) {
            return;
        }

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

        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una carpeta.");
            return;
        }

        if (!seleccionado.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una carpeta, no un archivo.");
            return;
        }

        File imagenes = new File(carpetaRaiz, "Mis Imágenes");
        File documentos = new File(carpetaRaiz, "Mis Documentos");
        File musica = new File(carpetaRaiz, "Música");

        imagenes.mkdirs();
        documentos.mkdirs();
        musica.mkdirs();

        File[] archivos = seleccionado.listFiles();

        if (archivos == null) {
            JOptionPane.showMessageDialog(this, "No se pudieron leer los archivos.");
            return;
        }

        int movidos = 0;

        for (File archivo : archivos) {
            if (!archivo.isFile()) {
                continue;
            }

            String nombre = archivo.getName().toLowerCase();

            if (esImagen(nombre)) {
                if (moverArchivo(archivo, imagenes)) {
                    movidos++;
                }
            } else if (esDocumento(nombre)) {
                if (moverArchivo(archivo, documentos)) {
                    movidos++;
                }
            } else if (esMusica(nombre)) {
                if (moverArchivo(archivo, musica)) {
                    movidos++;
                }
            }
        }

        JOptionPane.showMessageDialog(this, "Organización terminada.\nArchivos organizados: " + movidos);
        cargarArbol();
    }

    private boolean esImagen(String nombre) {
        return nombre.endsWith(".png") || nombre.endsWith(".jpg") || nombre.endsWith(".jpeg") || nombre.endsWith(".gif") || nombre.endsWith(".bmp") || nombre.endsWith(".webp");
    }

    private boolean esDocumento(String nombre) {
        return nombre.endsWith(".txt") || nombre.endsWith(".pdf") || nombre.endsWith(".doc") || nombre.endsWith(".docx") || nombre.endsWith(".edt") || nombre.endsWith(".ppt") || nombre.endsWith(".pptx")  || nombre.endsWith(".xls")  || nombre.endsWith(".xlsx");
    }

    private boolean esMusica(String nombre) {
        return nombre.endsWith(".mp3") || nombre.endsWith(".wav") || nombre.endsWith(".wma") || nombre.endsWith(".aiff") || nombre.endsWith(".aif") || nombre.endsWith(".au");
    }

    private boolean moverArchivo(File archivo, File carpetaDestino) {
        try {
            if (!carpetaDestino.exists()) {
                if (!carpetaDestino.mkdirs()) {
                    return false;
                }
            }

            if (archivo.getParentFile().getCanonicalFile().equals(carpetaDestino.getCanonicalFile())) {
                return false;
            }

            File destino = new File(carpetaDestino, archivo.getName());

            if (destino.exists()) {
                destino = obtenerNombreDisponible(carpetaDestino, archivo.getName());
            }

            Files.move(archivo.toPath(), destino.toPath());
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo mover " + archivo.getName() + ".\n" + e.getMessage());
            return false;
        }
    }

    private void ordenarArchivos(File[] archivos) {
        String opcion = cmbOrdenar == null ? "Nombre" : cmbOrdenar.getSelectedItem().toString();
        Comparator<File> comparadorCarpetas = (a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) {
                return -1;
            }

            if (!a.isDirectory() && b.isDirectory()) {
                return 1;
            }

            return 0;
        };
        Comparator<File> comparador;
        if (opcion.equals("Fecha")) {
            comparador = Comparator.comparingLong(File::lastModified);
        } else if (opcion.equals("Tipo")) {
            comparador = Comparator.comparing(this::obtenerExtension, String.CASE_INSENSITIVE_ORDER);
        } else if (opcion.equals("Tamaño")) {
            comparador = Comparator.comparingLong(File::length);
        } else {
            comparador = Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER);
        }

        Arrays.sort(archivos, comparadorCarpetas.thenComparing(comparador));
    }
    
    private void importarCarpeta() {
        File destino = obtenerSeleccionado();
        if (destino == null) {
            destino = carpetaRaiz;
        }
        if (destino.isFile()) {
            destino = destino.getParentFile();
        }

        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Seleccione una carpeta de su computadora");
        selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int resultado = selector.showOpenDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File carpetaExterna = selector.getSelectedFile();
        File carpetaDestino =new File(destino, carpetaExterna.getName());

        if (carpetaDestino.exists()) {
            JOptionPane.showMessageDialog(this, "Ya existe una carpeta con ese nombre.");
            return;
        }

        try {
            copiarCarpeta(carpetaExterna, carpetaDestino);
            JOptionPane.showMessageDialog(this, "Carpeta importada correctamente.");
            cargarArbol();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo importar la carpeta:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String obtenerExtension(File archivo) {
        if (archivo.isDirectory()) {
            return "";
        }

        String nombre = archivo.getName();
        int punto = nombre.lastIndexOf(".");

        if (punto == -1) {
            return "";
        }

        return nombre.substring(punto + 1).toLowerCase();
    }
    
    private File obtenerNombreDisponible(File carpeta, String nombreOriginal){
        String nombre = nombreOriginal;
        String base;
        String extension;
        int punto = nombre.lastIndexOf('.');
        
        if(punto > 0){
            base = nombre.substring(0, punto);
            extension = nombre.substring(punto);
        }
        else{
            base = nombre;
            extension = "";
        }
        int contador = 1;
        File resultado = new File(carpeta, nombre);
        
        while(resultado.exists()){
            resultado = new File(carpeta, base + " (" + contador + ")" + extension);
            contador++;
        }
        return resultado;
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