'use client';

import { useEffect, useRef, useState } from 'react';
import { fetchChartData, ChartDataResponse } from '@/lib/api';
import {
  createChart,
  type IChartApi,
  ColorType,
  LineStyle,
} from 'lightweight-charts';

interface StockChartProps {
  symbol: string;
}

type Overlay = 'sma' | 'bollinger' | 'volume';
type SubChart = 'none' | 'rsi' | 'macd';

export default function StockChart({ symbol }: StockChartProps) {
  const mainRef = useRef<HTMLDivElement>(null);
  const subRef = useRef<HTMLDivElement>(null);
  const mainChartRef = useRef<IChartApi | null>(null);
  const subChartRef = useRef<IChartApi | null>(null);
  const [data, setData] = useState<ChartDataResponse | null>(null);
  const [period, setPeriod] = useState('1y');
  const [overlays, setOverlays] = useState<Set<Overlay>>(() => new Set<Overlay>(['sma', 'volume']));
  const [subChart, setSubChart] = useState<SubChart>('rsi');
  const [loading, setLoading] = useState(true);

  const toggleOverlay = (o: Overlay) => {
    setOverlays(prev => {
      const next = new Set(prev);
      next.has(o) ? next.delete(o) : next.add(o);
      return next;
    });
  };

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    fetchChartData(symbol, period).then(d => {
      if (!cancelled) { setData(d); setLoading(false); }
    }).catch(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [symbol, period]);

  useEffect(() => {
    if (!data || !data.candles?.length || !mainRef.current) return;

    if (mainChartRef.current) { mainChartRef.current.remove(); mainChartRef.current = null; }
    if (subChartRef.current) { subChartRef.current.remove(); subChartRef.current = null; }

    const chartOptions = {
      layout: { background: { type: ColorType.Solid as const, color: '#16161e' }, textColor: '#8c8ca1' },
      grid: { vertLines: { color: '#2a2a3a' }, horzLines: { color: '#2a2a3a' } },
      crosshair: { mode: 0 as const },
      rightPriceScale: { borderColor: '#2a2a3a' },
      timeScale: { borderColor: '#2a2a3a', timeVisible: false },
    };

    const chart = createChart(mainRef.current, { ...chartOptions, height: 340, width: mainRef.current.clientWidth });
    mainChartRef.current = chart;

    // Candlestick series (v4 API)
    const candleSeries = chart.addCandlestickSeries({
      upColor: '#00d09c', downColor: '#eb5757',
      borderUpColor: '#00d09c', borderDownColor: '#eb5757',
      wickUpColor: '#00d09c', wickDownColor: '#eb5757',
    });
    candleSeries.setData(data.candles.map(c => ({
      time: c.time as string,
      open: c.open, high: c.high, low: c.low, close: c.close,
    })) as any);

    // Volume histogram
    if (overlays.has('volume')) {
      const volSeries = chart.addHistogramSeries({
        priceFormat: { type: 'volume' as const },
        priceScaleId: 'vol',
      });
      chart.priceScale('vol').applyOptions({ scaleMargins: { top: 0.85, bottom: 0 } });
      volSeries.setData(data.candles.map(c => ({
        time: c.time as string,
        value: c.volume,
        color: c.close >= c.open ? 'rgba(0,208,156,0.15)' : 'rgba(235,87,87,0.15)',
      })) as any);
    }

    // SMA overlays
    if (overlays.has('sma')) {
      const addLine = (key: 'sma_20' | 'sma_50' | 'sma_200', color: string) => {
        const lineData = data.overlays.filter(o => o[key] !== null).map(o => ({ time: o.time, value: o[key]! }));
        if (lineData.length === 0) return;
        const s = chart.addLineSeries({ color, lineWidth: 1, priceLineVisible: false, lastValueVisible: false, crosshairMarkerVisible: false });
        s.setData(lineData as any);
      };
      addLine('sma_20', '#f5a623');
      addLine('sma_50', '#5b8def');
      addLine('sma_200', '#a78bfa');
    }

    // Bollinger Bands
    if (overlays.has('bollinger')) {
      const addBB = (key: 'bb_upper' | 'bb_middle' | 'bb_lower', color: string, dash?: boolean) => {
        const lineData = data.overlays.filter(o => o[key] !== null).map(o => ({ time: o.time, value: o[key]! }));
        if (lineData.length === 0) return;
        const s = chart.addLineSeries({
          color, lineWidth: 1, priceLineVisible: false, lastValueVisible: false,
          crosshairMarkerVisible: false,
          lineStyle: dash ? LineStyle.Dashed : LineStyle.Solid,
        });
        s.setData(lineData as any);
      };
      addBB('bb_upper', 'rgba(91,141,239,0.4)', true);
      addBB('bb_middle', 'rgba(91,141,239,0.6)');
      addBB('bb_lower', 'rgba(91,141,239,0.4)', true);
    }

    chart.timeScale().fitContent();

    // Sub chart (RSI or MACD)
    if (subChart !== 'none' && subRef.current) {
      const sub = createChart(subRef.current, { ...chartOptions, height: 120, width: subRef.current.clientWidth });
      subChartRef.current = sub;

      if (subChart === 'rsi') {
        const rsiData = data.overlays.filter(o => o.rsi !== null).map(o => ({ time: o.time, value: o.rsi! }));
        const rsiSeries = sub.addLineSeries({ color: '#a78bfa', lineWidth: 2, priceLineVisible: false, lastValueVisible: true });
        rsiSeries.setData(rsiData as any);
        const line70 = sub.addLineSeries({ color: 'rgba(235,87,87,0.3)', lineWidth: 1, lineStyle: LineStyle.Dashed, priceLineVisible: false, lastValueVisible: false, crosshairMarkerVisible: false });
        const line30 = sub.addLineSeries({ color: 'rgba(0,208,156,0.3)', lineWidth: 1, lineStyle: LineStyle.Dashed, priceLineVisible: false, lastValueVisible: false, crosshairMarkerVisible: false });
        const times = rsiData.map(d => d.time);
        line70.setData(times.map(t => ({ time: t, value: 70 })) as any);
        line30.setData(times.map(t => ({ time: t, value: 30 })) as any);
      } else if (subChart === 'macd') {
        const macdLine = data.overlays.filter(o => o.macd !== null).map(o => ({ time: o.time, value: o.macd! }));
        const signalLine = data.overlays.filter(o => o.macd_signal !== null).map(o => ({ time: o.time, value: o.macd_signal! }));
        const histData = data.overlays.filter(o => o.macd_hist !== null).map(o => ({
          time: o.time, value: o.macd_hist!,
          color: o.macd_hist! >= 0 ? 'rgba(0,208,156,0.5)' : 'rgba(235,87,87,0.5)',
        }));
        const ms = sub.addLineSeries({ color: '#5b8def', lineWidth: 2, priceLineVisible: false, lastValueVisible: false });
        ms.setData(macdLine as any);
        const ss = sub.addLineSeries({ color: '#f5a623', lineWidth: 1, priceLineVisible: false, lastValueVisible: false });
        ss.setData(signalLine as any);
        const hs = sub.addHistogramSeries({ priceLineVisible: false, lastValueVisible: false });
        hs.setData(histData as any);
      }
      sub.timeScale().fitContent();

      chart.timeScale().subscribeVisibleLogicalRangeChange((range: any) => {
        if (range) sub.timeScale().setVisibleLogicalRange(range);
      });
      sub.timeScale().subscribeVisibleLogicalRangeChange((range: any) => {
        if (range) chart.timeScale().setVisibleLogicalRange(range);
      });
    }

    const handleResize = () => {
      if (mainRef.current) chart.applyOptions({ width: mainRef.current.clientWidth });
      if (subRef.current && subChartRef.current) subChartRef.current.applyOptions({ width: subRef.current.clientWidth });
    };
    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
      chart.remove();
      if (subChartRef.current) { subChartRef.current.remove(); subChartRef.current = null; }
      mainChartRef.current = null;
    };
  }, [data, overlays, subChart]);

  const periods = [
    { label: '1M', value: '1mo' },
    { label: '3M', value: '3mo' },
    { label: '6M', value: '6mo' },
    { label: '1Y', value: '1y' },
    { label: '2Y', value: '2y' },
    { label: '5Y', value: '5y' },
  ];

  return (
    <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] overflow-hidden">
      {/* Controls */}
      <div className="flex items-center justify-between px-4 py-3 border-b border-[#2a2a3a]">
        <div className="flex items-center gap-1">
          {periods.map(p => (
            <button key={p.value} onClick={() => setPeriod(p.value)}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${period === p.value ? 'bg-[#00d09c]/15 text-[#00d09c]' : 'text-[#5c5c72] hover:text-white hover:bg-[#2a2a3a]'}`}>
              {p.label}
            </button>
          ))}
        </div>
        <div className="flex items-center gap-1">
          {([['sma', 'SMA'], ['bollinger', 'BB'], ['volume', 'Vol']] as [Overlay, string][]).map(([key, label]) => (
            <button key={key} onClick={() => toggleOverlay(key)}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${overlays.has(key) ? 'bg-[#5b8def]/15 text-[#5b8def]' : 'text-[#5c5c72] hover:text-white hover:bg-[#2a2a3a]'}`}>
              {label}
            </button>
          ))}
          <span className="w-px h-4 bg-[#2a2a3a] mx-1" />
          {([['rsi', 'RSI'], ['macd', 'MACD'], ['none', '—']] as [SubChart, string][]).map(([key, label]) => (
            <button key={key} onClick={() => setSubChart(key)}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${subChart === key ? 'bg-[#a78bfa]/15 text-[#a78bfa]' : 'text-[#5c5c72] hover:text-white hover:bg-[#2a2a3a]'}`}>
              {label}
            </button>
          ))}
        </div>
      </div>

      {/* Legend */}
      {overlays.has('sma') && (
        <div className="flex items-center gap-4 px-4 py-1.5 text-[10px] font-medium">
          <span className="flex items-center gap-1"><span className="w-3 h-0.5 bg-[#f5a623] rounded" />SMA 20</span>
          <span className="flex items-center gap-1"><span className="w-3 h-0.5 bg-[#5b8def] rounded" />SMA 50</span>
          <span className="flex items-center gap-1"><span className="w-3 h-0.5 bg-[#a78bfa] rounded" />SMA 200</span>
        </div>
      )}

      {/* Main chart */}
      <div className="relative">
        {loading && (
          <div className="absolute inset-0 flex items-center justify-center bg-[#16161e]/80 z-10">
            <div className="animate-spin rounded-full h-6 w-6 border-t-2 border-[#00d09c]" />
          </div>
        )}
        <div ref={mainRef} />
      </div>

      {/* Sub chart */}
      {subChart !== 'none' && (
        <div className="border-t border-[#2a2a3a]">
          <div className="px-4 py-1 text-[10px] font-medium text-[#8c8ca1] uppercase tracking-wider">{subChart}</div>
          <div ref={subRef} />
        </div>
      )}
    </div>
  );
}
