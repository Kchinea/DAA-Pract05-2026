package utils.tablas;

import model.Problema;

import java.util.ArrayList;
import java.util.List;

public class Tabla3ParesIncompatibles extends Tabla {
    private final Problema problema;

    public Tabla3ParesIncompatibles(Problema problema) {
        this.problema = problema;
    }

    @Override
    public String titulo() {
        return "TABLA 3: CONJUNTO DE PARES INCOMPATIBLES (Ipairs)";
    }

    @Override
    protected List<Bloque> bloques() {
        List<String[]> rows = new ArrayList<>();
        boolean[][] inc = problema.getIncompatibilidades();
        for (int i = 0; i < problema.getClientes().size(); i++) {
            for (int k = i + 1; k < problema.getClientes().size(); k++) {
                if (inc[i][k]) {
                    rows.add(new String[] {String.valueOf(i), String.valueOf(k)});
                }
            }
        }
        if (rows.isEmpty()) {
            rows.add(new String[] {"-", "-"});
        }

        return List.of(new Bloque(null, new String[] {"Cliente i1", "Cliente i2"}, rows));
    }
}
