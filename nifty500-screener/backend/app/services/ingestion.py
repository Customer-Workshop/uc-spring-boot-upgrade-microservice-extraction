"""Data ingestion layer: NIFTY 500 universe + OHLCV + fundamentals.

When API keys are missing, generates realistic mock data to keep interfaces identical.
"""

import random
from datetime import date, timedelta

import numpy as np
import pandas as pd
from sqlalchemy.orm import Session

from app.config import settings
from app.models.disclosures import PromoterDisclosure
from app.models.fundamentals import Fundamentals
from app.models.news import NewsItem
from app.models.ohlcv import OHLCV
from app.models.stock import Stock

NIFTY500_SAMPLE = [
    ("RELIANCE", "Reliance Industries Ltd", "Oil & Gas", "Energy", "INE002A01018"),
    ("TCS", "Tata Consultancy Services Ltd", "IT Services", "Technology", "INE467B01029"),
    ("HDFCBANK", "HDFC Bank Ltd", "Banking", "Financial Services", "INE040A01034"),
    ("INFY", "Infosys Ltd", "IT Services", "Technology", "INE009A01021"),
    ("ICICIBANK", "ICICI Bank Ltd", "Banking", "Financial Services", "INE090A01021"),
    ("HINDUNILVR", "Hindustan Unilever Ltd", "FMCG", "Consumer Staples", "INE030A01027"),
    ("SBIN", "State Bank of India", "Banking", "Financial Services", "INE062A01020"),
    ("BHARTIARTL", "Bharti Airtel Ltd", "Telecom", "Communication", "INE397D01024"),
    ("KOTAKBANK", "Kotak Mahindra Bank Ltd", "Banking", "Financial Services", "INE237A01028"),
    ("ITC", "ITC Ltd", "FMCG", "Consumer Staples", "INE154A01025"),
    ("LT", "Larsen & Toubro Ltd", "Construction", "Industrials", "INE018A01030"),
    ("AXISBANK", "Axis Bank Ltd", "Banking", "Financial Services", "INE238A01034"),
    ("ASIANPAINT", "Asian Paints Ltd", "Paints", "Materials", "INE021A01026"),
    ("MARUTI", "Maruti Suzuki India Ltd", "Auto", "Consumer Discretionary", "INE585B01010"),
    ("BAJFINANCE", "Bajaj Finance Ltd", "NBFC", "Financial Services", "INE296A01024"),
    ("TITAN", "Titan Company Ltd", "Jewellery", "Consumer Discretionary", "INE280A01028"),
    ("SUNPHARMA", "Sun Pharmaceutical Industries", "Pharma", "Healthcare", "INE044A01036"),
    ("WIPRO", "Wipro Ltd", "IT Services", "Technology", "INE075A01022"),
    ("ULTRACEMCO", "UltraTech Cement Ltd", "Cement", "Materials", "INE481G01011"),
    ("NESTLEIND", "Nestle India Ltd", "FMCG", "Consumer Staples", "INE239A01016"),
    ("TATAMOTORS", "Tata Motors Ltd", "Auto", "Consumer Discretionary", "INE155A01022"),
    ("HCLTECH", "HCL Technologies Ltd", "IT Services", "Technology", "INE860A01027"),
    ("POWERGRID", "Power Grid Corp", "Power", "Utilities", "INE752E01010"),
    ("NTPC", "NTPC Ltd", "Power", "Utilities", "INE733E01010"),
    ("ONGC", "Oil & Natural Gas Corp", "Oil & Gas", "Energy", "INE213A01029"),
    ("ADANIENT", "Adani Enterprises Ltd", "Diversified", "Industrials", "INE423A01024"),
    ("TECHM", "Tech Mahindra Ltd", "IT Services", "Technology", "INE669C01036"),
    ("JSWSTEEL", "JSW Steel Ltd", "Steel", "Materials", "INE019A01038"),
    ("TATASTEEL", "Tata Steel Ltd", "Steel", "Materials", "INE081A01020"),
    ("DRREDDY", "Dr. Reddy's Laboratories", "Pharma", "Healthcare", "INE089A01023"),
    ("CIPLA", "Cipla Ltd", "Pharma", "Healthcare", "INE059A01026"),
    ("DIVISLAB", "Divi's Laboratories Ltd", "Pharma", "Healthcare", "INE361B01024"),
    ("EICHERMOT", "Eicher Motors Ltd", "Auto", "Consumer Discretionary", "INE066A01021"),
    ("GRASIM", "Grasim Industries Ltd", "Cement", "Materials", "INE047A01021"),
    ("BAJAJ-AUTO", "Bajaj Auto Ltd", "Auto", "Consumer Discretionary", "INE917I01010"),
    ("COALINDIA", "Coal India Ltd", "Mining", "Energy", "INE522F01014"),
    ("HEROMOTOCO", "Hero MotoCorp Ltd", "Auto", "Consumer Discretionary", "INE158A01026"),
    ("BPCL", "Bharat Petroleum Corp", "Oil & Gas", "Energy", "INE029A01011"),
    ("INDUSINDBK", "IndusInd Bank Ltd", "Banking", "Financial Services", "INE095A01012"),
    ("HDFCLIFE", "HDFC Life Insurance Co", "Insurance", "Financial Services", "INE795G01014"),
    ("SBILIFE", "SBI Life Insurance Co", "Insurance", "Financial Services", "INE123W01016"),
    ("DABUR", "Dabur India Ltd", "FMCG", "Consumer Staples", "INE016A01026"),
    ("BRITANNIA", "Britannia Industries Ltd", "FMCG", "Consumer Staples", "INE216A01030"),
    ("PIDILITIND", "Pidilite Industries Ltd", "Chemicals", "Materials", "INE318A01026"),
    ("HAVELLS", "Havells India Ltd", "Electricals", "Industrials", "INE176B01034"),
    ("GODREJCP", "Godrej Consumer Products", "FMCG", "Consumer Staples", "INE102D01028"),
    ("TATACONSUM", "Tata Consumer Products", "FMCG", "Consumer Staples", "INE192A01025"),
    ("BERGEPAINT", "Berger Paints India Ltd", "Paints", "Materials", "INE463A01038"),
    ("APOLLOHOSP", "Apollo Hospitals Enterprise", "Healthcare", "Healthcare", "INE437A01024"),
    ("SIEMENS", "Siemens Ltd", "Electricals", "Industrials", "INE003A01024"),
]

SECTORS = list({s[3] for s in NIFTY500_SAMPLE})


def seed_universe(db: Session) -> list[Stock]:
    existing = db.query(Stock).count()
    if existing >= len(NIFTY500_SAMPLE):
        return db.query(Stock).all()

    stocks = []
    for symbol, name, industry, sector, isin in NIFTY500_SAMPLE:
        stock = db.query(Stock).filter(Stock.symbol == symbol).first()
        if not stock:
            stock = Stock(
                symbol=symbol,
                company_name=name,
                industry=industry,
                sector=sector,
                isin=isin,
                is_nifty500=True,
                market_cap=random.uniform(10_000, 1_500_000) * 1e6,
                avg_daily_value=random.uniform(5_000_000, 500_000_000),
            )
            db.add(stock)
        stocks.append(stock)

    db.commit()
    for s in stocks:
        db.refresh(s)
    return stocks


def generate_mock_ohlcv(
    db: Session, stock: Stock, days: int = 500
) -> list[OHLCV]:
    existing = db.query(OHLCV).filter(OHLCV.stock_id == stock.id).count()
    if existing >= days:
        return db.query(OHLCV).filter(OHLCV.stock_id == stock.id).order_by(OHLCV.date).all()

    base_price = random.uniform(50, 5000)
    records = []
    current_price = base_price
    today = date.today()

    for i in range(days, 0, -1):
        d = today - timedelta(days=i)
        if d.weekday() >= 5:
            continue

        change_pct = random.gauss(0.0005, 0.02)
        current_price *= 1 + change_pct
        current_price = max(current_price, 1)

        o = current_price * random.uniform(0.98, 1.02)
        h = max(o, current_price) * random.uniform(1.0, 1.03)
        l = min(o, current_price) * random.uniform(0.97, 1.0)
        c = current_price
        v = random.uniform(100_000, 50_000_000)

        record = OHLCV(
            stock_id=stock.id,
            date=d,
            timeframe="daily",
            open=round(o, 2),
            high=round(h, 2),
            low=round(l, 2),
            close=round(c, 2),
            volume=round(v),
            adjusted_close=round(c, 2),
            source="mock",
        )
        records.append(record)

    db.add_all(records)
    db.commit()
    return records


def generate_mock_fundamentals(db: Session, stock: Stock) -> Fundamentals:
    existing = db.query(Fundamentals).filter(Fundamentals.stock_id == stock.id).first()
    if existing:
        return existing

    f = Fundamentals(
        stock_id=stock.id,
        report_date=date.today() - timedelta(days=90),
        period="annual",
        roe=random.uniform(5, 35),
        roce=random.uniform(5, 30),
        roa=random.uniform(2, 20),
        gross_margin=random.uniform(20, 70),
        ebit_margin=random.uniform(5, 35),
        ebitda_margin=random.uniform(8, 40),
        net_margin=random.uniform(3, 25),
        revenue=random.uniform(1_000, 500_000) * 1e6,
        net_income=random.uniform(100, 50_000) * 1e6,
        eps=random.uniform(5, 500),
        revenue_growth_3y=random.uniform(-5, 30),
        revenue_growth_5y=random.uniform(-3, 25),
        eps_growth_3y=random.uniform(-10, 40),
        eps_growth_5y=random.uniform(-5, 35),
        profit_cagr_3y=random.uniform(-5, 35),
        profit_cagr_5y=random.uniform(-3, 30),
        debt_to_equity=random.uniform(0, 3),
        net_debt_to_ebitda=random.uniform(-1, 5),
        interest_coverage=random.uniform(1, 30),
        total_debt=random.uniform(0, 100_000) * 1e6,
        total_equity=random.uniform(1_000, 200_000) * 1e6,
        operating_cashflow=random.uniform(100, 50_000) * 1e6,
        free_cashflow=random.uniform(-5_000, 40_000) * 1e6,
        cfo_to_ni_ratio=random.uniform(0.5, 1.5),
        fcf_yield=random.uniform(-2, 10),
        asset_turnover=random.uniform(0.3, 2.5),
        working_capital_days=random.uniform(10, 120),
        pe_ratio=random.uniform(5, 80),
        pb_ratio=random.uniform(0.5, 15),
        ev_to_ebitda=random.uniform(3, 40),
        peg_ratio=random.uniform(0.3, 5),
        earnings_yield=random.uniform(1, 20),
        earnings_variability=random.uniform(0.05, 0.5),
        source="mock",
    )
    db.add(f)
    db.commit()
    db.refresh(f)
    return f


def generate_mock_disclosures(db: Session, stock: Stock, count: int = 5) -> list[PromoterDisclosure]:
    existing = db.query(PromoterDisclosure).filter(PromoterDisclosure.stock_id == stock.id).count()
    if existing >= count:
        return db.query(PromoterDisclosure).filter(PromoterDisclosure.stock_id == stock.id).all()

    records = []
    for i in range(count):
        d = PromoterDisclosure(
            stock_id=stock.id,
            disclosure_date=date.today() - timedelta(days=random.randint(1, 365)),
            disclosure_type=random.choice(["promoter", "insider"]),
            entity_name=f"Promoter Entity {i+1}",
            transaction_type=random.choice(["buy", "sell", "pledge"]),
            shares=random.uniform(1_000, 1_000_000),
            value_inr=random.uniform(100_000, 100_000_000),
            promoter_holding_pct=random.uniform(30, 75),
            promoter_pledge_pct=random.uniform(0, 30),
            change_in_holding_pct=random.uniform(-2, 2),
            is_clustered_buy=random.random() > 0.7,
            signal_strength=random.uniform(0, 1),
            source="mock",
        )
        records.append(d)

    db.add_all(records)
    db.commit()
    return records


def generate_mock_news(db: Session, stock: Stock, count: int = 3) -> list[NewsItem]:
    existing = db.query(NewsItem).filter(NewsItem.stock_id == stock.id).count()
    if existing >= count:
        return db.query(NewsItem).filter(NewsItem.stock_id == stock.id).all()

    sentiments = ["positive", "negative", "neutral"]
    categories = ["earnings", "regulation", "market", "supply_chain", "macro"]
    records = []
    for i in range(count):
        sentiment = random.choice(sentiments)
        n = NewsItem(
            stock_id=stock.id,
            title=f"{stock.company_name} - Mock news headline {i+1}",
            summary=f"This is a mock news summary for {stock.symbol}.",
            url=f"https://example.com/news/{stock.symbol.lower()}/{i+1}",
            source="mock",
            sentiment=sentiment,
            sentiment_score={"positive": 0.7, "negative": -0.6, "neutral": 0.1}[sentiment],
            impact_score=random.uniform(0, 1),
            category=random.choice(categories),
            sector_relevance=stock.sector,
        )
        records.append(n)

    db.add_all(records)
    db.commit()
    return records


def seed_all_mock_data(db: Session) -> None:
    stocks = seed_universe(db)
    for stock in stocks:
        generate_mock_ohlcv(db, stock)
        generate_mock_fundamentals(db, stock)
        generate_mock_disclosures(db, stock)
        generate_mock_news(db, stock)
