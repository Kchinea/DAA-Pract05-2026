package utils;

import model.Problema;
import model.Solucion;

/** Métricas y comprobaciones sobre una solución sin modificarla. */
public final class AnalisisSolucion {
    private static final double EPS = 1e-9;

    private AnalisisSolucion() {
    }

    public static int contarInstalacionesAbiertas(Solucion sol) {
        int abiertas = 0;
        for (boolean b : sol.getInstalacionesAbiertas()) {
            if (b) abiertas++;
        }
        return abiertas;
    }

    public static double costeFijo(Solucion sol) {
        double totalFijo = 0.0;
        for (int j = 0; j < sol.getInstalacionesAbiertas().length; j++) {
            if (sol.getInstalacionesAbiertas()[j]) {
                totalFijo += sol.getProblema().getInstalaciones().get(j).getCostoFijo();
            }
        }
        return totalFijo;
    }

    public static double costeVariable(Problema problema, Solucion sol) {
        double[][] c = problema.getCostosTransporte();
        double total = 0.0;
        for (int i = 0; i < problema.getClientes().size(); i++) {
            for (int j = 0; j < problema.getInstalaciones().size(); j++) {
                double q = sol.getSuministros()[i][j];
                if (q > EPS) {
                    total += q * c[i][j];
                }
            }
        }
        return total;
    }

    public static double cargaInstalacion(Solucion sol, int instalacionId) {
        double carga = 0.0;
        for (int i = 0; i < sol.getSuministros().length; i++) {
            double q = sol.getSuministros()[i][instalacionId];
            if (q > EPS) {
                carga += q;
            }
        }
        return carga;
    }

    public static int contarViolacionesIncompatibilidad(Problema problema, Solucion sol) {
        int violaciones = 0;
        boolean[][] inc = problema.getIncompatibilidades();
        int n = problema.getClientes().size();
        int m = problema.getInstalaciones().size();

        for (int j = 0; j < m; j++) {
            for (int i1 = 0; i1 < n; i1++) {
                if (sol.getSuministros()[i1][j] <= EPS) continue;
                for (int i2 = i1 + 1; i2 < n; i2++) {
                    if (!inc[i1][i2]) continue;
                    if (sol.getSuministros()[i2][j] > EPS) {
                        violaciones++;
                    }
                }
            }
        }
        return violaciones;
    }

    public static boolean cumpleCapacidad(Problema problema, Solucion sol, int instalacionId) {
        double carga = cargaInstalacion(sol, instalacionId);
        double cap = problema.getInstalaciones().get(instalacionId).getCapacidad();
        return carga <= cap + 1e-6;
    }

    public static boolean estaEnLimite(Problema problema, Solucion sol, int instalacionId) {
        double carga = cargaInstalacion(sol, instalacionId);
        double cap = problema.getInstalaciones().get(instalacionId).getCapacidad();
        return Math.abs(carga - cap) <= 1e-6;
    }
}
