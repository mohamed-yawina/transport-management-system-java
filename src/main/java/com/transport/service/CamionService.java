package com.transport.service;

import com.transport.ui.admin.GestionCamionsView.Camion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CamionService {
    
    private Connection connection;
    
    public CamionService() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/transport_db", 
                "root", 
                ""
            );
            System.out.println("✅ CamionService: Connexion BD établie");
        } catch (Exception e) {
            System.out.println("❌ CamionService: Connexion BD non établie: " + e.getMessage());
            connection = null;
        }
    }
    
    // Récupérer tous les chauffeurs (utilisateurs avec rôle CHAUFFEUR)
    public List<String> getAllChauffeurs() {
        List<String> chauffeurs = new ArrayList<>();
        
        if (connection == null) {
            // Données simulées
            chauffeurs.add("Chauffeur");
            chauffeurs.add("Morad");
            chauffeurs.add("Ahmed");
            return chauffeurs;
        }
        
        try {
            String sql = "SELECT nom FROM utilisateur WHERE role = 'CHAUFFEUR' ORDER BY nom";
            System.out.println("📝 Récupération des chauffeurs: " + sql);
            
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                chauffeurs.add(rs.getString("nom"));
            }
            
            System.out.println("👤 " + chauffeurs.size() + " chauffeurs trouvés");
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération chauffeurs: " + e.getMessage());
        }
        
        return chauffeurs;
    }
    
    // Récupérer tous les camions
    public List<Camion> getAllCamions() {
        List<Camion> camions = new ArrayList<>();
        
        if (connection == null) {
            System.out.println("⚠️ CamionService: Mode simulé activé");
            camions.add(new Camion(1, "ABC-123", "Volvo FH16", 40.0, "Disponible", 
                                   "Chauffeur", java.sql.Date.valueOf("2024-11-15"), 125450, 32.5));
            camions.add(new Camion(2, "DEF-456", "Mercedes Actros", 35.0, "En maintenance", 
                                   "Morad", java.sql.Date.valueOf("2024-10-20"), 98500, 28.7));
            return camions;
        }
        
        try {
            String sql = "SELECT id, matricule, modele, capacite, etat, chauffeur, " +
                        "derniere_maintenance, kilometrage, consommation FROM camion ORDER BY id";
            System.out.println("📝 CamionService SQL: " + sql);
            
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                Camion camion = new Camion(
                    rs.getInt("id"),
                    rs.getString("matricule"),
                    rs.getString("modele"),
                    rs.getDouble("capacite"),
                    rs.getString("etat"),
                    rs.getString("chauffeur"),
                    rs.getDate("derniere_maintenance"),
                    rs.getInt("kilometrage"),
                    rs.getDouble("consommation")
                );
                camions.add(camion);
            }
            
            System.out.println("📊 CamionService: " + count + " camions chargés");
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des camions: " + e.getMessage());
        }
        
        return camions;
    }
    
    // Ajouter un camion
    public boolean addCamion(Camion camion) {
        if (connection == null) {
            System.out.println("⚠️ CamionService: Ajout simulé: " + camion.getMatricule());
            return true;
        }
        
        try {
            String sql = "INSERT INTO camion (matricule, modele, capacite, etat, chauffeur, " +
                        "derniere_maintenance, kilometrage, consommation) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            System.out.println("📝 CamionService Insert: " + sql);
            System.out.println("📦 Valeurs: " + camion.getMatricule() + ", " + camion.getModele() + ", " + 
                              camion.getCapacite() + ", " + camion.getEtat() + ", " + camion.getChauffeur() + ", " +
                              camion.getDerniereMaintenance() + ", " + camion.getKilometrage() + ", " + camion.getConsommation());
            
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, camion.getMatricule());
            pstmt.setString(2, camion.getModele());
            pstmt.setDouble(3, camion.getCapacite());
            pstmt.setString(4, camion.getEtat());
            pstmt.setString(5, camion.getChauffeur());
            
            // Nouvelles colonnes
            if (camion.getDerniereMaintenance() != null) {
                pstmt.setDate(6, new java.sql.Date(camion.getDerniereMaintenance().getTime()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }
            pstmt.setInt(7, camion.getKilometrage());
            pstmt.setDouble(8, camion.getConsommation());
            
            int rows = pstmt.executeUpdate();
            pstmt.close();
            
            System.out.println("✅ CamionService: " + rows + " ligne(s) insérée(s)");
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout du camion: " + e.getMessage());
            return false;
        }
    }
    
    // Mettre à jour un camion
    public boolean updateCamion(Camion camion) {
        if (connection == null) {
            System.out.println("⚠️ CamionService: Mise à jour simulée: " + camion.getMatricule());
            return true;
        }
        
        try {
            String sql = "UPDATE camion SET matricule = ?, modele = ?, capacite = ?, " +
                        "etat = ?, chauffeur = ?, derniere_maintenance = ?, " +
                        "kilometrage = ?, consommation = ? WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, camion.getMatricule());
            pstmt.setString(2, camion.getModele());
            pstmt.setDouble(3, camion.getCapacite());
            pstmt.setString(4, camion.getEtat());
            pstmt.setString(5, camion.getChauffeur());
            
            // Nouvelles colonnes
            if (camion.getDerniereMaintenance() != null) {
                pstmt.setDate(6, new java.sql.Date(camion.getDerniereMaintenance().getTime()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }
            pstmt.setInt(7, camion.getKilometrage());
            pstmt.setDouble(8, camion.getConsommation());
            pstmt.setInt(9, camion.getId());
            
            int rows = pstmt.executeUpdate();
            pstmt.close();
            
            System.out.println("✅ CamionService: " + rows + " ligne(s) mise(s) à jour");
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour du camion: " + e.getMessage());
            return false;
        }
    }
    
    // Supprimer un camion
    public boolean deleteCamion(int id) {
        if (connection == null) {
            System.out.println("⚠️ CamionService: Suppression simulée pour ID: " + id);
            return true;
        }
        
        try {
            String sql = "DELETE FROM camion WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            
            int rows = pstmt.executeUpdate();
            pstmt.close();
            
            System.out.println("✅ CamionService: " + rows + " ligne(s) supprimée(s)");
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression du camion: " + e.getMessage());
            return false;
        }
    }
    
    // Vérifier si le matricule existe déjà
    public boolean matriculeExists(String matricule) {
        if (connection == null) {
            System.out.println("⚠️ CamionService: Vérification matricule simulée");
            return false;
        }
        
        try {
            String sql = "SELECT COUNT(*) FROM camion WHERE matricule = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, matricule);
            
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            
            rs.close();
            pstmt.close();
            
            System.out.println("🔍 CamionService: Matricule '" + matricule + "' existe? " + (count > 0));
            return count > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification du matricule: " + e.getMessage());
            return false;
        }
    }
    
    // Vérifier si le matricule existe déjà (sauf pour un ID donné)
    public boolean matriculeExists(String matricule, int excludeId) {
        if (connection == null) return false;
        
        try {
            String sql = "SELECT COUNT(*) FROM camion WHERE matricule = ? AND id != ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, matricule);
            pstmt.setInt(2, excludeId);
            
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            
            rs.close();
            pstmt.close();
            
            return count > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification matricule: " + e.getMessage());
            return false;
        }
    }
    
    // Rechercher des camions
    public List<Camion> searchCamions(String searchText, String etatFilter) {
        List<Camion> camions = new ArrayList<>();
        
        if (connection == null) return camions;
        
        try {
            StringBuilder sql = new StringBuilder(
                "SELECT id, matricule, modele, capacite, etat, chauffeur, " +
                "derniere_maintenance, kilometrage, consommation FROM camion WHERE 1=1"
            );
            
            if (searchText != null && !searchText.isEmpty()) {
                sql.append(" AND (matricule LIKE ? OR modele LIKE ? OR chauffeur LIKE ?)");
            }
            
            if (etatFilter != null && !etatFilter.equals("Tous")) {
                sql.append(" AND etat = ?");
            }
            
            sql.append(" ORDER BY id");
            
            System.out.println("🔍 Recherche camions: " + sql);
            
            PreparedStatement pstmt = connection.prepareStatement(sql.toString());
            
            int paramIndex = 1;
            if (searchText != null && !searchText.isEmpty()) {
                String searchPattern = "%" + searchText + "%";
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
            }
            
            if (etatFilter != null && !etatFilter.equals("Tous")) {
                pstmt.setString(paramIndex++, etatFilter);
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Camion camion = new Camion(
                    rs.getInt("id"),
                    rs.getString("matricule"),
                    rs.getString("modele"),
                    rs.getDouble("capacite"),
                    rs.getString("etat"),
                    rs.getString("chauffeur"),
                    rs.getDate("derniere_maintenance"),
                    rs.getInt("kilometrage"),
                    rs.getDouble("consommation")
                );
                camions.add(camion);
            }
            
            System.out.println("📊 Résultats recherche: " + camions.size() + " camions");
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche: " + e.getMessage());
        }
        
        return camions;
    }
    
    // Statistiques
    public int getTotalCamions() {
        if (connection == null) return 0;
        
        try {
            String sql = "SELECT COUNT(*) FROM camion";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            int total = rs.getInt(1);
            
            rs.close();
            pstmt.close();
            
            return total;
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage total: " + e.getMessage());
            return 0;
        }
    }
    
    public int countByEtat(String etat) {
        if (connection == null) return 0;
        
        try {
            String sql = "SELECT COUNT(*) FROM camion WHERE etat = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, etat);
            
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            
            rs.close();
            pstmt.close();
            
            return count;
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage par état: " + e.getMessage());
            return 0;
        }
    }
    
    public double getTotalCapacity() {
        if (connection == null) return 0;
        
        try {
            String sql = "SELECT SUM(capacite) FROM camion";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            double total = rs.getDouble(1);
            
            rs.close();
            pstmt.close();
            
            return total;
        } catch (SQLException e) {
            System.err.println("❌ Erreur calcul capacité totale: " + e.getMessage());
            return 0;
        }
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 CamionService: Connexion fermée");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur fermeture connexion: " + e.getMessage());
        }
    }
}