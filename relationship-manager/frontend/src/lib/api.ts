const API_BASE = '/api/v1'

export interface ApiResponse<T> {
  success: boolean
  data?: T
  error?: { code: string; message: string }
}

export interface ConversationResponse {
  conversationId: string
  currentPhase: string
  status: string
  channel: string
  createdAt: string
  lastMessageAt: string
}

export interface MessageResponse {
  messageId: string
  conversationId: string
  senderType: 'CUSTOMER' | 'AI' | 'HUMAN_RM'
  content: string
  contentType: string
  phase?: string
  entitiesExtracted?: Record<string, unknown>
  createdAt: string
}

export interface DashboardMetrics {
  totalConversations: number
  totalRiskAssessments: number
  totalRecommendations: number
  totalNotifications: number
  totalFollowUps: number
  riskDistribution: Record<string, number>
  channelDistribution: Record<string, number>
  timestamp: string
}

async function fetchApi<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${url}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  const json: ApiResponse<T> = await res.json()
  if (!json.success) throw new Error(json.error?.message ?? 'API error')
  return json.data!
}

export const api = {
  startConversation: (channel = 'WEB') =>
    fetchApi<ConversationResponse>('/conversations', {
      method: 'POST',
      body: JSON.stringify({ channel }),
    }),

  sendMessage: (conversationId: string, content: string) =>
    fetchApi<{ customerMessage: MessageResponse; aiResponse: MessageResponse }>(
      `/conversations/${conversationId}/messages`,
      { method: 'POST', body: JSON.stringify({ content }) }
    ),

  getConversationHistory: (conversationId: string) =>
    fetchApi<{
      conversationId: string
      currentPhase: string
      status: string
      messages: MessageResponse[]
      extractedData: Record<string, unknown>
    }>(`/conversations/${conversationId}`),

  getDashboard: () => fetchApi<DashboardMetrics>('/analytics/dashboard'),
}

export function createWebSocket(conversationId: string): WebSocket {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return new WebSocket(`${protocol}//${window.location.host}/ws/chat/${conversationId}`)
}
