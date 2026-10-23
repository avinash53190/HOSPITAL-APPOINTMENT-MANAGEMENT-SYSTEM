package com.hospital.service;

import com.hospital.dao.AdminDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Patient;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Service implementation for Authentication operations.
 * Implements AuthService interface to demonstrate OOP Polymorphism and Abstraction.
 * Enforces secure BCrypt password hashing for both Patients and Administrators.
 */
public class AuthenticationServiceImpl implements AuthService {

    private final PatientDAO patientDAO;
    private final AdminDAO adminDAO;

    public AuthenticationServiceImpl() {
        this.patientDAO = new PatientDAO();
        this.adminDAO = new AdminDAO();
    }

    // Constructor injection support
    public AuthenticationServiceImpl(PatientDAO patientDAO) {
        this.patientDAO = patientDAO;
        this.adminDAO = new AdminDAO();
    }

    public AuthenticationServiceImpl(PatientDAO patientDAO, AdminDAO adminDAO) {
        this.patientDAO = patientDAO;
        this.adminDAO = adminDAO;
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
        if (email == null || rawPassword == null) {
            return null;
        }
        Patient patient = patientDAO.findByEmail(email);
        if (patient == null) {
            return null;
        }
        // Verify plaintext password against the stored BCrypt hash
        if (BCrypt.checkpw(rawPassword, patient.getPasswordHash())) {
            return patient;
        }
        return null;
    }

    @Override
    public boolean authenticateAdmin(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        // Authenticates admin using BCrypt hash comparison against database
        return adminDAO.authenticate(username, password);
    }
}
