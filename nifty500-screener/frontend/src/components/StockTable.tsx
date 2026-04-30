"use client";

import { useState } from "react";
import type { RankedStock } from "@/lib/types";

interface Props {
  rankings: RankedStock[];
  onStockClick: (symbol: string) => void;
}

type SortKey = "rank" | "total_score" | "fundamentals_score" | "valuation_score" | "technical_score";

function getScoreClass(score: number): string {
  if (score >= 70) return "score-badge score-high";
  if (score >= 40) return "score-badge score-medium";
  return "score-badge score-low";
}

export default function StockTable({ rankings, onStockClick }: Props) {
  const [sortKey, setSortKey] = useState<SortKey>("rank");
  const [sortAsc, setSortAsc] = useState(true);

  const sorted = [...rankings].sort((a, b) => {
    const aVal = a[sortKey];
    const bVal = b[sortKey];
    return sortAsc ? aVal - bVal : bVal - aVal;
  });

  const handleSort = (key: SortKey) => {
    if (sortKey === key) {
      setSortAsc(!sortAsc);
    } else {
      setSortKey(key);
      setSortAsc(key === "rank");
    }
  };

  const SortHeader = ({ label, field }: { label: string; field: SortKey }) => (
    <th
      className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider cursor-pointer hover:text-blue-600"
      onClick={() => handleSort(field)}
    >
      {label} {sortKey === field ? (sortAsc ? "▲" : "▼") : ""}
    </th>
  );

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <SortHeader label="Rank" field="rank" />
            <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">
              Symbol
            </th>
            <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">
              Company
            </th>
            <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">
              Sector
            </th>
            <SortHeader label="Score" field="total_score" />
            <SortHeader label="Fundamentals" field="fundamentals_score" />
            <SortHeader label="Valuation" field="valuation_score" />
            <SortHeader label="Technical" field="technical_score" />
            <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">
              Why Ranked
            </th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-100">
          {sorted.map((stock) => (
            <tr
              key={stock.symbol}
              className="hover:bg-blue-50 cursor-pointer transition-colors"
              onClick={() => onStockClick(stock.symbol)}
            >
              <td className="px-4 py-3 text-sm font-bold text-gray-900">#{stock.rank}</td>
              <td className="px-4 py-3 text-sm font-semibold text-blue-600">{stock.symbol}</td>
              <td className="px-4 py-3 text-sm text-gray-700">{stock.company_name}</td>
              <td className="px-4 py-3 text-sm text-gray-500">{stock.sector || "-"}</td>
              <td className="px-4 py-3">
                <span className={getScoreClass(stock.total_score)}>
                  {stock.total_score.toFixed(1)}
                </span>
              </td>
              <td className="px-4 py-3">
                <span className={getScoreClass(stock.fundamentals_score)}>
                  {stock.fundamentals_score.toFixed(1)}
                </span>
              </td>
              <td className="px-4 py-3">
                <span className={getScoreClass(stock.valuation_score)}>
                  {stock.valuation_score.toFixed(1)}
                </span>
              </td>
              <td className="px-4 py-3">
                <span className={getScoreClass(stock.technical_score)}>
                  {stock.technical_score.toFixed(1)}
                </span>
              </td>
              <td className="px-4 py-3 text-xs text-gray-500 max-w-xs truncate">
                {stock.reasons_text || "-"}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
