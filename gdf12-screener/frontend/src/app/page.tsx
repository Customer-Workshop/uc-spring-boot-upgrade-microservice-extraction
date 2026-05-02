'use client';

import { useCallback, useEffect, useState } from 'react';
import {
  StockSummary, StockDetail as StockDetailType,
  fetchScreener, fetchStockDetail, fetchSectors, fetchWatchlist,
  addToWatchlist, removeFromWatchlist,
} from '@/lib/api';
import StockTable from '@/components/StockTable';
import StockDetailPanel from '@/components/StockDetailPanel';
import ScoreRing from '@/components/ScoreRing';
import SwingTab from '@/components/SwingTab';
import ETFTab from '@/components/ETFTab';
import AIInsights from '@/components/AIInsights';

type TabId = 'fundamental' | 'technical' | 'etf';

const TABS: { id: TabId; label: string; icon: string; desc: string }[] = [
  { id: 'fundamental', label: 'Fundamentals', icon: '📊', desc: 'GDF-12 Defensive Screener' },
  { id: 'technical', label: 'Technical Swing', icon: '📈', desc: 'Confirmation Ladder Scanner' },
  { id: 'etf', label: 'Best ETFs', icon: '🏦', desc: 'ETF Screener & Comparison' },
];

function FundamentalsTab() {
  const [stocks, setStocks] = useState<StockSummary[]>([]);
  const [sectors, setSectors] = useState<string[]>([]);
  const [watchlistSymbols, setWatchlistSymbols] = useState<Set<string>>(new Set());
  const [selectedStock, setSelectedStock] = useState<StockDetailType | null>(null);
  const [lastSelectedSymbol, setLastSelectedSymbol] = useState('');
  const [loading, setLoading] = useState(true);
  const [sector, setSector] = useState('');
  const [minScore, setMinScore] = useState(0);
  const [defensiveOnly, setDefensiveOnly] = useState(false);
  const [sortBy, setSortBy] = useState('score');

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const [stockData, sectorData, watchData] = await Promise.all([
        fetchScreener({ sector: sector || undefined, min_score: minScore || undefined, defensive_only: defensiveOnly, sort_by: sortBy }),
        fetchSectors(),
        fetchWatchlist(),
      ]);
      setStocks(stockData);
      setSectors(sectorData);
      setWatchlistSymbols(new Set(watchData.map(w => w.symbol)));
    } catch (err) { console.error(err); }
    setLoading(false);
  }, [sector, minScore, defensiveOnly, sortBy]);

  useEffect(() => { loadData(); }, [loadData]);

  const handleSelectStock = async (symbol: string) => {
    setLastSelectedSymbol(symbol);
    try {
      const detail = await fetchStockDetail(symbol);
      setSelectedStock(detail);
    } catch (err) { console.error(err); }
  };

  const handleToggleWatch = async (symbol: string) => {
    try {
      if (watchlistSymbols.has(symbol)) {
        await removeFromWatchlist(symbol);
        setWatchlistSymbols(prev => { const n = new Set(prev); n.delete(symbol); return n; });
      } else {
        await addToWatchlist(symbol);
        setWatchlistSymbols(prev => new Set(prev).add(symbol));
      }
    } catch (err) { console.error(err); }
  };

  const topPicks = stocks.filter(s => s.total_score >= 10);
  const qualityCandidates = stocks.filter(s => s.total_score >= 9 && s.total_score < 10);
  const avgScore = stocks.length > 0 ? (stocks.reduce((a, b) => a + b.total_score, 0) / stocks.length) : 0;

  return (
    <div>
      {/* Summary Cards */}
      <div className="grid grid-cols-4 gap-4 mb-6">
        <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] p-4">
          <div className="text-[#8b90a8] text-xs mb-1">Total Stocks</div>
          <div className="text-2xl font-bold">{stocks.length}</div>
        </div>
        <div className="bg-[#1e2235] rounded-xl border border-green-500/30 p-4">
          <div className="text-green-400 text-xs mb-1">Strong Buy on Dips (≥10)</div>
          <div className="text-2xl font-bold text-green-400">{topPicks.length}</div>
        </div>
        <div className="bg-[#1e2235] rounded-xl border border-emerald-500/30 p-4">
          <div className="text-emerald-400 text-xs mb-1">Quality Candidates (9)</div>
          <div className="text-2xl font-bold text-emerald-400">{qualityCandidates.length}</div>
        </div>
        <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] p-4">
          <div className="text-[#8b90a8] text-xs mb-1">Avg Score</div>
          <div className="flex items-center gap-2">
            <ScoreRing score={Math.round(avgScore)} size={42} />
            <span className="text-lg font-mono">{avgScore.toFixed(1)}/12</span>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] p-4 mb-6">
        <div className="flex flex-wrap items-center gap-4">
          <div className="flex items-center gap-2">
            <label className="text-xs text-[#8b90a8]">Sector</label>
            <select value={sector} onChange={e => setSector(e.target.value)}
              className="bg-[#0f1117] border border-[#2a2e45] rounded-lg px-3 py-1.5 text-sm text-white focus:outline-none focus:border-blue-500">
              <option value="">All Sectors</option>
              {sectors.map(s => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
          <div className="flex items-center gap-2">
            <label className="text-xs text-[#8b90a8]">Min Score</label>
            <select value={minScore} onChange={e => setMinScore(Number(e.target.value))}
              className="bg-[#0f1117] border border-[#2a2e45] rounded-lg px-3 py-1.5 text-sm text-white focus:outline-none">
              <option value={0}>Any</option>
              <option value={5}>≥ 5</option>
              <option value={7}>≥ 7</option>
              <option value={9}>≥ 9 (Quality)</option>
              <option value={10}>≥ 10 (Strong Buy)</option>
            </select>
          </div>
          <div className="flex items-center gap-2">
            <label className="text-xs text-[#8b90a8]">Sort By</label>
            <select value={sortBy} onChange={e => setSortBy(e.target.value)}
              className="bg-[#0f1117] border border-[#2a2e45] rounded-lg px-3 py-1.5 text-sm text-white focus:outline-none">
              <option value="score">GDF Score</option>
              <option value="pe">PE</option>
              <option value="roe">ROE</option>
              <option value="mcap">Market Cap</option>
              <option value="mos">Margin of Safety</option>
            </select>
          </div>
          <label className="flex items-center gap-2 cursor-pointer">
            <input type="checkbox" checked={defensiveOnly} onChange={e => setDefensiveOnly(e.target.checked)} className="rounded" />
            <span className="text-sm text-blue-300">Defensive sectors only</span>
          </label>
        </div>
      </div>

      {/* Table */}
      <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] overflow-hidden">
        {loading ? (
          <div className="text-center py-20 text-[#8b90a8]">Loading stocks...</div>
        ) : (
          <StockTable stocks={stocks} onSelect={handleSelectStock} watchlist={watchlistSymbols} onToggleWatch={handleToggleWatch} />
        )}
      </div>

      {/* AI Insights */}
      <AIInsights tab="fundamental" symbol={lastSelectedSymbol || undefined} />

      {/* GDF-12 Legend */}
      <div className="mt-6 bg-[#1e2235] rounded-xl border border-[#2a2e45] p-6">
        <h3 className="text-lg font-bold mb-4">GDF-12 Signal Model</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-xs">
          <div>
            <h4 className="text-blue-400 font-semibold mb-2">Group A — Business Quality</h4>
            <ul className="space-y-1 text-[#8b90a8]">
              <li>A1: Large &amp; Stable (MCap &gt;₹5K Cr)</li>
              <li>A2: 10Y Consistent Profits</li>
              <li>A3: Economic Moat</li>
            </ul>
          </div>
          <div>
            <h4 className="text-green-400 font-semibold mb-2">Group B — Financial Strength</h4>
            <ul className="space-y-1 text-[#8b90a8]">
              <li>B4: Low Debt (D/E &lt;0.5 or IC &gt;4)</li>
              <li>B5: ROE &amp; ROCE ≥15%</li>
              <li>B6: Strong Cash Flows</li>
            </ul>
          </div>
          <div>
            <h4 className="text-purple-400 font-semibold mb-2">Group C — Valuation</h4>
            <ul className="space-y-1 text-[#8b90a8]">
              <li>C7: PE &lt;25 &amp; below 5Y avg</li>
              <li>C8: Price ≤70% of Graham Number</li>
              <li>C9: P/B below sector average</li>
            </ul>
          </div>
          <div>
            <h4 className="text-orange-400 font-semibold mb-2">Group D — Shareholder Signals</h4>
            <ul className="space-y-1 text-[#8b90a8]">
              <li>D10: Promoter &gt;50%, No pledge</li>
              <li>D11: 7-10Y Dividend history</li>
              <li>D12: Silent Accumulation pattern</li>
            </ul>
          </div>
        </div>
        <div className="mt-4 flex gap-4 text-xs">
          <span className="verdict-strong-buy px-2 py-1 rounded">≥10: Strong Buy on Dips</span>
          <span className="verdict-quality px-2 py-1 rounded">9: Quality Candidate</span>
          <span className="verdict-watch px-2 py-1 rounded">7-8: Watch</span>
          <span className="verdict-weak px-2 py-1 rounded">5-6: Weak</span>
          <span className="verdict-avoid px-2 py-1 rounded">&lt;5: Avoid</span>
        </div>
      </div>

      {selectedStock && <StockDetailPanel stock={selectedStock} onClose={() => setSelectedStock(null)} />}
    </div>
  );
}

export default function Home() {
  const [activeTab, setActiveTab] = useState<TabId>('fundamental');

  return (
    <div className="min-h-screen bg-[#0f1117]">
      {/* Header */}
      <header className="border-b border-[#2a2e45] bg-[#0f1117] sticky top-0 z-40">
        <div className="max-w-[1500px] mx-auto px-4 py-3">
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 bg-gradient-to-br from-green-400 to-blue-500 rounded-lg flex items-center justify-center text-white font-bold text-sm">S</div>
              <div>
                <h1 className="text-lg font-bold">Stock &amp; ETF Explorer</h1>
                <p className="text-xs text-[#8b90a8]">AI-Powered Investment Research — Real-Time Data</p>
              </div>
            </div>
            <div className="flex items-center gap-2 text-xs text-[#8b90a8]">
              <span className="px-2 py-0.5 rounded bg-indigo-500/20 text-indigo-400 border border-indigo-500/30">AI Enabled</span>
              <span className="px-2 py-0.5 rounded bg-green-500/20 text-green-400 border border-green-500/30">Live Data</span>
            </div>
          </div>

          {/* Tab Navigation */}
          <div className="flex gap-1">
            {TABS.map(tab => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`px-5 py-2.5 rounded-t-lg text-sm font-medium transition-colors flex items-center gap-2 ${
                  activeTab === tab.id
                    ? 'bg-[#1e2235] text-white border-t-2 border-x border-[#2a2e45] border-t-blue-500'
                    : 'text-[#8b90a8] hover:text-white hover:bg-[#1e2235]/50'
                }`}
              >
                <span>{tab.icon}</span>
                <span>{tab.label}</span>
                <span className="text-xs opacity-60 hidden md:inline">— {tab.desc}</span>
              </button>
            ))}
          </div>
        </div>
      </header>

      <main className="max-w-[1500px] mx-auto px-4 py-6">
        {/* Disclaimer */}
        <div className="bg-yellow-500/10 border border-yellow-500/30 rounded-lg px-4 py-2.5 mb-6 text-xs text-yellow-300">
          Research tool only — not investment advice. Based on Benjamin Graham&apos;s defensive investor criteria.
          Always do your own due diligence before investing.
        </div>

        {/* Tab Content */}
        {activeTab === 'fundamental' && <FundamentalsTab />}
        {activeTab === 'technical' && (
          <>
            <SwingTab />
            <AIInsights tab="technical" />
          </>
        )}
        {activeTab === 'etf' && (
          <>
            <ETFTab />
            <AIInsights tab="etf" />
          </>
        )}
      </main>
    </div>
  );
}
