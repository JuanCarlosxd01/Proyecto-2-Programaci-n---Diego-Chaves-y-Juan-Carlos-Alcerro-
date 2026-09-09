package interfaz;

import java.awt.*;
import java.io.*;
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
        JButton btnRenombrar = new JButton("Renombrar");
        JButton btnCopiar = new JButton("Copiar");
        JButton btnPegar = new JButton("Pegar");
        JButton btnOrganizar = new JButton("Organizar");
        JButton btnActualizar = new JButton("Actualizar");

        cmbOrdenar = new JComboBox<>();
        cmbOrdenar.addItem("Nombre");
        cmbOrdenar.addItem("Fecha");
        cmbOrdenar.addItem("Tipo");
        cmbOrdenar.addItem("Tamaño");

        barra.add(btnNuevaCarpeta);
        barra.add(btnNuevoArchivo);
        barra.add(btnRenombrar);
        barra.add(btnCopiar);
        barra.add(btnPegar);
        barra.add(btnOrganizar);
        barra.add(new JLabel("Ordenar por:"));
        barra.add(cmbOrdenar);
        barra.add(btnActualizar);

        add(barra, BorderLayout.NORTH);

        btnNuevaCarpeta.addActionListener(e -> crearCarpeta());
        btnNuevoArchivo.addActionListener(e -> crearArchivo());
        btnRenombrar.addActionListener(e -> renombrar());
        btnCopiar.addActionListener(e -> copiar());
        btnPegar.addActionListener(e -> pegar());
        btnOrganizar.addActionListener(e -> organizar());
        btnActualizar.addActionListener(e -> cargarArbol());
        cmbOrdenar.addActionListener(e -> cargarArbol());
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

        if (!seleccionado.exists()) {
            seleccionado.mkdirs();
        }

        String nombre = JOptionPane.showInputDialog(this, "Nombre de la carpeta:");
        if (nombre == null || nombre.trim().isEmpty()) {
            return;
        }
        nombre = nombre.trim();
        File nuevaCarpeta = new File(seleccionado, nombre);

        if (nuevaCarpeta.exists()) {
            JOptionPane.showMessageDialog(this, "Ya existe una carpeta con ese nombre.");
            return;
        }

        if (nuevaCarpeta.mkdirs()) {
            JOptionPane.showMessageDialog(this, "Carpeta creada correctamente.");
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo crear la carpeta.\nRuta: " + nuevaCarpeta.getAbsolutePath());
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

        if (!seleccionado.exists()) {
            seleccionado.mkdirs();
        }

        String nombre = JOptionPane.showInputDialog(this, "Nombre del archivo:");

        if (nombre == null || nombre.trim().isEmpty()) {
            return;
        }
        nombre = nombre.trim();
        File nuevoArchivo = new File(seleccionado, nombre);

        if (nuevoArchivo.exists()) {
            JOptionPane.showMessageDialog(this, "El archivo ya existe.");
            return;
        }

        try {
            if (nuevoArchivo.createNewFile()) {
                JOptionPane.showMessageDialog(this, "Archivo creado correctamente.");
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo crear el archivo.");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al crear el archivo:\n" + e.getMessage());
            e.printStackTrace();
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

        String nuevoNombre = JOptionPane.showInputDialog(this, "Nuevo nombre:", seleccionado.getName());

        if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
            return;
        }

        File nuevoArchivo = new File(seleccionado.getParentFile(), nuevoNombre);

        if (nuevoArchivo.exists()) {
            JOptionPane.showMessageDialog(this, "Ya existe un elemento con ese nombre.");
            return;
        }

        if (!seleccionado.renameTo(nuevoArchivo)) {
            JOptionPane.showMessageDialog(this, "No se pudo renombrar.");
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

        File nuevo = new File(destino, archivoCopiado.getName());

        if (nuevo.exists()) {
            JOptionPane.showMessageDialog(this, "Ya existe un elemento con ese nombre.");
            return;
        }

        try {
            if (archivoCopiado.isDirectory()) {
                copiarCarpeta(archivoCopiado, nuevo);
            } else {
                copiarArchivo(archivoCopiado, nuevo);
            }

            JOptionPane.showMessageDialog(this, "Elemento pegado.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo pegar el elemento.");
        }

        cargarArbol();
    }

    private void copiarArchivo(File origen, File destino) throws IOException {
        FileInputStream entrada = new FileInputStream(origen);
        FileOutputStream salida = new FileOutputStream(destino);

        byte[] buffer = new byte[1024];
        int cantidad;

        while ((cantidad = entrada.read(buffer)) != -1) {
            salida.write(buffer, 0, cantidad);
        }

        entrada.close();
        salida.close();
    }

    private void copiarCarpeta(File origen, File destino) throws IOException {
        destino.mkdir();

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

        if (seleccionado == null || !seleccionado.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una carpeta.");
            return;
        }

        File imagenes = new File(seleccionado, "Mis Imágenes");
        File documentos = new File(seleccionado, "Mis Documentos");
        File musica = new File(seleccionado, "Música");

        File[] archivos = seleccionado.listFiles();

        if (archivos == null) {
            return;
        }

        for (File archivo : archivos) {
            if (archivo.isFile()) {
                String nombre = archivo.getName().toLowerCase();

                if (esImagen(nombre)) {
                    if (!imagenes.exists()) {
                        imagenes.mkdir();
                    }
                    moverArchivo(archivo, imagenes);
                } else if (esDocumento(nombre)) {
                    if (!documentos.exists()) {
                        documentos.mkdir();
                    }
                    moverArchivo(archivo, documentos);
                } else if (esMusica(nombre)) {
                    if (!musica.exists()) {
                        musica.mkdir();
                    }
                    moverArchivo(archivo, musica);
                }
            }
        }

        JOptionPane.showMessageDialog(this, "Archivos organizados.");
        cargarArbol();
    }

    private boolean esImagen(String nombre) {
        return nombre.endsWith(".png") || nombre.endsWith(".jpg") || nombre.endsWith(".jpeg") || nombre.endsWith(".gif");
    }

    private boolean esDocumento(String nombre) {
        return nombre.endsWith(".txt") || nombre.endsWith(".pdf") || nombre.endsWith(".doc") || nombre.endsWith(".docx") || nombre.endsWith(".edt");
    }

    private boolean esMusica(String nombre) {
        return nombre.endsWith(".mp3") || nombre.endsWith(".wav") || nombre.endsWith(".wma");
    }

    private void moverArchivo(File archivo, File carpetaDestino) {
        File destino = new File(carpetaDestino, archivo.getName());
        archivo.renameTo(destino);
    }

    private void ordenarArchivos(File[] archivos) {
        String opcion = cmbOrdenar == null ? "Nombre" : cmbOrdenar.getSelectedItem().toString();

        if (opcion.equals("Nombre")) {
            Arrays.sort(archivos, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        } else if (opcion.equals("Fecha")) {
            Arrays.sort(archivos, Comparator.comparingLong(File::lastModified));
        } else if (opcion.equals("Tipo")) {
            Arrays.sort(archivos, Comparator.comparing(this::obtenerExtension));
        } else if (opcion.equals("Tamaño")) {
            Arrays.sort(archivos, Comparator.comparingLong(File::length));
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
    
    public void setAccionCerrar(Runnable accionCerrar){
        this.accionCerrar = accionCerrar;
    }
    
    public void cerrar(){
        if(accionCerrar != null){
            accionCerrar.run();
        }
    }
}