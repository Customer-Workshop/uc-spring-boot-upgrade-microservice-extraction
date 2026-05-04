'use client'

import { useState } from 'react'
import ChatWindow from '@/components/ChatWindow'
import Dashboard from '@/components/Dashboard'

export default function Home() {
  const [activeTab, setActiveTab] = useState<'chat' | 'dashboard'>('chat')
  const [conversationId, setConversationId] = useState<string | null>(null)

  return (
    <div className="min-h-screen">
      {/* Header */}
      <header className="bg-bank-primary text-white shadow-lg">
        <div className="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-bank-accent rounded-full flex items-center justify-center text-lg font-bold">
              RM
            </div>
            <div>
              <h1 className="text-xl font-semibold">Banking Relationship Manager</h1>
              <p className="text-sm text-blue-200">AI-Powered Financial Advisory</p>
            </div>
          </div>
          <nav className="flex gap-1">
            <button
              onClick={() => setActiveTab('chat')}
              className={`px-4 py-2 rounded-lg transition-colors ${
                activeTab === 'chat'
                  ? 'bg-bank-accent text-white'
                  : 'text-blue-200 hover:bg-bank-secondary'
              }`}
            >
              Chat
            </button>
            <button
              onClick={() => setActiveTab('dashboard')}
              className={`px-4 py-2 rounded-lg transition-colors ${
                activeTab === 'dashboard'
                  ? 'bg-bank-accent text-white'
                  : 'text-blue-200 hover:bg-bank-secondary'
              }`}
            >
              Dashboard
            </button>
          </nav>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 py-6">
        {activeTab === 'chat' ? (
          <ChatWindow
            conversationId={conversationId}
            onConversationStart={(id) => setConversationId(id)}
          />
        ) : (
          <Dashboard />
        )}
      </main>
    </div>
  )
}
