"use client";

import { useEffect, useRef, useState } from "react";
import { api } from "@/lib/api";
import type { Instrument, OHLCV } from "@/lib/types";

interface Props {
  symbol: string;
  openOrder: (symbol: string, type: "BUY" | "SELL", price: number) => void;
}

export default function ChartView({ symbol, openOrder }: Props) {
  const containerRef = useRef<HTMLDivElement>(null);
  const chartRef = useRef<ReturnType<typeof import("lightweight-charts").createChart> | null>(null);
  const [instrument, setInstrument] = useState<Instrument | null>(null);
  const [ohlcv, setOhlcv] = useState<OHLCV[]>([]);

  useEffect(() => {
    api.getInstrument(symbol).then(setInstrument).catch(() => {});
    api.getOHLCV(symbol).then(setOhlcv).catch(() => {});
  }, [symbol]);

  useEffect(() => {
    if (!containerRef.current || ohlcv.length === 0) return;

    let mounted = true;
    let resizeHandler: (() => void) | null = null;

    import("lightweight-charts").then(({ createChart }) => {
      if (!mounted || !containerRef.current) return;
      if (chartRef.current) chartRef.current.remove();

      const chart = createChart(containerRef.current, {
        width: containerRef.current.clientWidth,
        height: 500,
        layout: { background: { color: "#ffffff" }, textColor: "#444" },
        grid: { vertLines: { color: "#f0f0f0" }, horzLines: { color: "#f0f0f0" } },
        crosshair: { mode: 0 },
        timeScale: { borderColor: "#e8e8e8" },
        rightPriceScale: { borderColor: "#e8e8e8" },
      });
      chartRef.current = chart;

      const candleSeries = chart.addCandlestickSeries({
        upColor: "#4caf50",
        downColor: "#e54040",
        wickUpColor: "#4caf50",
        wickDownColor: "#e54040",
        borderVisible: false,
      });

      candleSeries.setData(
        ohlcv.map((d) => ({
          time: d.date as string,
          open: d.open,
          high: d.high,
          low: d.low,
          close: d.close,
        }))
      );

      const volumeSeries = chart.addHistogramSeries({
        priceFormat: { type: "volume" },
        priceScaleId: "volume",
      });
      chart.priceScale("volume").applyOptions({ scaleMargins: { top: 0.85, bottom: 0 } });
      volumeSeries.setData(
        ohlcv.map((d) => ({
          time: d.date as string,
          value: d.volume,
          color: d.close >= d.open ? "#4caf5040" : "#e5404040",
        }))
      );

      chart.timeScale().fitContent();

      resizeHandler = () => {
        if (containerRef.current) chart.applyOptions({ width: containerRef.current.clientWidth });
      };
      window.addEventListener("resize", resizeHandler);
    });

    return () => {
      mounted = false;
      if (resizeHandler) window.removeEventListener("resize", resizeHandler);
      if (chartRef.current) { chartRef.current.remove(); chartRef.current = null; }
    };
  }, [ohlcv]);

  return (
    <div>
      {/* Instrument header */}
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-4">
          <h2 className="text-lg font-semibold text-kite-text">{symbol}</h2>
          {instrument && (
            <>
              <span className="text-xs text-kite-text-light">{instrument.exchange}</span>
              <span className="text-lg font-light text-kite-text">
                ₹{instrument.last_price.toLocaleString("en-IN", { maximumFractionDigits: 2 })}
              </span>
              <span className={`text-xs ${instrument.change >= 0 ? "text-positive" : "text-negative"}`}>
                {instrument.change >= 0 ? "+" : ""}
                {instrument.change.toFixed(2)} ({instrument.change_pct.toFixed(2)}%)
              </span>
            </>
          )}
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => instrument && openOrder(symbol, "BUY", instrument.last_price)}
            className="kite-btn-blue text-xs"
          >
            Buy
          </button>
          <button
            onClick={() => instrument && openOrder(symbol, "SELL", instrument.last_price)}
            className="kite-btn-orange text-xs"
          >
            Sell
          </button>
        </div>
      </div>

      {/* OHLCV bar */}
      {instrument && (
        <div className="flex gap-6 mb-4 text-xs text-kite-text-light">
          <span>O <span className="text-kite-text">{instrument.open_price.toFixed(2)}</span></span>
          <span>H <span className="text-kite-text">{instrument.high_price.toFixed(2)}</span></span>
          <span>L <span className="text-kite-text">{instrument.low_price.toFixed(2)}</span></span>
          <span>C <span className="text-kite-text">{instrument.last_price.toFixed(2)}</span></span>
          <span>Vol <span className="text-kite-text">{(instrument.volume / 1000000).toFixed(2)}M</span></span>
        </div>
      )}

      {/* Chart */}
      <div ref={containerRef} className="bg-white border border-kite-border rounded" />
    </div>
  );
}
