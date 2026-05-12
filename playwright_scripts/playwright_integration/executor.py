"""Execute Playwright test steps using sync API."""

import os
import time
from typing import Optional


def execute_steps(steps: list[dict], base_url: str, headless: bool = True,
                  tc_id: int = 0, run_id: str = "") -> dict:
    """Execute a list of test steps using Playwright sync API.

    Returns dict: {passed: bool, error: str, duration_ms: float}
    """
    from playwright.sync_api import sync_playwright

    start = time.time()
    error = ""

    try:
        with sync_playwright() as p:
            browser = p.chromium.launch(headless=headless)
            context = browser.new_context()
            page = context.new_page()

            if base_url:
                page.goto(base_url, wait_until="domcontentloaded", timeout=30000)

            for step in steps:
                try:
                    _execute_step(page, step, tc_id, run_id)
                except Exception as e:
                    error = f"Step {step.get('order', '?')} ({step.get('action', '')}) failed: {str(e)}"
                    _capture_screenshot(page, tc_id, run_id, step.get("order", 0))
                    break

            browser.close()

    except Exception as e:
        error = f"Browser error: {str(e)}"

    duration_ms = (time.time() - start) * 1000
    return {
        "passed": error == "",
        "error": error,
        "duration_ms": duration_ms,
    }


def _execute_step(page, step: dict, tc_id: int = 0, run_id: str = ""):
    """Execute a single test step on the page."""
    action = step.get("action", "").lower()
    selector = step.get("selector", "")
    value = step.get("value", "")

    if action == "navigate":
        page.goto(value, wait_until="domcontentloaded", timeout=30000)

    elif action == "click":
        page.locator(selector).click(timeout=10000)

    elif action == "dblclick":
        page.locator(selector).dblclick(timeout=10000)

    elif action == "fill":
        page.locator(selector).fill(value, timeout=10000)

    elif action == "type":
        page.locator(selector).type(value, timeout=10000)

    elif action == "press":
        page.locator(selector).press(value, timeout=10000)

    elif action == "select":
        page.locator(selector).select_option(value, timeout=10000)

    elif action == "assert_text":
        locator = page.locator(selector)
        actual = locator.text_content(timeout=10000)
        assert value in (actual or ""), f"Expected '{value}' in text, got '{actual}'"

    elif action == "assert_visible":
        page.locator(selector).wait_for(state="visible", timeout=10000)

    elif action == "assert_url":
        assert value in page.url, f"Expected URL to contain '{value}', got '{page.url}'"

    elif action == "wait":
        ms = int(value) if value.isdigit() else 1000
        page.wait_for_timeout(ms)

    elif action == "screenshot":
        _capture_screenshot(page, tc_id, run_id, step.get("order", 0))


def _capture_screenshot(page, tc_id: int, run_id: str, step_order: int):
    """Capture a screenshot and save to reports/screenshots/."""
    screenshots_dir = os.path.join(os.path.dirname(os.path.dirname(__file__)), "reports", "screenshots")
    os.makedirs(screenshots_dir, exist_ok=True)

    filename = f"tc{tc_id}_run{run_id}_step{step_order}_{int(time.time())}.png"
    filepath = os.path.join(screenshots_dir, filename)

    try:
        page.screenshot(path=filepath)
        from db.db_utils import add_screenshot, get_db_session
        with get_db_session() as db:
            add_screenshot(db, tc_id, run_id, step_order, filepath)
    except Exception:
        pass
