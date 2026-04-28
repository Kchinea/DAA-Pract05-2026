
package algorithms;

import model.Cliente;
import model.Fundation;
import model.Problema;
import model.Solucion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Algoritmo voraz de referencia para el MS-CFLP-CI.
 *
 * <p>Implementación fiel al algoritmo propuesto en:
 * Gjergji et al., "Large Neighborhood Search for Capacitated Facility Location
 * with Customer Incompatibilities", GECCO 2025.
 *
 * <p><b>Fase 1 — Selección de instalaciones:</b>
 * Se ordenan por coste fijo ascendente y se añaden hasta cubrir la demanda total.
 * Se añaden k=5 instalaciones extra como holgura por incompatibilidades.
 *
 * <p><b>Fase 2 — Asignación de clientes:</b>
 * Para cada cliente se ordena F_open por coste de transporte ascendente y se
 * asigna a la primera instalación compatible que tenga capacidad.
 */
public class VorazRef {

    private static final int K_HOLGURA = 5;

    private final Problema problema;

    public VorazRef(Problema problema) {
        this.problema = problema;
    }

    public Solucion ejecutar() {
        System.out.println("Ejecutando Voraz de Referencia (Gjergji et al., 2025)...");

        // ── FASE 1: Selección de instalaciones ───────────────────────────────

        // Ordenar todas las instalaciones por coste fijo ascendente
        List<Fundation> instalacionesOrdenadas = new ArrayList<>(problema.getInstalaciones());
        instalacionesOrdenadas.sort(Comparator.comparingDouble(Fundation::getCostoFijo));

        double demandaTotal = problema.getClientes().stream()
                .mapToDouble(Cliente::getDemanda)
                .sum();

        List<Integer> fOpen = new ArrayList<>();
        double capacidadAcumulada = 0.0;

        // Añadir instalaciones hasta cubrir la demanda total
        for (Fundation inst : instalacionesOrdenadas) {
            if (capacidadAcumulada < demandaTotal) {
                fOpen.add(inst.getId());
                capacidadAcumulada += inst.getCapacidad();
            }
        }

        // Añadir K_HOLGURA instalaciones extra para tener alternativas
        // ante las restricciones de incompatibilidad
        int añadidas = 0;
        for (Fundation inst : instalacionesOrdenadas) {
            if (añadidas >= K_HOLGURA) break;
            if (!fOpen.contains(inst.getId())) {
                fOpen.add(inst.getId());
                añadidas++;
            }
        }

        System.out.println("  Instalaciones preseleccionadas: " + fOpen.size()
                + " (demanda total=" + demandaTotal
                + ", capacidad acumulada=" + capacidadAcumulada + ")");

        // ── FASE 2: Asignación de clientes ───────────────────────────────────

        Solucion sol = new Solucion(problema);

        for (Cliente cliente : problema.getClientes()) {
            double demandaRestante = cliente.getDemanda();
            int cId = cliente.getId();

            // Ordenar F_open por coste de transporte c_{ij} ascendente para este cliente
            List<Integer> listaOrdenada = new ArrayList<>(fOpen);
            listaOrdenada.sort(Comparator.comparingDouble(
                    jId -> problema.getCostosTransporte()[cId][jId]));

            // Recorrer la lista y asignar a la primera instalación compatible
            for (int jId : listaOrdenada) {
                if (demandaRestante <= 0) break;

                double capRestante = sol.getCapacidadRestante()[jId];
                if (capRestante <= 0) continue;

                // Verificar compatibilidad: ningún cliente incompatible con i está en j
                if (!problema.esCompatible(sol, cId, jId)) continue;

                double cantidad = Math.min(demandaRestante, capRestante);
                sol.añadirSuministro(cId, jId, cantidad);
                demandaRestante -= cantidad;
            }

            // Aviso si no se pudo satisfacer toda la demanda
            if (demandaRestante > 1e-6) {
                System.out.println("  ¡Aviso! Demanda no satisfecha del cliente "
                        + cId + ": " + demandaRestante
                        + " (considera aumentar K_HOLGURA)");
            }
        }

        System.out.println("VorazRef terminado. Coste total: " + sol.getCosteTotal());
        return sol;
    }
}