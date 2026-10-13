<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hospital.dao.AppointmentDAO" %>
<%@ page import="com.hospital.model.Appointment" %>
<%@ page import="java.util.List" %>
<%
    // Syllabus Demonstration: Session-based Authentication in JSP
    // Check if user has an active HttpSession with PATIENT role
    if (session == null || session.getAttribute("patientId") == null) {
        // Unauthenticated access attempt: Redirect to login or show notice
        response.sendRedirect("login.jsp?error=unauthorized");
        return;
    }

    int patientId = (Integer) session.getAttribute("patientId");
    String patientName = (String) session.getAttribute("patientName");
    String patientEmail = (String) session.getAttribute("patientEmail");

    // Retrieve patient appointments via JDBC DAO
    AppointmentDAO appointmentDAO = new AppointmentDAO();
    List<Appointment> appointments = appointmentDAO.getAppointmentsByPatient(patientId);
%>
<!DOCTYPE html>
<html>
<head>
    <title>JSP Protected Patient Dashboard</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
        body { background-color: #f8fafc; color: #1e293b; padding-bottom: 50px; }
        header { background: linear-gradient(135deg, #0284c7, #0f766e); color: white; padding: 20px 40px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }
        .logo { font-size: 22px; font-weight: bold; }
        .nav-links a { color: white; text-decoration: none; margin-left: 20px; font-weight: 500; }
        .btn-logout { background: #ef4444; color: white; padding: 8px 16px; border-radius: 6px; text-decoration: none; font-weight: bold; }
        .container { max-width: 1000px; margin: 30px auto; padding: 0 20px; }
        .syllabus-badge { background: #e0f2fe; border-left: 4px solid #0284c7; padding: 12px 18px; margin-bottom: 25px; border-radius: 4px; font-size: 14px; }
        .welcome-card { background: white; border-radius: 10px; padding: 25px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); margin-bottom: 25px; display: flex; justify-content: space-between; align-items: center; }
        .stat-box { background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 8px; padding: 15px 25px; text-align: center; }
        .stat-num { font-size: 28px; font-weight: bold; color: #15803d; }
        .section-title { font-size: 20px; margin-bottom: 15px; color: #0f172a; }
        table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
        th, td { padding: 14px 18px; text-align: left; border-bottom: 1px solid #e2e8f0; font-size: 14px; }
        th { background: #f1f5f9; color: #475569; font-weight: 600; }
        .badge { display: inline-block; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: bold; }
        .badge-confirmed { background: #dcfce7; color: #166534; }
        .badge-cancelled { background: #fee2e2; color: #991b1b; }
        .empty-state { text-align: center; padding: 40px; color: #64748b; background: white; border-radius: 8px; }
    </style>
</head>
<body>

    <header>
        <div class="logo">🏥 CityCare Hospital — JSP Demonstration Dashboard</div>
        <div class="nav-links">
            <span>Logged in as: <strong><%= patientName %></strong></span>
            <a href="api/logout" class="btn-logout">Logout (Invalidate Session)</a>
        </div>
    </header>

    <div class="container">
        <!-- Syllabus requirement notice -->
        <div class="syllabus-badge">
            <strong>🎓 MCA Practical Syllabus Demonstration:</strong>
            This page is rendered directly by <strong>Apache Tomcat JSP Engine</strong> using <code>javax.servlet.http.HttpSession</code>.
            Session ID: <code><%= session.getId() %></code> | Timeout: <code><%= session.getMaxInactiveInterval() %>s</code>
        </div>

        <!-- Welcome Summary Card -->
        <div class="welcome-card">
            <div>
                <h2>Hello, <%= patientName %>!</h2>
                <p style="color: #64748b; margin-top: 5px;">Patient ID: #<%= patientId %> | Registered Email: <%= patientEmail %></p>
            </div>
            <div class="stat-box">
                <div class="stat-num"><%= appointments.size() %></div>
                <div style="font-size: 12px; color: #15803d; font-weight: bold;">Total Appointments</div>
            </div>
        </div>

        <!-- Appointments Table -->
        <h3 class="section-title">My Booked Appointments</h3>
        <% if (appointments.isEmpty()) { %>
            <div class="empty-state">
                <p>No appointments booked yet.</p>
                <p style="margin-top: 8px;"><a href="http://localhost:5173" style="color: #0284c7;">Go to React Frontend</a> to book your first doctor appointment!</p>
            </div>
        <% } else { %>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Doctor</th>
                        <th>Specialization</th>
                        <th>Date</th>
                        <th>Time</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Appointment a : appointments) { %>
                        <tr>
                            <td>#<%= a.getAppointmentId() %></td>
                            <td><strong><%= a.getDoctorName() %></strong></td>
                            <td><%= a.getDoctorSpecialization() %></td>
                            <td><%= a.getAppointmentDate() %></td>
                            <td><%= a.getAppointmentTime() %></td>
                            <td>
                                <% if ("CONFIRMED".equalsIgnoreCase(a.getStatus())) { %>
                                    <span class="badge badge-confirmed">Confirmed</span>
                                <% } else { %>
                                    <span class="badge badge-cancelled">Cancelled</span>
                                <% } %>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        <% } %>
    </div>

</body>
</html>

