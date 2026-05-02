'use client';

interface VerdictBadgeProps {
  verdict: string;
}

export default function VerdictBadge({ verdict }: VerdictBadgeProps) {
  let cls = 'verdict-avoid';
  if (verdict.includes('Strong Buy')) cls = 'verdict-strong-buy';
  else if (verdict.includes('Quality')) cls = 'verdict-quality';
  else if (verdict.includes('Watch')) cls = 'verdict-watch';
  else if (verdict.includes('Weak')) cls = 'verdict-weak';

  return (
    <span className={`px-2.5 py-1 rounded-full text-xs font-semibold whitespace-nowrap ${cls}`}>
      {verdict}
    </span>
  );
}
