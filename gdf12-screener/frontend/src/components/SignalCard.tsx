'use client';

import { SignalDetail } from '@/lib/api';

interface SignalCardProps {
  title: string;
  signals: SignalDetail[];
  color: string;
}

export default function SignalCard({ title, signals, color }: SignalCardProps) {
  const passed = signals.filter(s => s.passed).length;

  return (
    <div className="bg-[#1c1c27] rounded-2xl border border-[#2a2a3a] p-5">
      <div className="flex items-center justify-between mb-4">
        <h3 className="font-semibold text-sm flex items-center gap-2" style={{ color }}>
          <span className="w-1.5 h-1.5 rounded-full" style={{ backgroundColor: color }}></span>
          {title}
        </h3>
        <span className="text-xs text-[#5c5c72] bg-[#16161e] px-2.5 py-1 rounded-full">{passed}/{signals.length} passed</span>
      </div>
      <div className="space-y-2.5">
        {signals.map((s, i) => (
          <div key={i} className={`rounded-xl px-4 py-3 ${s.passed ? 'signal-pass' : 'signal-fail'}`}>
            <div className="flex items-center gap-2.5 mb-1">
              <span className={`text-xs ${s.passed ? 'text-emerald-400' : 'text-red-400'}`}>{s.passed ? '●' : '○'}</span>
              <span className="font-medium text-sm">{s.label}</span>
            </div>
            <p className="text-xs opacity-70 ml-6">{s.detail}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
