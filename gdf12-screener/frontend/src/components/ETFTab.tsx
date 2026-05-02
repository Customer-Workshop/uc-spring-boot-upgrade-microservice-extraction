'use client';

import { useCallback, useEffect, useState } from 'react';
import { ETFSummary, ETFDetail, fetchETFs, fetchETFDetail, fetchETFCategories } from '@/lib/api';
import ScoreRing from './ScoreRing';

function ETFVerdictBadge({ verdict }: { verdict: string }) {
  const cls = verdict === 'Best Pick' ? 'bg-green-500/20 text-green-400 border-green-500/30'
    : verdict === 'Good Choice' ? 'bg-blue-500/20 text-blue-400 border-blue-500/30'
    : verdict === 'Average' ? 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30'
    : 'bg-red-500/20 text-red-400 border-red-500/30';
  return <span className={`px-2.5 py-1 rounded-full text-xs font-semibold border ${cls}`}>{verdict}</span>;
}

function ReturnCell({ value }: { value: number }) {
  return (
    <span className={`font-mono ${value > 0 ? 'text-green-400' : value < 0 ? 'text-red-400' : 'text-[#8b90a8]'}`}>
      {value > 0 ? '+' : ''}{value.toFixed(1)}%
    </span>
  );
}

function ETFDetailPanel({ etf, onClose }: { etf: ETFDetail; onClose: () => void }) {
  const s = etf.score;
  const factors = [
    { name: 'Trend (Weekly/Monthly MAs)', score: s.trend?.score ?? 0, detail: s.trend?.detail ?? '' },
    { name: 'Momentum (RSI + Returns)', score: s.momentum?.score ?? 0, detail: s.momentum?.detail ?? '' },
    { name: 'Cost Efficiency', score: s.cost?.score ?? 0, detail: s.cost?.detail ?? '' },
    { name: 'Liquidity (AUM + Volume)', score: s.liquidity?.score ?? 0, detail: s.liquidity?.detail ?? '' },
    { name: 'Historical Performance', score: s.performance?.score ?? 0, detail: s.performance?.detail ?? '' },
  ];

  return (
    <div className="fixed inset-0 bg-black/60 z-50 flex justify-end" onClick={onClose}>
      <div className="bg-[#0f1117] w-full max-w-2xl overflow-y-auto border-l border-[#2a2e45]" onClick={e => e.stopPropagation()}>
        <div className="sticky top-0 bg-[#0f1117] border-b border-[#2a2e45] p-5 z-10">
          <div className="flex items-start justify-between">
            <div>
              <h2 className="text-xl font-bold">{etf.symbol}</h2>
              <p className="text-[#8b90a8] text-sm">{etf.name}</p>
              <p className="text-[#8b90a8] text-xs">{etf.category} · {etf.amc}</p>
            </div>
            <button onClick={onClose} className="text-[#8b90a8] hover:text-white text-2xl">×</button>
          </div>
          <div className="flex items-center justify-between mt-4">
            <div>
              <div className="text-3xl font-bold">₹{etf.current_price.toLocaleString('en-IN')}</div>
              <div className="text-xs text-[#8b90a8]">NAV: ₹{etf.nav} · 52W: ₹{etf.low_52w}–₹{etf.high_52w}</div>
            </div>
            <div className="flex items-center gap-3">
              <ScoreRing score={s.total_score} maxScore={10} size={64} />
              <ETFVerdictBadge verdict={s.verdict} />
            </div>
          </div>
        </div>

        <div className="p-5 space-y-4">
          {/* Returns Grid */}
          <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] p-4">
            <h3 className="text-green-400 font-semibold text-sm mb-3">Returns</h3>
            <div className="grid grid-cols-4 gap-3 text-center text-sm">
              <div><div className="text-xs text-[#8b90a8]">1W</div><ReturnCell value={etf.return_1w} /></div>
              <div><div className="text-xs text-[#8b90a8]">1M</div><ReturnCell value={etf.return_1m} /></div>
              <div><div className="text-xs text-[#8b90a8]">3M</div><ReturnCell value={etf.return_3m} /></div>
              <div><div className="text-xs text-[#8b90a8]">6M</div><ReturnCell value={etf.return_6m} /></div>
              <div><div className="text-xs text-[#8b90a8]">1Y</div><ReturnCell value={etf.return_1y} /></div>
              <div><div className="text-xs text-[#8b90a8]">3Y CAGR</div><ReturnCell value={etf.return_3y_cagr} /></div>
              <div><div className="text-xs text-[#8b90a8]">5Y CAGR</div><ReturnCell value={etf.return_5y_cagr} /></div>
              <div><div className="text-xs text-[#8b90a8]">RSI</div><span className="font-mono">{etf.rsi_14.toFixed(0)}</span></div>
            </div>
          </div>

          {/* Cost & Liquidity */}
          <div className="grid grid-cols-2 gap-4">
            <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] p-4">
              <h3 className="text-blue-400 font-semibold text-sm mb-2">Cost</h3>
              <div className="space-y-1 text-sm">
                <div className="flex justify-between"><span className="text-[#8b90a8]">Expense Ratio</span><span className={`font-mono ${etf.expense_ratio <= 0.2 ? 'text-green-400' : 'text-yellow-400'}`}>{etf.expense_ratio.toFixed(2)}%</span></div>
                <div className="flex justify-between"><span className="text-[#8b90a8]">Tracking Error</span><span className={`font-mono ${etf.tracking_error <= 0.5 ? 'text-green-400' : 'text-yellow-400'}`}>{etf.tracking_error.toFixed(2)}%</span></div>
              </div>
            </div>
            <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] p-4">
              <h3 className="text-purple-400 font-semibold text-sm mb-2">Liquidity</h3>
              <div className="space-y-1 text-sm">
                <div className="flex justify-between"><span className="text-[#8b90a8]">AUM</span><span className="font-mono">₹{(etf.aum_cr).toLocaleString('en-IN')} Cr</span></div>
                <div className="flex justify-between"><span className="text-[#8b90a8]">Avg Volume</span><span className="font-mono">{etf.avg_volume.toLocaleString('en-IN')}</span></div>
              </div>
            </div>
          </div>

          {/* MA Position */}
          <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] p-4">
            <h3 className="text-orange-400 font-semibold text-sm mb-2">Moving Average Position</h3>
            <div className="grid grid-cols-2 gap-3 text-sm">
              <div className="flex justify-between"><span className="text-[#8b90a8]">20W MA</span><span className="font-mono">₹{etf.sma_20w.toFixed(0)} {etf.above_20w_ma ? '✓' : '✗'}</span></div>
              <div className="flex justify-between"><span className="text-[#8b90a8]">50W MA</span><span className="font-mono">₹{etf.sma_50w.toFixed(0)} {etf.above_50w_ma ? '✓' : '✗'}</span></div>
              <div className="flex justify-between"><span className="text-[#8b90a8]">10M MA</span><span className="font-mono">₹{etf.sma_10m.toFixed(0)}</span></div>
              <div className="flex justify-between"><span className="text-[#8b90a8]">12M MA</span><span className="font-mono">₹{etf.sma_12m.toFixed(0)}</span></div>
            </div>
          </div>

          {/* Score Breakdown */}
          <h3 className="text-lg font-bold">Score Breakdown</h3>
          <div className="space-y-2">
            {factors.map((f, i) => (
              <div key={i} className={`rounded-lg px-4 py-3 ${f.score === 2 ? 'signal-pass' : f.score === 1 ? 'bg-yellow-500/10 border border-yellow-500/20' : 'signal-fail'}`}>
                <div className="flex items-center justify-between mb-1">
                  <span className="font-medium text-sm">{f.name}</span>
                  <span className={`text-xs font-bold px-2 py-0.5 rounded ${f.score === 2 ? 'bg-green-500/30 text-green-400' : f.score === 1 ? 'bg-yellow-500/30 text-yellow-400' : 'bg-red-500/30 text-red-400'}`}>{f.score}/2</span>
                </div>
                <p className="text-xs text-[#8b90a8]">{f.detail}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default function ETFTab() {
  const [etfs, setEtfs] = useState<ETFSummary[]>([]);
  const [categories, setCategories] = useState<string[]>([]);
  const [selected, setSelected] = useState<ETFDetail | null>(null);
  const [category, setCategory] = useState('');
  const [sortBy, setSortBy] = useState('score');
  const [loading, setLoading] = useState(true);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const [etfData, catData] = await Promise.all([
        fetchETFs({ category: category || undefined, sort_by: sortBy }),
        fetchETFCategories(),
      ]);
      setEtfs(etfData);
      setCategories(catData);
    } catch (err) { console.error(err); }
    setLoading(false);
  }, [category, sortBy]);

  useEffect(() => { loadData(); }, [loadData]);

  const handleSelect = async (symbol: string) => {
    try {
      const detail = await fetchETFDetail(symbol);
      setSelected(detail);
    } catch (err) { console.error(err); }
  };

  const bestPicks = etfs.filter(e => e.total_score >= 8);

  return (
    <div>
      <div className="grid grid-cols-3 gap-4 mb-6">
        <div className="bg-[#1e2235] rounded-xl border border-green-500/30 p-4">
          <div className="text-green-400 text-xs mb-1">Best Picks (≥8/10)</div>
          <div className="text-2xl font-bold text-green-400">{bestPicks.length}</div>
        </div>
        <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] p-4">
          <div className="text-[#8b90a8] text-xs mb-1">Total ETFs</div>
          <div className="text-2xl font-bold">{etfs.length}</div>
        </div>
        <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] p-4">
          <div className="text-[#8b90a8] text-xs mb-1">Categories</div>
          <div className="text-2xl font-bold">{categories.length}</div>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] p-4 mb-6">
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <label className="text-xs text-[#8b90a8]">Category</label>
            <select value={category} onChange={e => setCategory(e.target.value)}
              className="bg-[#0f1117] border border-[#2a2e45] rounded-lg px-3 py-1.5 text-sm text-white focus:outline-none">
              <option value="">All</option>
              {categories.map(c => <option key={c} value={c}>{c}</option>)}
            </select>
          </div>
          <div className="flex items-center gap-2">
            <label className="text-xs text-[#8b90a8]">Sort By</label>
            <select value={sortBy} onChange={e => setSortBy(e.target.value)}
              className="bg-[#0f1117] border border-[#2a2e45] rounded-lg px-3 py-1.5 text-sm text-white focus:outline-none">
              <option value="score">Score</option>
              <option value="return_1y">1Y Return</option>
              <option value="expense">Expense (Low→High)</option>
              <option value="aum">AUM</option>
            </select>
          </div>
        </div>
      </div>

      {/* Table */}
      <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] overflow-hidden">
        {loading ? <div className="text-center py-20 text-[#8b90a8]">Loading...</div> : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-[#2a2e45] text-[#8b90a8] text-xs uppercase">
                  <th className="py-3 px-3 text-left">ETF</th>
                  <th className="py-3 px-2 text-center">Score</th>
                  <th className="py-3 px-2 text-left">Verdict</th>
                  <th className="py-3 px-2 text-right">Price</th>
                  <th className="py-3 px-2 text-right">1M</th>
                  <th className="py-3 px-2 text-right">3M</th>
                  <th className="py-3 px-2 text-right">1Y</th>
                  <th className="py-3 px-2 text-right">3Y CAGR</th>
                  <th className="py-3 px-2 text-right">Expense</th>
                  <th className="py-3 px-2 text-right">AUM (Cr)</th>
                  <th className="py-3 px-2 text-center">20W MA</th>
                  <th className="py-3 px-2 text-center">50W MA</th>
                  <th className="py-3 px-2 text-left">Category</th>
                </tr>
              </thead>
              <tbody>
                {etfs.map(e => (
                  <tr key={e.symbol} className="border-b border-[#2a2e45]/50 hover:bg-[#252940] cursor-pointer transition-colors" onClick={() => handleSelect(e.symbol)}>
                    <td className="py-3 px-3"><div className="font-semibold">{e.symbol}</div><div className="text-xs text-[#8b90a8] truncate max-w-[200px]">{e.name}</div></td>
                    <td className="py-3 px-2 text-center"><ScoreRing score={e.total_score} maxScore={10} size={38} /></td>
                    <td className="py-3 px-2"><ETFVerdictBadge verdict={e.verdict} /></td>
                    <td className="py-3 px-2 text-right font-mono">₹{e.current_price.toLocaleString('en-IN')}</td>
                    <td className="py-3 px-2 text-right"><ReturnCell value={e.return_1m} /></td>
                    <td className="py-3 px-2 text-right"><ReturnCell value={e.return_3m} /></td>
                    <td className="py-3 px-2 text-right"><ReturnCell value={e.return_1y} /></td>
                    <td className="py-3 px-2 text-right"><ReturnCell value={e.return_3y_cagr} /></td>
                    <td className={`py-3 px-2 text-right font-mono ${e.expense_ratio <= 0.2 ? 'text-green-400' : 'text-yellow-400'}`}>{e.expense_ratio.toFixed(2)}%</td>
                    <td className="py-3 px-2 text-right font-mono">{(e.aum_cr).toLocaleString('en-IN')}</td>
                    <td className="py-3 px-2 text-center">{e.above_20w_ma ? <span className="text-green-400">●</span> : <span className="text-red-400">●</span>}</td>
                    <td className="py-3 px-2 text-center">{e.above_50w_ma ? <span className="text-green-400">●</span> : <span className="text-red-400">●</span>}</td>
                    <td className="py-3 px-2"><span className="px-2 py-0.5 rounded text-xs bg-gray-500/20 text-gray-400">{e.category}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Legend */}
      <div className="mt-6 bg-[#1e2235] rounded-xl border border-[#2a2e45] p-5">
        <h3 className="text-lg font-bold mb-3">ETF Scoring Model (10-Point)</h3>
        <div className="grid grid-cols-5 gap-4 text-xs text-[#8b90a8]">
          <div><span className="text-blue-400 font-semibold">Trend</span><br/>Price vs 20W/50W MAs</div>
          <div><span className="text-purple-400 font-semibold">Momentum</span><br/>RSI zone + 3M/6M returns</div>
          <div><span className="text-green-400 font-semibold">Cost</span><br/>Expense ratio + tracking error</div>
          <div><span className="text-yellow-400 font-semibold">Liquidity</span><br/>AUM ≥₹5K Cr + volume</div>
          <div><span className="text-orange-400 font-semibold">Performance</span><br/>3Y/5Y CAGR vs category</div>
        </div>
      </div>

      {selected && <ETFDetailPanel etf={selected} onClose={() => setSelected(null)} />}
    </div>
  );
}
