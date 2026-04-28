package localsearch.moves;

import model.Problema;
import model.Solucion;

/**
 * Representa un movimiento de búsqueda local.
 *
 * <p>Un movimiento sabe:
 * <ul>
 *   <li>Cuánto cambia el coste si se aplica ({@link #delta(Problema, Solucion)}).</li>
 *   <li>Cómo aplicarse sobre una {@link Solucion} ({@link #apply(Solucion)}).</li>
 * </ul>
 *
 * <p>Convención: si {@code delta < 0} el movimiento mejora la solución.
 */
public interface Move {

    /**
     * Devuelve el delta de coste (nuevoCoste - costeActual) si se aplica este movimiento.
     */
    double delta(Problema problema, Solucion sol);

    /**
     * Aplica el movimiento sobre la solución.
     */
    void apply(Solucion sol);

    /**
     * Descripción corta para depuración/log.
     */
    default String description() {
        return getClass().getSimpleName();
    }
}
