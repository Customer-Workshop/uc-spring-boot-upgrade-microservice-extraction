"""Seed mock data for the Kite trading platform."""

import random
from datetime import date, timedelta

import numpy as np
from passlib.context import CryptContext
from sqlalchemy.orm import Session

from app.models.fund import FundAccount
from app.models.holding import Holding
from app.models.instrument import Instrument
from app.models.ohlcv import OHLCV
from app.models.position import Position
from app.models.user import User
from app.models.watchlist import Watchlist, WatchlistItem

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

STOCKS = [
    ("RELIANCE", "Reliance Industries Ltd", "Energy", 2450.0),
    ("TCS", "Tata Consultancy Services Ltd", "Technology", 3650.0),
    ("HDFCBANK", "HDFC Bank Ltd", "Financial Services", 1580.0),
    ("INFY", "Infosys Ltd", "Technology", 1450.0),
    ("ICICIBANK", "ICICI Bank Ltd", "Financial Services", 1120.0),
    ("HINDUNILVR", "Hindustan Unilever Ltd", "Consumer Staples", 2580.0),
    ("ITC", "ITC Ltd", "Consumer Staples", 440.0),
    ("SBIN", "State Bank of India", "Financial Services", 620.0),
    ("BHARTIARTL", "Bharti Airtel Ltd", "Communication", 1480.0),
    ("KOTAKBANK", "Kotak Mahindra Bank Ltd", "Financial Services", 1780.0),
    ("LT", "Larsen & Toubro Ltd", "Industrials", 3200.0),
    ("AXISBANK", "Axis Bank Ltd", "Financial Services", 1050.0),
    ("BAJFINANCE", "Bajaj Finance Ltd", "Financial Services", 6800.0),
    ("MARUTI", "Maruti Suzuki India Ltd", "Consumer Discretionary", 10500.0),
    ("WIPRO", "Wipro Ltd", "Technology", 480.0),
    ("HCLTECH", "HCL Technologies Ltd", "Technology", 1520.0),
    ("ASIANPAINT", "Asian Paints Ltd", "Materials", 3100.0),
    ("TATAMOTORS", "Tata Motors Ltd", "Consumer Discretionary", 650.0),
    ("SUNPHARMA", "Sun Pharmaceutical Industries Ltd", "Healthcare", 1680.0),
    ("ULTRACEMCO", "UltraTech Cement Ltd", "Materials", 8200.0),
    ("TITAN", "Titan Company Ltd", "Consumer Discretionary", 3300.0),
    ("NESTLEIND", "Nestle India Ltd", "Consumer Staples", 2450.0),
    ("BAJAJFINSV", "Bajaj Finserv Ltd", "Financial Services", 1550.0),
    ("POWERGRID", "Power Grid Corporation of India Ltd", "Utilities", 290.0),
    ("NTPC", "NTPC Ltd", "Utilities", 350.0),
    ("ONGC", "Oil & Natural Gas Corporation Ltd", "Energy", 260.0),
    ("ADANIENT", "Adani Enterprises Ltd", "Industrials", 2900.0),
    ("ADANIPORTS", "Adani Ports and SEZ Ltd", "Industrials", 1150.0),
    ("TATASTEEL", "Tata Steel Ltd", "Materials", 130.0),
    ("JSWSTEEL", "JSW Steel Ltd", "Materials", 780.0),
    ("TECHM", "Tech Mahindra Ltd", "Technology", 1280.0),
    ("COALINDIA", "Coal India Ltd", "Energy", 390.0),
    ("BRITANNIA", "Britannia Industries Ltd", "Consumer Staples", 5100.0),
    ("DRREDDY", "Dr. Reddy's Laboratories Ltd", "Healthcare", 5600.0),
    ("CIPLA", "Cipla Ltd", "Healthcare", 1250.0),
    ("DIVISLAB", "Divi's Laboratories Ltd", "Healthcare", 3800.0),
    ("EICHERMOT", "Eicher Motors Ltd", "Consumer Discretionary", 3600.0),
    ("GRASIM", "Grasim Industries Ltd", "Materials", 2100.0),
    ("HEROMOTOCO", "Hero MotoCorp Ltd", "Consumer Discretionary", 4200.0),
    ("INDUSINDBK", "IndusInd Bank Ltd", "Financial Services", 1400.0),
    ("NIFTY 50", "NIFTY 50 Index", "Index", 22500.0),
    ("SENSEX", "S&P BSE SENSEX", "Index", 74000.0),
    ("BANKNIFTY", "Nifty Bank Index", "Index", 48000.0),
    ("CRUDEOIL", "Crude Oil Futures", "Commodity", 6200.0),
    ("GOLD", "Gold Futures", "Commodity", 71000.0),
]


def seed_all(db: Session) -> dict:
    """Seed instruments, user, watchlists, holdings, positions, funds, and OHLCV."""
    existing = db.query(Instrument).first()
    if existing:
        return {"status": "already_seeded", "instruments": db.query(Instrument).count()}

    # Seed instruments
    instruments = []
    for symbol, name, sector, base_price in STOCKS:
        change_pct = random.uniform(-4, 4)
        change = round(base_price * change_pct / 100, 2)
        last_price = round(base_price + change, 2)
        inst_type = "EQ"
        exchange = "NSE"
        if sector == "Index":
            inst_type = "INDEX"
        elif sector == "Commodity":
            inst_type = "FUT"
            exchange = "MCX"

        inst = Instrument(
            symbol=symbol,
            name=name,
            exchange=exchange,
            instrument_type=inst_type,
            sector=sector,
            last_price=last_price,
            change=change,
            change_pct=round(change_pct, 2),
            open_price=round(base_price * random.uniform(0.99, 1.01), 2),
            high_price=round(last_price * random.uniform(1.0, 1.02), 2),
            low_price=round(last_price * random.uniform(0.98, 1.0), 2),
            close_price=base_price,
            volume=random.randint(500000, 30000000),
        )
        instruments.append(inst)
    db.add_all(instruments)
    db.flush()

    # Seed demo user
    user = User(
        user_id="AB1234",
        name="Nithin",
        email="demo@kite.local",
        hashed_password=pwd_context.hash("demo123"),
    )
    db.add(user)
    db.flush()

    # Seed fund account
    fund = FundAccount(
        user_id=user.id,
        equity_available=500000.0,
        equity_used=52000.0,
        opening_balance=550000.0,
        payin=0.0,
        payout=0.0,
    )
    db.add(fund)

    # Seed watchlists
    wl_stocks = [
        ["INFY", "RELIANCE", "SBIN", "ITC", "HDFCBANK", "TCS", "TATAMOTORS", "BHARTIARTL"],
        ["NIFTY 50", "SENSEX", "BANKNIFTY", "CRUDEOIL", "GOLD"],
        ["BAJFINANCE", "MARUTI", "WIPRO", "HCLTECH", "ASIANPAINT"],
    ]
    for i, items in enumerate(wl_stocks):
        wl = Watchlist(user_id=user.id, name=f"Watchlist {i + 1}", position=i)
        db.add(wl)
        db.flush()
        for j, sym in enumerate(items):
            wi = WatchlistItem(watchlist_id=wl.id, symbol=sym, exchange="NSE", position=j)
            db.add(wi)

    # Seed holdings
    holding_stocks = [
        ("INFY", 50, 1380.0),
        ("RELIANCE", 20, 2350.0),
        ("HDFCBANK", 30, 1550.0),
        ("TCS", 15, 3500.0),
        ("ITC", 200, 420.0),
        ("SBIN", 100, 580.0),
        ("TATAMOTORS", 80, 600.0),
        ("BHARTIARTL", 25, 1420.0),
    ]
    for sym, qty, avg in holding_stocks:
        inst = db.query(Instrument).filter(Instrument.symbol == sym).first()
        lp = inst.last_price if inst else avg * random.uniform(0.95, 1.1)
        pnl = round((lp - avg) * qty, 2)
        dc = round(inst.change * qty, 2) if inst else 0.0
        dcp = inst.change_pct if inst else 0.0
        h = Holding(
            user_id=user.id,
            symbol=sym,
            quantity=qty,
            average_price=avg,
            last_price=lp,
            pnl=pnl,
            day_change=dc,
            day_change_pct=dcp,
        )
        db.add(h)

    # Seed positions
    pos_data = [
        ("INFY", "MIS", 50, 50, 0, 1445.0, 0.0),
        ("RELIANCE", "MIS", -20, 0, 20, 0.0, 2460.0),
        ("SBIN", "MIS", 100, 100, 0, 618.0, 0.0),
    ]
    for sym, product, qty, bq, sq, bp, sp in pos_data:
        inst = db.query(Instrument).filter(Instrument.symbol == sym).first()
        lp = inst.last_price if inst else bp or sp
        pnl_val = round((lp - bp) * bq if bq > 0 else (sp - lp) * sq, 2)
        p = Position(
            user_id=user.id,
            symbol=sym,
            product=product,
            quantity=qty,
            buy_qty=bq,
            sell_qty=sq,
            buy_price=bp,
            sell_price=sp,
            last_price=lp,
            pnl=pnl_val,
            day_buy_qty=bq,
            day_sell_qty=sq,
            day_buy_price=bp,
            day_sell_price=sp,
        )
        db.add(p)

    # Seed OHLCV for equity instruments (1 year of data)
    today = date.today()
    eq_instruments = [i for i in instruments if i.instrument_type == "EQ"]
    for inst in eq_instruments:
        ohlcv_rows = []
        price = inst.close_price * random.uniform(0.7, 0.9)
        for d in range(365):
            dt = today - timedelta(days=365 - d)
            if dt.weekday() >= 5:
                continue
            daily_return = random.gauss(0.0005, 0.02)
            price = price * (1 + daily_return)
            high = price * random.uniform(1.0, 1.03)
            low = price * random.uniform(0.97, 1.0)
            opn = random.uniform(low, high)
            vol = random.randint(500000, 20000000)
            ohlcv_rows.append(
                OHLCV(
                    symbol=inst.symbol,
                    date=dt,
                    open=round(opn, 2),
                    high=round(high, 2),
                    low=round(low, 2),
                    close=round(price, 2),
                    volume=vol,
                )
            )
        if ohlcv_rows:
            last_row = ohlcv_rows[-1]
            inst.last_price = last_row.close
            inst.close_price = ohlcv_rows[-2].close if len(ohlcv_rows) > 1 else last_row.close
            inst.change = round(last_row.close - inst.close_price, 2)
            inst.change_pct = round((inst.change / inst.close_price) * 100, 2) if inst.close_price else 0.0
            inst.open_price = last_row.open
            inst.high_price = last_row.high
            inst.low_price = last_row.low
            inst.volume = last_row.volume
        db.add_all(ohlcv_rows)

    db.commit()
    return {"status": "seeded", "instruments": len(instruments)}
