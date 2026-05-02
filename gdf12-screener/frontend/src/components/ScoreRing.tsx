'use client';

interface ScoreRingProps {
  score: number;
  maxScore?: number;
  size?: number;
}

export default function ScoreRing({ score, maxScore = 12, size = 56 }: ScoreRingProps) {
  const radius = (size - 8) / 2;
  const circumference = 2 * Math.PI * radius;
  const pct = score / maxScore;
  const offset = circumference * (1 - pct);

  let color = '#eb5757';
  if (score >= 10) color = '#00d09c';
  else if (score >= 9) color = '#44d7f5';
  else if (score >= 7) color = '#f5a623';
  else if (score >= 5) color = '#ff8c42';

  return (
    <div className="relative inline-flex items-center justify-center" style={{ width: size, height: size }}>
      <svg width={size} height={size} className="-rotate-90">
        <circle
          cx={size / 2} cy={size / 2} r={radius}
          fill="none" stroke="#2a2a3a" strokeWidth="3.5"
        />
        <circle
          cx={size / 2} cy={size / 2} r={radius}
          fill="none" stroke={color} strokeWidth="3.5"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          strokeLinecap="round"
          className="transition-all duration-700"
        />
      </svg>
      <span className="absolute text-sm font-bold" style={{ color }}>
        {score}
      </span>
    </div>
  );
}
