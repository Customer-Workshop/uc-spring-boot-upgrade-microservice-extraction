"""Tests for agent/parallel_executor.py — multi-user parallel execution."""

import time
import threading
import pytest

from agent.parallel_executor import (
    ExecutionTask,
    ParallelExecutionEngine,
    UserExecutionPool,
    execution_engine,
)


class TestExecutionTask:
    def test_task_defaults(self):
        task = ExecutionTask(tc_id=1, run_id="run-1", user_id="user1")
        assert task.status == "queued"
        assert task.headless is True
        assert task.duration_ms == 0.0

    def test_task_duration_while_running(self):
        task = ExecutionTask(tc_id=1, run_id="run-1", user_id="user1")
        task.started_at = time.time() - 2.0
        assert task.duration_ms >= 1900

    def test_task_duration_completed(self):
        task = ExecutionTask(tc_id=1, run_id="run-1", user_id="user1")
        task.started_at = 1000.0
        task.completed_at = 1001.5
        assert task.duration_ms == 1500.0


class TestUserExecutionPool:
    def test_pool_creation(self):
        pool = UserExecutionPool(user_id="test_user", max_threads=2)
        assert pool.user_id == "test_user"
        assert pool.max_threads == 2
        assert pool.executor is not None
        pool.shutdown()

    def test_pool_active_tasks(self):
        pool = UserExecutionPool(user_id="test_user", max_threads=2)
        assert len(pool.active_tasks) == 0
        assert len(pool.history) == 0
        pool.shutdown()


class TestParallelExecutionEngine:
    def setup_method(self):
        self.engine = ParallelExecutionEngine()

    def test_get_or_create_pool(self):
        pool = self.engine.get_or_create_pool("user_a")
        assert pool.user_id == "user_a"
        assert pool.max_threads == 4

        # Same pool returned on second call
        pool2 = self.engine.get_or_create_pool("user_a")
        assert pool is pool2

    def test_get_or_create_pool_custom_threads(self):
        pool = self.engine.get_or_create_pool("user_b", max_threads=8)
        assert pool.max_threads == 8

    def test_get_user_status_no_pool(self):
        status = self.engine.get_user_status("nonexistent_user")
        assert status["active"] is False
        assert status["running_tasks"] == 0
        assert status["tasks"] == []

    def test_update_user_threads(self):
        result = self.engine.update_user_threads("user_c", 8)
        assert result["max_threads"] == 8
        assert result["status"] == "updated"

    def test_update_user_threads_clamped(self):
        result = self.engine.update_user_threads("user_d", 100)
        assert result["max_threads"] == 16  # Max clamped to 16

        result = self.engine.update_user_threads("user_d", 0)
        assert result["max_threads"] == 1  # Min clamped to 1

    def test_cancel_no_tasks(self):
        cancelled = self.engine.cancel_user_tasks("empty_user")
        assert cancelled == 0

    def test_get_all_users_status(self):
        self.engine.get_or_create_pool("user_x")
        self.engine.get_or_create_pool("user_y")
        statuses = self.engine.get_all_users_status()
        assert len(statuses) >= 2

    def test_submit_execution_structure(self):
        # Test that submit returns correct structure (won't actually run test_case)
        # since symbolic_runner requires DB setup
        result = self.engine.submit_execution(
            user_id="submit_test",
            tc_ids=[1, 2, 3],
            headless=True,
            max_threads=2,
        )
        assert "batch_run_id" in result
        assert result["total_tasks"] == 3
        assert result["max_parallel"] == 2
        assert len(result["tasks"]) == 3
        for t in result["tasks"]:
            assert "tc_id" in t
            assert "run_id" in t


class TestGlobalEngine:
    def test_singleton_exists(self):
        assert execution_engine is not None
        assert isinstance(execution_engine, ParallelExecutionEngine)
