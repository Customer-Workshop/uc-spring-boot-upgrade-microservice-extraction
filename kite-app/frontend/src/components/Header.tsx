"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { MarketIndex } from "@/lib/types";
import type { ActiveTab } from "@/app/page";

interface Props {
  activeTab: ActiveTab;
  setActiveTab: (tab: ActiveTab) => void;
}

export default function Header({ activeTab, setActiveTab }: Props) {
  const [indices, setIndices] = useState<MarketIndex[]>([]);

  useEffect(() => {
    api.getIndices().then(setIndices).catch(() => {});
  }, []);

  const tabs: { key: ActiveTab; label: string }[] = [
    { key: "dashboard", label: "Dashboard" },
    { key: "orders", label: "Orders" },
    { key: "holdings", label: "Holdings" },
    { key: "positions", label: "Positions" },
    { key: "funds", label: "Funds" },
  ];

  return (
    <header className="bg-white border-b border-kite-border h-12 flex items-center px-4 justify-between shrink-0">
      {/* Left: Market indices */}
      <div className="flex items-center gap-6">
        {indices.map((idx) => (
          <div key={idx.name} className="flex items-center gap-2 text-xs">
            <span className="font-semibold text-kite-text">{idx.name}</span>
            <span className="text-kite-text">{idx.value.toLocaleString("en-IN", { maximumFractionDigits: 2 })}</span>
            <span className={idx.change >= 0 ? "text-positive" : "text-negative"}>
              {idx.change >= 0 ? "+" : ""}
              {idx.change_pct.toFixed(2)}%
            </span>
          </div>
        ))}
      </div>

      {/* Center: Logo */}
      <div className="flex items-center gap-1">
        <svg width="28" height="22" viewBox="0 0 28 22" fill="none">
          <path d="M0 11L14 0L28 11L14 22L0 11Z" fill="#e54040" />
          <path d="M0 11L14 0L14 22L0 11Z" fill="#e54040" />
          <path d="M14 0L28 11L14 22V0Z" fill="#d63535" />
        </svg>
      </div>

      {/* Right: Navigation */}
      <nav className="flex items-center gap-6">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`text-sm transition-colors ${
              activeTab === tab.key
                ? "text-kite-blue font-semibold"
                : "text-kite-text hover:text-kite-blue"
            }`}
          >
            {tab.label}
          </button>
        ))}
        <div className="flex items-center gap-2 text-xs text-kite-text-light ml-4">
          <div className="w-6 h-6 rounded-full bg-kite-blue text-white flex items-center justify-center text-xs font-bold">
            N
          </div>
          <span>AB1234</span>
        </div>
      </nav>
    </header>
  );
}
