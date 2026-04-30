import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "NIFTY 500 Stock Screener — Top 20 Picks",
  description:
    "Research-only stock screener ranking NIFTY 500 stocks using fundamentals, valuation, technicals, and pattern recognition.",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className="antialiased">
        <header className="bg-white border-b border-gray-200 sticky top-0 z-50">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex items-center justify-between h-16">
              <div className="flex items-center gap-2">
                <span className="text-2xl font-bold text-blue-700">NIFTY500</span>
                <span className="text-sm text-gray-500">Stock Screener</span>
              </div>
              <nav className="flex gap-6">
                <a href="/" className="text-gray-600 hover:text-blue-600 font-medium text-sm">
                  Dashboard
                </a>
                <a
                  href="/backtest"
                  className="text-gray-600 hover:text-blue-600 font-medium text-sm"
                >
                  Backtest
                </a>
              </nav>
            </div>
          </div>
        </header>

        <div className="bg-yellow-50 border-b border-yellow-200 px-4 py-2 text-center text-sm text-yellow-800">
          Research tool only — NOT investment advice. All data is for educational purposes.
        </div>

        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">{children}</main>

        <footer className="bg-white border-t border-gray-200 mt-12 py-6">
          <div className="max-w-7xl mx-auto px-4 text-center text-sm text-gray-500">
            NIFTY 500 Stock Screener v1.0 — Research purposes only.
          </div>
        </footer>
      </body>
    </html>
  );
}
