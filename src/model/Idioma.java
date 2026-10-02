package model;
import java.nio.file.Path;

/**
 * Representa un idioma importado (un archivo file_XX.json)
 * Guarda el codigo "en", "es", etc y la ruta al disco
 * **/
public class Idioma {
    private final String codigo;
    private final Path ruta;

    public Idioma(String codigo, Path ruta) {
        this.codigo = codigo;
        this.ruta = ruta;
    }

    public String getCodigo() {
        return codigo;
    }
    public Path getRuta() {
        return ruta;
    }

    @Override
    public String toString() {
        return codigo;
    }

}
