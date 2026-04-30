"use client";

import { useState } from "react";
import useSWR from "swr";
import { api } from "@/lib/api";
import type { RankingsResponse } from "@/lib/types";
import StockTable from "./StockTable";

interface Props {
  onStockClick: (symbol: string) => void;
}

export default function Dashboard({ onStockClick }: Props) {
  const [sector, setSector] = useState<string>("");
  const [topN, setTopN] = useState(20);

  const { data: sectors } = useSWR("sectors", () => api.getSectors());
  const { data: rankings, error, isLoading } = useSWR<RankingsResponse>(
    ["rankings", topN, sector],
    () => api.getRankings(topN, sector || undefined)
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Top {topN} Stock Rankings</h2>
          <p className="text-sm text-gray-500 mt-1">
            Multi-factor scoring: fundamentals, valuation, technicals, patterns & insider signals
          </p>
        </div>

        <div className="flex gap-3">
          <select
            value={sector}
            onChange={(e) => setSector(e.target.value)}
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
          >
            <option value="">All Sectors</option>
            {sectors?.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>

          <select
            value={topN}
            onChange={(e) => setTopN(Number(e.target.value))}
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
          >
            <option value={10}>Top 10</option>
            <option value={20}>Top 20</option>
            <option value={50}>Top 50</option>
          </select>
        </div>
      </div>

      {rankings && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="card text-center">
            <p className="text-sm text-gray-500">Universe</p>
            <p className="text-2xl font-bold text-gray-900">{rankings.total_universe}</p>
          </div>
          <div className="card text-center">
            <p className="text-sm text-gray-500">Passed Filters</p>
            <p className="text-2xl font-bold text-green-600">{rankings.passed_filters}</p>
          </div>
          <div className="card text-center">
            <p className="text-sm text-gray-500">Date</p>
            <p className="text-2xl font-bold text-blue-600">{rankings.date}</p>
          </div>
        </div>
      )}

      {isLoading && (
        <div className="card text-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto" />
          <p className="text-gray-500 mt-4">Loading rankings...</p>
        </div>
      )}

      {error && (
        <div className="card bg-red-50 border-red-200">
          <p className="text-red-700">
            Failed to load rankings. Make sure the backend is running at localhost:8000.
          </p>
          <p className="text-red-500 text-sm mt-1">{String(error)}</p>
        </div>
      )}

      {rankings && rankings.rankings.length > 0 && (
        <div className="card p-0 overflow-hidden">
          <StockTable rankings={rankings.rankings} onStockClick={onStockClick} />
        </div>
      )}

      {rankings && rankings.rankings.length === 0 && !isLoading && (
        <div className="card text-center py-12">
          <p className="text-gray-500">No rankings available yet.</p>
          <p className="text-sm text-gray-400 mt-2">
            Seed the database and run signal computation first.
          </p>
        </div>
      )}
    </div>
  );
}
