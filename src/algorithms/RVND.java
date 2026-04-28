package algorithms;

import localsearch.BusquedaLocal;
import model.Problema;
import model.Solucion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Randomized Variable Neighborhood Descent (RVND).
 *
 * <p>Alternativa simple a GVNS: selecciona vecindades aleatoriamente y reinicia
 * la lista de vecindades cuando encuentra una mejora.
 */
public class RVND {
    private final Problema problema;
    private final List<BusquedaLocal> entornos;
    private final Random random;

    public RVND(Problema problema, List<BusquedaLocal> entornos) {
        this.problema = problema;
        this.entornos = entornos;
        this.random = new Random();
    }

    public Solucion ejecutar(Solucion solucionInicial) {
        Solucion actual = new Solucion(solucionInicial);
        List<BusquedaLocal> disponibles = new ArrayList<>(entornos);

        while (!disponibles.isEmpty()) {
            int idx = random.nextInt(disponibles.size());
            BusquedaLocal bl = disponibles.get(idx);

            Solucion candidata = bl.mejorar(actual, problema);
            if (candidata.getCosteTotal() < actual.getCosteTotal() - 0.001) {
                actual = candidata;
                disponibles = new ArrayList<>(entornos);
            } else {
                disponibles.remove(idx);
            }
        }

        return actual;
    }
}
