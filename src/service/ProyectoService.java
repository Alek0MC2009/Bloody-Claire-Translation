package service;

import model.Idioma;
import model.Proyecto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Creación y gestión del ciclo de vida del proyecto.
 *
 * Responsabilidades:
 *  - Crear un proyecto nuevo desde cero (carpeta + archivos base).
 *  - Añadir idiomas nuevos al proyecto actual.
 *  - Asegurar que los archivos lang_XX.json existen en disco.
 */
public class ProyectoService {

    private final JsonService jsonService = new JsonService();
    private final StringsMaestroService maestroService = new StringsMaestroService();
    private final BackupService backupService = new BackupService();

    /**
     * Crea un proyecto nuevo en la carpeta indicada.
     * Crea strings.json, backup.json y un lang_XX.json por cada idioma.
     *
     * @param carpeta          carpeta destino (se crea si no existe).
     * @param codigosIdiomas   códigos de idioma iniciales, ej: ["en", "es"]
     * @return el Proyecto recién creado, listo para usar.
     */
    public Proyecto crearNuevo(Path carpeta, List<String> codigosIdiomas) throws IOException {
        Files.createDirectories(carpeta);

        Proyecto proyecto = new Proyecto();
        proyecto.setCarpeta(carpeta);

        // strings.json y backup.json vacíos
        maestroService.guardar(proyecto, carpeta.resolve(StringsMaestroService.NOMBRE_ARCHIVO));
        backupService.guardar(proyecto, carpeta.resolve(BackupService.NOMBRE_ARCHIVO));

        // Un lang_XX.json por idioma (vacío, solo con {"lang":"xx"})
        for (String codigo : codigosIdiomas) {
            String c = codigo.trim().toLowerCase();
            if (c.isEmpty()) continue;
            agregarIdioma(proyecto, c);
        }

        return proyecto;
    }

    /**
     * Añade un idioma al proyecto actual.
     * Si el archivo lang_XX.json no existe, lo crea con {"lang":"xx"}.
     */
    public void agregarIdioma(Proyecto proyecto, String codigo) throws IOException {
        String c = codigo.trim().toLowerCase();
        if (c.isEmpty()) throw new IOException("El código de idioma no puede estar vacío.");
        if (proyecto.tieneIdioma(c)) throw new IOException("El idioma \"" + c + "\" ya existe.");

        Path ruta = proyecto.rutaLang(c);

        // Crear el archivo si no existe, para que el usuario lo vea en el explorador
        if (!Files.exists(ruta)) {
            Files.createDirectories(ruta.getParent());
            Files.writeString(ruta, "{\n  \"lang\": \"" + c + "\"\n}\n", StandardCharsets.UTF_8);
        }

        proyecto.addIdioma(new Idioma(c, ruta));

        // Guardarlo ya con todas las claves vacías (opcional, pero consistente)
        jsonService.guardar(proyecto, proyecto.getIdiomas().get(c));
    }

    /** Elimina un idioma del proyecto (no borra el archivo en disco). */
    public void quitarIdioma(Proyecto proyecto, String codigo) {
        proyecto.getIdiomas().remove(codigo);
        // Limpiar traducciones huérfanas
        proyecto.getStrings().values().forEach(e -> e.getTraducciones().remove(codigo));
    }
}