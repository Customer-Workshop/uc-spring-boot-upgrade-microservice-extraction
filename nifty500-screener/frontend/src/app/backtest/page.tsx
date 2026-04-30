"use client";

import { useState } from "react";
import { api } from "@/lib/api";
import type { BacktestRequest, BacktestResponse } from "@/lib/types";
import BacktestForm from "@/components/BacktestForm";
import EquityCurve from "@/components/EquityCurve";

export default function BacktestPage() {
  const [result, setResult] = useState<BacktestResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleRunBacktest = async (request: BacktestRequest) => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.runBacktest(request);
      setResult(response);
    } catch (err) {
      setError(String(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900">Backtesting Engine</h2>
        <p className="text-sm text-gray-500 mt-1">
          Walk-forward backtest with transaction costs, slippage modeling, and comprehensive
          metrics
        </p>
      </div>

      <BacktestForm onSubmit={handleRunBacktest} loading={loading} />

      {error && (
        <div className="card bg-red-50 border-red-200">
          <p className="text-red-700">Backtest failed: {error}</p>
        </div>
      )}

      {result && <EquityCurve result={result} />}
    </div>
  );
}
