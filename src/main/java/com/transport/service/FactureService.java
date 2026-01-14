package com.transport.service;

import com.transport.model.Facture;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FactureService {
    
    private Connection connection;
    private String url = "jdbc:mysql://localhost:3306/transport_db";
    private String user = "root";
    private String password = "";
    
    public FactureService() {
        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("✅ FactureService: Connexion BD établie");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ FactureService: Erreur de connexion BD: " + e.getMessage());
        }
    }
    
    /**
     * Génère un numéro de facture unique
     */
    private String genererNumeroFacture() {
        try {
            String prefixe = "FACT-" + LocalDate.now().getYear() + "-";
            String query = "SELECT MAX(numero_facture) FROM facture WHERE numero_facture LIKE ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, prefixe + "%");
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next() && rs.getString(1) != null) {
                String dernierNumero = rs.getString(1);
                int dernierNum = Integer.parseInt(dernierNumero.substring(dernierNumero.lastIndexOf("-") + 1));
                return prefixe + String.format("%06d", dernierNum + 1);
            }
            
            rs.close();
            pstmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Si pas de facture existante
        return "FACT-" + LocalDate.now().getYear() + "-000001";
    }
    
    /**
     * Crée une nouvelle facture
     */
    public boolean genererFacture(int idLivraison, double montantTotal, double tva, double montantTTC) {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            // Générer le numéro de facture
            String numeroFacture = genererNumeroFacture();
            
            // Date d'échéance (30 jours après la facturation)
            LocalDate dateFacture = LocalDate.now();
            LocalDate dateEcheance = dateFacture.plusDays(30);
            
            // SUPPRIMER status_paiement de la requête
            String query = "INSERT INTO facture (" +
                         "id_livraison, numero_facture, montant_total, tva, " +
                         "montant_ttc, date_facture, date_echeance" +
                         ") VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, idLivraison);
            pstmt.setString(2, numeroFacture);
            pstmt.setDouble(3, montantTotal);
            pstmt.setDouble(4, tva);
            pstmt.setDouble(5, montantTTC);
            pstmt.setDate(6, Date.valueOf(dateFacture));
            pstmt.setDate(7, Date.valueOf(dateEcheance));
            // SUPPRIMER: pstmt.setString(8, "En attente");
            
            int rows = pstmt.executeUpdate();
            pstmt.close();
            
            if (rows > 0) {
                System.out.println("✅ Facture générée avec succès:");
                System.out.println("   Numéro: " + numeroFacture);
                System.out.println("   ID Livraison: " + idLivraison);
                System.out.println("   Montant TTC: " + montantTTC + " €");
                System.out.println("   Date échéance: " + dateEcheance);
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur création facture: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Récupère une facture par ID de livraison
     */
    public Facture getFactureByLivraisonId(int idLivraison) {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String query = "SELECT * FROM facture WHERE id_livraison = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, idLivraison);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Facture facture = new Facture();
                facture.setId(rs.getInt("id"));
                facture.setIdLivraison(rs.getInt("id_livraison"));
                facture.setNumeroFacture(rs.getString("numero_facture"));
                facture.setMontantTotal(rs.getDouble("montant_total"));
                facture.setTva(rs.getDouble("tva"));
                facture.setMontantTTC(rs.getDouble("montant_ttc"));
                facture.setDateFacture(rs.getDate("date_facture").toLocalDate());
                facture.setDateEcheance(rs.getDate("date_echeance").toLocalDate());
                // SUPPRIMER: facture.setStatusPaiement(rs.getString("status_paiement"));
                
                rs.close();
                pstmt.close();
                return facture;
            }
            
            rs.close();
            pstmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur récupération facture: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Récupère toutes les factures d'un client spécifique
     */
    public List<Facture> getFacturesByClientId(int clientId) {
        List<Facture> factures = new ArrayList<>();
        
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String query = """
                SELECT f.* 
                FROM facture f
                JOIN livraison l ON f.id_livraison = l.id
                WHERE l.id_client = ?
                ORDER BY f.date_facture DESC
                """;
            
            System.out.println("DEBUG: Exécution de la requête pour client ID: " + clientId);
            System.out.println("DEBUG: Requête: " + query);
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, clientId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Facture facture = new Facture();
                facture.setId(rs.getInt("id"));
                facture.setIdLivraison(rs.getInt("id_livraison"));
                facture.setNumeroFacture(rs.getString("numero_facture"));
                facture.setMontantTotal(rs.getDouble("montant_total"));
                facture.setTva(rs.getDouble("tva"));
                facture.setMontantTTC(rs.getDouble("montant_ttc"));
                facture.setDateFacture(rs.getDate("date_facture").toLocalDate());
                facture.setDateEcheance(rs.getDate("date_echeance").toLocalDate());
                // SUPPRIMER: facture.setStatusPaiement(rs.getString("status_paiement"));
                
                factures.add(facture);
            }
            
            rs.close();
            pstmt.close();
            
            System.out.println("✅ " + factures.size() + " facture(s) trouvée(s) pour le client " + clientId);
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur récupération factures client: " + e.getMessage());
        }
        
        return factures;
    }
    
    /**
     * Récupère toutes les factures (pour l'administration)
     */
    public List<Facture> getAllFactures() {
        List<Facture> factures = new ArrayList<>();
        
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String query = "SELECT * FROM facture ORDER BY date_facture DESC";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                Facture facture = new Facture();
                facture.setId(rs.getInt("id"));
                facture.setIdLivraison(rs.getInt("id_livraison"));
                facture.setNumeroFacture(rs.getString("numero_facture"));
                facture.setMontantTotal(rs.getDouble("montant_total"));
                facture.setTva(rs.getDouble("tva"));
                facture.setMontantTTC(rs.getDouble("montant_ttc"));
                facture.setDateFacture(rs.getDate("date_facture").toLocalDate());
                facture.setDateEcheance(rs.getDate("date_echeance").toLocalDate());
                // SUPPRIMER: facture.setStatusPaiement(rs.getString("status_paiement"));
                
                factures.add(facture);
            }
            
            rs.close();
            stmt.close();
            
            System.out.println("✅ " + factures.size() + " facture(s) trouvée(s) dans la base de données");
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur récupération toutes les factures: " + e.getMessage());
        }
        
        return factures;
    }
    
    /**
     * Crée la table facture si elle n'existe pas
     */
    public boolean createTableIfNotExists() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            // SUPPRIMER status_paiement de la création de table
            String query = """
                CREATE TABLE IF NOT EXISTS facture (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    id_livraison INT NOT NULL,
                    numero_facture VARCHAR(50) UNIQUE NOT NULL,
                    montant_total DECIMAL(10,2) NOT NULL,
                    tva DECIMAL(10,2) NOT NULL,
                    montant_ttc DECIMAL(10,2) NOT NULL,
                    date_facture DATE NOT NULL,
                    date_echeance DATE NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (id_livraison) REFERENCES livraison(id) ON DELETE CASCADE
                )
                """;
            
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
            
            System.out.println("✅ Table 'facture' vérifiée/créée");
            return true;
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur création table facture");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Récupère une facture par son ID
     */
    public Facture getFactureById(int idFacture) {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String query = "SELECT * FROM facture WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, idFacture);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Facture facture = new Facture();
                facture.setId(rs.getInt("id"));
                facture.setIdLivraison(rs.getInt("id_livraison"));
                facture.setNumeroFacture(rs.getString("numero_facture"));
                facture.setMontantTotal(rs.getDouble("montant_total"));
                facture.setTva(rs.getDouble("tva"));
                facture.setMontantTTC(rs.getDouble("montant_ttc"));
                facture.setDateFacture(rs.getDate("date_facture").toLocalDate());
                facture.setDateEcheance(rs.getDate("date_echeance").toLocalDate());
                // SUPPRIMER: facture.setStatusPaiement(rs.getString("status_paiement"));
                
                rs.close();
                pstmt.close();
                return facture;
            }
            
            rs.close();
            pstmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur récupération facture par ID: " + e.getMessage());
        }
        
        return null;
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Connexion FactureService fermée");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}