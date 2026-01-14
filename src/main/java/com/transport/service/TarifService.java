package com.transport.service;

import com.transport.model.Tarif;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TarifService {
    
    private Connection connection;
    private String url = "jdbc:mysql://localhost:3306/transport_db";
    private String user = "root";
    private String password = ""; // À adapter si nécessaire
    
    public TarifService() {
        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connexion à la BD réussie pour TarifService");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur de connexion à la base de données pour TarifService: " + e.getMessage());
        }
    }
    
    public List<Tarif> getAllTarifs() {
        List<Tarif> tarifs = new ArrayList<>();
        
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            // REQUÊTE CORRIGÉE : Utiliser les bons noms de colonnes
            String query = "SELECT id, type, prix_par_km, prix_par_ton, actif FROM tarifs ORDER BY type";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                Tarif tarif = new Tarif(
                    rs.getInt("id"),
                    rs.getString("type"),
                    rs.getDouble("prix_par_km"),
                    rs.getDouble("prix_par_ton"),
                    rs.getBoolean("actif")
                );
                tarifs.add(tarif);
                System.out.println("📦 Tarif chargé: " + tarif.getType() + " - " + 
                    tarif.getPrixParKm() + "€/km, " + tarif.getPrixParTon() + "€/tonne" +
                    " (actif: " + tarif.isActif() + ")");
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur lors de la récupération des tarifs: " + e.getMessage());
            tarifs = getTarifsDemo();
        }
        
        return tarifs;
    }
    
    public Tarif getTarifByType(String type) {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String query = "SELECT id, type, prix_par_km, prix_par_ton, actif FROM tarifs WHERE type = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, type);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Tarif tarif = new Tarif(
                    rs.getInt("id"),
                    rs.getString("type"),
                    rs.getDouble("prix_par_km"),
                    rs.getDouble("prix_par_ton"),
                    rs.getBoolean("actif")
                );
                System.out.println("🔍 Tarif trouvé: " + tarif);
                rs.close();
                pstmt.close();
                return tarif;
            }
            
            rs.close();
            pstmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur lors de la recherche du tarif: " + e.getMessage());
        }
        
        // Tarif par défaut si non trouvé
        return new Tarif(type, 1.50, 25.00, true);
    }
    
    public Tarif getTarifById(int id) {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String query = "SELECT id, type, prix_par_km, prix_par_ton, actif FROM tarifs WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Tarif tarif = new Tarif(
                    rs.getInt("id"),
                    rs.getString("type"),
                    rs.getDouble("prix_par_km"),
                    rs.getDouble("prix_par_ton"),
                    rs.getBoolean("actif")
                );
                rs.close();
                pstmt.close();
                return tarif;
            }
            
            rs.close();
            pstmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur lors de la recherche du tarif par ID: " + e.getMessage());
        }
        
        return null;
    }
    
    // Méthodes pour les données de démonstration
    private List<Tarif> getTarifsDemo() {
        List<Tarif> tarifsDemo = new ArrayList<>();
        
        // Tarifs basés sur votre table réelle
        tarifsDemo.add(new Tarif(1, "Standard", 1.50, 25.00, true));
        tarifsDemo.add(new Tarif(2, "Express", 2.20, 35.00, true));
        tarifsDemo.add(new Tarif(3, "Fragile", 1.80, 30.00, true));
        tarifsDemo.add(new Tarif(6, "Refrigéré", 2.00, 45.00, true));
        tarifsDemo.add(new Tarif(7, "Dangereux", 3.00, 55.00, false));
        tarifsDemo.add(new Tarif(8, "Volume", 1.30, 22.00, true));
        
        System.out.println("⚠️ Utilisation des tarifs de démonstration");
        return tarifsDemo;
    }
    
    // Méthodes CRUD pour GestionTarifsView
    
    public boolean addTarif(Tarif tarif) {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String query = "INSERT INTO tarifs (type, prix_par_km, prix_par_ton, actif) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, tarif.getType());
            pstmt.setDouble(2, tarif.getPrixParKm());
            pstmt.setDouble(3, tarif.getPrixParTon());
            pstmt.setBoolean(4, tarif.isActif());
            
            int rows = pstmt.executeUpdate();
            pstmt.close();
            
            System.out.println("✅ Tarif ajouté: " + tarif.getType());
            return rows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur lors de l'ajout du tarif: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateTarif(Tarif tarif) {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String query = "UPDATE tarifs SET type = ?, prix_par_km = ?, prix_par_ton = ?, actif = ? WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, tarif.getType());
            pstmt.setDouble(2, tarif.getPrixParKm());
            pstmt.setDouble(3, tarif.getPrixParTon());
            pstmt.setBoolean(4, tarif.isActif());
            pstmt.setInt(5, tarif.getId());
            
            int rows = pstmt.executeUpdate();
            pstmt.close();
            
            System.out.println("✅ Tarif mis à jour: " + tarif.getType());
            return rows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur lors de la mise à jour du tarif: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteTarif(int id) {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String query = "DELETE FROM tarifs WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, id);
            
            int rows = pstmt.executeUpdate();
            pstmt.close();
            
            System.out.println("✅ Tarif supprimé (ID: " + id + ")");
            return rows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur lors de la suppression du tarif: " + e.getMessage());
            return false;
        }
    }
    
    public boolean applyIncrease(double percentage) {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String query = "UPDATE tarifs SET prix_par_km = prix_par_km * (1 + ?/100), prix_par_ton = prix_par_ton * (1 + ?/100) WHERE actif = 1";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setDouble(1, percentage);
            pstmt.setDouble(2, percentage);
            
            int rows = pstmt.executeUpdate();
            pstmt.close();
            
            System.out.println("✅ Augmentation de " + percentage + "% appliquée à " + rows + " tarifs actifs");
            return rows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur lors de l'application de l'augmentation: " + e.getMessage());
            return false;
        }
    }
    
    public List<Tarif> getActiveTarifs() {
        List<Tarif> tarifs = new ArrayList<>();
        
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String query = "SELECT id, type, prix_par_km, prix_par_ton, actif FROM tarifs WHERE actif = 1 ORDER BY type";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                Tarif tarif = new Tarif(
                    rs.getInt("id"),
                    rs.getString("type"),
                    rs.getDouble("prix_par_km"),
                    rs.getDouble("prix_par_ton"),
                    rs.getBoolean("actif")
                );
                tarifs.add(tarif);
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur lors de la récupération des tarifs actifs: " + e.getMessage());
            // Retourner seulement les tarifs actifs de démo
            tarifs = getTarifsDemo().stream()
                    .filter(Tarif::isActif)
                    .collect(java.util.stream.Collectors.toList());
        }
        
        return tarifs;
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Connexion TarifService fermée");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}