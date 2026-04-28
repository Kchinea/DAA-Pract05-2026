package utils.tablas;

import model.Problema;
import model.Solucion;

import java.util.ArrayList;
import java.util.List;

public class Tabla5AsignacionClientes extends Tabla {
    private static final double EPS = 1e-9;

    private final Problema problema;
    private final Solucion solucion;

    public Tabla5AsignacionClientes(Problema problema, Solucion solucion) {
        this.problema = problema;
        this.solucion = solucion;
    }

    @Override
    public String titulo() {
        return "TABLA 5: DETALLE DE ASIGNACIÓN DE CLIENTES";
    }

    @Override
    protected List<Bloque> bloques() {
        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < problema.getClientes().size(); i++) {
            double demanda = problema.getClientes().get(i).getDemanda();
            for (int j = 0; j < problema.getInstalaciones().size(); j++) {
                double q = solucion.getSuministros()[i][j];
                if (q <= EPS) continue;
                double pct = demanda > EPS ? (q / demanda) * 100.0 : 0.0;
                rows.add(new String[] {
                        "Cliente " + i,
                        "Instalación " + j,
                        fmt(demanda),
                        fmt(q),
                        String.format("%.1f %%", pct)
                });
            }
        }
        if (rows.isEmpty()) {
            rows.add(new String[] {"-", "-", "-", "-", "-"});
        }

        return List.of(new Bloque(null, new String[] {"Cliente", "Instalación", "Demanda", "Asignada", "Porcentaje (%)"}, rows));
    }

    private static String fmt(double v) {
        return String.format("%.2f", v);
    }
}
