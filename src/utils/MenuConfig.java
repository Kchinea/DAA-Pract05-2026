package utils;

import java.util.List;

/** Configuración elegida por el usuario en el menú. */
public record MenuConfig(
        Modo modo,
        boolean usarTodasLasInstancias,
        String instanciaUnica,
        SolucionInicial solucionInicial,
        Metaheuristica metaheuristica,
        int ejecuciones,
        int graspIteraciones,
        int graspLrc,
        int gvnsIteracionesSinMejora,
        List<Integer> gvnsKmaxValores
) {
    public enum Modo { NORMAL, ESTUDIO }
    public enum SolucionInicial { VORAZ, GRASP }
    public enum Metaheuristica { GVNS, RVND }
}
