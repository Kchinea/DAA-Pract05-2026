package localsearch;

import model.Problema;
import model.Solucion;

public class SwapClientes implements BusquedaLocal {

    private static final double EPS = 0.001;

    @Override
    public boolean aplicarMejorMovimiento(Solucion sol, Problema problema) {
        Movimiento mejor = buscarMejorMovimiento(sol, problema);
        if (mejor == null || mejor.delta >= -EPS) {
            return false;
        }
        aplicarSwap(sol, mejor);
        return true;
    }

    private static class Movimiento {
        final int i1;
        final int i2;
        final int j1;
        final int j2;
        final double cant1;
        final double cant2;
        final double delta;

        Movimiento(int i1, int i2, int j1, int j2, double cant1, double cant2, double delta) {
            this.i1 = i1;
            this.i2 = i2;
            this.j1 = j1;
            this.j2 = j2;
            this.cant1 = cant1;
            this.cant2 = cant2;
            this.delta = delta;
        }
    }

    private Movimiento buscarMejorMovimiento(Solucion sol, Problema problema) {
        Movimiento mejor = null;

        for (int i1 = 0; i1 < problema.getClientes().size(); i1++) {
            for (int i2 = i1 + 1; i2 < problema.getClientes().size(); i2++) {
                int j1 = obtenerInstalacionPrincipal(sol, i1);
                int j2 = obtenerInstalacionPrincipal(sol, i2);
                if (j1 == -1 || j2 == -1 || j1 == j2) continue;

                double cant1 = sol.getSuministros()[i1][j1];
                double cant2 = sol.getSuministros()[i2][j2];
                if (cant1 <= 0 || cant2 <= 0) continue;

                if (!esFactible(sol, problema, i1, i2, j1, j2, cant1, cant2)) continue;

                double delta = calcularDelta(problema, i1, i2, j1, j2, cant1, cant2);
                if (mejor == null || delta < mejor.delta) {
                    mejor = new Movimiento(i1, i2, j1, j2, cant1, cant2, delta);
                }
            }
        }

        return mejor;
    }

    private boolean esFactible(Solucion sol, Problema problema, int i1, int i2, int j1, int j2, double cant1, double cant2) {
        // Capacidad tras quitar el suministro original de cada cliente
        if (sol.getCapacidadRestante()[j2] + cant2 < cant1) return false;
        if (sol.getCapacidadRestante()[j1] + cant1 < cant2) return false;

        // Compatibilidad en destino, ignorando el cliente que se va a quitar de esa instalación
        if (!esCompatibleIgnorando(sol, problema, i1, j2, i2, j2)) return false;
        if (!esCompatibleIgnorando(sol, problema, i2, j1, i1, j1)) return false;

        return true;
    }

    private boolean esCompatibleIgnorando(Solucion sol, Problema problema, int clienteId, int instalacionId, int clienteIgnorar, int instIgnorar) {
        for (int k = 0; k < problema.getClientes().size(); k++) {
            double suministro = sol.getSuministros()[k][instalacionId];
            if (k == clienteIgnorar && instalacionId == instIgnorar) {
                suministro = 0;
            }
            if (suministro > EPS && problema.sonIncompatibles(clienteId, k)) {
                return false;
            }
        }
        return true;
    }

    private double calcularDelta(Problema problema, int i1, int i2, int j1, int j2, double cant1, double cant2) {
        double[][] c = problema.getCostosTransporte();

        double delta1 = cant1 * (c[i1][j2] - c[i1][j1]);
        double delta2 = cant2 * (c[i2][j1] - c[i2][j2]);

        return delta1 + delta2;
    }

    private void aplicarSwap(Solucion sol, Movimiento mov) {
        sol.quitarSuministro(mov.i1, mov.j1, mov.cant1);
        sol.quitarSuministro(mov.i2, mov.j2, mov.cant2);
        sol.añadirSuministro(mov.i1, mov.j2, mov.cant1);
        sol.añadirSuministro(mov.i2, mov.j1, mov.cant2);
    }

    private int obtenerInstalacionPrincipal(Solucion sol, int clienteId) {
        int mejorJ = -1;
        double maxSuministro = 0;
        // Para simplificar el Swap, cogemos la instalación que le da más suministro a este cliente
        for (int j = 0; j < sol.getSuministros()[clienteId].length; j++) {
            if (sol.getSuministros()[clienteId][j] > maxSuministro) {
                maxSuministro = sol.getSuministros()[clienteId][j];
                mejorJ = j;
            }
        }
        return mejorJ;
    }
}