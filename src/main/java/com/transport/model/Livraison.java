package com.transport.model;

import java.time.LocalDate;

public class Livraison {
    private int id;
    private String nomClient;
    private String destination;
    private String typeTransport;
    private double distanceKm;
    private double poidsTonnes;
    private double prixTotal;
    private String status;
    private LocalDate dateCreation;
    private LocalDate dateModification;
    private int idChauffeur;
    private String chauffeurNom;
    
    // Constructeurs
    public Livraison() {}
    
    public Livraison(int id, int idClient, String nomClient, String destination, 
                    String typeTransport, double distanceKm, double poidsTonnes, 
                    double prixTotal, String status, LocalDate dateCreation, 
                    LocalDate dateModification, int idChauffeur, String chauffeurNom) {
        this.id = id;
        this.nomClient = nomClient;
        this.destination = destination;
        this.typeTransport = typeTransport;
        this.distanceKm = distanceKm;
        this.poidsTonnes = poidsTonnes;
        this.prixTotal = prixTotal;
        this.status = status;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
        this.idChauffeur = idChauffeur;
        this.chauffeurNom = chauffeurNom;
    }
    
    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }
    
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    
    public String getTypeTransport() { return typeTransport; }
    public void setTypeTransport(String typeTransport) { this.typeTransport = typeTransport; }
    
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    
    public double getPoidsTonnes() { return poidsTonnes; }
    public void setPoidsTonnes(double poidsTonnes) { this.poidsTonnes = poidsTonnes; }
    
    public double getPrixTotal() { return prixTotal; }
    public void setPrixTotal(double prixTotal) { this.prixTotal = prixTotal; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDate getDateModification() { return dateModification; }
    public void setDateModification(LocalDate dateModification) { this.dateModification = dateModification; }
    
    public int getIdChauffeur() { return idChauffeur; }
    public void setIdChauffeur(int idChauffeur) { this.idChauffeur = idChauffeur; }
    
    public String getChauffeurNom() { return chauffeurNom; }
    public void setChauffeurNom(String chauffeurNom) { this.chauffeurNom = chauffeurNom; }
    
    @Override
    public String toString() {
        return "Livraison{" +
               "id=" + id +
               ", nomClient='" + nomClient + '\'' +
               ", destination='" + destination + '\'' +
               ", typeTransport='" + typeTransport + '\'' +
               ", distanceKm=" + distanceKm +
               ", poidsTonnes=" + poidsTonnes +
               ", prixTotal=" + prixTotal +
               ", status='" + status + '\'' +
               ", dateCreation=" + dateCreation +
               ", dateModification=" + dateModification +
               ", idChauffeur=" + idChauffeur +
               ", chauffeurNom='" + chauffeurNom + '\'' +
               '}';
    }
}