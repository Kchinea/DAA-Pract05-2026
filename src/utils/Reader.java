package utils;

import model.Cliente;
import model.Fundation;
import model.Problema;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Reader {

    public Problema leerInstancia(String rutaArchivo) {
        try {
            Path ruta = resolverRutaInstancia(rutaArchivo);
            if (ruta == null) {
                System.err.println("Error al leer el archivo: " + rutaArchivo + " (no encontrado desde: " + System.getProperty("user.dir") + ")");
                return null;
            }

            // 1. Leemos todo el contenido del archivo como un único y gran String
            String contenido = new String(Files.readAllBytes(ruta));

            // 2. Extraer tamaños (Warehouses y Stores)
            int numInstalaciones = extraerEntero(contenido, "Warehouses\\s*=\\s*(\\d+);");
            int numClientes = extraerEntero(contenido, "Stores\\s*=\\s*(\\d+);");

            // 3. Extraer los arrays de datos simples
            double[] capacidades = extraerArray(contenido, "Capacity\\s*=\\s*\\[(.*?)\\];");
            double[] costosFijos = extraerArray(contenido, "FixedCost\\s*=\\s*\\[(.*?)\\];");
            double[] demandas = extraerArray(contenido, "Goods\\s*=\\s*\\[(.*?)\\];");

            // 4. Crear los objetos Instalacion y Cliente
            List<Fundation> instalaciones = new ArrayList<>();
            for (int j = 0; j < numInstalaciones; j++) {
                instalaciones.add(new Fundation(j, costosFijos[j], capacidades[j]));
            }

            List<Cliente> clientes = new ArrayList<>();
            for (int i = 0; i < numClientes; i++) {
                clientes.add(new Cliente(i, demandas[i]));
            }

            // 5. Extraer la Matriz de Costos de Transporte (SupplyCost)
            double[][] costosTransporte = new double[numClientes][numInstalaciones];
            String bloqueCostos = extraerBloque(contenido, "SupplyCost\\s*=\\s*\\[\\|(.*?)\\|\\];");
            // Las filas en dzn están separadas por el símbolo '|'
            String[] filas = bloqueCostos.split("\\|");
            
            for (int i = 0; i < numClientes; i++) {
                // Limpiamos la fila de espacios y comas sobrantes
                String[] valoresFila = filas[i].trim().split("\\s*,\\s*");
                for (int j = 0; j < numInstalaciones; j++) {
                    costosTransporte[i][j] = Double.parseDouble(valoresFila[j]);
                }
            }

            // 6. Extraer las Incompatibilidades (los pares al final del archivo)
            // Asumimos que los pares de incompatibilidades se leen extrayendo los números que quedan 
            // tras la palabra "Incompatibilities" o leyendo directamente los pares de la forma "cliente1, cliente2"
            boolean[][] incompatibilidades = new boolean[numClientes][numClientes];
            
            // Buscar todas las ocurrencias del tipo "numero, numero" que estén separadas por "|" al final
            // (Esta es una forma simplificada; si tu archivo tiene una etiqueta 'Incompatibilities = [|...|];', 
            // usarías la misma lógica que SupplyCost).
            String bloqueIncomp = contenido.substring(contenido.lastIndexOf("];") + 2); 
            String[] pares = bloqueIncomp.split("\\|");
            for (String par : pares) {
                if (par.contains(",")) {
                    String[] ids = par.trim().split("\\s*,\\s*");
                    if(ids.length == 2) {
                        // Ojo: En los dzn a veces los IDs empiezan en 1. Si es tu caso, hay que restar 1.
                        // Asumimos que empiezan en 1 según el estándar MiniZinc.
                        int c1 = Integer.parseInt(ids[0].trim()) - 1; 
                        int c2 = Integer.parseInt(ids[1].trim()) - 1;
                        
                        // Marcamos la matriz en ambas direcciones
                        incompatibilidades[c1][c2] = true;
                        incompatibilidades[c2][c1] = true;
                    }
                }
            }

            // 7. Retornamos el escenario (Problema) ya montado
            return new Problema(clientes, instalaciones, costosTransporte, incompatibilidades);

        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
            return null;
        }
    }

    /**
     * Resuelve la ruta de la instancia aunque el programa se ejecute desde carpetas distintas.
     *
     * <p>Casos típicos en este repo:
     * <ul>
     *   <li>Ejecutar desde la raíz del proyecto: ./instances/Public/...</li>
     *   <li>Ejecutar desde la carpeta padre que contiene otra carpeta Practica5-DAA: ./Practica5-DAA/instances/Public/...</li>
     * </ul>
     */
    private Path resolverRutaInstancia(String rutaArchivo) {
        // 0) Resolver usando el localizador del proyecto (robusto ante CWD variable)
        Path resolved = ProjectPaths.resolveInstanceFile(rutaArchivo);
        if (resolved != null) return resolved;

        // 1) Tal cual (por si es absoluta)
        Path p = Paths.get(rutaArchivo);
        if (Files.exists(p)) return p.toAbsolutePath().normalize();

        // 2) Relativa al directorio de ejecución
        Path base = Paths.get(System.getProperty("user.dir"));
        Path p2 = base.resolve(rutaArchivo);
        if (Files.exists(p2)) return p2.toAbsolutePath().normalize();

        // 3) Fallback: si estás en la carpeta padre, suele existir ./Practica5-DAA/
        Path p3 = base.resolve("Practica5-DAA").resolve(rutaArchivo);
        if (Files.exists(p3)) return p3.toAbsolutePath().normalize();

        // 4) Fallback extra por si hay doble anidación
        Path p4 = base.resolve("Practica5-DAA").resolve("Practica5-DAA").resolve(rutaArchivo);
        if (Files.exists(p4)) return p4.toAbsolutePath().normalize();

        return null;
    }

    // --- Métodos Auxiliares Internos para limpiar el código ---

    private int extraerEntero(String texto, String regex) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(regex).matcher(texto);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    private String extraerBloque(String texto, String regex) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.DOTALL).matcher(texto);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private double[] extraerArray(String texto, String regex) {
        String bloque = extraerBloque(texto, regex);
        String[] valores = bloque.split(",");
        double[] array = new double[valores.length];
        for (int i = 0; i < valores.length; i++) {
            array[i] = Double.parseDouble(valores[i].trim());
        }
        return array;
    }
}