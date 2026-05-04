'use client'

import { useState, useEffect } from 'react'
import { api, DashboardMetrics } from '@/lib/api'

export default function Dashboard() {
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.getDashboard()
      .then(setMetrics)
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-bank-primary" />
      </div>
    )
  }

  const cards = [
    { label: 'Total Conversations', value: metrics?.totalConversations ?? 0, color: 'bg-blue-500' },
    { label: 'Risk Assessments', value: metrics?.totalRiskAssessments ?? 0, color: 'bg-green-500' },
    { label: 'Recommendations', value: metrics?.totalRecommendations ?? 0, color: 'bg-purple-500' },
    { label: 'Follow-Ups', value: metrics?.totalFollowUps ?? 0, color: 'bg-orange-500' },
    { label: 'Notifications', value: metrics?.totalNotifications ?? 0, color: 'bg-pink-500' },
  ]

  const riskData = metrics?.riskDistribution ?? {}
  const channelData = metrics?.channelDistribution ?? {}

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold text-gray-800">Analytics Dashboard</h2>

      {/* Metric Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-4">
        {cards.map((card) => (
          <div key={card.label} className="bg-white rounded-xl shadow-md p-5">
            <div className={`w-10 h-10 ${card.color} rounded-lg flex items-center justify-center text-white text-lg font-bold mb-3`}>
              {card.value}
            </div>
            <p className="text-sm text-gray-600">{card.label}</p>
            <p className="text-2xl font-bold text-gray-800">{card.value.toLocaleString()}</p>
          </div>
        ))}
      </div>

      {/* Risk Distribution */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl shadow-md p-6">
          <h3 className="text-lg font-semibold mb-4">Risk Profile Distribution</h3>
          <div className="space-y-3">
            {Object.entries(riskData).map(([category, count]) => {
              const total = Object.values(riskData).reduce((a, b) => a + b, 0)
              const pct = total > 0 ? (count / total) * 100 : 0
              const colors: Record<string, string> = {
                CONSERVATIVE: 'bg-green-400',
                MODERATE: 'bg-blue-400',
                AGGRESSIVE: 'bg-orange-400',
                VERY_AGGRESSIVE: 'bg-red-400',
              }
              return (
                <div key={category}>
                  <div className="flex justify-between text-sm mb-1">
                    <span className="font-medium">{category}</span>
                    <span className="text-gray-500">{count} ({pct.toFixed(0)}%)</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div className={`${colors[category] ?? 'bg-gray-400'} h-2 rounded-full`} style={{ width: `${pct}%` }} />
                  </div>
                </div>
              )
            })}
            {Object.keys(riskData).length === 0 && <p className="text-gray-400 text-sm">No data yet</p>}
          </div>
        </div>

        {/* Channel Distribution */}
        <div className="bg-white rounded-xl shadow-md p-6">
          <h3 className="text-lg font-semibold mb-4">Channel Distribution</h3>
          <div className="space-y-3">
            {Object.entries(channelData)
              .filter(([k]) => !k.startsWith('notif_'))
              .map(([channel, count]) => (
                <div key={channel} className="flex items-center justify-between py-2 border-b last:border-0">
                  <span className="font-medium text-sm">{channel}</span>
                  <span className="bg-bank-light text-bank-primary px-3 py-1 rounded-full text-sm font-medium">
                    {count}
                  </span>
                </div>
              ))}
            {Object.keys(channelData).length === 0 && <p className="text-gray-400 text-sm">No data yet</p>}
          </div>
        </div>
      </div>

      {/* Wealth Projection Placeholder */}
      <div className="bg-white rounded-xl shadow-md p-6">
        <h3 className="text-lg font-semibold mb-4">Wealth Projection</h3>
        <div className="h-64 flex items-center justify-center text-gray-400 border-2 border-dashed rounded-lg">
          <div className="text-center">
            <p className="text-lg">Monte Carlo Simulation Chart</p>
            <p className="text-sm mt-1">Complete a risk assessment to generate projections</p>
            <p className="text-xs mt-2">10,000 scenarios | P10/P25/P50/P75/P90 bands</p>
          </div>
        </div>
      </div>
    </div>
  )
}
