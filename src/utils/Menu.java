package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Menú interactivo por consola para configurar ejecuciones y exportación de tablas. */
public class Menu {

    public MenuConfig preguntarConfiguracion(List<String> instanciasDisponibles) {
        Scanner sc = new Scanner(System.in);

        System.out.println("\n=== MENÚ ===");

        MenuConfig.Modo modoEjecucion = elegir(sc,
                "Modo de ejecución",
                List.of("Normal (1 ejecución)", "Estudio (varias ejecuciones + barrido parámetros)"),
                1
        ) == 1 ? MenuConfig.Modo.NORMAL : MenuConfig.Modo.ESTUDIO;

        int ejecuciones = 1;
        if (modoEjecucion == MenuConfig.Modo.ESTUDIO) {
            ejecuciones = leerEntero(sc, "Ejecuciones por configuración (por defecto 3)", 3);
            if (ejecuciones < 1) ejecuciones = 1;
        }
        //MODIFICACION
        MenuConfig.SolucionInicial inicial = elegir(sc,
            "Solución inicial para GVNS/RVND/VND",
                List.of("VorazRef", "GRASP"),
                1
        ) == 1 ? MenuConfig.SolucionInicial.VORAZ : MenuConfig.SolucionInicial.GRASP;

        int metaChoice = elegir(sc,
            "Metaheurística a ejecutar",
            List.of("GVNS", "RVND", "VND (secuencial)"),
            1
        );

        MenuConfig.Metaheuristica meta = switch (metaChoice) {
            case 1 -> MenuConfig.Metaheuristica.GVNS;
            case 2 -> MenuConfig.Metaheuristica.RVND;
            default -> MenuConfig.Metaheuristica.VND;
        };

        int modo = elegir(sc,
                "Instancias a ejecutar",
                List.of("Todas", "Una"),
                2
        );

        boolean todas = modo == 1;
        String unica = null;
        if (!todas) {
            unica = elegirInstancia(sc, instanciasDisponibles);
        }

        int graspIter = 10;
        int graspLrc = 3;
        if (inicial == MenuConfig.SolucionInicial.GRASP) {
            graspIter = leerEntero(sc, "Iteraciones de GRASP (por defecto 10)", 10);
            graspLrc = leerEntero(sc, "Tamaño LRC (por defecto 3)", 3);
        }

        int gvnsNoImprove = 50;
        List<Integer> gvnsKmaxValores = List.of(3);
        if (meta == MenuConfig.Metaheuristica.GVNS) {
            gvnsNoImprove = leerEntero(sc, "GVNS: iteraciones sin mejora (por defecto 50)", 50);

            if (modoEjecucion == MenuConfig.Modo.ESTUDIO) {
                gvnsKmaxValores = leerListaEnteros(sc,
                        "GVNS: valores de kmax (separados por comas; por defecto 2,3)",
                        List.of(2, 3)
                );
            } else {
                int gvnsKmax = leerEntero(sc, "GVNS: kmax (por defecto 3)", 3);
                gvnsKmaxValores = List.of(Math.max(1, gvnsKmax));
            }
        }

        return new MenuConfig(
                modoEjecucion,
                todas,
                unica,
                inicial,
                meta,
                ejecuciones,
                graspIter,
                graspLrc,
                gvnsNoImprove,
                gvnsKmaxValores
        );
    }

    private static int elegirInstanciaIndex(Scanner sc, List<String> instancias) {
        System.out.println("\nInstancias disponibles:");
        for (int i = 0; i < instancias.size(); i++) {
            System.out.println("  " + (i + 1) + ") " + instancias.get(i));
        }
        int idx;
        while (true) {
            System.out.print("Elige una instancia [1-" + instancias.size() + "]: ");
            String raw = nextLineOrNull(sc);
            if (raw == null) return 0; // stdin cerrado -> primera instancia
            String s = raw.trim();
            try {
                idx = Integer.parseInt(s);
                if (idx >= 1 && idx <= instancias.size()) {
                    return idx - 1;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Opción no válida.");
        }
    }

    private static String elegirInstancia(Scanner sc, List<String> instancias) {
        if (instancias.isEmpty()) {
            System.out.println("No se encontraron instancias.");
            return null;
        }
        int idx = elegirInstanciaIndex(sc, instancias);
        return instancias.get(idx);
    }

    private static int elegir(Scanner sc, String titulo, List<String> opciones, int defecto) {
        System.out.println("\n" + titulo + ":");
        for (int i = 0; i < opciones.size(); i++) {
            System.out.println("  " + (i + 1) + ") " + opciones.get(i));
        }
        while (true) {
            System.out.print("Elige [1-" + opciones.size() + "] (defecto " + defecto + "): ");
            String raw = nextLineOrNull(sc);
            if (raw == null) return defecto; // stdin cerrado
            String s = raw.trim();
            if (s.isBlank()) return defecto;
            try {
                int v = Integer.parseInt(s);
                if (v >= 1 && v <= opciones.size()) return v;
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Opción no válida.");
        }
    }

    private static int leerEntero(Scanner sc, String prompt, int defecto) {
        while (true) {
            System.out.print(prompt + ": ");
            String raw = nextLineOrNull(sc);
            if (raw == null) return defecto; // stdin cerrado
            String s = raw.trim();
            if (s.isBlank()) return defecto;
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
                System.out.println("Número no válido.");
            }
        }
    }

    private static List<Integer> leerListaEnteros(Scanner sc, String prompt, List<Integer> defecto) {
        while (true) {
            System.out.print(prompt + ": ");
            String raw = nextLineOrNull(sc);
            if (raw == null) return defecto; // stdin cerrado
            String s = raw.trim();
            if (s.isBlank()) return defecto;

            String[] parts = s.split(",");
            List<Integer> vals = new ArrayList<>();
            boolean ok = true;

            for (String p : parts) {
                String t = p.trim();
                if (t.isEmpty()) continue;
                try {
                    int v = Integer.parseInt(t);
                    if (v < 1) {
                        ok = false;
                        break;
                    }
                    vals.add(v);
                } catch (NumberFormatException e) {
                    ok = false;
                    break;
                }
            }

            if (ok && !vals.isEmpty()) return vals;
            System.out.println("Lista no válida. Ejemplo: 2,3,4");
        }
    }

    private static String nextLineOrNull(Scanner sc) {
        try {
            if (!sc.hasNextLine()) return null;
            return sc.nextLine();
        } catch (Exception e) {
            return null;
        }
    }
}
