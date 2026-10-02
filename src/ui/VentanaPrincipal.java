package ui;

import model.Idioma;
import model.Proyecto;
import service.BackupService;
import service.JsonService;
import service.StringsMaestroService;
import util.Dialogos;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Ventana principal.
 *
 *  - Toolbar superior: abrir / guardar / add / delete / backup
 *  - Toolbar de tags: tags del sistema de diálogos
 *  - Panel búsqueda y filtros
 *  - Split: árbol de grupos a la izquierda, tabla de traducciones a la derecha
 *  - Status bar inferior
 */
public class VentanaPrincipal extends JFrame {
    // CONFIG
 //   private final String windowName = "BLC Dialogue Editor";

    private final Proyecto proyecto = new Proyecto();
    private final JsonService jsonService = new JsonService();
    private final BackupService backupService = new BackupService();
    private final StringsMaestroService maestroService = new StringsMaestroService();
    private final service.ProyectoService proyectoService = new service.ProyectoService();
    private Path carpetaProyecto;

    private final TranslationTableModel tableModel;
    private final JTable tabla;
    private final TagInserter tagInserter;
    private final GrupoTreePanel grupoPanel;
    private final SearchFilterPanel searchPanel;
    private final JLabel statusBar;

    public VentanaPrincipal() {
        super("BLC Dialogue Editor");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 760);
        setLocationRelativeTo(null);

        // Modelo y tabla
        tableModel = new TranslationTableModel(proyecto);
        tabla = new JTable(tableModel);
        tabla.setRowHeight(22);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setCellSelectionEnabled(true);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        tagInserter = new TagInserter(tabla);

        // Panel de grupos
        grupoPanel = new GrupoTreePanel(proyecto);
        grupoPanel.setOnGrupoSeleccionado(idGrupo -> {
            tableModel.setGrupoFiltro(idGrupo);
            actualizarStatus();
        });

        // Búsqueda
        searchPanel = new SearchFilterPanel(
                tableModel::setBusqueda,
                tableModel::setFiltroTraduccion,
                tableModel::setIdiomaFiltro);

        // Status
        statusBar = new JLabel(" Listo ");
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());

        // Toolbars
        ActionToolbar actionToolbar = new ActionToolbar(
                this::nuevoProyecto,
                this::abrirProyecto,
                this::guardarTodo,
                this::añadirIdioma,
                this::addString,
                this::deleteString,
                this::guardarBackupManual);

        TagToolbar tagToolbar = new TagToolbar(this, tagInserter);

        // Layout superior
        JPanel norte = new JPanel(new BorderLayout());
        JPanel toolbars = new JPanel(new BorderLayout());
        toolbars.add(actionToolbar, BorderLayout.NORTH);
        toolbars.add(tagToolbar, BorderLayout.SOUTH);
        norte.add(toolbars, BorderLayout.NORTH);
        norte.add(searchPanel, BorderLayout.SOUTH);

        // Split
        JScrollPane scrollTabla = new JScrollPane(tabla);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, grupoPanel, scrollTabla);
        split.setDividerLocation(260);
        split.setResizeWeight(0.0);

        setJMenuBar(crearMenuBar());
        add(norte, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        instalarAtajos();
        actualizarStatus();
    }

    // ==========================================================
    // MENÚ
    // ==========================================================

    private JMenuBar crearMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("File");
        file.add(menuItem("Nuevo proyecto...", "control shift N", this::nuevoProyecto));
        file.add(menuItem("Abrir proyecto...", "control O", this::abrirProyecto));
        file.addSeparator();
        file.add(menuItem("Guardar todo", "control S", this::guardarTodo));
        file.add(menuItem("Guardar backup", null, this::guardarBackupManual));
        file.addSeparator();
        file.add(menuItem("Salir", null, this::dispose));
        bar.add(file);

        JMenu edit = new JMenu("Edit");
        edit.add(menuItem("Añadir idioma...", null, this::añadirIdioma));
        edit.add(menuItem("Añadir string", "control N", this::addString));
        edit.add(menuItem("Borrar string", null, this::deleteString));
        edit.addSeparator();
        edit.add(menuItem("Buscar...", "control F", searchPanel::enfocarBusqueda));
        bar.add(edit);

        return bar;
    }

    private JMenuItem menuItem(String texto, String atajo, Runnable accion) {
        JMenuItem item = new JMenuItem(texto);
        if (atajo != null) item.setAccelerator(KeyStroke.getKeyStroke(atajo));
        item.addActionListener(e -> accion.run());
        return item;
    }

    // ==========================================================
    // ACCIONES
    // ==========================================================

    private void abrirProyecto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Selecciona la carpeta del proyecto");

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        carpetaProyecto = chooser.getSelectedFile().toPath();
        cargarProyecto(carpetaProyecto);
    }

    private void cargarProyecto(Path carpeta) {
        try {
            // Resetear por si ya había algo
            limpiarProyecto();

            proyecto.setCarpeta(carpeta);
            carpetaProyecto = carpeta;

            // 1. strings.json
            maestroService.cargar(proyecto, carpeta.resolve(StringsMaestroService.NOMBRE_ARCHIVO));

            // 2. backup.json
            backupService.cargar(proyecto, carpeta.resolve(BackupService.NOMBRE_ARCHIVO));

            // 3. lang_*.json
            List<Path> langs = new ArrayList<>();
            try (Stream<Path> files = Files.list(carpeta)) {
                files.filter(p -> {
                    String n = p.getFileName().toString();
                    return n.startsWith("lang_") && n.endsWith(".json");
                }).forEach(langs::add);
            }

            List<String> errores = new ArrayList<>();
            for (Path p : langs) {
                try {
                    jsonService.importar(proyecto, p);
                } catch (IOException ex) {
                    errores.add(p.getFileName() + ": " + ex.getMessage());
                }
            }

            proyecto.aplicarOrden();
            searchPanel.actualizarIdiomas(proyecto.getIdiomas().keySet());
            refrescarTodo();

            if (!errores.isEmpty()) {
                Dialogos.aviso(this, "Errores al cargar:\n\n" + String.join("\n", errores));
            }
        } catch (IOException ex) {
            Dialogos.error(this, "No se pudo abrir el proyecto:\n" + ex.getMessage());
        }
    }

    private void ajustarColumnas() {
        if (tabla.getColumnCount() == 0) return;
        tabla.getColumnModel().getColumn(0).setPreferredWidth(200);
        for (int i = 1; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(340);
        }
    }

    private void guardarTodo() {
        if (carpetaProyecto == null) {
            Dialogos.aviso(this, "No hay proyecto abierto.");
            return;
        }
        try {
            jsonService.guardarTodo(proyecto);
            maestroService.guardar(proyecto, carpetaProyecto.resolve(StringsMaestroService.NOMBRE_ARCHIVO));
            backupService.guardar(proyecto, carpetaProyecto.resolve(BackupService.NOMBRE_ARCHIVO));
            Dialogos.aviso(this, "Guardado correctamente.");
            actualizarStatus();
        } catch (IOException ex) {
            Dialogos.error(this, "Error al guardar:\n" + ex.getMessage());
        }
    }

    private void guardarBackupManual() {
        if (carpetaProyecto == null) {
            Dialogos.aviso(this, "No hay proyecto abierto.");
            return;
        }
        try {
            backupService.guardar(proyecto, carpetaProyecto.resolve(BackupService.NOMBRE_ARCHIVO));
            Dialogos.aviso(this, "Backup guardado.");
        } catch (IOException ex) {
            Dialogos.error(this, "Error al guardar backup:\n" + ex.getMessage());
        }
    }

    private void addString() {
        String id = proyecto.generarNuevoId();
        // Si hay un grupo seleccionado, usar su prefijo
        String grupoActual = grupoPanel.getGrupoSeleccionado();
        if (grupoActual != null && !grupoActual.isEmpty()) {
            id = grupoActual + "." + id;
        }
        proyecto.addString(id);
        tableModel.recalcularFilas();
        grupoPanel.reconstruir();

        int fila = tableModel.getFilaDe(id);
        if (fila >= 0) {
            tabla.setRowSelectionInterval(fila, fila);
            tabla.scrollRectToVisible(tabla.getCellRect(fila, 0, true));
        }
        actualizarStatus();
    }

    private void deleteString() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Dialogos.aviso(this, "Selecciona una string primero.");
            return;
        }
        var entry = tableModel.getEntryAt(fila);
        if (entry == null) return;

        if (!Dialogos.confirmar(this, "Confirmar",
                "¿Borrar \"" + entry.getId() + "\"?")) return;

        proyecto.removeString(entry.getId());
        tableModel.recalcularFilas();
        grupoPanel.reconstruir();
        actualizarStatus();
    }

    // ==========================================================
    // ATAJOS
    // ==========================================================

    private void instalarAtajos() {
        JRootPane root = getRootPane();

        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), "buscar");
        root.getActionMap().put("buscar", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { searchPanel.enfocarBusqueda(); }
        });

        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "guardar");
        root.getActionMap().put("guardar", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { guardarTodo(); }
        });

        // Tras editar una celda, reconstruir el árbol (por si cambió el ID / grupo)
        tabla.addPropertyChangeListener("tableCellEditor", evt -> {
            if (!tabla.isEditing()) {
                grupoPanel.reconstruir();
            }
        });
    }

    // ==========================================================
    // STATUS
    // ==========================================================

    private void actualizarStatus() {
        int total = proyecto.getStrings().size();
        int visibles = tabla.getRowCount();
        int idiomas = proyecto.getIdiomas().size();
        statusBar.setText(String.format(" %d strings (%d visibles) · %d idiomas ",
                total, visibles, idiomas));
    }

    // ==========================================================
// NUEVO PROYECTO
// ==========================================================

    private void nuevoProyecto() {
        // 1. Elegir carpeta destino
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Elige la carpeta para el proyecto nuevo");

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        Path carpeta = chooser.getSelectedFile().toPath();

        // 2. Avisar si ya hay algo dentro
        if (Files.exists(carpeta) && contieneArchivos(carpeta)) {
            if (!Dialogos.confirmar(this, "Carpeta no vacía",
                    "La carpeta ya contiene archivos.\n¿Continuar de todas formas?")) {
                return;
            }
        }

        // 3. Pedir idiomas iniciales
        String idiomasStr = Dialogos.pedirTexto(this,
                "Idiomas iniciales",
                "Códigos de idioma separados por coma (ej: en,es,jp):",
                "en,es");

        if (idiomasStr == null) return; // cancelado

        List<String> codigos = new ArrayList<>();
        for (String s : idiomasStr.split(",")) {
            String c = s.trim().toLowerCase();
            if (!c.isEmpty()) codigos.add(c);
        }
        if (codigos.isEmpty()) {
            Dialogos.aviso(this, "Debes indicar al menos un idioma.");
            return;
        }

        try {
            // Resetear el proyecto en memoria
            limpiarProyecto();

            // Crear en disco
            Proyecto nuevo = proyectoService.crearNuevo(carpeta, codigos);

            // Copiar el estado al proyecto en memoria (que ya está referenciado por la UI)
            proyecto.setCarpeta(nuevo.getCarpeta());
            proyecto.getIdiomas().putAll(nuevo.getIdiomas());

            carpetaProyecto = carpeta;

            refrescarTodo();
            Dialogos.aviso(this, "Proyecto creado en:\n" + carpeta);
        } catch (IOException ex) {
            Dialogos.error(this, "No se pudo crear el proyecto:\n" + ex.getMessage());
        }
    }

    private boolean contieneArchivos(Path carpeta) {
        try (var s = Files.list(carpeta)) {
            return s.findAny().isPresent();
        } catch (IOException e) {
            return false;
        }
    }

// ==========================================================
// AÑADIR IDIOMA
// ==========================================================

    private void añadirIdioma() {
        if (carpetaProyecto == null) {
            Dialogos.aviso(this, "Abre o crea un proyecto primero.");
            return;
        }

        String codigo = Dialogos.pedirTexto(this,
                "Añadir idioma",
                "Código del nuevo idioma (ej: fr, de, jp):",
                "");
        if (codigo == null || codigo.isBlank()) return;

        try {
            proyectoService.agregarIdioma(proyecto, codigo.trim().toLowerCase());
            searchPanel.actualizarIdiomas(proyecto.getIdiomas().keySet());
            refrescarTodo();
        } catch (IOException ex) {
            Dialogos.error(this, "No se pudo añadir el idioma:\n" + ex.getMessage());
        }
    }

// ==========================================================
// HELPERS DE REFRESCO
// ==========================================================

    /** Limpia el proyecto en memoria (sin tocar disco). */
    private void limpiarProyecto() {
        proyecto.getIdiomas().clear();
        proyecto.getStrings().clear();
        proyecto.getOrdenStrings().clear();
        proyecto.getMetadatosGrupos().clear();
        proyecto.getGruposColapsados().clear();
        proyecto.setCarpeta(null);
        carpetaProyecto = null;
    }

    /** Refresca todos los componentes visuales tras cambiar el proyecto. */
    private void refrescarTodo() {
        tableModel.fireTableStructureChanged();
        tableModel.recalcularFilas();
        ajustarColumnas();
        grupoPanel.reconstruir();
        actualizarStatus();
    }
}