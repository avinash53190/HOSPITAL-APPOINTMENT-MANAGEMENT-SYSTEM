@echo off
title Launch CityCare Hospital System
echo ========================================================
echo  Starting Hospital Appointment Management System
echo  1. Starting Java Tomcat Backend (Port 8080)
echo  2. Starting React Vite Frontend (Port 5173)
echo ========================================================

start "Hospital Backend (Port 8080)" "%~dp0start-backend.bat"
timeout /t 3 /nobreak >nul
start "Hospital Frontend (Port 5173)" "%~dp0start-frontend.bat"

echo.
echo ========================================================
echo  Both servers launched in dedicated command windows!
echo  Frontend URL: http://localhost:5173/
echo  Backend API:  http://localhost:8080/
echo  JSP Portal:   http://localhost:8080/dashboard.jsp
echo ========================================================
echo.
pause

