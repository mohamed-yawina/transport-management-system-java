package com.transport.model;

public class Chauffeur extends Utilisateur {

    public Chauffeur(int id, String nom, String email, String motDePasse) {
        super(id, nom, email, motDePasse, "CHAUFFEUR");
    }
}
