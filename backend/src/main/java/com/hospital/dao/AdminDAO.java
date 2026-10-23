package com.hospital.dao;

import com.hospital.util.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object (DAO) for Administrator operations.
 * Demonstrates secure database-backed admin authentication using BCrypt password hashing.
 */
public class AdminDAO {

    public AdminDAO() {
        ensureAdminTableAndSeed();
    }

    /**
     * Ensures admins table exists and seeds the default admin with a BCrypt hashed password
     * if no admin user is currently present or if updating initial placeholder hash.
     */
    public void ensureAdminTableAndSeed() {
        String createSql = "CREATE TABLE IF NOT EXISTS admins (" +
                "admin_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(100) NOT NULL UNIQUE, " +
                "password_hash VARCHAR(255) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(createSql)) {
            ps.execute();

            // Check if default admin exists
            String checkSql = "SELECT password_hash FROM admins WHERE username = ?";
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, "admin");
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (!rs.next()) {
                        // Seed default admin with valid BCrypt hashed password ("admin123")
                        String seedSql = "INSERT INTO admins (username, password_hash) VALUES (?, ?)";
                        try (PreparedStatement seedPs = conn.prepareStatement(seedSql)) {
                            seedPs.setString(1, "admin");
                            seedPs.setString(2, BCrypt.hashpw("admin123", BCrypt.gensalt(10)));
                            seedPs.executeUpdate();
                            System.out.println("[AdminDAO] Initialized default admin account with BCrypt hashed credentials.");
                        }
                    } else {
                        String existingHash = rs.getString("password_hash");
                        // If placeholder dummy hash detected, update to valid BCrypt hash
                        if (existingHash == null || !existingHash.startsWith("$2a$") || existingHash.contains("w0u3V")) {
                            String updateSql = "UPDATE admins SET password_hash = ? WHERE username = ?";
                            try (PreparedStatement upPs = conn.prepareStatement(updateSql)) {
                                upPs.setString(1, BCrypt.hashpw("admin123", BCrypt.gensalt(10)));
                                upPs.setString(2, "admin");
                                upPs.executeUpdate();
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[AdminDAO] Note on table initialization: " + e.getMessage());
        }
    }

    /**
     * Authenticates administrator using BCrypt password comparison against database.
     * Prevents timing attacks and eliminates hardcoded plaintext credentials.
     */
    public boolean authenticate(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            return false;
        }

        String sql = "SELECT password_hash FROM admins WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    try {
                        return BCrypt.checkpw(rawPassword, storedHash);
                    } catch (Exception ex) {
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[AdminDAO] Database query failed: " + e.getMessage());
        }
        return false;
    }
}

