package com.transport.model;

public class Administrateur extends Utilisateur {

    public Administrateur(int id, String nom, String email, String motDePasse) {
        super(id, nom, email, motDePasse, "ADMIN");
    }
}
