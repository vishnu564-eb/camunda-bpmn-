@echo off
title Camunda 8 Run Launcher
echo ========================================================
echo Starting Camunda 8 Run (Zeebe, Operate, Tasklist)...
echo ========================================================

set JAVA_HOME=C:\Users\VISHN\openjdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

cd /d "C:\Users\VISHN\Downloads\camunda8-run-8.8-windows-x86_64 (1)\c8run-8.8.34"
c8run.exe start

pause
