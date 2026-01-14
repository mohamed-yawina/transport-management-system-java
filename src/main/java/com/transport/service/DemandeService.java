package com.transport.service;

import com.transport.model.Livraison;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DemandeService {
    
    private Connection connection;
    private String url = "jdbc:mysql://localhost:3306/transport_db";
    private String user = "root";
    private String password = "";
    
    public DemandeService() {
        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("✅ DemandeService: Connexion BD établie");
        } catch (SQLException e) {
            System.err.println("❌ DemandeService: Erreur de connexion BD: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Trouve ou crée un client dans la table utilisateur
     */
    private int trouverOuCreerClient(String nomClient) throws SQLException {
        System.out.println("🔍 Recherche du client: " + nomClient);
        
        // 1. Essayez de trouver un client existant avec ce nom
        String query = "SELECT id FROM utilisateur WHERE nom = ? AND role = 'client' LIMIT 1";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setString(1, nomClient);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            int id = rs.getInt("id");
            rs.close();
            pstmt.close();
            System.out.println("✅ Client existant trouvé avec ID: " + id);
            return id;
        }
        
        rs.close();
        pstmt.close();
        
        // 2. Si non trouvé, créez un nouveau client
        System.out.println("🆕 Création d'un nouveau client pour: " + nomClient);
        String email = nomClient.toLowerCase().replaceAll("[^a-z0-9]", ".") + "@transport.com";
        String insertQuery = "INSERT INTO utilisateur (nom, email, role, mot_de_passe) VALUES (?, ?, 'client', ?)";
        pstmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, nomClient);
        pstmt.setString(2, email);
        pstmt.setString(3, "password123"); // Mot de passe par défaut
        
        int affectedRows = pstmt.executeUpdate();
        
        if (affectedRows > 0) {
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int newId = generatedKeys.getInt(1);
                System.out.println("✅ Nouveau client créé avec ID: " + newId + " et email: " + email);
                generatedKeys.close();
                pstmt.close();
                return newId;
            }
            generatedKeys.close();
        }
        
        pstmt.close();
        
        // 3. Fallback : chercher n'importe quel client existant
        System.out.println("⚠️ Tentative de fallback: recherche d'un client existant");
        query = "SELECT id FROM utilisateur WHERE role = 'client' LIMIT 1";
        pstmt = connection.prepareStatement(query);
        rs = pstmt.executeQuery();
        
        if (rs.next()) {
            int fallbackId = rs.getInt("id");
            System.out.println("✅ Utilisation du client fallback ID: " + fallbackId);
            rs.close();
            pstmt.close();
            return fallbackId;
        }
        
        rs.close();
        pstmt.close();
        
        // 4. Dernier recours : ID 1 (doit exister si la table utilisateur n'est pas vide)
        System.out.println("⚠️ Utilisation de l'ID client par défaut: 1");
        return 1;
    }
    
    /**
     * Met à jour le statut d'une livraison
     */
    public boolean updateLivraisonStatus(int id, String newStatus) {
        System.out.println("\n=== MISE À JOUR STATUT ===");
        System.out.println("📝 Livraison ID: " + id);
        System.out.println("🔄 Nouveau statut: " + newStatus);
        
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("🔌 Connexion fermée, reconnexion...");
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String updateQuery = "UPDATE livraison SET status = ?, date_modification = NOW() WHERE id = ?";
            System.out.println("📝 Requête UPDATE: " + updateQuery);
            
            try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
                pstmt.setString(1, newStatus);
                pstmt.setInt(2, id);

                int rows = pstmt.executeUpdate();
                System.out.println("📊 Lignes affectées: " + rows);
                
                if (rows > 0) {
                    System.out.println("✅ SUCCÈS: Statut mis à jour");
                    return true;
                } else {
                    System.err.println("⚠️ ÉCHEC: 0 ligne affectée");
                    return false;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ ERREUR SQL dans updateLivraisonStatus: " + e.getMessage());
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ ERREUR Générale dans updateLivraisonStatus: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Récupère une livraison par son ID
     */
    public Livraison getLivraisonById(int id) {
        System.out.println("\n=== getLivraisonById ===");
        System.out.println("ID recherché: " + id);
        
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("🔌 Connexion fermée, reconnexion...");
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String query = "SELECT * FROM livraison WHERE id = ?";
            System.out.println("📝 Requête: " + query);
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Livraison livraison = createLivraisonFromResultSet(rs);
                
                System.out.println("✅ Livraison trouvée:");
                System.out.println("   ID: " + livraison.getId());
                System.out.println("   Destination: " + livraison.getDestination());
                System.out.println("   Statut: " + livraison.getStatus());
                System.out.println("   Client: " + livraison.getNomClient());
                System.out.println("   Chauffeur ID: " + livraison.getIdChauffeur());
                System.out.println("   Chauffeur Nom: " + livraison.getChauffeurNom());
                
                rs.close();
                pstmt.close();
                
                return livraison;
            } else {
                System.out.println("❌ Aucune livraison trouvée avec ID: " + id);
            }
            
            rs.close();
            pstmt.close();
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL dans getLivraisonById: " + e.getMessage());
            System.err.println("   Code erreur: " + e.getErrorCode());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Récupère toutes les livraisons d'un chauffeur
     */
    public List<Livraison> getLivraisonsByChauffeurId(int chauffeurId) {
        List<Livraison> livraisons = new ArrayList<>();
        
        System.out.println("\n=== getLivraisonsByChauffeurId ===");
        System.out.println("Chauffeur ID: " + chauffeurId);
        
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String query = "SELECT * FROM livraison WHERE id_chauffeur = ? ORDER BY date_creation DESC";
            System.out.println("📝 Requête: " + query);
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, chauffeurId);
            ResultSet rs = pstmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                Livraison livraison = createLivraisonFromResultSet(rs);
                livraisons.add(livraison);
                count++;
            }
            
            rs.close();
            pstmt.close();
            
            System.out.println("✅ " + count + " livraison(s) trouvée(s) pour le chauffeur ID: " + chauffeurId);
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération livraisons chauffeur: " + e.getMessage());
            System.err.println("   Code erreur: " + e.getErrorCode());
            e.printStackTrace();
        }
        
        return livraisons;
    }
    
    /**
     * Récupère toutes les livraisons d'un client
     */
    public List<Livraison> getLivraisonsByClient(int clientId) {
        List<Livraison> livraisons = new ArrayList<>();
        
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String query = "SELECT * FROM livraison WHERE id_client = ? ORDER BY date_creation DESC";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, clientId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Livraison livraison = createLivraisonFromResultSet(rs);
                livraisons.add(livraison);
            }
            
            rs.close();
            pstmt.close();
            
            System.out.println("✅ " + livraisons.size() + " livraison(s) trouvée(s) pour le client ID: " + clientId);
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération livraisons client: " + e.getMessage());
            e.printStackTrace();
        }
        
        return livraisons;
    }
    
    /**
     * Récupère toutes les livraisons de la base de données
     */
    public List<Livraison> getAllLivraisons() {
        List<Livraison> livraisons = new ArrayList<>();
        
        System.out.println("\n=== getAllLivraisons ===");
        
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String query = "SELECT * FROM livraison ORDER BY date_creation DESC";
            System.out.println("📝 Requête: " + query);
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            int count = 0;
            while (rs.next()) {
                Livraison livraison = createLivraisonFromResultSet(rs);
                livraisons.add(livraison);
                count++;
            }
            
            rs.close();
            stmt.close();
            
            System.out.println("✅ " + count + " livraison(s) récupérée(s) depuis la BD");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération toutes les livraisons: " + e.getMessage());
            System.err.println("   Code erreur: " + e.getErrorCode());
            e.printStackTrace();
        }
        
        return livraisons;
    }
    
    /**
     * Crée une nouvelle livraison
     */
    public boolean createLivraison(Livraison livraison) {
        System.out.println("\n=== createLivraison ===");
        System.out.println("Client: " + livraison.getNomClient());
        System.out.println("Destination: " + livraison.getDestination());
        System.out.println("Type: " + livraison.getTypeTransport());
        
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            // 1. Rechercher ou créer un ID client valide
            int idClient = trouverOuCreerClient(livraison.getNomClient());
            
            String query = "INSERT INTO livraison (" +
                         "id_client, nom_client, destination, type_transport, " +
                         "distance_km, poids_tonnes, prix_total, status, " +
                         "id_chauffeur, chauffeur_nom, date_creation" +
                         ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            System.out.println("📝 Requête INSERT: " + query);
            
            PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            
            // Utiliser l'ID client valide
            pstmt.setInt(1, idClient);
            pstmt.setString(2, livraison.getNomClient());
            pstmt.setString(3, livraison.getDestination());
            pstmt.setString(4, livraison.getTypeTransport());
            pstmt.setDouble(5, livraison.getDistanceKm());
            pstmt.setDouble(6, livraison.getPoidsTonnes());
            pstmt.setDouble(7, livraison.getPrixTotal());
            pstmt.setString(8, livraison.getStatus());
            
            // Gestion des valeurs nulles pour id_chauffeur
            if (livraison.getIdChauffeur() > 0) {
                pstmt.setInt(9, livraison.getIdChauffeur());
            } else {
                pstmt.setNull(9, java.sql.Types.INTEGER);
            }
            
            pstmt.setString(10, livraison.getChauffeurNom());
            pstmt.setDate(11, Date.valueOf(LocalDate.now()));
            
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    livraison.setId(generatedId);
                    System.out.println("✅ Livraison créée avec ID: " + generatedId);
                }
                
                generatedKeys.close();
                pstmt.close();
                return true;
            }
            
            pstmt.close();
            return false;
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur création livraison: " + e.getMessage());
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Met à jour une livraison complète
     */
    public boolean updateLivraison(Livraison livraison) {
        System.out.println("\n=== updateLivraison ===");
        System.out.println("ID: " + livraison.getId());
        System.out.println("Client: " + livraison.getNomClient());
        System.out.println("Destination: " + livraison.getDestination());
        System.out.println("Statut: " + livraison.getStatus());
        System.out.println("Chauffeur ID: " + livraison.getIdChauffeur());
        
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            // 1. D'abord, récupérer l'ID client existant de cette livraison
            String getClientQuery = "SELECT id_client FROM livraison WHERE id = ?";
            PreparedStatement getPstmt = connection.prepareStatement(getClientQuery);
            getPstmt.setInt(1, livraison.getId());
            ResultSet rs = getPstmt.executeQuery();
            
            int existingClientId = 0;
            if (rs.next()) {
                existingClientId = rs.getInt("id_client");
                System.out.println("📋 ID client existant: " + existingClientId);
            }
            rs.close();
            getPstmt.close();
            
            // 2. Si pas d'ID client existant ou si le nom a changé, en trouver/créer un
            int clientIdToUse;
            if (existingClientId == 0) {
                System.out.println("⚠️ Pas d'ID client existant, recherche/création d'un nouveau");
                clientIdToUse = trouverOuCreerClient(livraison.getNomClient());
            } else {
                // Vérifier si le nom du client a changé
                String checkNameQuery = "SELECT nom_client FROM livraison WHERE id = ?";
                getPstmt = connection.prepareStatement(checkNameQuery);
                getPstmt.setInt(1, livraison.getId());
                rs = getPstmt.executeQuery();
                
                String oldClientName = "";
                if (rs.next()) {
                    oldClientName = rs.getString("nom_client");
                }
                rs.close();
                getPstmt.close();
                
                if (!oldClientName.equals(livraison.getNomClient())) {
                    System.out.println("🔄 Nom client modifié, recherche d'un nouveau client: " + livraison.getNomClient());
                    clientIdToUse = trouverOuCreerClient(livraison.getNomClient());
                } else {
                    clientIdToUse = existingClientId;
                }
            }
            
            // 3. Maintenant faire la mise à jour avec l'ID client valide
            String updateQuery = "UPDATE livraison SET " +
                              "id_client = ?, " +
                              "nom_client = ?, " +
                              "destination = ?, " +
                              "type_transport = ?, " +
                              "distance_km = ?, " +
                              "poids_tonnes = ?, " +
                              "prix_total = ?, " +
                              "status = ?, " +
                              "id_chauffeur = ?, " +
                              "chauffeur_nom = ?, " +
                              "date_modification = NOW() " +
                              "WHERE id = ?";
            
            System.out.println("📝 Requête UPDATE: " + updateQuery);
            
            PreparedStatement pstmt = connection.prepareStatement(updateQuery);
            
            pstmt.setInt(1, clientIdToUse);
            pstmt.setString(2, livraison.getNomClient());
            pstmt.setString(3, livraison.getDestination());
            pstmt.setString(4, livraison.getTypeTransport());
            pstmt.setDouble(5, livraison.getDistanceKm());
            pstmt.setDouble(6, livraison.getPoidsTonnes());
            pstmt.setDouble(7, livraison.getPrixTotal());
            pstmt.setString(8, livraison.getStatus());
            
            if (livraison.getIdChauffeur() > 0) {
                pstmt.setInt(9, livraison.getIdChauffeur());
            } else {
                pstmt.setNull(9, java.sql.Types.INTEGER);
            }
            
            pstmt.setString(10, livraison.getChauffeurNom());
            pstmt.setInt(11, livraison.getId());
            
            int rows = pstmt.executeUpdate();
            pstmt.close();
            
            System.out.println("📊 Lignes affectées: " + rows);
            
            if (rows > 0) {
                System.out.println("✅ SUCCÈS: Livraison ID " + livraison.getId() + " mise à jour");
                System.out.println("   ID Client utilisé: " + clientIdToUse);
                return true;
            } else {
                System.err.println("⚠️ ÉCHEC: 0 ligne affectée pour ID " + livraison.getId());
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour livraison: " + e.getMessage());
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Met à jour seulement le chauffeur d'une livraison
     */
    public boolean updateChauffeurLivraison(int idLivraison, int idChauffeur, String chauffeurNom) {
        System.out.println("\n=== updateChauffeurLivraison ===");
        System.out.println("Livraison ID: " + idLivraison);
        System.out.println("Chauffeur ID: " + idChauffeur);
        System.out.println("Chauffeur Nom: " + chauffeurNom);
        
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String query = "UPDATE livraison SET id_chauffeur = ?, chauffeur_nom = ?, date_modification = NOW() WHERE id = ?";
            System.out.println("📝 Requête: " + query);
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, idChauffeur);
            pstmt.setString(2, chauffeurNom);
            pstmt.setInt(3, idLivraison);
            
            int rows = pstmt.executeUpdate();
            pstmt.close();
            
            System.out.println("📊 Lignes affectées: " + rows);
            
            if (rows > 0) {
                System.out.println("✅ SUCCÈS: Chauffeur assigné à livraison ID " + idLivraison);
                return true;
            } else {
                System.err.println("⚠️ ÉCHEC: 0 ligne affectée");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur assignation chauffeur: " + e.getMessage());
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Retire le chauffeur d'une livraison
     */
    public boolean retirerChauffeur(int idLivraison) {
        System.out.println("\n=== retirerChauffeur ===");
        System.out.println("Livraison ID: " + idLivraison);
        
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String query = "UPDATE livraison SET id_chauffeur = NULL, chauffeur_nom = NULL, date_modification = NOW() WHERE id = ?";
            System.out.println("📝 Requête: " + query);
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, idLivraison);
            
            int rows = pstmt.executeUpdate();
            pstmt.close();
            
            System.out.println("📊 Lignes affectées: " + rows);
            
            if (rows > 0) {
                System.out.println("✅ SUCCÈS: Chauffeur retiré de la livraison ID " + idLivraison);
                return true;
            } else {
                System.err.println("⚠️ ÉCHEC: 0 ligne affectée");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur retrait chauffeur: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Supprime une livraison par son ID
     */
    public boolean deleteLivraison(int id) {
        System.out.println("\n=== deleteLivraison ===");
        System.out.println("ID à supprimer: " + id);
        
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String query = "DELETE FROM livraison WHERE id = ?";
            System.out.println("📝 Requête DELETE: " + query);
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, id);
            
            int rows = pstmt.executeUpdate();
            pstmt.close();
            
            System.out.println("📊 Lignes affectées: " + rows);
            
            if (rows > 0) {
                System.out.println("✅ SUCCÈS: Livraison ID " + id + " supprimée");
                return true;
            } else {
                System.err.println("⚠️ ÉCHEC: 0 ligne affectée pour ID " + id);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression livraison: " + e.getMessage());
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Crée la table livraison si elle n'existe pas
     */
    public boolean createTableIfNotExists() {
        System.out.println("\n=== createTableIfNotExists ===");
        
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String query = """
                CREATE TABLE IF NOT EXISTS livraison (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    id_client INT NOT NULL,
                    nom_client VARCHAR(100) NOT NULL,
                    destination VARCHAR(200) NOT NULL,
                    type_transport VARCHAR(50) NOT NULL,
                    distance_km DECIMAL(10,2) NOT NULL,
                    poids_tonnes DECIMAL(10,2) NOT NULL,
                    prix_total DECIMAL(10,2) NOT NULL,
                    status VARCHAR(50) DEFAULT 'En attente',
                    id_chauffeur INT DEFAULT NULL,
                    chauffeur_nom VARCHAR(100) DEFAULT NULL,
                    date_creation DATE NOT NULL,
                    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_status (status),
                    INDEX idx_client (id_client),
                    INDEX idx_chauffeur (id_chauffeur),
                    FOREIGN KEY (id_client) REFERENCES utilisateur(id) ON DELETE CASCADE
                )
                """;
            
            System.out.println("📝 Requête CREATE TABLE: " + query);
            
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
            
            System.out.println("✅ Table 'livraison' vérifiée/créée");
            return true;
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur création table livraison");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Vérifie la structure de la table livraison
     */
    public void checkTableStructure() {
        System.out.println("\n=== checkTableStructure ===");
        
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            
            String query = "DESCRIBE livraison";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            System.out.println("📋 Structure de la table 'livraison':");
            System.out.println("-------------------------------------");
            while (rs.next()) {
                String field = rs.getString("Field");
                String type = rs.getString("Type");
                String nullable = rs.getString("Null");
                String key = rs.getString("Key");
                String defaultValue = rs.getString("Default");
                
                System.out.println(field + " | " + type + " | " + nullable + " | " + key + " | " + defaultValue);
            }
            System.out.println("-------------------------------------");
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification structure table: " + e.getMessage());
        }
    }
    
    /**
     * Méthode utilitaire pour créer un objet Livraison à partir d'un ResultSet
     */
    private Livraison createLivraisonFromResultSet(ResultSet rs) throws SQLException {
        Livraison livraison = new Livraison();
        livraison.setId(rs.getInt("id"));
        livraison.setNomClient(rs.getString("nom_client"));
        livraison.setDestination(rs.getString("destination"));
        livraison.setTypeTransport(rs.getString("type_transport"));
        livraison.setDistanceKm(rs.getDouble("distance_km"));
        livraison.setPoidsTonnes(rs.getDouble("poids_tonnes"));
        livraison.setPrixTotal(rs.getDouble("prix_total"));
        livraison.setStatus(rs.getString("status"));
        
        // Colonnes chauffeur
        livraison.setIdChauffeur(rs.getInt("id_chauffeur"));
        livraison.setChauffeurNom(rs.getString("chauffeur_nom"));
        
        // Dates
        if (rs.getDate("date_creation") != null) {
            livraison.setDateCreation(rs.getDate("date_creation").toLocalDate());
        }
        if (rs.getTimestamp("date_modification") != null) {
            livraison.setDateModification(rs.getTimestamp("date_modification").toLocalDateTime().toLocalDate());
        }
        
        return livraison;
    }
    
    /**
     * Ferme la connexion à la base de données
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Connexion DemandeService fermée");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}