const API_BASE = process.env.NEXT_PUBLIC_API_URL || '/api/v1';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const token = typeof window !== 'undefined' ? localStorage.getItem('rm_token') : null;
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const response = await fetch(`${API_BASE}${path}`, { ...options, headers: { ...headers, ...options?.headers } });
  if (!response.ok) throw new Error(`API error: ${response.status}`);
  const data = await response.json();
  return data.data ?? data;
}

export const api = {
  post: <T>(path: string, body: unknown) => request<T>(path, { method: 'POST', body: JSON.stringify(body) }),
  get: <T>(path: string) => request<T>(path),
};

export function connectWebSocket(conversationId: string, onMessage: (data: unknown) => void) {
  const wsUrl = `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws/chat/${conversationId}`;
  const ws = new WebSocket(wsUrl);
  ws.onmessage = (event) => onMessage(JSON.parse(event.data));
  ws.onerror = (error) => console.error('WebSocket error:', error);
  return ws;
}
