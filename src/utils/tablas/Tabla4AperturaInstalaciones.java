package utils.tablas;

import model.Problema;
import model.Solucion;
import utils.AnalisisSolucion;

import java.util.ArrayList;
import java.util.List;

public class Tabla4AperturaInstalaciones extends Tabla {
    private final Problema problema;
    private final Solucion solucion;

    public Tabla4AperturaInstalaciones(Problema problema, Solucion solucion) {
        this.problema = problema;
        this.solucion = solucion;
    }

    @Override
    public String titulo() {
        return "TABLA 4: SOLUCIÓN DE APERTURA DE INSTALACIONES";
    }

    @Override
    protected List<Bloque> bloques() {
        List<String[]> rows = new ArrayList<>();
        for (int j = 0; j < problema.getInstalaciones().size(); j++) {
            String estado = solucion.getInstalacionesAbiertas()[j] ? "Abierta" : "Cerrada";
            double fijo = solucion.getInstalacionesAbiertas()[j] ? problema.getInstalaciones().get(j).getCostoFijo() : 0.0;
            rows.add(new String[] {"Instalación " + j, estado, fmt(fijo)});
        }
        rows.add(new String[] {"TOTAL", "", fmt(AnalisisSolucion.costeFijo(solucion))});

        return List.of(new Bloque(null, new String[] {"Instalación", "Estado", "Coste Fijo"}, rows));
    }

    private static String fmt(double v) {
        return String.format("%.2f", v);
    }
}
