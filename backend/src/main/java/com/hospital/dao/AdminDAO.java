package com.hospital.dao;

import com.hospital.util.DBConnection;
import com.hospital.util.ValidationUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Data Access Object (DAO) for Administrator operations.
 * Demonstrates:
 * - Secure BCrypt password hashing.
 * - Safe first-time administrator initialization from secure configuration sources.
 * - Prevention of accidental or silent password overwrites.
 * - Strict parameterization to prevent SQL injection.
 */
public class AdminDAO {

    public AdminDAO() {
        ensureAdminTableAndInitialize();
    }

    /**
     * Ensures admins table exists and securely initializes the first administrator
     * ONLY if no administrator exists in the database and credentials are provided
     * through environment variables or local configuration.
     * Never silently resets an existing administrator's password.
     */
    public void ensureAdminTableAndInitialize() {
        String createSql = "CREATE TABLE IF NOT EXISTS admins (" +
                "admin_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(100) NOT NULL UNIQUE, " +
                "password_hash VARCHAR(255) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(createSql)) {
            ps.execute();

            if (!hasAnyAdmin()) {
                // Read from secure environment variables or local configuration
                String initialUser = System.getenv("ADMIN_USERNAME");
                if (initialUser == null) initialUser = System.getenv("INITIAL_ADMIN_USERNAME");

                String initialPass = System.getenv("ADMIN_PASSWORD");
                if (initialPass == null) initialPass = System.getenv("INITIAL_ADMIN_PASSWORD");

                // Check local config file if env vars not present
                if (initialPass == null || initialPass.trim().isEmpty()) {
                    Properties localProps = loadLocalAdminConfig();
                    if (localProps != null) {
                        if (localProps.getProperty("admin.initial.username") != null) {
                            initialUser = localProps.getProperty("admin.initial.username").trim();
                        }
                        if (localProps.getProperty("admin.initial.password") != null) {
                            initialPass = localProps.getProperty("admin.initial.password").trim();
                        }
                    }
                }

                if (initialPass != null && !initialPass.trim().isEmpty()) {
                    if (initialUser == null || initialUser.trim().isEmpty()) {
                        initialUser = "admin";
                    }
                    boolean created = createAdmin(initialUser.trim(), initialPass.trim());
                    if (created) {
                        System.out.println("[AdminDAO] Successfully initialized first administrator '"
                                + initialUser.trim() + "' from secure configuration.");
                    }
                } else {
                    System.out.println("[AdminDAO] Notice: No administrator account currently exists in the database.");
                    System.out.println("[AdminDAO] To create the first administrator, set the ADMIN_PASSWORD environment variable or use the initial admin setup.");
                }
            }
        } catch (SQLException e) {
            System.err.println("[AdminDAO] Table initialization note: " + e.getMessage());
        }
    }

    /**
     * Checks if at least one administrator account exists in the database.
     */
    public boolean hasAnyAdmin() {
        String checkSql = "SELECT COUNT(*) FROM admins";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[AdminDAO] Failed to check admin existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Creates a new administrator with BCrypt password hashing.
     */
    public boolean createAdmin(String username, String rawPassword) {
        if (ValidationUtil.isNullOrEmpty(username) || ValidationUtil.isNullOrEmpty(rawPassword)) {
            return false;
        }
        if (rawPassword.length() < 6) {
            return false;
        }

        String insertSql = "INSERT INTO admins (username, password_hash) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, username.trim().toLowerCase());
            ps.setString(2, BCrypt.hashpw(rawPassword, BCrypt.gensalt(10)));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AdminDAO] Failed to create admin: " + e.getMessage());
            return false;
        }
    }

    /**
     * Safe first-time administrator setup method.
     * Can only be executed when NO administrator account exists in the database.
     */
    public synchronized boolean setupInitialAdmin(String username, String rawPassword) {
        if (hasAnyAdmin()) {
            // Refuse: Initial setup is already locked once an admin exists
            return false;
        }
        return createAdmin(username, rawPassword);
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

    /**
     * Helper to load optional admin credentials from db-local.properties if present.
     */
    private Properties loadLocalAdminConfig() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("db-local.properties");
            if (is == null) {
                File f = new File("db-local.properties");
                if (f.exists()) is = new FileInputStream(f);
            }
            if (is != null) {
                try {
                    Properties p = new Properties();
                    p.load(is);
                    return p;
                } finally {
                    is.close();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
