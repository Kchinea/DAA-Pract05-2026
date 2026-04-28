package main;

import algorithms.Voraz;
import algorithms.GVNS;
import algorithms.Grasp;
import algorithms.RVND;
import localsearch.BusquedaLocal;
import localsearch.EliminarIncompatibilidad;
import localsearch.Shift;
import localsearch.SwapClientes;
import localsearch.SwapInstalaciones;
import model.Problema;
import model.Solucion;
import utils.Menu;
import utils.MenuConfig;
import utils.ProjectPaths;
import utils.Reader;
import utils.TablaExporter;
import utils.tablas.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    
    public static void main(String[] args) {
        Reader lector = new Reader();

        List<String> instancias = listarInstanciasDisponibles();
        MenuConfig cfg = new Menu().preguntarConfiguracion(instancias);

        List<String> aEjecutar;
        if (cfg.usarTodasLasInstancias()) {
            aEjecutar = instancias;
        } else {
            aEjecutar = cfg.instanciaUnica() == null ? List.of() : List.of(cfg.instanciaUnica());
        }

        if (aEjecutar.isEmpty()) {
            System.out.println("No hay instancias a ejecutar.");
            return;
        }

        Path outRoot = ProjectPaths.outputTablasDir();

        for (String nombreInstancia : aEjecutar) {
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

            int ejecuciones = (cfg.modo() == MenuConfig.Modo.ESTUDIO) ? Math.max(1, cfg.ejecuciones()) : 1;

            // --- SOLUCIÓN INICIAL (y tabla 9/10) ---
            Solucion solInicialParaMeta;
            if (cfg.solucionInicial() == MenuConfig.SolucionInicial.VORAZ) {
                Voraz voraz = new Voraz(problema);
                long ini = System.currentTimeMillis();
                Solucion solInicial = voraz.ejecutar();
                long tInicialMs = System.currentTimeMillis() - ini;

                TablaResultadosVoraz t = new TablaResultadosVoraz();
                t.addResultado(nombreInstancia, solInicial, tInicialMs);
                TablaExporter.exportar(dirInst, "Tabla9.txt", t);

                solInicialParaMeta = solInicial;
            } else {
                TablaResultadosGrasp t = new TablaResultadosGrasp();
                Solucion mejorInicial = null;
                double mejorCoste = Double.POSITIVE_INFINITY;

                for (int ejec = 1; ejec <= ejecuciones; ejec++) {
                    Grasp grasp = new Grasp(problema, blBasicas, cfg.graspLrc());
                    long ini = System.currentTimeMillis();
                    Solucion solInicial = grasp.ejecutar(cfg.graspIteraciones());
                    long tInicialMs = System.currentTimeMillis() - ini;

                    t.addResultado(nombreInstancia, cfg.graspLrc(), ejec, solInicial, tInicialMs);
                    if (solInicial.getCosteTotal() < mejorCoste) {
                        mejorCoste = solInicial.getCosteTotal();
                        mejorInicial = solInicial;
                    }
                }

                TablaExporter.exportar(dirInst, "Tabla10.txt", t);
                solInicialParaMeta = mejorInicial;
            }

            // --- METAHEURÍSTICA (y tabla 11/12) ---
            Solucion mejorFinal = null;
            double mejorCosteFinal = Double.POSITIVE_INFINITY;

            if (cfg.metaheuristica() == MenuConfig.Metaheuristica.GVNS) {
                TablaResultadosGvns t = new TablaResultadosGvns();

                List<Integer> kmaxVals = cfg.gvnsKmaxValores();
                if (kmaxVals == null || kmaxVals.isEmpty()) {
                    kmaxVals = List.of(3);
                }

                for (int kmax : kmaxVals) {
                    int kMaxLocal = Math.max(1, kmax);
                    for (int ejec = 1; ejec <= ejecuciones; ejec++) {
                        GVNS gvns = new GVNS(problema, blCompletas);
                        long ini = System.currentTimeMillis();
                        Solucion solFinal = gvns.ejecutar(solInicialParaMeta, cfg.gvnsIteracionesSinMejora(), kMaxLocal);
                        long tMetaMs = System.currentTimeMillis() - ini;

                        t.addResultado(nombreInstancia, kMaxLocal, ejec, solFinal, tMetaMs);
                        if (solFinal.getCosteTotal() < mejorCosteFinal) {
                            mejorCosteFinal = solFinal.getCosteTotal();
                            mejorFinal = solFinal;
                        }
                    }
                }

                TablaExporter.exportar(dirInst, "Tabla11.txt", t);
            } else {
                TablaResultadosRvnd t = new TablaResultadosRvnd();

                for (int ejec = 1; ejec <= ejecuciones; ejec++) {
                    RVND rvnd = new RVND(problema, blCompletas);
                    long ini = System.currentTimeMillis();
                    Solucion solFinal = rvnd.ejecutar(solInicialParaMeta);
                    long tMetaMs = System.currentTimeMillis() - ini;

                    t.addResultado(nombreInstancia, ejec, solFinal, tMetaMs);
                    if (solFinal.getCosteTotal() < mejorCosteFinal) {
                        mejorCosteFinal = solFinal.getCosteTotal();
                        mejorFinal = solFinal;
                    }
                }

                TablaExporter.exportar(dirInst, "Tabla12.txt", t);
            }

            // Tablas 4-8: se exportan para la mejor solución final del modo elegido
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

    private static List<String> listarInstanciasDisponibles() {
        Path instancesPublic = ProjectPaths.findInstancesPublicDir();
        if (instancesPublic == null) {
            return List.of();
        }
        try {
            return Files.list(instancesPublic)
                    .filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".dzn"))
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return List.of();
        }
    }

}