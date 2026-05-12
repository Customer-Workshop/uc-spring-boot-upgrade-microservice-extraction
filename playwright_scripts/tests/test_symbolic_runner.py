"""Tests for agent/symbolic_runner.py"""

import json
import os
import sys
from unittest.mock import MagicMock, patch

import pytest

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from agent.symbolic_runner import _run_api_test, _substitute_variables, run_test_case


class TestSubstituteVariables:
    def test_basic_substitution(self):
        steps = [
            {"order": 0, "action": "fill", "selector": "#user", "value": "{{username}}", "description": ""},
            {"order": 1, "action": "fill", "selector": "#pass", "value": "{{password}}", "description": ""},
        ]
        data = {"username": "alice", "password": "secret123"}
        result = _substitute_variables(steps, data)
        assert result[0]["value"] == "alice"
        assert result[1]["value"] == "secret123"

    def test_no_variables(self):
        steps = [{"order": 0, "action": "click", "selector": "#btn", "value": "", "description": ""}]
        data = {"key": "val"}
        result = _substitute_variables(steps, data)
        assert result[0]["value"] == ""

    def test_empty_data(self):
        steps = [{"order": 0, "action": "fill", "selector": "#x", "value": "{{name}}", "description": ""}]
        result = _substitute_variables(steps, {})
        assert result[0]["value"] == "{{name}}"

    def test_multiple_vars_in_one_value(self):
        steps = [{"order": 0, "action": "fill", "selector": "#url", "value": "{{host}}/{{path}}", "description": ""}]
        data = {"host": "http://localhost", "path": "api/v1"}
        result = _substitute_variables(steps, data)
        assert result[0]["value"] == "http://localhost/api/v1"

    def test_preserves_original(self):
        steps = [{"order": 0, "action": "fill", "selector": "#x", "value": "{{name}}", "description": ""}]
        data = {"name": "Bob"}
        result = _substitute_variables(steps, data)
        assert steps[0]["value"] == "{{name}}"  # Original unchanged
        assert result[0]["value"] == "Bob"


class TestRunTestCaseDispatch:
    @patch("agent.symbolic_runner.get_db_session")
    @patch("agent.symbolic_runner._run_web_test")
    def test_dispatches_web(self, mock_run_web, mock_db):
        mock_session = MagicMock()
        mock_db.return_value.__enter__ = MagicMock(return_value=mock_session)
        mock_db.return_value.__exit__ = MagicMock(return_value=False)

        mock_tc = MagicMock()
        mock_tc.flow_type = "web"
        mock_tc.name = "Test Web"

        with patch("agent.symbolic_runner.get_test_case", return_value=mock_tc), \
             patch("agent.symbolic_runner.update_tc_status"), \
             patch("agent.symbolic_runner.add_log"):
            run_id = run_test_case(1, headless=True)

        assert len(run_id) == 8
        mock_run_web.assert_called_once()

    @patch("agent.symbolic_runner.get_db_session")
    @patch("agent.symbolic_runner._run_api_test")
    def test_dispatches_api(self, mock_run_api, mock_db):
        mock_session = MagicMock()
        mock_db.return_value.__enter__ = MagicMock(return_value=mock_session)
        mock_db.return_value.__exit__ = MagicMock(return_value=False)

        mock_tc = MagicMock()
        mock_tc.flow_type = "api"
        mock_tc.name = "Test API"

        with patch("agent.symbolic_runner.get_test_case", return_value=mock_tc), \
             patch("agent.symbolic_runner.update_tc_status"), \
             patch("agent.symbolic_runner.add_log"):
            run_id = run_test_case(2, headless=True)

        assert len(run_id) == 8
        mock_run_api.assert_called_once()

    @patch("agent.symbolic_runner.get_db_session")
    def test_nonexistent_tc(self, mock_db):
        mock_session = MagicMock()
        mock_db.return_value.__enter__ = MagicMock(return_value=mock_session)
        mock_db.return_value.__exit__ = MagicMock(return_value=False)

        with patch("agent.symbolic_runner.get_test_case", return_value=None), \
             patch("agent.symbolic_runner.update_tc_status"), \
             patch("agent.symbolic_runner.add_log"):
            run_id = run_test_case(999, headless=True)
        assert len(run_id) == 8


class TestRunApiTest:
    @patch("agent.symbolic_runner.get_db_session")
    def test_no_endpoints_passes(self, mock_db):
        mock_session = MagicMock()
        mock_db.return_value.__enter__ = MagicMock(return_value=mock_session)
        mock_db.return_value.__exit__ = MagicMock(return_value=False)

        mock_tc = MagicMock()
        mock_tc.base_url = "http://localhost"

        with patch("agent.symbolic_runner.get_test_case", return_value=mock_tc), \
             patch("agent.symbolic_runner.get_endpoints_for_tc", return_value=[]), \
             patch("agent.symbolic_runner.add_log") as mock_log, \
             patch("agent.symbolic_runner.update_tc_status") as mock_status:
            _run_api_test(1, "test123")
            mock_status.assert_called_with(mock_session, 1, "PASS")

    @patch("agent.symbolic_runner.get_db_session")
    def test_pass_status_computation(self, mock_db):
        mock_session = MagicMock()
        mock_db.return_value.__enter__ = MagicMock(return_value=mock_session)
        mock_db.return_value.__exit__ = MagicMock(return_value=False)

        mock_tc = MagicMock()
        mock_tc.base_url = "http://localhost"

        mock_ep = MagicMock()
        mock_ep.id = 1
        mock_ep.method = "GET"
        mock_ep.path = "/test"
        mock_ep.summary = "Test"
        mock_ep.request_headers = "{}"
        mock_ep.request_params = "{}"
        mock_ep.request_body = "{}"
        mock_ep.expected_status = 200

        mock_result = {"actual_status": 200, "response_body": "{}"}

        with patch("agent.symbolic_runner.get_test_case", return_value=mock_tc), \
             patch("agent.symbolic_runner.get_endpoints_for_tc", return_value=[mock_ep]), \
             patch("agent.symbolic_runner.add_log"), \
             patch("agent.symbolic_runner.update_tc_status") as mock_status, \
             patch("agent.symbolic_runner.update_endpoint"), \
             patch("api_integration.executor.execute_endpoint", return_value=mock_result):
            _run_api_test(1, "test123")
            # Should be called with PASS since actual == expected
            calls = mock_status.call_args_list
            assert any(c[0][2] == "PASS" for c in calls)

    @patch("agent.symbolic_runner.get_db_session")
    def test_performance_threshold_fail(self, mock_db):
        """Test that performance threshold causes failure when exceeded."""
        import time
        mock_session = MagicMock()
        mock_db.return_value.__enter__ = MagicMock(return_value=mock_session)
        mock_db.return_value.__exit__ = MagicMock(return_value=False)

        mock_tc = MagicMock()
        mock_tc.base_url = "http://localhost"

        mock_ep = MagicMock()
        mock_ep.id = 1
        mock_ep.method = "GET"
        mock_ep.path = "/slow"
        mock_ep.summary = "[PERF] Slow endpoint"
        mock_ep.request_headers = json.dumps({"X-Perf-Threshold-Ms": "10"})
        mock_ep.request_params = "{}"
        mock_ep.request_body = "{}"
        mock_ep.expected_status = 200

        def slow_execute(ep):
            time.sleep(0.05)  # 50ms, above 10ms threshold
            return {"actual_status": 200, "response_body": "{}"}

        with patch("agent.symbolic_runner.get_test_case", return_value=mock_tc), \
             patch("agent.symbolic_runner.get_endpoints_for_tc", return_value=[mock_ep]), \
             patch("agent.symbolic_runner.add_log"), \
             patch("agent.symbolic_runner.update_tc_status") as mock_status, \
             patch("agent.symbolic_runner.update_endpoint"), \
             patch("api_integration.executor.execute_endpoint", side_effect=slow_execute):
            _run_api_test(1, "test123")
            calls = mock_status.call_args_list
            assert any(c[0][2] == "FAIL" for c in calls)
