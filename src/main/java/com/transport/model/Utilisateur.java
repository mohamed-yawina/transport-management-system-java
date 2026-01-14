package com.transport.model;

public class Utilisateur {
    private int id;
    private String nom;
    private String email;
    private String motdepasse; // Notez: motdepasse sans underscore
    private String role;
    
    // Constructeurs
    public Utilisateur() {}
    
    public Utilisateur(int id, String nom, String email, String motdepasse, String role) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.motdepasse = motdepasse;
        this.role = role;
    }
    
    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getMotdepasse() { return motdepasse; } // motdepasse sans underscore
    public void setMotdepasse(String motdepasse) { this.motdepasse = motdepasse; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    @Override
    public String toString() {
        return nom + " (" + email + ") - " + role;
    }
}