package algorithms;

import localsearch.BusquedaLocal;
import model.Problema;
import model.Solucion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GVNS {
    private Problema problema;
    private List<BusquedaLocal> entornos;
    private double[] pesosRL;
    private Random random;

public GVNS(Problema problema, List<BusquedaLocal> entornos) {
        this.problema = problema;
        this.entornos = entornos;
        
        // ¡ESTAS SON LAS LÍNEAS QUE FALTAN!
        this.pesosRL = new double[entornos.size()];
        this.random = new Random();
        
        // Inicializamos los pesos del Reinforcement Learning a 1.0
        for(int i = 0; i < pesosRL.length; i++) {
            pesosRL[i] = 1.0; 
        }
    }

    public Solucion ejecutar(Solucion solucionInicial, int iteracionesMaximas) {
        Solucion mejorGlobal = new Solucion(solucionInicial);
        int iteracionesSinMejora = 0;
        int k = 1; 
        int kMax = 5; // Fuerza máxima de la perturbación

        System.out.println("Iniciando GVNS-RL...");

        while (iteracionesSinMejora < iteracionesMaximas) {
            // 1. Shaking (Perturbación)
            Solucion solPerturbada = perturbar(mejorGlobal, k);

            // 2. VND con Reinforcement Learning
            Solucion solMejorada = rvndConReinforcementLearning(solPerturbada);

            // 3. Criterio de Aceptación
            if (solMejorada.getCosteTotal() < mejorGlobal.getCosteTotal() - 0.001) {
                mejorGlobal = new Solucion(solMejorada);
                k = 1; 
                iteracionesSinMejora = 0;
            } else {
                k = (k % kMax) + 1; 
                iteracionesSinMejora++;
            }
        }
        return mejorGlobal;
    }

    // --- FASE DE SHAKING ---
    private Solucion perturbar(Solucion sol, int k) {
        Solucion perturbada = new Solucion(sol);
        
        // Destruimos la asignación de 'k' clientes al azar y los reasignamos vorazmente
        for (int p = 0; p < k; p++) {
            int clienteAleatorio = random.nextInt(problema.getClientes().size());
            
            // Quitar todo su suministro
            for (int j = 0; j < problema.getInstalaciones().size(); j++) {
                double cant = perturbada.getSuministros()[clienteAleatorio][j];
                if (cant > 0) {
                    perturbada.quitarSuministro(clienteAleatorio, j, cant);
                }
            }

            // Reasignar (Voraz simple)
            double demandaRestante = problema.getClientes().get(clienteAleatorio).getDemanda();
            while (demandaRestante > 0) {
                int mejorJ = -1;
                double mejorCoste = Double.MAX_VALUE;

                for (int j = 0; j < problema.getInstalaciones().size(); j++) {
                    if (perturbada.getCapacidadRestante()[j] > 0 && problema.esCompatible(perturbada, clienteAleatorio, j)) {
                        double coste = problema.getCostosTransporte()[clienteAleatorio][j];
                        if (!perturbada.getInstalacionesAbiertas()[j]) {
                            coste += (problema.getInstalaciones().get(j).getCostoFijo() / problema.getInstalaciones().get(j).getCapacidad());
                        }
                        if (coste < mejorCoste) {
                            mejorCoste = coste;
                            mejorJ = j;
                        }
                    }
                }

                if (mejorJ == -1) break; // Infactibilidad provocada por el shaking, se quedará parcial
                double asig = Math.min(demandaRestante, perturbada.getCapacidadRestante()[mejorJ]);
                perturbada.añadirSuministro(clienteAleatorio, mejorJ, asig);
                demandaRestante -= asig;
            }
        }
        return perturbada;
    }

    // --- FASE RVND-RL ---
    private Solucion rvndConReinforcementLearning(Solucion sol) {
        Solucion actual = new Solucion(sol);
        List<Integer> entornosDisponibles = new ArrayList<>();
        for (int i = 0; i < entornos.size(); i++) entornosDisponibles.add(i);

        while (!entornosDisponibles.isEmpty()) {
            // Seleccionar entorno usando la ruleta de pesos
            int indiceEntorno = seleccionarEntornoPorRuleta(entornosDisponibles);
            BusquedaLocal bl = entornos.get(indiceEntorno);

            Solucion candidata = bl.mejorar(actual, problema);

            if (candidata.getCosteTotal() < actual.getCosteTotal() - 0.001) {
                actual = candidata;
                // REFUERZO POSITIVO: Aumentamos el peso
                pesosRL[indiceEntorno] += 1.0; 
                
                // Reiniciamos los entornos disponibles porque hemos encontrado un nuevo mínimo local
                entornosDisponibles.clear();
                for (int i = 0; i < entornos.size(); i++) entornosDisponibles.add(i);
            } else {
                // REFUERZO NEGATIVO: Reducimos el peso (Decaimiento)
                pesosRL[indiceEntorno] = Math.max(0.1, pesosRL[indiceEntorno] * 0.9);
                
                // Lo quitamos de la lista para no volver a probarlo en este ciclo
                entornosDisponibles.remove(Integer.valueOf(indiceEntorno));
            }
        }
        return actual;
    }

    private int seleccionarEntornoPorRuleta(List<Integer> disponibles) {
        double sumaPesos = 0;
        for (int idx : disponibles) sumaPesos += pesosRL[idx];

        double valorAleatorio = random.nextDouble() * sumaPesos;
        double acumulado = 0;

        for (int idx : disponibles) {
            acumulado += pesosRL[idx];
            if (valorAleatorio <= acumulado) {
                return idx;
            }
        }
        return disponibles.get(disponibles.size() - 1); // Por seguridad
    }
}