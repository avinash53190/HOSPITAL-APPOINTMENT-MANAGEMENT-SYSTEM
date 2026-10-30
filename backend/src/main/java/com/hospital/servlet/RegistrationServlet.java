package com.hospital.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hospital.model.Patient;
import com.hospital.service.AuthService;
import com.hospital.service.AuthenticationServiceImpl;
import com.hospital.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Servlet handling Patient Registration.
 * URL: /api/register
 * Method: POST
 * Enforces server-side validation, BCrypt password hashing, and returns
 * structured JSON responses with appropriate HTTP status codes.
 */
@WebServlet("/api/register")
public class RegistrationServlet extends HttpServlet {

    private final AuthService authService = new AuthenticationServiceImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Read JSON payload from request body
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            String body = sb.toString().trim();
            if (body.isEmpty()) {
                sendJson(response, HttpServletResponse.SC_BAD_REQUEST, false, "Request body cannot be empty.");
                return;
            }

            JsonObject json;
            try {
                json = gson.fromJson(body, JsonObject.class);
            } catch (Exception e) {
                sendJson(response, HttpServletResponse.SC_BAD_REQUEST, false, "Invalid JSON payload.");
                return;
            }

            if (json == null) {
                sendJson(response, HttpServletResponse.SC_BAD_REQUEST, false, "Missing registration payload data.");
                return;
            }

            // Safe extraction of fields to prevent null pointer or JSON type exceptions
            String name = getSafeString(json, "name");
            String email = getSafeString(json, "email");
            String phone = getSafeString(json, "phone");
            String password = getSafeString(json, "password");

            // Diagnostic logging (safe: never logs passwords or secrets)
            System.out.println("[RegistrationServlet] Processing registration request for email: " + sanitizeForLog(email));

            // Server-Side Input Validation
            if (ValidationUtil.isNullOrEmpty(name)) {
                sendJson(response, HttpServletResponse.SC_BAD_REQUEST, false, "Full Name is required.");
                return;
            }

            if (!ValidationUtil.isValidEmail(email)) {
                sendJson(response, HttpServletResponse.SC_BAD_REQUEST, false, "Please enter a valid email address (e.g., user@example.com).");
                return;
            }

            if (!ValidationUtil.isValidPhone(phone)) {
                sendJson(response, HttpServletResponse.SC_BAD_REQUEST, false, "Phone number must contain 10 to 15 digits (e.g. +918789740826).");
                return;
            }

            if (!ValidationUtil.isValidPassword(password)) {
                sendJson(response, HttpServletResponse.SC_BAD_REQUEST, false, "Password must be at least 6 characters long.");
                return;
            }

            // Check if email is already registered
            if (authService.isEmailRegistered(email)) {
                System.out.println("[RegistrationServlet] Registration rejected: Email already exists: " + sanitizeForLog(email));
                sendJson(response, HttpServletResponse.SC_CONFLICT, false, "This email address is already registered. Please sign in.");
                return;
            }

            // Create Patient entity and save with hashed password
            Patient patient = new Patient(name, email, phone, null);
            boolean registered = authService.registerPatient(patient, password);

            if (registered) {
                System.out.println("[RegistrationServlet] Registration successful for email: " + sanitizeForLog(email));
                sendJson(response, HttpServletResponse.SC_CREATED, true, "Registration successful! You can now log in.");
            } else {
                System.err.println("[RegistrationServlet] Registration failed to persist for email: " + sanitizeForLog(email));
                sendJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, false, "Unable to save registration. Please try again or check database connectivity.");
            }
        } catch (Throwable t) {
            // Guarantee that even unexpected exceptions return a well-formed JSON 500 response
            System.err.println("[RegistrationServlet] Unexpected error during registration: " + t.getClass().getSimpleName() + " - " + t.getMessage());
            sendJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, false, "An unexpected server error occurred. Please try again.");
        }
    }

    private void sendJson(HttpServletResponse response, int statusCode, boolean success, String message) throws IOException {
        response.setStatus(statusCode);
        JsonObject obj = new JsonObject();
        obj.addProperty("success", success);
        obj.addProperty("message", message);
        response.getWriter().write(gson.toJson(obj));
    }

    private String getSafeString(JsonObject json, String field) {
        if (json.has(field) && !json.get(field).isJsonNull()) {
            return json.get(field).getAsString().trim();
        }
        return "";
    }

    private String sanitizeForLog(String val) {
        if (val == null) return "<null>";
        // Truncate or remove newlines to prevent log injection
        return val.replaceAll("[\\r\\n]", "").trim();
    }
}
