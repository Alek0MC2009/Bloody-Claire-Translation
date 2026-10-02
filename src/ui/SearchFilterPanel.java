package ui;

import javax.swing.*;
import java.awt.*;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Panel de búsqueda y filtros.
 * Notifica los cambios mediante callbacks.
 */
public class SearchFilterPanel extends JPanel {

    private final JTextField campoBusqueda = new JTextField(20);
    private final JComboBox<String> comboFiltro = new JComboBox<>(
            new String[]{"All", "Translated", "Missing"});
    private final JComboBox<String> comboIdioma = new JComboBox<>();

    public SearchFilterPanel(Consumer<String> onBusqueda,
                             Consumer<String> onFiltro,
                             Consumer<String> onIdioma) {

        setLayout(new FlowLayout(FlowLayout.RIGHT, 6, 6));

        campoBusqueda.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { onBusqueda.accept(campoBusqueda.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { onBusqueda.accept(campoBusqueda.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { onBusqueda.accept(campoBusqueda.getText()); }
        });

        comboFiltro.addActionListener(e -> {
            String v = (String) comboFiltro.getSelectedItem();
            String f = switch (v) {
                case "Translated" -> "translated";
                case "Missing" -> "missing";
                default -> "all";
            };
            onFiltro.accept(f);
        });

        comboIdioma.addActionListener(e -> {
            String v = (String) comboIdioma.getSelectedItem();
            onIdioma.accept(v == null ? "all" : v);
        });

        add(new JLabel("Search:"));
        add(campoBusqueda);
        add(new JLabel("Filtro:"));
        add(comboFiltro);
        add(new JLabel("Idioma:"));
        add(comboIdioma);

        actualizarIdiomas(Set.of());
    }

    public void actualizarIdiomas(Set<String> codigos) {
        String actual = (String) comboIdioma.getSelectedItem();
        comboIdioma.removeAllItems();
        comboIdioma.addItem("all");
        for (String c : codigos) comboIdioma.addItem(c);
        if (actual != null && (codigos.contains(actual) || "all".equals(actual))) {
            comboIdioma.setSelectedItem(actual);
        }
    }

    public void enfocarBusqueda() {
        campoBusqueda.requestFocusInWindow();
        campoBusqueda.selectAll();
    }
}