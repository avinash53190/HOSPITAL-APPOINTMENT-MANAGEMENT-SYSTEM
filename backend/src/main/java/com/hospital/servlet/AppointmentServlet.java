package com.hospital.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hospital.dao.AppointmentDAO;
import com.hospital.model.Appointment;
import com.hospital.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

/**
 * Servlet managing Appointment booking, listing, and cancellation.
 * URL: /api/appointments
 * Methods: GET (view appointments), POST (book or cancel appointment)
 */
@WebServlet({"/api/appointments", "/api/appointments/cancel"})
public class AppointmentServlet extends HttpServlet {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("role") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"Please log in to view appointments.\"}");
            return;
        }

        String role = (String) session.getAttribute("role");

        if ("ADMIN".equalsIgnoreCase(role)) {
            // Admin sees all appointments across the hospital
            List<Appointment> allAppts = appointmentDAO.getAllAppointments();
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(allAppts));
        } else if ("PATIENT".equalsIgnoreCase(role)) {
            // Patient sees ONLY their own appointments
            int patientId = (Integer) session.getAttribute("patientId");
            List<Appointment> patientAppts = appointmentDAO.getAppointmentsByPatient(patientId);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(patientAppts));
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"success\":false,\"message\":\"Access forbidden.\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || !"PATIENT".equalsIgnoreCase((String) session.getAttribute("role"))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"Only authenticated patients can perform this operation.\"}");
            return;
        }

        int patientId = (Integer) session.getAttribute("patientId");
        String path = request.getServletPath();

        // Read request payload
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
            response.getWriter().write("{\"success\":false,\"message\":\"Invalid JSON request format.\"}");
            return;
        }

        if (json == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Missing request data.\"}");
            return;
        }

        // Branch 1: Cancel Appointment (/api/appointments/cancel)
        if ("/api/appointments/cancel".equals(path)) {
            if (!json.has("appointmentId")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"message\":\"Appointment ID is required.\"}");
                return;
            }
            int appointmentId = json.get("appointmentId").getAsInt();

            // Cancel appointment verifying ownership using both appointmentId AND patientId
            boolean cancelled = appointmentDAO.cancelAppointmentByPatient(appointmentId, patientId);
            if (cancelled) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"success\":true,\"message\":\"Appointment cancelled successfully.\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"message\":\"Could not cancel appointment. Either it was already cancelled or it belongs to another patient.\"}");
            }
            return;
        }

        // Branch 2: Book Appointment (/api/appointments)
        if (!json.has("doctorId") || !json.has("appointmentDate") || !json.has("appointmentTime")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Doctor, date, and time are required.\"}");
            return;
        }

        int doctorId = json.get("doctorId").getAsInt();
        String dateStr = json.get("appointmentDate").getAsString();
        String timeStr = json.get("appointmentTime").getAsString();

        // Validation 1: Prevent booking in the past
        if (!ValidationUtil.isFutureOrPresentDate(dateStr)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Cannot book appointments for past dates. Please select today or a future date.\"}");
            return;
        }

        if (ValidationUtil.isNullOrEmpty(timeStr)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Please select a valid appointment time slot.\"}");
            return;
        }

        Date apptDate = Date.valueOf(dateStr);

        // Validation 2: Prevent conflicting bookings for the same doctor and time
        if (appointmentDAO.isSlotTaken(doctorId, apptDate, timeStr)) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write("{\"success\":false,\"message\":\"This doctor is already booked for " + timeStr + " on " + dateStr + ". Please select another time slot.\"}");
            return;
        }

        // Save appointment
        Appointment appointment = new Appointment(patientId, doctorId, apptDate, timeStr, "CONFIRMED");
        boolean booked = appointmentDAO.bookAppointment(appointment);

        if (booked) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"success\":true,\"message\":\"Appointment booked successfully!\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Failed to book appointment due to database error.\"}");
        }
    }
}

