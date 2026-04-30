"use client";

import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  Area,
  AreaChart,
} from "recharts";
import type { BacktestResponse } from "@/lib/types";

interface Props {
  result: BacktestResponse;
}

function formatCurrency(val: number): string {
  if (val >= 10_000_000) return `₹${(val / 10_000_000).toFixed(2)}Cr`;
  if (val >= 100_000) return `₹${(val / 100_000).toFixed(2)}L`;
  return `₹${val.toLocaleString()}`;
}

export default function EquityCurve({ result }: Props) {
  const { metrics, equity_curve, trades } = result;

  const metricsGrid = [
    { label: "CAGR", value: `${metrics.cagr.toFixed(2)}%` },
    { label: "Sharpe Ratio", value: metrics.sharpe_ratio.toFixed(2) },
    { label: "Max Drawdown", value: `${metrics.max_drawdown.toFixed(2)}%` },
    { label: "Win Rate", value: `${metrics.win_rate.toFixed(1)}%` },
    { label: "Total Trades", value: metrics.total_trades.toString() },
    { label: "Profit Factor", value: metrics.profit_factor.toFixed(2) },
    { label: "Avg R-Multiple", value: metrics.avg_r_multiple.toFixed(2) },
    { label: "Avg Hold (days)", value: metrics.avg_holding_days.toFixed(1) },
    { label: "Exposure", value: `${metrics.exposure_pct.toFixed(1)}%` },
    { label: "Expectancy", value: `₹${metrics.expectancy.toFixed(0)}` },
  ];

  return (
    <div className="space-y-6">
      <div className="card">
        <h3 className="text-lg font-bold text-gray-900 mb-4">Performance Metrics</h3>
        <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
          {metricsGrid.map((m) => (
            <div key={m.label} className="text-center">
              <p className="text-xs text-gray-500 uppercase">{m.label}</p>
              <p className="text-lg font-bold text-gray-900">{m.value}</p>
            </div>
          ))}
        </div>

        <div className="flex justify-between mt-4 pt-4 border-t border-gray-100 text-sm text-gray-500">
          <span>Capital: {formatCurrency(result.initial_capital)}</span>
          <span>Final: {formatCurrency(result.final_equity)}</span>
          <span>
            Return:{" "}
            {(((result.final_equity - result.initial_capital) / result.initial_capital) * 100).toFixed(2)}%
          </span>
        </div>
      </div>

      {equity_curve.length > 0 && (
        <div className="card">
          <h3 className="text-lg font-bold text-gray-900 mb-4">Equity Curve</h3>
          <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={equity_curve}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="date" tick={{ fontSize: 11 }} />
              <YAxis tickFormatter={(v) => formatCurrency(v)} tick={{ fontSize: 11 }} />
              <Tooltip formatter={(v: number) => formatCurrency(v)} />
              <Legend />
              <Area
                type="monotone"
                dataKey="equity"
                stroke="#2563eb"
                fill="#2563eb20"
                name="Strategy"
              />
              {equity_curve[0]?.benchmark_equity && (
                <Area
                  type="monotone"
                  dataKey="benchmark_equity"
                  stroke="#9ca3af"
                  fill="#9ca3af10"
                  name="Benchmark"
                />
              )}
            </AreaChart>
          </ResponsiveContainer>
        </div>
      )}

      {equity_curve.length > 0 && (
        <div className="card">
          <h3 className="text-lg font-bold text-gray-900 mb-4">Drawdown</h3>
          <ResponsiveContainer width="100%" height={200}>
            <AreaChart data={equity_curve}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="date" tick={{ fontSize: 11 }} />
              <YAxis tickFormatter={(v) => `${v.toFixed(1)}%`} tick={{ fontSize: 11 }} />
              <Tooltip formatter={(v: number) => `${v.toFixed(2)}%`} />
              <Area type="monotone" dataKey="drawdown" stroke="#ef4444" fill="#ef444420" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      )}

      {trades.length > 0 && (
        <div className="card">
          <h3 className="text-lg font-bold text-gray-900 mb-4">
            Trade List ({trades.length} trades)
          </h3>
          <div className="overflow-x-auto max-h-96 overflow-y-auto">
            <table className="min-w-full divide-y divide-gray-200 text-sm">
              <thead className="bg-gray-50 sticky top-0">
                <tr>
                  <th className="px-3 py-2 text-left text-xs font-semibold text-gray-500">
                    Symbol
                  </th>
                  <th className="px-3 py-2 text-left text-xs font-semibold text-gray-500">
                    Entry
                  </th>
                  <th className="px-3 py-2 text-left text-xs font-semibold text-gray-500">Exit</th>
                  <th className="px-3 py-2 text-right text-xs font-semibold text-gray-500">
                    P&L
                  </th>
                  <th className="px-3 py-2 text-right text-xs font-semibold text-gray-500">
                    P&L %
                  </th>
                  <th className="px-3 py-2 text-right text-xs font-semibold text-gray-500">R</th>
                  <th className="px-3 py-2 text-left text-xs font-semibold text-gray-500">
                    Reason
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {trades.map((t, i) => (
                  <tr key={i} className="hover:bg-gray-50">
                    <td className="px-3 py-2 font-medium text-blue-600">{t.symbol}</td>
                    <td className="px-3 py-2 text-gray-600">{t.entry_date}</td>
                    <td className="px-3 py-2 text-gray-600">{t.exit_date || "-"}</td>
                    <td
                      className={`px-3 py-2 text-right font-medium ${t.pnl >= 0 ? "text-green-600" : "text-red-600"}`}
                    >
                      {formatCurrency(t.pnl)}
                    </td>
                    <td
                      className={`px-3 py-2 text-right ${t.pnl_pct >= 0 ? "text-green-600" : "text-red-600"}`}
                    >
                      {t.pnl_pct.toFixed(2)}%
                    </td>
                    <td className="px-3 py-2 text-right">{t.r_multiple.toFixed(2)}</td>
                    <td className="px-3 py-2 text-gray-500">{t.exit_reason}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
