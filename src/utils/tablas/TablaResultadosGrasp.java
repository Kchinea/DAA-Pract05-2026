package utils.tablas;

import model.Problema;
import model.Solucion;
import utils.AnalisisSolucion;

public class TablaResultadosGrasp extends TablaResultadosBase {

    @Override
    public String titulo() {
        return "TABLA 10: RESULTADOS GRASP (AJUSTE Y RESULTADOS)";
    }

    @Override
    protected String[] header() {
        return new String[] {"Instancia", "|LRC|", "Ejec.", "|Jopen|", "C. Fijo", "C. Asig.", "C. Total", "Incomp.", "CPU_Time (s)"};
    }

    public void addResultado(String instancia, int lrc, int ejec, Solucion sol, long tiempoMs) {
        Problema problema = sol.getProblema();
        int jopen = AnalisisSolucion.contarInstalacionesAbiertas(sol);
        double fijo = AnalisisSolucion.costeFijo(sol);
        double variable = AnalisisSolucion.costeVariable(problema, sol);
        double total = fijo + variable;
        int incomp = AnalisisSolucion.contarViolacionesIncompatibilidad(problema, sol);
        double t = tiempoMs / 1000.0;

        addRow(
                new String[] {
                        instancia,
                        String.valueOf(lrc),
                        String.valueOf(ejec),
                        String.valueOf(jopen),
                        fmt(fijo),
                        fmt(variable),
                        fmt(total),
                        String.valueOf(incomp),
                        fmt(t)
                },
                new double[] {
                        Double.NaN,
                        Double.NaN,
                        Double.NaN,
                        jopen,
                        fijo,
                        variable,
                        total,
                        incomp,
                        t
                }
        );
    }

    @Override
    protected String[] buildPromedioRow() {
        String[] base = super.buildPromedioRow();
        if (base == null) return null;
        // Cols 1 y 2 no aplican a promedio global
        base[1] = "-";
        base[2] = "-";
        return base;
    }

    private static String fmt(double v) {
        return String.format("%.2f", v);
    }
}
