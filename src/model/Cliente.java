package model;

public class Cliente {
    private int id;
    private double demanda;

    // Constructor: Se ejecuta al crear un "new Cliente(...)"
    public Cliente(int id, double demanda) {
        this.id = id;
        this.demanda = demanda;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getDemanda() {
        return demanda;
    }

    public void setDemanda(double demanda) {
        this.demanda = demanda;
    }
}