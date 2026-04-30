"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { SearchResult, Watchlist } from "@/lib/types";

interface Props {
  openChart: (symbol: string) => void;
  openOrder: (symbol: string, type: "BUY" | "SELL", price: number) => void;
}

export default function Sidebar({ openChart, openOrder }: Props) {
  const [watchlists, setWatchlists] = useState<Watchlist[]>([]);
  const [activeWl, setActiveWl] = useState(0);
  const [search, setSearch] = useState("");
  const [searchResults, setSearchResults] = useState<SearchResult[]>([]);
  const [showSearch, setShowSearch] = useState(false);
  const [hoveredItem, setHoveredItem] = useState<string | null>(null);

  useEffect(() => {
    api.getWatchlists().then(setWatchlists).catch(() => {});
  }, []);

  useEffect(() => {
    if (search.length >= 1) {
      setShowSearch(true);
      const timer = setTimeout(() => {
        api.searchInstruments(search).then(setSearchResults).catch(() => {});
      }, 200);
      return () => clearTimeout(timer);
    } else {
      setShowSearch(false);
      setSearchResults([]);
    }
  }, [search]);

  const addToWatchlist = async (symbol: string) => {
    if (watchlists.length === 0) return;
    const wl = watchlists[activeWl];
    try {
      await api.addWatchlistItem(wl.id, symbol);
      const updated = await api.getWatchlists();
      setWatchlists(updated);
      setSearch("");
      setShowSearch(false);
    } catch {}
  };

  const removeItem = async (wlId: number, itemId: number) => {
    try {
      await api.removeWatchlistItem(wlId, itemId);
      const updated = await api.getWatchlists();
      setWatchlists(updated);
    } catch {}
  };

  const currentWl = watchlists[activeWl];

  return (
    <aside className="w-[320px] bg-white border-r border-kite-border flex flex-col shrink-0 overflow-hidden">
      {/* Search */}
      <div className="p-3 border-b border-kite-border relative">
        <div className="flex items-center bg-kite-bg rounded px-3 py-2">
          <svg className="w-4 h-4 text-kite-text-light mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            type="text"
            placeholder="Search eg: infy, nifty fut, nifty weekly"
            className="bg-transparent text-xs w-full outline-none text-kite-text placeholder-kite-text-light"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            onFocus={() => search.length > 0 && setShowSearch(true)}
          />
        </div>

        {/* Search dropdown */}
        {showSearch && searchResults.length > 0 && (
          <div className="absolute left-0 right-0 top-full bg-white border border-kite-border shadow-lg z-50 max-h-80 overflow-y-auto">
            {searchResults.map((r) => (
              <button
                key={r.symbol}
                onClick={() => addToWatchlist(r.symbol)}
                className="w-full flex items-center justify-between px-4 py-2.5 hover:bg-kite-hover text-left border-b border-kite-border last:border-0"
              >
                <div>
                  <span className="text-xs font-semibold text-kite-text">{r.symbol}</span>
                  <span className="text-xs text-kite-text-light ml-2">{r.exchange}</span>
                  <div className="text-xs text-kite-text-light truncate max-w-[180px]">{r.name}</div>
                </div>
                <span className="text-xs text-kite-text">₹{r.last_price.toLocaleString("en-IN")}</span>
              </button>
            ))}
          </div>
        )}
      </div>

      {/* Watchlist tabs */}
      <div className="flex border-b border-kite-border">
        {watchlists.map((wl, i) => (
          <button
            key={wl.id}
            onClick={() => setActiveWl(i)}
            className={`flex-1 py-2 text-xs text-center transition-colors ${
              activeWl === i
                ? "text-kite-blue border-b-2 border-kite-blue font-semibold"
                : "text-kite-text-light hover:text-kite-text"
            }`}
          >
            {i + 1}
          </button>
        ))}
      </div>

      {/* Watchlist items */}
      <div className="flex-1 overflow-y-auto">
        {currentWl?.items.map((item) => (
          <div
            key={item.id}
            className="flex items-center justify-between px-4 py-2 border-b border-kite-border hover:bg-kite-hover cursor-pointer group relative"
            onMouseEnter={() => setHoveredItem(item.symbol)}
            onMouseLeave={() => setHoveredItem(null)}
          >
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2">
                <span className="text-xs font-semibold text-kite-text truncate">{item.symbol}</span>
                {item.exchange !== "NSE" && (
                  <span className="text-[10px] text-kite-text-light">{item.exchange}</span>
                )}
              </div>
            </div>
            <div className="flex items-center gap-3">
              <span className={`text-xs ${item.change_pct >= 0 ? "text-positive" : "text-negative"}`}>
                {item.change_pct >= 0 ? "+" : ""}
                {item.change_pct.toFixed(2)}%
              </span>
              <span className="text-xs font-medium text-kite-text w-16 text-right">
                {item.last_price.toLocaleString("en-IN", { maximumFractionDigits: 2 })}
              </span>
            </div>

            {/* Hover actions */}
            {hoveredItem === item.symbol && (
              <div className="absolute right-2 top-1/2 -translate-y-1/2 flex items-center gap-1 bg-white">
                <button
                  onClick={(e) => { e.stopPropagation(); openOrder(item.symbol, "BUY", item.last_price); }}
                  className="text-[10px] bg-kite-blue text-white px-2 py-0.5 rounded"
                >
                  B
                </button>
                <button
                  onClick={(e) => { e.stopPropagation(); openOrder(item.symbol, "SELL", item.last_price); }}
                  className="text-[10px] bg-kite-orange text-white px-2 py-0.5 rounded"
                >
                  S
                </button>
                <button
                  onClick={(e) => { e.stopPropagation(); openChart(item.symbol); }}
                  className="text-[10px] bg-gray-200 text-kite-text px-2 py-0.5 rounded"
                >
                  ⊞
                </button>
                <button
                  onClick={(e) => { e.stopPropagation(); removeItem(currentWl.id, item.id); }}
                  className="text-[10px] bg-gray-200 text-kite-text px-2 py-0.5 rounded"
                >
                  ✕
                </button>
              </div>
            )}
          </div>
        ))}
        {currentWl?.items.length === 0 && (
          <div className="p-8 text-center text-xs text-kite-text-light">
            No instruments in this watchlist. Use search to add.
          </div>
        )}
      </div>

      {/* Watchlist count */}
      <div className="px-4 py-2 border-t border-kite-border text-[10px] text-kite-text-light">
        {currentWl?.items.length || 0} / 50
      </div>
    </aside>
  );
}
