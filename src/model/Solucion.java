package model;

public class Solucion {
    private Problema problema; 
    private boolean[] instalacionesAbiertas; 
    private double[][] suministros;          
    private double[] capacidadRestante;      
    private double costeTotal;

    // Constructor original (vacío)
    public Solucion(Problema problema) {
        this.problema = problema;
        int numClientes = problema.getClientes().size();
        int numInstalaciones = problema.getInstalaciones().size();

        this.instalacionesAbiertas = new boolean[numInstalaciones];
        this.suministros = new double[numClientes][numInstalaciones];
        this.capacidadRestante = new double[numInstalaciones];
        
        for (int j = 0; j < numInstalaciones; j++) {
            this.capacidadRestante[j] = problema.getInstalaciones().get(j).getCapacidad();
        }
        this.costeTotal = 0.0;
    }

    // NUEVO: Constructor de COPIA (Clonador)
    public Solucion(Solucion otra) {
        this.problema = otra.problema;
        this.costeTotal = otra.costeTotal;
        
        int nClientes = problema.getClientes().size();
        int nInstalaciones = problema.getInstalaciones().size();
        
        this.instalacionesAbiertas = otra.instalacionesAbiertas.clone();
        this.capacidadRestante = otra.capacidadRestante.clone();
        
        this.suministros = new double[nClientes][nInstalaciones];
        for (int i = 0; i < nClientes; i++) {
            this.suministros[i] = otra.suministros[i].clone();
        }
    }

    // Getters
    public Problema getProblema() { return problema; }
    public double getCosteTotal() { return costeTotal; }
    public boolean[] getInstalacionesAbiertas() { return instalacionesAbiertas; }
    public double[][] getSuministros() { return suministros; }
    public double[] getCapacidadRestante() { return capacidadRestante; } // ¡Añadido!

    public void añadirSuministro(int clienteId, int instalacionId, double cantidad) {
        if (!instalacionesAbiertas[instalacionId]) {
            instalacionesAbiertas[instalacionId] = true;
            costeTotal += problema.getInstalaciones().get(instalacionId).getCostoFijo();
        }
        suministros[clienteId][instalacionId] += cantidad;
        capacidadRestante[instalacionId] -= cantidad;
        costeTotal += (cantidad * problema.getCostosTransporte()[clienteId][instalacionId]);
    }

    // NUEVO: Método para quitar suministro (útil para Shift y Swap)
    public void quitarSuministro(int clienteId, int instalacionId, double cantidad) {
        suministros[clienteId][instalacionId] -= cantidad;
        capacidadRestante[instalacionId] += cantidad;
        costeTotal -= (cantidad * problema.getCostosTransporte()[clienteId][instalacionId]);

        // Si la instalación se queda vacía, la cerramos y ahorramos el coste fijo
        if (capacidadRestante[instalacionId] == problema.getInstalaciones().get(instalacionId).getCapacidad()) {
            instalacionesAbiertas[instalacionId] = false;
            costeTotal -= problema.getInstalaciones().get(instalacionId).getCostoFijo();
        }
    }
}