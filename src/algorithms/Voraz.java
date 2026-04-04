
package algorithms;

import model.Problema;
import model.Solucion;
import model.Cliente;
import model.Fundation;

public class Voraz {
    private Problema problema;

    public Voraz(Problema problema) {
        this.problema = problema;
    }

    public Solucion ejecutar() {
        Solucion sol = new Solucion(problema);
        System.out.println("Ejecutando algoritmo Voraz...");

        // 1. Recorremos todos los clientes uno por uno
        for (Cliente cliente : problema.getClientes()) {
            double demandaRestante = cliente.getDemanda();
            int cId = cliente.getId();

            // 2. Multi-Source: Un cliente puede necesitar varias instalaciones 
            // para satisfacer toda su demanda. Repetimos hasta que su demanda sea 0.
            while (demandaRestante > 0) {
                int mejorInstalacion = -1;
                double mejorCoste = Double.MAX_VALUE;

                // 3. Evaluamos todas las instalaciones para encontrar la más barata
                for (Fundation inst : problema.getInstalaciones()) {
                    int iId = inst.getId();
                    double capRestante = sol.getCapacidadRestante()[iId];

                    // Solo nos valen instalaciones con hueco y que sean COMPATIBLES
                    if (capRestante > 0 && esCompatible(sol, cId, iId)) {
                        
                        // Calculamos el coste de esta opción
                        double costeEvaluado = problema.getCostosTransporte()[cId][iId];

                        // Si está cerrada, le sumamos la penalización del coste fijo
                        if (!sol.getInstalacionesAbiertas()[iId]) {
                            costeEvaluado += (inst.getCostoFijo() / inst.getCapacidad());
                        }

                        // Nos quedamos con la más barata
                        if (costeEvaluado < mejorCoste) {
                            mejorCoste = costeEvaluado;
                            mejorInstalacion = iId;
                        }
                    }
                }

                // 4. Medida de seguridad: Si no encontramos instalación válida, la instancia es infactible
                if (mejorInstalacion == -1) {
                    System.out.println("¡Aviso! No se pudo satisfacer toda la demanda del cliente " + cId);
                    break; 
                }

                // 5. Asignamos tanta demanda como podamos
                double capDisponible = sol.getCapacidadRestante()[mejorInstalacion];
                double cantidadAsignar = Math.min(demandaRestante, capDisponible);

                sol.añadirSuministro(cId, mejorInstalacion, cantidadAsignar);
                demandaRestante -= cantidadAsignar; // Actualizamos lo que le falta al cliente
            }
        }
        
        System.out.println("Voraz terminado. Coste total: " + sol.getCosteTotal());
        return sol;
    }

    // --- MÉTODO CLAVE: Comprobar las restricciones CI ---
    private boolean esCompatible(Solucion sol, int clienteActualId, int instalacionId) {
        // Revisamos a todos los clientes del problema
        for (int k = 0; k < problema.getClientes().size(); k++) {
            
            // Si el cliente 'k' ya está recibiendo suministros de esta instalación...
            if (sol.getSuministros()[k][instalacionId] > 0) {
                
                // ... comprobamos si se lleva mal con nuestro cliente actual
                if (problema.sonIncompatibles(clienteActualId, k)) {
                    return false; // ¡Peligro! Son incompatibles, abortar.
                }
            }
        }
        return true; // Vía libre, no hay conflictos.
    }
}