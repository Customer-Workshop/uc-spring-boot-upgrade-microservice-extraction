"""Holdings, positions, and funds endpoints."""

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.fund import FundAccount
from app.models.holding import Holding
from app.models.position import Position
from app.schemas.schemas import FundOut, HoldingOut, PositionOut

router = APIRouter(prefix="/api", tags=["portfolio"])

DEMO_USER_ID = 1


@router.get("/holdings", response_model=list[HoldingOut])
def get_holdings(db: Session = Depends(get_db)):
    return (
        db.query(Holding)
        .filter(Holding.user_id == DEMO_USER_ID)
        .all()
    )


@router.get("/positions", response_model=list[PositionOut])
def get_positions(db: Session = Depends(get_db)):
    return (
        db.query(Position)
        .filter(Position.user_id == DEMO_USER_ID)
        .all()
    )


@router.get("/funds", response_model=FundOut)
def get_funds(db: Session = Depends(get_db)):
    fund = db.query(FundAccount).filter(FundAccount.user_id == DEMO_USER_ID).first()
    if not fund:
        return FundOut(
            equity_available=0,
            equity_used=0,
            commodity_available=0,
            commodity_used=0,
            opening_balance=0,
            payin=0,
            payout=0,
            collateral=0,
        )
    return fund
