import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'Banking Relationship Manager',
  description: 'AI-powered financial advisory platform',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className="min-h-screen bg-gray-50">{children}</body>
    </html>
  )
}
