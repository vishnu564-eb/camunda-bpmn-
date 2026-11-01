@echo off
title Camunda 8 Job Workers and Mock Services
echo ========================================================
echo Starting Spring Boot External Job Workers & Mock APIs...
echo Port: 8085
echo Zeebe Broker Target: localhost:26500
echo ========================================================

set JAVA_HOME=C:\Users\VISHN\openjdk-21
set PATH=%JAVA_HOME%\bin;C:\Users\VISHN\apache-maven-3.9.9\bin;%PATH%

cd /d "%~dp0spring-boot-workers"
call "C:\Users\VISHN\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run

pause
