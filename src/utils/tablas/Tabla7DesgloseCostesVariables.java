package utils.tablas;

import model.Problema;
import model.Solucion;

import java.util.ArrayList;
import java.util.List;

public class Tabla7DesgloseCostesVariables extends Tabla {
    private static final double EPS = 1e-9;

    private final Problema problema;
    private final Solucion solucion;

    public Tabla7DesgloseCostesVariables(Problema problema, Solucion solucion) {
        this.problema = problema;
        this.solucion = solucion;
    }

    @Override
    public String titulo() {
        return "TABLA 7: DESGLOSE DETALLADO DE COSTES VARIABLES DE TRANSPORTE";
    }

    @Override
    protected List<Bloque> bloques() {
        List<String[]> rows = new ArrayList<>();
        double total = 0.0;
        double[][] c = problema.getCostosTransporte();

        for (int i = 0; i < problema.getClientes().size(); i++) {
            for (int j = 0; j < problema.getInstalaciones().size(); j++) {
                double q = solucion.getSuministros()[i][j];
                if (q <= EPS) continue;
                double unit = c[i][j];
                double sub = q * unit;
                total += sub;
                rows.add(new String[] {
                        "Cliente " + i,
                        "Instalación " + j,
                        fmt(q),
                        fmt(unit),
                        fmt(sub)
                });
            }
        }
        rows.add(new String[] {"TOTAL", "", "", "", fmt(total)});

        return List.of(new Bloque(null, new String[] {"Cliente", "Instalación", "Cantidad (q)", "Coste Unitario (c)", "Subtotal"}, rows));
    }

    private static String fmt(double v) {
        return String.format("%.2f", v);
    }
}
