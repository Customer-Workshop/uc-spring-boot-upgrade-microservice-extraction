'use client';

import { useCallback, useEffect, useState } from 'react';
import { ETFSummary, ETFDetail, fetchETFs, fetchETFDetail, fetchETFCategories } from '@/lib/api';
import ScoreRing from './ScoreRing';

function ETFVerdictBadge({ verdict }: { verdict: string }) {
  const cls = verdict === 'Best Pick' ? 'bg-[#00d09c]/10 text-[#00d09c] border-[#00d09c]/25'
    : verdict === 'Good Choice' ? 'bg-[#5b8def]/10 text-[#5b8def] border-[#5b8def]/25'
    : verdict === 'Average' ? 'bg-[#f5a623]/10 text-[#f5a623] border-[#f5a623]/25'
    : 'bg-[#eb5757]/10 text-[#eb5757] border-[#eb5757]/25';
  return <span className={`px-2.5 py-1 rounded-full text-xs font-semibold border ${cls}`}>{verdict}</span>;
}

function ReturnCell({ value }: { value: number }) {
  return (
    <span className={`font-mono text-[13px] ${value > 0 ? 'text-[#00d09c]' : value < 0 ? 'text-[#eb5757]' : 'text-[#5c5c72]'}`}>
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
    <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex justify-end" onClick={onClose}>
      <div className="bg-[#0d0d12] w-full max-w-2xl overflow-y-auto border-l border-[#2a2a3a]" onClick={e => e.stopPropagation()}>
        <div className="sticky top-0 bg-[#16161e] border-b border-[#2a2a3a] p-6 z-10">
          <div className="flex items-start justify-between">
            <div>
              <h2 className="text-xl font-bold text-white">{etf.symbol}</h2>
              <p className="text-[#8c8ca1] text-sm">{etf.name}</p>
              <p className="text-[#5c5c72] text-xs">{etf.category} · {etf.amc}</p>
            </div>
            <button onClick={onClose} className="text-[#5c5c72] hover:text-white text-2xl w-8 h-8 flex items-center justify-center rounded-full hover:bg-[#2a2a3a] transition-colors">×</button>
          </div>
          <div className="flex items-center justify-between mt-5">
            <div>
              <div className="text-3xl font-bold text-white">₹{etf.current_price.toLocaleString('en-IN')}</div>
              <div className="text-xs text-[#5c5c72] mt-1">NAV: ₹{etf.nav} · 52W: ₹{etf.low_52w}–₹{etf.high_52w}</div>
            </div>
            <div className="flex items-center gap-3">
              <ScoreRing score={s.total_score} maxScore={10} size={64} />
              <ETFVerdictBadge verdict={s.verdict} />
            </div>
          </div>
        </div>

        <div className="p-6 space-y-5">
          {/* Returns Grid */}
          <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5">
            <h3 className="text-[#00d09c] font-semibold text-sm mb-4 flex items-center gap-2">
              <span className="w-1.5 h-1.5 rounded-full bg-[#00d09c]"></span>
              Returns
            </h3>
            <div className="grid grid-cols-4 gap-3 text-center text-sm">
              <div className="bg-[#16161e] rounded-xl p-3"><div className="text-[10px] text-[#5c5c72] uppercase tracking-wider mb-1">1W</div><ReturnCell value={etf.return_1w} /></div>
              <div className="bg-[#16161e] rounded-xl p-3"><div className="text-[10px] text-[#5c5c72] uppercase tracking-wider mb-1">1M</div><ReturnCell value={etf.return_1m} /></div>
              <div className="bg-[#16161e] rounded-xl p-3"><div className="text-[10px] text-[#5c5c72] uppercase tracking-wider mb-1">3M</div><ReturnCell value={etf.return_3m} /></div>
              <div className="bg-[#16161e] rounded-xl p-3"><div className="text-[10px] text-[#5c5c72] uppercase tracking-wider mb-1">6M</div><ReturnCell value={etf.return_6m} /></div>
              <div className="bg-[#16161e] rounded-xl p-3"><div className="text-[10px] text-[#5c5c72] uppercase tracking-wider mb-1">1Y</div><ReturnCell value={etf.return_1y} /></div>
              <div className="bg-[#16161e] rounded-xl p-3"><div className="text-[10px] text-[#5c5c72] uppercase tracking-wider mb-1">3Y CAGR</div><ReturnCell value={etf.return_3y_cagr} /></div>
              <div className="bg-[#16161e] rounded-xl p-3"><div className="text-[10px] text-[#5c5c72] uppercase tracking-wider mb-1">5Y CAGR</div><ReturnCell value={etf.return_5y_cagr} /></div>
              <div className="bg-[#16161e] rounded-xl p-3"><div className="text-[10px] text-[#5c5c72] uppercase tracking-wider mb-1">RSI</div><span className="font-mono text-white">{etf.rsi_14.toFixed(0)}</span></div>
            </div>
          </div>

          {/* Cost & Liquidity */}
          <div className="grid grid-cols-2 gap-4">
            <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5">
              <h3 className="text-[#5b8def] font-semibold text-sm mb-3 flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-[#5b8def]"></span>
                Cost
              </h3>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between py-1.5 border-b border-[#2a2a3a]/40"><span className="text-[#8c8ca1]">Expense Ratio</span><span className={`font-mono ${etf.expense_ratio <= 0.2 ? 'text-[#00d09c]' : 'text-[#f5a623]'}`}>{etf.expense_ratio.toFixed(2)}%</span></div>
                <div className="flex justify-between py-1.5"><span className="text-[#8c8ca1]">Tracking Error</span><span className={`font-mono ${etf.tracking_error <= 0.5 ? 'text-[#00d09c]' : 'text-[#f5a623]'}`}>{etf.tracking_error.toFixed(2)}%</span></div>
              </div>
            </div>
            <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5">
              <h3 className="text-[#a78bfa] font-semibold text-sm mb-3 flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-[#a78bfa]"></span>
                Liquidity
              </h3>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between py-1.5 border-b border-[#2a2a3a]/40"><span className="text-[#8c8ca1]">AUM</span><span className="font-mono text-white">₹{(etf.aum_cr).toLocaleString('en-IN')} Cr</span></div>
                <div className="flex justify-between py-1.5"><span className="text-[#8c8ca1]">Avg Volume</span><span className="font-mono text-white">{etf.avg_volume.toLocaleString('en-IN')}</span></div>
              </div>
            </div>
          </div>

          {/* MA Position */}
          <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5">
            <h3 className="text-[#f5a623] font-semibold text-sm mb-3 flex items-center gap-2">
              <span className="w-1.5 h-1.5 rounded-full bg-[#f5a623]"></span>
              Moving Average Position
            </h3>
            <div className="grid grid-cols-2 gap-3 text-sm">
              <div className="flex justify-between py-1.5 border-b border-[#2a2a3a]/40"><span className="text-[#8c8ca1]">20W MA</span><span className="font-mono text-white">₹{etf.sma_20w.toFixed(0)} <span className={etf.above_20w_ma ? 'text-[#00d09c]' : 'text-[#eb5757]'}>{etf.above_20w_ma ? '●' : '●'}</span></span></div>
              <div className="flex justify-between py-1.5 border-b border-[#2a2a3a]/40"><span className="text-[#8c8ca1]">50W MA</span><span className="font-mono text-white">₹{etf.sma_50w.toFixed(0)} <span className={etf.above_50w_ma ? 'text-[#00d09c]' : 'text-[#eb5757]'}>{etf.above_50w_ma ? '●' : '●'}</span></span></div>
              <div className="flex justify-between py-1.5"><span className="text-[#8c8ca1]">10M MA</span><span className="font-mono text-white">₹{etf.sma_10m.toFixed(0)}</span></div>
              <div className="flex justify-between py-1.5"><span className="text-[#8c8ca1]">12M MA</span><span className="font-mono text-white">₹{etf.sma_12m.toFixed(0)}</span></div>
            </div>
          </div>

          {/* Score Breakdown */}
          <h3 className="text-lg font-bold flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-[#00d09c]"></span>
            Score Breakdown
          </h3>
          <div className="space-y-2.5">
            {factors.map((f, i) => (
              <div key={i} className={`rounded-xl px-4 py-3.5 ${f.score === 2 ? 'bg-[#00d09c]/5 border border-[#00d09c]/20' : f.score === 1 ? 'bg-[#f5a623]/5 border border-[#f5a623]/15' : 'bg-[#eb5757]/5 border border-[#eb5757]/15'}`}>
                <div className="flex items-center justify-between mb-1">
                  <span className="font-medium text-sm text-white">{f.name}</span>
                  <span className={`text-xs font-bold px-2.5 py-0.5 rounded-full ${f.score === 2 ? 'bg-[#00d09c]/15 text-[#00d09c]' : f.score === 1 ? 'bg-[#f5a623]/15 text-[#f5a623]' : 'bg-[#eb5757]/15 text-[#eb5757]'}`}>{f.score}/2</span>
                </div>
                <p className="text-xs text-[#8c8ca1]">{f.detail}</p>
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
        <div className="bg-[#1c1c27] rounded-2xl border border-[#00d09c]/20 p-5 hover:border-[#00d09c]/40 transition-all">
          <div className="text-[#00d09c] text-xs font-medium mb-2 uppercase tracking-wider">Best Picks (≥8/10)</div>
          <div className="text-3xl font-bold text-[#00d09c]">{bestPicks.length}</div>
        </div>
        <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5 hover:border-[#3a3a4a] transition-all">
          <div className="text-[#8c8ca1] text-xs font-medium mb-2 uppercase tracking-wider">Total ETFs</div>
          <div className="text-3xl font-bold text-white">{etfs.length}</div>
        </div>
        <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5 hover:border-[#3a3a4a] transition-all">
          <div className="text-[#8c8ca1] text-xs font-medium mb-2 uppercase tracking-wider">Categories</div>
          <div className="text-3xl font-bold text-white">{categories.length}</div>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5 mb-6">
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <label className="text-xs text-[#8c8ca1] font-medium">Category</label>
            <select value={category} onChange={e => setCategory(e.target.value)}
              className="bg-[#0d0d12] border border-[#2a2a3a] rounded-xl px-4 py-2 text-sm text-white focus:outline-none focus:border-[#00d09c] transition-colors">
              <option value="">All</option>
              {categories.map(c => <option key={c} value={c}>{c}</option>)}
            </select>
          </div>
          <div className="flex items-center gap-2">
            <label className="text-xs text-[#8c8ca1] font-medium">Sort By</label>
            <select value={sortBy} onChange={e => setSortBy(e.target.value)}
              className="bg-[#0d0d12] border border-[#2a2a3a] rounded-xl px-4 py-2 text-sm text-white focus:outline-none focus:border-[#00d09c] transition-colors">
              <option value="score">Score</option>
              <option value="return_1y">1Y Return</option>
              <option value="expense">Expense (Low→High)</option>
              <option value="aum">AUM</option>
            </select>
          </div>
        </div>
      </div>

      {/* Table */}
      <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] overflow-hidden mb-6">
        {loading ? (
          <div className="text-center py-20 text-[#8c8ca1]">
            <div className="inline-block w-8 h-8 border-2 border-[#00d09c] border-t-transparent rounded-full animate-spin mb-3"></div>
            <div>Loading...</div>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-[#2a2a3a] text-[#5c5c72] text-[11px] uppercase tracking-wider font-medium">
                  <th className="py-4 px-4 text-left">ETF</th>
                  <th className="py-4 px-3 text-center">Score</th>
                  <th className="py-4 px-3 text-left">Verdict</th>
                  <th className="py-4 px-3 text-right">Price</th>
                  <th className="py-4 px-3 text-right">1M</th>
                  <th className="py-4 px-3 text-right">3M</th>
                  <th className="py-4 px-3 text-right">1Y</th>
                  <th className="py-4 px-3 text-right">3Y CAGR</th>
                  <th className="py-4 px-3 text-right">Expense</th>
                  <th className="py-4 px-3 text-right">AUM (Cr)</th>
                  <th className="py-4 px-3 text-center">20W MA</th>
                  <th className="py-4 px-3 text-center">50W MA</th>
                  <th className="py-4 px-3 text-left">Category</th>
                </tr>
              </thead>
              <tbody>
                {etfs.map(e => (
                  <tr key={e.symbol} className="border-b border-[#2a2a3a]/40 hover:bg-[#00d09c]/[0.03] cursor-pointer transition-colors duration-150" onClick={() => handleSelect(e.symbol)}>
                    <td className="py-4 px-4"><div className="font-semibold text-white">{e.symbol}</div><div className="text-[11px] text-[#5c5c72] truncate max-w-[200px]">{e.name}</div></td>
                    <td className="py-4 px-3 text-center"><ScoreRing score={e.total_score} maxScore={10} size={38} /></td>
                    <td className="py-4 px-3"><ETFVerdictBadge verdict={e.verdict} /></td>
                    <td className="py-4 px-3 text-right font-mono text-[13px] text-white">₹{e.current_price.toLocaleString('en-IN')}</td>
                    <td className="py-4 px-3 text-right"><ReturnCell value={e.return_1m} /></td>
                    <td className="py-4 px-3 text-right"><ReturnCell value={e.return_3m} /></td>
                    <td className="py-4 px-3 text-right"><ReturnCell value={e.return_1y} /></td>
                    <td className="py-4 px-3 text-right"><ReturnCell value={e.return_3y_cagr} /></td>
                    <td className={`py-4 px-3 text-right font-mono text-[13px] ${e.expense_ratio <= 0.2 ? 'text-[#00d09c]' : 'text-[#f5a623]'}`}>{e.expense_ratio.toFixed(2)}%</td>
                    <td className="py-4 px-3 text-right font-mono text-[13px] text-white">{(e.aum_cr).toLocaleString('en-IN')}</td>
                    <td className="py-4 px-3 text-center">{e.above_20w_ma ? <span className="text-[#00d09c]">●</span> : <span className="text-[#eb5757]">●</span>}</td>
                    <td className="py-4 px-3 text-center">{e.above_50w_ma ? <span className="text-[#00d09c]">●</span> : <span className="text-[#eb5757]">●</span>}</td>
                    <td className="py-4 px-3"><span className="px-2.5 py-1 rounded-full text-[10px] font-medium bg-[#2a2a3a] text-[#8c8ca1]">{e.category}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Legend */}
      <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-6">
        <h3 className="text-lg font-bold mb-4 flex items-center gap-2">
          <span className="w-2 h-2 rounded-full bg-[#00d09c]"></span>
          ETF Scoring Model (10-Point)
        </h3>
        <div className="grid grid-cols-5 gap-4 text-xs text-[#8c8ca1]">
          <div className="bg-[#16161e] rounded-xl p-3"><span className="text-[#5b8def] font-semibold block mb-1">Trend</span>Price vs 20W/50W MAs</div>
          <div className="bg-[#16161e] rounded-xl p-3"><span className="text-[#a78bfa] font-semibold block mb-1">Momentum</span>RSI zone + 3M/6M returns</div>
          <div className="bg-[#16161e] rounded-xl p-3"><span className="text-[#00d09c] font-semibold block mb-1">Cost</span>Expense ratio + tracking error</div>
          <div className="bg-[#16161e] rounded-xl p-3"><span className="text-[#f5a623] font-semibold block mb-1">Liquidity</span>AUM ≥₹5K Cr + volume</div>
          <div className="bg-[#16161e] rounded-xl p-3"><span className="text-[#44d7f5] font-semibold block mb-1">Performance</span>3Y/5Y CAGR vs category</div>
        </div>
      </div>

      {selected && <ETFDetailPanel etf={selected} onClose={() => setSelected(null)} />}
    </div>
  );
}
