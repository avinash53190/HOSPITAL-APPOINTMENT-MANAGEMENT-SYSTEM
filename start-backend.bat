@echo off
title Hospital Backend (Apache Tomcat on Port 8080)
set "ComSpec=%SystemRoot%\system32\cmd.exe"
set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2.1"
set "PATH=C:\Program Files\apache-maven-3.10.0\bin;%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0\backend"
echo ========================================================
echo  Starting Hospital Appointment System Backend Server...
echo  URL: http://localhost:8080/
echo  JSP Protected Dashboard: http://localhost:8080/dashboard.jsp
echo ========================================================
call mvn.cmd compile exec:java
pause

