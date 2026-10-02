package ui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;

/**
 * Inserta texto (tags) en la celda que el usuario está editando.
 */
public class TagInserter {

    private final JTable tabla;

    public TagInserter(JTable tabla) {
        this.tabla = tabla;
    }

    public void insertar(String tag) {
        if (!tabla.isEditing()) {
            int fila = tabla.getSelectedRow();
            int col = tabla.getSelectedColumn();
            if (fila < 0 || col <= 0) return; // no hay celda o es la columna ID
            tabla.editCellAt(fila, col);
        }

        Component editor = tabla.getEditorComponent();
        if (!(editor instanceof JTextComponent tc)) return;

        try {
            int pos = tc.getCaretPosition();
            tc.getDocument().insertString(pos, tag, null);
            tc.setCaretPosition(pos + tag.length());
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }

        tc.requestFocusInWindow();
    }
}