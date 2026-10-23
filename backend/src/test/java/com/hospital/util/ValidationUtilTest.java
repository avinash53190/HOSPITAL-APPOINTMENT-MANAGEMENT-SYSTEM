package com.hospital.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationUtil.
 * Demonstrates: Server-side validation testing, boundary conditions, and security sanitization.
 */
public class ValidationUtilTest {

    @Test
    public void testValidEmails() {
        assertTrue(ValidationUtil.isValidEmail("patient@hospital.com"));
        assertTrue(ValidationUtil.isValidEmail("john.doe123@example.org"));
        assertTrue(ValidationUtil.isValidEmail("care+vip@citycare.in"));
    }

    @Test
    public void testInvalidEmails() {
        assertFalse(ValidationUtil.isValidEmail(null));
        assertFalse(ValidationUtil.isValidEmail(""));
        assertFalse(ValidationUtil.isValidEmail("plainaddress"));
        assertFalse(ValidationUtil.isValidEmail("@missingusername.com"));
        assertFalse(ValidationUtil.isValidEmail("missingdomain@"));
    }

    @Test
    public void testValidPhoneNumbers() {
        assertTrue(ValidationUtil.isValidPhone("9876543210"));
        assertTrue(ValidationUtil.isValidPhone("123456789012"));
    }

    @Test
    public void testInvalidPhoneNumbers() {
        assertFalse(ValidationUtil.isValidPhone(null));
        assertFalse(ValidationUtil.isValidPhone("123"));
        assertFalse(ValidationUtil.isValidPhone("abcdefghij"));
        assertFalse(ValidationUtil.isValidPhone("123456789012345678"));
    }

    @Test
    public void testPasswordValidation() {
        assertTrue(ValidationUtil.isValidPassword("secret123"));
        assertTrue(ValidationUtil.isValidPassword("123456"));
        assertFalse(ValidationUtil.isValidPassword("12345"));
        assertFalse(ValidationUtil.isValidPassword(null));
    }

    @Test
    public void testFutureOrPresentDate() {
        String today = LocalDate.now().toString();
        String tomorrow = LocalDate.now().plusDays(1).toString();
        String yesterday = LocalDate.now().minusDays(1).toString();

        assertTrue(ValidationUtil.isFutureOrPresentDate(today));
        assertTrue(ValidationUtil.isFutureOrPresentDate(tomorrow));
        assertFalse(ValidationUtil.isFutureOrPresentDate(yesterday));
        assertFalse(ValidationUtil.isFutureOrPresentDate("invalid-date"));
        assertFalse(ValidationUtil.isFutureOrPresentDate(null));
    }

    @Test
    public void testHtmlEscapingForXss() {
        String unsafe = "<script>alert('xss')</script>";
        String safe = ValidationUtil.escapeHtml(unsafe);
        assertEquals("&lt;script&gt;alert(&#x27;xss&#x27;)&lt;/script&gt;", safe);

        assertEquals("&quot;hello&quot; &amp; welcome", ValidationUtil.escapeHtml("\"hello\" & welcome"));
        assertEquals("", ValidationUtil.escapeHtml(null));
    }
}

