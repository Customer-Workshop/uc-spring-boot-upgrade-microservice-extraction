"""Backtesting engine with walk-forward evaluation and slippage modeling."""

from dataclasses import dataclass, field
from datetime import date, timedelta

import numpy as np
import pandas as pd

from app.schemas.backtest import (
    BacktestMetrics,
    BacktestRequest,
    BacktestResponse,
    EquityPoint,
    TradeRecord,
)
from app.services.indicators import compute_rsi, compute_sma, compute_all_technicals


@dataclass
class Position:
    symbol: str
    entry_date: date
    entry_price: float
    shares: float
    stop_loss: float
    take_profit: float


def apply_slippage(price: float, slippage_pct: float, is_buy: bool) -> float:
    factor = 1 + slippage_pct / 100 if is_buy else 1 - slippage_pct / 100
    return price * factor


def apply_commission(value: float, commission_pct: float) -> float:
    return value * commission_pct / 100


def _breakout_signals(df: pd.DataFrame) -> pd.DataFrame:
    lookback = 20
    rolling_high = df["high"].rolling(window=lookback).max()
    avg_vol = df["volume"].rolling(window=lookback).mean()
    entry = (df["close"] > rolling_high.shift(1)) & (df["volume"] > avg_vol * 1.5)
    exit_signal = df["close"] < df["close"].rolling(window=10).mean()
    signals = pd.DataFrame({"entry": entry, "exit": exit_signal}, index=df.index)
    return signals


def _pullback_signals(df: pd.DataFrame) -> pd.DataFrame:
    tech = compute_all_technicals(df)
    entry = (
        (tech["close"] > tech["sma_200"])
        & (tech["close"] <= tech["sma_20"] * 1.02)
        & (tech["close"] >= tech["sma_20"] * 0.98)
        & (tech["rsi_14"] < 45)
    )
    exit_signal = tech["close"] > tech["sma_20"] * 1.05
    return pd.DataFrame({"entry": entry, "exit": exit_signal}, index=df.index)


def _rsi_mean_reversion_signals(df: pd.DataFrame) -> pd.DataFrame:
    rsi = compute_rsi(df["close"], 14)
    entry = rsi < 30
    exit_signal = rsi > 60
    return pd.DataFrame({"entry": entry, "exit": exit_signal}, index=df.index)


def _trend_following_signals(df: pd.DataFrame) -> pd.DataFrame:
    sma50 = compute_sma(df["close"], 50)
    sma200 = compute_sma(df["close"], 200)
    entry = (sma50 > sma200) & (sma50.shift(1) <= sma200.shift(1))
    exit_signal = (sma50 < sma200) & (sma50.shift(1) >= sma200.shift(1))
    return pd.DataFrame({"entry": entry, "exit": exit_signal}, index=df.index)


STRATEGY_MAP = {
    "breakout": _breakout_signals,
    "pullback": _pullback_signals,
    "rsi_mean_reversion": _rsi_mean_reversion_signals,
    "trend_following": _trend_following_signals,
}


def run_backtest(
    ohlcv_data: dict[str, pd.DataFrame],
    request: BacktestRequest,
    benchmark_data: pd.DataFrame | None = None,
) -> BacktestResponse:
    strategy_fn = STRATEGY_MAP.get(request.strategy, _breakout_signals)

    capital = request.initial_capital
    equity = capital
    positions: dict[str, Position] = {}
    trades: list[TradeRecord] = []
    equity_curve: list[EquityPoint] = []

    all_dates: set[date] = set()
    for sym, df in ohlcv_data.items():
        all_dates.update(df["date"].tolist())

    sorted_dates = sorted(all_dates)
    if request.start_date:
        sorted_dates = [d for d in sorted_dates if d >= request.start_date]
    if request.end_date:
        sorted_dates = [d for d in sorted_dates if d <= request.end_date]

    if not sorted_dates:
        return BacktestResponse(
            strategy=request.strategy,
            start_date=request.start_date or date.today(),
            end_date=request.end_date or date.today(),
            initial_capital=request.initial_capital,
            final_equity=request.initial_capital,
            metrics=_empty_metrics(),
            equity_curve=[],
            trades=[],
        )

    signals_cache: dict[str, pd.DataFrame] = {}
    for sym, df in ohlcv_data.items():
        try:
            signals_cache[sym] = strategy_fn(df)
        except Exception:
            continue

    peak_equity = capital
    benchmark_start = None

    for current_date in sorted_dates:
        for sym in list(positions.keys()):
            pos = positions[sym]
            df = ohlcv_data.get(sym)
            if df is None:
                continue
            row = df[df["date"] == current_date]
            if row.empty:
                continue

            price = float(row["close"].iloc[0])
            high = float(row["high"].iloc[0])
            low = float(row["low"].iloc[0])

            exit_reason = ""
            exit_price = None

            if low <= pos.stop_loss:
                exit_price = apply_slippage(pos.stop_loss, request.slippage_pct, False)
                exit_reason = "stop_loss"
            elif high >= pos.take_profit:
                exit_price = apply_slippage(pos.take_profit, request.slippage_pct, False)
                exit_reason = "take_profit"
            elif sym in signals_cache:
                sig_row = signals_cache[sym]
                if current_date in sig_row.index and sig_row.loc[current_date].get("exit", False):
                    exit_price = apply_slippage(price, request.slippage_pct, False)
                    exit_reason = "signal_exit"

            if exit_price is not None:
                pnl = (exit_price - pos.entry_price) * pos.shares
                commission = apply_commission(exit_price * pos.shares, request.commission_pct)
                pnl -= commission
                capital += exit_price * pos.shares - commission
                risk_per_share = pos.entry_price - pos.stop_loss
                r_mult = (exit_price - pos.entry_price) / risk_per_share if risk_per_share > 0 else 0

                trades.append(TradeRecord(
                    symbol=sym,
                    entry_date=pos.entry_date,
                    exit_date=current_date,
                    entry_price=pos.entry_price,
                    exit_price=exit_price,
                    shares=pos.shares,
                    pnl=round(pnl, 2),
                    pnl_pct=round(pnl / (pos.entry_price * pos.shares) * 100, 2),
                    r_multiple=round(r_mult, 2),
                    exit_reason=exit_reason,
                ))
                del positions[sym]

        symbols_to_check = request.symbols or list(ohlcv_data.keys())
        for sym in symbols_to_check:
            if sym in positions:
                continue
            df = ohlcv_data.get(sym)
            if df is None or sym not in signals_cache:
                continue
            row = df[df["date"] == current_date]
            if row.empty:
                continue

            sig = signals_cache[sym]
            if current_date not in sig.index:
                continue
            if not sig.loc[current_date].get("entry", False):
                continue

            price = float(row["close"].iloc[0])
            position_value = equity * request.position_size_pct / 100
            if position_value > capital:
                continue

            entry_price = apply_slippage(price, request.slippage_pct, True)
            shares = position_value / entry_price
            commission = apply_commission(position_value, request.commission_pct)
            capital -= position_value + commission

            positions[sym] = Position(
                symbol=sym,
                entry_date=current_date,
                entry_price=entry_price,
                shares=shares,
                stop_loss=entry_price * (1 - request.stop_loss_pct / 100),
                take_profit=entry_price * (1 + request.take_profit_pct / 100),
            )

        portfolio_value = capital
        for sym, pos in positions.items():
            df = ohlcv_data.get(sym)
            if df is None:
                portfolio_value += pos.entry_price * pos.shares
                continue
            row = df[df["date"] == current_date]
            if row.empty:
                portfolio_value += pos.entry_price * pos.shares
            else:
                portfolio_value += float(row["close"].iloc[0]) * pos.shares

        equity = portfolio_value
        peak_equity = max(peak_equity, equity)
        drawdown = (peak_equity - equity) / peak_equity * 100

        bench_eq = None
        if benchmark_data is not None and benchmark_start is None:
            bench_row = benchmark_data[benchmark_data["date"] == current_date]
            if not bench_row.empty:
                benchmark_start = float(bench_row["close"].iloc[0])
        if benchmark_data is not None and benchmark_start is not None:
            bench_row = benchmark_data[benchmark_data["date"] == current_date]
            if not bench_row.empty:
                bench_eq = request.initial_capital * float(bench_row["close"].iloc[0]) / benchmark_start

        equity_curve.append(EquityPoint(
            date=current_date,
            equity=round(equity, 2),
            drawdown=round(drawdown, 2),
            benchmark_equity=round(bench_eq, 2) if bench_eq else None,
        ))

    metrics = _compute_metrics(trades, equity_curve, request.initial_capital, equity, sorted_dates)

    return BacktestResponse(
        strategy=request.strategy,
        start_date=sorted_dates[0],
        end_date=sorted_dates[-1],
        initial_capital=request.initial_capital,
        final_equity=round(equity, 2),
        metrics=metrics,
        equity_curve=equity_curve,
        trades=trades,
    )


def _compute_metrics(
    trades: list[TradeRecord],
    equity_curve: list[EquityPoint],
    initial_capital: float,
    final_equity: float,
    dates: list[date],
) -> BacktestMetrics:
    if not trades or not dates:
        return _empty_metrics()

    years = max((dates[-1] - dates[0]).days / 365.25, 0.01)
    cagr = (final_equity / initial_capital) ** (1 / years) - 1

    equities = [ep.equity for ep in equity_curve]
    returns = np.diff(equities) / equities[:-1] if len(equities) > 1 else np.array([0])
    sharpe = float(np.mean(returns) / np.std(returns) * np.sqrt(252)) if np.std(returns) > 0 else 0

    max_dd = max((ep.drawdown for ep in equity_curve), default=0)

    winners = [t for t in trades if t.pnl > 0]
    losers = [t for t in trades if t.pnl <= 0]
    win_rate = len(winners) / len(trades) if trades else 0

    gross_profit = sum(t.pnl for t in winners)
    gross_loss = abs(sum(t.pnl for t in losers))
    profit_factor = gross_profit / gross_loss if gross_loss > 0 else float("inf")

    avg_r = np.mean([t.r_multiple for t in trades]) if trades else 0

    holding_days = []
    for t in trades:
        if t.exit_date:
            holding_days.append((t.exit_date - t.entry_date).days)
    avg_holding = np.mean(holding_days) if holding_days else 0

    total_invested_days = sum(holding_days)
    total_calendar_days = (dates[-1] - dates[0]).days if len(dates) > 1 else 1
    exposure = total_invested_days / total_calendar_days * 100 if total_calendar_days > 0 else 0

    turnover = len(trades) / years if years > 0 else 0

    avg_win = np.mean([t.pnl for t in winners]) if winners else 0
    avg_loss = abs(np.mean([t.pnl for t in losers])) if losers else 1
    expectancy = win_rate * avg_win - (1 - win_rate) * avg_loss

    return BacktestMetrics(
        cagr=round(cagr * 100, 2),
        sharpe_ratio=round(sharpe, 2),
        max_drawdown=round(max_dd, 2),
        win_rate=round(win_rate * 100, 2),
        avg_r_multiple=round(float(avg_r), 2),
        total_trades=len(trades),
        winning_trades=len(winners),
        losing_trades=len(losers),
        avg_holding_days=round(float(avg_holding), 1),
        exposure_pct=round(exposure, 2),
        turnover=round(turnover, 1),
        profit_factor=round(profit_factor, 2),
        expectancy=round(expectancy, 2),
    )


def _empty_metrics() -> BacktestMetrics:
    return BacktestMetrics(
        cagr=0, sharpe_ratio=0, max_drawdown=0, win_rate=0, avg_r_multiple=0,
        total_trades=0, winning_trades=0, losing_trades=0, avg_holding_days=0,
        exposure_pct=0, turnover=0, profit_factor=0, expectancy=0,
    )
