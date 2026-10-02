package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Nodo del árbol de grupos.
 *
 * El id es el path completo separado por puntos (ej: "story.intro").
 * El grupo raíz tiene id "" (cadena vacía).
 *
 * Los grupos se derivan automáticamente de los IDs de los strings:
 *   string "story.intro.001"  -> grupo "story" -> subgrupo "intro"
 *   string "menu_continue"    -> va directo a la raíz
 */
public class Grupo {

    private final String id;                 // path completo, "" para raíz
    private String nombre;                   // nombre visible
    private String colorHex;                 // opcional, null = sin color
    private Grupo padre;                     // null si es raíz
    private final List<Grupo> hijos = new ArrayList<>();
    private final List<StringEntry> strings = new ArrayList<>();

    public Grupo(String id) {
        this.id = id;
        this.nombre = calcularNombrePorDefecto(id);
    }

    /** Nombre por defecto: último segmento, o "(raíz)" para el nodo raíz. */
    private static String calcularNombrePorDefecto(String id) {
        if (id == null || id.isEmpty()) return "(raíz)";
        int i = id.lastIndexOf('.');
        return i < 0 ? id : id.substring(i + 1);
    }

    // ---- Getters y setters ----

    public String getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        this.nombre = (nombre == null || nombre.isBlank()) ? calcularNombrePorDefecto(id) : nombre;
    }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public Grupo getPadre() { return padre; }
    public void setPadre(Grupo padre) { this.padre = padre; }

    public List<Grupo> getHijos() { return hijos; }
    public List<StringEntry> getStrings() { return strings; }

    public void addHijo(Grupo hijo) {
        hijo.setPadre(this);
        hijos.add(hijo);
    }

    public void addString(StringEntry entry) {
        strings.add(entry);
    }

    /** Devuelve un hijo por su id completo o null si no existe. */
    public Grupo getHijo(String idHijo) {
        for (Grupo h : hijos) {
            if (h.getId().equals(idHijo)) return h;
        }
        return null;
    }

    /** Número de strings en este grupo (solo los directos). */
    public int contarStringsDirectos() { return strings.size(); }

    /** Número de strings en este grupo y todos sus descendientes. */
    public int contarStringsTotales() {
        int total = strings.size();
        for (Grupo h : hijos) total += h.contarStringsTotales();
        return total;
    }

    /** Recorre este grupo y todos sus descendientes, invocando el consumer. */
    public void recorrer(java.util.function.Consumer<Grupo> accion) {
        accion.accept(this);
        for (Grupo h : hijos) h.recorrer(accion);
    }

    @Override
    public String toString() {
        // Se usa como texto del nodo en el JTree
        int total = contarStringsTotales();
        return nombre + " (" + total + ")";
    }
}