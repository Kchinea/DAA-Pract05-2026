package utils;

import utils.tablas.Tabla;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Exporta tablas a ficheros de texto. */
public final class TablaExporter {
    private TablaExporter() {
    }

    public static void exportar(Path dirInstancia, String nombreArchivo, Tabla tabla) {
        try {
            Files.createDirectories(dirInstancia);
            Path out = dirInstancia.resolve(nombreArchivo);
            Files.writeString(out, tabla.render(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo escribir la tabla: " + nombreArchivo + " en " + dirInstancia, e);
        }
    }
}
