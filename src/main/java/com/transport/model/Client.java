package com.transport.model;

public class Client extends Utilisateur {

    public Client(int id, String nom, String email, String motDePasse) {
        super(id, nom, email, motDePasse, "CLIENT");
    }
}
