package com.transport.service;

import com.transport.model.Utilisateur;
import java.sql.*;

public class AuthentificationService {
    
    private Connection connection;
    
    public AuthentificationService() {
        try {
            System.out.println("🔄 Tentative de connexion à la BD...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/transport_db", 
                "root", 
                ""
            );
            System.out.println("✅ Connexion BD établie pour AuthentificationService");
            
            // Tester la connexion
            testConnection();
            
        } catch (Exception e) {
            System.out.println("❌ ERREUR Connexion BD: " + e.getMessage());
            e.printStackTrace();
            connection = null;
        }
    }
    
    private void testConnection() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM utilisateur");
            if (rs.next()) {
                System.out.println("📊 Utilisateurs dans la BD: " + rs.getInt("count"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("❌ Test connexion échoué: " + e.getMessage());
        }
    }
    
    public Utilisateur authentifier(String email, String motdepasse) throws Exception {
        System.out.println("🔐 Authentification pour: " + email);
        
        if (connection == null) {
            throw new Exception("Connexion BD impossible. Vérifiez MySQL.");
        }
        
        try {
            // CORRECTION: Utiliser "motdepasse" sans underscore
            String sql = "SELECT id, nom, email, motdepasse, role FROM utilisateur WHERE email = ?";
            
            System.out.println("📝 SQL: " + sql);
            
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, email);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("✅ Utilisateur trouvé: " + rs.getString("nom"));
                
                String motdepasseBD = rs.getString("motdepasse"); // motdepasse sans underscore
                
                // Debug détaillé
                System.out.println("🔑 Comparaison:");
                System.out.println("   BD: '" + motdepasseBD + "'");
                System.out.println("   Saisi: '" + motdepasse + "'");
                System.out.println("   Égal? " + motdepasseBD.equals(motdepasse));
                
                if (motdepasseBD.equals(motdepasse)) {
                    Utilisateur user = new Utilisateur();
                    user.setId(rs.getInt("id"));
                    user.setNom(rs.getString("nom"));
                    user.setEmail(rs.getString("email"));
                    user.setMotdepasse(motdepasseBD);
                    user.setRole(rs.getString("role"));
                    
                    System.out.println("🎉 Authentification RÉUSSIE: " + user.getNom() + " (" + user.getRole() + ")");
                    
                    rs.close();
                    pstmt.close();
                    return user;
                } else {
                    System.out.println("❌ Mot de passe incorrect");
                    rs.close();
                    pstmt.close();
                    throw new Exception("Mot de passe incorrect");
                }
            } else {
                System.out.println("❌ Email non trouvé: " + email);
                rs.close();
                pstmt.close();
                
                // Debug: afficher tous les emails
                debugAllEmails();
                
                throw new Exception("Aucun compte trouvé avec cet email");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ ERREUR SQL: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Erreur base de données: " + e.getMessage());
        }
    }
    
    private void debugAllEmails() {
        try {
            System.out.println("📧 Emails dans la BD:");
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT email FROM utilisateur");
            while (rs.next()) {
                System.out.println("   - " + rs.getString("email"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Erreur debug emails: " + e.getMessage());
        }
    }
    
    // Les autres méthodes restent les mêmes
    public boolean estAdmin(Utilisateur user) {
        return user != null && "ADMIN".equals(user.getRole());
    }
    
    public boolean estClient(Utilisateur user) {
        return user != null && "CLIENT".equals(user.getRole());
    }
    
    public boolean estChauffeur(Utilisateur user) {
        return user != null && "CHAUFFEUR".equals(user.getRole());
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Connexion BD fermée");
            }
        } catch (SQLException e) {
            System.err.println("Erreur fermeture connexion: " + e.getMessage());
        }
    }
}