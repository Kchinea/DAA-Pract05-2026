package utils.tablas;

import model.Problema;
import model.Solucion;
import utils.AnalisisSolucion;

public class TablaResultadosVoraz extends TablaResultadosBase {

    @Override
    public String titulo() {
        return "TABLA 9: RESULTADOS VORAZ (MS-CFLP-CI)";
    }

    @Override
    protected String[] header() {
        return new String[] {"Instancia", "|Jopen|", "Coste Fijo", "Coste Asig.", "Coste Total", "Incomp.", "CPU_Time (s)"};
    }

    public void addResultado(String instancia, Solucion sol, long tiempoMs) {
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
                        String.valueOf(jopen),
                        fmt(fijo),
                        fmt(variable),
                        fmt(total),
                        String.valueOf(incomp),
                        fmt(t)
                },
                new double[] {
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

    private static String fmt(double v) {
        return String.format("%.2f", v);
    }
}
