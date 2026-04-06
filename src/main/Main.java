package main;

import algorithms.Voraz;
import algorithms.GVNS;
import algorithms.Grasp;
import localsearch.BusquedaLocal;
import localsearch.EliminarIncompatibilidad;
import localsearch.Shift;
import localsearch.SwapClientes;
import localsearch.SwapInstalaciones;
import model.Problema;
import model.Solucion;
import utils.Reader;
import utils.GeneradorTablas;

import java.util.ArrayList;
import java.util.List;

public class Main {
    
    public static void main(String[] args) {
        
        Reader lector = new Reader();
        GeneradorTablas tablas = new GeneradorTablas();
        // Datos de la ejecución
        String nombreInstancia = "wlp02.dzn";
        String rutaArchivo = "instances/Public/" + nombreInstancia; 
        
        Problema instancia = lector.leerInstancia(rutaArchivo);
        if (instancia == null) return;

        // ==========================================
        // 1. PRUEBA Y TABLA DEL VORAZ
        // ==========================================
        Voraz algoritmoVoraz = new Voraz(instancia);
        
        long inicioVoraz = System.currentTimeMillis();
        Solucion solVoraz = algoritmoVoraz.ejecutar();
        long tiempoVoraz = System.currentTimeMillis() - inicioVoraz;
        
        tablas.imprimirCabeceraVoraz();
        tablas.imprimirFilaVoraz(nombreInstancia, solVoraz, tiempoVoraz);

        // ==========================================
        // 2. PRUEBA Y TABLA DEL GRASP
        // ==========================================
        List<BusquedaLocal> busquedasLocales = new ArrayList<>();
        busquedasLocales.add(new Shift());
        busquedasLocales.add(new SwapClientes());

        int iteraciones = 10; // Subimos a 50 iteraciones para obtener mejores resultados
        
        tablas.imprimirCabeceraGrasp();

        // El PDF pide probar con LRC = 2 y LRC = 3, y hacer varias ejecuciones (ej. 3 por cada LRC)
        int[] valoresLRC = {2, 3};
        int numEjecuciones = 3;

        for (int lrc : valoresLRC) {
            for (int ejec = 1; ejec <= numEjecuciones; ejec++) {
                
                Grasp algoritmoGrasp = new Grasp(instancia, busquedasLocales, lrc);
                
                long inicioGrasp = System.currentTimeMillis();
                Solucion solGrasp = algoritmoGrasp.ejecutar(iteraciones);
                long tiempoGrasp = System.currentTimeMillis() - inicioGrasp;
                
                tablas.imprimirFilaGrasp(nombreInstancia, lrc, ejec, solGrasp, tiempoGrasp);
            }
        }

        System.out.println("\n=========================================");
        System.out.println("   PRUEBA DEL GVNS-RL (Entrega Final)    ");
        System.out.println("=========================================");
            
        List<BusquedaLocal> todasLasBLs = new ArrayList<>();
        todasLasBLs.add(new Shift());
        todasLasBLs.add(new SwapClientes());
        todasLasBLs.add(new SwapInstalaciones());
        todasLasBLs.add(new EliminarIncompatibilidad());
            
        GVNS gvns = new GVNS(instancia, todasLasBLs);
            
        long inicioGVNS = System.currentTimeMillis();
        // Le pasamos la solución Voraz como punto de partida y le damos 50 iteraciones sin mejora como tope
        Solucion solFinalGVNS = gvns.ejecutar(solVoraz, 50); 
        long tiempoGVNS = System.currentTimeMillis() - inicioGVNS;
            
        System.out.printf("Coste Final GVNS-RL: %.2f (Tiempo: %d ms)\n", solFinalGVNS.getCosteTotal(), tiempoGVNS);
        System.out.println("Instalaciones abiertas: " + contarInstalaciones(solFinalGVNS));
    }

    private static int contarInstalaciones(Solucion solucion) {
        if (solucion == null) return 0;
        boolean[] abiertas = solucion.getInstalacionesAbiertas();
        if (abiertas == null) return 0;

        int total = 0;
        for (boolean abierta : abiertas) {
            if (abierta) total++;
        }
        return total;
    }
}