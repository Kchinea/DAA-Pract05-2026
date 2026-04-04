package model;

import java.util.List;

public class Problema {
    private List<Cliente> clientes;
    private List<Fundation> instalaciones;
    private double[][] costosTransporte; // Matriz donde la fila es el cliente y la columna la instalación
    private boolean[][] incompatibilidades; // Matriz booleana: true si el cliente i1 es incompatible con i2

    public Problema(List<Cliente> clientes, List<Fundation> instalaciones, 
                    double[][] costosTransporte, boolean[][] incompatibilidades) {
        this.clientes = clientes;
        this.instalaciones = instalaciones;
        this.costosTransporte = costosTransporte;
        this.incompatibilidades = incompatibilidades;
    }

    // Getters
    public List<Cliente> getClientes() { return clientes; }
    public List<Fundation> getInstalaciones() { return instalaciones; }
    public double[][] getCostosTransporte() { return costosTransporte; }
    public boolean[][] getIncompatibilidades() { return incompatibilidades; }
    
    // Método de ayuda para comprobar rápido si dos clientes son incompatibles
    public boolean sonIncompatibles(int cliente1, int cliente2) {
        return incompatibilidades[cliente1][cliente2];
    }
}