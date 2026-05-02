@echo off
echo ============================================
echo   Stock ^& ETF Explorer
echo   AI-Powered Investment Research
echo   Starting...
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
echo [2/5] Starting backend on http://localhost:8002 ...
start "GDF12 Backend" cmd /k "cd /d %~dp0backend && call venv\Scripts\activate.bat && uvicorn app.main:app --host 0.0.0.0 --port 8002"

:: Wait for backend to start
echo [3/5] Waiting for backend to be ready...
timeout /t 5 /nobreak >nul

:: Seed data + refresh with live prices
echo [4/5] Seeding data + fetching LIVE prices from Yahoo Finance...
curl -s -X POST http://localhost:8002/api/seed-and-refresh >nul 2>nul
if %ERRORLEVEL% neq 0 curl -s -X POST http://localhost:8002/api/seed >nul 2>nul
echo       Done. (Prices refreshed with live market data)

:: Install frontend dependencies and start
echo [5/5] Starting frontend on http://localhost:3003 ...
cd /d "%~dp0frontend"
if not exist "node_modules" (
    call npm install --silent
)
start "GDF12 Frontend" cmd /k "cd /d %~dp0frontend && npm run dev"

:: Wait and open browser
timeout /t 8 /nobreak >nul
start http://localhost:3003

echo.
echo ============================================
echo   Stock ^& ETF Explorer is running!
echo   Frontend: http://localhost:3003
echo   Backend:  http://localhost:8002
echo   API Docs: http://localhost:8002/docs
echo.
echo   AI: Set ANTHROPIC_API_KEY env var for Claude AI
echo       Or run Ollama for local Llama3 analysis
echo ============================================
echo.
echo To stop, run: stop.bat
echo.
pause
