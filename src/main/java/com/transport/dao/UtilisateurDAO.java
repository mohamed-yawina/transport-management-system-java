package com.transport.dao;

import com.transport.model.Utilisateur;
import com.transport.util.DBConnection;

import java.sql.*;

public class UtilisateurDAO {

    public Utilisateur login(String email, String password) {

        String sql = "SELECT * FROM utilisateur WHERE email=? AND mot_de_passe=?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Utilisateur(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe"),
                        rs.getString("role")
                ) {};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
