#!/bin/bash
set -e

echo "============================================"
echo "  Stock & ETF Explorer"
echo "  AI-Powered Investment Research"
echo "  Starting..."
echo "============================================"
echo

DIR="$(cd "$(dirname "$0")" && pwd)"

if ! command -v python3 &> /dev/null; then
    echo "ERROR: Python3 not found. Install Python 3.10+"
    exit 1
fi
if ! command -v node &> /dev/null; then
    echo "ERROR: Node.js not found. Install Node.js 18+"
    exit 1
fi

echo "[1/5] Installing backend dependencies..."
cd "$DIR/backend"
if [ ! -d "venv" ]; then python3 -m venv venv; fi
source venv/bin/activate
pip install -r requirements.txt --quiet

echo "[2/5] Starting backend on http://localhost:8002 ..."
uvicorn app.main:app --host 0.0.0.0 --port 8002 &
BACKEND_PID=$!
echo $BACKEND_PID > "$DIR/.backend.pid"

echo "[3/5] Waiting for backend to be ready..."
sleep 4

echo "[4/5] Seeding data and computing GDF-12 scores..."
curl -s -X POST http://localhost:8002/api/seed > /dev/null 2>&1 || true
echo "      Done."

echo "[5/5] Starting frontend on http://localhost:3003 ..."
cd "$DIR/frontend"
if [ ! -d "node_modules" ]; then npm install --silent; fi
NEXT_PUBLIC_API_URL=http://localhost:8002 npm run dev &
FRONTEND_PID=$!
echo $FRONTEND_PID > "$DIR/.frontend.pid"

sleep 5
if command -v open &> /dev/null; then open http://localhost:3003
elif command -v xdg-open &> /dev/null; then xdg-open http://localhost:3003; fi

echo
echo "============================================"
echo "  Stock & ETF Explorer is running!"
echo "  Frontend: http://localhost:3003"
echo "  Backend:  http://localhost:8002"
echo "  API Docs: http://localhost:8002/docs"
echo ""
echo "  AI: Set ANTHROPIC_API_KEY env var for Claude AI"
echo "       Or run Ollama for local Llama3 analysis"
echo "============================================"
echo
echo "To stop: ./stop.sh or Ctrl+C"
wait
