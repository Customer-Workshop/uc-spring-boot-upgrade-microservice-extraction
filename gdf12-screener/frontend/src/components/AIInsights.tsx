'use client';

import { useState } from 'react';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8002';

interface AIInsightsProps {
  tab: 'fundamental' | 'technical' | 'etf';
  symbol?: string;
}

export default function AIInsights({ tab, symbol }: AIInsightsProps) {
  const [analysis, setAnalysis] = useState('');
  const [recommendation, setRecommendation] = useState('');
  const [model, setModel] = useState('');
  const [loading, setLoading] = useState(false);
  const [recLoading, setRecLoading] = useState(false);

  const analyzeStock = async () => {
    if (!symbol) return;
    setLoading(true);
    try {
      const res = await fetch(`${API_URL}/api/ai/analyze/${symbol}?analysis_type=${tab}`);
      const data = await res.json();
      setAnalysis(data.analysis || 'Analysis unavailable');
      setModel(data.model || '');
    } catch (err) {
      setAnalysis('Failed to get AI analysis. Check API key configuration.');
    }
    setLoading(false);
  };

  const getRecommendation = async () => {
    setRecLoading(true);
    try {
      const res = await fetch(`${API_URL}/api/ai/recommend?tab=${tab}`);
      const data = await res.json();
      setRecommendation(data.recommendation || 'No recommendation available. Ensure Claude API key is configured.');
      setModel(data.model || '');
    } catch (err) {
      setRecommendation('Failed to get recommendation.');
    }
    setRecLoading(false);
  };

  return (
    <div className="bg-gradient-to-br from-[#1a1f35] to-[#1e2235] rounded-xl border border-indigo-500/30 p-5 mt-6">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 bg-gradient-to-br from-indigo-400 to-purple-500 rounded-md flex items-center justify-center text-white text-xs font-bold">AI</div>
          <h3 className="font-semibold text-sm">AI-Powered Insights</h3>
          {model && <span className="text-xs text-[#8b90a8] bg-[#0f1117] px-2 py-0.5 rounded">via {model}</span>}
        </div>
      </div>

      <div className="flex gap-3 mb-4">
        {symbol && (
          <button
            onClick={analyzeStock}
            disabled={loading}
            className="px-4 py-2 bg-indigo-500/20 text-indigo-400 border border-indigo-500/30 rounded-lg text-sm font-medium hover:bg-indigo-500/30 transition-colors disabled:opacity-50"
          >
            {loading ? 'Analyzing...' : `Analyze ${symbol}`}
          </button>
        )}
        <button
          onClick={getRecommendation}
          disabled={recLoading}
          className="px-4 py-2 bg-purple-500/20 text-purple-400 border border-purple-500/30 rounded-lg text-sm font-medium hover:bg-purple-500/30 transition-colors disabled:opacity-50"
        >
          {recLoading ? 'Thinking...' : 'Get Top 3 Picks'}
        </button>
      </div>

      {analysis && (
        <div className="bg-[#0f1117] rounded-lg p-4 mb-3 border border-[#2a2e45]">
          <div className="text-xs text-indigo-400 mb-2 font-semibold">Stock Analysis</div>
          <div className="text-sm text-[#c8cce0] whitespace-pre-wrap leading-relaxed">{analysis}</div>
        </div>
      )}

      {recommendation && (
        <div className="bg-[#0f1117] rounded-lg p-4 border border-[#2a2e45]">
          <div className="text-xs text-purple-400 mb-2 font-semibold">AI Top 3 Recommendations</div>
          <div className="text-sm text-[#c8cce0] whitespace-pre-wrap leading-relaxed">{recommendation}</div>
        </div>
      )}

      {!analysis && !recommendation && (
        <p className="text-xs text-[#8b90a8]">
          Click a button above to get AI-powered analysis using Claude or Ollama/Llama3.
          {!symbol && ' Select a stock first to analyze individual stocks.'}
        </p>
      )}
    </div>
  );
}
