package localsearch.moves;

import model.Problema;
import model.Solucion;

/**
 * Movimiento: intercambia el suministro principal de dos clientes entre sus instalaciones.
 *
 * <p>Nota: este movimiento asume que las cantidades son las que se van a intercambiar (p. ej. suministro principal).
 */
public class SwapClientesMove implements Move {
    private final int i1;
    private final int i2;
    private final int j1;
    private final int j2;
    private final double q1;
    private final double q2;

    public SwapClientesMove(int i1, int i2, int j1, int j2, double q1, double q2) {
        this.i1 = i1;
        this.i2 = i2;
        this.j1 = j1;
        this.j2 = j2;
        this.q1 = q1;
        this.q2 = q2;
    }

    @Override
    public double delta(Problema problema, Solucion sol) {
        double[][] c = problema.getCostosTransporte();
        double delta1 = q1 * (c[i1][j2] - c[i1][j1]);
        double delta2 = q2 * (c[i2][j1] - c[i2][j2]);
        return delta1 + delta2;
    }

    @Override
    public void apply(Solucion sol) {
        sol.quitarSuministro(i1, j1, q1);
        sol.quitarSuministro(i2, j2, q2);
        sol.añadirSuministro(i1, j2, q1);
        sol.añadirSuministro(i2, j1, q2);
    }

    @Override
    public String description() {
        return "SwapClientesMove(i1=" + i1 + ",i2=" + i2 + ")";
    }
}
