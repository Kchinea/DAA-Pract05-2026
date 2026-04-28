package localsearch;

import localsearch.moves.Move;
import localsearch.moves.SwapClientesMove;
import model.Problema;
import model.Solucion;

import java.util.Optional;

public class SwapClientes implements BusquedaLocal {

    /**
     * Swap de "suministro principal": intercambia la mayor asignación de cada cliente
     * (la instalación que más le suministra) con la del otro cliente.
     */
    @Override
    public Optional<Move> encontrarMejorMovimiento(Solucion sol, Problema problema) {
        Move mejor = null;
        double mejorDelta = Double.POSITIVE_INFINITY;

        for (int i1 = 0; i1 < problema.getClientes().size(); i1++) {
            int j1 = instalacionPrincipal(sol, i1);
            if (j1 == -1) continue;
            double q1 = sol.getSuministros()[i1][j1];
            if (q1 <= 0) continue;

            for (int i2 = i1 + 1; i2 < problema.getClientes().size(); i2++) {
                int j2 = instalacionPrincipal(sol, i2);
                if (j2 == -1 || j1 == j2) continue;
                double q2 = sol.getSuministros()[i2][j2];
                if (q2 <= 0) continue;

                if (!esFactibleSwap(sol, problema, i1, i2, j1, j2, q1, q2)) {
                    continue;
                }

                Move m = new SwapClientesMove(i1, i2, j1, j2, q1, q2);
                double delta = m.delta(problema, sol);
                if (delta < mejorDelta) {
                    mejorDelta = delta;
                    mejor = m;
                }
            }
        }

        return Optional.ofNullable(mejor);
    }

    private int instalacionPrincipal(Solucion sol, int clienteId) {
        int mejorJ = -1;
        double maxSuministro = 0;
        for (int j = 0; j < sol.getSuministros()[clienteId].length; j++) {
            double s = sol.getSuministros()[clienteId][j];
            if (s > maxSuministro) {
                maxSuministro = s;
                mejorJ = j;
            }
        }
        return mejorJ;
    }

    private boolean esFactibleSwap(Solucion sol,
                                  Problema problema,
                                  int i1,
                                  int i2,
                                  int j1,
                                  int j2,
                                  double q1,
                                  double q2) {
        sol.quitarSuministro(i1, j1, q1);
        sol.quitarSuministro(i2, j2, q2);

        boolean factible = sol.getCapacidadRestante()[j1] >= q2
                && sol.getCapacidadRestante()[j2] >= q1
                && problema.esCompatible(sol, i1, j2)
                && problema.esCompatible(sol, i2, j1);

        sol.añadirSuministro(i2, j2, q2);
        sol.añadirSuministro(i1, j1, q1);

        return factible;
    }
}