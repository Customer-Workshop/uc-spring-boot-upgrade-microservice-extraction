"""Playwright codegen recorder: launch visible browser for recording + POM generation."""

import json
import os
import re
import subprocess
import signal
import threading
import time
from typing import Optional


# Per-user recording sessions
_recording_sessions: dict[str, dict] = {}
_lock = threading.Lock()


class RecordingSession:
    """Manages a single recording session with visible browser."""

    def __init__(self, tc_id: int, base_url: str, user_id: str = "default"):
        self.tc_id = tc_id
        self.base_url = base_url
        self.user_id = user_id
        self.process: Optional[subprocess.Popen] = None
        self.output_file = f"recorded_tc_{tc_id}_{user_id}.py"
        self.started_at = time.time()
        self.status = "idle"  # idle, recording, stopping, done
        self.pom_elements: list[dict] = []
        self.pages_discovered: list[str] = []

    def start(self) -> bool:
        """Launch playwright codegen with VISIBLE browser (non-headless).

        The codegen tool always opens a visible Chromium window so the user
        can interact with the page. The generated script is written to output_file.
        """
        try:
            # Use --headed flag to ensure browser is always visible
            cmd = [
                "python", "-m", "playwright", "codegen",
                "--target", "python",
                "-o", self.output_file,
                "--viewport-size", "1280,720",
                self.base_url,
            ]

            env = os.environ.copy()
            # Ensure display is available for the visible browser
            if "DISPLAY" not in env:
                env["DISPLAY"] = ":1"

            self.process = subprocess.Popen(
                cmd,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                env=env,
                preexec_fn=os.setsid if os.name != "nt" else None,
            )
            self.status = "recording"
            return True
        except Exception as e:
            self.status = "error"
            return False

    def stop(self) -> str:
        """Stop the recording and return the output file path."""
        self.status = "stopping"
        if self.process:
            try:
                if os.name != "nt":
                    os.killpg(os.getpgid(self.process.pid), signal.SIGTERM)
                else:
                    self.process.terminate()
                # Give it a moment to write the file
                self.process.wait(timeout=5)
            except (ProcessLookupError, OSError, subprocess.TimeoutExpired):
                try:
                    self.process.kill()
                except Exception:
                    pass
            self.process = None

        self.status = "done"
        return self.output_file

    def is_alive(self) -> bool:
        """Check if the recording process is still running."""
        if self.process:
            return self.process.poll() is None
        return False

    @property
    def duration_seconds(self) -> int:
        return int(time.time() - self.started_at)


def start_recording(tc_id: int, base_url: str, user_id: str = "default") -> dict:
    """Start a new recording session for a user.

    Opens a VISIBLE browser window using playwright codegen.
    Returns status dict.
    """
    with _lock:
        # Stop any existing session for this user
        if user_id in _recording_sessions:
            old_session = _recording_sessions[user_id]
            if old_session.get("session") and old_session["session"].is_alive():
                old_session["session"].stop()

        session = RecordingSession(tc_id, base_url, user_id)
        success = session.start()

        if success:
            _recording_sessions[user_id] = {"session": session, "tc_id": tc_id}
            return {
                "status": "recording",
                "message": "Browser opened for recording. Interact with the page, then click Stop.",
                "tc_id": tc_id,
                "user_id": user_id,
            }
        else:
            return {"status": "error", "message": "Failed to start recording browser"}


def stop_recording(user_id: str = "default") -> dict:
    """Stop the active recording session and parse the recorded script."""
    with _lock:
        if user_id not in _recording_sessions:
            return {"status": "error", "message": "No active recording session"}

        session_data = _recording_sessions.pop(user_id)
        session: RecordingSession = session_data["session"]

    output_file = session.stop()
    steps = parse_codegen_script(output_file)
    pom = generate_pom_from_steps(steps, session.base_url)

    return {
        "status": "done",
        "steps": steps,
        "pom": pom,
        "duration_seconds": session.duration_seconds,
        "output_file": output_file,
    }


def get_recording_status(user_id: str = "default") -> dict:
    """Get current recording status for a user."""
    with _lock:
        if user_id not in _recording_sessions:
            return {"status": "idle", "recording": False}

        session: RecordingSession = _recording_sessions[user_id]["session"]
        return {
            "status": session.status,
            "recording": session.is_alive(),
            "tc_id": session.tc_id,
            "duration_seconds": session.duration_seconds,
            "base_url": session.base_url,
        }


def parse_codegen_script(script_path: str) -> list[dict]:
    """Parse a Playwright codegen Python script into test steps.

    Returns list of step dicts: {order, action, selector, value, description}
    """
    if not os.path.isfile(script_path):
        return []

    with open(script_path, "r") as f:
        content = f.read()

    steps = []
    order = 0

    lines = content.split("\n")
    for line in lines:
        line = line.strip()
        step = _parse_line(line)
        if step:
            step["order"] = order
            steps.append(step)
            order += 1

    return steps


def _parse_line(line: str) -> Optional[dict]:
    """Parse a single Python line into a step dict."""

    # page.goto("url")
    m = re.match(r'page\.goto\(["\'](.+?)["\']\)', line)
    if m:
        return {"action": "navigate", "selector": "", "value": m.group(1), "description": f"Navigate to {m.group(1)}"}

    # page.locator("selector").click()
    m = re.match(r'page\.locator\(["\'](.+?)["\']\)\.click\(\)', line)
    if m:
        return {"action": "click", "selector": m.group(1), "value": "", "description": f"Click {m.group(1)}"}

    # page.click("selector")
    m = re.match(r'page\.click\(["\'](.+?)["\']\)', line)
    if m:
        return {"action": "click", "selector": m.group(1), "value": "", "description": f"Click {m.group(1)}"}

    # page.locator("selector").dblclick()
    m = re.match(r'page\.locator\(["\'](.+?)["\']\)\.dblclick\(\)', line)
    if m:
        return {"action": "dblclick", "selector": m.group(1), "value": "", "description": f"Double-click {m.group(1)}"}

    # page.dblclick("selector")
    m = re.match(r'page\.dblclick\(["\'](.+?)["\']\)', line)
    if m:
        return {"action": "dblclick", "selector": m.group(1), "value": "", "description": f"Double-click {m.group(1)}"}

    # page.locator("selector").fill("value")
    m = re.match(r'page\.locator\(["\'](.+?)["\']\)\.fill\(["\'](.+?)["\']\)', line)
    if m:
        return {"action": "fill", "selector": m.group(1), "value": m.group(2), "description": f"Fill {m.group(1)} with '{m.group(2)}'"}

    # page.fill("selector", "value")
    m = re.match(r'page\.fill\(["\'](.+?)["\'],\s*["\'](.+?)["\']\)', line)
    if m:
        return {"action": "fill", "selector": m.group(1), "value": m.group(2), "description": f"Fill {m.group(1)} with '{m.group(2)}'"}

    # page.locator("selector").type("value")
    m = re.match(r'page\.locator\(["\'](.+?)["\']\)\.type\(["\'](.+?)["\']\)', line)
    if m:
        return {"action": "type", "selector": m.group(1), "value": m.group(2), "description": f"Type '{m.group(2)}' into {m.group(1)}"}

    # page.type("selector", "value")
    m = re.match(r'page\.type\(["\'](.+?)["\'],\s*["\'](.+?)["\']\)', line)
    if m:
        return {"action": "type", "selector": m.group(1), "value": m.group(2), "description": f"Type '{m.group(2)}' into {m.group(1)}"}

    # page.locator("selector").press("key")
    m = re.match(r'page\.locator\(["\'](.+?)["\']\)\.press\(["\'](.+?)["\']\)', line)
    if m:
        return {"action": "press", "selector": m.group(1), "value": m.group(2), "description": f"Press {m.group(2)} on {m.group(1)}"}

    # page.press("selector", "key")
    m = re.match(r'page\.press\(["\'](.+?)["\'],\s*["\'](.+?)["\']\)', line)
    if m:
        return {"action": "press", "selector": m.group(1), "value": m.group(2), "description": f"Press {m.group(2)} on {m.group(1)}"}

    # page.locator("selector").select_option("value")
    m = re.match(r'page\.locator\(["\'](.+?)["\']\)\.select_option\(["\'](.+?)["\']\)', line)
    if m:
        return {"action": "select", "selector": m.group(1), "value": m.group(2), "description": f"Select '{m.group(2)}' in {m.group(1)}"}

    # page.select_option("selector", "value")
    m = re.match(r'page\.select_option\(["\'](.+?)["\'],\s*["\'](.+?)["\']\)', line)
    if m:
        return {"action": "select", "selector": m.group(1), "value": m.group(2), "description": f"Select '{m.group(2)}' in {m.group(1)}"}

    # expect(page.locator("selector")).to_be_visible()
    m = re.match(r'expect\(page\.locator\(["\'](.+?)["\']\)\)\.to_be_visible', line)
    if m:
        return {"action": "assert_visible", "selector": m.group(1), "value": "", "description": f"Assert {m.group(1)} is visible"}

    # page.locator("selector").hover()
    m = re.match(r'page\.locator\(["\'](.+?)["\']\)\.hover\(\)', line)
    if m:
        return {"action": "hover", "selector": m.group(1), "value": "", "description": f"Hover over {m.group(1)}"}

    # page.locator("selector").check()
    m = re.match(r'page\.locator\(["\'](.+?)["\']\)\.check\(\)', line)
    if m:
        return {"action": "check", "selector": m.group(1), "value": "", "description": f"Check {m.group(1)}"}

    # page.locator("selector").uncheck()
    m = re.match(r'page\.locator\(["\'](.+?)["\']\)\.uncheck\(\)', line)
    if m:
        return {"action": "uncheck", "selector": m.group(1), "value": "", "description": f"Uncheck {m.group(1)}"}

    # page.wait_for_timeout(ms)
    m = re.match(r'page\.wait_for_timeout\((\d+)\)', line)
    if m:
        return {"action": "wait", "selector": "", "value": m.group(1), "description": f"Wait {m.group(1)}ms"}

    return None


def generate_pom_from_steps(steps: list[dict], base_url: str = "") -> dict:
    """Generate Page Object Model structure from recorded steps.

    Groups selectors by page (inferred from navigate actions).
    Returns POM dict with pages and their elements.
    """
    pages: dict[str, list[dict]] = {}
    current_page = "HomePage"

    for step in steps:
        action = step.get("action", "")
        selector = step.get("selector", "")
        value = step.get("value", "")

        # Detect page changes from navigate actions
        if action == "navigate" and value:
            current_page = _infer_page_name(value)
            if current_page not in pages:
                pages[current_page] = []
            continue

        # Collect elements with selectors
        if selector:
            if current_page not in pages:
                pages[current_page] = []

            element_name = _infer_element_name(selector)
            # Avoid duplicates
            existing = [e["name"] for e in pages[current_page]]
            if element_name not in existing:
                pages[current_page].append({
                    "name": element_name,
                    "selector": selector,
                    "action_used": action,
                    "locator_type": _classify_selector(selector),
                })

    # Generate POM code for each page
    pom_files = {}
    for page_name, elements in pages.items():
        pom_files[page_name] = _generate_page_class(page_name, elements)

    return {
        "pages": pages,
        "pom_code": pom_files,
        "total_pages": len(pages),
        "total_elements": sum(len(elems) for elems in pages.values()),
    }


def _infer_page_name(url: str) -> str:
    """Infer a page class name from a URL."""
    from urllib.parse import urlparse
    parsed = urlparse(url)
    path = parsed.path.strip("/")

    if not path:
        return "HomePage"

    # Take the last meaningful path segment
    parts = [p for p in path.split("/") if p and not p.isdigit()]
    if parts:
        name = parts[-1].replace("-", "_").replace(".", "_")
        return f"{name.title().replace('_', '')}Page"
    return "HomePage"


def _infer_element_name(selector: str) -> str:
    """Infer a meaningful element name from a selector."""
    # data-testid
    m = re.search(r'\[data-testid=["\']([^"\']+)["\']\]', selector)
    if m:
        return m.group(1).replace("-", "_")

    # id selector
    m = re.search(r'#([a-zA-Z_][\w-]*)', selector)
    if m:
        return m.group(1).replace("-", "_")

    # name attribute
    m = re.search(r'\[name=["\']([^"\']+)["\']\]', selector)
    if m:
        return f"{m.group(1).replace('-', '_')}_field"

    # class-based
    m = re.search(r'\.([a-zA-Z_][\w-]*)', selector)
    if m:
        return m.group(1).replace("-", "_")

    # aria-label
    m = re.search(r'\[aria-label=["\']([^"\']+)["\']\]', selector)
    if m:
        return m.group(1).lower().replace(" ", "_").replace("-", "_")

    # text-based
    m = re.search(r'text=["\']([^"\']+)["\']', selector)
    if m:
        return m.group(1).lower().replace(" ", "_")[:30]

    # Placeholder
    m = re.search(r'\[placeholder=["\']([^"\']+)["\']\]', selector)
    if m:
        return f"{m.group(1).lower().replace(' ', '_')}_input"

    # Fallback
    clean = re.sub(r'[^a-zA-Z0-9_]', '_', selector)[:30]
    return clean.strip("_") or "element"


def _classify_selector(selector: str) -> str:
    """Classify the type of selector."""
    if "[data-testid" in selector:
        return "data-testid"
    if selector.startswith("#"):
        return "id"
    if selector.startswith("."):
        return "class"
    if "[aria-" in selector:
        return "aria"
    if "role=" in selector:
        return "role"
    if "text=" in selector:
        return "text"
    if "[name=" in selector:
        return "name"
    if selector.startswith("//") or selector.startswith("xpath="):
        return "xpath"
    return "css"


def _generate_page_class(page_name: str, elements: list[dict]) -> str:
    """Generate a Page Object Model class for a page."""
    lines = [
        f'"""Page Object Model for {page_name}."""',
        "",
        "from playwright.sync_api import Page, expect",
        "",
        "",
        f"class {page_name}:",
        f'    """Page object for {page_name}."""',
        "",
        "    def __init__(self, page: Page):",
        "        self.page = page",
        "",
    ]

    # Properties for each element
    for elem in elements:
        prop_name = elem["name"]
        selector = elem["selector"]
        lines.append("    @property")
        lines.append(f"    def {prop_name}(self):")
        lines.append(f'        return self.page.locator("{selector}")')
        lines.append("")

    # Action methods based on what was recorded
    actions_added = set()
    for elem in elements:
        action = elem["action_used"]
        method_name = f"{action}_{elem['name']}"
        if method_name in actions_added:
            continue
        actions_added.add(method_name)

        if action == "click":
            lines.append(f"    def click_{elem['name']}(self):")
            lines.append(f"        self.{elem['name']}.click()")
            lines.append("")
        elif action in ("fill", "type"):
            lines.append(f"    def fill_{elem['name']}(self, value: str):")
            lines.append(f"        self.{elem['name']}.fill(value)")
            lines.append("")
        elif action == "select":
            lines.append(f"    def select_{elem['name']}(self, value: str):")
            lines.append(f"        self.{elem['name']}.select_option(value)")
            lines.append("")
        elif action in ("assert_visible", "assert_text"):
            lines.append(f"    def verify_{elem['name']}_visible(self):")
            lines.append(f"        expect(self.{elem['name']}).to_be_visible()")
            lines.append("")

    return "\n".join(lines)


# Legacy API compatibility
_active_process: Optional[subprocess.Popen] = None
_output_file: str = ""


def launch_codegen(base_url: str, output_path: str = "recorded_script.py") -> bool:
    """Legacy: Launch playwright codegen in background."""
    result = start_recording(tc_id=0, base_url=base_url, user_id="default")
    return result.get("status") == "recording"


def stop_codegen() -> str:
    """Legacy: Stop the running codegen process."""
    result = stop_recording(user_id="default")
    return result.get("output_file", "")
