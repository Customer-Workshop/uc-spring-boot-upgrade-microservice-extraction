import useSWR from "swr";
import { api } from "@/lib/api";
import type { RankingsResponse, Stock, StockSnapshot } from "@/lib/types";

const fetcher = <T>(fn: () => Promise<T>) => fn();

export function useUniverse(sector?: string, search?: string) {
  return useSWR<Stock[]>(
    ["universe", sector, search],
    () => api.getUniverse({ sector: sector || undefined, search: search || undefined }),
    { revalidateOnFocus: false }
  );
}

export function useSectors() {
  return useSWR<string[]>("sectors", () => api.getSectors(), {
    revalidateOnFocus: false,
  });
}

export function useRankings(topN = 20, sector?: string) {
  return useSWR<RankingsResponse>(
    ["rankings", topN, sector],
    () => api.getRankings(topN, sector || undefined),
    { revalidateOnFocus: false }
  );
}

export function useStockSnapshot(symbol: string | null) {
  return useSWR<StockSnapshot>(
    symbol ? ["snapshot", symbol] : null,
    () => (symbol ? api.getStockSnapshot(symbol) : Promise.reject("No symbol")),
    { revalidateOnFocus: false }
  );
}
