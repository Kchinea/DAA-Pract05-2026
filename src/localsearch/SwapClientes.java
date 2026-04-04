package localsearch;

import model.Problema;
import model.Solucion;

public class SwapClientes implements BusquedaLocal {

    @Override
    public Solucion mejorar(Solucion solucionActual, Problema problema) {
        boolean mejora = true;
        Solucion mejorSolucion = new Solucion(solucionActual);

        while (mejora) {
            mejora = false;

            for (int i1 = 0; i1 < problema.getClientes().size(); i1++) {
                for (int i2 = i1 + 1; i2 < problema.getClientes().size(); i2++) {
                    
                    // Buscamos la instalación principal de i1 y i2 (simplificación para el Swap total)
                    int j1 = obtenerInstalacionPrincipal(mejorSolucion, i1);
                    int j2 = obtenerInstalacionPrincipal(mejorSolucion, i2);

                    if (j1 != -1 && j2 != -1 && j1 != j2) {
                        double cant1 = mejorSolucion.getSuministros()[i1][j1];
                        double cant2 = mejorSolucion.getSuministros()[i2][j2];

                        // Quitamos temporalmente a ambos para evaluar bien las capacidades e incompatibilidades cruzadas
                        Solucion solCandidata = new Solucion(mejorSolucion);
                        solCandidata.quitarSuministro(i1, j1, cant1);
                        solCandidata.quitarSuministro(i2, j2, cant2);

                        // Comprobamos si, tras quitarlos, caben intercambiados y son compatibles
                        if (solCandidata.getCapacidadRestante()[j2] >= cant1 && 
                            solCandidata.getCapacidadRestante()[j1] >= cant2 &&
                            esCompatible(solCandidata, problema, i1, j2) && 
                            esCompatible(solCandidata, problema, i2, j1)) {
                            
                            // Aplicamos el intercambio
                            solCandidata.añadirSuministro(i1, j2, cant1);
                            solCandidata.añadirSuministro(i2, j1, cant2);

                            if (solCandidata.getCosteTotal() < mejorSolucion.getCosteTotal() - 0.001) { // -0.001 para evitar bucles por precisión de coma flotante
                                mejorSolucion = solCandidata;
                                mejora = true;
                                break;
                            }
                        }
                    }
                }
                if (mejora) break;
            }
        }
        return mejorSolucion;
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

    private boolean esCompatible(Solucion sol, Problema problema, int clienteId, int instalacionId) {
        for (int k = 0; k < problema.getClientes().size(); k++) {
            if (sol.getSuministros()[k][instalacionId] > 0 && problema.sonIncompatibles(clienteId, k)) {
                return false;
            }
        }
        return true;
    }
}