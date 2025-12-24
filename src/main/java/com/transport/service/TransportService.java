package com.transport.service;

import com.transport.model.Livraison;

public class TransportService {

    public Livraison creerDemandeTransport(String destination) {
        return new Livraison(1, destination, "EN_ATTENTE");
    }

    public void suivreTransport(Livraison livraison) {
        System.out.println("Statut : " + livraison.getStatus());
    }
}
