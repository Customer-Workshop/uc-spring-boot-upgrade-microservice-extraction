'use client';

import { useCallback, useEffect, useState } from 'react';
import { SwingStockSummary, SwingStockDetail, fetchSwingStocks, fetchSwingStockDetail } from '@/lib/api';
import ScoreRing from './ScoreRing';

function SignalBadge({ signal }: { signal: string }) {
  const cls = signal === 'BUY' ? 'bg-[#00d09c]/10 text-[#00d09c] border-[#00d09c]/25'
    : signal === 'WATCH' ? 'bg-[#f5a623]/10 text-[#f5a623] border-[#f5a623]/25'
    : 'bg-[#eb5757]/10 text-[#eb5757] border-[#eb5757]/25';
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
    <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex justify-end" onClick={onClose}>
      <div className="bg-[#0d0d12] w-full max-w-2xl overflow-y-auto border-l border-[#2a2a3a]" onClick={e => e.stopPropagation()}>
        <div className="sticky top-0 bg-[#16161e] border-b border-[#2a2a3a] p-6 z-10">
          <div className="flex items-start justify-between">
            <div>
              <h2 className="text-xl font-bold text-white">{stock.symbol}</h2>
              <p className="text-[#8c8ca1] text-sm">{stock.name} · {stock.sector}</p>
            </div>
            <button onClick={onClose} className="text-[#5c5c72] hover:text-white text-2xl w-8 h-8 flex items-center justify-center rounded-full hover:bg-[#2a2a3a] transition-colors">×</button>
          </div>
          <div className="flex items-center justify-between mt-5">
            <div>
              <div className="text-3xl font-bold text-white">₹{stock.current_price.toLocaleString('en-IN')}</div>
              <div className={`text-sm font-mono ${stock.current_price > stock.prev_close ? 'text-[#00d09c]' : 'text-[#eb5757]'}`}>
                {((stock.current_price - stock.prev_close) / stock.prev_close * 100).toFixed(2)}%
              </div>
            </div>
            <div className="flex items-center gap-3">
              <ScoreRing score={s.total_score} maxScore={10} size={64} />
              <div>
                <SignalBadge signal={s.signal} />
                <div className="text-xs text-[#5c5c72] mt-1.5">R:R {s.risk_reward.toFixed(1)}</div>
              </div>
            </div>
          </div>
        </div>

        <div className="p-6 space-y-5">
          {/* Trade Setup */}
          {s.signal === 'BUY' && (
            <div className="bg-[#00d09c]/5 border border-[#00d09c]/20 rounded-2xl p-5">
              <h3 className="text-[#00d09c] font-semibold text-sm mb-4 flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-[#00d09c]"></span>
                Trade Setup
              </h3>
              <div className="grid grid-cols-4 gap-3 text-center">
                <div className="bg-[#16161e] rounded-xl p-3"><div className="text-[10px] text-[#5c5c72] uppercase tracking-wider mb-1">Entry</div><div className="font-mono text-[#00d09c] font-semibold">₹{s.entry_price.toFixed(0)}</div></div>
                <div className="bg-[#16161e] rounded-xl p-3"><div className="text-[10px] text-[#5c5c72] uppercase tracking-wider mb-1">Stop Loss</div><div className="font-mono text-[#eb5757] font-semibold">₹{s.stop_loss.toFixed(0)}</div></div>
                <div className="bg-[#16161e] rounded-xl p-3"><div className="text-[10px] text-[#5c5c72] uppercase tracking-wider mb-1">Target 1</div><div className="font-mono text-[#5b8def] font-semibold">₹{s.target_1.toFixed(0)}</div></div>
                <div className="bg-[#16161e] rounded-xl p-3"><div className="text-[10px] text-[#5c5c72] uppercase tracking-wider mb-1">Target 2</div><div className="font-mono text-[#5b8def] font-semibold">₹{s.target_2.toFixed(0)}</div></div>
              </div>
            </div>
          )}

          {/* Technical Indicators */}
          <div className="grid grid-cols-2 gap-4">
            <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5">
              <h3 className="text-[#5b8def] font-semibold text-sm mb-3 flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-[#5b8def]"></span>
                Moving Averages
              </h3>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between py-1.5 border-b border-[#2a2a3a]/40"><span className="text-[#8c8ca1]">SMA 20</span><span className="font-mono text-white">₹{stock.sma_20.toFixed(0)}</span></div>
                <div className="flex justify-between py-1.5 border-b border-[#2a2a3a]/40"><span className="text-[#8c8ca1]">SMA 50</span><span className="font-mono text-white">₹{stock.sma_50.toFixed(0)}</span></div>
                <div className="flex justify-between py-1.5"><span className="text-[#8c8ca1]">SMA 200</span><span className="font-mono text-white">₹{stock.sma_200.toFixed(0)}</span></div>
              </div>
            </div>
            <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5">
              <h3 className="text-[#a78bfa] font-semibold text-sm mb-3 flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-[#a78bfa]"></span>
                Momentum
              </h3>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between py-1.5 border-b border-[#2a2a3a]/40"><span className="text-[#8c8ca1]">RSI (14)</span><span className={`font-mono ${stock.rsi_14 > 55 ? 'text-[#00d09c]' : stock.rsi_14 < 40 ? 'text-[#eb5757]' : 'text-white'}`}>{stock.rsi_14.toFixed(1)}</span></div>
                <div className="flex justify-between py-1.5 border-b border-[#2a2a3a]/40"><span className="text-[#8c8ca1]">MACD Hist</span><span className={`font-mono ${stock.macd_histogram > 0 ? 'text-[#00d09c]' : 'text-[#eb5757]'}`}>{stock.macd_histogram.toFixed(2)}</span></div>
                <div className="flex justify-between py-1.5"><span className="text-[#8c8ca1]">ATR (14)</span><span className="font-mono text-white">₹{stock.atr_14.toFixed(1)}</span></div>
              </div>
            </div>
          </div>

          {/* Confirmation Ladder */}
          <h3 className="text-lg font-bold flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-[#00d09c]"></span>
            Confirmation Ladder
          </h3>
          <div className="space-y-2.5">
            {signals.map((sig, i) => (
              <div key={i} className={`rounded-xl px-4 py-3.5 ${sig.score === 2 ? 'bg-[#00d09c]/5 border border-[#00d09c]/20' : sig.score === 1 ? 'bg-[#f5a623]/5 border border-[#f5a623]/15' : 'bg-[#eb5757]/5 border border-[#eb5757]/15'}`}>
                <div className="flex items-center justify-between mb-1">
                  <span className="font-medium text-sm text-white">{sig.name}</span>
                  <span className={`text-xs font-bold px-2.5 py-0.5 rounded-full ${sig.score === 2 ? 'bg-[#00d09c]/15 text-[#00d09c]' : sig.score === 1 ? 'bg-[#f5a623]/15 text-[#f5a623]' : 'bg-[#eb5757]/15 text-[#eb5757]'}`}>
                    {sig.score}/2
                  </span>
                </div>
                <p className="text-xs text-[#8c8ca1]">{sig.detail}</p>
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
        <div className="bg-[#1c1c27] rounded-2xl border border-[#00d09c]/20 p-5 hover:border-[#00d09c]/40 transition-all">
          <div className="text-[#00d09c] text-xs font-medium mb-2 uppercase tracking-wider">BUY Signals (8-10/10)</div>
          <div className="text-3xl font-bold text-[#00d09c]">{buyCount}</div>
        </div>
        <div className="bg-[#1c1c27] rounded-2xl border border-[#f5a623]/20 p-5 hover:border-[#f5a623]/40 transition-all">
          <div className="text-[#f5a623] text-xs font-medium mb-2 uppercase tracking-wider">WATCH (6-7/10)</div>
          <div className="text-3xl font-bold text-[#f5a623]">{watchCount}</div>
        </div>
        <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5 hover:border-[#3a3a4a] transition-all">
          <div className="text-[#8c8ca1] text-xs font-medium mb-2 uppercase tracking-wider">Total Scanned</div>
          <div className="text-3xl font-bold text-white">{stocks.length}</div>
        </div>
      </div>

      {/* Filter */}
      <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5 mb-6">
        <div className="flex items-center gap-4">
          <label className="text-xs text-[#8c8ca1] font-medium">Signal</label>
          <select value={signalFilter} onChange={e => setSignalFilter(e.target.value)}
            className="bg-[#0d0d12] border border-[#2a2a3a] rounded-xl px-4 py-2 text-sm text-white focus:outline-none focus:border-[#00d09c] transition-colors">
            <option value="">All</option>
            <option value="BUY">BUY only</option>
            <option value="WATCH">WATCH only</option>
            <option value="SKIP">SKIP only</option>
          </select>
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
                  <th className="py-4 px-4 text-left">Stock</th>
                  <th className="py-4 px-3 text-center">Score</th>
                  <th className="py-4 px-3 text-center">Signal</th>
                  <th className="py-4 px-3 text-right">Price</th>
                  <th className="py-4 px-3 text-right">Change</th>
                  <th className="py-4 px-3 text-right">RSI</th>
                  <th className="py-4 px-3 text-right">MACD</th>
                  <th className="py-4 px-3 text-right">Vol Ratio</th>
                  <th className="py-4 px-3 text-center">Squeeze</th>
                  <th className="py-4 px-3 text-center">Breakout</th>
                  <th className="py-4 px-3 text-right">Entry</th>
                  <th className="py-4 px-3 text-right">SL</th>
                  <th className="py-4 px-3 text-right">T1</th>
                  <th className="py-4 px-3 text-right">R:R</th>
                </tr>
              </thead>
              <tbody>
                {stocks.map(s => (
                  <tr key={s.symbol} className="border-b border-[#2a2a3a]/40 hover:bg-[#00d09c]/[0.03] cursor-pointer transition-colors duration-150" onClick={() => handleSelect(s.symbol)}>
                    <td className="py-4 px-4"><div className="font-semibold text-white">{s.symbol}</div><div className="text-[11px] text-[#5c5c72]">{s.sector}</div></td>
                    <td className="py-4 px-3 text-center"><ScoreRing score={s.total_score} maxScore={10} size={38} /></td>
                    <td className="py-4 px-3 text-center"><SignalBadge signal={s.signal} /></td>
                    <td className="py-4 px-3 text-right font-mono text-[13px] text-white">₹{s.current_price.toLocaleString('en-IN')}</td>
                    <td className={`py-4 px-3 text-right font-mono text-[13px] ${s.change_pct >= 0 ? 'text-[#00d09c]' : 'text-[#eb5757]'}`}>{s.change_pct >= 0 ? '+' : ''}{s.change_pct.toFixed(2)}%</td>
                    <td className={`py-4 px-3 text-right font-mono text-[13px] ${s.rsi_14 > 55 ? 'text-[#00d09c]' : s.rsi_14 < 40 ? 'text-[#eb5757]' : 'text-white'}`}>{s.rsi_14.toFixed(1)}</td>
                    <td className={`py-4 px-3 text-right font-mono text-[13px] ${s.macd_histogram > 0 ? 'text-[#00d09c]' : 'text-[#eb5757]'}`}>{s.macd_histogram.toFixed(2)}</td>
                    <td className={`py-4 px-3 text-right font-mono text-[13px] ${s.volume_ratio >= 1.5 ? 'text-[#00d09c]' : s.volume_ratio >= 1.2 ? 'text-[#f5a623]' : 'text-[#5c5c72]'}`}>{s.volume_ratio.toFixed(2)}x</td>
                    <td className="py-4 px-3 text-center">{s.bb_squeeze ? <span className="text-[#f5a623]">●</span> : <span className="text-[#2a2a3a]">○</span>}</td>
                    <td className="py-4 px-3 text-center">{s.breakout ? <span className="text-[#00d09c]">●</span> : <span className="text-[#2a2a3a]">○</span>}</td>
                    <td className="py-4 px-3 text-right font-mono text-[13px] text-[#00d09c]">₹{s.entry_price.toFixed(0)}</td>
                    <td className="py-4 px-3 text-right font-mono text-[13px] text-[#eb5757]">₹{s.stop_loss.toFixed(0)}</td>
                    <td className="py-4 px-3 text-right font-mono text-[13px] text-[#5b8def]">₹{s.target_1.toFixed(0)}</td>
                    <td className={`py-4 px-3 text-right font-mono text-[13px] ${s.risk_reward >= 2 ? 'text-[#00d09c]' : 'text-[#f5a623]'}`}>{s.risk_reward.toFixed(1)}</td>
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
          Confirmation Ladder (10-Point Scoring)
        </h3>
        <div className="grid grid-cols-5 gap-4 text-xs text-[#8c8ca1]">
          <div className="bg-[#16161e] rounded-xl p-3"><span className="text-[#5b8def] font-semibold block mb-1">A. Trend</span>Price vs 50/200 MA alignment</div>
          <div className="bg-[#16161e] rounded-xl p-3"><span className="text-[#a78bfa] font-semibold block mb-1">B. Momentum</span>RSI &gt;55 + MACD crossover</div>
          <div className="bg-[#16161e] rounded-xl p-3"><span className="text-[#00d09c] font-semibold block mb-1">C. Volume</span>Volume &gt;1.5x average</div>
          <div className="bg-[#16161e] rounded-xl p-3"><span className="text-[#f5a623] font-semibold block mb-1">D. Volatility</span>Bollinger squeeze + expansion</div>
          <div className="bg-[#16161e] rounded-xl p-3"><span className="text-[#44d7f5] font-semibold block mb-1">E. Structure</span>Breakout above resistance</div>
        </div>
        <div className="mt-4 flex gap-3 text-xs">
          <span className="bg-[#00d09c]/10 text-[#00d09c] px-3 py-1.5 rounded-full border border-[#00d09c]/20 font-medium">8-10: BUY</span>
          <span className="bg-[#f5a623]/10 text-[#f5a623] px-3 py-1.5 rounded-full border border-[#f5a623]/20 font-medium">6-7: WATCH</span>
          <span className="bg-[#eb5757]/10 text-[#eb5757] px-3 py-1.5 rounded-full border border-[#eb5757]/20 font-medium">&lt;6: SKIP</span>
        </div>
      </div>

      {selected && <SwingDetailPanel stock={selected} onClose={() => setSelected(null)} />}
    </div>
  );
}
