const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8002';

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
  return res.json();
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
