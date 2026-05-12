"""Tests for smart_wait.py and test_retry.py modules."""

import os
import sys
import time

import pytest

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from agent.test_retry import (
    ParallelConfig,
    ParallelResult,
    RetryConfig,
    RetryResult,
    run_parallel,
    run_with_retry,
)


class TestRetryConfig:
    def test_defaults(self):
        config = RetryConfig()
        assert config.max_retries == 3
        assert config.backoff_base_ms == 1000
        assert config.backoff_multiplier == 2.0

    def test_custom_config(self):
        config = RetryConfig(max_retries=5, backoff_base_ms=500)
        assert config.max_retries == 5
        assert config.backoff_base_ms == 500


class TestRetryResult:
    def test_to_dict(self):
        result = RetryResult(
            passed=True,
            attempt_count=2,
            total_duration_ms=1500.0,
        )
        d = result.to_dict()
        assert d["passed"] is True
        assert d["attempt_count"] == 2
        assert d["total_duration_ms"] == 1500.0


class TestRunWithRetry:
    def test_passes_on_first_attempt(self):
        call_count = [0]

        def execute():
            call_count[0] += 1
            return {"passed": True, "error": "", "duration_ms": 100}

        result = run_with_retry(execute)
        assert result.passed is True
        assert result.attempt_count == 1
        assert call_count[0] == 1

    def test_passes_on_second_attempt(self):
        call_count = [0]

        def execute():
            call_count[0] += 1
            if call_count[0] < 2:
                return {"passed": False, "error": "TimeoutError: element not found", "duration_ms": 100}
            return {"passed": True, "error": "", "duration_ms": 100}

        config = RetryConfig(max_retries=3, backoff_base_ms=10)
        result = run_with_retry(execute, config)
        assert result.passed is True
        assert result.attempt_count == 2

    def test_fails_after_all_retries(self):
        def execute():
            return {"passed": False, "error": "AssertionError: expected X", "duration_ms": 50}

        config = RetryConfig(max_retries=2, backoff_base_ms=10)
        result = run_with_retry(execute, config)
        assert result.passed is False
        assert result.attempt_count == 2
        assert "AssertionError" in result.last_error

    def test_collects_healed_selectors(self):
        def execute():
            return {"passed": True, "error": "", "duration_ms": 100,
                    "healed": [{"original": "#old", "healed": "#new"}]}

        result = run_with_retry(execute)
        assert len(result.healed_selectors) == 1
        assert result.healed_selectors[0]["original"] == "#old"

    def test_handles_exception_in_execute(self):
        call_count = [0]

        def execute():
            call_count[0] += 1
            if call_count[0] == 1:
                raise RuntimeError("Unexpected error")
            return {"passed": True, "error": "", "duration_ms": 50}

        config = RetryConfig(max_retries=3, backoff_base_ms=10)
        result = run_with_retry(execute, config)
        assert result.passed is True
        assert result.attempt_count == 2


class TestParallelResult:
    def test_pass_rate_all_pass(self):
        r = ParallelResult(total_cases=5, passed_count=5, failed_count=0, total_duration_ms=1000)
        assert r.pass_rate == 1.0

    def test_pass_rate_half(self):
        r = ParallelResult(total_cases=4, passed_count=2, failed_count=2, total_duration_ms=1000)
        assert r.pass_rate == 0.5

    def test_pass_rate_empty(self):
        r = ParallelResult(total_cases=0, passed_count=0, failed_count=0, total_duration_ms=0)
        assert r.pass_rate == 0.0

    def test_to_dict(self):
        r = ParallelResult(total_cases=3, passed_count=2, failed_count=1, total_duration_ms=500)
        d = r.to_dict()
        assert d["pass_rate"] == 66.7


class TestRunParallel:
    def test_all_pass(self):
        def execute(tc_id):
            return {"passed": True, "error": "", "duration_ms": 50}

        result = run_parallel([1, 2, 3], execute, ParallelConfig(max_workers=2))
        assert result.passed_count == 3
        assert result.failed_count == 0
        assert result.total_cases == 3

    def test_mixed_results(self):
        def execute(tc_id):
            if tc_id == 2:
                return {"passed": False, "error": "failed", "duration_ms": 50}
            return {"passed": True, "error": "", "duration_ms": 50}

        result = run_parallel([1, 2, 3], execute, ParallelConfig(max_workers=3))
        assert result.passed_count == 2
        assert result.failed_count == 1

    def test_handles_exception(self):
        def execute(tc_id):
            if tc_id == 1:
                raise RuntimeError("boom")
            return {"passed": True, "error": "", "duration_ms": 50}

        result = run_parallel([1, 2], execute, ParallelConfig(max_workers=2))
        assert result.failed_count >= 1

    def test_empty_tc_list(self):
        def execute(tc_id):
            return {"passed": True, "error": "", "duration_ms": 50}

        result = run_parallel([], execute)
        assert result.total_cases == 0
        assert result.passed_count == 0
