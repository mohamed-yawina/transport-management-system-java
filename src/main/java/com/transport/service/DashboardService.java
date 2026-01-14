package com.transport.service;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DashboardService {
    
    private Connection connection;
    
    public DashboardService() {
        try {
            // Essayer de se connecter, mais continuer même si ça échoue
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/transport_db", 
                "root", 
                ""
            );
        } catch (Exception e) {
            System.out.println("⚠️ Connexion BD non établie, utilisation des données simulées");
            connection = null;
        }
    }
    
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            if (connection != null && !connection.isClosed()) {
                // Utiliser try-catch pour chaque requête au cas où une table n'existe pas
                stats.put("totalUsers", safeCountFromTable("utilisateur"));
                stats.put("totalCamions", safeCountFromTable("camion"));
                stats.put("totalLivraisons", safeCountFromTable("livraison"));
                stats.put("totalRevenus", safeCalculateTotalRevenue());
            } else {
                // Valeurs par défaut si pas de connexion
                setDefaultStats(stats);
            }
        } catch (Exception e) {
            System.err.println("Erreur dans getDashboardStatistics: " + e.getMessage());
            setDefaultStats(stats);
        }
        
        return stats;
    }
    
    private int safeCountFromTable(String tableName) {
        try {
            String sql = "SELECT COUNT(*) FROM " + tableName;
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.out.println("Table " + tableName + " non trouvée, valeur par défaut: 0");
            return 0;
        }
    }
    
    private double safeCalculateTotalRevenue() {
        try {
            String sql = "SELECT SUM(montant) FROM facture";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        } catch (SQLException e) {
            System.out.println("Table facture non trouvée, revenus par défaut: 0");
            return 0.0;
        }
    }
    
    private void setDefaultStats(Map<String, Object> stats) {
        stats.put("totalUsers", 42);
        stats.put("totalCamions", 18);
        stats.put("totalLivraisons", 156);
        stats.put("totalRevenus", 24500);
        stats.put("camionsDisponibles", 12);
        stats.put("livraisonsEnCours", 8);
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
        }
    }
}