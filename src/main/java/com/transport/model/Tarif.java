package com.transport.model;

public class Tarif {
    private int id;
    private String type;
    private double prixParKm;
    private double prixParTon;
    private boolean actif;
    
    // Constructeurs
    public Tarif() {
    }
    
    public Tarif(String type, double prixParKm, double prixParTon, boolean actif) {
        this.type = type;
        this.prixParKm = prixParKm;
        this.prixParTon = prixParTon;
        this.actif = actif;
    }
    
    public Tarif(int id, String type, double prixParKm, double prixParTon, boolean actif) {
        this.id = id;
        this.type = type;
        this.prixParKm = prixParKm;
        this.prixParTon = prixParTon;
        this.actif = actif;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public double getPrixParKm() {
        return prixParKm;
    }
    
    public void setPrixParKm(double prixParKm) {
        this.prixParKm = prixParKm;
    }
    
    public double getPrixParTon() {
        return prixParTon;
    }
    
    public void setPrixParTon(double prixParTon) {
        this.prixParTon = prixParTon;
    }
    
    public boolean isActif() {
        return actif;
    }
    
    public void setActif(boolean actif) {
        this.actif = actif;
    }
    
    @Override
    public String toString() {
        return String.format("Tarif[%s] : %.2f€/km + %.2f€/t", type, prixParKm, prixParTon);
    }
}