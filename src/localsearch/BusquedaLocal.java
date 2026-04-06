package localsearch;

import model.Problema;
import model.Solucion;

public interface BusquedaLocal {
    // Aplica como máximo 1 movimiento (el mejor que encuentre) sobre la solución.
    // Devuelve true si aplicó un movimiento que mejora.
    boolean aplicarMejorMovimiento(Solucion sol, Problema problema);

    // Recibe la solución actual y el problema, y devuelve una solución mejorada
    // aplicando como máximo 1 movimiento.
    default Solucion mejorar(Solucion solucionActual, Problema problema) {
        Solucion copia = new Solucion(solucionActual);
        aplicarMejorMovimiento(copia, problema);
        return copia;
    }
}