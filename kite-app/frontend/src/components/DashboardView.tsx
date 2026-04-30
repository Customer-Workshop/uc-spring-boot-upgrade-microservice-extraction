"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { Dashboard, Holding } from "@/lib/types";

interface Props {
  openChart: (symbol: string) => void;
}

export default function DashboardView({ openChart }: Props) {
  const [dashboard, setDashboard] = useState<Dashboard | null>(null);
  const [holdings, setHoldings] = useState<Holding[]>([]);
  const [seeded, setSeeded] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = () => {
    api.getDashboard().then(setDashboard).catch(() => {});
    api.getHoldings().then(setHoldings).catch(() => {});
  };

  const handleSeed = async () => {
    await api.seed();
    setSeeded(true);
    loadData();
  };

  if (!dashboard) {
    return (
      <div className="flex flex-col items-center justify-center h-full gap-4">
        <p className="text-kite-text-light text-sm">Loading dashboard...</p>
        {!seeded && (
          <button onClick={handleSeed} className="kite-btn-blue">
            Seed Mock Data
          </button>
        )}
      </div>
    );
  }

  const formatCurrency = (n: number) =>
    n >= 100000
      ? `₹${(n / 100000).toFixed(2)}L`
      : `₹${n.toLocaleString("en-IN", { maximumFractionDigits: 0 })}`;

  return (
    <div className="max-w-4xl">
      {/* Greeting */}
      <h1 className="text-2xl font-light text-kite-text mb-6">Hi, Nithin</h1>

      {/* Equity / Commodity */}
      <div className="grid grid-cols-2 gap-6 mb-8">
        <div>
          <div className="flex items-center gap-2 mb-3">
            <span className="text-kite-text-light text-xs">⊚</span>
            <span className="text-sm font-semibold text-kite-text">Equity</span>
          </div>
          <div className="flex items-baseline gap-4">
            <span className="text-3xl font-light text-kite-text">
              {formatCurrency(dashboard.holdings_value > 0 ? 500000 : 0)}
            </span>
            <div className="text-xs text-kite-text-light">
              <div>Margins used <span className="text-kite-text">{formatCurrency(52000)}</span></div>
              <div>Account value <span className="text-kite-text">{formatCurrency(552000)}</span></div>
            </div>
          </div>
        </div>
        <div>
          <div className="flex items-center gap-2 mb-3">
            <span className="text-kite-text-light text-xs">◇</span>
            <span className="text-sm font-semibold text-kite-text">Commodity</span>
          </div>
          <div className="flex items-baseline gap-4">
            <span className="text-3xl font-light text-kite-text-light">—</span>
          </div>
        </div>
      </div>

      {/* Holdings */}
      <div className="mb-8">
        <div className="flex items-center gap-2 mb-3">
          <span className="text-kite-text-light text-xs">⊞</span>
          <span className="text-sm font-semibold text-kite-text">
            Holdings ({dashboard.holdings_count})
          </span>
        </div>
        <div className="flex items-baseline gap-4 mb-3">
          <span className={`text-3xl font-light ${dashboard.holdings_pnl >= 0 ? "text-positive" : "text-negative"}`}>
            {formatCurrency(Math.abs(dashboard.holdings_pnl))}
          </span>
          <span className={`text-xs ${dashboard.holdings_pnl >= 0 ? "text-positive" : "text-negative"}`}>
            {((dashboard.holdings_pnl / (dashboard.holdings_investment || 1)) * 100).toFixed(2)}%
          </span>
          <div className="text-xs text-kite-text-light">
            <span>Current value <span className="text-kite-text">{formatCurrency(dashboard.holdings_value)}</span></span>
            <span className="ml-4">Investment <span className="text-kite-text">{formatCurrency(dashboard.holdings_investment)}</span></span>
          </div>
        </div>

        {/* Holdings bar */}
        <div className="flex h-6 rounded overflow-hidden mb-3">
          {holdings.map((h, i) => {
            const pct = (h.last_price * h.quantity) / (dashboard.holdings_value || 1) * 100;
            const colors = ["#e54040", "#e57373", "#ff8a65", "#ffb74d", "#fff176", "#aed581", "#4db6ac", "#4fc3f7", "#7986cb", "#ba68c8"];
            return (
              <div
                key={h.symbol}
                className="cursor-pointer hover:opacity-80 transition-opacity"
                style={{ width: `${pct}%`, backgroundColor: colors[i % colors.length] }}
                title={`${h.symbol}: ₹${(h.last_price * h.quantity).toLocaleString("en-IN")}`}
                onClick={() => openChart(h.symbol)}
              />
            );
          })}
        </div>
        <div className="flex items-center gap-4 text-[10px] text-kite-text-light">
          <span>⊗ Current Value</span>
          <span>⊗ Investment Value</span>
          <span>⊗ P&L</span>
        </div>
      </div>

      {/* Market overview + Positions */}
      <div className="grid grid-cols-2 gap-6">
        <div>
          <div className="flex items-center gap-2 mb-3">
            <span className="text-kite-text-light text-xs">↗</span>
            <span className="text-sm font-semibold text-kite-text">Market overview</span>
          </div>
          <div className="kite-card h-48 flex items-center justify-center">
            <div className="text-center">
              {dashboard.indices.map((idx) => (
                <div key={idx.name} className="mb-2">
                  <span className="text-xs font-semibold text-kite-text mr-2">{idx.name}</span>
                  <span className="text-xs text-kite-text">{idx.value.toLocaleString("en-IN")}</span>
                  <span className={`text-xs ml-2 ${idx.change >= 0 ? "text-positive" : "text-negative"}`}>
                    {idx.change >= 0 ? "▲" : "▼"} {Math.abs(idx.change_pct).toFixed(2)}%
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
        <div>
          <div className="flex items-center gap-2 mb-3">
            <span className="text-kite-text-light text-xs">⊞</span>
            <span className="text-sm font-semibold text-kite-text">
              Positions ({dashboard.positions_count})
            </span>
          </div>
          <div className="kite-card h-48 flex items-center justify-center">
            <div className="text-center">
              <span className={`text-2xl font-light ${dashboard.positions_pnl >= 0 ? "text-positive" : "text-negative"}`}>
                {dashboard.positions_pnl >= 0 ? "+" : "-"}₹{Math.abs(dashboard.positions_pnl).toLocaleString("en-IN")}
              </span>
              <div className="text-xs text-kite-text-light mt-1">Day P&L</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
