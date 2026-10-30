package com.hospital.service;

import com.hospital.dao.PatientDAO;
import com.hospital.model.Patient;
import com.hospital.util.ValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end unit tests for Patient Registration flow,
 * input validation, BCrypt password hashing, and duplicate detection.
 */
public class RegistrationFlowTest {

    private AuthService authService;
    private PatientDAO patientDAO;

    @BeforeEach
    public void setUp() {
        patientDAO = new PatientDAO();
        authService = new AuthenticationServiceImpl(patientDAO);
    }

    @Test
    public void testPhoneValidationAcceptsInternationalFormat() {
        assertTrue(ValidationUtil.isValidPhone("+918789740826"));
        assertTrue(ValidationUtil.isValidPhone("9876543210"));
        assertTrue(ValidationUtil.isValidPhone("+12025550143"));
        assertTrue(ValidationUtil.isValidPhone("+91 87897 40826"));
        assertFalse(ValidationUtil.isValidPhone("12345")); // too short
        assertFalse(ValidationUtil.isValidPhone("abc8789740826")); // letters
        assertFalse(ValidationUtil.isValidPhone(null));
        assertFalse(ValidationUtil.isValidPhone(""));
    }

    @Test
    public void testEmailValidation() {
        assertTrue(ValidationUtil.isValidEmail("avinash@example.com"));
        assertTrue(ValidationUtil.isValidEmail("patient.kumar@hospital.org"));
        assertFalse(ValidationUtil.isValidEmail("avinashkumar46537@")); // Incomplete
        assertFalse(ValidationUtil.isValidEmail("missingdomain"));
        assertFalse(ValidationUtil.isValidEmail(null));
        assertFalse(ValidationUtil.isValidEmail(""));
    }

    @Test
    public void testCompletePatientRegistrationFlow() {
        String uniqueEmail = "patient_" + System.currentTimeMillis() + "@citycare.test";
        String rawPassword = "PatientSecure#2026";
        String phone = "+918789740826";

        // 1. Verify email does not exist initially
        assertFalse(authService.isEmailRegistered(uniqueEmail));

        // 2. Perform registration
        Patient patient = new Patient("Avinash Kumar", uniqueEmail, phone, null);
        boolean registered = authService.registerPatient(patient, rawPassword);
        assertTrue(registered, "Patient registration should succeed");

        // 3. Verify email is now registered
        assertTrue(authService.isEmailRegistered(uniqueEmail));

        // 4. Verify password was properly BCrypt hashed (not stored in plaintext)
        Patient retrieved = patientDAO.findByEmail(uniqueEmail);
        assertNotNull(retrieved);
        assertEquals("Avinash Kumar", retrieved.getName());
        assertEquals(uniqueEmail, retrieved.getEmail());
        assertNotEquals(rawPassword, retrieved.getPasswordHash(), "Password must be securely hashed with BCrypt");
        assertTrue(BCrypt.checkpw(rawPassword, retrieved.getPasswordHash()), "BCrypt hash must verify against raw password");

        // 5. Verify authentication succeeds with raw password
        Patient authenticated = authService.authenticatePatient(uniqueEmail, rawPassword);
        assertNotNull(authenticated);
        assertEquals(retrieved.getPatientId(), authenticated.getPatientId());

        // 6. Verify authentication fails with incorrect password
        assertNull(authService.authenticatePatient(uniqueEmail, "WrongPassword@123"));
    }
}

