@echo off
echo ============================================
echo   GDF-12 Screener - Stopping...
echo ============================================
echo.

:: Kill backend (uvicorn on port 8002)
echo Stopping backend (port 8002)...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8002" ^| findstr "LISTENING"') do (
    taskkill /PID %%a /F >nul 2>nul
)

:: Kill frontend (next.js on port 3003)
echo Stopping frontend (port 3003)...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":3003" ^| findstr "LISTENING"') do (
    taskkill /PID %%a /F >nul 2>nul
)

:: Close windows
taskkill /FI "WINDOWTITLE eq GDF12 Backend*" /F >nul 2>nul
taskkill /FI "WINDOWTITLE eq GDF12 Frontend*" /F >nul 2>nul

echo.
echo ============================================
echo   All services stopped.
echo ============================================
echo.
pause
