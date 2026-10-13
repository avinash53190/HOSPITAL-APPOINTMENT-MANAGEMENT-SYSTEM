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

        // Read JSON payload from request body
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

        String name = json.has("name") ? json.get("name").getAsString() : "";
        String email = json.has("email") ? json.get("email").getAsString() : "";
        String phone = json.has("phone") ? json.get("phone").getAsString() : "";
        String password = json.has("password") ? json.get("password").getAsString() : "";

        // Server-Side Input Validation
        if (ValidationUtil.isNullOrEmpty(name)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Full Name is required.\"}");
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Please enter a valid email address.\"}");
            return;
        }

        if (!ValidationUtil.isValidPhone(phone)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Phone number must be between 10 and 15 digits.\"}");
            return;
        }

        if (!ValidationUtil.isValidPassword(password)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Password must be at least 6 characters long.\"}");
            return;
        }

        // Check if email already exists
        if (authService.isEmailRegistered(email)) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write("{\"success\":false,\"message\":\"This email is already registered. Please login.\"}");
            return;
        }

        // Create Patient entity and save with hashed password
        Patient patient = new Patient(name, email, phone, null);
        boolean registered = authService.registerPatient(patient, password);

        if (registered) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"success\":true,\"message\":\"Registration successful! You can now log in.\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Registration failed due to a server error. Please try again.\"}");
        }
    }
}

