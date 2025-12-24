package com.transport.service;

import com.transport.model.Livraison;
import com.transport.exception.TransportException;

import java.util.HashMap;
import java.util.Map;

public class LivraisonService {

    private Map<Integer, Livraison> livraisons = new HashMap<>();

    // CREATE
    public void ajouterLivraison(Livraison livraison) {
        livraisons.put(livraison.getId(), livraison);
    }

    // READ
    public Livraison getLivraison(int id) throws TransportException {
        if (!livraisons.containsKey(id)) {
            throw new TransportException("Livraison non trouvée");
        }
        return livraisons.get(id);
    }

    // UPDATE
    public void mettreAJourStatut(int id, String nouveauStatut) throws TransportException {
        Livraison livraison = getLivraison(id);
        livraison.setStatus(nouveauStatut);
    }

    // DELETE
    public void supprimerLivraison(int id) {
        livraisons.remove(id);
    }

    // LIST
    public void afficherLivraisons() {
        livraisons.values().forEach(l ->
                System.out.println(l.getId() + " -> " + l.getDestination() + " (" + l.getStatus() + ")")
        );
    }
    public void afficherLivraisonsParStatut(String statut) {
    livraisons.values().stream()
            .filter(l -> l.getStatus().equalsIgnoreCase(statut))
            .forEach(l -> System.out.println(l.getDestination()));
}
  public void trierLivraisonsParDestination() {
    livraisons.values().stream()
            .sorted((l1, l2) -> l1.getDestination()
                    .compareToIgnoreCase(l2.getDestination()))
            .forEach(l -> System.out.println(l.getDestination()));
}
  public boolean existeLivraisonEnAttente() {
    return livraisons.values().stream()
            .anyMatch(l -> l.getStatus().equalsIgnoreCase("EN_ATTENTE"));
}

}
