package com.hospital.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Authentication service and BCrypt security verification.
 */
public class AuthenticationServiceTest {

    private AuthService authService;

    @BeforeEach
    public void setUp() {
        authService = new AuthenticationServiceImpl();
    }

    @Test
    public void testBCryptHashingProducesDifferentSalts() {
        String raw = "StudentPass@2026";
        String hash1 = BCrypt.hashpw(raw, BCrypt.gensalt(10));
        String hash2 = BCrypt.hashpw(raw, BCrypt.gensalt(10));

        assertNotEquals(hash1, hash2, "BCrypt must generate unique salts per hash invocation");
        assertTrue(BCrypt.checkpw(raw, hash1));
        assertTrue(BCrypt.checkpw(raw, hash2));
        assertFalse(BCrypt.checkpw("WrongPassword", hash1));
    }

    @Test
    public void testAdminAuthentication() {
        // Default seeded admin credentials: admin / admin123
        boolean valid = authService.authenticateAdmin("admin", "admin123");
        assertTrue(valid, "Admin with valid credentials must authenticate successfully");

        boolean invalidPassword = authService.authenticateAdmin("admin", "wrongpassword");
        assertFalse(invalidPassword, "Admin with invalid password must fail authentication");

        boolean invalidUser = authService.authenticateAdmin("unknown_admin", "admin123");
        assertFalse(invalidUser, "Non-existent admin user must fail authentication");
    }

    @Test
    public void testNullCredentialsSafety() {
        assertFalse(authService.authenticateAdmin(null, null));
        assertFalse(authService.authenticateAdmin("admin", null));
        assertNull(authService.authenticatePatient(null, "pass"));
        assertNull(authService.authenticatePatient("patient@test.com", null));
    }
}

