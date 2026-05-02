'use client';

import { StockDetail } from '@/lib/api';
import ScoreRing from './ScoreRing';
import SignalCard from './SignalCard';
import VerdictBadge from './VerdictBadge';

interface StockDetailPanelProps {
  stock: StockDetail;
  onClose: () => void;
}

function MetricRow({ label, value, unit, good }: { label: string; value: string | number; unit?: string; good?: boolean }) {
  return (
    <div className="flex justify-between items-center py-1.5 border-b border-[#2a2e45]/50">
      <span className="text-[#8b90a8] text-sm">{label}</span>
      <span className={`font-mono text-sm ${good === true ? 'text-green-400' : good === false ? 'text-red-400' : ''}`}>
        {typeof value === 'number' ? value.toLocaleString('en-IN') : value}
        {unit && <span className="text-[#8b90a8] text-xs ml-1">{unit}</span>}
      </span>
    </div>
  );
}

export default function StockDetailPanel({ stock, onClose }: StockDetailPanelProps) {
  const gdf = stock.gdf_score;
  const priceRange52w = stock.high_52w - stock.low_52w;
  const pricePosition = priceRange52w > 0 ? ((stock.current_price - stock.low_52w) / priceRange52w) * 100 : 50;

  return (
    <div className="fixed inset-0 bg-black/60 z-50 flex justify-end" onClick={onClose}>
      <div
        className="bg-[#0f1117] w-full max-w-3xl overflow-y-auto border-l border-[#2a2e45]"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="sticky top-0 bg-[#0f1117] border-b border-[#2a2e45] p-5 z-10">
          <div className="flex items-start justify-between">
            <div>
              <div className="flex items-center gap-3">
                <h2 className="text-xl font-bold">{stock.symbol}</h2>
                {stock.is_defensive_sector && (
                  <span className="px-2 py-0.5 rounded text-xs bg-blue-500/20 text-blue-300 border border-blue-500/30">
                    Defensive Sector
                  </span>
                )}
              </div>
              <p className="text-[#8b90a8] text-sm mt-1">{stock.name}</p>
              <p className="text-[#8b90a8] text-xs">{stock.sector} · {stock.industry} · {stock.exchange}</p>
            </div>
            <button onClick={onClose} className="text-[#8b90a8] hover:text-white text-2xl leading-none">×</button>
          </div>

          {/* Price and Score */}
          <div className="flex items-center justify-between mt-4">
            <div>
              <div className="text-3xl font-bold">
                ₹{stock.current_price.toLocaleString('en-IN')}
              </div>
              <div className="text-xs text-[#8b90a8] mt-1">
                52W: ₹{stock.low_52w.toLocaleString('en-IN')} — ₹{stock.high_52w.toLocaleString('en-IN')}
              </div>
              {/* 52W Range bar */}
              <div className="w-48 h-1.5 bg-[#2a2e45] rounded-full mt-2 relative">
                <div className="absolute h-full bg-gradient-to-r from-red-500 via-yellow-500 to-green-500 rounded-full" style={{ width: '100%', opacity: 0.3 }} />
                <div className="absolute h-3 w-3 bg-blue-400 rounded-full -top-[3px] border-2 border-[#0f1117]" style={{ left: `${pricePosition}%` }} />
              </div>
            </div>
            {gdf && (
              <div className="flex items-center gap-4">
                <ScoreRing score={gdf.total_score} size={72} />
                <div>
                  <VerdictBadge verdict={gdf.verdict} />
                  <div className="text-xs text-[#8b90a8] mt-2">
                    Graham #: ₹{gdf.graham_number.toLocaleString('en-IN')}
                  </div>
                  <div className={`text-xs font-mono ${gdf.margin_of_safety_pct > 0 ? 'text-green-400' : 'text-red-400'}`}>
                    MoS: {gdf.margin_of_safety_pct.toFixed(1)}%
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="p-5 space-y-6">
          {/* Disclaimer */}
          <div className="bg-yellow-500/10 border border-yellow-500/30 rounded-lg p-3 text-xs text-yellow-300">
            Research tool only — not investment advice. Always do your own due diligence.
          </div>

          {/* Fundamentals Grid */}
          <div className="grid grid-cols-2 gap-4">
            <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] p-4">
              <h3 className="font-semibold text-sm text-blue-400 mb-3">Financials</h3>
              <MetricRow label="Market Cap" value={`₹${(stock.market_cap_cr / 1000).toFixed(0)}K Cr`} />
              <MetricRow label="Revenue" value={`₹${stock.sales_cr.toLocaleString('en-IN')} Cr`} />
              <MetricRow label="Net Profit" value={`₹${stock.net_profit_cr.toLocaleString('en-IN')} Cr`} good={stock.net_profit_cr > 0} />
              <MetricRow label="EPS" value={`₹${stock.eps.toFixed(1)}`} good={stock.eps > 0} />
              <MetricRow label="Book Value" value={`₹${stock.book_value.toFixed(1)}`} />
            </div>

            <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] p-4">
              <h3 className="font-semibold text-sm text-purple-400 mb-3">Valuation</h3>
              <MetricRow label="PE Ratio" value={stock.pe_ratio.toFixed(1)} good={stock.pe_ratio > 0 && stock.pe_ratio < 25} />
              <MetricRow label="PE 5Y Avg" value={stock.pe_5y_avg.toFixed(1)} />
              <MetricRow label="P/B Ratio" value={stock.pb_ratio.toFixed(2)} good={stock.pb_ratio < stock.sector_avg_pb} />
              <MetricRow label="Sector Avg P/B" value={stock.sector_avg_pb.toFixed(2)} />
              <MetricRow label="Div Yield" value={`${stock.dividend_yield.toFixed(2)}%`} good={stock.dividend_yield > 1} />
            </div>

            <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] p-4">
              <h3 className="font-semibold text-sm text-green-400 mb-3">Quality</h3>
              <MetricRow label="ROE" value={`${stock.roe.toFixed(1)}%`} good={stock.roe >= 15} />
              <MetricRow label="ROCE" value={`${stock.roce.toFixed(1)}%`} good={stock.roce >= 15} />
              <MetricRow label="D/E Ratio" value={stock.debt_to_equity.toFixed(2)} good={stock.debt_to_equity < 0.5} />
              <MetricRow label="Int Coverage" value={`${stock.interest_coverage.toFixed(1)}x`} good={stock.interest_coverage > 4} />
              <MetricRow label="OCF" value={`₹${stock.ocf_cr.toLocaleString('en-IN')} Cr`} good={stock.ocf_cr > 0} />
            </div>

            <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] p-4">
              <h3 className="font-semibold text-sm text-orange-400 mb-3">Promoter & Dividends</h3>
              <MetricRow label="Promoter Holding" value={`${stock.promoter_holding_pct.toFixed(1)}%`} good={stock.promoter_holding_pct >= 50} />
              <MetricRow label="Promoter Pledge" value={`${stock.promoter_pledge_pct.toFixed(1)}%`} good={stock.promoter_pledge_pct <= 5} />
              <MetricRow label="Dividend Years" value={`${stock.dividend_years}/10`} good={stock.dividend_years >= 7} />
              <MetricRow label="Profit Years" value={`${stock.profit_years_positive}/10`} good={stock.profit_years_positive >= 10} />
              <MetricRow label="OCF Positive Yrs" value={`${stock.ocf_positive_years}/10`} good={stock.ocf_positive_years >= 8} />
            </div>
          </div>

          {/* Moat */}
          {stock.moat_description && (
            <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] p-4">
              <h3 className="font-semibold text-sm text-yellow-400 mb-2">Economic Moat</h3>
              <p className="text-sm text-[#8b90a8]">{stock.moat_description}</p>
              <div className="flex gap-2 mt-2">
                {stock.has_strong_brand && <span className="px-2 py-0.5 rounded text-xs bg-green-500/20 text-green-300">Brand</span>}
                {stock.has_cost_advantage && <span className="px-2 py-0.5 rounded text-xs bg-green-500/20 text-green-300">Cost Advantage</span>}
                {stock.has_regulatory_barrier && <span className="px-2 py-0.5 rounded text-xs bg-green-500/20 text-green-300">Regulatory Barrier</span>}
                {stock.has_dominant_share && <span className="px-2 py-0.5 rounded text-xs bg-green-500/20 text-green-300">Market Share</span>}
              </div>
            </div>
          )}

          {/* GDF-12 Signal Groups */}
          {gdf && (
            <>
              <h3 className="text-lg font-bold mt-4">GDF-12 Signal Breakdown</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <SignalCard title="Group A — Business Quality" signals={gdf.group_a} color="#42a5f5" />
                <SignalCard title="Group B — Financial Strength" signals={gdf.group_b} color="#66bb6a" />
                <SignalCard title="Group C — Valuation" signals={gdf.group_c} color="#ab47bc" />
                <SignalCard title="Group D — Shareholder Signals" signals={gdf.group_d} color="#ff9800" />
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
