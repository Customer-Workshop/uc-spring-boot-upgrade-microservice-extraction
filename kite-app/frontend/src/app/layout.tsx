import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Kite — Zerodha Trading Platform",
  description: "Zerodha Kite-style trading platform",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className="bg-kite-bg">{children}</body>
    </html>
  );
}
