"""APScheduler integration for scheduled test execution."""

from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.cron import CronTrigger
from datetime import datetime

_scheduler: BackgroundScheduler = None


def start_scheduler():
    """Initialize and start the APScheduler background scheduler."""
    global _scheduler
    _scheduler = BackgroundScheduler()
    _scheduler.start()

    # Load existing jobs from DB
    from db.db_utils import get_all_scheduled_jobs, get_db_session
    with get_db_session() as db:
        jobs = get_all_scheduled_jobs(db)
        for job in jobs:
            if job.enabled:
                try:
                    _add_job_internal(job.id, job.tc_id, job.cron_expr)
                except Exception:
                    pass


def add_job(job_id: int, tc_id: int, cron_expr: str):
    """Add a new scheduled job."""
    if _scheduler:
        _add_job_internal(job_id, tc_id, cron_expr)


def remove_job(job_id: int):
    """Remove a scheduled job."""
    if _scheduler:
        job_name = f"scheduled_tc_{job_id}"
        try:
            _scheduler.remove_job(job_name)
        except Exception:
            pass


def toggle_job(job_id: int, enabled: bool):
    """Enable or disable a scheduled job."""
    if not _scheduler:
        return

    job_name = f"scheduled_tc_{job_id}"
    if enabled:
        from db.db_utils import get_db_session, get_scheduled_job
        with get_db_session() as db:
            job = get_scheduled_job(db, job_id)
            if job:
                _add_job_internal(job_id, job.tc_id, job.cron_expr)
    else:
        try:
            _scheduler.remove_job(job_name)
        except Exception:
            pass


def _add_job_internal(job_id: int, tc_id: int, cron_expr: str):
    """Internal: add job to APScheduler."""
    job_name = f"scheduled_tc_{job_id}"

    # Remove existing if any
    try:
        _scheduler.remove_job(job_name)
    except Exception:
        pass

    # Parse cron expression (5-field: min hour day month dow)
    parts = cron_expr.strip().split()
    if len(parts) >= 5:
        trigger = CronTrigger(
            minute=parts[0],
            hour=parts[1],
            day=parts[2],
            month=parts[3],
            day_of_week=parts[4],
        )
    else:
        trigger = CronTrigger(minute="*/30")

    _scheduler.add_job(
        _execute_scheduled,
        trigger=trigger,
        id=job_name,
        args=[job_id, tc_id],
        replace_existing=True,
    )


def _execute_scheduled(job_id: int, tc_id: int):
    """Execute a scheduled test case."""
    from agent.symbolic_runner import run_test_case
    from db.db_utils import get_db_session, get_scheduled_job

    run_test_case(tc_id, headless=True)

    # Update last_run timestamp
    with get_db_session() as db:
        job = get_scheduled_job(db, job_id)
        if job:
            job.last_run = datetime.utcnow()
