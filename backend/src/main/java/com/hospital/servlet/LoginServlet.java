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

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        JsonObject json;
        try {
            json = gson.fromJson(sb.toString(), JsonObject.class);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Invalid JSON request body.\"}");
            return;
        }

        if (json == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Missing request data.\"}");
            return;
        }

        String path = request.getServletPath();

        // Branch 1: Safe First-Time Administrator Setup (/api/admin/setup)
        if ("/api/admin/setup".equals(path)) {
            if (authService.hasAnyAdmin()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"success\":false,\"message\":\"Administrator account is already initialized. Initial setup is locked.\"}");
                return;
            }

            String adminUser = json.has("username") ? json.get("username").getAsString() : "";
            String adminPass = json.has("password") ? json.get("password").getAsString() : "";

            if (ValidationUtil.isNullOrEmpty(adminUser) || ValidationUtil.isNullOrEmpty(adminPass)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"message\":\"Administrator username and password are required.\"}");
                return;
            }

            if (!ValidationUtil.isValidPassword(adminPass)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"message\":\"Administrator password must be at least 6 characters.\"}");
                return;
            }

            boolean setupSuccess = authService.setupInitialAdmin(adminUser.trim(), adminPass.trim());
            if (setupSuccess) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write("{\"success\":true,\"message\":\"Initial administrator created successfully with BCrypt encryption. You can now log in.\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"success\":false,\"message\":\"Failed to create administrator account.\"}");
            }
            return;
        }

        // Branch 2: Standard Login (/api/login)
        String email = json.has("email") ? json.get("email").getAsString() : "";
        String password = json.has("password") ? json.get("password").getAsString() : "";
        String role = json.has("role") ? json.get("role").getAsString() : "PATIENT";

        if (ValidationUtil.isNullOrEmpty(email) || ValidationUtil.isNullOrEmpty(password)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Email/username and password are required.\"}");
            return;
        }

        // Admin Login Branch
        if ("ADMIN".equalsIgnoreCase(role)) {
            if (!authService.hasAnyAdmin()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"needsInitialSetup\":true,\"message\":\"No administrator account exists yet. Configure the ADMIN_PASSWORD environment variable or initialize the first administrator.\"}");
                return;
            }

            boolean isAdminValid = authService.authenticateAdmin(email, password);
            if (isAdminValid) {
                // Invalidate any previous session and create a fresh one to prevent session fixation
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

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(res));
                return;
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"success\":false,\"message\":\"Invalid Administrator credentials.\"}");
                return;
            }
        }

        // Patient Login Branch
        Patient patient = authService.authenticatePatient(email, password);
        if (patient != null) {
            // Invalidate any previous session and create a fresh one
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

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(res));
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"Invalid email or password.\"}");
        }
    }
}
