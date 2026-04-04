package localsearch;

import model.Problema;
import model.Solucion;

public interface BusquedaLocal {
    // Recibe la solución actual y el problema, y devuelve una solución mejorada 
    // (o la misma si no encontró mejora)
    Solucion mejorar(Solucion solucionActual, Problema problema);
}