package com.hospital.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hospital.dao.DoctorDAO;
import com.hospital.model.Doctor;
import com.hospital.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servlet managing Doctor listings and Admin doctor additions.
 * URL: /api/doctors
 * Methods: GET (public/authenticated), POST (admin only)
 */
@WebServlet("/api/doctors")
public class DoctorServlet extends HttpServlet {

    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        List<Doctor> doctors = doctorDAO.getAllDoctors();

        // Optional filtering by search query or specialization
        String query = request.getParameter("search");
        String specialization = request.getParameter("specialization");

        if (!ValidationUtil.isNullOrEmpty(query)) {
            String q = query.trim().toLowerCase();
            doctors = doctors.stream()
                    .filter(d -> d.getName().toLowerCase().contains(q) || d.getSpecialization().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }

        if (!ValidationUtil.isNullOrEmpty(specialization) && !"ALL".equalsIgnoreCase(specialization)) {
            String spec = specialization.trim().toLowerCase();
            doctors = doctors.stream()
                    .filter(d -> d.getSpecialization().toLowerCase().equalsIgnoreCase(spec))
                    .collect(Collectors.toList());
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(doctors));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Enforce Admin Authentication
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equalsIgnoreCase((String) session.getAttribute("role"))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"success\":false,\"message\":\"Access denied. Administrator privileges required.\"}");
            return;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        JsonObject json = gson.fromJson(sb.toString(), JsonObject.class);
        if (json == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Missing doctor details.\"}");
            return;
        }

        String name = json.has("name") ? json.get("name").getAsString() : "";
        String specialization = json.has("specialization") ? json.get("specialization").getAsString() : "";
        boolean available = !json.has("available") || json.get("available").getAsBoolean();

        if (ValidationUtil.isNullOrEmpty(name) || ValidationUtil.isNullOrEmpty(specialization)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Doctor name and specialization are required.\"}");
            return;
        }

        Doctor doctor = new Doctor(name, specialization, available);
        boolean added = doctorDAO.addDoctor(doctor);

        if (added) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"success\":true,\"message\":\"Doctor added successfully.\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Failed to add doctor.\"}");
        }
    }
}

