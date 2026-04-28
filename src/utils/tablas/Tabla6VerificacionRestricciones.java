package utils.tablas;

import model.Problema;
import model.Solucion;
import utils.AnalisisSolucion;

import java.util.ArrayList;
import java.util.List;

public class Tabla6VerificacionRestricciones extends Tabla {
    private final Problema problema;
    private final Solucion solucion;

    public Tabla6VerificacionRestricciones(Problema problema, Solucion solucion) {
        this.problema = problema;
        this.solucion = solucion;
    }

    @Override
    public String titulo() {
        return "TABLA 6: VERIFICACIÓN DE RESTRICCIONES";
    }

    @Override
    protected List<Bloque> bloques() {
        List<Bloque> bloques = new ArrayList<>();

        List<String[]> rowsCap = new ArrayList<>();
        for (int j = 0; j < problema.getInstalaciones().size(); j++) {
            double carga = AnalisisSolucion.cargaInstalacion(solucion, j);
            double cap = problema.getInstalaciones().get(j).getCapacidad();
            String estado;
            if (!AnalisisSolucion.cumpleCapacidad(problema, solucion, j)) {
                estado = "✗VIOLA";
            } else if (AnalisisSolucion.estaEnLimite(problema, solucion, j)) {
                estado = "✓Límite";
            } else {
                estado = "✓OK";
            }
            rowsCap.add(new String[] {"Instalación " + j, fmt(carga), fmt(cap), estado});
        }
        bloques.add(new Bloque(
                "Capacidad",
                new String[] {"Instalación", "Carga Total", "Capacidad", "Estado"},
                rowsCap
        ));

        // Incompatibilidades
        List<String[]> rowsInc = new ArrayList<>();
        boolean[][] inc = problema.getIncompatibilidades();
        int n = problema.getClientes().size();
        int m = problema.getInstalaciones().size();

        for (int i1 = 0; i1 < n; i1++) {
            for (int i2 = i1 + 1; i2 < n; i2++) {
                if (!inc[i1][i2]) continue;
                boolean ok = true;
                for (int j = 0; j < m; j++) {
                    if (solucion.getSuministros()[i1][j] > 1e-9 && solucion.getSuministros()[i2][j] > 1e-9) {
                        ok = false;
                        break;
                    }
                }
                rowsInc.add(new String[] {
                        "Cliente " + i1 + " y " + i2,
                        ok ? "✓OK" : "✗VIOLA"
                });
            }
        }
        if (rowsInc.isEmpty()) {
            rowsInc.add(new String[] {"-", "✓OK"});
        }

        bloques.add(new Bloque(
                "Incompatibilidades",
                new String[] {"Par incompatible", "Estado"},
                rowsInc
        ));

        return bloques;
    }

    private static String fmt(double v) {
        return String.format("%.2f", v);
    }
}
