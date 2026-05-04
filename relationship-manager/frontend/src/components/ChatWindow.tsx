'use client'

import { useState, useRef, useEffect, useCallback } from 'react'
import { api, createWebSocket, MessageResponse } from '@/lib/api'

interface ChatWindowProps {
  conversationId: string | null
  onConversationStart: (id: string) => void
}

interface ChatMessage {
  id: string
  sender: 'customer' | 'ai'
  content: string
  phase?: string
  timestamp: string
}

export default function ChatWindow({ conversationId, onConversationStart }: ChatWindowProps) {
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [phase, setPhase] = useState('GREETING')
  const [status, setStatus] = useState<string>('')
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const wsRef = useRef<WebSocket | null>(null)

  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [])

  useEffect(() => { scrollToBottom() }, [messages, scrollToBottom])

  const startConversation = async () => {
    setLoading(true)
    try {
      const conv = await api.startConversation()
      onConversationStart(conv.conversationId)
      setPhase(conv.currentPhase)
      setStatus(conv.status)

      // Load greeting message
      const history = await api.getConversationHistory(conv.conversationId)
      setMessages(history.messages.map(toChat))
    } catch (err) {
      console.error('Failed to start conversation:', err)
    } finally {
      setLoading(false)
    }
  }

  const sendMessage = async () => {
    if (!input.trim() || !conversationId) return
    const text = input.trim()
    setInput('')
    setLoading(true)

    // Optimistic customer message
    const customerMsg: ChatMessage = {
      id: Date.now().toString(),
      sender: 'customer',
      content: text,
      timestamp: new Date().toISOString(),
    }
    setMessages(prev => [...prev, customerMsg])

    try {
      const result = await api.sendMessage(conversationId, text)
      const aiChat = toChat(result.aiResponse)
      setMessages(prev => [...prev, aiChat])
      if (result.aiResponse.phase) setPhase(result.aiResponse.phase)
    } catch (err) {
      console.error('Failed to send message:', err)
      setMessages(prev => [
        ...prev,
        { id: 'err', sender: 'ai', content: 'Sorry, something went wrong. Please try again.', timestamp: new Date().toISOString() }
      ])
    } finally {
      setLoading(false)
    }
  }

  const toChat = (msg: MessageResponse): ChatMessage => ({
    id: msg.messageId,
    sender: msg.senderType === 'CUSTOMER' ? 'customer' : 'ai',
    content: msg.content,
    phase: msg.phase ?? undefined,
    timestamp: msg.createdAt,
  })

  const phaseLabels: Record<string, string> = {
    GREETING: 'Welcome',
    PERSONAL: 'Personal Details',
    FINANCIAL: 'Financial Profile',
    GOALS: 'Retirement Goals',
    RISK_ASSESSMENT: 'Risk Assessment',
    RECOMMENDATION: 'Product Recommendations',
    CHANNEL_PREF: 'Communication Preference',
    FOLLOWUP_SCHEDULE: 'Follow-Up Schedule',
    COMPLETED: 'Complete',
  }

  const phases = Object.keys(phaseLabels)
  const currentPhaseIdx = phases.indexOf(phase)

  return (
    <div className="bg-white rounded-xl shadow-lg overflow-hidden max-w-4xl mx-auto">
      {/* Phase Progress Bar */}
      <div className="bg-bank-light px-4 py-3 border-b">
        <div className="flex items-center gap-1 overflow-x-auto">
          {phases.map((p, idx) => (
            <div key={p} className="flex items-center">
              <div
                className={`px-2 py-1 rounded text-xs font-medium whitespace-nowrap ${
                  idx < currentPhaseIdx
                    ? 'bg-green-100 text-green-700'
                    : idx === currentPhaseIdx
                    ? 'bg-bank-accent text-white'
                    : 'bg-gray-100 text-gray-400'
                }`}
              >
                {phaseLabels[p]}
              </div>
              {idx < phases.length - 1 && (
                <div className={`w-4 h-0.5 mx-0.5 ${idx < currentPhaseIdx ? 'bg-green-300' : 'bg-gray-200'}`} />
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Messages */}
      <div className="h-[500px] overflow-y-auto p-4 space-y-4">
        {!conversationId && (
          <div className="flex flex-col items-center justify-center h-full text-center">
            <div className="w-16 h-16 bg-bank-light rounded-full flex items-center justify-center mb-4">
              <span className="text-2xl">RM</span>
            </div>
            <h2 className="text-xl font-semibold text-gray-700 mb-2">
              Welcome to Your Digital Relationship Manager
            </h2>
            <p className="text-gray-500 mb-4 max-w-md">
              I'll help you explore financial products tailored to your goals.
              Our conversation is private and secure.
            </p>
            <button
              onClick={startConversation}
              disabled={loading}
              className="bg-bank-primary text-white px-6 py-3 rounded-lg hover:bg-bank-secondary transition-colors disabled:opacity-50"
            >
              {loading ? 'Starting...' : 'Start Conversation'}
            </button>
          </div>
        )}

        {messages.map((msg) => (
          <div key={msg.id} className={`flex ${msg.sender === 'customer' ? 'justify-end' : 'justify-start'}`}>
            <div
              className={`max-w-[75%] rounded-2xl px-4 py-3 ${
                msg.sender === 'customer'
                  ? 'bg-bank-primary text-white rounded-br-md'
                  : 'bg-gray-100 text-gray-800 rounded-bl-md'
              }`}
            >
              <p className="whitespace-pre-wrap">{msg.content}</p>
              <p className={`text-xs mt-1 ${msg.sender === 'customer' ? 'text-blue-200' : 'text-gray-400'}`}>
                {new Date(msg.timestamp).toLocaleTimeString()}
              </p>
            </div>
          </div>
        ))}

        {loading && (
          <div className="flex justify-start">
            <div className="bg-gray-100 rounded-2xl px-4 py-3 rounded-bl-md">
              <div className="flex gap-1">
                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0ms' }} />
                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '150ms' }} />
                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '300ms' }} />
              </div>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Input */}
      {conversationId && (
        <div className="border-t p-4 bg-gray-50">
          <div className="flex gap-2">
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && !e.shiftKey && sendMessage()}
              placeholder="Type your message..."
              className="flex-1 border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-bank-accent focus:border-transparent"
              disabled={loading}
            />
            <button
              onClick={sendMessage}
              disabled={loading || !input.trim()}
              className="bg-bank-primary text-white px-6 py-2 rounded-lg hover:bg-bank-secondary transition-colors disabled:opacity-50"
            >
              Send
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
