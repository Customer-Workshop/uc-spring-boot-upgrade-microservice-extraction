"use client";

import type { Score } from "@/lib/types";

interface Props {
  score: Score;
}

function ScoreBar({ label, value, max = 100 }: { label: string; value: number; max?: number }) {
  const pct = Math.min((value / max) * 100, 100);
  const color = pct >= 70 ? "bg-green-500" : pct >= 40 ? "bg-yellow-500" : "bg-red-500";

  return (
    <div className="mb-3">
      <div className="flex justify-between text-sm mb-1">
        <span className="text-gray-600">{label}</span>
        <span className="font-medium">{value.toFixed(1)}</span>
      </div>
      <div className="w-full bg-gray-200 rounded-full h-2">
        <div className={`${color} rounded-full h-2 transition-all`} style={{ width: `${pct}%` }} />
      </div>
    </div>
  );
}

export default function ScoreCard({ score }: Props) {
  return (
    <div className="card">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-bold text-gray-900">Score Breakdown</h3>
        <div className="text-3xl font-bold text-blue-600">{score.total_score.toFixed(1)}</div>
      </div>

      {score.rank && (
        <p className="text-sm text-gray-500 mb-4">Rank #{score.rank} in NIFTY 500</p>
      )}

      <ScoreBar label="Fundamentals (35%)" value={score.fundamentals_score} />
      <ScoreBar label="Valuation (20%)" value={score.valuation_score} />
      <ScoreBar label="Technical (25%)" value={score.technical_score} />
      <ScoreBar label="Patterns (10%)" value={score.pattern_score} />
      <ScoreBar label="Insider/News (10%)" value={score.insider_news_score} />

      <div className="mt-4 pt-4 border-t border-gray-100">
        <p className="text-sm text-gray-600 mb-2">
          Data Completeness: {(score.data_completeness * 100).toFixed(0)}%
        </p>
        {score.reasons_text && (
          <p className="text-sm text-gray-700 italic">{score.reasons_text}</p>
        )}
      </div>

      {score.top_factors && Object.keys(score.top_factors).length > 0 && (
        <div className="mt-4">
          <h4 className="text-sm font-semibold text-green-700 mb-1">Top Strengths</h4>
          <div className="flex flex-wrap gap-1">
            {Object.entries(score.top_factors).map(([k, v]) => (
              <span key={k} className="score-badge score-high">
                {k}: {(v as number).toFixed(0)}
              </span>
            ))}
          </div>
        </div>
      )}

      {score.key_risks && Object.keys(score.key_risks).length > 0 && (
        <div className="mt-3">
          <h4 className="text-sm font-semibold text-red-700 mb-1">Key Risks</h4>
          <div className="flex flex-wrap gap-1">
            {Object.entries(score.key_risks).map(([k, v]) => (
              <span key={k} className="score-badge score-low">
                {k}: {(v as number).toFixed(0)}
              </span>
            ))}
          </div>
        </div>
      )}

      {!score.passed_hard_filters && score.filter_failures && (
        <div className="mt-3 p-3 bg-red-50 rounded-lg">
          <p className="text-sm text-red-700 font-medium">Filter Failures:</p>
          <p className="text-sm text-red-600">{score.filter_failures}</p>
        </div>
      )}
    </div>
  );
}
