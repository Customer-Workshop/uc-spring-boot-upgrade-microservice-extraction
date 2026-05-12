"""SQLAlchemy ORM models for the Playwright Test Automation Platform."""

import json
from datetime import datetime

from sqlalchemy import (
    Boolean,
    Column,
    DateTime,
    Float,
    ForeignKey,
    Integer,
    String,
    Text,
    create_engine,
)
from sqlalchemy.orm import DeclarativeBase, relationship


class Base(DeclarativeBase):
    pass


class User(Base):
    """User model for authentication & multi-user support."""

    __tablename__ = "users"

    id = Column(Integer, primary_key=True, autoincrement=True)
    username = Column(String(100), unique=True, nullable=False)
    password_hash = Column(String(255), nullable=False)
    role = Column(String(20), nullable=False, default="editor")  # admin/viewer/editor
    created_at = Column(DateTime, default=datetime.utcnow)


class Project(Base):
    __tablename__ = "projects"

    id = Column(Integer, primary_key=True, autoincrement=True)
    name = Column(String(200), unique=True, nullable=False)
    description = Column(Text, default="")
    created_at = Column(DateTime, default=datetime.utcnow)

    suites = relationship("TestSuite", back_populates="project", cascade="all, delete-orphan")


class TestSuite(Base):
    __tablename__ = "test_suites"

    id = Column(Integer, primary_key=True, autoincrement=True)
    project_id = Column(Integer, ForeignKey("projects.id"), nullable=True)
    name = Column(String(200), unique=True, nullable=False)
    flow_type = Column(String(10), nullable=False, default="web")  # web|api
    description = Column(Text, default="")
    created_at = Column(DateTime, default=datetime.utcnow)

    project = relationship("Project", back_populates="suites")
    test_cases = relationship("TestCase", back_populates="suite", cascade="all, delete-orphan")


class TestCase(Base):
    __tablename__ = "test_cases"

    id = Column(Integer, primary_key=True, autoincrement=True)
    suite_id = Column(Integer, ForeignKey("test_suites.id"), nullable=False)
    name = Column(String(300), nullable=False)
    flow_type = Column(String(10), nullable=False, default="web")
    base_url = Column(String(500), default="")
    headless = Column(Boolean, default=True)
    status = Column(String(20), default="PENDING")  # PENDING|RUNNING|PASS|FAIL
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    suite = relationship("TestSuite", back_populates="test_cases")
    steps = relationship("TestStep", back_populates="test_case", cascade="all, delete-orphan")
    test_data = relationship("TestData", back_populates="test_case", cascade="all, delete-orphan")
    endpoints = relationship("ApiEndpoint", back_populates="test_case", cascade="all, delete-orphan")
    logs = relationship("ExecutionLog", back_populates="test_case", cascade="all, delete-orphan")
    screenshots = relationship("Screenshot", back_populates="test_case", cascade="all, delete-orphan")


class TestStep(Base):
    __tablename__ = "test_steps"

    id = Column(Integer, primary_key=True, autoincrement=True)
    test_case_id = Column(Integer, ForeignKey("test_cases.id"), nullable=False)
    order = Column(Integer, nullable=False, default=0)
    action = Column(String(50), nullable=False)
    selector = Column(String(500), default="")
    value = Column(Text, default="")
    description = Column(String(500), default="")

    test_case = relationship("TestCase", back_populates="steps")


class TestData(Base):
    __tablename__ = "test_data"

    id = Column(Integer, primary_key=True, autoincrement=True)
    test_case_id = Column(Integer, ForeignKey("test_cases.id"), nullable=False)
    row_index = Column(Integer, default=0)
    data_json = Column(Text, default="{}")

    test_case = relationship("TestCase", back_populates="test_data")

    @property
    def data_dict(self):
        try:
            return json.loads(self.data_json) if self.data_json else {}
        except (json.JSONDecodeError, TypeError):
            return {}


class ApiEndpoint(Base):
    __tablename__ = "api_endpoints"

    id = Column(Integer, primary_key=True, autoincrement=True)
    test_case_id = Column(Integer, ForeignKey("test_cases.id"), nullable=False)
    method = Column(String(10), nullable=False, default="GET")
    path = Column(String(500), nullable=False, default="/")
    summary = Column(String(500), default="")
    request_headers = Column(Text, default="{}")
    request_params = Column(Text, default="{}")
    request_body = Column(Text, default="{}")
    expected_status = Column(Integer, default=200)
    actual_status = Column(Integer, nullable=True)
    response_body = Column(Text, default="")
    passed = Column(Boolean, nullable=True)
    response_schema = Column(Text, default="")

    test_case = relationship("TestCase", back_populates="endpoints")

    @property
    def headers_dict(self):
        try:
            return json.loads(self.request_headers) if self.request_headers else {}
        except (json.JSONDecodeError, TypeError):
            return {}

    @property
    def params_dict(self):
        try:
            return json.loads(self.request_params) if self.request_params else {}
        except (json.JSONDecodeError, TypeError):
            return {}

    @property
    def body_dict(self):
        try:
            return json.loads(self.request_body) if self.request_body else {}
        except (json.JSONDecodeError, TypeError):
            return {}


class ExecutionLog(Base):
    __tablename__ = "execution_logs"

    id = Column(Integer, primary_key=True, autoincrement=True)
    test_case_id = Column(Integer, ForeignKey("test_cases.id"), nullable=False)
    run_id = Column(String(50), nullable=False)
    level = Column(String(10), default="INFO")  # INFO|WARN|ERROR
    message = Column(Text, default="")
    agent_state = Column(String(50), nullable=True)
    duration_ms = Column(Float, nullable=True)
    timestamp = Column(DateTime, default=datetime.utcnow)

    test_case = relationship("TestCase", back_populates="logs")


class Screenshot(Base):
    __tablename__ = "screenshots"

    id = Column(Integer, primary_key=True, autoincrement=True)
    test_case_id = Column(Integer, ForeignKey("test_cases.id"), nullable=False)
    run_id = Column(String(50), nullable=False)
    step_order = Column(Integer, default=0)
    file_path = Column(String(500), nullable=False)
    captured_at = Column(DateTime, default=datetime.utcnow)

    test_case = relationship("TestCase", back_populates="screenshots")


class LocatorField(Base):
    __tablename__ = "locator_fields"

    id = Column(Integer, primary_key=True, autoincrement=True)
    app_name = Column(String(200), default="")
    page_name = Column(String(200), default="")
    field_name = Column(String(200), default="")
    css_selector = Column(String(500), default="")
    xpath = Column(String(500), default="")
    id_attr = Column(String(200), default="")
    name_attr = Column(String(200), default="")
    text_content = Column(String(500), default="")
    role_attr = Column(String(100), default="")
    test_id = Column(String(200), default="")
    placeholder = Column(String(300), default="")
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


class SymbolicLocator(Base):
    """Legacy locator table - kept for backward compatibility."""

    __tablename__ = "symbolic_locators"

    id = Column(Integer, primary_key=True, autoincrement=True)
    abstract_name = Column(String(200), unique=True, nullable=False)
    selector = Column(String(500), default="")
    page_context = Column(String(200), default="")
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


class ScheduledJob(Base):
    """Scheduled test execution jobs (APScheduler integration)."""

    __tablename__ = "scheduled_jobs"

    id = Column(Integer, primary_key=True, autoincrement=True)
    tc_id = Column(Integer, ForeignKey("test_cases.id"), nullable=False)
    cron_expr = Column(String(100), nullable=False)
    enabled = Column(Boolean, default=True)
    last_run = Column(DateTime, nullable=True)
    next_run = Column(DateTime, nullable=True)
    notify_on_failure = Column(String(500), default="")  # email or webhook URL
    created_at = Column(DateTime, default=datetime.utcnow)

    test_case = relationship("TestCase")


class Environment(Base):
    """Environment configuration for multi-env test execution."""

    __tablename__ = "environments"

    id = Column(Integer, primary_key=True, autoincrement=True)
    name = Column(String(100), unique=True, nullable=False)
    base_url = Column(String(500), nullable=False)
    auth_headers = Column(Text, default="{}")
    created_at = Column(DateTime, default=datetime.utcnow)
