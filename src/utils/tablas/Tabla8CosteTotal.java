package utils.tablas;

import model.Problema;
import model.Solucion;
import utils.AnalisisSolucion;

import java.util.ArrayList;
import java.util.List;

public class Tabla8CosteTotal extends Tabla {
    private final Problema problema;
    private final Solucion solucion;

    public Tabla8CosteTotal(Problema problema, Solucion solucion) {
        this.problema = problema;
        this.solucion = solucion;
    }

    @Override
    public String titulo() {
        return "TABLA 8: CÁLCULO DEL COSTE TOTAL DE LA SOLUCIÓN";
    }

    @Override
    protected List<Bloque> bloques() {
        double fijo = AnalisisSolucion.costeFijo(solucion);
        double variable = AnalisisSolucion.costeVariable(problema, solucion);
        double total = fijo + variable;

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[] {"Costos Fijos (Apertura fj)", fmt(fijo)});
        rows.add(new String[] {"Costos Variables (Transporte)", fmt(variable)});
        rows.add(new String[] {"Costo Total (Función Objetivo)", fmt(total)});

        List<String> footer = new ArrayList<>();
        footer.add("CosteTotal(Solucion) = " + fmt(solucion.getCosteTotal()));

        return List.of(new Bloque(null, new String[] {"Concepto", "Coste"}, rows, footer));
    }

    private static String fmt(double v) {
        return String.format("%.2f", v);
    }
}
