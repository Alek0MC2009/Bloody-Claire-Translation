package ui;

import util.Dialogos;

import javax.swing.*;
import java.awt.*;

/**
 * Toolbar de tags del sistema de diálogos.
 * Inserta texto en la celda activa.
 */
public class TagToolbar extends JToolBar {

    private final TagInserter inserter;
    private final JFrame padre;

    public TagToolbar(JFrame padre, TagInserter inserter) {
        this.padre = padre;
        this.inserter = inserter;

        setFloatable(false);

        addTag("&", "&", "Salto de línea");
        addTag("&&", "&&", "Ampersand literal");
        addTag("/df", "/df", "Resetear speaker / voz / color");
        addTag("^ms", null, "Pausa en milisegundos");
        addSeparator();

        addTag("/NL", "/NL", "Nombre de Lara");
        addTag("/NA", "/NA", "Nombre de Ale");
        addTag("/NX", "/NX", "Nombre de Ximena");
        addTag("/MN", "/MN", "Mostrar dinero");
        addSeparator();

        addTag("/wave", "/wave", "Efecto wave");
        addTag("/shake", "/shake", "Efecto shake");
        addTag("/glitch", "/glitch", "Efecto glitch");
        addSeparator();

        addTag("/sfx:", null, "Reproducir un sonido");
        addTag("/sprite:", null, "Cambiar sprite");
        addSeparator();

        add(new JLabel(" color "));
        add(combo("/c", "Insertar /cN"));
        add(new JLabel(" expr "));
        add(combo("/f", "Insertar /fN"));
        addSeparator();

        addTag("/C{}", null, "Menú de elección");
    }

    private void addTag(String etiqueta, String tag, String tooltip) {
        JButton b = new JButton(etiqueta);
        b.setToolTipText(tooltip);
        b.setFocusable(false);
        b.addActionListener(e -> {
            if (tag != null) inserter.insertar(tag);
            else insertarConDialogo(etiqueta);
        });
        add(b);
    }

    private JComboBox<Integer> combo(String prefijo, String tooltip) {
        Integer[] vals = {0,1,2,3,4,5,6,7,8,9};
        JComboBox<Integer> c = new JComboBox<>(vals);
        c.setToolTipText(tooltip);
        c.setFocusable(false);
        c.setMaximumSize(new Dimension(60, 26));
        c.addActionListener(e -> {
            Integer n = (Integer) c.getSelectedItem();
            if (n != null) inserter.insertar(prefijo + n);
        });
        return c;
    }

    private void insertarConDialogo(String etiqueta) {
        switch (etiqueta) {
            case "^ms" -> {
                String ms = Dialogos.pedirTexto(padre, "Pausa", "Duración en ms:", "30");
                if (ms != null && ms.matches("\\d+")) inserter.insertar("^" + ms);
            }
            case "/sfx:" -> {
                String s = Dialogos.pedirTexto(padre, "Insertar /sfx:", "Nombre del sonido:", "");
                if (s != null && !s.isBlank()) inserter.insertar("/sfx:" + s.trim() + " ");
            }
            case "/sprite:" -> {
                String s = Dialogos.pedirTexto(padre, "Insertar /sprite:", "Nombre del sprite:", "");
                if (s != null && !s.isBlank()) inserter.insertar("/sprite:" + s.trim() + " ");
            }
            case "/C{}" -> {
                String s = Dialogos.pedirTexto(padre, "Insertar elección",
                        "Opciones separadas por | (ej: Si|No|Quizás)", "");
                if (s != null && !s.isBlank()) inserter.insertar("/C{" + s.trim() + "}");
            }
        }
    }
}