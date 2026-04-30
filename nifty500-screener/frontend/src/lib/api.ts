const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8000";

async function fetchAPI<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  if (!res.ok) {
    throw new Error(`API error: ${res.status} ${res.statusText}`);
  }
  return res.json();
}

export const api = {
  getUniverse: (params?: { sector?: string; search?: string }) => {
    const query = new URLSearchParams();
    if (params?.sector) query.set("sector", params.sector);
    if (params?.search) query.set("search", params.search);
    const qs = query.toString();
    return fetchAPI<import("./types").Stock[]>(`/universe${qs ? `?${qs}` : ""}`);
  },

  getSectors: () => fetchAPI<string[]>("/universe/sectors"),

  getRankings: (topN = 20, sector?: string) => {
    const query = new URLSearchParams({ top_n: topN.toString() });
    if (sector) query.set("sector", sector);
    return fetchAPI<import("./types").RankingsResponse>(`/rankings/today?${query}`);
  },

  getStockSnapshot: (symbol: string) =>
    fetchAPI<import("./types").StockSnapshot>(`/stock/${symbol}/snapshot`),

  getChartData: (symbol: string, days = 365) =>
    fetchAPI<import("./types").OHLCV[]>(`/stock/${symbol}/chart?days=${days}`),

  getStockFundamentals: (symbol: string) =>
    fetchAPI<import("./types").Fundamentals[]>(`/stock/${symbol}/fundamentals`),

  getStockTechnicals: (symbol: string, days = 30) =>
    fetchAPI<import("./types").TechnicalSignal[]>(`/stock/${symbol}/technicals?days=${days}`),

  runBacktest: (request: import("./types").BacktestRequest) =>
    fetchAPI<import("./types").BacktestResponse>("/backtest/run", {
      method: "POST",
      body: JSON.stringify(request),
    }),

  seedData: () => fetchAPI<{ status: string }>("/signals/seed", { method: "POST" }),

  recomputeSignals: () =>
    fetchAPI<Record<string, unknown>>("/signals/recompute", { method: "POST" }),
};
