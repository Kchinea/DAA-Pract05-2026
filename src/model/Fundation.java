package model;

public class Fundation {
    private int id;
    private double costoFijo;
    private double capacidad;

    public Fundation(int id, double costoFijo, double capacidad) {
        this.id = id;
        this.costoFijo = costoFijo;
        this.capacidad = capacidad;
    }

    public int getId() { return id; }
    public double getCostoFijo() { return costoFijo; }
    public double getCapacidad() { return capacidad; }

    public void setId(int id) { this.id = id; }
    public void setCostoFijo(double costoFijo) { this.costoFijo = costoFijo; }
    public void setCapacidad(double capacidad) { this.capacidad = capacidad; }
}