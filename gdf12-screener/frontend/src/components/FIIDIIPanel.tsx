'use client';

import { useEffect, useState } from 'react';
import { fetchFIIDII, FIIDIIData } from '@/lib/api';

interface FIIDIIPanelProps {
  symbol: string;
}

function SignalBadge({ signal }: { signal: string }) {
  const map: Record<string, { cls: string; label: string }> = {
    STRONG_ACCUMULATION: { cls: 'bg-[#00d09c]/10 text-[#00d09c] border-[#00d09c]/25', label: 'Strong Accumulation' },
    ACCUMULATION: { cls: 'bg-[#5b8def]/10 text-[#5b8def] border-[#5b8def]/25', label: 'Accumulation' },
    DISTRIBUTION: { cls: 'bg-[#eb5757]/10 text-[#eb5757] border-[#eb5757]/25', label: 'Distribution' },
    NEUTRAL: { cls: 'bg-[#f5a623]/10 text-[#f5a623] border-[#f5a623]/25', label: 'Neutral' },
  };
  const m = map[signal] || map.NEUTRAL;
  return <span className={`px-3 py-1 rounded-full text-xs font-semibold border ${m.cls}`}>{m.label}</span>;
}

function HoldingBar({ label, pct, color }: { label: string; pct: number; color: string }) {
  return (
    <div className="flex items-center gap-3">
      <span className="text-xs text-[#8c8ca1] w-20">{label}</span>
      <div className="flex-1 h-3 bg-[#16161e] rounded-full overflow-hidden">
        <div className="h-full rounded-full transition-all duration-500" style={{ width: `${Math.min(100, pct)}%`, backgroundColor: color }} />
      </div>
      <span className="text-xs font-mono text-white w-12 text-right">{pct.toFixed(1)}%</span>
    </div>
  );
}

function FlowBar({ value, maxAbs }: { value: number; maxAbs: number }) {
  const pct = maxAbs > 0 ? Math.abs(value) / maxAbs * 50 : 0;
  const isPositive = value >= 0;
  return (
    <div className="flex items-center h-4">
      <div className="w-1/2 flex justify-end">
        {!isPositive && <div className="h-full rounded-l" style={{ width: `${pct}%`, backgroundColor: '#eb5757', minWidth: value !== 0 ? '2px' : '0' }} />}
      </div>
      <div className="w-px h-full bg-[#3a3a4a]" />
      <div className="w-1/2">
        {isPositive && <div className="h-full rounded-r" style={{ width: `${pct}%`, backgroundColor: '#00d09c', minWidth: value !== 0 ? '2px' : '0' }} />}
      </div>
    </div>
  );
}

export default function FIIDIIPanel({ symbol }: FIIDIIPanelProps) {
  const [data, setData] = useState<FIIDIIData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    fetchFIIDII(symbol).then(d => {
      if (!cancelled) { setData(d); setLoading(false); }
    }).catch(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [symbol]);

  if (loading) {
    return (
      <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-6 flex items-center justify-center h-48">
        <div className="animate-spin rounded-full h-6 w-6 border-t-2 border-[#00d09c]" />
      </div>
    );
  }

  if (!data || data.error) {
    return (
      <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-6 text-center text-[#5c5c72] text-sm">
        No institutional data available
      </div>
    );
  }

  const allFlows = [...data.fii_net_monthly, ...data.dii_net_monthly];
  const maxAbsFlow = Math.max(...allFlows.map(Math.abs), 1);

  return (
    <div className="space-y-4">
      {/* Institutional Signal */}
      <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5">
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-semibold text-sm text-[#f5a623] flex items-center gap-2">
            <span className="w-1.5 h-1.5 rounded-full bg-[#f5a623]" />
            Institutional Activity
          </h3>
          <SignalBadge signal={data.institutional_signal} />
        </div>

        {/* Shareholding pattern */}
        <div className="space-y-2.5 mb-5">
          <HoldingBar label="FII" pct={data.fii_holding_pct} color="#5b8def" />
          <HoldingBar label="DII" pct={data.dii_holding_pct} color="#00d09c" />
          <HoldingBar label="Promoter" pct={data.promoter_holding_pct} color="#a78bfa" />
          <HoldingBar label="Public" pct={data.public_holding_pct} color="#f5a623" />
        </div>

        {/* Net flow summary */}
        <div className="grid grid-cols-3 gap-3">
          <div className="bg-[#16161e] rounded-xl p-3 text-center">
            <div className="text-[10px] text-[#5c5c72] uppercase tracking-wider mb-1">FII Net</div>
            <div className={`font-mono text-sm font-semibold ${data.fii_net_total_cr >= 0 ? 'text-[#00d09c]' : 'text-[#eb5757]'}`}>
              {data.fii_net_total_cr >= 0 ? '+' : ''}{data.fii_net_total_cr.toFixed(0)} Cr
            </div>
          </div>
          <div className="bg-[#16161e] rounded-xl p-3 text-center">
            <div className="text-[10px] text-[#5c5c72] uppercase tracking-wider mb-1">DII Net</div>
            <div className={`font-mono text-sm font-semibold ${data.dii_net_total_cr >= 0 ? 'text-[#00d09c]' : 'text-[#eb5757]'}`}>
              {data.dii_net_total_cr >= 0 ? '+' : ''}{data.dii_net_total_cr.toFixed(0)} Cr
            </div>
          </div>
          <div className="bg-[#16161e] rounded-xl p-3 text-center">
            <div className="text-[10px] text-[#5c5c72] uppercase tracking-wider mb-1">Delivery %</div>
            <div className={`font-mono text-sm font-semibold ${data.delivery_pct > 55 ? 'text-[#00d09c]' : 'text-white'}`}>
              {data.delivery_pct}%
            </div>
          </div>
        </div>
      </div>

      {/* Monthly FII/DII Flow Chart */}
      <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5">
        <h3 className="font-semibold text-sm text-[#5b8def] mb-4 flex items-center gap-2">
          <span className="w-1.5 h-1.5 rounded-full bg-[#5b8def]" />
          Monthly Net Flow (₹ Cr)
        </h3>
        <div className="flex items-center gap-4 mb-3 text-[10px] font-medium">
          <span className="flex items-center gap-1"><span className="w-3 h-2 bg-[#5b8def] rounded-sm" />FII</span>
          <span className="flex items-center gap-1"><span className="w-3 h-2 bg-[#00d09c] rounded-sm" />DII</span>
        </div>
        <div className="space-y-1">
          {data.months.map((month, i) => (
            <div key={month} className="grid grid-cols-[40px_1fr_60px_1fr_60px] gap-1 items-center text-xs">
              <span className="text-[#5c5c72]">{month}</span>
              {/* FII bar */}
              <div className="h-5 flex items-center">
                <div
                  className="h-3.5 rounded transition-all"
                  style={{
                    width: `${Math.min(100, Math.abs(data.fii_net_monthly[i] || 0) / maxAbsFlow * 100)}%`,
                    backgroundColor: (data.fii_net_monthly[i] || 0) >= 0 ? '#5b8def' : 'rgba(91,141,239,0.3)',
                    minWidth: '2px',
                  }}
                />
              </div>
              <span className={`font-mono text-right ${(data.fii_net_monthly[i] || 0) >= 0 ? 'text-[#00d09c]' : 'text-[#eb5757]'}`}>
                {(data.fii_net_monthly[i] || 0) >= 0 ? '+' : ''}{(data.fii_net_monthly[i] || 0).toFixed(0)}
              </span>
              {/* DII bar */}
              <div className="h-5 flex items-center">
                <div
                  className="h-3.5 rounded transition-all"
                  style={{
                    width: `${Math.min(100, Math.abs(data.dii_net_monthly[i] || 0) / maxAbsFlow * 100)}%`,
                    backgroundColor: (data.dii_net_monthly[i] || 0) >= 0 ? '#00d09c' : 'rgba(0,208,156,0.3)',
                    minWidth: '2px',
                  }}
                />
              </div>
              <span className={`font-mono text-right ${(data.dii_net_monthly[i] || 0) >= 0 ? 'text-[#00d09c]' : 'text-[#eb5757]'}`}>
                {(data.dii_net_monthly[i] || 0) >= 0 ? '+' : ''}{(data.dii_net_monthly[i] || 0).toFixed(0)}
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* Volume Profile */}
      <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5">
        <h3 className="font-semibold text-sm text-[#a78bfa] mb-3 flex items-center gap-2">
          <span className="w-1.5 h-1.5 rounded-full bg-[#a78bfa]" />
          Volume Profile
        </h3>
        <div className="grid grid-cols-2 gap-3 text-sm">
          <div className="flex justify-between py-1.5 border-b border-[#2a2a3a]/40">
            <span className="text-[#8c8ca1]">Avg Vol (20D)</span>
            <span className="font-mono text-white">{(data.volume_profile.avg_volume_20d / 100000).toFixed(1)}L</span>
          </div>
          <div className="flex justify-between py-1.5 border-b border-[#2a2a3a]/40">
            <span className="text-[#8c8ca1]">Avg Vol (50D)</span>
            <span className="font-mono text-white">{(data.volume_profile.avg_volume_50d / 100000).toFixed(1)}L</span>
          </div>
          <div className="flex justify-between py-1.5 border-b border-[#2a2a3a]/40">
            <span className="text-[#8c8ca1]">Latest Vol</span>
            <span className="font-mono text-white">{(data.volume_profile.latest_volume / 100000).toFixed(1)}L</span>
          </div>
          <div className="flex justify-between py-1.5 border-b border-[#2a2a3a]/40">
            <span className="text-[#8c8ca1]">Vol Trend</span>
            <span className={`font-mono text-xs px-2 py-0.5 rounded ${data.volume_profile.volume_trend === 'INCREASING' ? 'text-[#00d09c] bg-[#00d09c]/10' : 'text-[#f5a623] bg-[#f5a623]/10'}`}>
              {data.volume_profile.volume_trend}
            </span>
          </div>
        </div>
      </div>

      {/* Bulk/Block Deals */}
      {data.bulk_deals.length > 0 && (
        <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5">
          <h3 className="font-semibold text-sm text-[#eb5757] mb-3 flex items-center gap-2">
            <span className="w-1.5 h-1.5 rounded-full bg-[#eb5757]" />
            Recent Bulk / Block Deals
          </h3>
          <div className="space-y-2">
            {data.bulk_deals.map((deal, i) => (
              <div key={i} className="flex items-center justify-between bg-[#16161e] rounded-xl px-4 py-2.5 text-xs">
                <div>
                  <span className={`px-2 py-0.5 rounded text-[10px] font-medium mr-2 ${deal.type === 'Block Deal' ? 'bg-[#a78bfa]/10 text-[#a78bfa]' : 'bg-[#f5a623]/10 text-[#f5a623]'}`}>
                    {deal.type}
                  </span>
                  <span className={`font-medium ${deal.buyer_seller.includes('Buy') ? 'text-[#00d09c]' : 'text-[#eb5757]'}`}>
                    {deal.buyer_seller}
                  </span>
                </div>
                <div className="text-[#8c8ca1]">
                  {deal.quantity_lakh.toFixed(1)}L shares @ ₹{deal.price.toFixed(0)} · {deal.date}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
