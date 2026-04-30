"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { Fund } from "@/lib/types";

export default function FundsView() {
  const [funds, setFunds] = useState<Fund | null>(null);

  useEffect(() => {
    api.getFunds().then(setFunds).catch(() => {});
  }, []);

  if (!funds) return <div className="text-kite-text-light text-sm">Loading funds...</div>;

  const formatCurrency = (n: number) => `₹${n.toLocaleString("en-IN", { maximumFractionDigits: 2 })}`;

  const rows = [
    { label: "Opening balance", equity: funds.opening_balance, commodity: 0 },
    { label: "Pay-in", equity: funds.payin, commodity: 0 },
    { label: "Payout", equity: funds.payout, commodity: 0 },
    { label: "SPAN", equity: 0, commodity: 0 },
    { label: "Delivery margin", equity: 0, commodity: 0 },
    { label: "Exposure", equity: 0, commodity: 0 },
    { label: "Options premium", equity: 0, commodity: 0 },
    { label: "Collateral (Liquid funds)", equity: funds.collateral, commodity: 0 },
    { label: "Collateral (Equity)", equity: 0, commodity: 0 },
    { label: "Total collateral", equity: funds.collateral, commodity: 0 },
  ];

  return (
    <div className="max-w-3xl">
      <h2 className="text-lg font-light text-kite-text mb-6">Funds</h2>

      {/* Available balance cards */}
      <div className="grid grid-cols-2 gap-6 mb-8">
        <div className="kite-card">
          <div className="text-xs text-kite-text-light mb-2">Available margin (Equity)</div>
          <div className="text-3xl font-light text-kite-green">
            {formatCurrency(funds.equity_available)}
          </div>
          <div className="text-xs text-kite-text-light mt-1">
            Used: {formatCurrency(funds.equity_used)}
          </div>
        </div>
        <div className="kite-card">
          <div className="text-xs text-kite-text-light mb-2">Available margin (Commodity)</div>
          <div className="text-3xl font-light text-kite-text-light">
            {formatCurrency(funds.commodity_available)}
          </div>
          <div className="text-xs text-kite-text-light mt-1">
            Used: {formatCurrency(funds.commodity_used)}
          </div>
        </div>
      </div>

      {/* Fund details */}
      <table className="w-full">
        <thead>
          <tr className="text-xs text-kite-text-light border-b border-kite-border">
            <th className="text-left py-2 font-normal w-1/2"></th>
            <th className="text-right py-2 font-normal">Equity</th>
            <th className="text-right py-2 font-normal">Commodity</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((r) => (
            <tr key={r.label} className="border-b border-kite-border">
              <td className="py-2 text-xs text-kite-text-light">{r.label}</td>
              <td className="py-2 text-xs text-kite-text text-right">{formatCurrency(r.equity)}</td>
              <td className="py-2 text-xs text-kite-text-light text-right">{formatCurrency(r.commodity)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
