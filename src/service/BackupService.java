package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Backup;
import model.Proyecto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Lee y escribe backup.json (metadatos de grupos).
 */
public class BackupService {

    public static final String NOMBRE_ARCHIVO = "backup.json";

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public void cargar(Proyecto proyecto, Path ruta) throws IOException {
        if (!Files.exists(ruta)) return;

        try (BufferedReader r = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            Backup backup = gson.fromJson(r, Backup.class);
            if (backup == null) return;

            proyecto.getMetadatosGrupos().clear();
            if (backup.grupos != null) {
                proyecto.getMetadatosGrupos().putAll(backup.grupos);
            }
            proyecto.getGruposColapsados().clear();
            if (backup.colapsados != null) {
                proyecto.getGruposColapsados().addAll(backup.colapsados);
            }
        }
    }

    public void guardar(Proyecto proyecto, Path ruta) throws IOException {
        Backup backup = new Backup();
        backup.grupos.putAll(proyecto.getMetadatosGrupos());
        backup.colapsados.addAll(proyecto.getGruposColapsados());

        try (BufferedWriter w = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
            gson.toJson(backup, w);
        }
    }
}