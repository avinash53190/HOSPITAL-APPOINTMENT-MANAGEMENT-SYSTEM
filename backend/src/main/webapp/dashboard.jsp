<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hospital.dao.AppointmentDAO" %>
<%@ page import="com.hospital.model.Appointment" %>
<%@ page import="com.hospital.util.ValidationUtil" %>
<%@ page import="java.util.List" %>
<%
    // Syllabus Demonstration: Session-based Authentication in JSP
    // Check if user has an active HttpSession with PATIENT role
    if (session == null || session.getAttribute("patientId") == null) {
        response.sendRedirect("http://localhost:5173/login?error=unauthorized");
        return;
    }

    int patientId = (Integer) session.getAttribute("patientId");
    String patientName = (String) session.getAttribute("patientName");
    String patientEmail = (String) session.getAttribute("patientEmail");

    // XSS Sanitization
    String safePatientName = ValidationUtil.escapeHtml(patientName);
    String safePatientEmail = ValidationUtil.escapeHtml(patientEmail);

    // Retrieve patient appointments via JDBC DAO
    AppointmentDAO appointmentDAO = new AppointmentDAO();
    List<Appointment> appointments = appointmentDAO.getAppointmentsByPatient(patientId);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Patient Dashboard — CityCare Hospital (JSP Session Protected)</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary: #123047;
            --secondary: #0D9488;
            --background: #F7F9FC;
            --text-main: #172B3A;
            --text-muted: #64748B;
            --border: #E5EAF0;
            --success: #15803D;
            --error: #DC2626;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
        body { background-color: var(--background); color: var(--text-main); line-height: 1.5; padding-bottom: 60px; }
        header { background: #FFFFFF; border-bottom: 1px solid var(--border); padding: 16px 36px; display: flex; justify-content: space-between; align-items: center; position: sticky; top: 0; z-index: 20; }
        .logo-group { display: flex; align-items: center; gap: 12px; }
        .logo-icon { width: 36px; height: 36px; background: var(--secondary); color: white; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 20px; }
        .logo-title { font-weight: 700; font-size: 18px; color: var(--primary); }
        .logo-subtitle { font-size: 11px; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); font-weight: 600; }
        .nav-links { display: flex; align-items: center; gap: 16px; }
        .user-pill { background: #F1F5F9; padding: 6px 14px; border-radius: 20px; font-size: 13px; font-weight: 500; color: var(--primary); }
        .btn { padding: 8px 18px; border-radius: 8px; font-weight: 600; font-size: 13px; text-decoration: none; display: inline-flex; align-items: center; cursor: pointer; transition: all 0.2s; border: none; }
        .btn-outline { background: white; border: 1px solid var(--border); color: var(--text-main); }
        .btn-outline:hover { background: #F8FAFC; border-color: #CBD5E1; }
        .btn-danger { background: #FEE2E2; color: var(--error); border: 1px solid #FECACA; }
        .btn-danger:hover { background: #FECDCD; }
        .container { max-width: 1040px; margin: 36px auto; padding: 0 24px; }
        .syllabus-callout { background: #F0FDFA; border: 1px solid #99F6E4; border-left: 4px solid var(--secondary); border-radius: 8px; padding: 16px 20px; margin-bottom: 28px; }
        .syllabus-callout h4 { font-size: 14px; font-weight: 700; color: #0F766E; margin-bottom: 4px; }
        .syllabus-callout p { font-size: 13px; color: #115E59; }
        .syllabus-callout code { background: #CCFBF1; padding: 2px 6px; border-radius: 4px; font-family: monospace; font-size: 12px; }
        .welcome-card { background: #FFFFFF; border: 1px solid var(--border); border-radius: 12px; padding: 28px; margin-bottom: 28px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 1px 3px rgba(0,0,0,0.02); }
        .welcome-card h2 { font-size: 24px; font-weight: 700; color: var(--primary); margin-bottom: 6px; }
        .welcome-card p { font-size: 14px; color: var(--text-muted); }
        .stat-card { background: #F0FDF4; border: 1px solid #BBF7D0; border-radius: 10px; padding: 16px 24px; text-align: center; min-width: 140px; }
        .stat-number { font-size: 30px; font-weight: 700; color: var(--success); }
        .stat-label { font-size: 12px; font-weight: 600; color: #166534; text-transform: uppercase; letter-spacing: 0.05em; }
        .section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
        .section-header h3 { font-size: 18px; font-weight: 700; color: var(--primary); }
        .table-wrap { background: #FFFFFF; border: 1px solid var(--border); border-radius: 12px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.02); }
        table { width: 100%; border-collapse: collapse; text-align: left; }
        th { background: #F8FAFC; color: var(--text-muted); font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; padding: 14px 20px; border-bottom: 1px solid var(--border); }
        td { padding: 16px 20px; font-size: 14px; border-bottom: 1px solid var(--border); color: var(--text-main); }
        tr:last-child td { border-bottom: none; }
        .badge { display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 9999px; font-size: 12px; font-weight: 600; }
        .badge-confirmed { background: #DCFCE7; color: var(--success); }
        .badge-cancelled { background: #FEE2E2; color: var(--error); }
        .empty-state { padding: 48px 24px; text-align: center; color: var(--text-muted); }
        .empty-state p { margin-bottom: 16px; font-size: 14px; }
    </style>
</head>
<body>

    <header>
        <div class="logo-group">
            <div class="logo-icon">+</div>
            <div>
                <div class="logo-title">CityCare</div>
                <div class="logo-subtitle">Healthcare &bull; JSP Session Portal</div>
            </div>
        </div>
        <div class="nav-links">
            <span class="user-pill"><%= safePatientName %> (Patient #<%= patientId %>)</span>
            <a href="http://localhost:5173" class="btn btn-outline">&larr; Return to React App</a>
            <a href="api/logout" class="btn btn-danger">Logout (Invalidate Session)</a>
        </div>
    </header>

    <div class="container">
        <!-- Syllabus Demonstration Callout -->
        <div class="syllabus-callout">
            <h4>Academic Practical Syllabus Demonstration:</h4>
            <p>
                This protected dashboard is rendered natively by the <strong>Apache Tomcat JSP Engine</strong> using <code>javax.servlet.http.HttpSession</code>.
                Active Session ID: <code><%= session.getId() %></code> | Timeout: <code><%= session.getMaxInactiveInterval() %>s</code>
            </p>
        </div>

        <!-- Welcome Summary Card -->
        <div class="welcome-card">
            <div>
                <h2>Welcome, <%= safePatientName %></h2>
                <p>Email: <%= safePatientEmail %> &bull; Patient ID: #<%= patientId %></p>
            </div>
            <div class="stat-card">
                <div class="stat-number"><%= appointments.size() %></div>
                <div class="stat-label">Total Bookings</div>
            </div>
        </div>

        <!-- Appointments Table -->
        <div class="section-header">
            <h3>My Booked Appointments</h3>
        </div>

        <div class="table-wrap">
            <% if (appointments.isEmpty()) { %>
                <div class="empty-state">
                    <p>You have not booked any appointments yet.</p>
                    <a href="http://localhost:5173/book" class="btn btn-outline">Book an Appointment</a>
                </div>
            <% } else { %>
                <table>
                    <thead>
                        <tr>
                            <th>Booking ID</th>
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
                                <td><strong>#<%= a.getAppointmentId() %></strong></td>
                                <td><%= ValidationUtil.escapeHtml(a.getDoctorName()) %></td>
                                <td><%= ValidationUtil.escapeHtml(a.getDoctorSpecialization()) %></td>
                                <td><%= a.getAppointmentDate() %></td>
                                <td><%= ValidationUtil.escapeHtml(a.getAppointmentTime()) %></td>
                                <td>
                                    <% if ("CONFIRMED".equalsIgnoreCase(a.getStatus())) { %>
                                        <span class="badge badge-confirmed">&bull; Confirmed</span>
                                    <% } else { %>
                                        <span class="badge badge-cancelled">&bull; Cancelled</span>
                                    <% } %>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            <% } %>
        </div>
    </div>

</body>
</html>
