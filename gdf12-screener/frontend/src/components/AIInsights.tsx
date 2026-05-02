'use client';

import { useState } from 'react';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8002';

interface AIInsightsProps {
  tab: 'fundamental' | 'technical' | 'etf';
  symbol?: string;
}

interface NewsImpact {
  sector: string;
  sector_outlook: {
    key_drivers: string[];
    current_outlook: string;
    outlook_detail: string;
    geopolitical_risk: string;
    employment_sensitivity: string;
  };
  macro_events: { category: string; event: string; impact: string; severity: string }[];
  promoter_signals: {
    signals: string[];
    risk_level: string;
    promoter_confidence: string;
  };
}

interface PatternBacktest {
  current_setup: {
    rsi: number;
    volume_ratio: number;
    breakout: boolean;
    bb_squeeze: boolean;
    confirmation_score: number;
    signal: string;
  };
  backtest: {
    pattern_name: string;
    conditions: string;
    sample_size: number;
    win_rate: number;
    avg_profit_pct: number;
    avg_loss_pct: number;
    avg_duration_days: number;
    max_profit_pct: number;
    max_loss_pct: number;
    expectancy_r: number;
    sector_adjustment: string;
    notes: string;
    recommendation: string;
  };
}

interface ConfigStatus {
  claude_configured: boolean;
  claude_key_hint: string;
  ollama_url: string;
  env_file_path: string;
  instructions: string;
}

export default function AIInsights({ tab, symbol }: AIInsightsProps) {
  const [analysis, setAnalysis] = useState('');
  const [recommendation, setRecommendation] = useState('');
  const [newsImpact, setNewsImpact] = useState<NewsImpact | null>(null);
  const [patternBT, setPatternBT] = useState<PatternBacktest | null>(null);
  const [configStatus, setConfigStatus] = useState<ConfigStatus | null>(null);
  const [model, setModel] = useState('');
  const [loading, setLoading] = useState(false);
  const [recLoading, setRecLoading] = useState(false);
  const [newsLoading, setNewsLoading] = useState(false);
  const [patternLoading, setPatternLoading] = useState(false);

  const analyzeStock = async () => {
    if (!symbol) return;
    setLoading(true);
    try {
      const res = await fetch(`${API_URL}/api/ai/analyze/${symbol}?analysis_type=${tab}`);
      const data = await res.json();
      setAnalysis(data.analysis || 'Analysis unavailable');
      setModel(data.model || '');
    } catch {
      setAnalysis('Failed to get AI analysis. Check API key configuration.');
    }
    setLoading(false);
  };

  const getRecommendation = async () => {
    setRecLoading(true);
    try {
      const res = await fetch(`${API_URL}/api/ai/recommend?tab=${tab}`);
      const data = await res.json();
      setRecommendation(data.recommendation || 'Configure Claude API key in .env for AI recommendations.');
      setModel(data.model || '');
    } catch {
      setRecommendation('Failed to get recommendation.');
    }
    setRecLoading(false);
  };

  const getNewsImpact = async () => {
    if (!symbol) return;
    setNewsLoading(true);
    try {
      const res = await fetch(`${API_URL}/api/ai/news-impact/${symbol}`);
      const data = await res.json();
      setNewsImpact(data);
    } catch { /* ignore */ }
    setNewsLoading(false);
  };

  const getPatternBacktest = async () => {
    if (!symbol) return;
    setPatternLoading(true);
    try {
      const res = await fetch(`${API_URL}/api/ai/pattern-backtest/${symbol}`);
      const data = await res.json();
      if (!data.error) setPatternBT(data);
    } catch { /* ignore */ }
    setPatternLoading(false);
  };

  const checkConfig = async () => {
    try {
      const res = await fetch(`${API_URL}/api/ai/config-status`);
      const data = await res.json();
      setConfigStatus(data);
    } catch { /* ignore */ }
  };

  return (
    <div className="bg-gradient-to-br from-[#1a1f35] to-[#1e2235] rounded-xl border border-indigo-500/30 p-5 mt-6">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 bg-gradient-to-br from-indigo-400 to-purple-500 rounded-md flex items-center justify-center text-white text-xs font-bold">AI</div>
          <h3 className="font-semibold text-sm">AI-Powered Insights</h3>
          {model && <span className="text-xs text-[#8b90a8] bg-[#0f1117] px-2 py-0.5 rounded">via {model}</span>}
        </div>
        <button onClick={checkConfig} className="text-xs text-[#8b90a8] hover:text-white transition-colors">
          API Config
        </button>
      </div>

      {/* Config status */}
      {configStatus && (
        <div className={`rounded-lg p-3 mb-4 text-xs border ${configStatus.claude_configured ? 'bg-green-500/10 border-green-500/30 text-green-400' : 'bg-yellow-500/10 border-yellow-500/30 text-yellow-400'}`}>
          <div className="font-semibold mb-1">{configStatus.claude_configured ? 'Claude API Connected' : 'Claude API Not Configured'}</div>
          <div className="text-[#8b90a8]">Key: {configStatus.claude_key_hint}</div>
          <div className="text-[#8b90a8]">{configStatus.instructions}</div>
        </div>
      )}

      {/* Action buttons */}
      <div className="flex flex-wrap gap-2 mb-4">
        {symbol && (
          <>
            <button onClick={analyzeStock} disabled={loading}
              className="px-3 py-1.5 bg-indigo-500/20 text-indigo-400 border border-indigo-500/30 rounded-lg text-xs font-medium hover:bg-indigo-500/30 transition-colors disabled:opacity-50">
              {loading ? 'Analyzing...' : `Analyze ${symbol}`}
            </button>
            <button onClick={getNewsImpact} disabled={newsLoading}
              className="px-3 py-1.5 bg-orange-500/20 text-orange-400 border border-orange-500/30 rounded-lg text-xs font-medium hover:bg-orange-500/30 transition-colors disabled:opacity-50">
              {newsLoading ? 'Loading...' : 'News & Macro Impact'}
            </button>
            {tab === 'technical' && (
              <button onClick={getPatternBacktest} disabled={patternLoading}
                className="px-3 py-1.5 bg-cyan-500/20 text-cyan-400 border border-cyan-500/30 rounded-lg text-xs font-medium hover:bg-cyan-500/30 transition-colors disabled:opacity-50">
                {patternLoading ? 'Loading...' : 'Pattern Backtest'}
              </button>
            )}
          </>
        )}
        <button onClick={getRecommendation} disabled={recLoading}
          className="px-3 py-1.5 bg-purple-500/20 text-purple-400 border border-purple-500/30 rounded-lg text-xs font-medium hover:bg-purple-500/30 transition-colors disabled:opacity-50">
          {recLoading ? 'Thinking...' : 'Top 3 Picks + Buy Reasoning'}
        </button>
      </div>

      {/* Stock Analysis */}
      {analysis && (
        <div className="bg-[#0f1117] rounded-lg p-4 mb-3 border border-[#2a2e45]">
          <div className="text-xs text-indigo-400 mb-2 font-semibold">Stock Analysis + Buy Reasoning</div>
          <div className="text-sm text-[#c8cce0] whitespace-pre-wrap leading-relaxed">{analysis}</div>
        </div>
      )}

      {/* News & Macro Impact */}
      {newsImpact && (
        <div className="bg-[#0f1117] rounded-lg p-4 mb-3 border border-orange-500/20">
          <div className="text-xs text-orange-400 mb-3 font-semibold">
            News & Macro Impact — {newsImpact.sector}
          </div>

          {/* Sector Outlook */}
          <div className="mb-3">
            <div className="flex items-center gap-2 mb-1">
              <span className="text-xs font-semibold text-white">Sector Outlook:</span>
              <span className={`text-xs px-2 py-0.5 rounded ${
                newsImpact.sector_outlook.current_outlook === 'Positive' ? 'bg-green-500/20 text-green-400' :
                newsImpact.sector_outlook.current_outlook === 'Negative' ? 'bg-red-500/20 text-red-400' :
                'bg-yellow-500/20 text-yellow-400'
              }`}>{newsImpact.sector_outlook.current_outlook}</span>
            </div>
            <p className="text-xs text-[#8b90a8]">{newsImpact.sector_outlook.outlook_detail}</p>
          </div>

          {/* Key Drivers */}
          <div className="mb-3">
            <div className="text-xs font-semibold text-white mb-1">Key Drivers:</div>
            <div className="flex flex-wrap gap-1">
              {newsImpact.sector_outlook.key_drivers.map((d, i) => (
                <span key={i} className="text-xs bg-[#1e2235] text-[#8b90a8] px-2 py-0.5 rounded">{d}</span>
              ))}
            </div>
          </div>

          {/* Macro Events */}
          <div className="mb-3">
            <div className="text-xs font-semibold text-white mb-2">Geopolitical & Macro Events:</div>
            <div className="space-y-2">
              {newsImpact.macro_events.slice(0, 5).map((evt, i) => (
                <div key={i} className={`rounded px-3 py-2 border text-xs ${
                  evt.severity === 'high' ? 'border-red-500/30 bg-red-500/5' :
                  evt.severity === 'medium' ? 'border-yellow-500/30 bg-yellow-500/5' :
                  'border-[#2a2e45] bg-[#1e2235]'
                }`}>
                  <div className="flex items-center gap-2 mb-1">
                    <span className={`px-1.5 py-0.5 rounded text-[10px] font-semibold ${
                      evt.severity === 'high' ? 'bg-red-500/20 text-red-400' :
                      evt.severity === 'medium' ? 'bg-yellow-500/20 text-yellow-400' :
                      'bg-gray-500/20 text-gray-400'
                    }`}>{evt.category}</span>
                    <span className="font-medium text-white">{evt.event}</span>
                  </div>
                  <p className="text-[#8b90a8]">{evt.impact}</p>
                </div>
              ))}
            </div>
          </div>

          {/* Promoter Signals */}
          <div>
            <div className="flex items-center gap-2 mb-1">
              <span className="text-xs font-semibold text-white">Promoter Signals:</span>
              <span className={`text-xs px-2 py-0.5 rounded ${
                newsImpact.promoter_signals.risk_level === 'low' ? 'bg-green-500/20 text-green-400' :
                newsImpact.promoter_signals.risk_level === 'high' ? 'bg-red-500/20 text-red-400' :
                'bg-yellow-500/20 text-yellow-400'
              }`}>Risk: {newsImpact.promoter_signals.risk_level}</span>
              <span className={`text-xs px-2 py-0.5 rounded ${
                newsImpact.promoter_signals.promoter_confidence === 'high' ? 'bg-green-500/20 text-green-400' :
                'bg-yellow-500/20 text-yellow-400'
              }`}>Confidence: {newsImpact.promoter_signals.promoter_confidence}</span>
            </div>
            <ul className="text-xs text-[#8b90a8] space-y-0.5">
              {newsImpact.promoter_signals.signals.map((s, i) => (
                <li key={i}>• {s}</li>
              ))}
            </ul>
          </div>
        </div>
      )}

      {/* Pattern Backtest */}
      {patternBT && (
        <div className="bg-[#0f1117] rounded-lg p-4 mb-3 border border-cyan-500/20">
          <div className="text-xs text-cyan-400 mb-3 font-semibold">Pattern Backtest — Historical Probability</div>

          <div className="mb-3">
            <div className="text-sm font-semibold text-white mb-1">{patternBT.backtest.pattern_name}</div>
            <p className="text-xs text-[#8b90a8]">{patternBT.backtest.conditions}</p>
          </div>

          <div className="grid grid-cols-4 gap-3 mb-3">
            <div className="bg-[#1e2235] rounded p-2 text-center">
              <div className="text-[10px] text-[#8b90a8]">Win Rate</div>
              <div className={`text-lg font-bold ${patternBT.backtest.win_rate >= 60 ? 'text-green-400' : patternBT.backtest.win_rate >= 50 ? 'text-yellow-400' : 'text-red-400'}`}>
                {patternBT.backtest.win_rate}%
              </div>
            </div>
            <div className="bg-[#1e2235] rounded p-2 text-center">
              <div className="text-[10px] text-[#8b90a8]">Avg Profit</div>
              <div className="text-lg font-bold text-green-400">+{patternBT.backtest.avg_profit_pct}%</div>
            </div>
            <div className="bg-[#1e2235] rounded p-2 text-center">
              <div className="text-[10px] text-[#8b90a8]">Avg Loss</div>
              <div className="text-lg font-bold text-red-400">{patternBT.backtest.avg_loss_pct}%</div>
            </div>
            <div className="bg-[#1e2235] rounded p-2 text-center">
              <div className="text-[10px] text-[#8b90a8]">Expectancy</div>
              <div className={`text-lg font-bold ${patternBT.backtest.expectancy_r >= 1 ? 'text-green-400' : 'text-yellow-400'}`}>
                {patternBT.backtest.expectancy_r}R
              </div>
            </div>
          </div>

          <div className="grid grid-cols-3 gap-3 mb-3 text-xs">
            <div className="text-[#8b90a8]">Sample Size: <span className="text-white">{patternBT.backtest.sample_size} trades</span></div>
            <div className="text-[#8b90a8]">Avg Duration: <span className="text-white">{patternBT.backtest.avg_duration_days} days</span></div>
            <div className="text-[#8b90a8]">Sector Adj: <span className={patternBT.backtest.sector_adjustment === 'positive' ? 'text-green-400' : patternBT.backtest.sector_adjustment === 'negative' ? 'text-red-400' : 'text-white'}>{patternBT.backtest.sector_adjustment}</span></div>
          </div>

          <div className={`rounded px-3 py-2 text-xs border ${
            patternBT.backtest.win_rate >= 60 ? 'bg-green-500/10 border-green-500/30 text-green-400' :
            patternBT.backtest.win_rate >= 50 ? 'bg-yellow-500/10 border-yellow-500/30 text-yellow-400' :
            'bg-red-500/10 border-red-500/30 text-red-400'
          }`}>
            <span className="font-semibold">Recommendation:</span> {patternBT.backtest.recommendation}
          </div>

          <p className="text-[10px] text-[#8b90a8] mt-2">{patternBT.backtest.notes}</p>
        </div>
      )}

      {/* Recommendation */}
      {recommendation && (
        <div className="bg-[#0f1117] rounded-lg p-4 border border-[#2a2e45]">
          <div className="text-xs text-purple-400 mb-2 font-semibold">AI Top 3 Recommendations + Buy Reasoning</div>
          <div className="text-sm text-[#c8cce0] whitespace-pre-wrap leading-relaxed">{recommendation}</div>
        </div>
      )}

      {!analysis && !recommendation && !newsImpact && !patternBT && !configStatus && (
        <p className="text-xs text-[#8b90a8]">
          Click a button above to get AI-powered analysis.
          {!symbol && ' Select a stock first for individual analysis.'}
          <br />
          <span className="text-indigo-400">Tip:</span> Add your Claude API key to <code className="bg-[#0f1117] px-1 rounded">backend/.env</code> for enhanced AI analysis.
        </p>
      )}
    </div>
  );
}
