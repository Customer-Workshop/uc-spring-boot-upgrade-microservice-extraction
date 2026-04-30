const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8001";

async function fetchJSON<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...options?.headers,
    },
  });
  if (!res.ok) {
    throw new Error(`API error ${res.status}: ${await res.text()}`);
  }
  return res.json();
}

export const api = {
  getDashboard: () => fetchJSON<import("./types").Dashboard>("/api/dashboard"),
  getWatchlists: () => fetchJSON<import("./types").Watchlist[]>("/api/watchlists"),
  getIndices: () => fetchJSON<import("./types").MarketIndex[]>("/api/market/indices"),
  searchInstruments: (q: string) =>
    fetchJSON<import("./types").SearchResult[]>(`/api/instruments/search?q=${encodeURIComponent(q)}`),
  getInstrument: (symbol: string) =>
    fetchJSON<import("./types").Instrument>(`/api/instruments/${encodeURIComponent(symbol)}`),
  getOHLCV: (symbol: string) =>
    fetchJSON<import("./types").OHLCV[]>(`/api/instruments/${encodeURIComponent(symbol)}/ohlcv`),
  getOrders: () => fetchJSON<import("./types").Order[]>("/api/orders"),
  placeOrder: (data: import("./types").OrderFormData) =>
    fetchJSON<import("./types").Order>("/api/orders", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  getHoldings: () => fetchJSON<import("./types").Holding[]>("/api/holdings"),
  getPositions: () => fetchJSON<import("./types").Position[]>("/api/positions"),
  getFunds: () => fetchJSON<import("./types").Fund>("/api/funds"),
  seed: () => fetchJSON<Record<string, unknown>>("/api/seed", { method: "POST" }),
  addWatchlistItem: (wlId: number, symbol: string) =>
    fetchJSON<import("./types").WatchlistItem>(`/api/watchlists/${wlId}/items`, {
      method: "POST",
      body: JSON.stringify({ symbol }),
    }),
  removeWatchlistItem: (wlId: number, itemId: number) =>
    fetchJSON<Record<string, unknown>>(`/api/watchlists/${wlId}/items/${itemId}`, {
      method: "DELETE",
    }),
};

export const fetcher = (url: string) => fetchJSON(url);
