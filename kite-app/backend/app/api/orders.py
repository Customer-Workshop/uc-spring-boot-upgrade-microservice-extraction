"""Order management endpoints."""

import random
from datetime import UTC, datetime

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.fund import FundAccount
from app.models.holding import Holding
from app.models.instrument import Instrument
from app.models.order import Order
from app.models.position import Position
from app.schemas.schemas import OrderCreate, OrderOut

router = APIRouter(prefix="/api", tags=["orders"])

DEMO_USER_ID = 1


@router.get("/orders", response_model=list[OrderOut])
def get_orders(db: Session = Depends(get_db)):
    return (
        db.query(Order)
        .filter(Order.user_id == DEMO_USER_ID)
        .order_by(Order.order_timestamp.desc())
        .all()
    )


@router.post("/orders", response_model=OrderOut)
def place_order(order: OrderCreate, db: Session = Depends(get_db)):
    inst = db.query(Instrument).filter(Instrument.symbol == order.symbol).first()
    if not inst:
        raise HTTPException(status_code=404, detail=f"Instrument {order.symbol} not found")

    exec_price = order.price if order.order_type == "LIMIT" and order.price > 0 else inst.last_price
    slippage = exec_price * random.uniform(0.0001, 0.001)
    if order.transaction_type == "BUY":
        exec_price = round(exec_price + slippage, 2)
    else:
        exec_price = round(exec_price - slippage, 2)

    db_order = Order(
        user_id=DEMO_USER_ID,
        symbol=order.symbol,
        exchange=order.exchange,
        transaction_type=order.transaction_type,
        order_type=order.order_type,
        product=order.product,
        quantity=order.quantity,
        price=order.price,
        trigger_price=order.trigger_price,
        disclosed_qty=order.disclosed_qty,
        status="COMPLETE",
        filled_qty=order.quantity,
        average_price=exec_price,
        order_timestamp=datetime.now(UTC),
        exchange_timestamp=datetime.now(UTC),
        tag=order.tag,
    )
    db.add(db_order)

    # Update holdings/positions
    _update_portfolio(db, order, exec_price)

    db.commit()
    db.refresh(db_order)
    return db_order


def _update_portfolio(db: Session, order: OrderCreate, exec_price: float) -> None:
    if order.product == "CNC":
        _update_holding(db, order, exec_price)
    else:
        _update_position(db, order, exec_price)

    # Update fund balance
    fund = db.query(FundAccount).filter(FundAccount.user_id == DEMO_USER_ID).first()
    if fund:
        trade_value = exec_price * order.quantity
        if order.transaction_type == "BUY":
            fund.equity_used += trade_value
            fund.equity_available -= trade_value
        else:
            fund.equity_used -= trade_value
            fund.equity_available += trade_value


def _update_holding(db: Session, order: OrderCreate, exec_price: float) -> None:
    holding = (
        db.query(Holding)
        .filter(Holding.user_id == DEMO_USER_ID, Holding.symbol == order.symbol)
        .first()
    )
    if order.transaction_type == "BUY":
        if holding:
            total_qty = holding.quantity + order.quantity
            holding.average_price = round(
                (holding.average_price * holding.quantity + exec_price * order.quantity) / total_qty, 2
            )
            holding.quantity = total_qty
            holding.last_price = exec_price
            holding.pnl = round((exec_price - holding.average_price) * total_qty, 2)
        else:
            holding = Holding(
                user_id=DEMO_USER_ID,
                symbol=order.symbol,
                exchange=order.exchange,
                quantity=order.quantity,
                average_price=exec_price,
                last_price=exec_price,
                pnl=0.0,
            )
            db.add(holding)
    else:
        if holding:
            holding.quantity -= order.quantity
            holding.pnl = round((exec_price - holding.average_price) * holding.quantity, 2)
            if holding.quantity <= 0:
                db.delete(holding)


def _update_position(db: Session, order: OrderCreate, exec_price: float) -> None:
    position = (
        db.query(Position)
        .filter(
            Position.user_id == DEMO_USER_ID,
            Position.symbol == order.symbol,
            Position.product == order.product,
        )
        .first()
    )
    if order.transaction_type == "BUY":
        if position:
            position.buy_qty += order.quantity
            position.day_buy_qty += order.quantity
            position.buy_price = round(
                (position.buy_price * (position.buy_qty - order.quantity) + exec_price * order.quantity)
                / position.buy_qty, 2
            )
            position.day_buy_price = position.buy_price
            position.quantity += order.quantity
        else:
            position = Position(
                user_id=DEMO_USER_ID,
                symbol=order.symbol,
                exchange=order.exchange,
                product=order.product,
                quantity=order.quantity,
                buy_qty=order.quantity,
                buy_price=exec_price,
                last_price=exec_price,
                day_buy_qty=order.quantity,
                day_buy_price=exec_price,
            )
            db.add(position)
    else:
        if position:
            position.sell_qty += order.quantity
            position.day_sell_qty += order.quantity
            position.sell_price = round(
                (position.sell_price * (position.sell_qty - order.quantity) + exec_price * order.quantity)
                / position.sell_qty, 2
            )
            position.day_sell_price = position.sell_price
            position.quantity -= order.quantity
        else:
            position = Position(
                user_id=DEMO_USER_ID,
                symbol=order.symbol,
                exchange=order.exchange,
                product=order.product,
                quantity=-order.quantity,
                sell_qty=order.quantity,
                sell_price=exec_price,
                last_price=exec_price,
                day_sell_qty=order.quantity,
                day_sell_price=exec_price,
            )
            db.add(position)

    if position and position.id:
        position.last_price = exec_price
        if position.buy_qty > 0 and position.sell_qty > 0:
            position.pnl = round(
                (position.sell_price - position.buy_price) * min(position.buy_qty, position.sell_qty), 2
            )
        elif position.buy_qty > 0:
            position.pnl = round((exec_price - position.buy_price) * position.buy_qty, 2)
        else:
            position.pnl = round((position.sell_price - exec_price) * position.sell_qty, 2)
