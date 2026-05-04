export const metadata = { title: 'RM - Banking Relationship Manager', description: 'AI-powered banking relationship management platform' };

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body style={{ margin: 0, padding: 0, background: '#f0f2f5' }}>{children}</body>
    </html>
  );
}
