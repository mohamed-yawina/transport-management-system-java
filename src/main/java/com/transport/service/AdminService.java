package com.transport.service;

import com.transport.model.Facture;
import java.util.Date;

public class AdminService {

    public Facture preparerFacture(int clientId, double montant) {
        return new Facture(1, montant, clientId, new Date());
    }

    public void gererUtilisateurs() {
        System.out.println("Gestion des utilisateurs");
    }
}
