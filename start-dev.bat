@echo off
setlocal enabledelayedexpansion

set "ROOT_DIR=%~dp0"

:: Load environment variables from .env file
if exist "%ROOT_DIR%.env" (
    echo Loading .env file...
    for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%ROOT_DIR%.env") do (
        set "KEY=%%A"
        set "VALUE=%%B"
        if defined KEY if defined VALUE (
            set "!KEY!=!VALUE!"
        )
    )
)

:: Check Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo Error: Docker is not running or not installed.
    echo Please start Docker Desktop and try again.
    pause
    exit /b 1
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

echo.
echo [1/3] Starting Docker Compose for MongoDB and Kafka...
docker-compose -f "%ROOT_DIR%src\docker\docker-compose.yml" --env-file "%ROOT_DIR%.env" up -d mongodb kafka

echo Waiting 10 seconds for containers to start...
ping -n 10 127.0.0.1 >nul

echo.
echo [2/3] Starting Spring Boot Backend...
pushd "%ROOT_DIR%src\backend"
start "WorkflowNet-Backend" cmd.exe /k gradlew.bat bootRun
popd

echo.
echo [3/3] Installing frontend dependencies and starting Angular...
pushd "%ROOT_DIR%src\frontend"
if not exist "node_modules" (
    echo Installing frontend dependencies...
    call npm install
)
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
docker-compose -f "%ROOT_DIR%src\docker\docker-compose.yml" --env-file "%ROOT_DIR%.env" down
echo Done.
pause
