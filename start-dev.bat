@echo off
setlocal enabledelayedexpansion

where java >nul 2>&1
if errorlevel 1 (
    echo Error: Java ^(JDK 17^) not found in PATH.
    pause
    exit /b 1
)

where node >nul 2>&1
if errorlevel 1 (
    echo Error: Node.js not found in PATH
    pause
    exit /b 1
)

where docker >nul 2>&1
if errorlevel 1 (
    echo Error: Docker not found or not running
    pause
    exit /b 1
)

echo.
echo [1/3] Starting Docker Compose for MongoDB and Kafka...
docker-compose -f docker-compose.yml up -d mongodb kafka

echo Waiting 10 seconds for containers to start...
ping -n 10 127.0.0.1 >nul

echo.
echo [2/3] Starting Spring Boot Backend...
start "WorkflowNet-Backend" cmd.exe /c "cd src\backend ^&^& gradlew bootRun"

echo.
echo [3/3] Starting Angular Frontend...
start "WorkflowNet-Frontend" cmd.exe /c "cd src\frontend ^&^& ng serve --proxy-config proxy.conf.json --open"

echo.
echo ============================================
echo All services started.
echo Backend:    http://localhost:8080
echo Frontend:   http://localhost:4200
echo Swagger UI: http://localhost:8080/swagger-ui.html
echo MongoDB:    localhost:27017
echo Kafka:      localhost:9092
echo ============================================
echo.
echo Press any key to stop all services...
pause >nul

echo Shutting down...
docker-compose -f docker-compose.yml down
echo Done.
pause
