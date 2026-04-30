"""Signals API: trigger recomputation of technical signals and scores."""

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.database import get_db
from app.services.ingestion import seed_all_mock_data

router = APIRouter(prefix="/signals", tags=["Signals"])


@router.post("/recompute", response_model=dict)
def recompute_signals(db: Session = Depends(get_db)):
    from app.tasks.scheduled import recompute_signals as _recompute_signals
    from app.tasks.scheduled import recompute_scores as _recompute_scores

    sig_result = _recompute_signals()
    score_result = _recompute_scores()
    return {"signals": sig_result, "scores": score_result}


@router.post("/seed", response_model=dict)
def seed_data(db: Session = Depends(get_db)):
    seed_all_mock_data(db)
    return {"status": "ok", "message": "Mock data seeded successfully"}
