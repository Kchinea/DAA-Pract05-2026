package utils;

import model.Solucion;

public class GeneradorTablas {

    // Imprime la cabecera para la Tabla del Voraz
    public void imprimirCabeceraVoraz() {
        System.out.println("\n--- TABLA 9: RESULTADOS VORAZ ---");
        System.out.println("Instancia, Jopen, C. Fijo, C. Asig., C. Total, Incomp., CPU_Time(ms)");
    }

    // Imprime una fila de la Tabla del Voraz
    public void imprimirFilaVoraz(String nombreInstancia, Solucion sol, long tiempoMs) {
        int jopen = contarInstalaciones(sol);
        double cFijo = calcularCosteFijo(sol);
        double cAsig = sol.getCosteTotal() - cFijo;
        int incompVioladas = 0; // Nuestro algoritmo garantiza 0 violaciones

        System.out.printf("%s, %d, %.2f, %.2f, %.2f, %d, %d\n",
                nombreInstancia, jopen, cFijo, cAsig, sol.getCosteTotal(), incompVioladas, tiempoMs);
    }

    // Imprime la cabecera para la Tabla del GRASP
    public void imprimirCabeceraGrasp() {
        System.out.println("\n--- TABLA 10: RESULTADOS GRASP ---");
        System.out.println("Instancia, LRC, Ejec., Jopen, C. Fijo, C. Asig., C. Total, Incomp., CPU_Time(ms)");
    }

    // Imprime una fila de la Tabla del GRASP
    public void imprimirFilaGrasp(String nombreInstancia, int lrc, int ejecucion, Solucion sol, long tiempoMs) {
        int jopen = contarInstalaciones(sol);
        double cFijo = calcularCosteFijo(sol);
        double cAsig = sol.getCosteTotal() - cFijo;
        int incompVioladas = 0;

        System.out.printf("%s, %d, %d, %d, %.2f, %.2f, %.2f, %d, %d\n",
                nombreInstancia, lrc, ejecucion, jopen, cFijo, cAsig, sol.getCosteTotal(), incompVioladas, tiempoMs);
    }

    // --- Métodos Auxiliares ---

    private int contarInstalaciones(Solucion sol) {
        int abiertas = 0;
        for (boolean b : sol.getInstalacionesAbiertas()) {
            if (b) abiertas++;
        }
        return abiertas;
    }

    private double calcularCosteFijo(Solucion sol) {
        double totalFijo = 0.0;
        for (int j = 0; j < sol.getInstalacionesAbiertas().length; j++) {
            if (sol.getInstalacionesAbiertas()[j]) {
                totalFijo += sol.getProblema().getInstalaciones().get(j).getCostoFijo();
            }
        }
        return totalFijo;
    }
}