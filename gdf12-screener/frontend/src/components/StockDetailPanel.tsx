'use client';

import { StockDetail } from '@/lib/api';
import ScoreRing from './ScoreRing';
import SignalCard from './SignalCard';
import StockChart from './StockChart';
import FIIDIIPanel from './FIIDIIPanel';
import VerdictBadge from './VerdictBadge';

interface StockDetailPanelProps {
  stock: StockDetail;
  onClose: () => void;
}

function MetricRow({ label, value, unit, good }: { label: string; value: string | number; unit?: string; good?: boolean }) {
  return (
    <div className="flex justify-between items-center py-2 border-b border-[#2a2a3a]/40">
      <span className="text-[#8c8ca1] text-sm">{label}</span>
      <span className={`font-mono text-sm ${good === true ? 'text-[#00d09c]' : good === false ? 'text-[#eb5757]' : 'text-white'}`}>
        {typeof value === 'number' ? value.toLocaleString('en-IN') : value}
        {unit && <span className="text-[#5c5c72] text-xs ml-1">{unit}</span>}
      </span>
    </div>
  );
}

export default function StockDetailPanel({ stock, onClose }: StockDetailPanelProps) {
  const gdf = stock.gdf_score;
  const priceRange52w = stock.high_52w - stock.low_52w;
  const pricePosition = priceRange52w > 0 ? ((stock.current_price - stock.low_52w) / priceRange52w) * 100 : 50;

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex justify-end" onClick={onClose}>
      <div
        className="bg-[#0d0d12] w-full max-w-3xl overflow-y-auto border-l border-[#2a2a3a]"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="sticky top-0 bg-[#16161e] border-b border-[#2a2a3a] p-6 z-10">
          <div className="flex items-start justify-between">
            <div>
              <div className="flex items-center gap-3">
                <h2 className="text-xl font-bold text-white">{stock.symbol}</h2>
                {stock.is_defensive_sector && (
                  <span className="px-2.5 py-0.5 rounded-full text-[10px] font-medium bg-[#5b8def]/10 text-[#5b8def] border border-[#5b8def]/20">
                    Defensive Sector
                  </span>
                )}
              </div>
              <p className="text-[#8c8ca1] text-sm mt-1">{stock.name}</p>
              <p className="text-[#5c5c72] text-xs">{stock.sector} · {stock.industry} · {stock.exchange}</p>
            </div>
            <button onClick={onClose} className="text-[#5c5c72] hover:text-white text-2xl leading-none w-8 h-8 flex items-center justify-center rounded-full hover:bg-[#2a2a3a] transition-colors">×</button>
          </div>

          {/* Price and Score */}
          <div className="flex items-center justify-between mt-5">
            <div>
              <div className="text-3xl font-bold text-white">
                ₹{stock.current_price.toLocaleString('en-IN')}
              </div>
              <div className="text-xs text-[#5c5c72] mt-1.5">
                52W: ₹{stock.low_52w.toLocaleString('en-IN')} — ₹{stock.high_52w.toLocaleString('en-IN')}
              </div>
              {/* 52W Range bar */}
              <div className="w-48 h-1.5 bg-[#2a2a3a] rounded-full mt-2.5 relative">
                <div className="absolute h-full bg-gradient-to-r from-[#eb5757] via-[#f5a623] to-[#00d09c] rounded-full" style={{ width: '100%', opacity: 0.3 }} />
                <div className="absolute h-3 w-3 bg-[#5b8def] rounded-full -top-[3px] border-2 border-[#0d0d12]" style={{ left: `${pricePosition}%` }} />
              </div>
            </div>
            {gdf && (
              <div className="flex items-center gap-4">
                <ScoreRing score={gdf.total_score} size={72} />
                <div>
                  <VerdictBadge verdict={gdf.verdict} />
                  <div className="text-xs text-[#5c5c72] mt-2">
                    Graham #: ₹{gdf.graham_number.toLocaleString('en-IN')}
                  </div>
                  <div className={`text-xs font-mono ${gdf.margin_of_safety_pct > 0 ? 'text-[#00d09c]' : 'text-[#eb5757]'}`}>
                    MoS: {gdf.margin_of_safety_pct.toFixed(1)}%
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="p-6 space-y-6">
          {/* Disclaimer */}
          <div className="bg-amber-500/5 border border-amber-500/15 rounded-xl p-3.5 text-xs text-amber-400/80 flex items-center gap-2">
            <span>&#9888;</span>
            Research tool only — not investment advice. Always do your own due diligence.
          </div>

          {/* Fundamentals Grid */}
          <div className="grid grid-cols-2 gap-4">
            <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5">
              <h3 className="font-semibold text-sm text-[#5b8def] mb-3 flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-[#5b8def]"></span>
                Financials
              </h3>
              <MetricRow label="Market Cap" value={`₹${(stock.market_cap_cr / 1000).toFixed(0)}K Cr`} />
              <MetricRow label="Revenue" value={`₹${stock.sales_cr.toLocaleString('en-IN')} Cr`} />
              <MetricRow label="Net Profit" value={`₹${stock.net_profit_cr.toLocaleString('en-IN')} Cr`} good={stock.net_profit_cr > 0} />
              <MetricRow label="EPS" value={`₹${stock.eps.toFixed(1)}`} good={stock.eps > 0} />
              <MetricRow label="Book Value" value={`₹${stock.book_value.toFixed(1)}`} />
            </div>

            <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5">
              <h3 className="font-semibold text-sm text-[#a78bfa] mb-3 flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-[#a78bfa]"></span>
                Valuation
              </h3>
              <MetricRow label="PE Ratio" value={stock.pe_ratio.toFixed(1)} good={stock.pe_ratio > 0 && stock.pe_ratio < 25} />
              <MetricRow label="PE 5Y Avg" value={stock.pe_5y_avg.toFixed(1)} />
              <MetricRow label="P/B Ratio" value={stock.pb_ratio.toFixed(2)} good={stock.pb_ratio < stock.sector_avg_pb} />
              <MetricRow label="Sector Avg P/B" value={stock.sector_avg_pb.toFixed(2)} />
              <MetricRow label="Div Yield" value={`${stock.dividend_yield.toFixed(2)}%`} good={stock.dividend_yield > 1} />
            </div>

            <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5">
              <h3 className="font-semibold text-sm text-[#00d09c] mb-3 flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-[#00d09c]"></span>
                Quality
              </h3>
              <MetricRow label="ROE" value={`${stock.roe.toFixed(1)}%`} good={stock.roe >= 15} />
              <MetricRow label="ROCE" value={`${stock.roce.toFixed(1)}%`} good={stock.roce >= 15} />
              <MetricRow label="D/E Ratio" value={stock.debt_to_equity.toFixed(2)} good={stock.debt_to_equity < 0.5} />
              <MetricRow label="Int Coverage" value={`${stock.interest_coverage.toFixed(1)}x`} good={stock.interest_coverage > 4} />
              <MetricRow label="OCF" value={`₹${stock.ocf_cr.toLocaleString('en-IN')} Cr`} good={stock.ocf_cr > 0} />
            </div>

            <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5">
              <h3 className="font-semibold text-sm text-[#f5a623] mb-3 flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-[#f5a623]"></span>
                Promoter & Dividends
              </h3>
              <MetricRow label="Promoter Holding" value={`${stock.promoter_holding_pct.toFixed(1)}%`} good={stock.promoter_holding_pct >= 50} />
              <MetricRow label="Promoter Pledge" value={`${stock.promoter_pledge_pct.toFixed(1)}%`} good={stock.promoter_pledge_pct <= 5} />
              <MetricRow label="Dividend Years" value={`${stock.dividend_years}/10`} good={stock.dividend_years >= 7} />
              <MetricRow label="Profit Years" value={`${stock.profit_years_positive}/10`} good={stock.profit_years_positive >= 10} />
              <MetricRow label="OCF Positive Yrs" value={`${stock.ocf_positive_years}/10`} good={stock.ocf_positive_years >= 8} />
            </div>
          </div>

          {/* Moat */}
          {stock.moat_description && (
            <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5">
              <h3 className="font-semibold text-sm text-[#f5a623] mb-3 flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-[#f5a623]"></span>
                Economic Moat
              </h3>
              <p className="text-sm text-[#8c8ca1]">{stock.moat_description}</p>
              <div className="flex flex-wrap gap-2 mt-3">
                {stock.has_strong_brand && <span className="px-3 py-1 rounded-full text-xs bg-[#00d09c]/10 text-[#00d09c] border border-[#00d09c]/20 font-medium">Brand</span>}
                {stock.has_cost_advantage && <span className="px-3 py-1 rounded-full text-xs bg-[#00d09c]/10 text-[#00d09c] border border-[#00d09c]/20 font-medium">Cost Advantage</span>}
                {stock.has_regulatory_barrier && <span className="px-3 py-1 rounded-full text-xs bg-[#00d09c]/10 text-[#00d09c] border border-[#00d09c]/20 font-medium">Regulatory Barrier</span>}
                {stock.has_dominant_share && <span className="px-3 py-1 rounded-full text-xs bg-[#00d09c]/10 text-[#00d09c] border border-[#00d09c]/20 font-medium">Market Share</span>}
              </div>
            </div>
          )}

          {/* Price Chart */}
          <h3 className="text-lg font-bold mt-2 flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-[#5b8def]"></span>
            Price Chart
          </h3>
          <StockChart symbol={stock.symbol} />

          {/* FII/DII Institutional Activity */}
          <h3 className="text-lg font-bold mt-2 flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-[#f5a623]"></span>
            FII / DII Activity
          </h3>
          <FIIDIIPanel symbol={stock.symbol} />

          {/* GDF-12 Signal Groups */}
          {gdf && (
            <>
              <h3 className="text-lg font-bold mt-4 flex items-center gap-2">
                <span className="w-2 h-2 rounded-full bg-[#00d09c]"></span>
                GDF-12 Signal Breakdown
              </h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <SignalCard title="Group A — Business Quality" signals={gdf.group_a} color="#5b8def" />
                <SignalCard title="Group B — Financial Strength" signals={gdf.group_b} color="#00d09c" />
                <SignalCard title="Group C — Valuation" signals={gdf.group_c} color="#a78bfa" />
                <SignalCard title="Group D — Shareholder Signals" signals={gdf.group_d} color="#f5a623" />
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
