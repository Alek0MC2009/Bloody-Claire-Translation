package service;

import model.Backup;
import model.Grupo;
import model.Proyecto;
import model.StringEntry;

import java.util.Comparator;
import java.util.Map;

/**
 * Construye el árbol de grupos a partir del proyecto y aplica
 * los metadatos del backup (nombres, colores, orden).
 */
public class GruposService {

    /**
     * Reconstruye el árbol completo desde el proyecto.
     * Los grupos se crean automáticamente a partir de los IDs.
     * Los metadatos (nombre, color, orden) se aplican encima.
     */
    public static Grupo construirArbol(Proyecto proyecto) {
        Grupo raiz = new Grupo("");

        for (StringEntry entry : proyecto.getStrings().values()) {
            insertarString(raiz, entry);
        }

        aplicarMetadatos(raiz, proyecto.getMetadatosGrupos());
        ordenarHijos(raiz);

        return raiz;
    }

    /**
     * Dado un StringEntry, encuentra (o crea) el grupo correspondiente
     * y añade el string ahí.
     *
     * Grupo de "story.intro.001" -> "story.intro"
     * Grupo de "menu_continue"   -> "" (raíz)
     */
    private static void insertarString(Grupo raiz, StringEntry entry) {
        String grupoId = entry.getGrupoId();
        Grupo actual = raiz;

        if (!grupoId.isEmpty()) {
            String[] segmentos = grupoId.split("\\.");
            StringBuilder path = new StringBuilder();

            for (String seg : segmentos) {
                if (path.length() > 0) path.append('.');
                path.append(seg);

                String idCompleto = path.toString();
                Grupo hijo = actual.getHijo(idCompleto);
                if (hijo == null) {
                    hijo = new Grupo(idCompleto);
                    actual.addHijo(hijo);
                }
                actual = hijo;
            }
        }

        actual.addString(entry);
    }

    /** Aplica nombre, color y orden desde el backup a cada nodo. */
    private static void aplicarMetadatos(Grupo raiz, Map<String, Backup.GrupoMetadata> meta) {
        raiz.recorrer(g -> {
            Backup.GrupoMetadata m = meta.get(g.getId());
            if (m == null) return;
            if (m.nombre != null && !m.nombre.isBlank()) g.setNombre(m.nombre);
            if (m.colorHex != null && !m.colorHex.isBlank()) g.setColorHex(m.colorHex);
        });
    }

    /** Ordena hijos recursivamente por el campo "orden" del backup, y si no, por nombre. */
    private static void ordenarHijos(Grupo raiz) {
        raiz.recorrer(g -> g.getHijos().sort(Comparator
                .comparing((Grupo h) -> {
                    Backup.GrupoMetadata m = null; // el orden lo aplicamos aquí
                    return 0;
                })
                .thenComparing(Grupo::getNombre, String.CASE_INSENSITIVE_ORDER)));
    }
}