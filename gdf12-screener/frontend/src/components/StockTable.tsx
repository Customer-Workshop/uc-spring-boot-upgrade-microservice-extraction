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
          <tr className="border-b border-[#2a2a3a] text-[#5c5c72] text-[11px] uppercase tracking-wider font-medium">
            <th className="py-4 px-4 text-left">Stock</th>
            <th className="py-4 px-3 text-center">Score</th>
            <th className="py-4 px-3 text-left">Verdict</th>
            <th className="py-4 px-3 text-right">Price</th>
            <th className="py-4 px-3 text-right">PE</th>
            <th className="py-4 px-3 text-right">ROE</th>
            <th className="py-4 px-3 text-right">D/E</th>
            <th className="py-4 px-3 text-right">Promoter</th>
            <th className="py-4 px-3 text-right">Div Yld</th>
            <th className="py-4 px-3 text-right">MoS</th>
            <th className="py-4 px-3 text-center">Sector</th>
            <th className="py-4 px-2 text-center w-10"></th>
          </tr>
        </thead>
        <tbody>
          {stocks.map((s) => (
            <tr
              key={s.symbol}
              className="border-b border-[#2a2a3a]/40 hover:bg-[#00d09c]/[0.03] cursor-pointer transition-colors duration-150"
              onClick={() => onSelect(s.symbol)}
            >
              <td className="py-4 px-4">
                <div className="font-semibold text-white">{s.symbol}</div>
                <div className="text-[11px] text-[#5c5c72] truncate max-w-[180px] mt-0.5">{s.name}</div>
              </td>
              <td className="py-4 px-3 text-center">
                <ScoreRing score={s.total_score} size={44} />
              </td>
              <td className="py-4 px-3">
                <VerdictBadge verdict={s.verdict} />
              </td>
              <td className="py-4 px-3 text-right font-mono text-[13px] text-white">
                {s.current_price.toLocaleString('en-IN', { maximumFractionDigits: 0 })}
              </td>
              <td className={`py-4 px-3 text-right font-mono text-[13px] ${s.pe_ratio > 25 ? 'text-[#eb5757]' : s.pe_ratio > 0 ? 'text-[#00d09c]' : 'text-[#5c5c72]'}`}>
                {s.pe_ratio > 0 ? s.pe_ratio.toFixed(1) : 'N/A'}
              </td>
              <td className={`py-4 px-3 text-right font-mono text-[13px] ${s.roe >= 15 ? 'text-[#00d09c]' : 'text-[#f5a623]'}`}>
                {s.roe.toFixed(1)}%
              </td>
              <td className={`py-4 px-3 text-right font-mono text-[13px] ${s.debt_to_equity < 0.5 ? 'text-[#00d09c]' : s.debt_to_equity < 1 ? 'text-[#f5a623]' : 'text-[#eb5757]'}`}>
                {s.debt_to_equity.toFixed(2)}
              </td>
              <td className={`py-4 px-3 text-right font-mono text-[13px] ${s.promoter_holding_pct >= 50 ? 'text-[#00d09c]' : 'text-[#f5a623]'}`}>
                {s.promoter_holding_pct.toFixed(1)}%
              </td>
              <td className="py-4 px-3 text-right font-mono text-[13px] text-[#5b8def]">
                {s.dividend_yield.toFixed(2)}%
              </td>
              <td className={`py-4 px-3 text-right font-mono text-[13px] ${s.margin_of_safety_pct > 0 ? 'text-[#00d09c]' : 'text-[#eb5757]'}`}>
                {s.margin_of_safety_pct.toFixed(0)}%
              </td>
              <td className="py-4 px-3 text-center">
                <span className={`px-2.5 py-1 rounded-full text-[10px] font-medium ${s.is_defensive_sector ? 'bg-[#5b8def]/10 text-[#5b8def] border border-[#5b8def]/20' : 'bg-[#2a2a3a] text-[#5c5c72]'}`}>
                  {s.sector}
                </span>
              </td>
              <td className="py-4 px-2 text-center">
                <button
                  onClick={(e) => { e.stopPropagation(); onToggleWatch(s.symbol); }}
                  className={`text-lg transition-all duration-200 ${watchlist.has(s.symbol) ? 'text-[#f5a623] scale-110' : 'text-[#2a2a3a] hover:text-[#f5a623]/50'}`}
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
        <div className="text-center py-16 text-[#5c5c72]">
          <div className="text-4xl mb-3 opacity-30">📊</div>
          No stocks match the current filters
        </div>
      )}
    </div>
  );
}
