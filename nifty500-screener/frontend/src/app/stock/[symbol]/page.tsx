"use client";

import { useParams } from "next/navigation";
import useSWR from "swr";
import { api } from "@/lib/api";
import type { StockSnapshot, OHLCV } from "@/lib/types";
import StockChart from "@/components/StockChart";
import ScoreCard from "@/components/ScoreCard";

export default function StockDetailPage() {
  const params = useParams();
  const symbol = (params.symbol as string)?.toUpperCase();

  const { data: snapshot, isLoading } = useSWR<StockSnapshot>(
    symbol ? ["snapshot", symbol] : null,
    () => api.getStockSnapshot(symbol)
  );

  const { data: chartData } = useSWR<OHLCV[]>(
    symbol ? ["chart", symbol] : null,
    () => api.getChartData(symbol, 365)
  );

  if (isLoading) {
    return (
      <div className="text-center py-20">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto" />
        <p className="text-gray-500 mt-4">Loading {symbol}...</p>
      </div>
    );
  }

  if (!snapshot) {
    return (
      <div className="card text-center py-12">
        <p className="text-gray-500">Stock {symbol} not found.</p>
        <a href="/" className="text-blue-600 text-sm mt-2 inline-block">
          Back to Dashboard
        </a>
      </div>
    );
  }

  const { stock, fundamentals, technical, disclosures, news, score } = snapshot;

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <a href="/" className="text-blue-600 hover:text-blue-800 text-sm">
          &larr; Dashboard
        </a>
        <div>
          <h1 className="text-3xl font-bold text-gray-900">{stock.symbol}</h1>
          <p className="text-gray-500">{stock.company_name}</p>
          <div className="flex gap-2 mt-1">
            {stock.sector && (
              <span className="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full">
                {stock.sector}
              </span>
            )}
            {stock.industry && (
              <span className="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded-full">
                {stock.industry}
              </span>
            )}
          </div>
        </div>
      </div>

      {snapshot.latest_price && (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div className="card text-center">
            <p className="text-xs text-gray-500">Close</p>
            <p className="text-xl font-bold">₹{snapshot.latest_price.close.toFixed(2)}</p>
          </div>
          <div className="card text-center">
            <p className="text-xs text-gray-500">Volume</p>
            <p className="text-xl font-bold">
              {(snapshot.latest_price.volume / 1_000_000).toFixed(2)}M
            </p>
          </div>
          <div className="card text-center">
            <p className="text-xs text-gray-500">Market Cap</p>
            <p className="text-xl font-bold">
              {stock.market_cap
                ? `₹${(stock.market_cap / 10_000_000).toFixed(0)}Cr`
                : "-"}
            </p>
          </div>
          <div className="card text-center">
            <p className="text-xs text-gray-500">Avg Daily Value</p>
            <p className="text-xl font-bold">
              {stock.avg_daily_value
                ? `₹${(stock.avg_daily_value / 10_000_000).toFixed(1)}Cr`
                : "-"}
            </p>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          {chartData && <StockChart data={chartData} symbol={symbol} />}
        </div>
        <div>{score && <ScoreCard score={score} />}</div>
      </div>

      {fundamentals && (
        <div className="card">
          <h3 className="text-lg font-bold text-gray-900 mb-4">Fundamentals</h3>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            {[
              ["ROE", fundamentals.roe, "%"],
              ["ROCE", fundamentals.roce, "%"],
              ["Net Margin", fundamentals.net_margin, "%"],
              ["P/E", fundamentals.pe_ratio, "x"],
              ["P/B", fundamentals.pb_ratio, "x"],
              ["EV/EBITDA", fundamentals.ev_to_ebitda, "x"],
              ["D/E", fundamentals.debt_to_equity, "x"],
              ["FCF Yield", fundamentals.fcf_yield, "%"],
              ["Rev Growth 3Y", fundamentals.revenue_growth_3y, "%"],
              ["EPS Growth 3Y", fundamentals.eps_growth_3y, "%"],
              ["EBITDA Margin", fundamentals.ebitda_margin, "%"],
              ["Interest Coverage", fundamentals.interest_coverage, "x"],
            ].map(([label, val, suffix]) => (
              <div key={label as string}>
                <p className="text-gray-500">{label as string}</p>
                <p className="font-semibold">
                  {val != null ? `${(val as number).toFixed(2)}${suffix}` : "-"}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}

      {technical && (
        <div className="card">
          <h3 className="text-lg font-bold text-gray-900 mb-4">Technical Indicators</h3>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            {[
              ["RSI (14)", technical.rsi_14],
              ["SMA 20", technical.sma_20],
              ["SMA 50", technical.sma_50],
              ["SMA 200", technical.sma_200],
              ["MACD", technical.macd_line],
              ["MACD Signal", technical.macd_signal],
              ["ATR (14)", technical.atr_14],
              ["RS vs NIFTY500", technical.rs_vs_nifty500],
            ].map(([label, val]) => (
              <div key={label as string}>
                <p className="text-gray-500">{label as string}</p>
                <p className="font-semibold">
                  {val != null ? (val as number).toFixed(2) : "-"}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}

      {disclosures.length > 0 && (
        <div className="card">
          <h3 className="text-lg font-bold text-gray-900 mb-4">
            Promoter/Insider Disclosures
          </h3>
          <div className="space-y-3">
            {disclosures.map((d, i) => (
              <div
                key={i}
                className={`flex items-center justify-between p-3 rounded-lg ${
                  d.transaction_type === "buy"
                    ? "bg-green-50"
                    : d.transaction_type === "sell"
                      ? "bg-red-50"
                      : "bg-yellow-50"
                }`}
              >
                <div>
                  <span
                    className={`text-xs font-semibold uppercase ${
                      d.transaction_type === "buy"
                        ? "text-green-700"
                        : d.transaction_type === "sell"
                          ? "text-red-700"
                          : "text-yellow-700"
                    }`}
                  >
                    {d.transaction_type}
                  </span>
                  <p className="text-sm text-gray-700">{d.entity_name || d.disclosure_type}</p>
                  <p className="text-xs text-gray-500">{d.disclosure_date}</p>
                </div>
                <div className="text-right">
                  {d.value_inr && (
                    <p className="text-sm font-medium">
                      ₹{(d.value_inr / 10_000_000).toFixed(2)}Cr
                    </p>
                  )}
                  {d.promoter_holding_pct != null && (
                    <p className="text-xs text-gray-500">
                      Holding: {d.promoter_holding_pct.toFixed(1)}%
                    </p>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {news.length > 0 && (
        <div className="card">
          <h3 className="text-lg font-bold text-gray-900 mb-4">News & Sentiment</h3>
          <div className="space-y-3">
            {news.map((n, i) => (
              <div key={i} className="p-3 bg-gray-50 rounded-lg">
                <div className="flex items-start justify-between">
                  <div>
                    <p className="text-sm font-medium text-gray-900">{n.title}</p>
                    {n.summary && <p className="text-xs text-gray-500 mt-1">{n.summary}</p>}
                  </div>
                  {n.sentiment && (
                    <span
                      className={`score-badge ${
                        n.sentiment === "positive"
                          ? "score-high"
                          : n.sentiment === "negative"
                            ? "score-low"
                            : "score-medium"
                      }`}
                    >
                      {n.sentiment}
                    </span>
                  )}
                </div>
                <div className="flex gap-3 mt-2 text-xs text-gray-400">
                  {n.source && <span>{n.source}</span>}
                  {n.category && <span>{n.category}</span>}
                  {n.impact_score != null && <span>Impact: {n.impact_score.toFixed(2)}</span>}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
