package com.transport.service;

import com.transport.exception.TransportException;
import com.transport.model.Facture;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FactureService {

    private List<Facture> factures = new ArrayList<>();

    // CREATE
    public void ajouterFacture(Facture facture) {
        factures.add(facture);
    }

    // READ (par client)
    public List<Facture> getFacturesParClient(int clientId) {
        List<Facture> result = new ArrayList<>();
        for (Facture f : factures) {
            if (f.getClientId() == clientId) {
                result.add(f);
            }
        }
        return result;
    }

    // DELETE
    public void supprimerFacture(int id) {
        factures.removeIf(f -> f.getId() == id);
    }

    // LIST
    public void afficherFactures() {
        factures.forEach(f ->
                System.out.println("Facture " + f.getId() + " : " + f.getMontant())
        );
    }
    public double calculerChiffreAffaires() {
    return factures.stream()
            .mapToDouble(Facture::getMontant)
            .sum();
}
  public double calculerMoyenneFactures() {
    return factures.stream()
            .mapToDouble(Facture::getMontant)
            .average()
            .orElse(0.0);
}
   public Facture factureMax() {
    return factures.stream()
            .max((f1, f2) -> Double.compare(f1.getMontant(), f2.getMontant()))
            .orElse(null);
}
  public List<Facture> getFacturesClientStream(int clientId) {
    return factures.stream()
            .filter(f -> f.getClientId() == clientId)
            .toList();
}
public Map<Integer, Double> totalParClient() {
    return factures.stream()
            .collect(Collectors.groupingBy(
                    Facture::getClientId,
                    Collectors.summingDouble(Facture::getMontant)
            ));
}

    // Persistence using Java object streams (flux E/S)
    public void sauvegarderFactures(String chemin) throws TransportException {
        try {
            File parent = new File(chemin).getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(chemin))) {
                oos.writeObject(factures);
            }
        } catch (IOException e) {
            throw new TransportException("Erreur lors de la sauvegarde des factures : " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void chargerFactures(String chemin) throws TransportException {
        File f = new File(chemin);
        if (!f.exists()) {
            this.factures = new ArrayList<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(chemin))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                this.factures = (List<Facture>) obj;
            } else {
                throw new TransportException("Fichier de factures invalide");
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new TransportException("Erreur lors du chargement des factures : " + e.getMessage());
        }
    }


}
