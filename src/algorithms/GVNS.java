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

    private static final double EPS = 1e-9;
    private static final double EXPLORATION_RATE = 0.2;

public GVNS(Problema problema, List<BusquedaLocal> entornos) {
        this.problema = problema;
        this.entornos = entornos;
        
        this.pesosRL = new double[entornos.size()];
        this.random = new Random();
        
        for(int i = 0; i < pesosRL.length; i++) {
            pesosRL[i] = 1.0; 
        }
    }

    public Solucion ejecutar(Solucion solucionInicial, int iteracionesMaximas) {
        return ejecutar(solucionInicial, iteracionesMaximas, 5);
    }

    public Solucion ejecutar(Solucion solucionInicial, int iteracionesMaximas, int kMax) {
        Solucion mejorGlobal = new Solucion(solucionInicial);
        int iteracionesSinMejora = 0;
        int k = 1; 
        int kMaxLocal = Math.max(1, kMax); // Fuerza máxima de la perturbación

        System.out.println("Iniciando GVNS-RL...");

        while (iteracionesSinMejora < iteracionesMaximas) {
            // 1. Shaking (Perturbación)
            Solucion solPerturbada = perturbar(mejorGlobal, k);

            // 2. VND con Reinforcement Learning
            Solucion solMejorada = rvndConReinforcementLearning(solPerturbada);

            // 3. Criterio de Aceptación
            if (demandaSatisfecha(solMejorada)  && solMejorada.getCosteTotal() < mejorGlobal.getCosteTotal() - 0.001) {
                mejorGlobal = new Solucion(solMejorada);
                k = 1; 
                iteracionesSinMejora = 0;
            } else {
                k = (k % kMaxLocal) + 1; 
                iteracionesSinMejora++;
            }
        }
        return mejorGlobal;
    }

    private boolean demandaSatisfecha(Solucion sol) {
        int n = problema.getClientes().size();
        int m = problema.getInstalaciones().size();

        for (int i = 0; i < n; i++) {
            double asignado = 0.0;
            for (int j = 0; j < m; j++) {
                asignado += sol.getSuministros()[i][j];
            }
            double demanda = problema.getClientes().get(i).getDemanda();
            if (asignado + 1e-6 < demanda) {
                return false;
            }
        }
        return true;
    }

    // --- FASE DE SHAKING ---
    private Solucion perturbar(Solucion sol, int k) {
        Solucion perturbada = new Solucion(sol);
        
        // Destruimos la asignación de 'k' clientes al azar y los reasignamos vorazmente
        for (int p = 0; p < k; p++) {
            int clienteAleatorio = random.nextInt(problema.getClientes().size());

            // Guardar asignación original del cliente para poder revertir si la reparación falla
            double[] asignacionOriginal = perturbada.getSuministros()[clienteAleatorio].clone();
            
            // Quitar todo su suministro
            for (int j = 0; j < problema.getInstalaciones().size(); j++) {
                double cant = asignacionOriginal[j];
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

                if (mejorJ == -1) break; // No hay instalación factible para seguir reparando
                double asig = Math.min(demandaRestante, perturbada.getCapacidadRestante()[mejorJ]);
                perturbada.añadirSuministro(clienteAleatorio, mejorJ, asig);
                demandaRestante -= asig;
            }

            // Si no hemos podido satisfacer toda la demanda, revertimos al estado original
            if (demandaRestante > EPS) {
                for (int j = 0; j < problema.getInstalaciones().size(); j++) {
                    double cant = perturbada.getSuministros()[clienteAleatorio][j];
                    if (cant > 0) {
                        perturbada.quitarSuministro(clienteAleatorio, j, cant);
                    }
                }
                for (int j = 0; j < problema.getInstalaciones().size(); j++) {
                    double cant = asignacionOriginal[j];
                    if (cant > 0) {
                        perturbada.añadirSuministro(clienteAleatorio, j, cant);
                    }
                }
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

            // OPT: evitar clonar Solucion (n*m) en cada intento.
            // aplicarMejorMovimiento aplica como máximo 1 movimiento si mejora.
            boolean mejora = bl.aplicarMejorMovimiento(actual, problema);

            if (mejora) {
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
        if (disponibles.isEmpty()) {
            return -1;
        }

        if (random.nextDouble() < EXPLORATION_RATE) {
            return disponibles.get(random.nextInt(disponibles.size()));
        }

        List<Integer> ordenados = new ArrayList<>(disponibles);
        ordenados.sort((a, b) -> Double.compare(pesosRL[b], pesosRL[a]));

        double sumaPesos = 0.0;
        for (int idx : ordenados) sumaPesos += pesosRL[idx];
        if (sumaPesos <= 0.0) {
            return disponibles.get(random.nextInt(disponibles.size()));
        }

        double valorAleatorio = random.nextDouble() * sumaPesos;
        double acumulado = 0.0;

        for (int idx : ordenados) {
            acumulado += pesosRL[idx];
            if (valorAleatorio <= acumulado) {
                return idx;
            }
        }
        return ordenados.get(ordenados.size() - 1); // Por seguridad unicamente
    }
}