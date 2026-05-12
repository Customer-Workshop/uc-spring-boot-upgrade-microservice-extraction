"""Tests for app/routes.py - route logic and run_status computation."""

import json
import os
import sys
from unittest.mock import MagicMock, patch

import pytest

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.routes import _compute_run_status


class TestComputeRunStatus:
    def test_pass_with_zero_failures(self):
        logs = [
            MagicMock(message="Starting API test", level="INFO"),
            MagicMock(message="GET /pets -> 200", level="INFO"),
            MagicMock(message="Summary: PASS=3, FAIL=0", level="INFO"),
        ]
        assert _compute_run_status(logs) == "PASS"

    def test_fail_with_nonzero_failures(self):
        logs = [
            MagicMock(message="Starting API test", level="INFO"),
            MagicMock(message="GET /pets -> 500", level="ERROR"),
            MagicMock(message="Summary: PASS=2, FAIL=1", level="INFO"),
        ]
        assert _compute_run_status(logs) == "FAIL"

    def test_fail_on_error_level_fallback(self):
        logs = [
            MagicMock(message="Starting test", level="INFO"),
            MagicMock(message="Connection refused", level="ERROR"),
        ]
        assert _compute_run_status(logs) == "FAIL"

    def test_pass_with_no_errors(self):
        logs = [
            MagicMock(message="Starting test", level="INFO"),
            MagicMock(message="Completed successfully", level="INFO"),
        ]
        assert _compute_run_status(logs) == "PASS"

    def test_empty_logs(self):
        assert _compute_run_status([]) == "PASS"

    def test_uses_last_summary_line(self):
        """If multiple summary lines exist, uses the last one."""
        logs = [
            MagicMock(message="Summary: PASS=0, FAIL=2", level="INFO"),
            MagicMock(message="Summary: PASS=3, FAIL=0", level="INFO"),
        ]
        # reversed() hits the second one first
        assert _compute_run_status(logs) == "PASS"

    def test_dict_logs(self):
        """Also works with dict-style logs."""
        logs = [
            {"message": "Starting test", "level": "INFO"},
            {"message": "Summary: PASS=5, FAIL=0", "level": "INFO"},
        ]
        assert _compute_run_status(logs) == "PASS"

    def test_dict_logs_with_error(self):
        logs = [
            {"message": "Starting test", "level": "INFO"},
            {"message": "Something broke", "level": "ERROR"},
        ]
        assert _compute_run_status(logs) == "FAIL"


class TestStateMAchine:
    def test_fsm_transitions(self):
        from agent.state_machine import AgentStateMachine
        fsm = AgentStateMachine()
        assert fsm.current_state() == "IDLE"

        fsm.start_recording()
        assert fsm.current_state() == "RECORDING"

        fsm.stop_recording()
        assert fsm.current_state() == "PARSING"

        fsm.parsing_done()
        assert fsm.current_state() == "READY"

        fsm.start_execution()
        assert fsm.current_state() == "EXECUTING"

        fsm.execution_done()
        assert fsm.current_state() == "REPORTING"

        fsm.report_done()
        assert fsm.current_state() == "IDLE"

    def test_fsm_swagger_path(self):
        from agent.state_machine import AgentStateMachine
        fsm = AgentStateMachine()
        fsm.start_swagger()
        assert fsm.current_state() == "SWAGGER_PARSE"
        fsm.swagger_done()
        assert fsm.current_state() == "READY"

    def test_fsm_error_recovery(self):
        from agent.state_machine import AgentStateMachine
        fsm = AgentStateMachine()
        fsm.start_execution()
        fsm.safe_fail("test error")
        assert fsm.current_state() == "ERROR"
        fsm.reset()
        assert fsm.current_state() == "IDLE"

    def test_fsm_invalid_trigger(self):
        from transitions import MachineError
        from agent.state_machine import AgentStateMachine
        fsm = AgentStateMachine()
        with pytest.raises(MachineError):
            fsm.stop_recording()  # Can't stop recording from IDLE

    def test_fsm_status_dict(self):
        from agent.state_machine import AgentStateMachine
        fsm = AgentStateMachine()
        status = fsm.status_dict()
        assert status["state"] == "IDLE"
        assert status["name"] == "PlaywrightAgent"
        assert status["error_reason"] == ""


class TestRuleEngine:
    def test_classify_record(self):
        from agent.rule_engine import classify_intent
        assert classify_intent("start recording") == "record"
        assert classify_intent("capture my test") == "record"

    def test_classify_execute(self):
        from agent.rule_engine import classify_intent
        assert classify_intent("run the tests") == "execute"
        assert classify_intent("execute test case") == "execute"

    def test_classify_swagger(self):
        from agent.rule_engine import classify_intent
        assert classify_intent("import swagger spec") == "swagger"
        assert classify_intent("parse openapi file") == "swagger"

    def test_classify_unknown(self):
        from agent.rule_engine import classify_intent
        assert classify_intent("hello world") == "unknown"

    def test_extract_params(self):
        from agent.rule_engine import extract_parameters
        params = extract_parameters("run tc_42 headless", "execute")
        assert params["tc_id"] == 42
        assert params["headless"] is True
