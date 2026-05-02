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
    <div className="bg-[#1c1c27] rounded-2xl border border-[#a78bfa]/20 p-6 mt-6">
      <div className="flex items-center justify-between mb-5">
        <div className="flex items-center gap-3">
          <div className="w-7 h-7 bg-gradient-to-br from-[#a78bfa] to-[#7c3aed] rounded-lg flex items-center justify-center text-white text-[10px] font-bold">AI</div>
          <h3 className="font-semibold text-sm text-white">AI-Powered Insights</h3>
          {model && <span className="text-[10px] text-[#5c5c72] bg-[#16161e] px-2.5 py-1 rounded-full">via {model}</span>}
        </div>
        <button onClick={checkConfig} className="text-xs text-[#5c5c72] hover:text-[#a78bfa] transition-colors px-3 py-1.5 rounded-lg hover:bg-[#a78bfa]/5">
          API Config
        </button>
      </div>

      {/* Config status */}
      {configStatus && (
        <div className={`rounded-xl p-4 mb-4 text-xs border ${configStatus.claude_configured ? 'bg-[#00d09c]/5 border-[#00d09c]/20 text-[#00d09c]' : 'bg-[#f5a623]/5 border-[#f5a623]/15 text-[#f5a623]'}`}>
          <div className="font-semibold mb-1">{configStatus.claude_configured ? 'Claude API Connected' : 'Claude API Not Configured'}</div>
          <div className="text-[#8c8ca1]">Key: {configStatus.claude_key_hint}</div>
          <div className="text-[#8c8ca1]">{configStatus.instructions}</div>
        </div>
      )}

      {/* Action buttons */}
      <div className="flex flex-wrap gap-2.5 mb-5">
        {symbol && (
          <>
            <button onClick={analyzeStock} disabled={loading}
              className="px-4 py-2 bg-[#a78bfa]/10 text-[#a78bfa] border border-[#a78bfa]/20 rounded-xl text-xs font-medium hover:bg-[#a78bfa]/15 transition-all disabled:opacity-50">
              {loading ? 'Analyzing...' : `Analyze ${symbol}`}
            </button>
            <button onClick={getNewsImpact} disabled={newsLoading}
              className="px-4 py-2 bg-[#f5a623]/10 text-[#f5a623] border border-[#f5a623]/20 rounded-xl text-xs font-medium hover:bg-[#f5a623]/15 transition-all disabled:opacity-50">
              {newsLoading ? 'Loading...' : 'News & Macro Impact'}
            </button>
            {tab === 'technical' && (
              <button onClick={getPatternBacktest} disabled={patternLoading}
                className="px-4 py-2 bg-[#44d7f5]/10 text-[#44d7f5] border border-[#44d7f5]/20 rounded-xl text-xs font-medium hover:bg-[#44d7f5]/15 transition-all disabled:opacity-50">
                {patternLoading ? 'Loading...' : 'Pattern Backtest'}
              </button>
            )}
          </>
        )}
        <button onClick={getRecommendation} disabled={recLoading}
          className="px-4 py-2 bg-[#00d09c]/10 text-[#00d09c] border border-[#00d09c]/20 rounded-xl text-xs font-medium hover:bg-[#00d09c]/15 transition-all disabled:opacity-50">
          {recLoading ? 'Thinking...' : 'Top 3 Picks + Buy Reasoning'}
        </button>
      </div>

      {/* Stock Analysis */}
      {analysis && (
        <div className="bg-[#16161e] rounded-xl p-5 mb-3 border border-[#2a2a3a]">
          <div className="text-xs text-[#a78bfa] mb-2.5 font-semibold">Stock Analysis + Buy Reasoning</div>
          <div className="text-sm text-[#c8cce0] whitespace-pre-wrap leading-relaxed">{analysis}</div>
        </div>
      )}

      {/* News & Macro Impact */}
      {newsImpact && (
        <div className="bg-[#16161e] rounded-xl p-5 mb-3 border border-[#f5a623]/15">
          <div className="text-xs text-[#f5a623] mb-3 font-semibold flex items-center gap-2">
            <span className="w-1.5 h-1.5 rounded-full bg-[#f5a623]"></span>
            News & Macro Impact — {newsImpact.sector}
          </div>

          {/* Sector Outlook */}
          <div className="mb-3">
            <div className="flex items-center gap-2 mb-1">
              <span className="text-xs font-semibold text-white">Sector Outlook:</span>
              <span className={`text-xs px-2.5 py-0.5 rounded-full ${
                newsImpact.sector_outlook.current_outlook === 'Positive' ? 'bg-[#00d09c]/10 text-[#00d09c] border border-[#00d09c]/20' :
                newsImpact.sector_outlook.current_outlook === 'Negative' ? 'bg-[#eb5757]/10 text-[#eb5757] border border-[#eb5757]/20' :
                'bg-[#f5a623]/10 text-[#f5a623] border border-[#f5a623]/20'
              }`}>{newsImpact.sector_outlook.current_outlook}</span>
            </div>
            <p className="text-xs text-[#8c8ca1]">{newsImpact.sector_outlook.outlook_detail}</p>
          </div>

          {/* Key Drivers */}
          <div className="mb-3">
            <div className="text-xs font-semibold text-white mb-1.5">Key Drivers:</div>
            <div className="flex flex-wrap gap-1.5">
              {newsImpact.sector_outlook.key_drivers.map((d, i) => (
                <span key={i} className="text-[10px] bg-[#1c1c27] text-[#8c8ca1] px-2.5 py-1 rounded-full border border-[#2a2a3a]">{d}</span>
              ))}
            </div>
          </div>

          {/* Macro Events */}
          <div className="mb-3">
            <div className="text-xs font-semibold text-white mb-2">Geopolitical & Macro Events:</div>
            <div className="space-y-2">
              {newsImpact.macro_events.slice(0, 5).map((evt, i) => (
                <div key={i} className={`rounded-xl px-4 py-3 border text-xs ${
                  evt.severity === 'high' ? 'border-[#eb5757]/20 bg-[#eb5757]/5' :
                  evt.severity === 'medium' ? 'border-[#f5a623]/15 bg-[#f5a623]/5' :
                  'border-[#2a2a3a] bg-[#1c1c27]'
                }`}>
                  <div className="flex items-center gap-2 mb-1">
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                      evt.severity === 'high' ? 'bg-[#eb5757]/15 text-[#eb5757]' :
                      evt.severity === 'medium' ? 'bg-[#f5a623]/15 text-[#f5a623]' :
                      'bg-[#2a2a3a] text-[#5c5c72]'
                    }`}>{evt.category}</span>
                    <span className="font-medium text-white">{evt.event}</span>
                  </div>
                  <p className="text-[#8c8ca1]">{evt.impact}</p>
                </div>
              ))}
            </div>
          </div>

          {/* Promoter Signals */}
          <div>
            <div className="flex items-center gap-2 mb-1.5">
              <span className="text-xs font-semibold text-white">Promoter Signals:</span>
              <span className={`text-[10px] px-2.5 py-0.5 rounded-full border ${
                newsImpact.promoter_signals.risk_level === 'low' ? 'bg-[#00d09c]/10 text-[#00d09c] border-[#00d09c]/20' :
                newsImpact.promoter_signals.risk_level === 'high' ? 'bg-[#eb5757]/10 text-[#eb5757] border-[#eb5757]/20' :
                'bg-[#f5a623]/10 text-[#f5a623] border-[#f5a623]/20'
              }`}>Risk: {newsImpact.promoter_signals.risk_level}</span>
              <span className={`text-[10px] px-2.5 py-0.5 rounded-full border ${
                newsImpact.promoter_signals.promoter_confidence === 'high' ? 'bg-[#00d09c]/10 text-[#00d09c] border-[#00d09c]/20' :
                'bg-[#f5a623]/10 text-[#f5a623] border-[#f5a623]/20'
              }`}>Confidence: {newsImpact.promoter_signals.promoter_confidence}</span>
            </div>
            <ul className="text-xs text-[#8c8ca1] space-y-1">
              {newsImpact.promoter_signals.signals.map((s, i) => (
                <li key={i}>• {s}</li>
              ))}
            </ul>
          </div>
        </div>
      )}

      {/* Pattern Backtest */}
      {patternBT && (
        <div className="bg-[#16161e] rounded-xl p-5 mb-3 border border-[#44d7f5]/15">
          <div className="text-xs text-[#44d7f5] mb-3 font-semibold flex items-center gap-2">
            <span className="w-1.5 h-1.5 rounded-full bg-[#44d7f5]"></span>
            Pattern Backtest — Historical Probability
          </div>

          <div className="mb-3">
            <div className="text-sm font-semibold text-white mb-1">{patternBT.backtest.pattern_name}</div>
            <p className="text-xs text-[#8c8ca1]">{patternBT.backtest.conditions}</p>
          </div>

          <div className="grid grid-cols-4 gap-3 mb-3">
            <div className="bg-[#1c1c27] rounded-xl p-3 text-center">
              <div className="text-[10px] text-[#5c5c72] uppercase tracking-wider">Win Rate</div>
              <div className={`text-lg font-bold ${patternBT.backtest.win_rate >= 60 ? 'text-[#00d09c]' : patternBT.backtest.win_rate >= 50 ? 'text-[#f5a623]' : 'text-[#eb5757]'}`}>
                {patternBT.backtest.win_rate}%
              </div>
            </div>
            <div className="bg-[#1c1c27] rounded-xl p-3 text-center">
              <div className="text-[10px] text-[#5c5c72] uppercase tracking-wider">Avg Profit</div>
              <div className="text-lg font-bold text-[#00d09c]">+{patternBT.backtest.avg_profit_pct}%</div>
            </div>
            <div className="bg-[#1c1c27] rounded-xl p-3 text-center">
              <div className="text-[10px] text-[#5c5c72] uppercase tracking-wider">Avg Loss</div>
              <div className="text-lg font-bold text-[#eb5757]">{patternBT.backtest.avg_loss_pct}%</div>
            </div>
            <div className="bg-[#1c1c27] rounded-xl p-3 text-center">
              <div className="text-[10px] text-[#5c5c72] uppercase tracking-wider">Expectancy</div>
              <div className={`text-lg font-bold ${patternBT.backtest.expectancy_r >= 1 ? 'text-[#00d09c]' : 'text-[#f5a623]'}`}>
                {patternBT.backtest.expectancy_r}R
              </div>
            </div>
          </div>

          <div className="grid grid-cols-3 gap-3 mb-3 text-xs">
            <div className="text-[#5c5c72]">Sample Size: <span className="text-white">{patternBT.backtest.sample_size} trades</span></div>
            <div className="text-[#5c5c72]">Avg Duration: <span className="text-white">{patternBT.backtest.avg_duration_days} days</span></div>
            <div className="text-[#5c5c72]">Sector Adj: <span className={patternBT.backtest.sector_adjustment === 'positive' ? 'text-[#00d09c]' : patternBT.backtest.sector_adjustment === 'negative' ? 'text-[#eb5757]' : 'text-white'}>{patternBT.backtest.sector_adjustment}</span></div>
          </div>

          <div className={`rounded-xl px-4 py-2.5 text-xs border ${
            patternBT.backtest.win_rate >= 60 ? 'bg-[#00d09c]/5 border-[#00d09c]/20 text-[#00d09c]' :
            patternBT.backtest.win_rate >= 50 ? 'bg-[#f5a623]/5 border-[#f5a623]/15 text-[#f5a623]' :
            'bg-[#eb5757]/5 border-[#eb5757]/15 text-[#eb5757]'
          }`}>
            <span className="font-semibold">Recommendation:</span> {patternBT.backtest.recommendation}
          </div>

          <p className="text-[10px] text-[#5c5c72] mt-2">{patternBT.backtest.notes}</p>
        </div>
      )}

      {/* Recommendation */}
      {recommendation && (
        <div className="bg-[#16161e] rounded-xl p-5 border border-[#2a2a3a]">
          <div className="text-xs text-[#00d09c] mb-2.5 font-semibold flex items-center gap-2">
            <span className="w-1.5 h-1.5 rounded-full bg-[#00d09c]"></span>
            AI Top 3 Recommendations + Buy Reasoning
          </div>
          <div className="text-sm text-[#c8cce0] whitespace-pre-wrap leading-relaxed">{recommendation}</div>
        </div>
      )}

      {!analysis && !recommendation && !newsImpact && !patternBT && !configStatus && (
        <p className="text-xs text-[#5c5c72]">
          Click a button above to get AI-powered analysis.
          {!symbol && ' Select a stock first for individual analysis.'}
          <br />
          <span className="text-[#a78bfa]">Tip:</span> Add your Claude API key to <code className="bg-[#16161e] px-1.5 py-0.5 rounded text-[#8c8ca1]">backend/.env</code> for enhanced AI analysis.
        </p>
      )}
    </div>
  );
}
