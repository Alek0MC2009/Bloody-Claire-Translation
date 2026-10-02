package ui;

import javax.swing.*;

/**
 * Toolbar superior: acciones de proyecto.
 */
public class ActionToolbar extends JToolBar {

    public ActionToolbar(Runnable onNuevoProyecto,
                         Runnable onAbrirProyecto,
                         Runnable onGuardar,
                         Runnable onAddIdioma,
                         Runnable onAddString,
                         Runnable onDeleteString,
                         Runnable onGuardarBackup) {

        setFloatable(false);

        add(boton("Nuevo", "Crear proyecto nuevo", onNuevoProyecto));
        add(boton("Abrir", "Abrir carpeta de proyecto", onAbrirProyecto));
        add(boton("Guardar", "Guardar lang + strings + backup", onGuardar));
        addSeparator();
        add(boton("+ Idioma", "Añadir idioma nuevo", onAddIdioma));
        addSeparator();
        add(boton("+ String", "Añadir string", onAddString));
        add(boton("- String", "Borrar string seleccionada", onDeleteString));
        addSeparator();
        add(boton("Backup", "Guardar metadatos de grupos", onGuardarBackup));
    }

    private JButton boton(String texto, String tooltip, Runnable accion) {
        JButton b = new JButton(texto);
        b.setToolTipText(tooltip);
        b.setFocusable(false);
        b.addActionListener(e -> accion.run());
        return b;
    }
}