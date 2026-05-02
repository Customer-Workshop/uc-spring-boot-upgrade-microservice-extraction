'use client';

import { useCallback, useEffect, useState } from 'react';
import { SwingStockSummary, SwingStockDetail, fetchSwingStocks, fetchSwingStockDetail } from '@/lib/api';
import ScoreRing from './ScoreRing';

function SignalBadge({ signal }: { signal: string }) {
  const cls = signal === 'BUY' ? 'bg-green-500/20 text-green-400 border-green-500/30'
    : signal === 'WATCH' ? 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30'
    : 'bg-red-500/20 text-red-400 border-red-500/30';
  return <span className={`px-2.5 py-1 rounded-full text-xs font-semibold border ${cls}`}>{signal}</span>;
}

function SwingDetailPanel({ stock, onClose }: { stock: SwingStockDetail; onClose: () => void }) {
  const s = stock.score;
  const signals = [
    { name: 'Trend (MA Alignment)', score: s.trend?.score ?? 0, detail: s.trend?.detail ?? '' },
    { name: 'Momentum (RSI + MACD)', score: s.momentum?.score ?? 0, detail: s.momentum?.detail ?? '' },
    { name: 'Volume Confirmation', score: s.volume?.score ?? 0, detail: s.volume?.detail ?? '' },
    { name: 'Volatility (Bollinger)', score: s.volatility?.score ?? 0, detail: s.volatility?.detail ?? '' },
    { name: 'Structure (Breakout)', score: s.structure?.score ?? 0, detail: s.structure?.detail ?? '' },
  ];

  return (
    <div className="fixed inset-0 bg-black/60 z-50 flex justify-end" onClick={onClose}>
      <div className="bg-[#0f1117] w-full max-w-2xl overflow-y-auto border-l border-[#2a2e45]" onClick={e => e.stopPropagation()}>
        <div className="sticky top-0 bg-[#0f1117] border-b border-[#2a2e45] p-5 z-10">
          <div className="flex items-start justify-between">
            <div>
              <h2 className="text-xl font-bold">{stock.symbol}</h2>
              <p className="text-[#8b90a8] text-sm">{stock.name} · {stock.sector}</p>
            </div>
            <button onClick={onClose} className="text-[#8b90a8] hover:text-white text-2xl">×</button>
          </div>
          <div className="flex items-center justify-between mt-4">
            <div>
              <div className="text-3xl font-bold">₹{stock.current_price.toLocaleString('en-IN')}</div>
              <div className={`text-sm font-mono ${stock.current_price > stock.prev_close ? 'text-green-400' : 'text-red-400'}`}>
                {((stock.current_price - stock.prev_close) / stock.prev_close * 100).toFixed(2)}%
              </div>
            </div>
            <div className="flex items-center gap-3">
              <ScoreRing score={s.total_score} maxScore={10} size={64} />
              <div>
                <SignalBadge signal={s.signal} />
                <div className="text-xs text-[#8b90a8] mt-1">R:R {s.risk_reward.toFixed(1)}</div>
              </div>
            </div>
          </div>
        </div>

        <div className="p-5 space-y-4">
          {/* Trade Setup */}
          {s.signal === 'BUY' && (
            <div className="bg-green-500/10 border border-green-500/30 rounded-xl p-4">
              <h3 className="text-green-400 font-semibold text-sm mb-3">Trade Setup</h3>
              <div className="grid grid-cols-4 gap-3 text-center">
                <div><div className="text-xs text-[#8b90a8]">Entry</div><div className="font-mono text-green-400">₹{s.entry_price.toFixed(0)}</div></div>
                <div><div className="text-xs text-[#8b90a8]">Stop Loss</div><div className="font-mono text-red-400">₹{s.stop_loss.toFixed(0)}</div></div>
                <div><div className="text-xs text-[#8b90a8]">Target 1</div><div className="font-mono text-blue-400">₹{s.target_1.toFixed(0)}</div></div>
                <div><div className="text-xs text-[#8b90a8]">Target 2</div><div className="font-mono text-blue-400">₹{s.target_2.toFixed(0)}</div></div>
              </div>
            </div>
          )}

          {/* Technical Indicators */}
          <div className="grid grid-cols-2 gap-4">
            <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] p-4">
              <h3 className="text-blue-400 font-semibold text-sm mb-2">Moving Averages</h3>
              <div className="space-y-1 text-sm">
                <div className="flex justify-between"><span className="text-[#8b90a8]">SMA 20</span><span className="font-mono">₹{stock.sma_20.toFixed(0)}</span></div>
                <div className="flex justify-between"><span className="text-[#8b90a8]">SMA 50</span><span className="font-mono">₹{stock.sma_50.toFixed(0)}</span></div>
                <div className="flex justify-between"><span className="text-[#8b90a8]">SMA 200</span><span className="font-mono">₹{stock.sma_200.toFixed(0)}</span></div>
              </div>
            </div>
            <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] p-4">
              <h3 className="text-purple-400 font-semibold text-sm mb-2">Momentum</h3>
              <div className="space-y-1 text-sm">
                <div className="flex justify-between"><span className="text-[#8b90a8]">RSI (14)</span><span className={`font-mono ${stock.rsi_14 > 55 ? 'text-green-400' : stock.rsi_14 < 40 ? 'text-red-400' : ''}`}>{stock.rsi_14.toFixed(1)}</span></div>
                <div className="flex justify-between"><span className="text-[#8b90a8]">MACD Hist</span><span className={`font-mono ${stock.macd_histogram > 0 ? 'text-green-400' : 'text-red-400'}`}>{stock.macd_histogram.toFixed(2)}</span></div>
                <div className="flex justify-between"><span className="text-[#8b90a8]">ATR (14)</span><span className="font-mono">₹{stock.atr_14.toFixed(1)}</span></div>
              </div>
            </div>
          </div>

          {/* Confirmation Ladder */}
          <h3 className="text-lg font-bold">Confirmation Ladder</h3>
          <div className="space-y-2">
            {signals.map((sig, i) => (
              <div key={i} className={`rounded-lg px-4 py-3 ${sig.score === 2 ? 'signal-pass' : sig.score === 1 ? 'bg-yellow-500/10 border border-yellow-500/20' : 'signal-fail'}`}>
                <div className="flex items-center justify-between mb-1">
                  <span className="font-medium text-sm">{sig.name}</span>
                  <span className={`text-xs font-bold px-2 py-0.5 rounded ${sig.score === 2 ? 'bg-green-500/30 text-green-400' : sig.score === 1 ? 'bg-yellow-500/30 text-yellow-400' : 'bg-red-500/30 text-red-400'}`}>
                    {sig.score}/2
                  </span>
                </div>
                <p className="text-xs text-[#8b90a8]">{sig.detail}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default function SwingTab() {
  const [stocks, setStocks] = useState<SwingStockSummary[]>([]);
  const [selected, setSelected] = useState<SwingStockDetail | null>(null);
  const [signalFilter, setSignalFilter] = useState('');
  const [loading, setLoading] = useState(true);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const data = await fetchSwingStocks({ signal: signalFilter || undefined });
      setStocks(data);
    } catch (err) { console.error(err); }
    setLoading(false);
  }, [signalFilter]);

  useEffect(() => { loadData(); }, [loadData]);

  const handleSelect = async (symbol: string) => {
    try {
      const detail = await fetchSwingStockDetail(symbol);
      setSelected(detail);
    } catch (err) { console.error(err); }
  };

  const buyCount = stocks.filter(s => s.signal === 'BUY').length;
  const watchCount = stocks.filter(s => s.signal === 'WATCH').length;

  return (
    <div>
      {/* Summary */}
      <div className="grid grid-cols-3 gap-4 mb-6">
        <div className="bg-[#1e2235] rounded-xl border border-green-500/30 p-4">
          <div className="text-green-400 text-xs mb-1">BUY Signals (8-10/10)</div>
          <div className="text-2xl font-bold text-green-400">{buyCount}</div>
        </div>
        <div className="bg-[#1e2235] rounded-xl border border-yellow-500/30 p-4">
          <div className="text-yellow-400 text-xs mb-1">WATCH (6-7/10)</div>
          <div className="text-2xl font-bold text-yellow-400">{watchCount}</div>
        </div>
        <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] p-4">
          <div className="text-[#8b90a8] text-xs mb-1">Total Scanned</div>
          <div className="text-2xl font-bold">{stocks.length}</div>
        </div>
      </div>

      {/* Filter */}
      <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] p-4 mb-6">
        <div className="flex items-center gap-4">
          <label className="text-xs text-[#8b90a8]">Signal</label>
          <select value={signalFilter} onChange={e => setSignalFilter(e.target.value)}
            className="bg-[#0f1117] border border-[#2a2e45] rounded-lg px-3 py-1.5 text-sm text-white focus:outline-none">
            <option value="">All</option>
            <option value="BUY">BUY only</option>
            <option value="WATCH">WATCH only</option>
            <option value="SKIP">SKIP only</option>
          </select>
        </div>
      </div>

      {/* Table */}
      <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] overflow-hidden">
        {loading ? <div className="text-center py-20 text-[#8b90a8]">Loading...</div> : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-[#2a2e45] text-[#8b90a8] text-xs uppercase">
                  <th className="py-3 px-3 text-left">Stock</th>
                  <th className="py-3 px-2 text-center">Score</th>
                  <th className="py-3 px-2 text-center">Signal</th>
                  <th className="py-3 px-2 text-right">Price</th>
                  <th className="py-3 px-2 text-right">Change</th>
                  <th className="py-3 px-2 text-right">RSI</th>
                  <th className="py-3 px-2 text-right">MACD</th>
                  <th className="py-3 px-2 text-right">Vol Ratio</th>
                  <th className="py-3 px-2 text-center">Squeeze</th>
                  <th className="py-3 px-2 text-center">Breakout</th>
                  <th className="py-3 px-2 text-right">Entry</th>
                  <th className="py-3 px-2 text-right">SL</th>
                  <th className="py-3 px-2 text-right">T1</th>
                  <th className="py-3 px-2 text-right">R:R</th>
                </tr>
              </thead>
              <tbody>
                {stocks.map(s => (
                  <tr key={s.symbol} className="border-b border-[#2a2e45]/50 hover:bg-[#252940] cursor-pointer transition-colors" onClick={() => handleSelect(s.symbol)}>
                    <td className="py-3 px-3"><div className="font-semibold">{s.symbol}</div><div className="text-xs text-[#8b90a8]">{s.sector}</div></td>
                    <td className="py-3 px-2 text-center"><ScoreRing score={s.total_score} maxScore={10} size={38} /></td>
                    <td className="py-3 px-2 text-center"><SignalBadge signal={s.signal} /></td>
                    <td className="py-3 px-2 text-right font-mono">₹{s.current_price.toLocaleString('en-IN')}</td>
                    <td className={`py-3 px-2 text-right font-mono ${s.change_pct >= 0 ? 'text-green-400' : 'text-red-400'}`}>{s.change_pct >= 0 ? '+' : ''}{s.change_pct.toFixed(2)}%</td>
                    <td className={`py-3 px-2 text-right font-mono ${s.rsi_14 > 55 ? 'text-green-400' : s.rsi_14 < 40 ? 'text-red-400' : ''}`}>{s.rsi_14.toFixed(1)}</td>
                    <td className={`py-3 px-2 text-right font-mono ${s.macd_histogram > 0 ? 'text-green-400' : 'text-red-400'}`}>{s.macd_histogram.toFixed(2)}</td>
                    <td className={`py-3 px-2 text-right font-mono ${s.volume_ratio >= 1.5 ? 'text-green-400' : s.volume_ratio >= 1.2 ? 'text-yellow-400' : 'text-[#8b90a8]'}`}>{s.volume_ratio.toFixed(2)}x</td>
                    <td className="py-3 px-2 text-center">{s.bb_squeeze ? <span className="text-yellow-400">●</span> : <span className="text-[#2a2e45]">○</span>}</td>
                    <td className="py-3 px-2 text-center">{s.breakout ? <span className="text-green-400">●</span> : <span className="text-[#2a2e45]">○</span>}</td>
                    <td className="py-3 px-2 text-right font-mono text-green-400">₹{s.entry_price.toFixed(0)}</td>
                    <td className="py-3 px-2 text-right font-mono text-red-400">₹{s.stop_loss.toFixed(0)}</td>
                    <td className="py-3 px-2 text-right font-mono text-blue-400">₹{s.target_1.toFixed(0)}</td>
                    <td className={`py-3 px-2 text-right font-mono ${s.risk_reward >= 2 ? 'text-green-400' : 'text-yellow-400'}`}>{s.risk_reward.toFixed(1)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Legend */}
      <div className="mt-6 bg-[#1e2235] rounded-xl border border-[#2a2e45] p-5">
        <h3 className="text-lg font-bold mb-3">Confirmation Ladder (10-Point Scoring)</h3>
        <div className="grid grid-cols-5 gap-4 text-xs text-[#8b90a8]">
          <div><span className="text-blue-400 font-semibold">A. Trend</span><br/>Price vs 50/200 MA alignment</div>
          <div><span className="text-purple-400 font-semibold">B. Momentum</span><br/>RSI &gt;55 + MACD crossover</div>
          <div><span className="text-green-400 font-semibold">C. Volume</span><br/>Volume &gt;1.5x average</div>
          <div><span className="text-yellow-400 font-semibold">D. Volatility</span><br/>Bollinger squeeze + expansion</div>
          <div><span className="text-orange-400 font-semibold">E. Structure</span><br/>Breakout above resistance</div>
        </div>
        <div className="mt-3 flex gap-3 text-xs">
          <span className="bg-green-500/20 text-green-400 px-2 py-1 rounded">8-10: BUY</span>
          <span className="bg-yellow-500/20 text-yellow-400 px-2 py-1 rounded">6-7: WATCH</span>
          <span className="bg-red-500/20 text-red-400 px-2 py-1 rounded">&lt;6: SKIP</span>
        </div>
      </div>

      {selected && <SwingDetailPanel stock={selected} onClose={() => setSelected(null)} />}
    </div>
  );
}
