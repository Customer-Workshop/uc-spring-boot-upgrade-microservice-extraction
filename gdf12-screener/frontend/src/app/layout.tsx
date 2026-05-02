import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'GDF-12 Defensive Stock Screener',
  description: 'Find fundamentally strong Indian stocks at the right price using the 12-signal defensive investor framework',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  )
}
