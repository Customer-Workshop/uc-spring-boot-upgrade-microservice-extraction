"""Multi-user parallel test execution engine.

Each user gets their own thread pool with configurable concurrency.
Supports per-user execution queues, progress tracking, and resource isolation.
"""

import threading
import time
import uuid
from concurrent.futures import ThreadPoolExecutor, Future
from dataclasses import dataclass, field
from typing import Optional


@dataclass
class ExecutionTask:
    """A single test execution task."""
    tc_id: int
    run_id: str
    user_id: str
    headless: bool = True
    status: str = "queued"  # queued, running, passed, failed, cancelled
    started_at: Optional[float] = None
    completed_at: Optional[float] = None
    error: str = ""
    future: Optional[Future] = field(default=None, repr=False)

    @property
    def duration_ms(self) -> float:
        if self.started_at and self.completed_at:
            return (self.completed_at - self.started_at) * 1000
        elif self.started_at:
            return (time.time() - self.started_at) * 1000
        return 0.0


@dataclass
class UserExecutionPool:
    """Per-user execution context with configurable thread pool."""
    user_id: str
    max_threads: int = 4
    executor: Optional[ThreadPoolExecutor] = field(default=None, repr=False)
    active_tasks: dict[str, ExecutionTask] = field(default_factory=dict)
    history: list[ExecutionTask] = field(default_factory=list)
    _lock: threading.Lock = field(default_factory=threading.Lock, repr=False)

    def __post_init__(self):
        self.executor = ThreadPoolExecutor(
            max_workers=self.max_threads,
            thread_name_prefix=f"user-{self.user_id}"
        )

    def shutdown(self):
        if self.executor:
            self.executor.shutdown(wait=False)


class ParallelExecutionEngine:
    """Manages parallel test execution across multiple users.

    Each user gets their own thread pool to prevent one user from
    blocking another. Configurable max threads per user.
    """

    DEFAULT_MAX_THREADS = 4

    def __init__(self):
        self._pools: dict[str, UserExecutionPool] = {}
        self._lock = threading.Lock()

    def get_or_create_pool(self, user_id: str, max_threads: int = None) -> UserExecutionPool:
        """Get or create an execution pool for a user."""
        with self._lock:
            if user_id not in self._pools:
                threads = max_threads or self.DEFAULT_MAX_THREADS
                self._pools[user_id] = UserExecutionPool(
                    user_id=user_id,
                    max_threads=threads,
                )
            return self._pools[user_id]

    def submit_execution(self, user_id: str, tc_ids: list[int],
                         headless: bool = True, max_threads: int = None) -> dict:
        """Submit test cases for parallel execution under a user's pool.

        Returns execution batch info with run_id and task statuses.
        """
        pool = self.get_or_create_pool(user_id, max_threads)
        batch_run_id = str(uuid.uuid4())[:8]
        tasks = []

        for tc_id in tc_ids:
            run_id = f"{batch_run_id}-{tc_id}"
            task = ExecutionTask(
                tc_id=tc_id,
                run_id=run_id,
                user_id=user_id,
                headless=headless,
            )

            # Submit to the user's thread pool
            future = pool.executor.submit(self._execute_task, task)
            task.future = future

            with pool._lock:
                pool.active_tasks[run_id] = task

            tasks.append(task)

        return {
            "batch_run_id": batch_run_id,
            "user_id": user_id,
            "total_tasks": len(tasks),
            "max_parallel": pool.max_threads,
            "tasks": [{"tc_id": t.tc_id, "run_id": t.run_id, "status": t.status} for t in tasks],
        }

    def _execute_task(self, task: ExecutionTask):
        """Execute a single test case (runs in thread pool)."""
        task.status = "running"
        task.started_at = time.time()

        try:
            from agent.symbolic_runner import run_test_case
            run_test_case(task.tc_id, headless=task.headless, run_id=task.run_id)

            # Check final status from DB
            from db.db_utils import get_db_session, get_test_case
            with get_db_session() as db:
                tc = get_test_case(db, task.tc_id)
                if tc and tc.status == "PASS":
                    task.status = "passed"
                else:
                    task.status = "failed"
                    if tc:
                        task.error = f"Test case status: {tc.status}"

        except Exception as e:
            task.status = "failed"
            task.error = str(e)

        task.completed_at = time.time()

        # Move from active to history
        pool = self._pools.get(task.user_id)
        if pool:
            with pool._lock:
                pool.active_tasks.pop(task.run_id, None)
                pool.history.append(task)
                # Keep history manageable
                if len(pool.history) > 200:
                    pool.history = pool.history[-100:]

    def get_user_status(self, user_id: str) -> dict:
        """Get execution status for a user's pool."""
        pool = self._pools.get(user_id)
        if not pool:
            return {
                "user_id": user_id,
                "active": False,
                "running_tasks": 0,
                "max_threads": self.DEFAULT_MAX_THREADS,
                "tasks": [],
                "recent_history": [],
            }

        with pool._lock:
            active_list = [
                {
                    "tc_id": t.tc_id,
                    "run_id": t.run_id,
                    "status": t.status,
                    "duration_ms": t.duration_ms,
                }
                for t in pool.active_tasks.values()
            ]
            history_list = [
                {
                    "tc_id": t.tc_id,
                    "run_id": t.run_id,
                    "status": t.status,
                    "duration_ms": t.duration_ms,
                    "error": t.error,
                }
                for t in pool.history[-20:]
            ]

        return {
            "user_id": user_id,
            "active": len(active_list) > 0,
            "running_tasks": len(active_list),
            "max_threads": pool.max_threads,
            "tasks": active_list,
            "recent_history": history_list,
        }

    def cancel_user_tasks(self, user_id: str) -> int:
        """Cancel all pending/running tasks for a user. Returns cancelled count."""
        pool = self._pools.get(user_id)
        if not pool:
            return 0

        cancelled = 0
        with pool._lock:
            for task in list(pool.active_tasks.values()):
                if task.future and not task.future.done():
                    task.future.cancel()
                    task.status = "cancelled"
                    cancelled += 1
            pool.active_tasks.clear()

        return cancelled

    def update_user_threads(self, user_id: str, max_threads: int) -> dict:
        """Update the max parallel threads for a user (recreates pool)."""
        max_threads = max(1, min(max_threads, 16))  # Clamp 1-16

        with self._lock:
            old_pool = self._pools.get(user_id)
            if old_pool:
                old_pool.shutdown()

            new_pool = UserExecutionPool(
                user_id=user_id,
                max_threads=max_threads,
            )
            self._pools[user_id] = new_pool

        return {"user_id": user_id, "max_threads": max_threads, "status": "updated"}

    def get_all_users_status(self) -> list[dict]:
        """Get status of all user pools."""
        statuses = []
        for user_id in list(self._pools.keys()):
            statuses.append(self.get_user_status(user_id))
        return statuses


# Global singleton
execution_engine = ParallelExecutionEngine()
