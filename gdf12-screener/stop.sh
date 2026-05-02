#!/bin/bash
echo "============================================"
echo "  GDF-12 Screener - Stopping..."
echo "============================================"
echo

DIR="$(cd "$(dirname "$0")" && pwd)"

for pidfile in .backend.pid .frontend.pid; do
    if [ -f "$DIR/$pidfile" ]; then
        PID=$(cat "$DIR/$pidfile")
        if kill -0 "$PID" 2>/dev/null; then
            echo "Stopping PID $PID..."
            kill "$PID" 2>/dev/null
        fi
        rm -f "$DIR/$pidfile"
    fi
done

for PORT in 8002 3003; do
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
