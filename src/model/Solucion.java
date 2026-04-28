package model;

import java.util.BitSet;

public class Solucion {
    private static final double EPS = 1e-9;

    private Problema problema; 
    private boolean[] instalacionesAbiertas; 
    private double[][] suministros;          
    private double[] capacidadRestante;      
    private double costeTotal;
    private BitSet[] clientesEnInstalacion;

    public Solucion(Problema problema) {
        this.problema = problema;
        int numClientes = problema.getClientes().size();
        int numInstalaciones = problema.getInstalaciones().size();

        this.instalacionesAbiertas = new boolean[numInstalaciones];
        this.suministros = new double[numClientes][numInstalaciones];
        this.capacidadRestante = new double[numInstalaciones];
        this.clientesEnInstalacion = new BitSet[numInstalaciones];
        
        for (int j = 0; j < numInstalaciones; j++) {
            this.capacidadRestante[j] = problema.getInstalaciones().get(j).getCapacidad();
            this.clientesEnInstalacion[j] = new BitSet(numClientes);
        }
        this.costeTotal = 0.0;
    }

    public Solucion(Solucion otra) {
        this.problema = otra.problema;
        this.costeTotal = otra.costeTotal;
        
        int nClientes = problema.getClientes().size();
        int nInstalaciones = problema.getInstalaciones().size();
        
        this.instalacionesAbiertas = otra.instalacionesAbiertas.clone();
        this.capacidadRestante = otra.capacidadRestante.clone();
        this.clientesEnInstalacion = new BitSet[nInstalaciones];
        for (int j = 0; j < nInstalaciones; j++) {
            this.clientesEnInstalacion[j] = (BitSet) otra.clientesEnInstalacion[j].clone();
        }
        
        this.suministros = new double[nClientes][nInstalaciones];
        for (int i = 0; i < nClientes; i++) {
            this.suministros[i] = otra.suministros[i].clone();
        }
    }

    public Problema getProblema() { return problema; }
    public double getCosteTotal() { return costeTotal; }
    public boolean[] getInstalacionesAbiertas() { return instalacionesAbiertas; }
    public double[][] getSuministros() { return suministros; }
    public double[] getCapacidadRestante() { return capacidadRestante; } // ¡Añadido!
    public BitSet getClientesEnInstalacion(int instalacionId) { return clientesEnInstalacion[instalacionId]; }

    public void añadirSuministro(int clienteId, int instalacionId, double cantidad) {
        double before = suministros[clienteId][instalacionId];
        if (!instalacionesAbiertas[instalacionId]) {
            instalacionesAbiertas[instalacionId] = true;
            costeTotal += problema.getInstalaciones().get(instalacionId).getCostoFijo();
        }
        suministros[clienteId][instalacionId] += cantidad;
        double after = suministros[clienteId][instalacionId];
        if (before <= EPS && after > EPS) {
            clientesEnInstalacion[instalacionId].set(clienteId);
        }
        capacidadRestante[instalacionId] -= cantidad;
        costeTotal += (cantidad * problema.getCostosTransporte()[clienteId][instalacionId]);
    }

    public void quitarSuministro(int clienteId, int instalacionId, double cantidad) {
        double before = suministros[clienteId][instalacionId];
        suministros[clienteId][instalacionId] -= cantidad;
        if (suministros[clienteId][instalacionId] <= EPS) {
            suministros[clienteId][instalacionId] = 0.0;
        }
        double after = suministros[clienteId][instalacionId];
        if (before > EPS && after <= EPS) {
            clientesEnInstalacion[instalacionId].clear(clienteId);
        }
        capacidadRestante[instalacionId] += cantidad;
        costeTotal -= (cantidad * problema.getCostosTransporte()[clienteId][instalacionId]);

        double capacidadTotal = problema.getInstalaciones().get(instalacionId).getCapacidad();
        if (capacidadRestante[instalacionId] >= capacidadTotal - EPS) {
            capacidadRestante[instalacionId] = capacidadTotal;
            instalacionesAbiertas[instalacionId] = false;
            costeTotal -= problema.getInstalaciones().get(instalacionId).getCostoFijo();
            clientesEnInstalacion[instalacionId].clear();
        }
    }
}