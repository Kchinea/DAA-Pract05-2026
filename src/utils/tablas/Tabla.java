package utils.tablas;

import java.util.ArrayList;
import java.util.List;

/**
 * Tabla base (POO) para imprimir resultados en consola.
 *
 * <p>Cada tabla concreta se implementa como subclase y aporta sus bloques.
 */
public abstract class Tabla {

    /** Bloque rectangular (cabecera + filas) opcionalmente con subtítulo. */
    protected static final class Bloque {
        private final String subtitulo;
        private final String[] header;
        private final List<String[]> rows;
        private final List<String> footerLines;

        public Bloque(String subtitulo, String[] header, List<String[]> rows) {
            this(subtitulo, header, rows, List.of());
        }

        public Bloque(String subtitulo, String[] header, List<String[]> rows, List<String> footerLines) {
            this.subtitulo = subtitulo;
            this.header = header;
            this.rows = rows;
            this.footerLines = footerLines;
        }
    }

    /** Título principal de la tabla (ej: "TABLA 1: ..."). */
    public abstract String titulo();

    /** Devuelve los bloques que componen esta tabla (1 o varios). */
    protected abstract List<Bloque> bloques();

    /** Renderiza en texto listo para imprimir. */
    public final String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- ").append(titulo()).append(" ---\n");

        List<Bloque> bs = bloques();
        for (int b = 0; b < bs.size(); b++) {
            Bloque bloque = bs.get(b);
            if (bloque.subtitulo != null && !bloque.subtitulo.isBlank()) {
                sb.append(bloque.subtitulo).append("\n");
            }
            sb.append(renderBloque(bloque.header, bloque.rows));
            if (!bloque.footerLines.isEmpty()) {
                for (String line : bloque.footerLines) {
                    sb.append(line).append("\n");
                }
            }
            if (b != bs.size() - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    public final void print() {
        System.out.print(render());
    }

    private static String renderBloque(String[] header, List<String[]> rows) {
        int cols = header.length;
        int[] w = new int[cols];

        for (int c = 0; c < cols; c++) {
            w[c] = header[c].length();
        }
        for (String[] r : rows) {
            for (int c = 0; c < cols; c++) {
                String cell = c < r.length && r[c] != null ? r[c] : "";
                w[c] = Math.max(w[c], cell.length());
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(formatRow(header, w)).append("\n");
        sb.append(separator(w)).append("\n");
        for (String[] r : rows) {
            sb.append(formatRow(r, w)).append("\n");
        }
        return sb.toString();
    }

    private static String separator(int[] w) {
        List<String> parts = new ArrayList<>();
        for (int width : w) {
            parts.add("-".repeat(Math.max(3, width)));
        }
        return String.join("-+-", parts);
    }

    private static String formatRow(String[] row, int[] w) {
        List<String> parts = new ArrayList<>();
        for (int c = 0; c < w.length; c++) {
            String cell = c < row.length && row[c] != null ? row[c] : "";
            parts.add(padRight(cell, w[c]));
        }
        return String.join(" | ", parts);
    }

    private static String padRight(String s, int w) {
        if (s.length() >= w) return s;
        return s + " ".repeat(w - s.length());
    }
}
