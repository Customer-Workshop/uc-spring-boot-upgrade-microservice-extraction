"""Seed realistic Indian ETF data for ETF screener."""

from sqlalchemy.orm import Session

from app.models.etf import ETF

SEED_ETFS = [
    # === INDEX ETFs ===
    {
        "symbol": "NIFTYBEES", "name": "Nippon India ETF Nifty BeES",
        "category": "Large Cap Index", "amc": "Nippon India Mutual Fund",
        "current_price": 245, "nav": 244.5, "high_52w": 265, "low_52w": 200,
        "sma_20w": 238, "sma_50w": 228, "sma_10m": 232, "sma_12m": 225,
        "return_1w": 1.2, "return_1m": 3.5, "return_3m": 5.8, "return_6m": 8.2,
        "return_1y": 14.5, "return_3y_cagr": 15.2, "return_5y_cagr": 14.8,
        "expense_ratio": 0.05, "tracking_error": 0.08, "aum_cr": 42000, "avg_volume": 2500000,
        "rsi_14": 58.5, "above_20w_ma": 1, "above_50w_ma": 1,
    },
    {
        "symbol": "JUNIORBEES", "name": "Nippon India ETF Junior BeES",
        "category": "Large Cap Index", "amc": "Nippon India Mutual Fund",
        "current_price": 680, "nav": 679, "high_52w": 750, "low_52w": 540,
        "sma_20w": 660, "sma_50w": 635, "sma_10m": 645, "sma_12m": 630,
        "return_1w": 0.8, "return_1m": 2.8, "return_3m": 4.5, "return_6m": 12.5,
        "return_1y": 18.2, "return_3y_cagr": 16.8, "return_5y_cagr": 15.5,
        "expense_ratio": 0.20, "tracking_error": 0.25, "aum_cr": 8500, "avg_volume": 180000,
        "rsi_14": 55.2, "above_20w_ma": 1, "above_50w_ma": 1,
    },
    {
        "symbol": "SETFNIF50", "name": "SBI ETF Nifty 50",
        "category": "Large Cap Index", "amc": "SBI Mutual Fund",
        "current_price": 242, "nav": 241.8, "high_52w": 262, "low_52w": 198,
        "sma_20w": 235, "sma_50w": 225, "sma_10m": 228, "sma_12m": 222,
        "return_1w": 1.1, "return_1m": 3.2, "return_3m": 5.5, "return_6m": 7.8,
        "return_1y": 14.0, "return_3y_cagr": 14.8, "return_5y_cagr": 14.2,
        "expense_ratio": 0.07, "tracking_error": 0.10, "aum_cr": 185000, "avg_volume": 350000,
        "rsi_14": 57.8, "above_20w_ma": 1, "above_50w_ma": 1,
    },
    # === SECTOR ETFs ===
    {
        "symbol": "BANKBEES", "name": "Nippon India ETF Bank BeES",
        "category": "Banking Sector", "amc": "Nippon India Mutual Fund",
        "current_price": 510, "nav": 509, "high_52w": 560, "low_52w": 400,
        "sma_20w": 495, "sma_50w": 475, "sma_10m": 480, "sma_12m": 465,
        "return_1w": 1.5, "return_1m": 4.2, "return_3m": 7.2, "return_6m": 10.5,
        "return_1y": 16.8, "return_3y_cagr": 14.5, "return_5y_cagr": 12.8,
        "expense_ratio": 0.20, "tracking_error": 0.18, "aum_cr": 18500, "avg_volume": 850000,
        "rsi_14": 62.3, "above_20w_ma": 1, "above_50w_ma": 1,
    },
    {
        "symbol": "ITBEES", "name": "Nippon India ETF Nifty IT",
        "category": "IT Sector", "amc": "Nippon India Mutual Fund",
        "current_price": 380, "nav": 379, "high_52w": 480, "low_52w": 320,
        "sma_20w": 370, "sma_50w": 385, "sma_10m": 378, "sma_12m": 390,
        "return_1w": 0.5, "return_1m": -1.2, "return_3m": 2.8, "return_6m": -3.5,
        "return_1y": 5.2, "return_3y_cagr": 8.5, "return_5y_cagr": 16.2,
        "expense_ratio": 0.25, "tracking_error": 0.30, "aum_cr": 2800, "avg_volume": 120000,
        "rsi_14": 45.8, "above_20w_ma": 1, "above_50w_ma": 0,
    },
    {
        "symbol": "PHARMABEES", "name": "Nippon India ETF Nifty Pharma",
        "category": "Pharma Sector", "amc": "Nippon India Mutual Fund",
        "current_price": 18.5, "nav": 18.4, "high_52w": 20.5, "low_52w": 13.8,
        "sma_20w": 17.8, "sma_50w": 17.0, "sma_10m": 17.2, "sma_12m": 16.5,
        "return_1w": 1.8, "return_1m": 4.5, "return_3m": 8.2, "return_6m": 15.5,
        "return_1y": 22.5, "return_3y_cagr": 18.2, "return_5y_cagr": 16.8,
        "expense_ratio": 0.30, "tracking_error": 0.35, "aum_cr": 620, "avg_volume": 25000,
        "rsi_14": 60.5, "above_20w_ma": 1, "above_50w_ma": 1,
    },
    # === BROAD / THEMATIC ===
    {
        "symbol": "MOM50", "name": "Motilal Oswal Nifty 50 ETF",
        "category": "Large Cap Index", "amc": "Motilal Oswal AMC",
        "current_price": 195, "nav": 194.5, "high_52w": 210, "low_52w": 160,
        "sma_20w": 190, "sma_50w": 182, "sma_10m": 185, "sma_12m": 180,
        "return_1w": 1.0, "return_1m": 3.0, "return_3m": 5.2, "return_6m": 7.5,
        "return_1y": 13.5, "return_3y_cagr": 14.2, "return_5y_cagr": 13.8,
        "expense_ratio": 0.10, "tracking_error": 0.15, "aum_cr": 3200, "avg_volume": 45000,
        "rsi_14": 56.2, "above_20w_ma": 1, "above_50w_ma": 1,
    },
    {
        "symbol": "MIDCAPETF", "name": "Nippon India ETF Nifty Midcap 150",
        "category": "Mid Cap Index", "amc": "Nippon India Mutual Fund",
        "current_price": 165, "nav": 164.5, "high_52w": 195, "low_52w": 125,
        "sma_20w": 158, "sma_50w": 150, "sma_10m": 152, "sma_12m": 148,
        "return_1w": 1.5, "return_1m": 4.8, "return_3m": 8.5, "return_6m": 18.2,
        "return_1y": 25.5, "return_3y_cagr": 22.5, "return_5y_cagr": 18.8,
        "expense_ratio": 0.22, "tracking_error": 0.28, "aum_cr": 1800, "avg_volume": 85000,
        "rsi_14": 62.8, "above_20w_ma": 1, "above_50w_ma": 1,
    },
    {
        "symbol": "GOLDBEES", "name": "Nippon India ETF Gold BeES",
        "category": "Gold", "amc": "Nippon India Mutual Fund",
        "current_price": 58.5, "nav": 58.2, "high_52w": 62, "low_52w": 48,
        "sma_20w": 56, "sma_50w": 54, "sma_10m": 54.5, "sma_12m": 53,
        "return_1w": 0.8, "return_1m": 2.5, "return_3m": 5.5, "return_6m": 12.0,
        "return_1y": 18.5, "return_3y_cagr": 14.5, "return_5y_cagr": 13.2,
        "expense_ratio": 0.79, "tracking_error": 0.50, "aum_cr": 12500, "avg_volume": 1200000,
        "rsi_14": 55.0, "above_20w_ma": 1, "above_50w_ma": 1,
    },
    {
        "symbol": "SILVERBEES", "name": "Nippon India ETF Silver BeES",
        "category": "Silver", "amc": "Nippon India Mutual Fund",
        "current_price": 72, "nav": 71.5, "high_52w": 82, "low_52w": 55,
        "sma_20w": 69, "sma_50w": 66, "sma_10m": 67, "sma_12m": 65,
        "return_1w": 2.0, "return_1m": 5.5, "return_3m": 10.2, "return_6m": 20.5,
        "return_1y": 28.0, "return_3y_cagr": 12.0, "return_5y_cagr": 10.5,
        "expense_ratio": 0.65, "tracking_error": 0.85, "aum_cr": 3200, "avg_volume": 450000,
        "rsi_14": 62.0, "above_20w_ma": 1, "above_50w_ma": 1,
    },
    {
        "symbol": "LIQUIDBEES", "name": "Nippon India ETF Liquid BeES",
        "category": "Liquid / Money Market", "amc": "Nippon India Mutual Fund",
        "current_price": 1000.2, "nav": 1000.1, "high_52w": 1000.5, "low_52w": 999.5,
        "sma_20w": 1000.0, "sma_50w": 999.8, "sma_10m": 999.9, "sma_12m": 999.7,
        "return_1w": 0.13, "return_1m": 0.55, "return_3m": 1.65, "return_6m": 3.3,
        "return_1y": 6.8, "return_3y_cagr": 5.5, "return_5y_cagr": 5.2,
        "expense_ratio": 0.07, "tracking_error": 0.02, "aum_cr": 38000, "avg_volume": 4500000,
        "rsi_14": 50.0, "above_20w_ma": 1, "above_50w_ma": 1,
    },
    {
        "symbol": "PSUBNKBEES", "name": "Nippon India ETF Nifty PSU Bank BeES",
        "category": "PSU Bank Sector", "amc": "Nippon India Mutual Fund",
        "current_price": 82, "nav": 81.5, "high_52w": 98, "low_52w": 52,
        "sma_20w": 78, "sma_50w": 72, "sma_10m": 74, "sma_12m": 70,
        "return_1w": 2.5, "return_1m": 6.8, "return_3m": 12.5, "return_6m": 28.0,
        "return_1y": 42.5, "return_3y_cagr": 35.0, "return_5y_cagr": 22.0,
        "expense_ratio": 0.49, "tracking_error": 0.55, "aum_cr": 2800, "avg_volume": 380000,
        "rsi_14": 65.8, "above_20w_ma": 1, "above_50w_ma": 1,
    },
    {
        "symbol": "CPSEETF", "name": "Nippon India CPSE ETF",
        "category": "PSU Thematic", "amc": "Nippon India Mutual Fund",
        "current_price": 72, "nav": 71.5, "high_52w": 88, "low_52w": 48,
        "sma_20w": 68, "sma_50w": 65, "sma_10m": 66, "sma_12m": 62,
        "return_1w": 1.8, "return_1m": 5.2, "return_3m": 10.0, "return_6m": 22.0,
        "return_1y": 35.0, "return_3y_cagr": 28.5, "return_5y_cagr": 18.5,
        "expense_ratio": 0.07, "tracking_error": 0.20, "aum_cr": 28000, "avg_volume": 520000,
        "rsi_14": 58.2, "above_20w_ma": 1, "above_50w_ma": 1,
    },
    {
        "symbol": "NETFNIFTY", "name": "ICICI Prudential Nifty ETF",
        "category": "Large Cap Index", "amc": "ICICI Prudential AMC",
        "current_price": 244, "nav": 243.5, "high_52w": 264, "low_52w": 199,
        "sma_20w": 237, "sma_50w": 227, "sma_10m": 230, "sma_12m": 224,
        "return_1w": 1.1, "return_1m": 3.3, "return_3m": 5.6, "return_6m": 8.0,
        "return_1y": 14.2, "return_3y_cagr": 15.0, "return_5y_cagr": 14.5,
        "expense_ratio": 0.05, "tracking_error": 0.06, "aum_cr": 12000, "avg_volume": 180000,
        "rsi_14": 57.5, "above_20w_ma": 1, "above_50w_ma": 1,
    },
    {
        "symbol": "MOM100", "name": "Motilal Oswal Nifty 100 ETF",
        "category": "Large Cap Index", "amc": "Motilal Oswal AMC",
        "current_price": 182, "nav": 181.5, "high_52w": 198, "low_52w": 148,
        "sma_20w": 175, "sma_50w": 168, "sma_10m": 170, "sma_12m": 165,
        "return_1w": 1.3, "return_1m": 3.8, "return_3m": 6.2, "return_6m": 10.5,
        "return_1y": 16.5, "return_3y_cagr": 15.8, "return_5y_cagr": 15.0,
        "expense_ratio": 0.15, "tracking_error": 0.18, "aum_cr": 1200, "avg_volume": 15000,
        "rsi_14": 59.0, "above_20w_ma": 1, "above_50w_ma": 1,
    },
]


def seed_etfs(db: Session) -> int:
    existing = db.query(ETF).count()
    if existing > 0:
        return existing
    for data in SEED_ETFS:
        db.add(ETF(**data))
    db.commit()
    return len(SEED_ETFS)
