@echo off
setlocal
title WildGuard - Start All Services

set "ROOT=%~dp0"
set "PSDIR=%WINDIR%\System32\WindowsPowerShell\v1.0"
if exist "%PSDIR%\powershell.exe" set "PATH=%PSDIR%;%PATH%"

echo.
echo ==========================================
echo       WILDGUARD - START ALL SERVICES
echo ==========================================
echo.
echo Platform      : http://localhost:8081
echo Wildlife      : http://localhost:8082
echo Operations    : http://localhost:8083
echo Conservation  : http://localhost:8084
echo.
echo Starting each service in a separate terminal...
echo.

if not exist "%ROOT%mvnw.cmd" (
    echo ERROR: Maven wrapper not found: "%ROOT%mvnw.cmd"
    pause
    exit /b 1
)

if not exist "%ROOT%pom.xml" (
    echo ERROR: Root pom.xml not found: "%ROOT%pom.xml"
    pause
    exit /b 1
)

start "WildGuard Platform - 8081" cmd /k "set PATH=%PSDIR%;%PATH%&& cd /d ""%ROOT%"" && call mvnw.cmd spring-boot:run"

if exist "%ROOT%wildlife-service\pom.xml" (
    start "WildGuard Wildlife - 8082" cmd /k "set PATH=%PSDIR%;%PATH%&& cd /d ""%ROOT%wildlife-service"" && call ..\mvnw.cmd spring-boot:run"
) else (
    echo WARNING: wildlife-service\pom.xml not found. Skipping wildlife service.
)

if exist "%ROOT%operations-service\pom.xml" (
    start "WildGuard Operations - 8083" cmd /k "set PATH=%PSDIR%;%PATH%&& cd /d ""%ROOT%operations-service"" && call ..\mvnw.cmd spring-boot:run"
) else (
    echo WARNING: operations-service\pom.xml not found. Skipping operations service.
)

if exist "%ROOT%conservation-service\pom.xml" (
    start "WildGuard Conservation - 8084" cmd /k "set PATH=%PSDIR%;%PATH%&& cd /d ""%ROOT%conservation-service"" && call ..\mvnw.cmd spring-boot:run"
) else (
    echo WARNING: conservation-service\pom.xml not found. Skipping conservation service.
)

echo.
echo Startup commands launched.
echo Check each service window for successful startup.
echo Wildlife service may fail until its Java compilation errors are fixed.
echo Keep the service windows open while using WildGuard.
echo.
pause
endlocal
