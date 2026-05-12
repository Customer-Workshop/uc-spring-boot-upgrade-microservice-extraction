"""Tests for playwright_integration/recorder.py — enhanced recording with POM generation."""

import os
import tempfile
import pytest

from playwright_integration.recorder import (
    RecordingSession,
    parse_codegen_script,
    generate_pom_from_steps,
    get_recording_status,
    _infer_page_name,
    _infer_element_name,
    _classify_selector,
    _generate_page_class,
)


class TestRecordingSession:
    def test_session_init(self):
        session = RecordingSession(tc_id=5, base_url="https://example.com", user_id="user1")
        assert session.tc_id == 5
        assert session.base_url == "https://example.com"
        assert session.user_id == "user1"
        assert session.status == "idle"
        assert "user1" in session.output_file

    def test_session_not_alive_before_start(self):
        session = RecordingSession(tc_id=1, base_url="http://test.com")
        assert session.is_alive() is False

    def test_session_duration(self):
        session = RecordingSession(tc_id=1, base_url="http://test.com")
        import time
        session.started_at = time.time() - 10
        assert session.duration_seconds >= 9


class TestParseCodegenScript:
    def test_parse_nonexistent_file(self):
        steps = parse_codegen_script("/nonexistent/path.py")
        assert steps == []

    def test_parse_goto(self):
        with tempfile.NamedTemporaryFile(mode='w', suffix='.py', delete=False) as f:
            f.write('page.goto("https://example.com/login")\n')
            f.flush()
            steps = parse_codegen_script(f.name)
        os.unlink(f.name)
        assert len(steps) == 1
        assert steps[0]["action"] == "navigate"
        assert steps[0]["value"] == "https://example.com/login"

    def test_parse_locator_click(self):
        with tempfile.NamedTemporaryFile(mode='w', suffix='.py', delete=False) as f:
            f.write('page.locator("#submit-btn").click()\n')
            f.flush()
            steps = parse_codegen_script(f.name)
        os.unlink(f.name)
        assert len(steps) == 1
        assert steps[0]["action"] == "click"
        assert steps[0]["selector"] == "#submit-btn"

    def test_parse_locator_fill(self):
        with tempfile.NamedTemporaryFile(mode='w', suffix='.py', delete=False) as f:
            f.write('page.locator("#email").fill("test@example.com")\n')
            f.flush()
            steps = parse_codegen_script(f.name)
        os.unlink(f.name)
        assert len(steps) == 1
        assert steps[0]["action"] == "fill"
        assert steps[0]["selector"] == "#email"
        assert steps[0]["value"] == "test@example.com"

    def test_parse_hover(self):
        with tempfile.NamedTemporaryFile(mode='w', suffix='.py', delete=False) as f:
            f.write('page.locator(".menu-item").hover()\n')
            f.flush()
            steps = parse_codegen_script(f.name)
        os.unlink(f.name)
        assert len(steps) == 1
        assert steps[0]["action"] == "hover"

    def test_parse_select_option(self):
        with tempfile.NamedTemporaryFile(mode='w', suffix='.py', delete=False) as f:
            f.write('page.locator("#country").select_option("US")\n')
            f.flush()
            steps = parse_codegen_script(f.name)
        os.unlink(f.name)
        assert len(steps) == 1
        assert steps[0]["action"] == "select"
        assert steps[0]["value"] == "US"

    def test_parse_wait(self):
        with tempfile.NamedTemporaryFile(mode='w', suffix='.py', delete=False) as f:
            f.write('page.wait_for_timeout(3000)\n')
            f.flush()
            steps = parse_codegen_script(f.name)
        os.unlink(f.name)
        assert len(steps) == 1
        assert steps[0]["action"] == "wait"
        assert steps[0]["value"] == "3000"

    def test_parse_multiple_steps(self):
        with tempfile.NamedTemporaryFile(mode='w', suffix='.py', delete=False) as f:
            f.write('page.goto("https://app.com")\n')
            f.write('page.locator("#user").fill("admin")\n')
            f.write('page.locator("#pass").fill("secret")\n')
            f.write('page.locator(".login-btn").click()\n')
            f.flush()
            steps = parse_codegen_script(f.name)
        os.unlink(f.name)
        assert len(steps) == 4
        assert steps[0]["order"] == 0
        assert steps[3]["order"] == 3


class TestInferPageName:
    def test_home_page(self):
        assert _infer_page_name("https://example.com/") == "HomePage"
        assert _infer_page_name("https://example.com") == "HomePage"

    def test_login_page(self):
        assert _infer_page_name("https://app.com/login") == "LoginPage"

    def test_dashboard_page(self):
        assert _infer_page_name("https://app.com/admin/dashboard") == "DashboardPage"

    def test_hyphenated_page(self):
        result = _infer_page_name("https://app.com/user-settings")
        assert "Page" in result


class TestInferElementName:
    def test_data_testid(self):
        assert _infer_element_name('[data-testid="submit-form"]') == "submit_form"

    def test_id_selector(self):
        assert _infer_element_name("#login-button") == "login_button"

    def test_name_attr(self):
        assert _infer_element_name('[name="email"]') == "email_field"

    def test_class_selector(self):
        assert _infer_element_name(".nav-link") == "nav_link"

    def test_aria_label(self):
        assert _infer_element_name('[aria-label="Close dialog"]') == "close_dialog"

    def test_placeholder(self):
        assert _infer_element_name('[placeholder="Enter email"]') == "enter_email_input"


class TestClassifySelector:
    def test_data_testid(self):
        assert _classify_selector('[data-testid="btn"]') == "data-testid"

    def test_id(self):
        assert _classify_selector("#myid") == "id"

    def test_class(self):
        assert _classify_selector(".myclass") == "class"

    def test_aria(self):
        assert _classify_selector('[aria-label="x"]') == "aria"

    def test_xpath(self):
        assert _classify_selector("//div[@id='x']") == "xpath"

    def test_text(self):
        assert _classify_selector('text="Click me"') == "text"

    def test_css_fallback(self):
        assert _classify_selector("div > span.item") == "css"


class TestGeneratePomFromSteps:
    def test_basic_pom(self):
        steps = [
            {"action": "navigate", "selector": "", "value": "https://app.com/login"},
            {"action": "fill", "selector": "#username", "value": "admin"},
            {"action": "fill", "selector": "#password", "value": "secret"},
            {"action": "click", "selector": ".btn-submit", "value": ""},
        ]
        pom = generate_pom_from_steps(steps, "https://app.com")
        assert pom["total_pages"] >= 1
        assert pom["total_elements"] == 3  # username, password, btn-submit
        assert "LoginPage" in pom["pom_code"]

    def test_multi_page_pom(self):
        steps = [
            {"action": "navigate", "selector": "", "value": "https://app.com/"},
            {"action": "click", "selector": "#nav-login", "value": ""},
            {"action": "navigate", "selector": "", "value": "https://app.com/login"},
            {"action": "fill", "selector": "#email", "value": "test@test.com"},
        ]
        pom = generate_pom_from_steps(steps)
        assert pom["total_pages"] == 2

    def test_empty_steps(self):
        pom = generate_pom_from_steps([])
        assert pom["total_pages"] == 0
        assert pom["total_elements"] == 0

    def test_pom_code_has_class(self):
        steps = [
            {"action": "navigate", "selector": "", "value": "https://app.com/dashboard"},
            {"action": "click", "selector": "#logout", "value": ""},
        ]
        pom = generate_pom_from_steps(steps)
        code = pom["pom_code"].get("DashboardPage", "")
        assert "class DashboardPage" in code
        assert "def __init__(self, page: Page)" in code
        assert "logout" in code


class TestGeneratePageClass:
    def test_generates_properties(self):
        elements = [
            {"name": "submit_btn", "selector": "#submit", "action_used": "click", "locator_type": "id"},
            {"name": "email_input", "selector": "#email", "action_used": "fill", "locator_type": "id"},
        ]
        code = _generate_page_class("LoginPage", elements)
        assert "class LoginPage:" in code
        assert "def submit_btn(self)" in code
        assert "def email_input(self)" in code
        assert "def click_submit_btn(self)" in code
        assert "def fill_email_input(self, value: str)" in code


class TestRecordingStatus:
    def test_idle_status(self):
        status = get_recording_status("nonexistent_user_xyz")
        assert status["status"] == "idle"
        assert status["recording"] is False
