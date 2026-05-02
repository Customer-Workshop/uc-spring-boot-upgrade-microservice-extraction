#!/bin/bash

echo "============================================"
echo "  Kite Trading Platform - Stopping..."
echo "============================================"
echo

DIR="$(cd "$(dirname "$0")" && pwd)"

# Stop backend
if [ -f "$DIR/.backend.pid" ]; then
    PID=$(cat "$DIR/.backend.pid")
    if kill -0 "$PID" 2>/dev/null; then
        echo "Stopping backend (PID $PID)..."
        kill "$PID" 2>/dev/null
    fi
    rm -f "$DIR/.backend.pid"
fi

# Stop frontend
if [ -f "$DIR/.frontend.pid" ]; then
    PID=$(cat "$DIR/.frontend.pid")
    if kill -0 "$PID" 2>/dev/null; then
        echo "Stopping frontend (PID $PID)..."
        kill "$PID" 2>/dev/null
    fi
    rm -f "$DIR/.frontend.pid"
fi

# Also kill any remaining processes on the ports
for PORT in 8001 3002; do
    PID=$(lsof -ti :$PORT 2>/dev/null)
    if [ -n "$PID" ]; then
        echo "Killing process on port $PORT (PID $PID)..."
        kill "$PID" 2>/dev/null
    fi
done

echo
echo "============================================"
echo "  All services stopped."
echo "============================================"
