"""Execute Playwright test steps using sync API with auto-healing support."""

import logging
import os
import time
from typing import Optional

logger = logging.getLogger(__name__)


def execute_steps(steps: list[dict], base_url: str, headless: bool = True,
                  tc_id: int = 0, run_id: str = "", auto_heal: bool = True) -> dict:
    """Execute a list of test steps using Playwright sync API.

    Features:
    - Auto-healing: when a selector fails, tries alternative locator strategies
    - Smart retry: retries flaky actions up to 2 times
    - Screenshot on failure

    Returns dict: {passed: bool, error: str, duration_ms: float, healed: list}
    """
    from playwright.sync_api import sync_playwright

    start = time.time()
    error = ""
    healed_selectors = []

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
                    # Auto-healing: try to find the element with alternative selectors
                    if auto_heal and step.get("selector"):
                        healing_result = _attempt_healing(page, step, tc_id, run_id)
                        if healing_result:
                            healed_selectors.append(healing_result)
                            logger.info(f"Auto-healed step {step.get('order')}: "
                                       f"'{step['selector']}' -> '{healing_result['healed_selector']}'")
                            continue

                    # Smart retry (once) for timing issues
                    retry_ok = _smart_retry(page, step, tc_id, run_id)
                    if retry_ok:
                        continue

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
        "healed": healed_selectors,
    }


def _attempt_healing(page, step: dict, tc_id: int, run_id: str) -> Optional[dict]:
    """Attempt to auto-heal a broken selector."""
    try:
        from agent.auto_healer import auto_healer

        result = auto_healer.heal(page, step["selector"], action=step.get("action", "click"))
        if result.success:
            # Re-execute the step with the healed selector
            healed_step = step.copy()
            healed_step["selector"] = result.healed_selector
            try:
                _execute_step(page, healed_step, tc_id, run_id)
                return {
                    "step_order": step.get("order"),
                    "original_selector": result.original_selector,
                    "healed_selector": result.healed_selector,
                    "strategy": result.strategy_used,
                    "confidence": result.confidence,
                }
            except Exception:
                return None
    except Exception as e:
        logger.debug(f"Auto-healing error: {e}")
    return None


def _smart_retry(page, step: dict, tc_id: int, run_id: str, max_retries: int = 2) -> bool:
    """Smart retry for flaky actions (network delays, animations, etc.)."""
    action = step.get("action", "")
    if action in ("navigate", "wait", "screenshot"):
        return False

    for attempt in range(max_retries):
        try:
            page.wait_for_timeout(500 * (attempt + 1))
            _execute_step(page, step, tc_id, run_id)
            return True
        except Exception:
            continue
    return False


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

    elif action == "hover":
        page.locator(selector).hover(timeout=10000)

    elif action == "scroll":
        if selector:
            page.locator(selector).scroll_into_view_if_needed(timeout=10000)
        else:
            page.evaluate(f"window.scrollBy(0, {value or 300})")

    elif action == "clear":
        page.locator(selector).clear(timeout=10000)

    elif action == "check":
        page.locator(selector).check(timeout=10000)

    elif action == "uncheck":
        page.locator(selector).uncheck(timeout=10000)

    elif action == "focus":
        page.locator(selector).focus(timeout=10000)

    elif action == "assert_value":
        actual = page.locator(selector).input_value(timeout=10000)
        assert value == actual, f"Expected value '{value}', got '{actual}'"

    elif action == "assert_count":
        count = page.locator(selector).count()
        expected = int(value) if value.isdigit() else 1
        assert count == expected, f"Expected {expected} elements, found {count}"

    elif action == "assert_enabled":
        assert page.locator(selector).is_enabled(timeout=10000), f"Element '{selector}' is not enabled"

    elif action == "assert_disabled":
        assert not page.locator(selector).is_enabled(timeout=10000), f"Element '{selector}' is enabled"

    elif action == "upload":
        page.locator(selector).set_input_files(value, timeout=10000)

    elif action == "drag_drop":
        # value format: "target_selector"
        page.locator(selector).drag_to(page.locator(value), timeout=10000)


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
