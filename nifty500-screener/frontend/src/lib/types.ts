export interface Stock {
  id: number;
  symbol: string;
  company_name: string;
  industry: string | null;
  sector: string | null;
  isin: string | null;
  market_cap: number | null;
  is_nifty500: boolean;
  avg_daily_value: number | null;
}

export interface OHLCV {
  date: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
  adjusted_close: number | null;
}

export interface Fundamentals {
  report_date: string;
  period: string;
  roe: number | null;
  roce: number | null;
  roa: number | null;
  gross_margin: number | null;
  ebitda_margin: number | null;
  net_margin: number | null;
  revenue: number | null;
  eps: number | null;
  revenue_growth_3y: number | null;
  eps_growth_3y: number | null;
  debt_to_equity: number | null;
  interest_coverage: number | null;
  pe_ratio: number | null;
  pb_ratio: number | null;
  ev_to_ebitda: number | null;
  fcf_yield: number | null;
}

export interface TechnicalSignal {
  date: string;
  rsi_14: number | null;
  sma_20: number | null;
  sma_50: number | null;
  sma_200: number | null;
  macd_line: number | null;
  macd_signal: number | null;
  macd_histogram: number | null;
  bb_upper: number | null;
  bb_middle: number | null;
  bb_lower: number | null;
  atr_14: number | null;
  rs_vs_nifty500: number | null;
}

export interface Disclosure {
  disclosure_date: string;
  disclosure_type: string;
  entity_name: string | null;
  transaction_type: string;
  shares: number | null;
  value_inr: number | null;
  promoter_holding_pct: number | null;
  promoter_pledge_pct: number | null;
  signal_strength: number | null;
}

export interface NewsItem {
  title: string;
  summary: string | null;
  url: string | null;
  source: string | null;
  published_at: string | null;
  sentiment: string | null;
  sentiment_score: number | null;
  impact_score: number | null;
  category: string | null;
}

export interface Score {
  date: string;
  total_score: number;
  rank: number | null;
  fundamentals_score: number;
  valuation_score: number;
  technical_score: number;
  pattern_score: number;
  insider_news_score: number;
  passed_hard_filters: boolean;
  filter_failures: string | null;
  top_factors: Record<string, number> | null;
  key_risks: Record<string, number> | null;
  missing_data: Record<string, string> | null;
  reasons_text: string | null;
  data_completeness: number;
}

export interface StockSnapshot {
  stock: Stock;
  latest_price: OHLCV | null;
  fundamentals: Fundamentals | null;
  technical: TechnicalSignal | null;
  disclosures: Disclosure[];
  news: NewsItem[];
  score: Score | null;
}

export interface RankedStock {
  rank: number;
  symbol: string;
  company_name: string;
  sector: string | null;
  total_score: number;
  fundamentals_score: number;
  valuation_score: number;
  technical_score: number;
  pattern_score: number;
  insider_news_score: number;
  reasons_text: string | null;
  top_factors: Record<string, number> | null;
  key_risks: Record<string, number> | null;
}

export interface RankingsResponse {
  date: string;
  total_universe: number;
  passed_filters: number;
  rankings: RankedStock[];
}

export interface BacktestRequest {
  strategy: string;
  start_date?: string;
  end_date?: string;
  initial_capital?: number;
  position_size_pct?: number;
  stop_loss_pct?: number;
  take_profit_pct?: number;
  slippage_pct?: number;
  commission_pct?: number;
  symbols?: string[];
}

export interface TradeRecord {
  symbol: string;
  entry_date: string;
  exit_date: string | null;
  entry_price: number;
  exit_price: number | null;
  shares: number;
  pnl: number;
  pnl_pct: number;
  r_multiple: number;
  exit_reason: string;
}

export interface BacktestMetrics {
  cagr: number;
  sharpe_ratio: number;
  max_drawdown: number;
  win_rate: number;
  avg_r_multiple: number;
  total_trades: number;
  winning_trades: number;
  losing_trades: number;
  avg_holding_days: number;
  exposure_pct: number;
  turnover: number;
  profit_factor: number;
  expectancy: number;
}

export interface EquityPoint {
  date: string;
  equity: number;
  drawdown: number;
  benchmark_equity: number | null;
}

export interface BacktestResponse {
  strategy: string;
  start_date: string;
  end_date: string;
  initial_capital: number;
  final_equity: number;
  metrics: BacktestMetrics;
  equity_curve: EquityPoint[];
  trades: TradeRecord[];
}
