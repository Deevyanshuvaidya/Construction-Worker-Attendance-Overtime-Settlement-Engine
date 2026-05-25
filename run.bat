@echo off
title Construction Attendance & Overtime Settlement Engine

echo ========================================================
echo Starting local Caching layer (Redis)...
echo ========================================================
docker-compose up -d redis

echo.
echo ========================================================
echo Loading Database Credentials from .env...
echo ========================================================
:: Read .env line by line and set variables (skipping comments and empty lines)
for /f "usebackq delims=" %%x in (".env") do (
    echo %%x | findstr /r "^[a-zA-Z_]" >nul && set %%x
)

echo.
echo ========================================================
echo Booting Spring Boot Application...
echo ========================================================
call mvnw spring-boot:run
