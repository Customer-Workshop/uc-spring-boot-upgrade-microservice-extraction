function getApiUrl(): string {
  const raw = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8002';
  return raw.replace(/\s+/g, '').replace(/\/+$/, '');
}

const API_URL = getApiUrl();

async function safeFetch(url: string, options?: RequestInit): Promise<Response> {
  const cleanUrl = url.replace(/([^:]\/)\/+/g, '$1').replace(/\s+/g, '');
  const res = await fetch(cleanUrl, options);
  if (!res.ok) {
    throw new Error(`API error ${res.status}: ${res.statusText}`);
  }
  return res;
}

export interface StockSummary {
  symbol: string;
  name: string;
  sector: string;
  current_price: number;
  market_cap_cr: number;
  pe_ratio: number;
  roe: number;
  roce: number;
  debt_to_equity: number;
  promoter_holding_pct: number;
  dividend_yield: number;
  is_defensive_sector: boolean;
  total_score: number;
  verdict: string;
  graham_number: number;
  margin_of_safety_pct: number;
}

export interface SignalDetail {
  passed: boolean;
  label: string;
  detail: string;
}

export interface GDFScore {
  symbol: string;
  total_score: number;
  graham_number: number;
  margin_of_safety_pct: number;
  verdict: string;
  group_a: SignalDetail[];
  group_b: SignalDetail[];
  group_c: SignalDetail[];
  group_d: SignalDetail[];
}

export interface StockDetail {
  symbol: string;
  name: string;
  sector: string;
  industry: string | null;
  exchange: string;
  current_price: number;
  high_52w: number;
  low_52w: number;
  market_cap_cr: number;
  sales_cr: number;
  net_profit_cr: number;
  pe_ratio: number;
  pe_5y_avg: number;
  pb_ratio: number;
  sector_avg_pb: number;
  eps: number;
  book_value: number;
  roe: number;
  roce: number;
  debt_to_equity: number;
  interest_coverage: number;
  ocf_cr: number;
  promoter_holding_pct: number;
  promoter_pledge_pct: number;
  dividend_yield: number;
  dividend_years: number;
  profit_years_positive: number;
  ocf_positive_years: number;
  is_defensive_sector: boolean;
  moat_description: string | null;
  has_strong_brand: boolean;
  has_cost_advantage: boolean;
  has_regulatory_barrier: boolean;
  has_dominant_share: boolean;
  gdf_score: GDFScore | null;
}

export interface WatchlistItem {
  id: number;
  symbol: string;
  added_at: string;
  notes: string | null;
}

export async function seedData(): Promise<void> {
  await fetch(`${API_URL}/api/seed`, { method: 'POST' });
}

export async function fetchScreener(params: {
  sector?: string;
  min_score?: number;
  defensive_only?: boolean;
  sort_by?: string;
}): Promise<StockSummary[]> {
  const sp = new URLSearchParams();
  if (params.sector) sp.set('sector', params.sector);
  if (params.min_score) sp.set('min_score', params.min_score.toString());
  if (params.defensive_only) sp.set('defensive_only', 'true');
  if (params.sort_by) sp.set('sort_by', params.sort_by);
  const res = await fetch(`${API_URL}/api/screener?${sp.toString()}`);
  const data = await res.json();

  // If no data returned, auto-seed and retry
  if (Array.isArray(data) && data.length === 0) {
    await seedData();
    const retry = await fetch(`${API_URL}/api/screener?${sp.toString()}`);
    return retry.json();
  }
  return data;
}

export async function fetchStockDetail(symbol: string): Promise<StockDetail> {
  const res = await fetch(`${API_URL}/api/stock/${symbol}`);
  return res.json();
}

export async function fetchSectors(): Promise<string[]> {
  const res = await fetch(`${API_URL}/api/sectors`);
  const data = await res.json();
  return data.sectors;
}

export async function fetchWatchlist(): Promise<WatchlistItem[]> {
  const res = await fetch(`${API_URL}/api/watchlist`);
  return res.json();
}

export async function addToWatchlist(symbol: string, notes?: string): Promise<WatchlistItem> {
  const res = await fetch(`${API_URL}/api/watchlist`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ symbol, notes }),
  });
  return res.json();
}

export async function removeFromWatchlist(symbol: string): Promise<void> {
  await fetch(`${API_URL}/api/watchlist/${symbol}`, { method: 'DELETE' });
}

// === SWING TRADING API ===

export interface SwingStockSummary {
  symbol: string;
  name: string;
  sector: string;
  current_price: number;
  prev_close: number;
  change_pct: number;
  rsi_14: number;
  macd_histogram: number;
  volume_ratio: number;
  bb_squeeze: boolean;
  breakout: boolean;
  total_score: number;
  signal: string;
  verdict: string;
  entry_price: number;
  stop_loss: number;
  target_1: number;
  target_2: number;
  risk_reward: number;
}

export interface SwingScoreDetail {
  score: number;
  detail: string;
}

export interface SwingStockDetail {
  symbol: string;
  name: string;
  sector: string;
  current_price: number;
  prev_close: number;
  high_52w: number;
  low_52w: number;
  sma_20: number;
  sma_50: number;
  sma_200: number;
  rsi_14: number;
  macd_line: number;
  macd_signal: number;
  macd_histogram: number;
  volume: number;
  avg_volume_20: number;
  volume_ratio: number;
  bb_upper: number;
  bb_middle: number;
  bb_lower: number;
  bb_width: number;
  bb_squeeze: boolean;
  atr_14: number;
  resistance_level: number;
  support_level: number;
  breakout_above_resistance: boolean;
  score: {
    total_score: number;
    signal: string;
    verdict: string;
    trend: SwingScoreDetail | null;
    momentum: SwingScoreDetail | null;
    volume: SwingScoreDetail | null;
    volatility: SwingScoreDetail | null;
    structure: SwingScoreDetail | null;
    entry_price: number;
    stop_loss: number;
    target_1: number;
    target_2: number;
    risk_reward: number;
  };
}

export async function fetchSwingStocks(params?: {
  signal?: string;
  min_score?: number;
  sort_by?: string;
}): Promise<SwingStockSummary[]> {
  const sp = new URLSearchParams();
  if (params?.signal) sp.set('signal', params.signal);
  if (params?.min_score) sp.set('min_score', params.min_score.toString());
  if (params?.sort_by) sp.set('sort_by', params.sort_by);
  const res = await fetch(`${API_URL}/api/swing/stocks?${sp.toString()}`);
  return res.json();
}

export async function fetchSwingStockDetail(symbol: string): Promise<SwingStockDetail> {
  const res = await fetch(`${API_URL}/api/swing/stock/${symbol}`);
  return res.json();
}

// === ETF API ===

export interface ETFSummary {
  symbol: string;
  name: string;
  category: string;
  amc: string;
  current_price: number;
  nav: number;
  return_1m: number;
  return_3m: number;
  return_6m: number;
  return_1y: number;
  return_3y_cagr: number;
  return_5y_cagr: number;
  expense_ratio: number;
  tracking_error: number;
  aum_cr: number;
  rsi_14: number;
  above_20w_ma: boolean;
  above_50w_ma: boolean;
  total_score: number;
  verdict: string;
}

export interface ETFDetail {
  symbol: string;
  name: string;
  category: string;
  amc: string;
  current_price: number;
  nav: number;
  high_52w: number;
  low_52w: number;
  sma_20w: number;
  sma_50w: number;
  sma_10m: number;
  sma_12m: number;
  return_1w: number;
  return_1m: number;
  return_3m: number;
  return_6m: number;
  return_1y: number;
  return_3y_cagr: number;
  return_5y_cagr: number;
  expense_ratio: number;
  tracking_error: number;
  aum_cr: number;
  avg_volume: number;
  rsi_14: number;
  above_20w_ma: boolean;
  above_50w_ma: boolean;
  score: {
    total_score: number;
    verdict: string;
    trend: SwingScoreDetail | null;
    momentum: SwingScoreDetail | null;
    cost: SwingScoreDetail | null;
    liquidity: SwingScoreDetail | null;
    performance: SwingScoreDetail | null;
  };
}

export async function fetchETFs(params?: {
  category?: string;
  min_score?: number;
  sort_by?: string;
}): Promise<ETFSummary[]> {
  const sp = new URLSearchParams();
  if (params?.category) sp.set('category', params.category);
  if (params?.min_score) sp.set('min_score', params.min_score.toString());
  if (params?.sort_by) sp.set('sort_by', params.sort_by);
  const res = await fetch(`${API_URL}/api/etf/list?${sp.toString()}`);
  return res.json();
}

export async function fetchETFDetail(symbol: string): Promise<ETFDetail> {
  const res = await fetch(`${API_URL}/api/etf/detail/${symbol}`);
  return res.json();
}

export async function fetchETFCategories(): Promise<string[]> {
  const res = await fetch(`${API_URL}/api/etf/categories`);
  const data = await res.json();
  return data.categories;
}

// === LIVE DATA REFRESH ===

export interface RefreshResult {
  status: string;
  updated: number;
  failed: string[];
  total: number;
  timestamp: string;
  rescored: number;
}

export async function refreshAllData(): Promise<{
  status: string;
  fundamentals: RefreshResult;
  swing: RefreshResult;
  etfs: RefreshResult;
}> {
  const res = await fetch(`${API_URL}/api/refresh`, { method: 'POST' });
  return res.json();
}

export async function refreshFundamentals(): Promise<RefreshResult> {
  const res = await fetch(`${API_URL}/api/refresh/fundamentals`, { method: 'POST' });
  return res.json();
}

export async function refreshSwing(): Promise<RefreshResult> {
  const res = await fetch(`${API_URL}/api/refresh/swing`, { method: 'POST' });
  return res.json();
}

export async function refreshETFs(): Promise<RefreshResult> {
  const res = await fetch(`${API_URL}/api/refresh/etfs`, { method: 'POST' });
  return res.json();
}

export async function getLastRefresh(): Promise<Record<string, string>> {
  const res = await fetch(`${API_URL}/api/last-refresh`);
  return res.json();
}

export async function seedAndRefresh(): Promise<unknown> {
  const res = await fetch(`${API_URL}/api/seed-and-refresh`, { method: 'POST' });
  return res.json();
}

// === CHART DATA ===

export interface CandleData {
  time: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

export interface OverlayData {
  time: string;
  sma_20: number | null;
  sma_50: number | null;
  sma_200: number | null;
  rsi: number | null;
  bb_upper: number | null;
  bb_middle: number | null;
  bb_lower: number | null;
  macd: number | null;
  macd_signal: number | null;
  macd_hist: number | null;
}

export interface ChartDataResponse {
  symbol: string;
  period: string;
  interval: string;
  currency: string;
  price: number;
  prev_close: number;
  high_52w: number;
  low_52w: number;
  candles: CandleData[];
  overlays: OverlayData[];
  error?: string;
}

export async function fetchChartData(symbol: string, period: string = '1y', interval: string = '1d'): Promise<ChartDataResponse> {
  const res = await fetch(`${API_URL}/api/chart-data/${symbol}?period=${period}&interval=${interval}`);
  return res.json();
}

// === FII / DII DATA ===

export interface BulkDeal {
  date: string;
  type: string;
  buyer_seller: string;
  quantity_lakh: number;
  price: number;
}

export interface FIIDIIData {
  symbol: string;
  fii_holding_pct: number;
  dii_holding_pct: number;
  promoter_holding_pct: number;
  public_holding_pct: number;
  fii_net_monthly: number[];
  dii_net_monthly: number[];
  months: string[];
  fii_net_total_cr: number;
  dii_net_total_cr: number;
  delivery_pct: number;
  institutional_signal: string;
  bulk_deals: BulkDeal[];
  volume_profile: {
    avg_volume_20d: number;
    avg_volume_50d: number;
    latest_volume: number;
    volume_trend: string;
  };
  error?: string;
}

export async function fetchFIIDII(symbol: string): Promise<FIIDIIData> {
  const res = await fetch(`${API_URL}/api/fii-dii/${symbol}`);
  return res.json();
}
