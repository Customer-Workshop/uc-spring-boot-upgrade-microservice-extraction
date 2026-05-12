"""Tests for db/models.py and db/db_utils.py"""

import json
import os
import sys

import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from db.models import (
    ApiEndpoint,
    Base,
    ExecutionLog,
    LocatorField,
    Project,
    ScheduledJob,
    TestCase,
    TestData,
    TestStep,
    TestSuite,
    User,
)
from db.db_utils import (
    create_project,
    create_suite,
    create_test_case,
    create_user,
    delete_project,
    delete_suite,
    delete_test_case,
    get_all_projects,
    get_all_suites,
    get_project,
    get_suite,
    get_test_case,
    get_user_by_username,
    save_steps,
    save_test_data,
    get_steps,
    get_test_data,
    create_endpoint,
    get_endpoints_for_tc,
    add_log,
    get_logs_for_tc,
    get_runs_for_tc,
    upsert_locator_field,
    get_locator_fields_grouped,
)


class TestProjectCRUD:
    def test_create_project(self, db_session):
        p = create_project(db_session, "Test Project", "A description")
        assert p.id is not None
        assert p.name == "Test Project"
        assert p.description == "A description"

    def test_get_all_projects(self, db_session):
        create_project(db_session, "P1")
        create_project(db_session, "P2")
        projects = get_all_projects(db_session)
        assert len(projects) == 2

    def test_delete_project(self, db_session):
        p = create_project(db_session, "To Delete")
        delete_project(db_session, p.id)
        assert get_project(db_session, p.id) is None


class TestSuiteCRUD:
    def test_create_suite(self, db_session):
        s = create_suite(db_session, "API Suite", "api", "desc")
        assert s.id is not None
        assert s.flow_type == "api"

    def test_suite_with_project(self, db_session):
        p = create_project(db_session, "Parent")
        s = create_suite(db_session, "Child Suite", "web", project_id=p.id)
        assert s.project_id == p.id

    def test_delete_suite_cascades(self, db_session):
        s = create_suite(db_session, "Suite1", "web")
        tc = create_test_case(db_session, s.id, "TC1", "web")
        delete_suite(db_session, s.id)
        assert get_test_case(db_session, tc.id) is None


class TestTestCaseCRUD:
    def test_create_test_case(self, db_session):
        s = create_suite(db_session, "Suite", "web")
        tc = create_test_case(db_session, s.id, "Login Test", "web", "http://localhost")
        assert tc.id is not None
        assert tc.status == "PENDING"
        assert tc.base_url == "http://localhost"

    def test_delete_test_case(self, db_session):
        s = create_suite(db_session, "Suite", "web")
        tc = create_test_case(db_session, s.id, "Test", "web")
        delete_test_case(db_session, tc.id)
        assert get_test_case(db_session, tc.id) is None


class TestStepsCRUD:
    def test_save_and_get_steps(self, db_session):
        s = create_suite(db_session, "Suite", "web")
        tc = create_test_case(db_session, s.id, "Test", "web")
        steps = [
            {"order": 0, "action": "navigate", "selector": "", "value": "http://example.com", "description": "Go to page"},
            {"order": 1, "action": "click", "selector": "#btn", "value": "", "description": "Click button"},
        ]
        save_steps(db_session, tc.id, steps)
        result = get_steps(db_session, tc.id)
        assert len(result) == 2
        assert result[0].action == "navigate"
        assert result[1].selector == "#btn"

    def test_save_replaces_existing(self, db_session):
        s = create_suite(db_session, "Suite", "web")
        tc = create_test_case(db_session, s.id, "Test", "web")
        save_steps(db_session, tc.id, [{"order": 0, "action": "click", "selector": "#a", "value": "", "description": ""}])
        save_steps(db_session, tc.id, [{"order": 0, "action": "fill", "selector": "#b", "value": "x", "description": ""}])
        result = get_steps(db_session, tc.id)
        assert len(result) == 1
        assert result[0].action == "fill"


class TestTestDataCRUD:
    def test_save_and_get_data(self, db_session):
        s = create_suite(db_session, "Suite", "web")
        tc = create_test_case(db_session, s.id, "Test", "web")
        rows = [
            {"row_index": 0, "data_json": '{"user": "alice"}'},
            {"row_index": 1, "data_json": '{"user": "bob"}'},
        ]
        save_test_data(db_session, tc.id, rows)
        result = get_test_data(db_session, tc.id)
        assert len(result) == 2
        assert result[0].data_dict == {"user": "alice"}


class TestEndpointCRUD:
    def test_create_endpoint(self, db_session):
        s = create_suite(db_session, "Suite", "api")
        tc = create_test_case(db_session, s.id, "API Test", "api")
        ep = create_endpoint(db_session, tc.id, method="POST", path="/pets", expected_status=201)
        assert ep.id is not None
        assert ep.method == "POST"

    def test_get_endpoints_for_tc(self, db_session):
        s = create_suite(db_session, "Suite", "api")
        tc = create_test_case(db_session, s.id, "API Test", "api")
        create_endpoint(db_session, tc.id, method="GET", path="/a")
        create_endpoint(db_session, tc.id, method="POST", path="/b")
        eps = get_endpoints_for_tc(db_session, tc.id)
        assert len(eps) == 2


class TestLogsCRUD:
    def test_add_and_get_logs(self, db_session):
        s = create_suite(db_session, "Suite", "web")
        tc = create_test_case(db_session, s.id, "Test", "web")
        add_log(db_session, tc.id, "run1", "INFO", "Test started", "EXECUTING", 0)
        add_log(db_session, tc.id, "run1", "INFO", "Summary: PASS=1, FAIL=0", "REPORTING", 100)
        logs = get_logs_for_tc(db_session, tc.id)
        assert len(logs) == 2

    def test_get_runs(self, db_session):
        s = create_suite(db_session, "Suite", "web")
        tc = create_test_case(db_session, s.id, "Test", "web")
        add_log(db_session, tc.id, "run1", "INFO", "msg1")
        add_log(db_session, tc.id, "run2", "INFO", "msg2")
        runs = get_runs_for_tc(db_session, tc.id)
        assert len(runs) == 2


class TestUserCRUD:
    def test_create_user(self, db_session):
        u = create_user(db_session, "admin", "hashed_pw", "admin")
        assert u.id is not None
        assert u.username == "admin"
        assert u.role == "admin"

    def test_get_user_by_username(self, db_session):
        create_user(db_session, "testuser", "pw", "editor")
        u = get_user_by_username(db_session, "testuser")
        assert u is not None
        assert u.username == "testuser"

    def test_nonexistent_user(self, db_session):
        u = get_user_by_username(db_session, "nobody")
        assert u is None


class TestLocatorCRUD:
    def test_upsert_new_field(self, db_session):
        f = upsert_locator_field(db_session, app_name="App", page_name="Login", field_name="Username", css_selector="#user")
        assert f.id is not None
        assert f.css_selector == "#user"

    def test_grouped(self, db_session):
        upsert_locator_field(db_session, app_name="App1", page_name="Page1", field_name="F1", css_selector="#f1")
        upsert_locator_field(db_session, app_name="App1", page_name="Page2", field_name="F2", css_selector="#f2")
        grouped = get_locator_fields_grouped(db_session)
        assert "App1" in grouped
        assert "Page1" in grouped["App1"]
        assert "Page2" in grouped["App1"]
