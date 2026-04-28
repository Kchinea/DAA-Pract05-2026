package localsearch;

import localsearch.moves.Move;
import localsearch.moves.ShiftMove;
import model.Problema;
import model.Solucion;

import java.util.Optional;

public class EliminarIncompatibilidad implements BusquedaLocal {

    /**
     * Intenta reducir incompatibilidades moviendo un cliente "problemático" desde la instalación más cara.
     * Mantiene el criterio original: sólo mover a instalaciones ya abiertas.
     */
    @Override
    public Optional<Move> encontrarMejorMovimiento(Solucion sol, Problema problema) {
        int peorInst = buscarPeorInstalacion(sol, problema);
        if (peorInst == -1) return Optional.empty();

        int cliente = buscarClienteProblematico(sol, problema, peorInst);
        if (cliente == -1) return Optional.empty();

        double cantidad = sol.getSuministros()[cliente][peorInst];
        if (cantidad <= 0) return Optional.empty();

        Move mejor = null;
        double mejorDelta = Double.POSITIVE_INFINITY;

        for (int jAlt = 0; jAlt < problema.getInstalaciones().size(); jAlt++) {
            if (jAlt == peorInst) continue;
            if (!sol.getInstalacionesAbiertas()[jAlt]) continue; // mantenemos el criterio original
            if (sol.getCapacidadRestante()[jAlt] < cantidad) continue;
            if (!problema.esCompatible(sol, cliente, jAlt)) continue;

            Move m = new ShiftMove(cliente, peorInst, jAlt, cantidad, EPS);
            double delta = m.delta(problema, sol);
            if (delta < mejorDelta) {
                mejorDelta = delta;
                mejor = m;
            }
        }

        return Optional.ofNullable(mejor);
    }

    private int buscarPeorInstalacion(Solucion sol, Problema problema) {
        int peorInst = -1;
        double maxCoste = -1;

        for (int j = 0; j < problema.getInstalaciones().size(); j++) {
            if (!sol.getInstalacionesAbiertas()[j]) continue;

            double coste = problema.getInstalaciones().get(j).getCostoFijo();
            for (int i = 0; i < problema.getClientes().size(); i++) {
                coste += sol.getSuministros()[i][j] * problema.getCostosTransporte()[i][j];
            }

            if (coste > maxCoste) {
                maxCoste = coste;
                peorInst = j;
            }
        }

        return peorInst;
    }

    private int buscarClienteProblematico(Solucion sol, Problema problema, int instId) {
        int cliente = -1;
        int maxIncompat = -1;

        for (int i = 0; i < problema.getClientes().size(); i++) {
            if (sol.getSuministros()[i][instId] <= 0) continue;

            int count = 0;
            for (int k = 0; k < problema.getClientes().size(); k++) {
                if (problema.sonIncompatibles(i, k)) count++;
            }

            if (count > maxIncompat) {
                maxIncompat = count;
                cliente = i;
            }
        }

        return cliente;
    }
}