package com.hospital.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet handling Logout and Session Invalidation.
 * URL: /api/logout
 * Methods: POST, GET
 */
@WebServlet("/api/logout")
public class LogoutServlet extends HttpServlet {

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null) {
            // Invalidate the session on the Java server
            session.invalidate();
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("{\"success\":true,\"message\":\"Logged out successfully and session invalidated.\"}");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleLogout(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleLogout(request, response);
    }
}

