'use client';
import { useState, useRef, useEffect } from 'react';
import { api, connectWebSocket } from '@/lib/api';

interface Message {
  messageId: string;
  senderType: 'CUSTOMER' | 'AI' | 'HUMAN_RM';
  content: string;
  phase: string;
  createdAt: string;
}

const PHASES = ['GREETING', 'PERSONAL', 'FINANCIAL', 'GOALS', 'RISK_ASSESSMENT', 'RECOMMENDATION', 'CHANNEL_PREF', 'FOLLOWUP_SCHEDULE', 'COMPLETED'];

export default function ChatWindow() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [conversationId, setConversationId] = useState<string | null>(null);
  const [currentPhase, setCurrentPhase] = useState('GREETING');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const wsRef = useRef<WebSocket | null>(null);

  const scrollToBottom = () => messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  useEffect(scrollToBottom, [messages]);

  const startConversation = async () => {
    setLoading(true);
    try {
      const response = await api.post<{ conversationId: string; currentPhase: string }>('/conversations', { channel: 'WEB' });
      setConversationId(response.conversationId);
      setCurrentPhase(response.currentPhase);
      const detail = await api.get<{ messages: Message[] }>(`/conversations/${response.conversationId}`);
      setMessages(detail.messages || []);
      wsRef.current = connectWebSocket(response.conversationId, (data: any) => {
        if (data.aiResponse) {
          setMessages(prev => [...prev, data.aiResponse]);
          setCurrentPhase(data.aiResponse.phase);
        }
      });
    } catch (error) { console.error('Failed to start conversation:', error); }
    setLoading(false);
  };

  const sendMessage = async () => {
    if (!input.trim() || !conversationId) return;
    const userMsg: Message = { messageId: Date.now().toString(), senderType: 'CUSTOMER', content: input, phase: currentPhase, createdAt: new Date().toISOString() };
    setMessages(prev => [...prev, userMsg]);
    setInput('');
    setLoading(true);
    try {
      if (wsRef.current?.readyState === WebSocket.OPEN) {
        wsRef.current.send(input);
      } else {
        const response = await api.post<{ aiResponse: Message }>(`/conversations/${conversationId}/messages`, { content: input });
        if (response.aiResponse) {
          setMessages(prev => [...prev, response.aiResponse]);
          setCurrentPhase(response.aiResponse.phase);
        }
      }
    } catch (error) { console.error('Failed to send message:', error); }
    setLoading(false);
  };

  const phaseIndex = PHASES.indexOf(currentPhase);
  const progress = ((phaseIndex + 1) / PHASES.length) * 100;

  return (
    <div style={{ maxWidth: 600, margin: '0 auto', fontFamily: 'system-ui, sans-serif' }}>
      <div style={{ background: '#1a73e8', color: 'white', padding: '16px', borderRadius: '8px 8px 0 0' }}>
        <h2 style={{ margin: 0 }}>Banking Relationship Manager</h2>
        <div style={{ marginTop: 8, background: 'rgba(255,255,255,0.3)', borderRadius: 4, height: 6 }}>
          <div style={{ background: 'white', height: '100%', borderRadius: 4, width: `${progress}%`, transition: 'width 0.3s' }} />
        </div>
        <div style={{ fontSize: 12, marginTop: 4 }}>{currentPhase.replace('_', ' ')} ({phaseIndex + 1}/{PHASES.length})</div>
      </div>

      <div style={{ height: 400, overflowY: 'auto', padding: 16, background: '#f5f5f5', border: '1px solid #ddd' }}>
        {!conversationId ? (
          <div style={{ textAlign: 'center', paddingTop: 120 }}>
            <button onClick={startConversation} disabled={loading}
              style={{ padding: '12px 24px', fontSize: 16, background: '#1a73e8', color: 'white', border: 'none', borderRadius: 8, cursor: 'pointer' }}>
              {loading ? 'Starting...' : 'Start Conversation'}
            </button>
          </div>
        ) : (
          messages.map((msg) => (
            <div key={msg.messageId} style={{ marginBottom: 12, textAlign: msg.senderType === 'CUSTOMER' ? 'right' : 'left' }}>
              <div style={{
                display: 'inline-block', maxWidth: '80%', padding: '10px 14px', borderRadius: 12,
                background: msg.senderType === 'CUSTOMER' ? '#1a73e8' : 'white',
                color: msg.senderType === 'CUSTOMER' ? 'white' : '#333',
                boxShadow: '0 1px 2px rgba(0,0,0,0.1)',
              }}>
                {msg.content}
              </div>
            </div>
          ))
        )}
        <div ref={messagesEndRef} />
      </div>

      {conversationId && (
        <div style={{ display: 'flex', gap: 8, padding: 12, background: 'white', border: '1px solid #ddd', borderTop: 'none', borderRadius: '0 0 8px 8px' }}>
          <input value={input} onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && sendMessage()}
            placeholder="Type your message..." disabled={loading || currentPhase === 'COMPLETED'}
            style={{ flex: 1, padding: '10px 14px', border: '1px solid #ddd', borderRadius: 8, fontSize: 14 }} />
          <button onClick={sendMessage} disabled={loading || !input.trim() || currentPhase === 'COMPLETED'}
            style={{ padding: '10px 20px', background: '#1a73e8', color: 'white', border: 'none', borderRadius: 8, cursor: 'pointer', fontSize: 14 }}>
            {loading ? '...' : 'Send'}
          </button>
        </div>
      )}
    </div>
  );
}
