from celery import Celery

from app.config import settings

celery_app = Celery(
    "nifty500_screener",
    broker=settings.celery_broker_url,
    backend=settings.celery_result_backend,
)

celery_app.conf.update(
    task_serializer="json",
    accept_content=["json"],
    result_serializer="json",
    timezone="Asia/Kolkata",
    enable_utc=True,
    beat_schedule={
        "daily-universe-refresh": {
            "task": "app.tasks.scheduled.refresh_universe",
            "schedule": 86400.0,  # once per day
        },
        "daily-signals-recompute": {
            "task": "app.tasks.scheduled.recompute_signals",
            "schedule": 86400.0,
        },
        "daily-scores-recompute": {
            "task": "app.tasks.scheduled.recompute_scores",
            "schedule": 86400.0,
        },
    },
)
