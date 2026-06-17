@echo off
setlocal enabledelayedexpansion

:: Load environment variables from .env file
if exist "%~dp0.env" (
    echo Loading .env file...
    for /f "usebackq tokens=1,* delims==" %%A in ("%~dp0.env") do (
        set "KEY=%%A"
        set "VALUE=%%B"
        if not "!KEY:~0,1!"=="#" if not "!KEY!"=="" (
            set "!KEY!=!VALUE!"
        )
    )
)
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

set "ROOT_DIR=%~dp0"

echo.
echo [1/3] Starting Docker Compose for MongoDB and Kafka...
docker-compose -f "%ROOT_DIR%docker-compose.yml" up -d mongodb kafka

echo Waiting 10 seconds for containers to start...
ping -n 10 127.0.0.1 >nul

echo.
echo [2/3] Starting Spring Boot Backend...
pushd "%ROOT_DIR%src\backend"
start "WorkflowNet-Backend" cmd.exe /k gradlew.bat bootRun
popd

echo.
echo [3/3] Starting Angular Frontend...
pushd "%ROOT_DIR%src\frontend"
start "WorkflowNet-Frontend" cmd.exe /k npm start -- --proxy-config proxy.conf.json --open
popd

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
docker-compose -f "%ROOT_DIR%docker-compose.yml" down
echo Done.
pause