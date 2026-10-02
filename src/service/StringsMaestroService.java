package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Proyecto;
import model.StringsMaestro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Lee y escribe strings.json (orden canónico de todos los IDs).
 */
public class StringsMaestroService {

    public static final String NOMBRE_ARCHIVO = "strings.json";

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    /** Carga el orden canónico en el proyecto. */
    public void cargar(Proyecto proyecto, Path ruta) throws IOException {
        if (!Files.exists(ruta)) return;

        try (BufferedReader r = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            StringsMaestro maestro = gson.fromJson(r, StringsMaestro.class);
            if (maestro != null && maestro.order != null) {
                proyecto.getOrdenStrings().clear();
                proyecto.getOrdenStrings().addAll(maestro.order);
            }
        }
    }

    /** Guarda el orden canónico desde el proyecto. */
    public void guardar(Proyecto proyecto, Path ruta) throws IOException {
        StringsMaestro maestro = new StringsMaestro();
        maestro.order.addAll(proyecto.getOrdenStrings());

        try (BufferedWriter w = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
            gson.toJson(maestro, w);
        }
    }
}