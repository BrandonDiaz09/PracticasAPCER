package Cliente;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.File;
import java.util.Objects;

public class InterfazGrafica extends JFrame {
    private static Cliente cliente;
    private JPanel panel1;
    private JTree tree1;
    private JButton borrarButton;
    private JButton cargarArchivosButton;
    private JButton crearButton;
    private JButton enviarButton;
    private JTextField textField1;
    private JTextField textField2;
    private JComboBox comboBox1;
    private JButton cabiarCarpetaBaseButton;
    private JButton salirButton;
    private DefaultTreeModel modelo;
    private DefaultMutableTreeNode nodoSeleccionado;
    private StringBuilder rutaCompleta;

    public InterfazGrafica(Cliente cliente) {
        $$$setupUI$$$();
        initComponents();
        this.cliente = cliente;  // Asignar la instancia de Cliente al atributo cliente
        add(panel1);
        listarLocal();

    }

    private void makeModelDirectory(DefaultTreeModel modelo, DefaultMutableTreeNode nodoPadre, File[] afiles){
        for (File file : afiles
        ) {
            if (file.isDirectory()){
                File[] new_afiles = file.listFiles();
                DefaultMutableTreeNode n = new DefaultMutableTreeNode(file.getName());
                modelo.insertNodeInto(n, nodoPadre, nodoPadre.getChildCount());
                makeModelDirectory(modelo, n,new_afiles);
            }
            else{
                DefaultMutableTreeNode n = new DefaultMutableTreeNode(file.getName());
                modelo.insertNodeInto(n, nodoPadre, nodoPadre.getChildCount());
            }

        }
    }
    private void listarLocal() {
        modelo = new DefaultTreeModel(new DefaultMutableTreeNode(cliente.getRutaCLocal()));
        tree1.setModel(modelo);
        File archivo = new File(cliente.getRutaCLocal());
        File[] afiles = archivo.listFiles();
        DefaultMutableTreeNode nodoRaiz = (DefaultMutableTreeNode) modelo.getRoot();
        makeModelDirectory(modelo, nodoRaiz,afiles);
    }


    private void listarRemoto() {
        modelo = new DefaultTreeModel(new DefaultMutableTreeNode(cliente.getRutaCRemote()));
        tree1.setModel(modelo);
        File localFolder = new File(cliente.getRutaCRemote());
        String[] archivosRemotos = localFolder.list();
        DefaultMutableTreeNode nodoRaiz = (DefaultMutableTreeNode) modelo.getRoot();
        if (archivosRemotos.length != 0) {
            for (String localContent : archivosRemotos
            ) {
                DefaultMutableTreeNode n = new DefaultMutableTreeNode(localContent);
                modelo.insertNodeInto(n, nodoRaiz, nodoRaiz.getChildCount());
            }
        }
    }

    private void crearCarpetaLocal(String nombreCarpeta) {
        cliente.NewFolderLocal(nombreCarpeta);
        listarLocal();
    }

    private void crearCarpetaRemota(String nombreCarpeta) {
        cliente.NewFolderInRemote(nombreCarpeta);
        listarRemoto();
    }

    private void eliminarCarpetaLocal(String carpetaAEliminar) {
        cliente.eliminarArchivo(carpetaAEliminar);
        listarLocal();
    }

    private void eliminarCarpetaRemota(String carpetaAEliminar) {
        cliente.eliminarRemoto(carpetaAEliminar);
        listarRemoto();
    }

    private void cambiarDirectorioLocal() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // Esta línea debe ir antes de mostrar el diálogo
        int response = fileChooser.showOpenDialog(null);
        if (response == JFileChooser.APPROVE_OPTION) { // Debes verificar si el usuario presionó el botón de aprobar
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                String nuevaRutaLocal = selectedFile.getAbsolutePath();
                cliente.ChangeFileBaseLocal(nuevaRutaLocal);
                listarLocal();
            } else {
                System.out.println("No se seleccionó ningún directorio.");
            }
        } else {
            System.out.println("Cambio de directorio cancelado por el usuario.");
        }
    }

    private void cambiarDirectorioRemoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // Esta línea debe ir antes de mostrar el diálogo
        int response = fileChooser.showOpenDialog(null);
        if (response == JFileChooser.APPROVE_OPTION) { // Debes verificar si el usuario presionó el botón de aprobar
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                String nuevaRutaRemota = selectedFile.getAbsolutePath();
                cliente.ChangeFileBaseRemote(nuevaRutaRemota);
                listarLocal();
            } else {
                System.out.println("No se seleccionó ningún directorio.");
            }
        } else {
            System.out.println("Cambio de directorio cancelado por el usuario.");
        }
    }

    private void salir() {
        cliente.finishConection();
        System.exit(0);
    }
    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Aplicación de Almacenamiento Remoto de Archivos");
        comboBox1.addItem("Local");
        comboBox1.addItem("Remoto");
        cargarArchivosButton.addActionListener(evt -> {
            String seleccion = (String) comboBox1.getSelectedItem();
            switch (seleccion) {
                case "Local":
                    listarLocal();
                    break;
                case "Remoto":
                    listarRemoto();
                    break;
                default:
                    break;
            }
        });
        crearButton.addActionListener(evt -> {
            String seleccion = (String) comboBox1.getSelectedItem();
            String nombreCarpeta = textField1.getText();
            if (!Objects.equals(nombreCarpeta, "")) {
                switch (seleccion) {
                    case "Local":
                        crearCarpetaLocal(nombreCarpeta);
                        break;

                    case "Remoto":
                        crearCarpetaRemota(nombreCarpeta);
                        break;
                    default:
                        break;
                }
            }
        });
        tree1.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                nodoSeleccionado = (DefaultMutableTreeNode) tree1.getLastSelectedPathComponent();
                if (nodoSeleccionado != null) {
                    // Establecer el objeto de usuario del nodo seleccionado en textField2
                    textField2.setText((String) nodoSeleccionado.getUserObject());

                    // Obtener la ruta completa desde la raíz hasta el nodo seleccionado
                    TreeNode[] nodes = nodoSeleccionado.getPath();
                    rutaCompleta = new StringBuilder();
                    for (int i = 0; i < nodes.length; i++) {
                        rutaCompleta.append(nodes[i].toString());
                        if (i < nodes.length - 1) {
                            rutaCompleta.append("/");
                        }
                    }

                    // Mostrar la ruta completa en la consola o en algún otro componente
                    //System.out.println("Ruta completa del nodo: " + rutaCompleta.toString());

                    // Si deseas mostrar esta ruta en otro campo de texto u otro componente, puedes hacerlo aquí
                    // por ejemplo: textFieldRuta.setText(rutaCompleta.toString());
                }
            }
        });

        borrarButton.addActionListener(evt -> {
            String seleccion = (String) comboBox1.getSelectedItem();
            String carpetaAEliminar = (String) nodoSeleccionado.getUserObject();
            if (nodoSeleccionado != null) {
                switch (seleccion) {
                    case "Local":
                        eliminarCarpetaLocal(rutaCompleta.toString());
                        break;

                    case "Remoto":
                        eliminarCarpetaRemota(rutaCompleta.toString());
                        break;
                    default:
                        break;
                }
            }
        });
        cabiarCarpetaBaseButton.addActionListener(evt -> {
            String seleccion = (String) comboBox1.getSelectedItem();
            switch (seleccion) {
                case "Local":
                    cambiarDirectorioLocal();
                    break;

                case "Remoto":
                    cambiarDirectorioRemoto();
                    break;
                default:
                    break;
            }
        });
        enviarButton.addActionListener(evt -> {
            String seleccion = (String) comboBox1.getSelectedItem();
            String archivoAEnviar = (String) nodoSeleccionado.getUserObject();
            if (nodoSeleccionado != null) {
                switch (seleccion) {
                    case "Local":
                        cliente.enviarARemoto(archivoAEnviar,rutaCompleta.toString());
                        break;

                    case "Remoto":
                        cliente.enviarALocal(archivoAEnviar,rutaCompleta.toString());
                        break;
                    default:
                        break;
                }
            }
        });
        salirButton.addActionListener(evt -> {
            salir();
        });

    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(11, 2, new Insets(0, 0, 0, 0), -1, -1));
        tree1 = new JTree();
        panel1.add(tree1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 9, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(10, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        borrarButton = new JButton();
        borrarButton.setText("Borrar");
        panel1.add(borrarButton, new com.intellij.uiDesigner.core.GridConstraints(8, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cargarArchivosButton = new JButton();
        cargarArchivosButton.setText("Cargar Archivos");
        panel1.add(cargarArchivosButton, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        crearButton = new JButton();
        crearButton.setText("Crear");
        panel1.add(crearButton, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        enviarButton = new JButton();
        enviarButton.setText("Enviar");
        panel1.add(enviarButton, new com.intellij.uiDesigner.core.GridConstraints(9, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Crear carpeta:");
        panel1.add(label1, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField1 = new JTextField();
        panel1.add(textField1, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Carpeta/Archivo seleccionado:");
        panel1.add(label2, new com.intellij.uiDesigner.core.GridConstraints(6, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField2 = new JTextField();
        panel1.add(textField2, new com.intellij.uiDesigner.core.GridConstraints(7, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        comboBox1 = new JComboBox();
        panel1.add(comboBox1, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cabiarCarpetaBaseButton = new JButton();
        cabiarCarpetaBaseButton.setText("Cabiar Carpeta Base");
        panel1.add(cabiarCarpetaBaseButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        salirButton = new JButton();
        salirButton.setText("Salir");
        panel1.add(salirButton, new com.intellij.uiDesigner.core.GridConstraints(9, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

}
