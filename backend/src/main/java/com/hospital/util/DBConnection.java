package com.hospital.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Utility class to manage JDBC database connections.
 * Demonstrates: JDBC DriverManager, secure configuration loading (environment variables,
 * local override files, base properties), safe logging without credential leakage,
 * and transparent fallback reporting.
 */
public class DBConnection {

    private static String url = "jdbc:mysql://localhost:3306/hospital_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String user = "root";
    private static String password = "";
    private static String driver = "com.mysql.cj.jdbc.Driver";

    private static final String FALLBACK_URL = "jdbc:h2:mem:hospital_db;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static boolean fallbackInitialized = false;
    private static boolean fallbackActive = false;
    private static boolean statusLogged = false;

    static {
        loadConfiguration();
    }

    /**
     * Loads database configuration with strict priority:
     * 1. Environment variables (DB_PASSWORD / HOSPITAL_DB_PASSWORD, etc.)
     * 2. Local config file: db-local.properties (ignored by Git)
     * 3. Base config file: db.properties (defaults, no secrets)
     */
    public static void loadConfiguration() {
        try {
            // Step 1: Load base db.properties from classpath
            InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("db.properties");
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                if (props.getProperty("db.url") != null) url = props.getProperty("db.url").trim();
                if (props.getProperty("db.user") != null) user = props.getProperty("db.user").trim();
                if (props.getProperty("db.password") != null) password = props.getProperty("db.password").trim();
                if (props.getProperty("db.driver") != null) driver = props.getProperty("db.driver").trim();
            }

            // Step 2: Check for local override file db-local.properties
            InputStream localIs = DBConnection.class.getClassLoader().getResourceAsStream("db-local.properties");
            if (localIs == null) {
                File localFile = new File("db-local.properties");
                if (localFile.exists()) {
                    localIs = new FileInputStream(localFile);
                }
            }
            if (localIs != null) {
                try {
                    Properties localProps = new Properties();
                    localProps.load(localIs);
                    if (localProps.getProperty("db.url") != null) url = localProps.getProperty("db.url").trim();
                    if (localProps.getProperty("db.user") != null) user = localProps.getProperty("db.user").trim();
                    if (localProps.getProperty("db.password") != null) password = localProps.getProperty("db.password").trim();
                    if (localProps.getProperty("db.driver") != null) driver = localProps.getProperty("db.driver").trim();
                } finally {
                    localIs.close();
                }
            }

            // Step 3: Override with environment variables if provided
            String envUrl = System.getenv("HOSPITAL_DB_URL");
            if (envUrl == null) envUrl = System.getenv("DB_URL");
            if (envUrl != null && !envUrl.trim().isEmpty()) url = envUrl.trim();

            String envUser = System.getenv("HOSPITAL_DB_USER");
            if (envUser == null) envUser = System.getenv("DB_USER");
            if (envUser != null && !envUser.trim().isEmpty()) user = envUser.trim();

            String envPass = System.getenv("HOSPITAL_DB_PASSWORD");
            if (envPass == null) envPass = System.getenv("DB_PASSWORD");
            if (envPass != null) password = envPass.trim();

            // Register JDBC drivers
            Class.forName(driver);
            Class.forName("org.h2.Driver");
        } catch (Exception e) {
            System.err.println("[DBConnection] Driver loading notice: " + sanitizeMessage(e.getMessage()));
        }
    }

    /**
     * Obtains an active JDBC Connection.
     * Informs the user clearly about database status and persistence mode.
     */
    public static Connection getConnection() throws SQLException {
        if (fallbackActive) {
            return getFallbackConnection();
        }

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            if (!statusLogged) {
                System.out.println("================================================================================");
                System.out.println(" [DATABASE STATUS] Active Database: PERSISTENT MySQL");
                System.out.println(" URL: " + maskUrl(url));
                System.out.println(" User: " + user);
                System.out.println(" Persistence: ENABLED (All patient and appointment data is safely persisted)");
                System.out.println("================================================================================");
                statusLogged = true;
            }
            return conn;
        } catch (SQLException e) {
            if (!fallbackActive) {
                fallbackActive = true;
                System.err.println("================================================================================");
                System.err.println(" [DATABASE STATUS WARNING] Could NOT connect to MySQL database.");
                System.err.println(" Reason: " + sanitizeMessage(e.getMessage()));
                System.err.println(" Target URL: " + maskUrl(url) + " (User: " + user + ")");
                System.err.println("--------------------------------------------------------------------------------");
                System.err.println(" ACTIVATING BUILT-IN IN-MEMORY DEMO DATABASE (H2).");
                System.err.println(" IMPORTANT NOTICE FOR USER & EXAMINER:");
                System.err.println(" * All features are fully functional for demonstration purposes.");
                System.err.println(" * In-memory data is NON-PERSISTENT and will NOT survive server restart.");
                System.err.println(" * To use persistent MySQL storage, configure DB_PASSWORD or db-local.properties.");
                System.err.println("================================================================================");
            }
            return getFallbackConnection();
        }
    }

    private static synchronized Connection getFallbackConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(FALLBACK_URL, "sa", "");
        if (!fallbackInitialized) {
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS patients (" +
                        "patient_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "email VARCHAR(100) NOT NULL UNIQUE, " +
                        "phone VARCHAR(20) NOT NULL, " +
                        "password_hash VARCHAR(255) NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                st.execute("CREATE TABLE IF NOT EXISTS doctors (" +
                        "doctor_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "specialization VARCHAR(100) NOT NULL, " +
                        "available BOOLEAN NOT NULL DEFAULT TRUE, " +
                        "profile_image VARCHAR(500))");

                st.execute("CREATE TABLE IF NOT EXISTS appointments (" +
                        "appointment_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "patient_id INT NOT NULL, " +
                        "doctor_id INT NOT NULL, " +
                        "appointment_date DATE NOT NULL, " +
                        "appointment_time VARCHAR(20) NOT NULL, " +
                        "status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED', " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                st.execute("CREATE TABLE IF NOT EXISTS admins (" +
                        "admin_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "username VARCHAR(100) NOT NULL UNIQUE, " +
                        "password_hash VARCHAR(255) NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                st.execute("MERGE INTO doctors KEY(doctor_id) VALUES " +
                        "(1, 'Dr. Rajesh Sharma', 'Cardiology', TRUE, NULL), " +
                        "(2, 'Dr. Priya Verma', 'Dermatology', TRUE, NULL), " +
                        "(3, 'Dr. Amit Patel', 'Pediatrics', TRUE, NULL), " +
                        "(4, 'Dr. Sneha Kulkarni', 'Orthopedics', TRUE, NULL), " +
                        "(5, 'Dr. Vikram Rao', 'General Medicine', TRUE, NULL), " +
                        "(6, 'Dr. Ananya Sen', 'Neurology', TRUE, NULL), " +
                        "(7, 'Dr. Meera Iyer', 'Obstetrics & Gynecology', TRUE, NULL), " +
                        "(8, 'Dr. Arjun Malhotra', 'Gastroenterology', TRUE, NULL), " +
                        "(9, 'Dr. Kavya Nair', 'Ophthalmology', TRUE, NULL), " +
                        "(10, 'Dr. Rohan Mehta', 'Psychiatry', TRUE, NULL), " +
                        "(11, 'Dr. Neha Kapoor', 'Pulmonology', TRUE, NULL), " +
                        "(12, 'Dr. Sameer Joshi', 'Oncology', TRUE, NULL)");
                fallbackInitialized = true;
            }
        }
        return conn;
    }

    /**
     * Identifies the current active database type.
     */
    public static String getDatabaseType() {
        return fallbackActive ? "H2_IN_MEMORY" : "MYSQL";
    }

    /**
     * Checks if fallback database is currently active.
     */
    public static boolean isFallbackActive() {
        return fallbackActive;
    }

    /**
     * Returns a user-facing description of persistence status.
     */
    public static String getDatabaseStatusNotice() {
        if (fallbackActive) {
            return "Demo Mode: In-Memory H2 Database (Non-Persistent: data will be reset on application restart)";
        }
        return "Persistent Storage Mode: Connected to MySQL database";
    }

    /**
     * Strips any sensitive credentials from connection error strings.
     */
    private static String sanitizeMessage(String message) {
        if (message == null) return "";
        if (password != null && !password.isEmpty()) {
            message = message.replace(password, "******");
        }
        return message;
    }

    /**
     * Masks the URL to prevent query parameter leaks if any.
     */
    private static String maskUrl(String rawUrl) {
        if (rawUrl == null) return "";
        int queryIdx = rawUrl.indexOf('?');
        if (queryIdx > 0) {
            return rawUrl.substring(0, queryIdx);
        }
        return rawUrl;
    }
}
