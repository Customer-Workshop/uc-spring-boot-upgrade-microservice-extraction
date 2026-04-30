"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { Holding } from "@/lib/types";

interface Props {
  openChart: (symbol: string) => void;
  openOrder: (symbol: string, type: "BUY" | "SELL", price: number) => void;
}

export default function HoldingsView({ openChart, openOrder }: Props) {
  const [holdings, setHoldings] = useState<Holding[]>([]);
  const [hoveredSymbol, setHoveredSymbol] = useState<string | null>(null);

  useEffect(() => {
    api.getHoldings().then(setHoldings).catch(() => {});
  }, []);

  const totalInvestment = holdings.reduce((s, h) => s + h.average_price * h.quantity, 0);
  const totalCurrent = holdings.reduce((s, h) => s + h.last_price * h.quantity, 0);
  const totalPnl = totalCurrent - totalInvestment;
  const totalDayChange = holdings.reduce((s, h) => s + h.day_change, 0);

  return (
    <div className="max-w-5xl">
      <div className="flex items-baseline justify-between mb-6">
        <div>
          <h2 className="text-lg font-light text-kite-text">Holdings ({holdings.length})</h2>
        </div>
        <div className="flex items-center gap-8 text-xs">
          <div>
            <span className="text-kite-text-light">Current value </span>
            <span className="font-semibold text-kite-text">₹{totalCurrent.toLocaleString("en-IN", { maximumFractionDigits: 2 })}</span>
          </div>
          <div>
            <span className="text-kite-text-light">Investment </span>
            <span className="font-semibold text-kite-text">₹{totalInvestment.toLocaleString("en-IN", { maximumFractionDigits: 2 })}</span>
          </div>
          <div>
            <span className="text-kite-text-light">P&L </span>
            <span className={`font-semibold ${totalPnl >= 0 ? "text-positive" : "text-negative"}`}>
              {totalPnl >= 0 ? "+" : ""}₹{totalPnl.toLocaleString("en-IN", { maximumFractionDigits: 2 })}
              <span className="ml-1">({((totalPnl / (totalInvestment || 1)) * 100).toFixed(2)}%)</span>
            </span>
          </div>
          <div>
            <span className="text-kite-text-light">Day&apos;s P&L </span>
            <span className={`font-semibold ${totalDayChange >= 0 ? "text-positive" : "text-negative"}`}>
              {totalDayChange >= 0 ? "+" : ""}₹{totalDayChange.toLocaleString("en-IN", { maximumFractionDigits: 2 })}
            </span>
          </div>
        </div>
      </div>

      <table className="w-full">
        <thead>
          <tr className="text-xs text-kite-text-light border-b border-kite-border">
            <th className="text-left py-2 font-normal">Instrument</th>
            <th className="text-right py-2 font-normal">Qty.</th>
            <th className="text-right py-2 font-normal">Avg. cost</th>
            <th className="text-right py-2 font-normal">LTP</th>
            <th className="text-right py-2 font-normal">Cur. val</th>
            <th className="text-right py-2 font-normal">P&L</th>
            <th className="text-right py-2 font-normal">Net chg.</th>
            <th className="text-right py-2 font-normal">Day chg.</th>
          </tr>
        </thead>
        <tbody>
          {holdings.map((h) => {
            const curVal = h.last_price * h.quantity;
            const invVal = h.average_price * h.quantity;
            const netChg = ((h.last_price - h.average_price) / h.average_price) * 100;
            return (
              <tr
                key={h.symbol}
                className="border-b border-kite-border hover:bg-kite-hover cursor-pointer relative"
                onMouseEnter={() => setHoveredSymbol(h.symbol)}
                onMouseLeave={() => setHoveredSymbol(null)}
                onClick={() => openChart(h.symbol)}
              >
                <td className="py-2.5 text-xs font-semibold text-kite-text">{h.symbol}</td>
                <td className="py-2.5 text-xs text-kite-text text-right">{h.quantity}</td>
                <td className="py-2.5 text-xs text-kite-text text-right">{h.average_price.toFixed(2)}</td>
                <td className="py-2.5 text-xs text-kite-text text-right">{h.last_price.toFixed(2)}</td>
                <td className="py-2.5 text-xs text-kite-text text-right">{curVal.toLocaleString("en-IN", { maximumFractionDigits: 2 })}</td>
                <td className={`py-2.5 text-xs text-right font-medium ${h.pnl >= 0 ? "text-positive" : "text-negative"}`}>
                  {h.pnl >= 0 ? "+" : ""}{h.pnl.toFixed(2)}
                </td>
                <td className={`py-2.5 text-xs text-right ${netChg >= 0 ? "text-positive" : "text-negative"}`}>
                  {netChg.toFixed(2)}%
                </td>
                <td className={`py-2.5 text-xs text-right ${h.day_change_pct >= 0 ? "text-positive" : "text-negative"}`}>
                  {h.day_change_pct.toFixed(2)}%
                </td>
                {hoveredSymbol === h.symbol && (
                  <td className="absolute right-2 top-1/2 -translate-y-1/2 flex gap-1">
                    <button
                      onClick={(e) => { e.stopPropagation(); openOrder(h.symbol, "BUY", h.last_price); }}
                      className="text-[10px] bg-kite-blue text-white px-2 py-0.5 rounded"
                    >
                      B
                    </button>
                    <button
                      onClick={(e) => { e.stopPropagation(); openOrder(h.symbol, "SELL", h.last_price); }}
                      className="text-[10px] bg-kite-orange text-white px-2 py-0.5 rounded"
                    >
                      S
                    </button>
                  </td>
                )}
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
