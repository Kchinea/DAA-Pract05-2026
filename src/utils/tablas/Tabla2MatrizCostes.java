package utils.tablas;

import model.Problema;

import java.util.ArrayList;
import java.util.List;

public class Tabla2MatrizCostes extends Tabla {
    private final Problema problema;

    public Tabla2MatrizCostes(Problema problema) {
        this.problema = problema;
    }

    @Override
    public String titulo() {
        return "TABLA 2: MATRIZ DE COSTES UNITARIOS DE TRANSPORTE (cij)";
    }

    @Override
    protected List<Bloque> bloques() {
        int m = problema.getInstalaciones().size();
        String[] header = new String[m + 1];
        header[0] = "Cliente\\Instalación";
        for (int j = 0; j < m; j++) {
            header[j + 1] = "Facility " + j;
        }

        List<String[]> rows = new ArrayList<>();
        double[][] c = problema.getCostosTransporte();
        for (int i = 0; i < problema.getClientes().size(); i++) {
            String[] r = new String[m + 1];
            r[0] = String.valueOf(i);
            for (int j = 0; j < m; j++) {
                r[j + 1] = fmt(c[i][j]);
            }
            rows.add(r);
        }

        return List.of(new Bloque(null, header, rows));
    }

    private static String fmt(double v) {
        return String.format("%.2f", v);
    }
}
