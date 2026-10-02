package model;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Una string traducible: ID + Traducciones por cada idooma
 * LinkedHashMap para preservar el orden de los idiomas
 */
public class StringEntry {
    private String id;
    private final Map<String, String> traducciones = new LinkedHashMap<>();

    public StringEntry(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
    public void setId(String newId) {
        // Hacemos que si no es == con .equals
        // Se asigne la nueva id
        if (!this.id.equals(newId)) {
            this.id = newId;
        }
    }

    public Map<String,String> getTraducciones() {
        return traducciones;
    }

    public String getTraduccion(String idioma) {
        return traducciones.getOrDefault(idioma, "");
    }

    public void setTraduccion(String idioma, String texto) {
        // Si no hay texto lo ponemos vacio
        traducciones.put(idioma, texto == null ? "" : texto);
    }

    public boolean estaVacioEn(String idioma) {
        return getTraduccion(idioma).isBlank();
    }

    // Devuelve el grupo al que pertenece segun su id (todo menos el ultimo segmento)
    public String getGrupoId() {
        int i = id.lastIndexOf('.');
        return i < 0 ? "" : id.substring(0, i);
    }


}
