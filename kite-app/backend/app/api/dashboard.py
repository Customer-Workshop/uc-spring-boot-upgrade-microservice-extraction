"""Dashboard summary endpoint."""

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.holding import Holding
from app.models.instrument import Instrument
from app.models.position import Position
from app.schemas.schemas import DashboardOut, MarketIndex

router = APIRouter(prefix="/api", tags=["dashboard"])

DEMO_USER_ID = 1


@router.get("/dashboard", response_model=DashboardOut)
def get_dashboard(db: Session = Depends(get_db)):
    # Market indices
    index_symbols = ["NIFTY 50", "SENSEX", "BANKNIFTY"]
    indices = []
    for sym in index_symbols:
        inst = db.query(Instrument).filter(Instrument.symbol == sym).first()
        if inst:
            indices.append(
                MarketIndex(
                    name=sym,
                    value=inst.last_price,
                    change=inst.change,
                    change_pct=inst.change_pct,
                )
            )

    # Holdings summary
    holdings = db.query(Holding).filter(Holding.user_id == DEMO_USER_ID).all()
    holdings_value = sum(h.last_price * h.quantity for h in holdings)
    holdings_investment = sum(h.average_price * h.quantity for h in holdings)
    holdings_pnl = sum(h.pnl for h in holdings)
    holdings_day_pnl = sum(h.day_change for h in holdings)

    # Positions summary
    positions = db.query(Position).filter(Position.user_id == DEMO_USER_ID).all()
    positions_pnl = sum(p.pnl for p in positions)

    return DashboardOut(
        indices=indices,
        holdings_count=len(holdings),
        holdings_value=round(holdings_value, 2),
        holdings_investment=round(holdings_investment, 2),
        holdings_pnl=round(holdings_pnl, 2),
        holdings_day_pnl=round(holdings_day_pnl, 2),
        positions_count=len(positions),
        positions_pnl=round(positions_pnl, 2),
    )
