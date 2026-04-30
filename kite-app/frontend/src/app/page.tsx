"use client";

import { useState } from "react";
import Header from "@/components/Header";
import Sidebar from "@/components/Sidebar";
import DashboardView from "@/components/DashboardView";
import OrdersView from "@/components/OrdersView";
import HoldingsView from "@/components/HoldingsView";
import PositionsView from "@/components/PositionsView";
import FundsView from "@/components/FundsView";
import ChartView from "@/components/ChartView";
import OrderDialog from "@/components/OrderDialog";

export type ActiveTab = "dashboard" | "orders" | "holdings" | "positions" | "funds" | "chart";

export default function Home() {
  const [activeTab, setActiveTab] = useState<ActiveTab>("dashboard");
  const [chartSymbol, setChartSymbol] = useState<string>("RELIANCE");
  const [orderDialog, setOrderDialog] = useState<{
    open: boolean;
    symbol: string;
    type: "BUY" | "SELL";
    price: number;
  }>({ open: false, symbol: "", type: "BUY", price: 0 });

  const openChart = (symbol: string) => {
    setChartSymbol(symbol);
    setActiveTab("chart");
  };

  const openOrder = (symbol: string, type: "BUY" | "SELL", price: number) => {
    setOrderDialog({ open: true, symbol, type, price });
  };

  const closeOrder = () => {
    setOrderDialog({ open: false, symbol: "", type: "BUY", price: 0 });
  };

  return (
    <div className="h-screen flex flex-col">
      <Header activeTab={activeTab} setActiveTab={setActiveTab} />
      <div className="flex flex-1 overflow-hidden">
        <Sidebar openChart={openChart} openOrder={openOrder} />
        <main className="flex-1 overflow-y-auto p-6">
          {activeTab === "dashboard" && <DashboardView openChart={openChart} />}
          {activeTab === "orders" && <OrdersView />}
          {activeTab === "holdings" && <HoldingsView openChart={openChart} openOrder={openOrder} />}
          {activeTab === "positions" && <PositionsView openChart={openChart} openOrder={openOrder} />}
          {activeTab === "funds" && <FundsView />}
          {activeTab === "chart" && <ChartView symbol={chartSymbol} openOrder={openOrder} />}
        </main>
      </div>
      {orderDialog.open && (
        <OrderDialog
          symbol={orderDialog.symbol}
          transactionType={orderDialog.type}
          initialPrice={orderDialog.price}
          onClose={closeOrder}
        />
      )}
    </div>
  );
}
