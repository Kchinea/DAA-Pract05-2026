package localsearch.moves;

import model.Problema;
import model.Solucion;

/**
 * Movimiento: mueve una cantidad fija de suministro de un cliente desde una instalación origen a una destino.
 */
public class ShiftMove implements Move {
    private final int clienteId;
    private final int instOrigen;
    private final int instDestino;
    private final double cantidad;
    private final double eps;

    public ShiftMove(int clienteId, int instOrigen, int instDestino, double cantidad, double eps) {
        this.clienteId = clienteId;
        this.instOrigen = instOrigen;
        this.instDestino = instDestino;
        this.cantidad = cantidad;
        this.eps = eps;
    }

    @Override
    public double delta(Problema problema, Solucion sol) {
        double[][] c = problema.getCostosTransporte();
        double deltaTransporte = cantidad * (c[clienteId][instDestino] - c[clienteId][instOrigen]);

        double deltaFijo = 0.0;
        if (!sol.getInstalacionesAbiertas()[instDestino]) {
            deltaFijo += problema.getInstalaciones().get(instDestino).getCostoFijo();
        }

        if (MoveUtils.wouldBecomeEmptyAfterRemoving(sol, instOrigen, clienteId, cantidad, eps)) {
            deltaFijo -= problema.getInstalaciones().get(instOrigen).getCostoFijo();
        }

        return deltaTransporte + deltaFijo;
    }

    @Override
    public void apply(Solucion sol) {
        sol.quitarSuministro(clienteId, instOrigen, cantidad);
        sol.añadirSuministro(clienteId, instDestino, cantidad);
    }

    @Override
    public String description() {
        return "ShiftMove(c=" + clienteId + ", " + instOrigen + "->" + instDestino + ", q=" + cantidad + ")";
    }
}
