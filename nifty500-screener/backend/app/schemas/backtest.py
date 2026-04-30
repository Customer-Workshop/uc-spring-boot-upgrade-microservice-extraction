from datetime import date

from pydantic import BaseModel


class BacktestRequest(BaseModel):
    strategy: str = "breakout"  # breakout, pullback, rsi_mean_reversion, trend_following
    start_date: date | None = None
    end_date: date | None = None
    initial_capital: float = 1_000_000.0
    position_size_pct: float = 5.0
    stop_loss_pct: float = 5.0
    take_profit_pct: float = 15.0
    slippage_pct: float = 0.1
    commission_pct: float = 0.05
    symbols: list[str] | None = None


class TradeRecord(BaseModel):
    symbol: str
    entry_date: date
    exit_date: date | None = None
    entry_price: float
    exit_price: float | None = None
    shares: float
    pnl: float = 0.0
    pnl_pct: float = 0.0
    r_multiple: float = 0.0
    exit_reason: str = ""


class BacktestMetrics(BaseModel):
    cagr: float
    sharpe_ratio: float
    max_drawdown: float
    win_rate: float
    avg_r_multiple: float
    total_trades: int
    winning_trades: int
    losing_trades: int
    avg_holding_days: float
    exposure_pct: float
    turnover: float
    profit_factor: float
    expectancy: float


class EquityPoint(BaseModel):
    date: date
    equity: float
    drawdown: float
    benchmark_equity: float | None = None


class BacktestResponse(BaseModel):
    strategy: str
    start_date: date
    end_date: date
    initial_capital: float
    final_equity: float
    metrics: BacktestMetrics
    equity_curve: list[EquityPoint]
    trades: list[TradeRecord]
