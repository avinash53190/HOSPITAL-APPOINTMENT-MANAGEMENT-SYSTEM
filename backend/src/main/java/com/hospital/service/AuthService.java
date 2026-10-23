package com.hospital.service;

import com.hospital.model.Patient;

/**
 * Interface defining contract for User and Admin Authentication operations.
 * Demonstrates: Java interfaces, abstraction, and contract-driven design.
 */
public interface AuthService {
    /**
     * Checks if an email is already present in the database.
     */
    boolean isEmailRegistered(String email);

    /**
     * Registers a new patient with password hashing.
     */
    boolean registerPatient(Patient patient, String rawPassword);

    /**
     * Authenticates a patient by comparing plaintext password against stored BCrypt hash.
     * @return Patient object if valid, null otherwise
     */
    Patient authenticatePatient(String email, String rawPassword);

    /**
     * Authenticates administrator credentials.
     */
    boolean authenticateAdmin(String username, String password);

    /**
     * Checks if at least one administrator account exists in the system.
     */
    boolean hasAnyAdmin();

    /**
     * Sets up the initial administrator account. Allowed ONLY when no admin exists.
     */
    boolean setupInitialAdmin(String username, String rawPassword);
}
