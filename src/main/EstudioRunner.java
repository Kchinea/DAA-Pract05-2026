package main;

import algorithms.GVNS;
import algorithms.Grasp;
import localsearch.BusquedaLocal;
import localsearch.EliminarIncompatibilidad;
import localsearch.Shift;
import localsearch.SwapClientes;
import localsearch.SwapInstalaciones;
import model.Problema;
import model.Solucion;
import utils.ProjectPaths;
import utils.Reader;
import utils.TablaExporter;
import utils.tablas.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Runner no interactivo para regenerar tablas de estudio (wlp01..wlp05).
 *
 * <p>Esto evita el menú y hace el experimento reproducible para el informe.
 */
public final class EstudioRunner {

    public static void main(String[] args) {
        // Configuración fija (alineada con lo observado en tablas existentes)
        int ejecuciones = 3;
        int graspIteraciones = 10;
        int graspLrc = 3;
        int gvnsIteracionesSinMejora = 50;
        List<Integer> gvnsKmaxValores = List.of(2, 3);

        List<String> instancias = List.of(
                "wlp01.dzn",
                "wlp02.dzn",
                "wlp03.dzn",
                "wlp04.dzn",
                "wlp05.dzn"
        );

        Reader lector = new Reader();
        Path outRoot = ProjectPaths.outputTablasDir();

        for (String nombreInstancia : instancias) {
            Problema problema = lector.leerInstancia("instances/Public/" + nombreInstancia);
            if (problema == null) {
                System.out.println("Saltando instancia: " + nombreInstancia);
                continue;
            }

            // Vecindades
            List<BusquedaLocal> blBasicas = new ArrayList<>();
            blBasicas.add(new Shift());
            blBasicas.add(new SwapClientes());

            List<BusquedaLocal> blCompletas = new ArrayList<>();
            blCompletas.add(new Shift());
            blCompletas.add(new SwapClientes());
            blCompletas.add(new SwapInstalaciones());
            blCompletas.add(new EliminarIncompatibilidad());

            Path dirInst = outRoot.resolve(nombreInstancia);

            // Tablas 1-3
            TablaExporter.exportar(dirInst, "Tabla1.txt", new Tabla1ParametrosInstancia(problema));
            TablaExporter.exportar(dirInst, "Tabla2.txt", new Tabla2MatrizCostes(problema));
            TablaExporter.exportar(dirInst, "Tabla3.txt", new Tabla3ParesIncompatibles(problema));

            // --- Solución inicial: GRASP (Tabla10) ---
            TablaResultadosGrasp tGrasp = new TablaResultadosGrasp();
            Solucion mejorInicial = null;
            double mejorCoste = Double.POSITIVE_INFINITY;

            for (int ejec = 1; ejec <= ejecuciones; ejec++) {
                Grasp grasp = new Grasp(problema, blBasicas, graspLrc);
                long ini = System.currentTimeMillis();
                Solucion solInicial = grasp.ejecutar(graspIteraciones);
                long tInicialMs = System.currentTimeMillis() - ini;

                tGrasp.addResultado(nombreInstancia, graspLrc, ejec, solInicial, tInicialMs);
                if (solInicial.getCosteTotal() < mejorCoste) {
                    mejorCoste = solInicial.getCosteTotal();
                    mejorInicial = solInicial;
                }
            }

            TablaExporter.exportar(dirInst, "Tabla10.txt", tGrasp);

            // --- Metaheurística: GVNS (Tabla11) ---
            TablaResultadosGvns tGvns = new TablaResultadosGvns();
            Solucion mejorFinal = null;
            double mejorCosteFinal = Double.POSITIVE_INFINITY;

            for (int kmax : gvnsKmaxValores) {
                int kMaxLocal = Math.max(1, kmax);
                for (int ejec = 1; ejec <= ejecuciones; ejec++) {
                    GVNS gvns = new GVNS(problema, blCompletas);
                    long ini = System.currentTimeMillis();
                    Solucion solFinal = gvns.ejecutar(mejorInicial, gvnsIteracionesSinMejora, kMaxLocal);
                    long tMetaMs = System.currentTimeMillis() - ini;

                    tGvns.addResultado(nombreInstancia, kMaxLocal, ejec, solFinal, tMetaMs);
                    if (solFinal.getCosteTotal() < mejorCosteFinal) {
                        mejorCosteFinal = solFinal.getCosteTotal();
                        mejorFinal = solFinal;
                    }
                }
            }

            TablaExporter.exportar(dirInst, "Tabla11.txt", tGvns);

            // Tablas 4-8 para la mejor solución final
            if (mejorFinal != null) {
                TablaExporter.exportar(dirInst, "Tabla4.txt", new Tabla4AperturaInstalaciones(problema, mejorFinal));
                TablaExporter.exportar(dirInst, "Tabla5.txt", new Tabla5AsignacionClientes(problema, mejorFinal));
                TablaExporter.exportar(dirInst, "Tabla6.txt", new Tabla6VerificacionRestricciones(problema, mejorFinal));
                TablaExporter.exportar(dirInst, "Tabla7.txt", new Tabla7DesgloseCostesVariables(problema, mejorFinal));
                TablaExporter.exportar(dirInst, "Tabla8.txt", new Tabla8CosteTotal(problema, mejorFinal));
            }

            System.out.println("Tablas exportadas: " + dirInst.toAbsolutePath());
        }

        System.out.println("\nSalida en: " + outRoot.toAbsolutePath());
    }
}
