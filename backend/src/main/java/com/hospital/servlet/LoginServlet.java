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
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Servlet handling User Login and Safe Initial Administrator Setup.
 * URLs: /api/login, /api/admin/setup
 * Method: POST
 * Manages user authentication, session creation, and secure first-time admin setup.
 * Enforces server-side validation, BCrypt password hashing, and returns
 * structured JSON responses with appropriate HTTP status codes.
 */
@WebServlet({"/api/login", "/api/admin/setup"})
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthenticationServiceImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
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
                sendJson(response, HttpServletResponse.SC_BAD_REQUEST, false, "Invalid JSON request body.");
                return;
            }

            if (json == null) {
                sendJson(response, HttpServletResponse.SC_BAD_REQUEST, false, "Missing request data.");
                return;
            }

            String path = request.getServletPath();

            // Branch 1: Safe First-Time Administrator Setup (/api/admin/setup)
            if ("/api/admin/setup".equals(path)) {
                handleAdminSetup(json, response);
                return;
            }

            // Branch 2: Standard Login (/api/login)
            handleLogin(request, response, json);

        } catch (Throwable t) {
            System.err.println("[LoginServlet] Internal error during authentication: " + t.getClass().getSimpleName() + " - " + t.getMessage());
            sendJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, false, "An unexpected server error occurred during authentication.");
        }
    }

    private void handleAdminSetup(JsonObject json, HttpServletResponse response) throws IOException {
        if (authService.hasAnyAdmin()) {
            sendJson(response, HttpServletResponse.SC_FORBIDDEN, false, "Administrator account is already initialized. Initial setup is locked.");
            return;
        }

        String adminUser = getSafeString(json, "username");
        String adminPass = getSafeString(json, "password");

        if (ValidationUtil.isNullOrEmpty(adminUser) || ValidationUtil.isNullOrEmpty(adminPass)) {
            sendJson(response, HttpServletResponse.SC_BAD_REQUEST, false, "Administrator username and password are required.");
            return;
        }

        if (!ValidationUtil.isValidPassword(adminPass)) {
            sendJson(response, HttpServletResponse.SC_BAD_REQUEST, false, "Administrator password must be at least 6 characters.");
            return;
        }

        System.out.println("[LoginServlet] Setting up initial administrator: " + sanitizeForLog(adminUser));
        boolean setupSuccess = authService.setupInitialAdmin(adminUser, adminPass);
        if (setupSuccess) {
            sendJson(response, HttpServletResponse.SC_CREATED, true, "Initial administrator created successfully with BCrypt encryption. You can now log in.");
        } else {
            sendJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, false, "Failed to create administrator account. Please check database connectivity.");
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response, JsonObject json) throws IOException {
        String email = getSafeString(json, "email");
        String password = getSafeString(json, "password");
        String role = getSafeString(json, "role");
        if (role.isEmpty()) role = "PATIENT";

        System.out.println("[LoginServlet] Login attempt for user: " + sanitizeForLog(email) + " (Role: " + role + ")");

        if (ValidationUtil.isNullOrEmpty(email) || ValidationUtil.isNullOrEmpty(password)) {
            sendJson(response, HttpServletResponse.SC_BAD_REQUEST, false, "Email/username and password are required.");
            return;
        }

        // Admin Login Branch
        if ("ADMIN".equalsIgnoreCase(role)) {
            if (!authService.hasAnyAdmin()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject res = new JsonObject();
                res.addProperty("success", false);
                res.addProperty("needsInitialSetup", true);
                res.addProperty("message", "No administrator account exists yet. Configure the ADMIN_PASSWORD environment variable or initialize the first administrator.");
                response.getWriter().write(gson.toJson(res));
                return;
            }

            boolean isAdminValid = authService.authenticateAdmin(email, password);
            if (isAdminValid) {
                HttpSession oldSession = request.getSession(false);
                if (oldSession != null) oldSession.invalidate();

                HttpSession session = request.getSession(true);
                session.setAttribute("role", "ADMIN");
                session.setAttribute("adminUser", email);

                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.addProperty("role", "ADMIN");
                res.addProperty("message", "Admin login successful.");

                JsonObject userObj = new JsonObject();
                userObj.addProperty("name", "System Administrator (" + email + ")");
                userObj.addProperty("email", email);
                userObj.addProperty("role", "ADMIN");
                res.add("user", userObj);

                System.out.println("[LoginServlet] Administrator authenticated successfully: " + sanitizeForLog(email));
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(res));
            } else {
                System.out.println("[LoginServlet] Administrator authentication failed for: " + sanitizeForLog(email));
                sendJson(response, HttpServletResponse.SC_UNAUTHORIZED, false, "Invalid Administrator credentials.");
            }
            return;
        }

        // Patient Login Branch
        Patient patient = authService.authenticatePatient(email, password);
        if (patient != null) {
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) oldSession.invalidate();

            HttpSession session = request.getSession(true);
            session.setAttribute("patientId", patient.getPatientId());
            session.setAttribute("patientName", patient.getName());
            session.setAttribute("patientEmail", patient.getEmail());
            session.setAttribute("role", "PATIENT");

            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("role", "PATIENT");
            res.addProperty("message", "Login successful.");

            JsonObject userObj = new JsonObject();
            userObj.addProperty("id", patient.getPatientId());
            userObj.addProperty("name", patient.getName());
            userObj.addProperty("email", patient.getEmail());
            userObj.addProperty("phone", patient.getPhone());
            userObj.addProperty("role", "PATIENT");
            res.add("user", userObj);

            System.out.println("[LoginServlet] Patient authenticated successfully: " + sanitizeForLog(email));
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(res));
        } else {
            System.out.println("[LoginServlet] Patient authentication failed for: " + sanitizeForLog(email));
            sendJson(response, HttpServletResponse.SC_UNAUTHORIZED, false, "Invalid email or password.");
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
        return val.replaceAll("[\\r\\n]", "").trim();
    }
}
