package model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Estado completo del proyecto:
 *  - idiomas cargados
 *  - strings (orden canónico + traducciones)
 *  - orden de strings (para saber el orden original)
 *  - metadatos de grupos (backup)
 */
public class Proyecto {
    private java.nio.file.Path carpeta;
    private final Map<String, Idioma> idiomas = new LinkedHashMap<>();
    private final Map<String, StringEntry> strings = new LinkedHashMap<>();

    /** Orden canónico de los strings, tal como se guarda en strings.json. */
    private final java.util.List<String> ordenStrings = new java.util.ArrayList<>();

    /** Metadatos de grupos (del backup.json). */
    private final Map<String, Backup.GrupoMetadata> metadatosGrupos = new LinkedHashMap<>();

    /** Grupos colapsados en el árbol. */
    private final java.util.Set<String> gruposColapsados = new java.util.LinkedHashSet<>();

    // ---- IDIOMAS ----

    public Map<String, Idioma> getIdiomas() { return idiomas; }

    public boolean tieneIdioma(String codigo) { return idiomas.containsKey(codigo); }

    public void addIdioma(Idioma idioma) { idiomas.put(idioma.getCodigo(), idioma); }

    // ---- STRINGS ----

    public Map<String, StringEntry> getStrings() { return strings; }

    public StringEntry getString(String id) { return strings.get(id); }

    public StringEntry getOCrearString(String id) {
        StringEntry e = strings.get(id);
        if (e == null) {
            e = new StringEntry(id);
            strings.put(id, e);
            if (!ordenStrings.contains(id)) ordenStrings.add(id);
        }
        return e;
    }

    public void addString(String id) {
        if (!strings.containsKey(id)) {
            strings.put(id, new StringEntry(id));
            ordenStrings.add(id);
        }
    }

    public void removeString(String id) {
        strings.remove(id);
        ordenStrings.remove(id);
    }

    /** Renombra un string conservando el orden y moviéndolo de grupo si cambia el prefijo. */
    public boolean renombrarString(String viejoId, String nuevoId) {
        if (!strings.containsKey(viejoId)) return false;
        if (strings.containsKey(nuevoId)) return false;

        // Reconstruimos el LinkedHashMap para cambiar la clave sin perder el orden
        Map<String, StringEntry> nuevo = new LinkedHashMap<>();
        for (Map.Entry<String, StringEntry> e : strings.entrySet()) {
            if (e.getKey().equals(viejoId)) {
                StringEntry entry = e.getValue();
                entry.setId(nuevoId);
                nuevo.put(nuevoId, entry);
            } else {
                nuevo.put(e.getKey(), e.getValue());
            }
        }
        strings.clear();
        strings.putAll(nuevo);

        // Reemplazar en el orden canónico (misma posición)
        int idx = ordenStrings.indexOf(viejoId);
        if (idx >= 0) ordenStrings.set(idx, nuevoId);
        else ordenStrings.add(nuevoId);

        return true;
    }

    /** Genera un id nuevo tipo "new_string", "new_string_1", ... */
    public String generarNuevoId() {
        String base = "new_string";
        String id = base;
        int n = 0;
        while (strings.containsKey(id)) {
            n++;
            id = base + "_" + n;
        }
        return id;
    }

    // ---- ORDEN ----

    public java.util.List<String> getOrdenStrings() { return ordenStrings; }

    /**
     * Aplica el orden canónico a los strings.
     * Los que no estén en la lista se añaden al final.
     */
    public void aplicarOrden() {
        Map<String, StringEntry> reordenado = new LinkedHashMap<>();
        for (String id : ordenStrings) {
            StringEntry e = strings.get(id);
            if (e != null) reordenado.put(id, e);
        }
        for (Map.Entry<String, StringEntry> e : strings.entrySet()) {
            if (!reordenado.containsKey(e.getKey())) reordenado.put(e.getKey(), e.getValue());
        }
        strings.clear();
        strings.putAll(reordenado);
    }

    // ---- METADATOS DE GRUPOS ----

    public Map<String, Backup.GrupoMetadata> getMetadatosGrupos() { return metadatosGrupos; }

    public java.util.Set<String> getGruposColapsados() { return gruposColapsados; }

    public java.nio.file.Path getCarpeta() { return carpeta; }
    public void setCarpeta(java.nio.file.Path carpeta) { this.carpeta = carpeta; }
    public java.nio.file.Path rutaLang(String codigo) {
        if (carpeta == null) throw new IllegalStateException("Proyecto sin carpeta asignada.");
        return carpeta.resolve("lang_" + codigo + ".json");
    }
}