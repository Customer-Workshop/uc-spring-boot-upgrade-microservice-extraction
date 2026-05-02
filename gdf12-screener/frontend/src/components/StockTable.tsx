'use client';

import { StockSummary } from '@/lib/api';
import ScoreRing from './ScoreRing';
import VerdictBadge from './VerdictBadge';

interface StockTableProps {
  stocks: StockSummary[];
  onSelect: (symbol: string) => void;
  watchlist: Set<string>;
  onToggleWatch: (symbol: string) => void;
}

export default function StockTable({ stocks, onSelect, watchlist, onToggleWatch }: StockTableProps) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="border-b border-[#2a2e45] text-[#8b90a8] text-xs uppercase">
            <th className="py-3 px-3 text-left">Stock</th>
            <th className="py-3 px-2 text-center">Score</th>
            <th className="py-3 px-2 text-left">Verdict</th>
            <th className="py-3 px-2 text-right">Price</th>
            <th className="py-3 px-2 text-right">PE</th>
            <th className="py-3 px-2 text-right">ROE</th>
            <th className="py-3 px-2 text-right">D/E</th>
            <th className="py-3 px-2 text-right">Promoter</th>
            <th className="py-3 px-2 text-right">Div Yld</th>
            <th className="py-3 px-2 text-right">MoS</th>
            <th className="py-3 px-2 text-center">Sector</th>
            <th className="py-3 px-2 text-center"></th>
          </tr>
        </thead>
        <tbody>
          {stocks.map((s) => (
            <tr
              key={s.symbol}
              className="border-b border-[#2a2e45]/50 hover:bg-[#1e2235] cursor-pointer transition-colors"
              onClick={() => onSelect(s.symbol)}
            >
              <td className="py-3 px-3">
                <div className="font-semibold">{s.symbol}</div>
                <div className="text-xs text-[#8b90a8] truncate max-w-[180px]">{s.name}</div>
              </td>
              <td className="py-3 px-2 text-center">
                <ScoreRing score={s.total_score} size={42} />
              </td>
              <td className="py-3 px-2">
                <VerdictBadge verdict={s.verdict} />
              </td>
              <td className="py-3 px-2 text-right font-mono">
                {s.current_price.toLocaleString('en-IN', { maximumFractionDigits: 0 })}
              </td>
              <td className={`py-3 px-2 text-right font-mono ${s.pe_ratio > 25 ? 'text-red-400' : s.pe_ratio > 0 ? 'text-green-400' : 'text-[#8b90a8]'}`}>
                {s.pe_ratio > 0 ? s.pe_ratio.toFixed(1) : 'N/A'}
              </td>
              <td className={`py-3 px-2 text-right font-mono ${s.roe >= 15 ? 'text-green-400' : 'text-orange-400'}`}>
                {s.roe.toFixed(1)}%
              </td>
              <td className={`py-3 px-2 text-right font-mono ${s.debt_to_equity < 0.5 ? 'text-green-400' : s.debt_to_equity < 1 ? 'text-yellow-400' : 'text-red-400'}`}>
                {s.debt_to_equity.toFixed(2)}
              </td>
              <td className={`py-3 px-2 text-right font-mono ${s.promoter_holding_pct >= 50 ? 'text-green-400' : 'text-orange-400'}`}>
                {s.promoter_holding_pct.toFixed(1)}%
              </td>
              <td className="py-3 px-2 text-right font-mono text-blue-400">
                {s.dividend_yield.toFixed(2)}%
              </td>
              <td className={`py-3 px-2 text-right font-mono ${s.margin_of_safety_pct > 0 ? 'text-green-400' : 'text-red-400'}`}>
                {s.margin_of_safety_pct.toFixed(0)}%
              </td>
              <td className="py-3 px-2 text-center">
                <span className={`px-2 py-0.5 rounded text-xs ${s.is_defensive_sector ? 'bg-blue-500/20 text-blue-300' : 'bg-gray-500/20 text-gray-400'}`}>
                  {s.sector}
                </span>
              </td>
              <td className="py-3 px-2 text-center">
                <button
                  onClick={(e) => { e.stopPropagation(); onToggleWatch(s.symbol); }}
                  className={`text-lg transition-colors ${watchlist.has(s.symbol) ? 'text-yellow-400' : 'text-[#2a2e45] hover:text-yellow-400/50'}`}
                  title={watchlist.has(s.symbol) ? 'Remove from watchlist' : 'Add to watchlist'}
                >
                  ★
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {stocks.length === 0 && (
        <div className="text-center py-12 text-[#8b90a8]">
          No stocks match the current filters
        </div>
      )}
    </div>
  );
}
