package ui;

import model.Grupo;
import model.Proyecto;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Árbol lateral de grupos.
 *
 * - Los grupos se derivan de los IDs (con puntos).
 * - Los nodos son colapsables.
 * - Al seleccionar un nodo, se notifica el id del grupo al listener.
 * - Los colores custom del backup se aplican a los nodos.
 */
public class GrupoTreePanel extends JPanel {

    private final Proyecto proyecto;
    private final JTree arbol;
    private final DefaultTreeModel modelo;
    private Consumer<String> onGrupoSeleccionado;

    public GrupoTreePanel(Proyecto proyecto) {
        this.proyecto = proyecto;
        setLayout(new BorderLayout());

        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("(todos)");
        modelo = new DefaultTreeModel(raiz);
        arbol = new JTree(modelo);
        arbol.setRootVisible(true);
        arbol.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        arbol.setCellRenderer(new GrupoRenderer());
        arbol.setRowHeight(20);

        // Al seleccionar un nodo, notificar
        arbol.addTreeSelectionListener(this::onSeleccion);

        JScrollPane scroll = new JScrollPane(arbol);
        scroll.setPreferredSize(new Dimension(260, 0));
        add(scroll, BorderLayout.CENTER);
    }

    public void setOnGrupoSeleccionado(Consumer<String> listener) {
        this.onGrupoSeleccionado = listener;
    }

    /** Reconstruye el árbol completo desde el proyecto. */
    public void reconstruir() {
        // Recordar qué grupo estaba seleccionado
        String seleccionado = getGrupoSeleccionado();

        DefaultMutableTreeNode raizUI = new DefaultMutableTreeNode("(todos)");
        Grupo raiz = service.GruposService.construirArbol(proyecto);
        agregarHijos(raizUI, raiz);

        modelo.setRoot(raizUI);
        arbol.expandRow(0);

        // Restaurar selección si es posible
        if (seleccionado != null) seleccionarPorId(seleccionado);
    }

    private void agregarHijos(DefaultMutableTreeNode nodoPadre, Grupo grupo) {
        for (Grupo hijo : grupo.getHijos()) {
            DefaultMutableTreeNode nodoHijo = new DefaultMutableTreeNode(hijo);
            agregarHijos(nodoHijo, hijo);
            nodoPadre.add(nodoHijo);
        }
        // Nota: los strings directos del grupo NO se muestran en el árbol;
        // se ven en la tabla al seleccionar el grupo.
    }

    private void onSeleccion( TreeSelectionEvent e) {
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) arbol.getLastSelectedPathComponent();
        if (nodo == null || onGrupoSeleccionado == null) return;

        Object user = nodo.getUserObject();
        if (user instanceof Grupo g) {
            onGrupoSeleccionado.accept(g.getId());
        } else {
            onGrupoSeleccionado.accept(null); // raíz "todos"
        }
    }

    public String getGrupoSeleccionado() {
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) arbol.getLastSelectedPathComponent();
        if (nodo == null) return null;
        Object user = nodo.getUserObject();
        return user instanceof Grupo g ? g.getId() : null;
    }

    private void seleccionarPorId(String id) {
        DefaultMutableTreeNode raizUI = (DefaultMutableTreeNode) modelo.getRoot();
        DefaultMutableTreeNode encontrado = buscarNodo(raizUI, id);
        if (encontrado != null) {
            var path = new javax.swing.tree.TreePath(encontrado.getPath());
            arbol.setSelectionPath(path);
            arbol.scrollPathToVisible(path);
        }
    }

    private DefaultMutableTreeNode buscarNodo(DefaultMutableTreeNode raiz, String id) {
        if (raiz.getUserObject() instanceof Grupo g && g.getId().equals(id)) return raiz;
        for (int i = 0; i < raiz.getChildCount(); i++) {
            DefaultMutableTreeNode r = buscarNodo((DefaultMutableTreeNode) raiz.getChildAt(i), id);
            if (r != null) return r;
        }
        return null;
    }

    /** Renderer que aplica el color del grupo al texto si está definido. */
    private static class GrupoRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof DefaultMutableTreeNode n && n.getUserObject() instanceof Grupo g) {
                if (g.getColorHex() != null) {
                    try {
                        setForeground(Color.decode(g.getColorHex()));
                    } catch (NumberFormatException ignored) {}
                }
            }
            return this;
        }
    }
}