package ui;

import model.Proyecto;
import model.StringEntry;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de la tabla:  [ID] [EN] [ES] ...
 *
 * Filtros aplicables:
 *  - búsqueda de texto (en ID o traducciones)
 *  - traducción (all / translated / missing)
 *  - idioma concreto
 *  - grupo activo (o null para mostrar todos)
 */
public class TranslationTableModel extends AbstractTableModel {

    private final Proyecto proyecto;
    private final List<StringEntry> filasVisibles = new ArrayList<>();

    private String busqueda = "";
    private String filtroTraduccion = "all";
    private String idiomaFiltro = "all";
    private String grupoFiltro = null; // id completo de grupo o null

    public TranslationTableModel(Proyecto proyecto) {
        this.proyecto = proyecto;
        recalcularFilas();
    }

    // ---- Filtros ----

    public void setBusqueda(String t) {
        this.busqueda = t == null ? "" : t.trim().toLowerCase();
        recalcularFilas();
    }

    public void setFiltroTraduccion(String v) {
        this.filtroTraduccion = v;
        recalcularFilas();
    }

    public void setIdiomaFiltro(String v) {
        this.idiomaFiltro = v;
        recalcularFilas();
    }

    /** null = sin filtro de grupo. */
    public void setGrupoFiltro(String grupoId) {
        this.grupoFiltro = grupoId;
        recalcularFilas();
    }

    public void recalcularFilas() {
        filasVisibles.clear();
        for (StringEntry e : proyecto.getStrings().values()) {
            if (pasaFiltros(e)) filasVisibles.add(e);
        }
        fireTableDataChanged();
    }

    private boolean pasaFiltros(StringEntry e) {
        return pasaGrupo(e) && pasaBusqueda(e) && pasaTraduccion(e);
    }

    private boolean pasaGrupo(StringEntry e) {
        if (grupoFiltro == null) return true;
        String g = e.getGrupoId();
        // Incluye subgrupos: "story" incluye "story.intro" etc.
        if (grupoFiltro.isEmpty()) return true; // raíz = todo
        return g.equals(grupoFiltro) || g.startsWith(grupoFiltro + ".");
    }

    private boolean pasaBusqueda(StringEntry e) {
        if (busqueda.isEmpty()) return true;
        if (e.getId().toLowerCase().contains(busqueda)) return true;
        for (String t : e.getTraducciones().values()) {
            if (t != null && t.toLowerCase().contains(busqueda)) return true;
        }
        return false;
    }

    private boolean pasaTraduccion(StringEntry e) {
        if ("all".equals(filtroTraduccion)) return true;

        List<String> idiomas = new ArrayList<>();
        if ("all".equals(idiomaFiltro)) idiomas.addAll(proyecto.getIdiomas().keySet());
        else idiomas.add(idiomaFiltro);

        boolean tieneAlguna = false;
        for (String id : idiomas) {
            if (!e.estaVacioEn(id)) { tieneAlguna = true; break; }
        }

        return switch (filtroTraduccion) {
            case "translated" -> tieneAlguna;
            case "missing"    -> !tieneAlguna;
            default           -> true;
        };
    }

    // ---- AbstractTableModel ----

    @Override public int getRowCount() { return filasVisibles.size(); }

    @Override public int getColumnCount() { return 1 + proyecto.getIdiomas().size(); }

    @Override public String getColumnName(int col) {
        if (col == 0) return "ID";
        return new ArrayList<>(proyecto.getIdiomas().keySet()).get(col - 1);
    }

    @Override public boolean isCellEditable(int f, int c) { return true; }

    @Override public Object getValueAt(int f, int c) {
        StringEntry e = filasVisibles.get(f);
        if (c == 0) return e.getId();
        return e.getTraduccion(getColumnName(c));
    }

    @Override public void setValueAt(Object v, int f, int c) {
        StringEntry e = filasVisibles.get(f);
        String texto = v == null ? "" : v.toString();

        if (c == 0) {
            String nuevo = texto.trim();
            if (nuevo.isEmpty() || nuevo.equals(e.getId())) return;
            if (proyecto.getString(nuevo) != null) return; // ya existe
            proyecto.renombrarString(e.getId(), nuevo);
            recalcularFilas();
        } else {
            e.setTraduccion(getColumnName(c), texto);
            if (!"all".equals(filtroTraduccion)) recalcularFilas();
        }
        fireTableRowsUpdated(f, f);
    }

    // ---- Helpers ----

    public StringEntry getEntryAt(int fila) {
        if (fila < 0 || fila >= filasVisibles.size()) return null;
        return filasVisibles.get(fila);
    }

    public int getFilaDe(String id) {
        for (int i = 0; i < filasVisibles.size(); i++) {
            if (filasVisibles.get(i).getId().equals(id)) return i;
        }
        return -1;
    }

    public Proyecto getProyecto() { return proyecto; }
}