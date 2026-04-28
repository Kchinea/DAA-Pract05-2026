package model;

import java.util.BitSet;
import java.util.List;

public class Problema {
    private static final double EPS = 1e-9;

    private List<Cliente> clientes;
    private List<Fundation> instalaciones;
    private double[][] costosTransporte; // Matriz donde la fila es el cliente y la columna la instalación
    private boolean[][] incompatibilidades; // Matriz booleana: true si el cliente i1 es incompatible con i2
    private final BitSet[] incompatiblesCon;

    public Problema(List<Cliente> clientes, List<Fundation> instalaciones, 
                    double[][] costosTransporte, boolean[][] incompatibilidades) {
        this.clientes = clientes;
        this.instalaciones = instalaciones;
        this.costosTransporte = costosTransporte;
        this.incompatibilidades = incompatibilidades;

        int n = clientes.size();
        this.incompatiblesCon = new BitSet[n];
        for (int i = 0; i < n; i++) {
            BitSet bs = new BitSet(n);
            for (int k = 0; k < n; k++) {
                if (incompatibilidades[i][k]) {
                    bs.set(k);
                }
            }
            incompatiblesCon[i] = bs;
        }
    }

    public List<Cliente> getClientes() { return clientes; }
    public List<Fundation> getInstalaciones() { return instalaciones; }
    public double[][] getCostosTransporte() { return costosTransporte; }
    public boolean[][] getIncompatibilidades() { return incompatibilidades; }
    
    public boolean sonIncompatibles(int cliente1, int cliente2) {
        return incompatibilidades[cliente1][cliente2];
    }

    public boolean esCompatible(Solucion sol, int clienteId, int instalacionId) {
        // Ruta rápida: usar bitsets (O(n/word)) en lugar de escanear todos los clientes.
        BitSet presentes = sol.getClientesEnInstalacion(instalacionId);
        if (presentes != null) {
            return !incompatiblesCon[clienteId].intersects(presentes);
        }

        for (int k = 0; k < clientes.size(); k++) {
            if (sol.getSuministros()[k][instalacionId] > EPS && sonIncompatibles(clienteId, k)) {
                return false;
            }
        }
        return true;
    }
}