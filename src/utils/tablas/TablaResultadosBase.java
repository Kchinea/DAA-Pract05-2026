package utils.tablas;

import java.util.ArrayList;
import java.util.List;

/** Tabla base para resultados de algoritmos (acumula filas y puede añadir un promedio). */
public abstract class TablaResultadosBase extends Tabla {

    protected final List<String[]> displayRows = new ArrayList<>();
    protected final List<double[]> metricRows = new ArrayList<>();

    protected abstract String[] header();

    /** Añade una fila: display es lo que se imprime; metrics son números (NaN si no aplica) para promedios. */
    protected final void addRow(String[] display, double[] metrics) {
        displayRows.add(display);
        metricRows.add(metrics);
    }

    @Override
    protected final List<Bloque> bloques() {
        List<String[]> rows = new ArrayList<>(displayRows);
        String[] promedio = buildPromedioRow();
        if (promedio != null) {
            rows.add(promedio);
        }
        return List.of(new Bloque(null, header(), rows));
    }

    /** Construye fila de promedio. Por defecto promedia todas las columnas con métricas no-NaN. */
    protected String[] buildPromedioRow() {
        if (metricRows.isEmpty()) return null;

        int cols = header().length;
        double[] sum = new double[cols];
        int[] count = new int[cols];

        for (double[] mr : metricRows) {
            for (int c = 0; c < Math.min(cols, mr.length); c++) {
                double v = mr[c];
                if (!Double.isNaN(v)) {
                    sum[c] += v;
                    count[c]++;
                }
            }
        }

        String[] row = new String[cols];
        row[0] = "Promedio";
        for (int c = 1; c < cols; c++) {
            if (count[c] == 0) {
                row[c] = "-";
            } else {
                row[c] = String.format("%.2f", sum[c] / count[c]);
            }
        }
        return row;
    }
}
