package com.transport.model;

import java.time.LocalDate;

public class Facture {
    private int id;
    private int idLivraison;
    private String numeroFacture;
    private double montantTotal;
    private double tva;
    private double montantTTC;
    private LocalDate dateFacture;
    private LocalDate dateEcheance;
    private String statusPaiement;
    
    // Constructeurs
    public Facture() {}
    
    public Facture(int idLivraison, String numeroFacture, double montantTotal, 
                  double tva, double montantTTC, LocalDate dateFacture, 
                  LocalDate dateEcheance, String statusPaiement) {
        this.idLivraison = idLivraison;
        this.numeroFacture = numeroFacture;
        this.montantTotal = montantTotal;
        this.tva = tva;
        this.montantTTC = montantTTC;
        this.dateFacture = dateFacture;
        this.dateEcheance = dateEcheance;
    }
    
    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getIdLivraison() { return idLivraison; }
    public void setIdLivraison(int idLivraison) { this.idLivraison = idLivraison; }
    
    public String getNumeroFacture() { return numeroFacture; }
    public void setNumeroFacture(String numeroFacture) { this.numeroFacture = numeroFacture; }
    
    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double montantTotal) { this.montantTotal = montantTotal; }
    
    public double getTva() { return tva; }
    public void setTva(double tva) { this.tva = tva; }
    
    public double getMontantTTC() { return montantTTC; }
    public void setMontantTTC(double montantTTC) { this.montantTTC = montantTTC; }
    
    public LocalDate getDateFacture() { return dateFacture; }
    public void setDateFacture(LocalDate dateFacture) { this.dateFacture = dateFacture; }
    
    public LocalDate getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(LocalDate dateEcheance) { this.dateEcheance = dateEcheance; }
    
}