package localsearch;

import localsearch.moves.Move;
import localsearch.moves.ShiftMove;
import model.Problema;
import model.Solucion;

import java.util.Optional;

public class Shift implements BusquedaLocal {

    /**
     * Busca el mejor ShiftMove (mover todo el suministro de i desde j1 a j2).
     *
     * <p>Devuelve {@link Optional#empty()} si no hay movimiento factible.
     */
    @Override
    public Optional<Move> encontrarMejorMovimiento(Solucion sol, Problema problema) {
        Move mejor = null;
        double mejorDelta = Double.POSITIVE_INFINITY;

        for (int i = 0; i < problema.getClientes().size(); i++) {
            for (int j1 = 0; j1 < problema.getInstalaciones().size(); j1++) {
                double cantidad = sol.getSuministros()[i][j1];
                if (cantidad <= 0) continue;

                for (int j2 = 0; j2 < problema.getInstalaciones().size(); j2++) {
                    if (j1 == j2) continue;
                    if (sol.getCapacidadRestante()[j2] < cantidad) continue;
                    if (!problema.esCompatible(sol, i, j2)) continue;

                    Move m = new ShiftMove(i, j1, j2, cantidad, EPS);
                    double delta = m.delta(problema, sol);
                    if (delta < mejorDelta) {
                        mejorDelta = delta;
                        mejor = m;
                    }
                }
            }
        }

        return Optional.ofNullable(mejor);
    }
}