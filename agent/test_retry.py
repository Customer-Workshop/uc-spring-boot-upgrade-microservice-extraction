"""Test Retry and Parallel Execution Module.

Provides intelligent retry logic for flaky tests and parallel execution
support for running multiple test cases concurrently.

Features:
- Configurable retry count per test case
- Exponential backoff between retries
- Parallel execution with concurrency limit
- Result aggregation across retries
"""

import asyncio
import logging
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from dataclasses import dataclass, field
from typing import Callable, Optional

logger = logging.getLogger(__name__)


@dataclass
class RetryConfig:
    """Configuration for test retry behavior."""

    max_retries: int = 3
    backoff_base_ms: int = 1000
    backoff_multiplier: float = 2.0
    retry_on_error_types: list = field(default_factory=lambda: ["TimeoutError", "AssertionError"])
    fail_fast: bool = False


@dataclass
class RetryResult:
    """Result of a retried test execution."""

    passed: bool
    attempt_count: int
    total_duration_ms: float
    last_error: str = ""
    attempt_results: list = field(default_factory=list)
    healed_selectors: list = field(default_factory=list)

    def to_dict(self) -> dict:
        return {
            "passed": self.passed,
            "attempt_count": self.attempt_count,
            "total_duration_ms": self.total_duration_ms,
            "last_error": self.last_error,
            "attempt_results": self.attempt_results,
            "healed_selectors": self.healed_selectors,
        }


def run_with_retry(execute_fn: Callable, config: Optional[RetryConfig] = None) -> RetryResult:
    """Execute a test function with retry logic.

    Args:
        execute_fn: Callable that returns dict with {passed, error, duration_ms}
        config: Retry configuration (uses defaults if None)

    Returns:
        RetryResult with aggregated results from all attempts
    """
    if config is None:
        config = RetryConfig()

    start = time.time()
    attempts = []
    healed = []

    for attempt in range(config.max_retries):
        try:
            result = execute_fn()
            attempts.append({
                "attempt": attempt + 1,
                "passed": result.get("passed", False),
                "error": result.get("error", ""),
                "duration_ms": result.get("duration_ms", 0),
            })

            if result.get("healed"):
                healed.extend(result["healed"])

            if result.get("passed", False):
                total_ms = (time.time() - start) * 1000
                return RetryResult(
                    passed=True,
                    attempt_count=attempt + 1,
                    total_duration_ms=total_ms,
                    attempt_results=attempts,
                    healed_selectors=healed,
                )

            # Check if we should retry based on error type
            error_msg = result.get("error", "")
            should_retry = any(
                err_type.lower() in error_msg.lower()
                for err_type in config.retry_on_error_types
            )

            if not should_retry and config.fail_fast:
                break

        except Exception as e:
            attempts.append({
                "attempt": attempt + 1,
                "passed": False,
                "error": str(e),
                "duration_ms": 0,
            })

        # Exponential backoff between retries
        if attempt < config.max_retries - 1:
            wait_ms = config.backoff_base_ms * (config.backoff_multiplier ** attempt)
            time.sleep(wait_ms / 1000)

    total_ms = (time.time() - start) * 1000
    last_error = attempts[-1]["error"] if attempts else "No attempts executed"

    return RetryResult(
        passed=False,
        attempt_count=len(attempts),
        total_duration_ms=total_ms,
        last_error=last_error,
        attempt_results=attempts,
        healed_selectors=healed,
    )


@dataclass
class ParallelConfig:
    """Configuration for parallel test execution."""

    max_workers: int = 4
    timeout_per_tc_ms: int = 300000  # 5 minutes per TC
    fail_fast: bool = False


@dataclass
class ParallelResult:
    """Result of parallel test execution."""

    total_cases: int
    passed_count: int
    failed_count: int
    total_duration_ms: float
    results: list = field(default_factory=list)

    @property
    def pass_rate(self) -> float:
        if self.total_cases == 0:
            return 0.0
        return self.passed_count / self.total_cases

    def to_dict(self) -> dict:
        return {
            "total_cases": self.total_cases,
            "passed_count": self.passed_count,
            "failed_count": self.failed_count,
            "pass_rate": round(self.pass_rate * 100, 1),
            "total_duration_ms": self.total_duration_ms,
            "results": self.results,
        }


def run_parallel(tc_ids: list[int], execute_fn: Callable,
                 config: Optional[ParallelConfig] = None) -> ParallelResult:
    """Run multiple test cases in parallel with a thread pool.

    Args:
        tc_ids: List of test case IDs to execute
        execute_fn: Callable that takes tc_id and returns dict with {passed, error, duration_ms}
        config: Parallel execution configuration

    Returns:
        ParallelResult with aggregated results
    """
    if config is None:
        config = ParallelConfig()

    start = time.time()
    results = []
    passed = 0
    failed = 0

    with ThreadPoolExecutor(max_workers=config.max_workers) as executor:
        future_to_tc = {
            executor.submit(execute_fn, tc_id): tc_id
            for tc_id in tc_ids
        }

        for future in as_completed(future_to_tc):
            tc_id = future_to_tc[future]
            try:
                result = future.result(timeout=config.timeout_per_tc_ms / 1000)
                result["tc_id"] = tc_id
                results.append(result)

                if result.get("passed", False):
                    passed += 1
                else:
                    failed += 1
                    if config.fail_fast:
                        # Cancel remaining futures
                        for f in future_to_tc:
                            f.cancel()
                        break

            except Exception as e:
                failed += 1
                results.append({
                    "tc_id": tc_id,
                    "passed": False,
                    "error": f"Execution error: {str(e)}",
                    "duration_ms": 0,
                })

    total_ms = (time.time() - start) * 1000

    return ParallelResult(
        total_cases=len(tc_ids),
        passed_count=passed,
        failed_count=failed,
        total_duration_ms=total_ms,
        results=results,
    )
