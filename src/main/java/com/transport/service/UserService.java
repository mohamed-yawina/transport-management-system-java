package com.transport.service;

import com.transport.model.Utilisateur;
import com.transport.exception.TransportException;

import java.util.HashMap;
import java.util.Map;

public class UserService {

    private Map<Integer, Utilisateur> utilisateurs = new HashMap<>();

    // CREATE
    public void ajouterUtilisateur(Utilisateur user) throws TransportException {
        if (utilisateurs.containsKey(user.getId())) {
            throw new TransportException("Utilisateur déjà existant");
        }
        utilisateurs.put(user.getId(), user);
    }

    // READ
    public Utilisateur getUtilisateur(int id) throws TransportException {
        if (!utilisateurs.containsKey(id)) {
            throw new TransportException("Utilisateur introuvable");
        }
        return utilisateurs.get(id);
    }

    // UPDATE
    public void modifierUtilisateur(int id, String nouveauNom) throws TransportException {
        Utilisateur user = getUtilisateur(id);
        user.setNom(nouveauNom);
    }

    // DELETE
    public void supprimerUtilisateur(int id) throws TransportException {
        if (!utilisateurs.containsKey(id)) {
            throw new TransportException("Suppression impossible : utilisateur inexistant");
        }
        utilisateurs.remove(id);
    }

    // LIST
    public void afficherUtilisateurs() {
        utilisateurs.values().forEach(u ->
                System.out.println(u.getId() + " - " + u.getNom())
        );
    }
    public void afficherUtilisateursParRole(String role) {
    utilisateurs.values().stream()
            .filter(u -> u.getRole().equalsIgnoreCase(role))
            .forEach(u -> System.out.println(u.getId() + " - " + u.getNom()));
}
   public void trierUtilisateursParNom() {
    utilisateurs.values().stream()
            .sorted((u1, u2) -> u1.getNom().compareToIgnoreCase(u2.getNom()))
            .forEach(u -> System.out.println(u.getNom()));
}
   public long compterUtilisateursParRole(String role) {
    return utilisateurs.values().stream()
            .filter(u -> u.getRole().equalsIgnoreCase(role))
            .count();
}

}
