package localsearch;

import model.Problema;
import model.Solucion;

public class Shift implements BusquedaLocal {

    @Override
    public Solucion mejorar(Solucion solucionActual, Problema problema) {
        boolean mejora = true;
        Solucion mejorSolucion = new Solucion(solucionActual);

        // Bucle hasta que no encontremos ninguna mejora (First Improvement / Best Improvement)
        while (mejora) {
            mejora = false;
            
            for (int i = 0; i < problema.getClientes().size(); i++) {
                for (int j1 = 0; j1 < problema.getInstalaciones().size(); j1++) {
                    
                    // Si el cliente 'i' recibe suministro de 'j1'
                    double cantidad = mejorSolucion.getSuministros()[i][j1];
                    if (cantidad > 0) {
                        
                        // Intentamos moverlo a un 'j2'
                        for (int j2 = 0; j2 < problema.getInstalaciones().size(); j2++) {
                            if (j1 != j2) {
                                // Comprobamos factibilidad: Capacidad y Compatibilidad
                                if (mejorSolucion.getCapacidadRestante()[j2] >= cantidad &&
                                    esCompatible(mejorSolucion, problema, i, j2)) {
                                    
                                    // Probamos el movimiento en una solución temporal
                                    Solucion solCandidata = new Solucion(mejorSolucion);
                                    solCandidata.quitarSuministro(i, j1, cantidad);
                                    solCandidata.añadirSuministro(i, j2, cantidad);
                                    
                                    // Si es estrictamente mejor, nos la quedamos
                                    if (solCandidata.getCosteTotal() < mejorSolucion.getCosteTotal()) {
                                        mejorSolucion = solCandidata;
                                        mejora = true;
                                        break; // Rompemos para aplicar el cambio y volver a empezar
                                    }
                                }
                            }
                        }
                    }
                    if (mejora) break;
                }
                if (mejora) break;
            }
        }
        return mejorSolucion;
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