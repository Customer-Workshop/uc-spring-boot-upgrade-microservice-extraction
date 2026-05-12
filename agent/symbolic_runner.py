"""Dispatches web/API test execution, handles performance thresholds."""

import json
import re
import time
import uuid
from datetime import datetime

from db.db_utils import (
    add_log,
    get_db_session,
    get_endpoints_for_tc,
    get_steps,
    get_test_case,
    get_test_data,
    update_endpoint,
    update_tc_status,
)


def run_test_case(tc_id: int, headless: bool = True) -> str:
    """Dispatch to web or API test execution. Returns run_id."""
    run_id = str(uuid.uuid4())[:8]

    with get_db_session() as db:
        tc = get_test_case(db, tc_id)
        if not tc:
            return run_id

        update_tc_status(db, tc_id, "RUNNING")
        add_log(db, tc_id, run_id, "INFO", f"Starting {tc.flow_type} test: {tc.name}", "EXECUTING")

    try:
        if tc.flow_type == "api":
            _run_api_test(tc_id, run_id)
        else:
            _run_web_test(tc_id, run_id, headless)
    except Exception as e:
        with get_db_session() as db:
            add_log(db, tc_id, run_id, "ERROR", f"Execution error: {str(e)}", "ERROR")
            update_tc_status(db, tc_id, "FAIL")

    return run_id


def _run_web_test(tc_id: int, run_id: str, headless: bool):
    """Execute web test case with data-driven rows."""
    from playwright_integration.executor import execute_steps

    with get_db_session() as db:
        tc = get_test_case(db, tc_id)
        steps = get_steps(db, tc_id)
        data_rows = get_test_data(db, tc_id)

    if not steps:
        with get_db_session() as db:
            add_log(db, tc_id, run_id, "WARN", "No test steps defined", "EXECUTING")
            update_tc_status(db, tc_id, "PASS")
        return

    step_dicts = [{"order": s.order, "action": s.action, "selector": s.selector, "value": s.value, "description": s.description} for s in steps]
    rows = [r.data_dict for r in data_rows] if data_rows else [{}]

    total_pass = 0
    total_fail = 0

    for row_idx, row_data in enumerate(rows):
        resolved_steps = _substitute_variables(step_dicts, row_data)

        with get_db_session() as db:
            add_log(db, tc_id, run_id, "INFO", f"Running data row {row_idx + 1}/{len(rows)}", "EXECUTING")

        try:
            result = execute_steps(resolved_steps, tc.base_url, headless=headless, tc_id=tc_id, run_id=run_id)
            if result.get("passed"):
                total_pass += 1
            else:
                total_fail += 1
                with get_db_session() as db:
                    add_log(db, tc_id, run_id, "ERROR", f"Row {row_idx + 1} failed: {result.get('error', '')}", "EXECUTING")
        except Exception as e:
            total_fail += 1
            with get_db_session() as db:
                add_log(db, tc_id, run_id, "ERROR", f"Row {row_idx + 1} exception: {str(e)}", "EXECUTING")

    status = "PASS" if total_fail == 0 else "FAIL"
    with get_db_session() as db:
        add_log(db, tc_id, run_id, "INFO", f"Summary: PASS={total_pass}, FAIL={total_fail}", "REPORTING")
        update_tc_status(db, tc_id, status)


def _run_api_test(tc_id: int, run_id: str):
    """Execute API test case: iterate endpoints, check status and performance."""
    from api_integration.executor import execute_endpoint

    with get_db_session() as db:
        tc = get_test_case(db, tc_id)
        endpoints = get_endpoints_for_tc(db, tc_id)

    if not endpoints:
        with get_db_session() as db:
            add_log(db, tc_id, run_id, "WARN", "No API endpoints defined", "EXECUTING")
            update_tc_status(db, tc_id, "PASS")
        return

    total_pass = 0
    total_fail = 0

    for ep in endpoints:
        ep_dict = {
            "id": ep.id,
            "method": ep.method,
            "path": ep.path,
            "summary": ep.summary,
            "base_url": tc.base_url,
            "request_headers": ep.request_headers,
            "request_params": ep.request_params,
            "request_body": ep.request_body,
            "expected_status": ep.expected_status,
        }

        start_time = time.time()
        try:
            result = execute_endpoint(ep_dict)
            duration_ms = (time.time() - start_time) * 1000
        except Exception as e:
            duration_ms = (time.time() - start_time) * 1000
            with get_db_session() as db:
                add_log(db, tc_id, run_id, "ERROR", f"{ep.method} {ep.path} → Exception: {str(e)}", "EXECUTING", duration_ms)
            total_fail += 1
            continue

        actual_status = result.get("actual_status", 0)
        passed = actual_status == ep.expected_status

        # Performance threshold check
        headers = {}
        try:
            headers = json.loads(ep.request_headers) if ep.request_headers else {}
        except (json.JSONDecodeError, TypeError):
            pass

        threshold = headers.get("X-Perf-Threshold-Ms")
        if threshold and passed:
            try:
                if duration_ms > float(threshold):
                    passed = False
                    with get_db_session() as db:
                        add_log(db, tc_id, run_id, "WARN",
                                f"[PERF] {ep.method} {ep.path} took {duration_ms:.0f}ms > {threshold}ms threshold",
                                "EXECUTING", duration_ms)
            except (ValueError, TypeError):
                pass

        with get_db_session() as db:
            update_endpoint(db, ep.id, actual_status=actual_status, response_body=result.get("response_body", ""), passed=passed)
            level = "INFO" if passed else "ERROR"
            add_log(db, tc_id, run_id, level, f"{ep.method} {ep.path} → {actual_status} (expected {ep.expected_status})", "EXECUTING", duration_ms)

        if passed:
            total_pass += 1
        else:
            total_fail += 1

    status = "PASS" if total_fail == 0 else "FAIL"
    with get_db_session() as db:
        add_log(db, tc_id, run_id, "INFO", f"Summary: PASS={total_pass}, FAIL={total_fail}", "REPORTING")
        update_tc_status(db, tc_id, status)


def _substitute_variables(steps: list[dict], data: dict) -> list[dict]:
    """Replace {{variable}} in step values with data row values."""
    if not data:
        return steps

    resolved = []
    for step in steps:
        s = dict(step)
        value = s.get("value", "")
        if value and "{{" in value:
            for key, val in data.items():
                value = value.replace("{{" + key + "}}", str(val))
            s["value"] = value
        resolved.append(s)
    return resolved
