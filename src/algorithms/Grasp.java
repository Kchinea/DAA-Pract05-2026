package algorithms;

import localsearch.BusquedaLocal;
import model.Cliente;
import model.Fundation;
import model.Problema;
import model.Solucion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Grasp {
    private Problema problema;
    private List<BusquedaLocal> busquedasLocales;
    private int tamañoLRC;
    private Random random;

    public Grasp(Problema problema, List<BusquedaLocal> busquedasLocales, int tamañoLRC) {
        this.problema = problema;
        this.busquedasLocales = busquedasLocales;
        this.tamañoLRC = tamañoLRC;
        // Usamos una semilla fija (ej. 42) temporalmente para que las pruebas te den siempre igual.
        // Cuando vayas a entregar, puedes quitar el '42' para que sea totalmente aleatorio.
        this.random = new Random(42); 
    }

    public Solucion ejecutar(int iteraciones) {
        Solucion mejorSolucionGlobal = null;
        double mejorCosteGlobal = Double.MAX_VALUE;

        System.out.println("Iniciando GRASP (LRC = " + tamañoLRC + ", Iteraciones = " + iteraciones + ")...");

        for (int i = 0; i < iteraciones; i++) {
            // Fase 1: Construcción Aleatorizada
            Solucion solucionActual = faseConstructiva();

            // Fase 2: Búsquedas Locales
            for (BusquedaLocal bl : busquedasLocales) {
                solucionActual = bl.mejorar(solucionActual, problema);
            }

            // Actualizar la mejor solución si hemos mejorado
            if (mejorSolucionGlobal == null || solucionActual.getCosteTotal() < mejorCosteGlobal) {
                mejorSolucionGlobal = new Solucion(solucionActual); // Copia de la mejor
                mejorCosteGlobal = solucionActual.getCosteTotal();
                System.out.println("  -> ¡Nueva mejor solución en iteración " + (i+1) + "! Coste: " + mejorCosteGlobal);
            }
        }

        System.out.println("GRASP Finalizado.");
        return mejorSolucionGlobal;
    }

    private Solucion faseConstructiva() {
        Solucion sol = new Solucion(problema);

        for (Cliente cliente : problema.getClientes()) {
            double demandaRestante = cliente.getDemanda();
            int cId = cliente.getId();

            while (demandaRestante > 0) {
                List<Candidato> candidatos = new ArrayList<>();

                // Evaluamos todas las opciones
                for (Fundation inst : problema.getInstalaciones()) {
                    int iId = inst.getId();
                    double capRestante = sol.getCapacidadRestante()[iId];

                    if (capRestante > 0 && esCompatible(sol, cId, iId)) {
                        double costeEvaluado = problema.getCostosTransporte()[cId][iId];
                        if (!sol.getInstalacionesAbiertas()[iId]) {
                            costeEvaluado += (inst.getCostoFijo() / inst.getCapacidad());
                        }
                        candidatos.add(new Candidato(iId, costeEvaluado));
                    }
                }

                if (candidatos.isEmpty()) break; // Seguridad anti-cuelgues

                // Ordenamos por pseudo-coste (de más barato a más caro)
                candidatos.sort(Comparator.comparingDouble(c -> c.coste));

                // Cortamos la lista al tamaño del LRC (ej. las 3 mejores)
                int limite = Math.min(tamañoLRC, candidatos.size());
                List<Candidato> lrc = candidatos.subList(0, limite);

                // Elegimos una al azar dentro de esas 3 mejores
                Candidato elegido = lrc.get(random.nextInt(lrc.size()));
                int mejorInstalacion = elegido.idInstalacion;

                // Asignamos
                double capDisponible = sol.getCapacidadRestante()[mejorInstalacion];
                double cantidadAsignar = Math.min(demandaRestante, capDisponible);

                sol.añadirSuministro(cId, mejorInstalacion, cantidadAsignar);
                demandaRestante -= cantidadAsignar;
            }
        }
        return sol;
    }

    private boolean esCompatible(Solucion sol, int clienteId, int instalacionId) {
        for (int k = 0; k < problema.getClientes().size(); k++) {
            if (sol.getSuministros()[k][instalacionId] > 0 && problema.sonIncompatibles(clienteId, k)) {
                return false;
            }
        }
        return true;
    }

    // Clase auxiliar muy simple para guardar la pareja (ID, coste)
    private static class Candidato {
        int idInstalacion;
        double coste;
        Candidato(int id, double coste) {
            this.idInstalacion = id;
            this.coste = coste;
        }
    }
}