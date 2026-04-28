package localsearch;

import localsearch.moves.CompositeMove;
import localsearch.moves.Move;
import model.Problema;
import model.Solucion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SwapInstalaciones implements BusquedaLocal {

    /**
     * Intenta "apagar" una instalación abierta (jOpen) abriendo una cerrada (jClosed) y
     * reubicando todos los suministros afectados.
     */
    @Override
    public Optional<Move> encontrarMejorMovimiento(Solucion sol, Problema problema) {
        CompositeMove mejor = null;
        double mejorDelta = Double.POSITIVE_INFINITY;

        for (int jOpen = 0; jOpen < problema.getInstalaciones().size(); jOpen++) {
            if (!sol.getInstalacionesAbiertas()[jOpen]) continue;

            int[] clientesAfectados = clientesConSuministro(sol, problema, jOpen);
            if (clientesAfectados.length == 0) continue;

            double cargaOpen = cargaEnInstalacion(sol, clientesAfectados, jOpen);
            if (cargaOpen <= EPS) continue;

            for (int jClosed = 0; jClosed < problema.getInstalaciones().size(); jClosed++) {
                if (sol.getInstalacionesAbiertas()[jClosed]) continue;

                // Poda rápida: si ni sumando capacidades libres cabe lo que hay que mover, no es factible.
                if (!hayCapacidadSuficiente(sol, problema, jOpen, jClosed, cargaOpen)) continue;

                CompositeMove move = evaluarSwap(sol, problema, jOpen, jClosed, clientesAfectados);
                if (move == null) continue;

                double delta = move.delta(problema, sol);
                if (delta < mejorDelta) {
                    mejorDelta = delta;
                    mejor = move;
                }
            }
        }

        return Optional.ofNullable(mejor);
    }

    private CompositeMove evaluarSwap(Solucion sol, Problema problema, int jOpen, int jClosed, int[] clientesAfectados) {
        List<CompositeMove.Operation> ops = new ArrayList<>();

        boolean factible = intentarMoverClientes(sol, problema, jOpen, jClosed, clientesAfectados, ops);
        if (!factible) {
            rollback(sol, ops);
            return null;
        }

        double delta = calcularDelta(problema, jOpen, jClosed, ops);

        rollback(sol, ops);

        String desc = "SwapInstalaciones: cerrar=" + jOpen + " abrir=" + jClosed;
        return new CompositeMove(ops, delta, desc);
    }

    private boolean intentarMoverClientes(Solucion sol, Problema problema, int jOpen, int jClosed, int[] clientesAfectados, List<CompositeMove.Operation> ops) {
        for (int i : clientesAfectados) {
            double cantidadAMover = sol.getSuministros()[i][jOpen];
            if (cantidadAMover <= 0) continue;

            aplicarOperacion(sol, ops, new CompositeMove.Operation(i, jOpen, cantidadAMover, false));

            double demandaRestante = cantidadAMover;

            if (problema.esCompatible(sol, i, jClosed)) {
                double capDisponible = sol.getCapacidadRestante()[jClosed];
                double asignar = Math.min(demandaRestante, capDisponible);
                if (asignar > 0) {
                    aplicarOperacion(sol, ops, new CompositeMove.Operation(i, jClosed, asignar, true));
                    demandaRestante -= asignar;
                }
            }

            if (demandaRestante > EPS) {
                for (int jOtra = 0; jOtra < problema.getInstalaciones().size(); jOtra++) {
                    if (demandaRestante <= EPS) break;
                    if (jOtra == jOpen) continue;
                    if (!sol.getInstalacionesAbiertas()[jOtra]) continue;
                    if (!problema.esCompatible(sol, i, jOtra)) continue;

                    double capDisponible = sol.getCapacidadRestante()[jOtra];
                    double asignar = Math.min(demandaRestante, capDisponible);
                    if (asignar > 0) {
                        aplicarOperacion(sol, ops, new CompositeMove.Operation(i, jOtra, asignar, true));
                        demandaRestante -= asignar;
                    }
                }
            }

            if (demandaRestante > EPS) {
                return false;
            }
        }

        // Debe quedar cerrada la instalación que queremos apagar
        return !sol.getInstalacionesAbiertas()[jOpen];
    }

    private int[] clientesConSuministro(Solucion sol, Problema problema, int instalacionId) {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < problema.getClientes().size(); i++) {
            if (sol.getSuministros()[i][instalacionId] > EPS) {
                ids.add(i);
            }
        }
        int[] arr = new int[ids.size()];
        for (int k = 0; k < ids.size(); k++) arr[k] = ids.get(k);
        return arr;
    }

    private double cargaEnInstalacion(Solucion sol, int[] clientesAfectados, int instalacionId) {
        double carga = 0.0;
        for (int i : clientesAfectados) {
            carga += sol.getSuministros()[i][instalacionId];
        }
        return carga;
    }

    private boolean hayCapacidadSuficiente(Solucion sol, Problema problema, int jOpen, int jClosed, double cargaOpen) {
        double disponible = 0.0;
        // jClosed está cerrada: su capacidad restante suele ser toda su capacidad.
        disponible += sol.getCapacidadRestante()[jClosed];
        for (int j = 0; j < problema.getInstalaciones().size(); j++) {
            if (j == jOpen || j == jClosed) continue;
            if (!sol.getInstalacionesAbiertas()[j]) continue;
            disponible += sol.getCapacidadRestante()[j];
        }
        return disponible + EPS >= cargaOpen;
    }

    private void aplicarOperacion(Solucion sol, List<CompositeMove.Operation> ops, CompositeMove.Operation op) {
        if (op.esAdd()) {
            sol.añadirSuministro(op.clienteId(), op.instalacionId(), op.cantidad());
        } else {
            sol.quitarSuministro(op.clienteId(), op.instalacionId(), op.cantidad());
        }
        ops.add(op);
    }

    private void rollback(Solucion sol, List<CompositeMove.Operation> ops) {
        for (int idx = ops.size() - 1; idx >= 0; idx--) {
            CompositeMove.Operation op = ops.get(idx);
            if (op.esAdd()) {
                sol.quitarSuministro(op.clienteId(), op.instalacionId(), op.cantidad());
            } else {
                sol.añadirSuministro(op.clienteId(), op.instalacionId(), op.cantidad());
            }
        }
    }

    private double calcularDelta(Problema problema, int jOpen, int jClosed, List<CompositeMove.Operation> ops) {
        double[][] c = problema.getCostosTransporte();
        double deltaTransporte = 0.0;
        for (CompositeMove.Operation op : ops) {
            double d = op.cantidad() * c[op.clienteId()][op.instalacionId()];
            deltaTransporte += op.esAdd() ? d : -d;
        }

        double deltaFijo = -problema.getInstalaciones().get(jOpen).getCostoFijo();

        boolean seAbreJClosed = false;
        for (CompositeMove.Operation op : ops) {
            if (op.esAdd() && op.instalacionId() == jClosed && op.cantidad() > EPS) {
                seAbreJClosed = true;
                break;
            }
        }
        if (seAbreJClosed) {
            deltaFijo += problema.getInstalaciones().get(jClosed).getCostoFijo();
        }

        return deltaTransporte + deltaFijo;
    }
}