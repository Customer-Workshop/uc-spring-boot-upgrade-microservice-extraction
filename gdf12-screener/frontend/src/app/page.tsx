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
      {/* Summary Cards - Groww style */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5 hover:border-[#3a3a4a] transition-all">
          <div className="text-[#8c8ca1] text-xs font-medium mb-2 uppercase tracking-wider">Total Stocks</div>
          <div className="text-3xl font-bold text-white">{stocks.length}</div>
          <div className="text-[#5c5c72] text-xs mt-1">NIFTY Universe</div>
        </div>
        <div className="bg-[#1c1c27] rounded-2xl border border-emerald-500/20 p-5 hover:border-emerald-500/40 transition-all">
          <div className="text-emerald-400 text-xs font-medium mb-2 uppercase tracking-wider">Strong Buy on Dips</div>
          <div className="text-3xl font-bold text-emerald-400">{topPicks.length}</div>
          <div className="text-[#5c5c72] text-xs mt-1">Score ≥ 10/12</div>
        </div>
        <div className="bg-[#1c1c27] rounded-2xl border border-cyan-500/20 p-5 hover:border-cyan-500/40 transition-all">
          <div className="text-cyan-400 text-xs font-medium mb-2 uppercase tracking-wider">Quality Candidates</div>
          <div className="text-3xl font-bold text-cyan-400">{qualityCandidates.length}</div>
          <div className="text-[#5c5c72] text-xs mt-1">Score 9/12</div>
        </div>
        <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5 hover:border-[#3a3a4a] transition-all">
          <div className="text-[#8c8ca1] text-xs font-medium mb-2 uppercase tracking-wider">Avg Score</div>
          <div className="flex items-center gap-3">
            <ScoreRing score={Math.round(avgScore)} size={48} />
            <span className="text-2xl font-bold">{avgScore.toFixed(1)}<span className="text-sm text-[#5c5c72] font-normal">/12</span></span>
          </div>
        </div>
      </div>

      {/* Filters - Groww rounded style */}
      <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5 mb-6">
        <div className="flex flex-wrap items-center gap-4">
          <div className="flex items-center gap-2">
            <label className="text-xs text-[#8c8ca1] font-medium">Sector</label>
            <select value={sector} onChange={e => setSector(e.target.value)}
              className="bg-[#0d0d12] border border-[#2a2a3a] rounded-xl px-4 py-2 text-sm text-white focus:outline-none focus:border-[#00d09c] transition-colors">
              <option value="">All Sectors</option>
              {sectors.map(s => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
          <div className="flex items-center gap-2">
            <label className="text-xs text-[#8c8ca1] font-medium">Min Score</label>
            <select value={minScore} onChange={e => setMinScore(Number(e.target.value))}
              className="bg-[#0d0d12] border border-[#2a2a3a] rounded-xl px-4 py-2 text-sm text-white focus:outline-none focus:border-[#00d09c] transition-colors">
              <option value={0}>Any</option>
              <option value={5}>≥ 5</option>
              <option value={7}>≥ 7</option>
              <option value={9}>≥ 9 (Quality)</option>
              <option value={10}>≥ 10 (Strong Buy)</option>
            </select>
          </div>
          <div className="flex items-center gap-2">
            <label className="text-xs text-[#8c8ca1] font-medium">Sort By</label>
            <select value={sortBy} onChange={e => setSortBy(e.target.value)}
              className="bg-[#0d0d12] border border-[#2a2a3a] rounded-xl px-4 py-2 text-sm text-white focus:outline-none focus:border-[#00d09c] transition-colors">
              <option value="score">GDF Score</option>
              <option value="pe">PE</option>
              <option value="roe">ROE</option>
              <option value="mcap">Market Cap</option>
              <option value="mos">Margin of Safety</option>
            </select>
          </div>
          <label className="flex items-center gap-2 cursor-pointer ml-auto">
            <div className="relative">
              <input type="checkbox" checked={defensiveOnly} onChange={e => setDefensiveOnly(e.target.checked)}
                className="sr-only peer" />
              <div className="w-9 h-5 bg-[#2a2a3a] rounded-full peer-checked:bg-[#00d09c] transition-colors"></div>
              <div className="absolute top-0.5 left-0.5 w-4 h-4 bg-white rounded-full transition-transform peer-checked:translate-x-4"></div>
            </div>
            <span className="text-sm text-[#8c8ca1]">Defensive sectors only</span>
          </label>
        </div>
      </div>

      {/* Table */}
      <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] overflow-hidden mb-6">
        {loading ? (
          <div className="text-center py-20 text-[#8c8ca1]">
            <div className="inline-block w-8 h-8 border-2 border-[#00d09c] border-t-transparent rounded-full animate-spin mb-3"></div>
            <div>Loading stocks...</div>
          </div>
        ) : (
          <StockTable stocks={stocks} onSelect={handleSelectStock} watchlist={watchlistSymbols} onToggleWatch={handleToggleWatch} />
        )}
      </div>

      {/* AI Insights */}
      <AIInsights tab="fundamental" symbol={lastSelectedSymbol || undefined} />

      {/* GDF-12 Legend */}
      <div className="mt-6 bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-6">
        <h3 className="text-lg font-bold mb-5 flex items-center gap-2">
          <span className="w-2 h-2 rounded-full bg-[#00d09c]"></span>
          GDF-12 Signal Model
        </h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-5 text-xs">
          <div className="bg-[#16161e] rounded-xl p-4">
            <h4 className="text-[#5b8def] font-semibold mb-3 uppercase tracking-wide text-[10px]">Group A — Business Quality</h4>
            <ul className="space-y-2 text-[#8c8ca1]">
              <li className="flex items-start gap-2"><span className="text-[#5b8def] mt-0.5">&#9679;</span> Large &amp; Stable (MCap &gt;₹5K Cr)</li>
              <li className="flex items-start gap-2"><span className="text-[#5b8def] mt-0.5">&#9679;</span> 10Y Consistent Profits</li>
              <li className="flex items-start gap-2"><span className="text-[#5b8def] mt-0.5">&#9679;</span> Economic Moat</li>
            </ul>
          </div>
          <div className="bg-[#16161e] rounded-xl p-4">
            <h4 className="text-[#00d09c] font-semibold mb-3 uppercase tracking-wide text-[10px]">Group B — Financial Strength</h4>
            <ul className="space-y-2 text-[#8c8ca1]">
              <li className="flex items-start gap-2"><span className="text-[#00d09c] mt-0.5">&#9679;</span> Low Debt (D/E &lt;0.5 or IC &gt;4)</li>
              <li className="flex items-start gap-2"><span className="text-[#00d09c] mt-0.5">&#9679;</span> ROE &amp; ROCE ≥15%</li>
              <li className="flex items-start gap-2"><span className="text-[#00d09c] mt-0.5">&#9679;</span> Strong Cash Flows</li>
            </ul>
          </div>
          <div className="bg-[#16161e] rounded-xl p-4">
            <h4 className="text-[#a78bfa] font-semibold mb-3 uppercase tracking-wide text-[10px]">Group C — Valuation</h4>
            <ul className="space-y-2 text-[#8c8ca1]">
              <li className="flex items-start gap-2"><span className="text-[#a78bfa] mt-0.5">&#9679;</span> PE &lt;25 &amp; below 5Y avg</li>
              <li className="flex items-start gap-2"><span className="text-[#a78bfa] mt-0.5">&#9679;</span> Price ≤70% of Graham Number</li>
              <li className="flex items-start gap-2"><span className="text-[#a78bfa] mt-0.5">&#9679;</span> P/B below sector average</li>
            </ul>
          </div>
          <div className="bg-[#16161e] rounded-xl p-4">
            <h4 className="text-[#f5a623] font-semibold mb-3 uppercase tracking-wide text-[10px]">Group D — Shareholder Signals</h4>
            <ul className="space-y-2 text-[#8c8ca1]">
              <li className="flex items-start gap-2"><span className="text-[#f5a623] mt-0.5">&#9679;</span> Promoter &gt;50%, No pledge</li>
              <li className="flex items-start gap-2"><span className="text-[#f5a623] mt-0.5">&#9679;</span> 7-10Y Dividend history</li>
              <li className="flex items-start gap-2"><span className="text-[#f5a623] mt-0.5">&#9679;</span> Silent Accumulation pattern</li>
            </ul>
          </div>
        </div>
        <div className="mt-5 flex flex-wrap gap-3 text-xs">
          <span className="verdict-strong-buy px-3 py-1.5">≥10: Strong Buy on Dips</span>
          <span className="verdict-quality px-3 py-1.5">9: Quality Candidate</span>
          <span className="verdict-watch px-3 py-1.5">7-8: Watch</span>
          <span className="verdict-weak px-3 py-1.5">5-6: Weak</span>
          <span className="verdict-avoid px-3 py-1.5">&lt;5: Avoid</span>
        </div>
      </div>

      {selectedStock && <StockDetailPanel stock={selectedStock} onClose={() => setSelectedStock(null)} />}
    </div>
  );
}

export default function Home() {
  const [activeTab, setActiveTab] = useState<TabId>('fundamental');

  return (
    <div className="min-h-screen bg-[#0d0d12]">
      {/* Header - Groww style */}
      <header className="bg-[#16161e] border-b border-[#2a2a3a] sticky top-0 z-40">
        <div className="max-w-[1440px] mx-auto px-6 py-4">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-4">
              <div className="w-10 h-10 bg-gradient-to-br from-[#00d09c] to-[#00b386] rounded-xl flex items-center justify-center text-white font-bold text-base shadow-lg shadow-emerald-500/20">S</div>
              <div>
                <h1 className="text-xl font-bold tracking-tight">Stock &amp; ETF Explorer</h1>
                <p className="text-xs text-[#5c5c72] mt-0.5">AI-Powered Investment Research</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <span className="px-3 py-1.5 rounded-full bg-[#a78bfa]/10 text-[#a78bfa] text-xs font-semibold border border-[#a78bfa]/20">AI Enabled</span>
              <span className="px-3 py-1.5 rounded-full bg-[#00d09c]/10 text-[#00d09c] text-xs font-semibold border border-[#00d09c]/20 flex items-center gap-1.5">
                <span className="w-1.5 h-1.5 rounded-full bg-[#00d09c] animate-pulse"></span>
                Live Data
              </span>
            </div>
          </div>

          {/* Tab Navigation - Groww pill style */}
          <div className="flex gap-2">
            {TABS.map(tab => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`px-5 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 flex items-center gap-2 ${
                  activeTab === tab.id
                    ? 'bg-[#00d09c]/10 text-[#00d09c] border border-[#00d09c]/30 shadow-sm shadow-emerald-500/10'
                    : 'text-[#8c8ca1] hover:text-white hover:bg-[#1c1c27] border border-transparent'
                }`}
              >
                <span className="text-base">{tab.icon}</span>
                <span>{tab.label}</span>
                <span className="text-[10px] opacity-50 hidden lg:inline">— {tab.desc}</span>
              </button>
            ))}
          </div>
        </div>
      </header>

      <main className="max-w-[1440px] mx-auto px-6 py-6">
        {/* Disclaimer - subtle */}
        <div className="bg-[#1c1c27] border border-[#2a2a3a] rounded-xl px-5 py-3 mb-6 text-xs text-[#8c8ca1] flex items-center gap-2">
          <span className="text-amber-400">&#9888;</span>
          Research tool only — not investment advice. Based on Benjamin Graham&apos;s defensive investor criteria. Always do your own due diligence.
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

      {/* Footer */}
      <footer className="border-t border-[#2a2a3a] py-4 text-center text-xs text-[#5c5c72]">
        Stock &amp; ETF Explorer &bull; Powered by GDF-12 Framework &bull; Data for educational purposes only
      </footer>
    </div>
  );
}
