package com.hospital.service;

import com.hospital.dao.PatientDAO;
import com.hospital.model.Patient;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Service implementation for Authentication operations.
 * Implements AuthService interface to demonstrate OOP Polymorphism and Abstraction.
 */
public class AuthenticationServiceImpl implements AuthService {

    private final PatientDAO patientDAO;

    public AuthenticationServiceImpl() {
        this.patientDAO = new PatientDAO();
    }

    // Constructor injection support
    public AuthenticationServiceImpl(PatientDAO patientDAO) {
        this.patientDAO = patientDAO;
    }

    @Override
    public boolean isEmailRegistered(String email) {
        return patientDAO.emailExists(email);
    }

    @Override
    public boolean registerPatient(Patient patient, String rawPassword) {
        // Hash password securely using BCrypt with automatic salt generation
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
        patient.setPasswordHash(hashedPassword);
        return patientDAO.save(patient);
    }

    @Override
    public Patient authenticatePatient(String email, String rawPassword) {
        Patient patient = patientDAO.findByEmail(email);
        if (patient == null) {
            return null;
        }
        // Verify plaintext password against the BCrypt hash
        if (BCrypt.checkpw(rawPassword, patient.getPasswordHash())) {
            return patient;
        }
        return null;
    }

    @Override
    public boolean authenticateAdmin(String username, String password) {
        // Simple college project admin credentials
        return "admin".equalsIgnoreCase(username) && "admin123".equals(password);
    }
}

