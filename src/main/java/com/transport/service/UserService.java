package com.transport.service;

import com.transport.model.Utilisateur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    
    private Connection connection;
    
    public UserService() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/transport_db", 
                "root", 
                ""
            );
            System.out.println("✅ UserService: Connexion BD établie");
        } catch (Exception e) {
            System.out.println("❌ UserService: Connexion BD non établie");
            connection = null;
        }
    }
    
    // CORRECTION: Utiliser motdepasse sans underscore
    public List<Utilisateur> getAllUsers() {
        List<Utilisateur> users = new ArrayList<>();
        
        if (connection == null) {
            System.out.println("⚠️ UserService: Pas de connexion BD");
            return users;
        }
        
        try {
            String sql = "SELECT id, nom, email, motdepasse, role FROM utilisateur"; // motdepasse sans underscore
            System.out.println("📝 UserService SQL: " + sql);
            
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                Utilisateur user = new Utilisateur();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setEmail(rs.getString("email"));
                user.setMotdepasse(rs.getString("motdepasse")); // motdepasse sans underscore
                user.setRole(rs.getString("role"));
                users.add(user);
            }
            
            System.out.println("📊 UserService: " + count + " utilisateurs chargés");
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("❌ UserService Erreur: " + e.getMessage());
        }
        
        return users;
    }
    
    // CORRECTION: Utiliser motdepasse sans underscore
    public boolean addUser(Utilisateur user) {
        if (connection == null) {
            System.out.println("❌ UserService: Pas de connexion BD");
            return false;
        }
        
        try {
            // CORRECTION: Utiliser motdepasse sans underscore
            String sql = "INSERT INTO utilisateur (nom, email, motdepasse, role) VALUES (?, ?, ?, ?)";
            System.out.println("📝 UserService Insert: " + sql);
            
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getMotdepasse());
            pstmt.setString(4, user.getRole());
            
            int rows = pstmt.executeUpdate();
            pstmt.close();
            
            System.out.println("✅ UserService: " + rows + " ligne(s) insérée(s)");
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ UserService Erreur ajout: " + e.getMessage());
            return false;
        }
    }
    
    // CORRECTION pour updateUser (pas besoin de motdepasse ici)
    public boolean updateUser(Utilisateur user) {
        if (connection == null) return false;
        
        try {
            String sql = "UPDATE utilisateur SET nom = ?, email = ?, role = ? WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getRole());
            pstmt.setInt(4, user.getId());
            
            int rows = pstmt.executeUpdate();
            pstmt.close();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur updateUser: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteUser(int id) {
        if (connection == null) return false;
        
        try {
            String sql = "DELETE FROM utilisateur WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            
            int rows = pstmt.executeUpdate();
            pstmt.close();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur deleteUser: " + e.getMessage());
            return false;
        }
    }
    
    // CORRECTION: Utiliser motdepasse sans underscore
    public boolean emailExists(String email) {
        if (connection == null) return false;
        
        try {
            String sql = "SELECT COUNT(*) FROM utilisateur WHERE email = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, email);
            
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            
            rs.close();
            pstmt.close();
            
            System.out.println("🔍 emailExists '" + email + "': " + (count > 0));
            return count > 0;
        } catch (SQLException e) {
            System.err.println("Erreur emailExists: " + e.getMessage());
            return false;
        }
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 UserService: Connexion fermée");
            }
        } catch (SQLException e) {
            System.err.println("Erreur fermeture UserService: " + e.getMessage());
        }
    }
}