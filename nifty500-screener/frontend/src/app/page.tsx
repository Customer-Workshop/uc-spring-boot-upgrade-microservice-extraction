"use client";

import { useRouter } from "next/navigation";
import Dashboard from "@/components/Dashboard";

export default function HomePage() {
  const router = useRouter();

  return (
    <Dashboard
      onStockClick={(symbol) => {
        router.push(`/stock/${symbol}`);
      }}
    />
  );
}
