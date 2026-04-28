package localsearch;

import localsearch.moves.Move;
import model.Problema;
import model.Solucion;

import java.util.Optional;

public interface BusquedaLocal {
    /** Epsilon para comparar deltas y evitar ruido de coma flotante. */
    double EPS = 0.001;

    /**
     * Encuentra el mejor movimiento de esta vecindad (si existe).
     *
     * <p>Debe devolver un movimiento que sea factible sobre la solución actual.
     * Si no existe movimiento factible, devuelve {@link Optional#empty()}.
     */
    Optional<Move> encontrarMejorMovimiento(Solucion sol, Problema problema);

    /**
     * Aplica como máximo 1 movimiento (el mejor encontrado).
     * Devuelve true si el movimiento aplicado mejora (delta < 0).
     */
    default boolean aplicarMejorMovimiento(Solucion sol, Problema problema) {
        Optional<Move> m = encontrarMejorMovimiento(sol, problema);
        if (m.isEmpty()) {
            return false;
        }

        Move move = m.get();
        if (move.delta(problema, sol) < -EPS) {
            move.apply(sol);
            return true;
        }
        return false;
    }

    // Recibe la solución actual y el problema, y devuelve una solución mejorada
    // aplicando como máximo 1 movimiento.
    default Solucion mejorar(Solucion solucionActual, Problema problema) {
        Solucion copia = new Solucion(solucionActual);
        aplicarMejorMovimiento(copia, problema);
        return copia;
    }
}