package localsearch;

import model.Problema;
import model.Solucion;

public class Shift implements BusquedaLocal {

    private static final double EPS = 0.001;

    @Override
    public boolean aplicarMejorMovimiento(Solucion sol, Problema problema) {
        Movimiento mejor = buscarMejorMovimiento(sol, problema);
        if (mejor == null || mejor.delta >= -EPS) {
            return false;
        }
        sol.quitarSuministro(mejor.clienteId, mejor.instOrigen, mejor.cantidad);
        sol.añadirSuministro(mejor.clienteId, mejor.instDestino, mejor.cantidad);
        return true;
    }

    private static class Movimiento {
        final int clienteId;
        final int instOrigen;
        final int instDestino;
        final double cantidad;
        final double delta;

        Movimiento(int clienteId, int instOrigen, int instDestino, double cantidad, double delta) {
            this.clienteId = clienteId;
            this.instOrigen = instOrigen;
            this.instDestino = instDestino;
            this.cantidad = cantidad;
            this.delta = delta;
        }
    }

    private Movimiento buscarMejorMovimiento(Solucion sol, Problema problema) {
        Movimiento mejor = null;

        for (int i = 0; i < problema.getClientes().size(); i++) {
            for (int j1 = 0; j1 < problema.getInstalaciones().size(); j1++) {
                double cantidad = sol.getSuministros()[i][j1];
                if (cantidad <= 0) continue;

                for (int j2 = 0; j2 < problema.getInstalaciones().size(); j2++) {
                    if (j1 == j2) continue;

                    if (sol.getCapacidadRestante()[j2] >= cantidad && problema.esCompatible(sol, i, j2)) {
                        double delta = calcularDelta(sol, problema, i, j1, j2, cantidad);
                        if (mejor == null || delta < mejor.delta) {
                            mejor = new Movimiento(i, j1, j2, cantidad, delta);
                        }
                    }
                }
            }
        }

        return mejor;
    }

    private double calcularDelta(Solucion sol, Problema problema, int clienteId, int instOrigen, int instDestino, double cantidad) {
        double[][] c = problema.getCostosTransporte();
        double deltaTransporte = cantidad * (c[clienteId][instDestino] - c[clienteId][instOrigen]);

        double deltaFijo = 0.0;

        if (!sol.getInstalacionesAbiertas()[instDestino]) {
            deltaFijo += problema.getInstalaciones().get(instDestino).getCostoFijo();
        }

        if (quedariaVaciaTrasQuitar(sol, instOrigen, clienteId, cantidad)) {
            deltaFijo -= problema.getInstalaciones().get(instOrigen).getCostoFijo();
        }

        return deltaTransporte + deltaFijo;
    }

    private boolean quedariaVaciaTrasQuitar(Solucion sol, int instalacionId, int clienteId, double cantidadQuitada) {
        for (int k = 0; k < sol.getSuministros().length; k++) {
            double suministro = sol.getSuministros()[k][instalacionId];
            if (k == clienteId) {
                suministro -= cantidadQuitada;
            }
            if (suministro > EPS) {
                return false;
            }
        }
        return true;
    }
}