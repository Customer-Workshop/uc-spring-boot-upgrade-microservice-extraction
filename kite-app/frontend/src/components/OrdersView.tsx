"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { Order } from "@/lib/types";

export default function OrdersView() {
  const [orders, setOrders] = useState<Order[]>([]);

  useEffect(() => {
    api.getOrders().then(setOrders).catch(() => {});
  }, []);

  return (
    <div className="max-w-5xl">
      <h2 className="text-lg font-light text-kite-text mb-1">Orders</h2>
      <p className="text-xs text-kite-text-light mb-6">View all your orders for the day</p>

      {orders.length === 0 ? (
        <div className="text-center py-20 text-kite-text-light">
          <svg className="w-12 h-12 mx-auto mb-3 text-kite-border" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
          <p className="text-sm">No orders placed today</p>
          <p className="text-xs mt-1">Place an order from the watchlist or chart</p>
        </div>
      ) : (
        <table className="w-full">
          <thead>
            <tr className="text-xs text-kite-text-light border-b border-kite-border">
              <th className="text-left py-2 font-normal">Time</th>
              <th className="text-left py-2 font-normal">Type</th>
              <th className="text-left py-2 font-normal">Instrument</th>
              <th className="text-left py-2 font-normal">Product</th>
              <th className="text-right py-2 font-normal">Qty</th>
              <th className="text-right py-2 font-normal">Price</th>
              <th className="text-left py-2 font-normal">Status</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((o) => (
              <tr key={o.id} className="border-b border-kite-border hover:bg-kite-hover">
                <td className="py-2 text-xs text-kite-text-light">
                  {o.order_timestamp ? new Date(o.order_timestamp).toLocaleTimeString("en-IN", { hour: "2-digit", minute: "2-digit" }) : "—"}
                </td>
                <td className="py-2">
                  <span className={`text-xs font-semibold ${o.transaction_type === "BUY" ? "text-kite-blue" : "text-kite-orange"}`}>
                    {o.transaction_type}
                  </span>
                </td>
                <td className="py-2 text-xs font-semibold text-kite-text">
                  {o.symbol}
                  <span className="text-kite-text-light font-normal ml-1">{o.exchange}</span>
                </td>
                <td className="py-2 text-xs text-kite-text-light">{o.product}</td>
                <td className="py-2 text-xs text-kite-text text-right">
                  {o.filled_qty} / {o.quantity}
                </td>
                <td className="py-2 text-xs text-kite-text text-right">
                  ₹{o.average_price.toLocaleString("en-IN", { maximumFractionDigits: 2 })}
                </td>
                <td className="py-2">
                  <span className={`text-xs px-2 py-0.5 rounded ${
                    o.status === "COMPLETE" ? "bg-green-100 text-kite-green" :
                    o.status === "REJECTED" ? "bg-red-100 text-kite-red" :
                    "bg-blue-100 text-kite-blue"
                  }`}>
                    {o.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
