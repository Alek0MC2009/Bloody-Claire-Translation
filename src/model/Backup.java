package model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Representa el contenido de backup.json.
 * Solo guarda metadatos de la app: no duplica traducciones.
 */
public class Backup {

    public int version = 1;
    public Map<String, GrupoMetadata> grupos = new LinkedHashMap<>();
    public Set<String> colapsados = new LinkedHashSet<>();
    public String ultimoIdioma;

    /** Metadatos opcionales de un grupo concreto. */
    public static class GrupoMetadata {
        public String nombre;      // nombre visible, si difiere del automático
        public String colorHex;    // color en hex, ej: "#FFAA00"
        public Integer orden;      // posición entre hermanos, null = orden natural

        public GrupoMetadata() {}

        public GrupoMetadata(String nombre, String colorHex, Integer orden) {
            this.nombre = nombre;
            this.colorHex = colorHex;
            this.orden = orden;
        }
    }
}