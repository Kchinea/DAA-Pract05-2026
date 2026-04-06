package localsearch;

import model.Problema;
import model.Solucion;

import java.util.ArrayList;
import java.util.List;

public class SwapInstalaciones implements BusquedaLocal {

    private static final double EPS = 0.001;

    private static class Operacion {
        final int clienteId;
        final int instalacionId;
        final double cantidad;
        final boolean esAdd;

        Operacion(int clienteId, int instalacionId, double cantidad, boolean esAdd) {
            this.clienteId = clienteId;
            this.instalacionId = instalacionId;
            this.cantidad = cantidad;
            this.esAdd = esAdd;
        }
    }

    private static class Plan {
        final int jOpen;
        final int jClosed;
        final double delta;
        final List<Operacion> ops;

        Plan(int jOpen, int jClosed, double delta, List<Operacion> ops) {
            this.jOpen = jOpen;
            this.jClosed = jClosed;
            this.delta = delta;
            this.ops = ops;
        }
    }

    @Override
    public boolean aplicarMejorMovimiento(Solucion sol, Problema problema) {
        Plan mejorPlan = buscarMejorPlan(sol, problema);
        if (mejorPlan == null || mejorPlan.delta >= -EPS) {
            return false;
        }
        aplicarPlan(sol, mejorPlan.ops);
        return true;
    }

    private Plan buscarMejorPlan(Solucion sol, Problema problema) {
        Plan mejor = null;

        for (int jOpen = 0; jOpen < problema.getInstalaciones().size(); jOpen++) {
            if (!sol.getInstalacionesAbiertas()[jOpen]) continue;

            for (int jClosed = 0; jClosed < problema.getInstalaciones().size(); jClosed++) {
                if (sol.getInstalacionesAbiertas()[jClosed]) continue;

                Plan plan = evaluarPlan(sol, problema, jOpen, jClosed);
                if (plan == null) continue;

                if (mejor == null || plan.delta < mejor.delta) {
                    mejor = plan;
                }
            }
        }

        return mejor;
    }

    private Plan evaluarPlan(Solucion sol, Problema problema, int jOpen, int jClosed) {
        List<Operacion> ops = new ArrayList<>();

        boolean factible = intentarMoverClientes(sol, problema, jOpen, jClosed, ops);
        if (!factible) {
            rollback(sol, ops);
            return null;
        }

        double delta = calcularDelta(problema, jOpen, jClosed, ops);

        // Importante: dejar la solución como estaba
        rollback(sol, ops);

        return new Plan(jOpen, jClosed, delta, ops);
    }

    private boolean intentarMoverClientes(Solucion sol, Problema problema, int jOpen, int jClosed, List<Operacion> ops) {
        for (int i = 0; i < problema.getClientes().size(); i++) {
            double cantidadAMover = sol.getSuministros()[i][jOpen];
            if (cantidadAMover <= 0) continue;

            aplicarOperacion(sol, ops, new Operacion(i, jOpen, cantidadAMover, false));

            double demandaRestante = cantidadAMover;

            if (problema.esCompatible(sol, i, jClosed)) {
                double capDisponible = sol.getCapacidadRestante()[jClosed];
                double asignar = Math.min(demandaRestante, capDisponible);
                if (asignar > 0) {
                    aplicarOperacion(sol, ops, new Operacion(i, jClosed, asignar, true));
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
                        aplicarOperacion(sol, ops, new Operacion(i, jOtra, asignar, true));
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

    private void aplicarOperacion(Solucion sol, List<Operacion> ops, Operacion op) {
        if (op.esAdd) {
            sol.añadirSuministro(op.clienteId, op.instalacionId, op.cantidad);
        } else {
            sol.quitarSuministro(op.clienteId, op.instalacionId, op.cantidad);
        }
        ops.add(op);
    }

    private void rollback(Solucion sol, List<Operacion> ops) {
        for (int idx = ops.size() - 1; idx >= 0; idx--) {
            Operacion op = ops.get(idx);
            if (op.esAdd) {
                sol.quitarSuministro(op.clienteId, op.instalacionId, op.cantidad);
            } else {
                sol.añadirSuministro(op.clienteId, op.instalacionId, op.cantidad);
            }
        }
    }

    private void aplicarPlan(Solucion sol, List<Operacion> ops) {
        for (Operacion op : ops) {
            if (op.esAdd) {
                sol.añadirSuministro(op.clienteId, op.instalacionId, op.cantidad);
            } else {
                sol.quitarSuministro(op.clienteId, op.instalacionId, op.cantidad);
            }
        }
    }

    private double calcularDelta(Problema problema, int jOpen, int jClosed, List<Operacion> ops) {
        double[][] c = problema.getCostosTransporte();
        double deltaTransporte = 0.0;
        for (Operacion op : ops) {
            double d = op.cantidad * c[op.clienteId][op.instalacionId];
            deltaTransporte += op.esAdd ? d : -d;
        }

        double deltaFijo = -problema.getInstalaciones().get(jOpen).getCostoFijo();

        boolean seAbreJClosed = false;
        for (Operacion op : ops) {
            if (op.esAdd && op.instalacionId == jClosed && op.cantidad > EPS) {
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