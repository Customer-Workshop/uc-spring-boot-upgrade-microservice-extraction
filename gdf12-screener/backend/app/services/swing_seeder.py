"""Seed realistic Indian stock technical data for swing trading scanner."""

from sqlalchemy.orm import Session

from app.models.swing_stock import SwingStock

SEED_SWING_STOCKS = [
    # === STRONG SETUPS (score 8-10) ===
    {
        "symbol": "RELIANCE", "name": "Reliance Industries Ltd", "sector": "Energy",
        "current_price": 1310, "prev_close": 1285, "high_52w": 1600, "low_52w": 1150,
        "sma_20": 1280, "sma_50": 1240, "sma_200": 1220, "ema_12": 1295, "ema_26": 1260,
        "rsi_14": 62.5, "macd_line": 18.5, "macd_signal": 12.3, "macd_histogram": 6.2,
        "volume": 18500000, "avg_volume_20": 12000000, "volume_ratio": 1.54,
        "bb_upper": 1320, "bb_middle": 1265, "bb_lower": 1210, "bb_width": 0.087, "bb_squeeze": True,
        "atr_14": 28.5, "resistance_level": 1300, "support_level": 1240,
        "breakout_above_resistance": True, "market_cap_cr": 1700000,
    },
    {
        "symbol": "TCS", "name": "Tata Consultancy Services Ltd", "sector": "IT Services",
        "current_price": 3520, "prev_close": 3480, "high_52w": 4260, "low_52w": 3060,
        "sma_20": 3450, "sma_50": 3380, "sma_200": 3350, "ema_12": 3490, "ema_26": 3420,
        "rsi_14": 58.2, "macd_line": 32.4, "macd_signal": 28.1, "macd_histogram": 4.3,
        "volume": 4200000, "avg_volume_20": 3500000, "volume_ratio": 1.20,
        "bb_upper": 3560, "bb_middle": 3430, "bb_lower": 3300, "bb_width": 0.076, "bb_squeeze": False,
        "atr_14": 55.0, "resistance_level": 3500, "support_level": 3380,
        "breakout_above_resistance": True, "market_cap_cr": 1248000,
    },
    {
        "symbol": "HDFCBANK", "name": "HDFC Bank Ltd", "sector": "Banking",
        "current_price": 1680, "prev_close": 1640, "high_52w": 1790, "low_52w": 1390,
        "sma_20": 1620, "sma_50": 1580, "sma_200": 1550, "ema_12": 1650, "ema_26": 1610,
        "rsi_14": 65.8, "macd_line": 22.5, "macd_signal": 15.2, "macd_histogram": 7.3,
        "volume": 12000000, "avg_volume_20": 7500000, "volume_ratio": 1.60,
        "bb_upper": 1690, "bb_middle": 1610, "bb_lower": 1530, "bb_width": 0.099, "bb_squeeze": True,
        "atr_14": 32.0, "resistance_level": 1660, "support_level": 1580,
        "breakout_above_resistance": True, "market_cap_cr": 1230000,
    },
    {
        "symbol": "SUNPHARMA", "name": "Sun Pharmaceutical Industries", "sector": "Pharma",
        "current_price": 1750, "prev_close": 1720, "high_52w": 1960, "low_52w": 1220,
        "sma_20": 1700, "sma_50": 1650, "sma_200": 1580, "ema_12": 1730, "ema_26": 1680,
        "rsi_14": 61.3, "macd_line": 25.8, "macd_signal": 20.1, "macd_histogram": 5.7,
        "volume": 6800000, "avg_volume_20": 4200000, "volume_ratio": 1.62,
        "bb_upper": 1770, "bb_middle": 1690, "bb_lower": 1610, "bb_width": 0.095, "bb_squeeze": True,
        "atr_14": 35.0, "resistance_level": 1730, "support_level": 1650,
        "breakout_above_resistance": True, "market_cap_cr": 410000,
    },
    {
        "symbol": "COALINDIA", "name": "Coal India Ltd", "sector": "Mining",
        "current_price": 400, "prev_close": 388, "high_52w": 490, "low_52w": 290,
        "sma_20": 385, "sma_50": 370, "sma_200": 360, "ema_12": 392, "ema_26": 378,
        "rsi_14": 63.2, "macd_line": 8.5, "macd_signal": 5.2, "macd_histogram": 3.3,
        "volume": 15000000, "avg_volume_20": 9500000, "volume_ratio": 1.58,
        "bb_upper": 405, "bb_middle": 378, "bb_lower": 351, "bb_width": 0.143, "bb_squeeze": False,
        "atr_14": 12.0, "resistance_level": 395, "support_level": 370,
        "breakout_above_resistance": True, "market_cap_cr": 237000,
    },
    # === MODERATE SETUPS (score 5-7) ===
    {
        "symbol": "INFY", "name": "Infosys Ltd", "sector": "IT Services",
        "current_price": 1540, "prev_close": 1525, "high_52w": 1960, "low_52w": 1360,
        "sma_20": 1520, "sma_50": 1500, "sma_200": 1510, "ema_12": 1530, "ema_26": 1515,
        "rsi_14": 54.5, "macd_line": 5.2, "macd_signal": 4.8, "macd_histogram": 0.4,
        "volume": 8500000, "avg_volume_20": 7200000, "volume_ratio": 1.18,
        "bb_upper": 1580, "bb_middle": 1520, "bb_lower": 1460, "bb_width": 0.079, "bb_squeeze": False,
        "atr_14": 25.0, "resistance_level": 1560, "support_level": 1500,
        "breakout_above_resistance": False, "market_cap_cr": 631000,
    },
    {
        "symbol": "ITC", "name": "ITC Ltd", "sector": "FMCG",
        "current_price": 435, "prev_close": 430, "high_52w": 500, "low_52w": 390,
        "sma_20": 428, "sma_50": 420, "sma_200": 425, "ema_12": 432, "ema_26": 426,
        "rsi_14": 56.8, "macd_line": 3.2, "macd_signal": 2.8, "macd_histogram": 0.4,
        "volume": 22000000, "avg_volume_20": 18000000, "volume_ratio": 1.22,
        "bb_upper": 450, "bb_middle": 425, "bb_lower": 400, "bb_width": 0.118, "bb_squeeze": False,
        "atr_14": 8.5, "resistance_level": 445, "support_level": 420,
        "breakout_above_resistance": False, "market_cap_cr": 537000,
    },
    {
        "symbol": "MARUTI", "name": "Maruti Suzuki India Ltd", "sector": "Automobile",
        "current_price": 12100, "prev_close": 11950, "high_52w": 13200, "low_52w": 9800,
        "sma_20": 11800, "sma_50": 11500, "sma_200": 11200, "ema_12": 12000, "ema_26": 11700,
        "rsi_14": 58.0, "macd_line": 150, "macd_signal": 120, "macd_histogram": 30,
        "volume": 800000, "avg_volume_20": 650000, "volume_ratio": 1.23,
        "bb_upper": 12300, "bb_middle": 11800, "bb_lower": 11300, "bb_width": 0.085, "bb_squeeze": False,
        "atr_14": 280, "resistance_level": 12200, "support_level": 11500,
        "breakout_above_resistance": False, "market_cap_cr": 370000,
    },
    {
        "symbol": "WIPRO", "name": "Wipro Ltd", "sector": "IT Services",
        "current_price": 425, "prev_close": 418, "high_52w": 570, "low_52w": 380,
        "sma_20": 415, "sma_50": 410, "sma_200": 430, "ema_12": 420, "ema_26": 415,
        "rsi_14": 52.3, "macd_line": 2.1, "macd_signal": 1.8, "macd_histogram": 0.3,
        "volume": 9500000, "avg_volume_20": 8000000, "volume_ratio": 1.19,
        "bb_upper": 440, "bb_middle": 415, "bb_lower": 390, "bb_width": 0.121, "bb_squeeze": False,
        "atr_14": 10.0, "resistance_level": 435, "support_level": 410,
        "breakout_above_resistance": False, "market_cap_cr": 219000,
    },
    {
        "symbol": "BAJFINANCE", "name": "Bajaj Finance Ltd", "sector": "NBFC",
        "current_price": 7050, "prev_close": 6950, "high_52w": 8200, "low_52w": 6200,
        "sma_20": 6900, "sma_50": 6800, "sma_200": 6850, "ema_12": 6980, "ema_26": 6880,
        "rsi_14": 59.5, "macd_line": 45.0, "macd_signal": 35.0, "macd_histogram": 10.0,
        "volume": 3200000, "avg_volume_20": 2800000, "volume_ratio": 1.14,
        "bb_upper": 7200, "bb_middle": 6900, "bb_lower": 6600, "bb_width": 0.087, "bb_squeeze": False,
        "atr_14": 120.0, "resistance_level": 7100, "support_level": 6800,
        "breakout_above_resistance": False, "market_cap_cr": 420000,
    },
    {
        "symbol": "SBIN", "name": "State Bank of India", "sector": "Banking",
        "current_price": 775, "prev_close": 760, "high_52w": 912, "low_52w": 620,
        "sma_20": 755, "sma_50": 740, "sma_200": 720, "ema_12": 765, "ema_26": 748,
        "rsi_14": 60.2, "macd_line": 9.8, "macd_signal": 7.5, "macd_histogram": 2.3,
        "volume": 25000000, "avg_volume_20": 20000000, "volume_ratio": 1.25,
        "bb_upper": 790, "bb_middle": 750, "bb_lower": 710, "bb_width": 0.107, "bb_squeeze": False,
        "atr_14": 18.0, "resistance_level": 780, "support_level": 740,
        "breakout_above_resistance": False, "market_cap_cr": 678000,
    },
    # === WEAK SETUPS (score 0-4) ===
    {
        "symbol": "TATAMOTORS", "name": "Tata Motors Ltd", "sector": "Automobile",
        "current_price": 640, "prev_close": 655, "high_52w": 1080, "low_52w": 580,
        "sma_20": 660, "sma_50": 700, "sma_200": 780, "ema_12": 650, "ema_26": 670,
        "rsi_14": 38.5, "macd_line": -12.5, "macd_signal": -8.2, "macd_histogram": -4.3,
        "volume": 14000000, "avg_volume_20": 16000000, "volume_ratio": 0.88,
        "bb_upper": 710, "bb_middle": 665, "bb_lower": 620, "bb_width": 0.135, "bb_squeeze": False,
        "atr_14": 22.0, "resistance_level": 700, "support_level": 620,
        "breakout_above_resistance": False, "market_cap_cr": 235000,
    },
    {
        "symbol": "ADANIENT", "name": "Adani Enterprises Ltd", "sector": "Infrastructure",
        "current_price": 2380, "prev_close": 2420, "high_52w": 3740, "low_52w": 2020,
        "sma_20": 2450, "sma_50": 2500, "sma_200": 2600, "ema_12": 2400, "ema_26": 2460,
        "rsi_14": 42.1, "macd_line": -28.0, "macd_signal": -18.0, "macd_histogram": -10.0,
        "volume": 5500000, "avg_volume_20": 6800000, "volume_ratio": 0.81,
        "bb_upper": 2600, "bb_middle": 2450, "bb_lower": 2300, "bb_width": 0.122, "bb_squeeze": False,
        "atr_14": 65.0, "resistance_level": 2500, "support_level": 2300,
        "breakout_above_resistance": False, "market_cap_cr": 274000,
    },
    {
        "symbol": "ZOMATO", "name": "Zomato Ltd", "sector": "Internet",
        "current_price": 170, "prev_close": 178, "high_52w": 265, "low_52w": 110,
        "sma_20": 180, "sma_50": 195, "sma_200": 175, "ema_12": 175, "ema_26": 182,
        "rsi_14": 35.8, "macd_line": -4.5, "macd_signal": -2.8, "macd_histogram": -1.7,
        "volume": 32000000, "avg_volume_20": 38000000, "volume_ratio": 0.84,
        "bb_upper": 200, "bb_middle": 182, "bb_lower": 164, "bb_width": 0.198, "bb_squeeze": False,
        "atr_14": 7.5, "resistance_level": 190, "support_level": 160,
        "breakout_above_resistance": False, "market_cap_cr": 155000,
    },
    {
        "symbol": "PAYTM", "name": "One 97 Communications Ltd (Paytm)", "sector": "Fintech",
        "current_price": 660, "prev_close": 680, "high_52w": 980, "low_52w": 320,
        "sma_20": 690, "sma_50": 720, "sma_200": 650, "ema_12": 672, "ema_26": 695,
        "rsi_14": 40.2, "macd_line": -12.0, "macd_signal": -8.0, "macd_histogram": -4.0,
        "volume": 8000000, "avg_volume_20": 10000000, "volume_ratio": 0.80,
        "bb_upper": 740, "bb_middle": 690, "bb_lower": 640, "bb_width": 0.145, "bb_squeeze": False,
        "atr_14": 25.0, "resistance_level": 700, "support_level": 620,
        "breakout_above_resistance": False, "market_cap_cr": 43200,
    },
    {
        "symbol": "CIPLA", "name": "Cipla Ltd", "sector": "Pharma",
        "current_price": 1450, "prev_close": 1430, "high_52w": 1680, "low_52w": 1080,
        "sma_20": 1420, "sma_50": 1400, "sma_200": 1350, "ema_12": 1440, "ema_26": 1410,
        "rsi_14": 57.8, "macd_line": 15.2, "macd_signal": 12.8, "macd_histogram": 2.4,
        "volume": 3800000, "avg_volume_20": 3200000, "volume_ratio": 1.19,
        "bb_upper": 1480, "bb_middle": 1420, "bb_lower": 1360, "bb_width": 0.084, "bb_squeeze": False,
        "atr_14": 28.0, "resistance_level": 1460, "support_level": 1400,
        "breakout_above_resistance": False, "market_cap_cr": 115000,
    },
    {
        "symbol": "ASIANPAINT", "name": "Asian Paints Ltd", "sector": "Chemicals",
        "current_price": 2800, "prev_close": 2780, "high_52w": 3540, "low_52w": 2440,
        "sma_20": 2750, "sma_50": 2700, "sma_200": 2800, "ema_12": 2780, "ema_26": 2740,
        "rsi_14": 53.5, "macd_line": 15.0, "macd_signal": 12.0, "macd_histogram": 3.0,
        "volume": 2200000, "avg_volume_20": 2000000, "volume_ratio": 1.10,
        "bb_upper": 2860, "bb_middle": 2750, "bb_lower": 2640, "bb_width": 0.080, "bb_squeeze": False,
        "atr_14": 45.0, "resistance_level": 2850, "support_level": 2700,
        "breakout_above_resistance": False, "market_cap_cr": 264000,
    },
]


def seed_swing_stocks(db: Session) -> int:
    existing = db.query(SwingStock).count()
    if existing > 0:
        return existing
    for data in SEED_SWING_STOCKS:
        db.add(SwingStock(**data))
    db.commit()
    return len(SEED_SWING_STOCKS)
