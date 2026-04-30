from datetime import date, datetime

from sqlalchemy import Date, DateTime, Float, ForeignKey, Integer, String, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database import Base


class PromoterDisclosure(Base):
    __tablename__ = "promoter_disclosures"

    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    stock_id: Mapped[int] = mapped_column(Integer, ForeignKey("stocks.id"), nullable=False)
    disclosure_date: Mapped[date] = mapped_column(Date, nullable=False, index=True)

    disclosure_type: Mapped[str] = mapped_column(String(50), nullable=False)
    entity_name: Mapped[str | None] = mapped_column(String(200), nullable=True)
    transaction_type: Mapped[str] = mapped_column(String(20), nullable=False)  # buy, sell, pledge
    shares: Mapped[float | None] = mapped_column(Float, nullable=True)
    value_inr: Mapped[float | None] = mapped_column(Float, nullable=True)

    promoter_holding_pct: Mapped[float | None] = mapped_column(Float, nullable=True)
    promoter_pledge_pct: Mapped[float | None] = mapped_column(Float, nullable=True)
    change_in_holding_pct: Mapped[float | None] = mapped_column(Float, nullable=True)

    is_clustered_buy: Mapped[bool | None] = mapped_column(nullable=True)
    signal_strength: Mapped[float | None] = mapped_column(Float, nullable=True)

    source: Mapped[str] = mapped_column(String(50), default="mock")
    source_url: Mapped[str | None] = mapped_column(String(500), nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())

    stock = relationship("Stock", back_populates="disclosures")
