package localsearch;

import model.Problema;
import model.Solucion;

public class EliminarIncompatibilidad implements BusquedaLocal {

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
        int peorInst = buscarPeorInstalacion(sol, problema);
        if (peorInst == -1) return null;

        int cliente = buscarClienteProblematico(sol, problema, peorInst);
        if (cliente == -1) return null;

        double cantidad = sol.getSuministros()[cliente][peorInst];
        if (cantidad <= 0) return null;

        Movimiento mejor = null;

        for (int jAlt = 0; jAlt < problema.getInstalaciones().size(); jAlt++) {
            if (jAlt == peorInst) continue;
            if (!sol.getInstalacionesAbiertas()[jAlt]) continue; // mantenemos el criterio original
            if (sol.getCapacidadRestante()[jAlt] < cantidad) continue;
            if (!problema.esCompatible(sol, cliente, jAlt)) continue;

            double delta = calcularDelta(sol, problema, cliente, peorInst, jAlt, cantidad);
            if (mejor == null || delta < mejor.delta) {
                mejor = new Movimiento(cliente, peorInst, jAlt, cantidad, delta);
            }
        }

        return mejor;
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

    private double calcularDelta(Solucion sol, Problema problema, int clienteId, int instOrigen, int instDestino, double cantidad) {
        double[][] c = problema.getCostosTransporte();
        double deltaTransporte = cantidad * (c[clienteId][instDestino] - c[clienteId][instOrigen]);

        double deltaFijo = 0.0;
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