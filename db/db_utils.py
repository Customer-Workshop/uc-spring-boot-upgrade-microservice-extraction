"""Database utilities: engine creation, session management, CRUD helpers."""

import os
from contextlib import contextmanager
from typing import Generator

from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker

from db.models import (
    ApiEndpoint,
    Base,
    Environment,
    ExecutionLog,
    LocatorField,
    Project,
    ScheduledJob,
    Screenshot,
    SymbolicLocator,
    TestCase,
    TestData,
    TestStep,
    TestSuite,
    User,
)

DATABASE_URL = os.environ.get("DATABASE_URL", "sqlite:///playwright_agent.db")

if DATABASE_URL.startswith("sqlite"):
    engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False}, echo=False)
else:
    engine = create_engine(DATABASE_URL, pool_size=10, max_overflow=20, echo=False)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


def init_db():
    """Create all tables."""
    Base.metadata.create_all(bind=engine)


def get_db() -> Generator[Session, None, None]:
    """FastAPI dependency: yields a DB session."""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@contextmanager
def get_db_session() -> Generator[Session, None, None]:
    """Context manager for DB session outside of FastAPI routes."""
    db = SessionLocal()
    try:
        yield db
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# --- Project CRUD ---

def create_project(db: Session, name: str, description: str = "") -> Project:
    project = Project(name=name, description=description)
    db.add(project)
    db.commit()
    db.refresh(project)
    return project


def get_all_projects(db: Session):
    return db.query(Project).order_by(Project.created_at.desc()).all()


def get_project(db: Session, project_id: int):
    return db.query(Project).filter(Project.id == project_id).first()


def delete_project(db: Session, project_id: int):
    project = get_project(db, project_id)
    if project:
        db.delete(project)
        db.commit()


# --- Suite CRUD ---

def create_suite(db: Session, name: str, flow_type: str, description: str = "", project_id: int = None) -> TestSuite:
    suite = TestSuite(name=name, flow_type=flow_type, description=description, project_id=project_id)
    db.add(suite)
    db.commit()
    db.refresh(suite)
    return suite


def get_all_suites(db: Session):
    return db.query(TestSuite).order_by(TestSuite.created_at.desc()).all()


def get_suite(db: Session, suite_id: int):
    return db.query(TestSuite).filter(TestSuite.id == suite_id).first()


def delete_suite(db: Session, suite_id: int):
    suite = get_suite(db, suite_id)
    if suite:
        db.delete(suite)
        db.commit()


# --- TestCase CRUD ---

def create_test_case(db: Session, suite_id: int, name: str, flow_type: str, base_url: str = "") -> TestCase:
    tc = TestCase(suite_id=suite_id, name=name, flow_type=flow_type, base_url=base_url)
    db.add(tc)
    db.commit()
    db.refresh(tc)
    return tc


def get_test_case(db: Session, tc_id: int):
    return db.query(TestCase).filter(TestCase.id == tc_id).first()


def get_all_test_cases(db: Session):
    return db.query(TestCase).order_by(TestCase.created_at.desc()).all()


def delete_test_case(db: Session, tc_id: int):
    tc = get_test_case(db, tc_id)
    if tc:
        db.delete(tc)
        db.commit()


def update_tc_status(db: Session, tc_id: int, status: str):
    tc = get_test_case(db, tc_id)
    if tc:
        tc.status = status
        db.commit()


# --- TestStep CRUD ---

def save_steps(db: Session, tc_id: int, steps: list):
    db.query(TestStep).filter(TestStep.test_case_id == tc_id).delete()
    for s in steps:
        step = TestStep(
            test_case_id=tc_id,
            order=s.get("order", 0),
            action=s.get("action", ""),
            selector=s.get("selector", ""),
            value=s.get("value", ""),
            description=s.get("description", ""),
        )
        db.add(step)
    db.commit()


def get_steps(db: Session, tc_id: int):
    return db.query(TestStep).filter(TestStep.test_case_id == tc_id).order_by(TestStep.order).all()


# --- TestData CRUD ---

def save_test_data(db: Session, tc_id: int, data_rows: list):
    db.query(TestData).filter(TestData.test_case_id == tc_id).delete()
    for row in data_rows:
        td = TestData(
            test_case_id=tc_id,
            row_index=row.get("row_index", 0),
            data_json=row.get("data_json", "{}") if isinstance(row.get("data_json"), str) else __import__("json").dumps(row.get("data_json", {})),
        )
        db.add(td)
    db.commit()


def get_test_data(db: Session, tc_id: int):
    return db.query(TestData).filter(TestData.test_case_id == tc_id).order_by(TestData.row_index).all()


# --- ApiEndpoint CRUD ---

def create_endpoint(db: Session, tc_id: int, **kwargs) -> ApiEndpoint:
    ep = ApiEndpoint(test_case_id=tc_id, **kwargs)
    db.add(ep)
    db.commit()
    db.refresh(ep)
    return ep


def get_endpoint(db: Session, ep_id: int):
    return db.query(ApiEndpoint).filter(ApiEndpoint.id == ep_id).first()


def get_endpoints_for_tc(db: Session, tc_id: int):
    return db.query(ApiEndpoint).filter(ApiEndpoint.test_case_id == tc_id).all()


def update_endpoint(db: Session, ep_id: int, **kwargs):
    ep = get_endpoint(db, ep_id)
    if ep:
        for k, v in kwargs.items():
            if hasattr(ep, k):
                setattr(ep, k, v)
        db.commit()
        db.refresh(ep)
    return ep


# --- ExecutionLog CRUD ---

def add_log(db: Session, tc_id: int, run_id: str, level: str, message: str,
            agent_state: str = None, duration_ms: float = None):
    log = ExecutionLog(
        test_case_id=tc_id,
        run_id=run_id,
        level=level,
        message=message,
        agent_state=agent_state,
        duration_ms=duration_ms,
    )
    db.add(log)
    db.commit()
    return log


def get_logs_for_tc(db: Session, tc_id: int):
    return db.query(ExecutionLog).filter(ExecutionLog.test_case_id == tc_id).order_by(ExecutionLog.timestamp).all()


def get_logs_for_run(db: Session, run_id: str):
    return db.query(ExecutionLog).filter(ExecutionLog.run_id == run_id).order_by(ExecutionLog.timestamp).all()


def get_runs_for_tc(db: Session, tc_id: int):
    """Get distinct run_ids for a test case."""
    rows = db.query(ExecutionLog.run_id).filter(ExecutionLog.test_case_id == tc_id).distinct().all()
    return [r[0] for r in rows]


def delete_run_logs(db: Session, run_id: str, tc_id: int = None):
    q = db.query(ExecutionLog).filter(ExecutionLog.run_id == run_id)
    if tc_id:
        q = q.filter(ExecutionLog.test_case_id == tc_id)
    q.delete()
    db.query(Screenshot).filter(Screenshot.run_id == run_id).delete()
    db.commit()


# --- Screenshot CRUD ---

def add_screenshot(db: Session, tc_id: int, run_id: str, step_order: int, file_path: str):
    ss = Screenshot(test_case_id=tc_id, run_id=run_id, step_order=step_order, file_path=file_path)
    db.add(ss)
    db.commit()
    return ss


def get_screenshots_for_run(db: Session, run_id: str):
    return db.query(Screenshot).filter(Screenshot.run_id == run_id).order_by(Screenshot.step_order).all()


# --- LocatorField CRUD ---

def upsert_locator_field(db: Session, **kwargs) -> LocatorField:
    field_id = kwargs.pop("id", None)
    if field_id:
        field = db.query(LocatorField).filter(LocatorField.id == field_id).first()
        if field:
            for k, v in kwargs.items():
                setattr(field, k, v)
            db.commit()
            db.refresh(field)
            return field
    field = LocatorField(**kwargs)
    db.add(field)
    db.commit()
    db.refresh(field)
    return field


def get_locator_field(db: Session, field_id: int):
    return db.query(LocatorField).filter(LocatorField.id == field_id).first()


def delete_locator_field(db: Session, field_id: int):
    field = get_locator_field(db, field_id)
    if field:
        db.delete(field)
        db.commit()


def get_locator_fields_grouped(db: Session):
    fields = db.query(LocatorField).order_by(LocatorField.app_name, LocatorField.page_name).all()
    grouped = {}
    for f in fields:
        app = f.app_name or "Default"
        page = f.page_name or "Default"
        grouped.setdefault(app, {}).setdefault(page, []).append(f)
    return grouped


# --- SymbolicLocator CRUD (legacy) ---

def upsert_symbolic_locator(db: Session, abstract_name: str, selector: str, page_context: str = ""):
    loc = db.query(SymbolicLocator).filter(SymbolicLocator.abstract_name == abstract_name).first()
    if loc:
        loc.selector = selector
        loc.page_context = page_context
    else:
        loc = SymbolicLocator(abstract_name=abstract_name, selector=selector, page_context=page_context)
        db.add(loc)
    db.commit()
    return loc


# --- ScheduledJob CRUD ---

def create_scheduled_job(db: Session, tc_id: int, cron_expr: str, notify_on_failure: str = "") -> ScheduledJob:
    job = ScheduledJob(tc_id=tc_id, cron_expr=cron_expr, notify_on_failure=notify_on_failure)
    db.add(job)
    db.commit()
    db.refresh(job)
    return job


def get_all_scheduled_jobs(db: Session):
    return db.query(ScheduledJob).all()


def get_scheduled_job(db: Session, job_id: int):
    return db.query(ScheduledJob).filter(ScheduledJob.id == job_id).first()


def delete_scheduled_job(db: Session, job_id: int):
    job = get_scheduled_job(db, job_id)
    if job:
        db.delete(job)
        db.commit()


# --- Environment CRUD ---

def create_environment(db: Session, name: str, base_url: str, auth_headers: str = "{}") -> Environment:
    env = Environment(name=name, base_url=base_url, auth_headers=auth_headers)
    db.add(env)
    db.commit()
    db.refresh(env)
    return env


def get_all_environments(db: Session):
    return db.query(Environment).all()


def get_environment(db: Session, env_id: int):
    return db.query(Environment).filter(Environment.id == env_id).first()


# --- User CRUD ---

def create_user(db: Session, username: str, password_hash: str, role: str = "editor") -> User:
    user = User(username=username, password_hash=password_hash, role=role)
    db.add(user)
    db.commit()
    db.refresh(user)
    return user


def get_user_by_username(db: Session, username: str):
    return db.query(User).filter(User.username == username).first()
