package utils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Utilidades para localizar carpetas del proyecto aunque el working directory cambie. */
public final class ProjectPaths {
    private ProjectPaths() {
    }

    /** Busca una carpeta que contenga `instances/Public` subiendo y mirando hijos directos. */
    public static Path findInstancesPublicDir() {
        Path cwd = Paths.get("").toAbsolutePath();
        for (int up = 0; up < 7; up++) {
            Path base = ascend(cwd, up);
            if (base == null) break;

            Path direct = base.resolve(Paths.get("instances", "Public"));
            if (Files.isDirectory(direct)) return direct;

            try (DirectoryStream<Path> ds = Files.newDirectoryStream(base)) {
                for (Path child : ds) {
                    if (!Files.isDirectory(child)) continue;
                    Path cand = child.resolve(Paths.get("instances", "Public"));
                    if (Files.isDirectory(cand)) return cand;
                }
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    /** Resuelve un fichero de instancia por nombre (wlp01.dzn) o ruta relativa. */
    public static Path resolveInstanceFile(String rutaOFileName) {
        if (rutaOFileName == null || rutaOFileName.isBlank()) return null;

        Path p = Paths.get(rutaOFileName);
        if (Files.exists(p)) return p.toAbsolutePath().normalize();

        String fileName = p.getFileName().toString();

        // Intento directo en instances/Public
        Path instancesPublic = findInstancesPublicDir();
        if (instancesPublic != null) {
            Path cand = instancesPublic.resolve(fileName);
            if (Files.exists(cand)) return cand.toAbsolutePath().normalize();
        }

        // Búsqueda por subida: base/instances/Public/<fileName>
        Path cwd = Paths.get("").toAbsolutePath();
        for (int up = 0; up < 7; up++) {
            Path base = ascend(cwd, up);
            if (base == null) break;
            Path cand = base.resolve(Paths.get("instances", "Public", fileName));
            if (Files.exists(cand)) return cand.toAbsolutePath().normalize();
        }

        return null;
    }

    /** Carpeta de salida para tablas (relativa al proyecto donde esté instances/Public). */
    public static Path outputTablasDir() {
        Path instancesPublic = findInstancesPublicDir();
        if (instancesPublic == null) {
            return Paths.get("tablas").toAbsolutePath();
        }
        // instances/Public -> project root = instancesPublic/.. /..
        Path root = instancesPublic.getParent() != null ? instancesPublic.getParent().getParent() : null;
        if (root == null) {
            return Paths.get("tablas").toAbsolutePath();
        }
        return root.resolve("tablas");
    }

    private static Path ascend(Path p, int up) {
        Path cur = p;
        for (int i = 0; i < up; i++) {
            if (cur == null) return null;
            cur = cur.getParent();
        }
        return cur;
    }
}
