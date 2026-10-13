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
 * Servlet handling User and Admin Login.
 * URL: /api/login
 * Method: POST
 * Manages user authentication and creates HttpSession.
 */
@WebServlet("/api/login")
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
            response.getWriter().write("{\"success\":false,\"message\":\"Missing login credentials.\"}");
            return;
        }

        String email = json.has("email") ? json.get("email").getAsString() : "";
        String password = json.has("password") ? json.get("password").getAsString() : "";
        String role = json.has("role") ? json.get("role").getAsString() : "PATIENT";

        if (ValidationUtil.isNullOrEmpty(email) || ValidationUtil.isNullOrEmpty(password)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Email and password are required.\"}");
            return;
        }

        // 1. Admin Login Branch
        if ("ADMIN".equalsIgnoreCase(role)) {
            boolean isAdminValid = authService.authenticateAdmin(email, password);
            if (isAdminValid) {
                // Invalidate any previous session and create a fresh one
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
                userObj.addProperty("name", "System Administrator");
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

        // 2. Patient Login Branch
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

