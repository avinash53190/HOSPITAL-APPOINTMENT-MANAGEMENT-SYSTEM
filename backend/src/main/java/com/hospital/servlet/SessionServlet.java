package com.hospital.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hospital.dao.AppointmentDAO;
import com.hospital.util.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet to check active session state and database persistence status.
 * URL: /api/auth/me
 * Method: GET
 * React calls this on initial page load to verify if user has an active HttpSession
 * and to discover whether persistent MySQL or demo in-memory H2 is currently active.
 */
@WebServlet("/api/auth/me")
public class SessionServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        JsonObject res = new JsonObject();
        // Provide transparent database status
        res.addProperty("databaseType", DBConnection.getDatabaseType());
        res.addProperty("isPersistent", !DBConnection.isFallbackActive());
        res.addProperty("databaseNotice", DBConnection.getDatabaseStatusNotice());

        if (session == null || session.getAttribute("role") == null) {
            res.addProperty("authenticated", false);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(res));
            return;
        }

        String role = (String) session.getAttribute("role");
        res.addProperty("authenticated", true);
        res.addProperty("role", role);

        JsonObject userObj = new JsonObject();
        if ("ADMIN".equalsIgnoreCase(role)) {
            userObj.addProperty("name", "System Administrator");
            userObj.addProperty("email", (String) session.getAttribute("adminUser"));
            userObj.addProperty("role", "ADMIN");
        } else {
            int patientId = (Integer) session.getAttribute("patientId");
            userObj.addProperty("id", patientId);
            userObj.addProperty("name", (String) session.getAttribute("patientName"));
            userObj.addProperty("email", (String) session.getAttribute("patientEmail"));
            userObj.addProperty("role", "PATIENT");

            // Also provide total appointments count for dashboard
            int totalAppts = appointmentDAO.getAppointmentCountByPatient(patientId);
            res.addProperty("totalAppointments", totalAppts);
        }

        res.add("user", userObj);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(res));
    }
}
