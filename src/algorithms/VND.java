package algorithms;

import localsearch.BusquedaLocal;
import model.Problema;
import model.Solucion;

import java.util.List;

//MODIFICACION


/**
 * Variable Neighborhood Descent (VND) en secuencial.
 *
 * <p>Aplica las vecindades en el orden dado (sin aleatoriedad, secuencialmente) hasta alcanzar un óptimo local
 * respecto al conjunto de vecindades: se repite dentro de una vecindad mientras mejore y,
 * si deja de encontrar mejora, se pasa a la siguiente vecindad.
 */
public class VND {
    private final Problema problema;
    private final List<BusquedaLocal> entornos;

    public VND(Problema problema, List<BusquedaLocal> entornos) {
        this.problema = problema;
        this.entornos = entornos;
    }

    public Solucion ejecutar(Solucion solucionInicial) {
        Solucion actual = new Solucion(solucionInicial);

        int k = 0;
        while (k < entornos.size()) {
            BusquedaLocal bl = entornos.get(k);

            boolean mejoro = false;
            while (bl.aplicarMejorMovimiento(actual, problema)) {
                mejoro = true;
            }

            if (!mejoro) {
                k++;
            }
        }

        return actual;
    }
}
