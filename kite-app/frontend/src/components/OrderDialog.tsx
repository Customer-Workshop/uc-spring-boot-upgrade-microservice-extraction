"use client";

import { useState } from "react";
import { api } from "@/lib/api";
import type { OrderFormData } from "@/lib/types";

interface Props {
  symbol: string;
  transactionType: "BUY" | "SELL";
  initialPrice: number;
  onClose: () => void;
}

export default function OrderDialog({ symbol, transactionType, initialPrice, onClose }: Props) {
  const [product, setProduct] = useState<"MIS" | "CNC" | "NRML">("CNC");
  const [orderType, setOrderType] = useState<"MARKET" | "LIMIT" | "SL" | "SL-M">("MARKET");
  const [quantity, setQuantity] = useState(1);
  const [price, setPrice] = useState(initialPrice);
  const [triggerPrice, setTriggerPrice] = useState(0);
  const [disclosedQty, setDisclosedQty] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const isBuy = transactionType === "BUY";
  const headerColor = isBuy ? "bg-kite-blue" : "bg-kite-orange";

  const handleSubmit = async () => {
    setLoading(true);
    setError("");
    try {
      const data: OrderFormData = {
        symbol,
        exchange: "NSE",
        transaction_type: transactionType,
        order_type: orderType,
        product,
        quantity,
        price: orderType === "MARKET" ? 0 : price,
        trigger_price: triggerPrice,
        disclosed_qty: disclosedQty,
      };
      await api.placeOrder(data);
      onClose();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Order failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/30 flex items-center justify-center z-50" onClick={onClose}>
      <div className="bg-white rounded-sm shadow-xl w-[480px]" onClick={(e) => e.stopPropagation()}>
        {/* Header */}
        <div className={`${headerColor} px-5 py-3 rounded-t-sm flex items-center justify-between`}>
          <div className="text-white">
            <span className="font-semibold text-sm">{transactionType}</span>
            <span className="ml-2 text-sm">{symbol}</span>
          </div>
          <div className="text-white text-xs">
            ₹{initialPrice.toLocaleString("en-IN", { maximumFractionDigits: 2 })}
          </div>
        </div>

        {/* Body */}
        <div className="p-5">
          {/* Product type */}
          <div className="flex gap-4 mb-5">
            {(["MIS", "CNC", "NRML"] as const).map((p) => (
              <label key={p} className="flex items-center gap-1.5 cursor-pointer text-xs">
                <input
                  type="radio"
                  name="product"
                  checked={product === p}
                  onChange={() => setProduct(p)}
                  className="accent-kite-blue"
                />
                {p}
              </label>
            ))}
            <div className="flex-1" />
            {(["MARKET", "LIMIT", "SL", "SL-M"] as const).map((ot) => (
              <label key={ot} className="flex items-center gap-1.5 cursor-pointer text-xs">
                <input
                  type="radio"
                  name="orderType"
                  checked={orderType === ot}
                  onChange={() => setOrderType(ot)}
                  className="accent-kite-blue"
                />
                {ot}
              </label>
            ))}
          </div>

          {/* Inputs */}
          <div className="grid grid-cols-4 gap-4 mb-5">
            <div>
              <label className="text-[10px] text-kite-text-light block mb-1">Qty.</label>
              <input
                type="number"
                value={quantity}
                onChange={(e) => setQuantity(Number(e.target.value))}
                className="w-full border border-kite-border rounded px-2 py-1.5 text-xs text-kite-text"
                min={1}
              />
            </div>
            <div>
              <label className="text-[10px] text-kite-text-light block mb-1">Price</label>
              <input
                type="number"
                value={orderType === "MARKET" ? 0 : price}
                onChange={(e) => setPrice(Number(e.target.value))}
                disabled={orderType === "MARKET"}
                className="w-full border border-kite-border rounded px-2 py-1.5 text-xs text-kite-text disabled:bg-gray-50 disabled:text-kite-text-light"
              />
            </div>
            <div>
              <label className="text-[10px] text-kite-text-light block mb-1">Trigger price</label>
              <input
                type="number"
                value={triggerPrice}
                onChange={(e) => setTriggerPrice(Number(e.target.value))}
                disabled={orderType !== "SL" && orderType !== "SL-M"}
                className="w-full border border-kite-border rounded px-2 py-1.5 text-xs text-kite-text disabled:bg-gray-50 disabled:text-kite-text-light"
              />
            </div>
            <div>
              <label className="text-[10px] text-kite-text-light block mb-1">Disclosed qty.</label>
              <input
                type="number"
                value={disclosedQty}
                onChange={(e) => setDisclosedQty(Number(e.target.value))}
                className="w-full border border-kite-border rounded px-2 py-1.5 text-xs text-kite-text"
              />
            </div>
          </div>

          {error && <div className="text-xs text-kite-red mb-3">{error}</div>}

          {/* Actions */}
          <div className="flex items-center justify-between">
            <button className="text-xs text-kite-blue hover:underline">More options</button>
            <div className="flex gap-3">
              <button
                onClick={handleSubmit}
                disabled={loading}
                className={`${isBuy ? "kite-btn-blue" : "kite-btn-orange"} ${loading ? "opacity-50" : ""}`}
              >
                {loading ? "Placing..." : transactionType === "BUY" ? "Buy" : "Sell"}
              </button>
              <button onClick={onClose} className="px-5 py-2 border border-kite-border rounded text-xs text-kite-text hover:bg-kite-hover">
                Cancel
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
