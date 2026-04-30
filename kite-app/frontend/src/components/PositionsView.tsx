"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { Position } from "@/lib/types";

interface Props {
  openChart: (symbol: string) => void;
  openOrder: (symbol: string, type: "BUY" | "SELL", price: number) => void;
}

export default function PositionsView({ openChart, openOrder }: Props) {
  const [positions, setPositions] = useState<Position[]>([]);

  useEffect(() => {
    api.getPositions().then(setPositions).catch(() => {});
  }, []);

  const totalPnl = positions.reduce((s, p) => s + p.pnl, 0);

  return (
    <div className="max-w-5xl">
      <div className="flex items-baseline justify-between mb-6">
        <h2 className="text-lg font-light text-kite-text">Positions ({positions.length})</h2>
        <div className="text-xs">
          <span className="text-kite-text-light">Total P&L </span>
          <span className={`font-semibold ${totalPnl >= 0 ? "text-positive" : "text-negative"}`}>
            {totalPnl >= 0 ? "+" : ""}₹{totalPnl.toLocaleString("en-IN", { maximumFractionDigits: 2 })}
          </span>
        </div>
      </div>

      {positions.length === 0 ? (
        <div className="text-center py-20 text-kite-text-light">
          <p className="text-sm">No open positions</p>
        </div>
      ) : (
        <table className="w-full">
          <thead>
            <tr className="text-xs text-kite-text-light border-b border-kite-border">
              <th className="text-left py-2 font-normal">Product</th>
              <th className="text-left py-2 font-normal">Instrument</th>
              <th className="text-right py-2 font-normal">Qty.</th>
              <th className="text-right py-2 font-normal">Avg.</th>
              <th className="text-right py-2 font-normal">LTP</th>
              <th className="text-right py-2 font-normal">P&L</th>
              <th className="text-right py-2 font-normal">Chg.</th>
            </tr>
          </thead>
          <tbody>
            {positions.map((p, i) => {
              const chgPct = p.buy_price > 0
                ? ((p.last_price - p.buy_price) / p.buy_price) * 100
                : p.sell_price > 0
                ? ((p.sell_price - p.last_price) / p.sell_price) * 100
                : 0;
              return (
                <tr
                  key={`${p.symbol}-${p.product}-${i}`}
                  className="border-b border-kite-border hover:bg-kite-hover cursor-pointer"
                  onClick={() => openChart(p.symbol)}
                >
                  <td className="py-2.5 text-xs text-kite-text-light">{p.product}</td>
                  <td className="py-2.5 text-xs font-semibold text-kite-text">
                    {p.symbol}
                    <span className={`ml-2 text-[10px] ${p.quantity >= 0 ? "text-kite-blue" : "text-kite-orange"}`}>
                      {p.quantity >= 0 ? "BUY" : "SELL"}
                    </span>
                  </td>
                  <td className="py-2.5 text-xs text-kite-text text-right">{Math.abs(p.quantity)}</td>
                  <td className="py-2.5 text-xs text-kite-text text-right">
                    {(p.buy_price || p.sell_price).toFixed(2)}
                  </td>
                  <td className="py-2.5 text-xs text-kite-text text-right">{p.last_price.toFixed(2)}</td>
                  <td className={`py-2.5 text-xs text-right font-medium ${p.pnl >= 0 ? "text-positive" : "text-negative"}`}>
                    {p.pnl >= 0 ? "+" : ""}₹{p.pnl.toFixed(2)}
                  </td>
                  <td className={`py-2.5 text-xs text-right ${chgPct >= 0 ? "text-positive" : "text-negative"}`}>
                    {chgPct.toFixed(2)}%
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      )}
    </div>
  );
}
