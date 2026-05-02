"""AI-powered stock analysis using Claude API (primary) or Ollama/Llama3 (fallback).

Generates natural language investment insights, recommendations, and risk assessments.
"""

import json
import logging
import os
from typing import Any

import httpx

logger = logging.getLogger(__name__)

ANTHROPIC_API_KEY = os.environ.get("ANTHROPIC_API_KEY", "")
OLLAMA_URL = os.environ.get("OLLAMA_URL", "http://localhost:11434")

SYSTEM_PROMPT = """You are a defensive investment research analyst specializing in Indian equities.
You follow Benjamin Graham's value investing principles.
You analyze stocks using fundamental analysis, technical indicators, and valuation metrics.

IMPORTANT RULES:
- This is RESEARCH ONLY, not investment advice
- Always emphasize margin of safety
- Highlight risks clearly
- Be specific with numbers and data points
- Keep analysis concise but thorough
- Focus on capital protection first, then returns
- For Indian stocks, consider INR values and Indian market specifics"""


async def analyze_with_claude(prompt: str, data: dict[str, Any]) -> str | None:
    """Analyze using Claude API."""
    if not ANTHROPIC_API_KEY:
        return None

    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.post(
                "https://api.anthropic.com/v1/messages",
                headers={
                    "x-api-key": ANTHROPIC_API_KEY,
                    "anthropic-version": "2023-06-01",
                    "content-type": "application/json",
                },
                json={
                    "model": "claude-sonnet-4-20250514",
                    "max_tokens": 1024,
                    "system": SYSTEM_PROMPT,
                    "messages": [
                        {
                            "role": "user",
                            "content": f"{prompt}\n\nStock Data:\n```json\n{json.dumps(data, indent=2)}\n```",
                        }
                    ],
                },
            )
            if response.status_code == 200:
                result = response.json()
                return result["content"][0]["text"]
            else:
                logger.warning(f"Claude API error {response.status_code}: {response.text}")
                return None
    except Exception as e:
        logger.warning(f"Claude API failed: {e}")
        return None


async def analyze_with_ollama(prompt: str, data: dict[str, Any]) -> str | None:
    """Analyze using local Ollama (Llama3 or other models)."""
    try:
        async with httpx.AsyncClient(timeout=60.0) as client:
            response = await client.post(
                f"{OLLAMA_URL}/api/generate",
                json={
                    "model": "llama3",
                    "prompt": f"{SYSTEM_PROMPT}\n\n{prompt}\n\nStock Data:\n{json.dumps(data, indent=2)}",
                    "stream": False,
                },
            )
            if response.status_code == 200:
                result = response.json()
                return result.get("response", "")
            return None
    except Exception as e:
        logger.debug(f"Ollama not available: {e}")
        return None


async def analyze_stock(stock_data: dict[str, Any], analysis_type: str = "fundamental") -> dict[str, Any]:
    """Run AI analysis on a stock. Tries Claude first, then Ollama, then rule-based fallback."""

    prompts = {
        "fundamental": (
            f"Analyze {stock_data.get('symbol', 'this stock')} from a defensive investor perspective.\n"
            "Evaluate:\n"
            "1. Business quality and economic moat\n"
            "2. Financial strength (debt, returns, cash flows)\n"
            "3. Valuation (PE, Graham Number, margin of safety)\n"
            "4. Promoter quality and dividend history\n"
            "5. Overall verdict: Is this a quality defensive pick? Why or why not?\n"
            "6. Key risks to watch\n"
            "Keep response under 300 words. Use bullet points."
        ),
        "technical": (
            f"Analyze {stock_data.get('symbol', 'this stock')} technically for swing trading.\n"
            "Evaluate:\n"
            "1. Trend (MA alignment, price vs 50/200 MA)\n"
            "2. Momentum (RSI, MACD)\n"
            "3. Volume confirmation\n"
            "4. Bollinger Band setup\n"
            "5. Entry/exit levels with stop-loss and targets\n"
            "6. Risk-reward assessment\n"
            "Keep response under 250 words. Be specific with price levels."
        ),
        "etf": (
            f"Analyze {stock_data.get('symbol', 'this ETF')} for long-term investment.\n"
            "Evaluate:\n"
            "1. Trend (weekly/monthly MA position)\n"
            "2. Cost efficiency (expense ratio, tracking error)\n"
            "3. Liquidity and AUM\n"
            "4. Historical performance vs category\n"
            "5. Is this a good time to invest? SIP or lump sum?\n"
            "Keep response under 200 words."
        ),
    }

    prompt = prompts.get(analysis_type, prompts["fundamental"])

    # Try Claude first
    result = await analyze_with_claude(prompt, stock_data)
    if result:
        return {"analysis": result, "model": "claude", "status": "success"}

    # Try Ollama
    result = await analyze_with_ollama(prompt, stock_data)
    if result:
        return {"analysis": result, "model": "ollama/llama3", "status": "success"}

    # Rule-based fallback
    return generate_rule_based_analysis(stock_data, analysis_type)


def generate_rule_based_analysis(data: dict[str, Any], analysis_type: str) -> dict[str, Any]:
    """Generate rule-based analysis when AI is unavailable."""
    symbol = data.get("symbol", "Stock")

    if analysis_type == "fundamental":
        lines = [f"**{symbol} — Fundamental Analysis**\n"]

        score = data.get("total_score", 0)
        verdict = data.get("verdict", "")
        if score >= 10:
            lines.append(f"**Verdict: {verdict}** — Score {score}/12, strong defensive candidate.")
        elif score >= 9:
            lines.append(f"**Verdict: {verdict}** — Score {score}/12, quality pick worth monitoring.")
        else:
            lines.append(f"**Verdict: {verdict}** — Score {score}/12, needs improvement in key areas.")

        pe = data.get("pe_ratio", 0)
        roe = data.get("roe", 0)
        de = data.get("debt_to_equity", 0)
        mos = data.get("margin_of_safety_pct", 0)

        strengths, weaknesses = [], []
        if roe >= 15:
            strengths.append(f"ROE {roe:.1f}% — above 15% threshold")
        else:
            weaknesses.append(f"ROE {roe:.1f}% — below 15% threshold")
        if de < 0.5:
            strengths.append(f"D/E {de:.2f} — conservative leverage")
        else:
            weaknesses.append(f"D/E {de:.2f} — higher than ideal 0.5")
        if 0 < pe < 25:
            strengths.append(f"PE {pe:.1f} — reasonable valuation")
        elif pe >= 25:
            weaknesses.append(f"PE {pe:.1f} — premium valuation")
        if mos > 0:
            strengths.append(f"Margin of Safety {mos:.0f}% — trading below Graham Number")
        else:
            weaknesses.append(f"Margin of Safety {mos:.0f}% — trading above Graham Number")

        if strengths:
            lines.append("\n**Strengths:**")
            for s in strengths:
                lines.append(f"• {s}")
        if weaknesses:
            lines.append("\n**Risks:**")
            for w in weaknesses:
                lines.append(f"• {w}")

        lines.append("\n*Research tool only — not investment advice.*")

    elif analysis_type == "technical":
        lines = [f"**{symbol} — Technical Analysis**\n"]
        signal = data.get("signal", "N/A")
        tscore = data.get("total_score", 0)
        lines.append(f"**Signal: {signal}** — Confirmation score {tscore}/10")

        rsi = data.get("rsi_14", 50)
        if rsi > 60:
            lines.append(f"• RSI {rsi:.1f} — bullish momentum")
        elif rsi < 40:
            lines.append(f"• RSI {rsi:.1f} — bearish/oversold")
        else:
            lines.append(f"• RSI {rsi:.1f} — neutral zone")

        if data.get("breakout"):
            lines.append("• Breakout above resistance confirmed")
        vol = data.get("volume_ratio", 1)
        if vol >= 1.5:
            lines.append(f"• Volume surge {vol:.1f}x average — institutional interest")

        entry = data.get("entry_price", 0)
        sl = data.get("stop_loss", 0)
        t1 = data.get("target_1", 0)
        t2 = data.get("target_2", 0)
        if entry > 0:
            lines.append(f"\n**Trade Setup:** Entry ₹{entry:.0f} | SL ₹{sl:.0f} | T1 ₹{t1:.0f} | T2 ₹{t2:.0f}")

    else:  # etf
        lines = [f"**{symbol} — ETF Analysis**\n"]
        tscore = data.get("total_score", 0)
        verdict = data.get("verdict", "")
        lines.append(f"**Verdict: {verdict}** — Score {tscore}/10")

        ret_1y = data.get("return_1y", 0)
        ret_3y = data.get("return_3y_cagr", 0)
        expense = data.get("expense_ratio", 0)
        lines.append(f"• 1Y Return: {ret_1y:.1f}% | 3Y CAGR: {ret_3y:.1f}%")
        lines.append(f"• Expense Ratio: {expense:.2f}%")

        if data.get("above_20w_ma") and data.get("above_50w_ma"):
            lines.append("• Above both 20W and 50W moving averages — uptrend intact")

    return {
        "analysis": "\n".join(lines),
        "model": "rule-based",
        "status": "success",
    }


async def get_ai_recommendation(stocks: list[dict], analysis_type: str = "fundamental") -> str | None:
    """Get AI recommendation for top picks from a list of stocks."""
    if not stocks:
        return None

    prompt = (
        f"From these {len(stocks)} Indian stocks, identify the top 3 best picks for a defensive investor.\n"
        "For each pick, explain in 2-3 sentences why it's attractive right now.\n"
        "Consider: valuation, quality, safety margin, and timing.\n"
        "End with one key risk for the overall market.\n"
        "Keep response under 300 words."
    )

    summary_data = [
        {k: v for k, v in s.items() if k in [
            "symbol", "name", "sector", "total_score", "verdict",
            "pe_ratio", "roe", "margin_of_safety_pct", "current_price",
            "signal", "rsi_14", "return_1y", "expense_ratio",
        ]}
        for s in stocks[:15]
    ]

    result = await analyze_with_claude(prompt, {"stocks": summary_data})
    if result:
        return result

    result = await analyze_with_ollama(prompt, {"stocks": summary_data})
    if result:
        return result

    return None
