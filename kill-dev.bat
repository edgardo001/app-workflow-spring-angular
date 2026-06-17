@echo off
setlocal enabledelayedexpansion

set "ROOT_DIR=%~dp0"

echo ============================================
echo  WorkflowNet - Kill Dev Environment
echo ============================================
echo.

:: Load environment variables from .env
if exist "%ROOT_DIR%.env" (
    for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%ROOT_DIR%.env") do (
        set "KEY=%%A"
        set "VALUE=%%B"
        if defined KEY if defined VALUE (
            set "TMPVAL=!VALUE!"
            if "!TMPVAL:~-1!"=="^M" set "TMPVAL=!TMPVAL:~0,-1!"
            set "!KEY!=!TMPVAL!"
        )
    )
)

:: 1. Kill Java processes (Backend / Gradle)
echo [1/5] Killing Java processes...
taskkill /F /IM java.exe >nul 2>&1
if errorlevel 1 (
    echo   No Java processes found.
) else (
    echo   Java processes terminated.
)

:: 2. Kill Node processes (Frontend / Angular)
echo [2/5] Killing Node.js processes...
taskkill /F /IM node.exe >nul 2>&1
if errorlevel 1 (
    echo   No Node.js processes found.
) else (
    echo   Node.js processes terminated.
)

:: 3. Kill processes on dev ports (cleanup orphaned connections)
echo [3/5] Cleaning up ports 8080, 4200, 27017, 9092...

for %%P in (8080 4200) do (
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%%P " ^| findstr "LISTENING" 2^>nul') do (
        echo   Killing PID %%a on port %%P
        taskkill /F /PID %%a >nul 2>&1
    )
)

echo   Ports cleared.

:: 4. Stop Docker containers
echo [4/5] Stopping Docker containers...
docker-compose -f "%ROOT_DIR%src\docker\docker-compose.yml" --env-file "%ROOT_DIR%.env" down --remove-orphans 2>nul
if errorlevel 1 (
    echo   Docker Compose not running or already stopped.
) else (
    echo   Docker containers stopped and removed.
)

:: 5. Verify
echo [5/5] Verifying cleanup...
set "JAVA_COUNT=0"
for /f %%a in ('tasklist /FI "IMAGENAME eq java.exe" 2^>nul ^| find /c "java.exe"') do set "JAVA_COUNT=%%a"
set "NODE_COUNT=0"
for /f %%a in ('tasklist /FI "IMAGENAME eq node.exe" 2^>nul ^| find /c "node.exe"') do set "NODE_COUNT=%%a"

if "!JAVA_COUNT!"=="0" if "!NODE_COUNT!"=="0" (
    echo   All processes killed.
) else (
    echo   WARNING: !JAVA_COUNT! Java + !NODE_COUNT! Node processes still running.
)

echo.
echo ============================================
echo  Dev environment stopped.
echo ============================================
echo.
pause
