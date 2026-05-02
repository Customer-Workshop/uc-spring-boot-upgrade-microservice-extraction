@echo off
echo ============================================
echo   Kite Trading Platform - Starting...
echo ============================================
echo.

:: Check Python
where python >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo ERROR: Python not found. Install Python 3.10+ from https://python.org
    pause
    exit /b 1
)

:: Check Node
where node >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo ERROR: Node.js not found. Install Node.js 18+ from https://nodejs.org
    pause
    exit /b 1
)

:: Install backend dependencies
echo [1/5] Installing backend dependencies...
cd /d "%~dp0backend"
if not exist "venv" (
    python -m venv venv
)
call venv\Scripts\activate.bat
pip install -r requirements.txt --quiet
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to install backend dependencies.
    echo Try: pip install --trusted-host pypi.org --trusted-host files.pythonhosted.org -r requirements.txt
    pause
    exit /b 1
)

:: Start backend
echo [2/5] Starting backend on http://localhost:8001 ...
start "Kite Backend" cmd /k "cd /d %~dp0backend && call venv\Scripts\activate.bat && uvicorn app.main:app --host 0.0.0.0 --port 8001"

:: Wait for backend to start
echo [3/5] Waiting for backend to be ready...
timeout /t 5 /nobreak >nul

:: Seed data
echo [4/5] Seeding mock data...
curl -s -X POST http://localhost:8001/api/seed >nul 2>nul
echo       Mock data seeded (45 instruments, 1yr OHLCV).

:: Install frontend dependencies and start
echo [5/5] Starting frontend on http://localhost:3002 ...
cd /d "%~dp0frontend"
if not exist "node_modules" (
    call npm install --silent
)
start "Kite Frontend" cmd /k "cd /d %~dp0frontend && set NEXT_PUBLIC_API_URL=http://localhost:8001 && npm run dev"

:: Wait and open browser
timeout /t 8 /nobreak >nul
start http://localhost:3002

echo.
echo ============================================
echo   Kite Trading Platform is running!
echo   Frontend: http://localhost:3002
echo   Backend:  http://localhost:8001
echo   API Docs: http://localhost:8001/docs
echo ============================================
echo.
echo To stop, run: stop.bat
echo.
pause
