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
    <div className="bg-[#1e2235] rounded-xl border border-[#2a2e45] p-5">
      <div className="flex items-center justify-between mb-4">
        <h3 className="font-semibold text-sm" style={{ color }}>{title}</h3>
        <span className="text-xs text-[#8b90a8]">{passed}/{signals.length} passed</span>
      </div>
      <div className="space-y-3">
        {signals.map((s, i) => (
          <div key={i} className={`rounded-lg px-3 py-2.5 ${s.passed ? 'signal-pass' : 'signal-fail'}`}>
            <div className="flex items-center gap-2 mb-1">
              <span className="text-base">{s.passed ? '●' : '○'}</span>
              <span className="font-medium text-sm">{s.label}</span>
            </div>
            <p className="text-xs opacity-80 ml-6">{s.detail}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
