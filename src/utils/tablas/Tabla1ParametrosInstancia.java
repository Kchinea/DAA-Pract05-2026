package utils.tablas;

import model.Problema;

import java.util.ArrayList;
import java.util.List;

public class Tabla1ParametrosInstancia extends Tabla {
    private final Problema problema;

    public Tabla1ParametrosInstancia(Problema problema) {
        this.problema = problema;
    }

    @Override
    public String titulo() {
        return "TABLA 1: PARÁMETROS DE LA INSTANCIA";
    }

    @Override
    protected List<Bloque> bloques() {
        List<Bloque> bloques = new ArrayList<>();

        // Instalaciones
        List<String[]> filasInst = new ArrayList<>();
        for (int j = 0; j < problema.getInstalaciones().size(); j++) {
            filasInst.add(new String[] {
                    String.valueOf(j),
                    fmt(problema.getInstalaciones().get(j).getCostoFijo()),
                    fmt(problema.getInstalaciones().get(j).getCapacidad())
            });
        }
        bloques.add(new Bloque(
                "Instalaciones (Facilities)",
                new String[] {"ID", "Coste Fijo (fj)", "Capacidad (sj)"},
                filasInst
        ));

        // Clientes
        List<String[]> filasCli = new ArrayList<>();
        boolean[][] inc = problema.getIncompatibilidades();
        for (int i = 0; i < problema.getClientes().size(); i++) {
            String incompat = incompatiblesDe(i, inc);
            filasCli.add(new String[] {
                    String.valueOf(i),
                    fmt(problema.getClientes().get(i).getDemanda()),
                    incompat.isBlank() ? "-" : incompat
            });
        }
        bloques.add(new Bloque(
                "Clientes (Customers)",
                new String[] {"ID", "Demanda (di)", "Incompatibilidad"},
                filasCli
        ));

        return bloques;
    }

    private static String incompatiblesDe(int clienteId, boolean[][] inc) {
        List<Integer> ids = new ArrayList<>();
        for (int k = 0; k < inc.length; k++) {
            if (inc[clienteId][k]) {
                ids.add(k);
            }
        }
        if (ids.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < ids.size(); idx++) {
            if (idx > 0) sb.append(", ");
            sb.append("con Cliente ").append(ids.get(idx));
        }
        return sb.toString();
    }

    private static String fmt(double v) {
        return String.format("%.2f", v);
    }
}
