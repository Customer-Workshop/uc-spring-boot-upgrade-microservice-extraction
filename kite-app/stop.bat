@echo off
echo ============================================
echo   Kite Trading Platform - Stopping...
echo ============================================
echo.

:: Kill backend (uvicorn on port 8001)
echo Stopping backend (port 8001)...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8001" ^| findstr "LISTENING"') do (
    taskkill /PID %%a /F >nul 2>nul
)

:: Kill frontend (next.js on port 3002)
echo Stopping frontend (port 3002)...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":3002" ^| findstr "LISTENING"') do (
    taskkill /PID %%a /F >nul 2>nul
)

:: Close any windows titled "Kite Backend" or "Kite Frontend"
taskkill /FI "WINDOWTITLE eq Kite Backend*" /F >nul 2>nul
taskkill /FI "WINDOWTITLE eq Kite Frontend*" /F >nul 2>nul

echo.
echo ============================================
echo   All services stopped.
echo ============================================
echo.
pause
