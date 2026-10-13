<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Hospital System - JSP Portal</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f0f4f8; display: flex; justify-content: center; align-items: center; min-height: 80vh; margin: 0; }
        .card { background: white; padding: 35px; border-radius: 10px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); max-width: 450px; width: 100%; }
        h2 { color: #0284c7; margin-bottom: 15px; }
        .alert { background: #fee2e2; border-left: 4px solid #ef4444; color: #991b1b; padding: 10px 15px; border-radius: 4px; font-size: 13px; margin-bottom: 20px; }
        .info { background: #e0f2fe; border-left: 4px solid #0284c7; color: #0369a1; padding: 10px 15px; border-radius: 4px; font-size: 13px; margin-bottom: 20px; }
        .btn { display: block; text-align: center; background: #0284c7; color: white; padding: 12px; border-radius: 6px; text-decoration: none; font-weight: bold; margin-top: 15px; }
        .btn:hover { background: #0369a1; }
    </style>
</head>
<body>
    <div class="card">
        <h2>🏥 CityCare Hospital</h2>

        <% if ("unauthorized".equals(request.getParameter("error"))) { %>
            <div class="alert">
                <strong>Access Denied:</strong> You tried to access <code>dashboard.jsp</code> without an active session. Please log in first!
            </div>
        <% } %>

        <div class="info">
            <strong>Syllabus Note:</strong> Authentication is handled by <code>LoginServlet</code> and stored in <code>HttpSession</code>. You can log in via the React frontend or test credentials below.
        </div>

        <p style="font-size: 14px; color: #475569; line-height: 1.5;">
            To access the protected JSP dashboard, log in with your registered patient email and password on the React frontend.
        </p>

        <a href="http://localhost:5173" class="btn">Open React Portal</a>
    </div>
</body>
</html>

