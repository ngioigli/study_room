@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot
set MAVEN_HOME=C:\Program Files\Maven\apache-maven-3.9.12
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

cd /d C:\Users\ngioigli\Desktop\java\qr_code
echo Starting Spring Boot application...
echo.
call mvn spring-boot:run
pause
