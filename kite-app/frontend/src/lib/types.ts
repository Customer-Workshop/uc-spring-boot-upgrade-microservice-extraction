export interface MarketIndex {
  name: string;
  value: number;
  change: number;
  change_pct: number;
}

export interface WatchlistItem {
  id: number;
  symbol: string;
  exchange: string;
  last_price: number;
  change: number;
  change_pct: number;
}

export interface Watchlist {
  id: number;
  name: string;
  items: WatchlistItem[];
}

export interface Instrument {
  id: number;
  symbol: string;
  name: string;
  exchange: string;
  instrument_type: string;
  sector: string | null;
  last_price: number;
  change: number;
  change_pct: number;
  open_price: number;
  high_price: number;
  low_price: number;
  close_price: number;
  volume: number;
}

export interface OHLCV {
  date: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

export interface Order {
  id: number;
  symbol: string;
  exchange: string;
  transaction_type: string;
  order_type: string;
  product: string;
  quantity: number;
  price: number;
  trigger_price: number;
  disclosed_qty: number;
  status: string;
  filled_qty: number;
  average_price: number;
  order_timestamp: string | null;
  tag: string | null;
}

export interface Holding {
  symbol: string;
  exchange: string;
  quantity: number;
  average_price: number;
  last_price: number;
  pnl: number;
  day_change: number;
  day_change_pct: number;
}

export interface Position {
  symbol: string;
  exchange: string;
  product: string;
  quantity: number;
  buy_qty: number;
  sell_qty: number;
  buy_price: number;
  sell_price: number;
  last_price: number;
  pnl: number;
}

export interface Fund {
  equity_available: number;
  equity_used: number;
  commodity_available: number;
  commodity_used: number;
  opening_balance: number;
  payin: number;
  payout: number;
  collateral: number;
}

export interface Dashboard {
  indices: MarketIndex[];
  holdings_count: number;
  holdings_value: number;
  holdings_investment: number;
  holdings_pnl: number;
  holdings_day_pnl: number;
  positions_count: number;
  positions_pnl: number;
}

export interface SearchResult {
  symbol: string;
  name: string;
  exchange: string;
  instrument_type: string;
  last_price: number;
}

export interface OrderFormData {
  symbol: string;
  exchange: string;
  transaction_type: "BUY" | "SELL";
  order_type: "MARKET" | "LIMIT" | "SL" | "SL-M";
  product: "CNC" | "MIS" | "NRML";
  quantity: number;
  price: number;
  trigger_price: number;
  disclosed_qty: number;
}
