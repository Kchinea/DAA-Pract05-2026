package localsearch.moves;

import model.Solucion;

/** Utilidades para cálculos de delta y checks sobre la solución sin modificarla. */
public final class MoveUtils {
    private MoveUtils() {
    }

    /**
     * Devuelve true si, al quitar {@code cantidad} del cliente {@code clienteId} en la instalación,
     * la instalación quedaría vacía (sin suministro positivo) y por tanto se cerraría.
     */
    public static boolean wouldBecomeEmptyAfterRemoving(Solucion sol, int instalacionId, int clienteId, double cantidad, double eps) {
        for (int k = 0; k < sol.getSuministros().length; k++) {
            double suministro = sol.getSuministros()[k][instalacionId];
            if (k == clienteId) {
                suministro -= cantidad;
            }
            if (suministro > eps) {
                return false;
            }
        }
        return true;
    }
}
