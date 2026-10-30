@echo off
title Hospital Frontend (React.js on Port 5173)
set "ComSpec=%SystemRoot%\system32\cmd.exe"
set "PATH=C:\Program Files\nodejs;%PATH%"
cd /d "%~dp0\frontend"
echo ========================================================
echo  Starting Hospital Appointment System React Frontend...
echo  URL: http://localhost:5173/
echo ========================================================
call npm.cmd run dev -- --host
pause

