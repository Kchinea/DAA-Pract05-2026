package localsearch.moves;

import model.Problema;
import model.Solucion;

import java.util.List;

/**
 * Movimiento compuesto: secuencia de operaciones de añadir/quitar suministro.
 *
 * <p>Se usa para vecindades donde un "movimiento" puede implicar varias reasignaciones.
 * El delta se almacena precomputado para evitar depender de {@link Solucion#getCosteTotal()}.
 */
public class CompositeMove implements Move {

    /** Operación atómica. Si {@code esAdd} es true -> añadir, si no -> quitar. */
    public record Operation(int clienteId, int instalacionId, double cantidad, boolean esAdd) {
    }

    private final List<Operation> ops;
    private final double precomputedDelta;
    private final String description;

    public CompositeMove(List<Operation> ops, double precomputedDelta, String description) {
        this.ops = ops;
        this.precomputedDelta = precomputedDelta;
        this.description = description;
    }

    @Override
    public double delta(Problema problema, Solucion sol) {
        return precomputedDelta;
    }

    @Override
    public void apply(Solucion sol) {
        for (Operation op : ops) {
            if (op.esAdd) {
                sol.añadirSuministro(op.clienteId, op.instalacionId, op.cantidad);
            } else {
                sol.quitarSuministro(op.clienteId, op.instalacionId, op.cantidad);
            }
        }
    }

    @Override
    public String description() {
        return description;
    }
}
