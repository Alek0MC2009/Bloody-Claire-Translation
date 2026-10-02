package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.Idioma;
import model.Proyecto;
import model.StringEntry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lectura y escritura de los lang_XX.json.
 *
 * Formato:
 * {
 *   "lang": "en",
 *   "story.intro.001": "Before time...",
 *   ...
 * }
 *
 * disableHtmlEscaping evita que <c_yellow> se escape a \u003c.
 */
public class JsonService {

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    /** Lee un lang_XX.json y devuelve el mapa completo (incluye "lang"). */
    public Map<String, String> leer(Path ruta) throws IOException {
        try (BufferedReader r = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            Type tipo = new TypeToken<LinkedHashMap<String, String>>() {}.getType();
            Map<String, String> datos = gson.fromJson(r, tipo);
            return datos != null ? datos : new LinkedHashMap<>();
        }
    }

    /** Importa un lang_XX.json dentro del proyecto. */
    public void importar(Proyecto proyecto, Path ruta) throws IOException {
        Map<String, String> datos = leer(ruta);

        String codigo = datos.get("lang");
        if (codigo == null || codigo.isBlank()) {
            throw new IOException("Falta el campo \"lang\" en " + ruta.getFileName());
        }
        if (proyecto.tieneIdioma(codigo)) {
            throw new IOException("El idioma \"" + codigo + "\" ya está importado.");
        }

        proyecto.addIdioma(new Idioma(codigo, ruta));

        for (Map.Entry<String, String> e : datos.entrySet()) {
            if ("lang".equals(e.getKey())) continue;
            proyecto.getOCrearString(e.getKey()).setTraduccion(codigo, e.getValue());
        }
    }

    /** Guarda un idioma en su archivo. */
    /** Guarda un idioma en su archivo. Crea la carpeta y el archivo si no existen. */
    public void guardar(Proyecto proyecto, Idioma idioma) throws IOException {
        Map<String, String> salida = new LinkedHashMap<>();
        salida.put("lang", idioma.getCodigo());

        for (StringEntry entry : proyecto.getStrings().values()) {
            salida.put(entry.getId(), entry.getTraduccion(idioma.getCodigo()));
        }

        Path ruta = idioma.getRuta();
        if (ruta.getParent() != null) Files.createDirectories(ruta.getParent());

        try (BufferedWriter w = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
            gson.toJson(salida, w);
        }
    }

    /** Guarda todos los idiomas cargados. */
    public void guardarTodo(Proyecto proyecto) throws IOException {
        for (Idioma idioma : proyecto.getIdiomas().values()) {
            guardar(proyecto, idioma);
        }
    }

    public Gson getGson() { return gson; }
}