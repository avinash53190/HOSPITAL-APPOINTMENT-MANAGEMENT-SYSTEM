@echo off
title Setup MySQL Database - hospital_db
echo ========================================================
echo  Hospital Appointment System - Database Setup
echo  Importing backend\database.sql into MySQL...
echo ========================================================
set /p MYSQL_PASS="Enter your MySQL root password (press Enter for 'root'): "
if "%MYSQL_PASS%"=="" set MYSQL_PASS=root
"C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe" -u root -p%MYSQL_PASS% < "%~dp0\backend\database.sql"
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================================
    echo  [SUCCESS] Database hospital_db and tables created successfully!
    echo ========================================================
) else (
    echo.
    echo ========================================================
    echo  [NOTE] If login failed, you can open:
    echo  backend\database.sql
    echo  inside MySQL Workbench and execute it directly!
    echo ========================================================
)
pause

