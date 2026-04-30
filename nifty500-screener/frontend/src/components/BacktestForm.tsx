"use client";

import { useState } from "react";
import type { BacktestRequest } from "@/lib/types";

interface Props {
  onSubmit: (request: BacktestRequest) => void;
  loading: boolean;
}

const STRATEGIES = [
  { value: "breakout", label: "Breakout from Consolidation" },
  { value: "pullback", label: "Pullback to Moving Average" },
  { value: "rsi_mean_reversion", label: "RSI Mean Reversion" },
  { value: "trend_following", label: "Trend Following (MA Crossover)" },
];

export default function BacktestForm({ onSubmit, loading }: Props) {
  const [strategy, setStrategy] = useState("breakout");
  const [capital, setCapital] = useState(1000000);
  const [positionSize, setPositionSize] = useState(5);
  const [stopLoss, setStopLoss] = useState(5);
  const [takeProfit, setTakeProfit] = useState(15);
  const [symbolsText, setSymbolsText] = useState("");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const symbols = symbolsText
      .split(",")
      .map((s) => s.trim().toUpperCase())
      .filter(Boolean);

    onSubmit({
      strategy,
      initial_capital: capital,
      position_size_pct: positionSize,
      stop_loss_pct: stopLoss,
      take_profit_pct: takeProfit,
      symbols: symbols.length > 0 ? symbols : undefined,
    });
  };

  return (
    <form onSubmit={handleSubmit} className="card">
      <h3 className="text-lg font-bold text-gray-900 mb-4">Backtest Configuration</h3>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Strategy</label>
          <select
            value={strategy}
            onChange={(e) => setStrategy(e.target.value)}
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
          >
            {STRATEGIES.map((s) => (
              <option key={s.value} value={s.value}>
                {s.label}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Initial Capital (INR)
          </label>
          <input
            type="number"
            value={capital}
            onChange={(e) => setCapital(Number(e.target.value))}
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Position Size (%)
          </label>
          <input
            type="number"
            step="0.5"
            value={positionSize}
            onChange={(e) => setPositionSize(Number(e.target.value))}
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Stop Loss (%)</label>
          <input
            type="number"
            step="0.5"
            value={stopLoss}
            onChange={(e) => setStopLoss(Number(e.target.value))}
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Take Profit (%)</label>
          <input
            type="number"
            step="0.5"
            value={takeProfit}
            onChange={(e) => setTakeProfit(Number(e.target.value))}
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Symbols (comma-separated, leave empty for all)
          </label>
          <input
            type="text"
            value={symbolsText}
            onChange={(e) => setSymbolsText(e.target.value)}
            placeholder="RELIANCE, TCS, INFY"
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>

      <button type="submit" disabled={loading} className="btn-primary mt-4 w-full disabled:opacity-50">
        {loading ? "Running Backtest..." : "Run Backtest"}
      </button>
    </form>
  );
}
