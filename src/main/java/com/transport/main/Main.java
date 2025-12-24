package com.transport.main;

import com.transport.model.*;
import com.transport.service.*;

public class Main {

    public static void main(String[] args) {

        try {
            UserService userService = new UserService();
            LivraisonService livraisonService = new LivraisonService();
            FactureService factureService = new FactureService();

            Client client = new Client(1, "Ahmed", "ahmed@mail.com", "1234");
            userService.ajouterUtilisateur(client);

            Livraison livraison = new Livraison(1, "Rabat", "EN_ATTENTE");
            livraisonService.ajouterLivraison(livraison);

            // Test sauvegarde / chargement des factures (flux E/S)
            factureService.ajouterFacture(new Facture(1, 250.0, client.getId(), new java.util.Date()));
            factureService.sauvegarderFactures("data/factures.dat");

            FactureService factureService2 = new FactureService();
            factureService2.chargerFactures("data/factures.dat");
            factureService2.afficherFactures();

            livraisonService.mettreAJourStatut(1, "LIVRÉ");

            userService.afficherUtilisateurs();
            livraisonService.afficherLivraisons();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
