"""Playwright codegen recorder: launch and parse recorded scripts."""

import os
import re
import subprocess
import signal
from typing import Optional


_active_process: Optional[subprocess.Popen] = None
_output_file: str = ""


def launch_codegen(base_url: str, output_path: str = "recorded_script.py") -> bool:
    """Launch playwright codegen in background.

    Returns True if started successfully.
    """
    global _active_process, _output_file
    _output_file = output_path

    try:
        cmd = ["python", "-m", "playwright", "codegen", "--target", "python", "-o", output_path, base_url]
        _active_process = subprocess.Popen(
            cmd,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            preexec_fn=os.setsid if os.name != "nt" else None,
        )
        return True
    except Exception:
        return False


def stop_codegen() -> str:
    """Stop the running codegen process and return the output file path."""
    global _active_process, _output_file

    if _active_process:
        try:
            if os.name != "nt":
                os.killpg(os.getpgid(_active_process.pid), signal.SIGTERM)
            else:
                _active_process.terminate()
        except (ProcessLookupError, OSError):
            pass
        _active_process = None

    return _output_file


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

    # page.click("selector")
    m = re.match(r'page\.click\(["\'](.+?)["\']\)', line)
    if m:
        return {"action": "click", "selector": m.group(1), "value": "", "description": f"Click {m.group(1)}"}

    # page.dblclick("selector")
    m = re.match(r'page\.dblclick\(["\'](.+?)["\']\)', line)
    if m:
        return {"action": "dblclick", "selector": m.group(1), "value": "", "description": f"Double-click {m.group(1)}"}

    # page.fill("selector", "value")
    m = re.match(r'page\.fill\(["\'](.+?)["\'],\s*["\'](.+?)["\']\)', line)
    if m:
        return {"action": "fill", "selector": m.group(1), "value": m.group(2), "description": f"Fill {m.group(1)} with '{m.group(2)}'"}

    # page.type("selector", "value")
    m = re.match(r'page\.type\(["\'](.+?)["\'],\s*["\'](.+?)["\']\)', line)
    if m:
        return {"action": "type", "selector": m.group(1), "value": m.group(2), "description": f"Type '{m.group(2)}' into {m.group(1)}"}

    # page.press("selector", "key")
    m = re.match(r'page\.press\(["\'](.+?)["\'],\s*["\'](.+?)["\']\)', line)
    if m:
        return {"action": "press", "selector": m.group(1), "value": m.group(2), "description": f"Press {m.group(2)} on {m.group(1)}"}

    # page.select_option("selector", "value")
    m = re.match(r'page\.select_option\(["\'](.+?)["\'],\s*["\'](.+?)["\']\)', line)
    if m:
        return {"action": "select", "selector": m.group(1), "value": m.group(2), "description": f"Select '{m.group(2)}' in {m.group(1)}"}

    # expect(page.locator("selector")).to_be_visible()
    m = re.match(r'expect\(page\.locator\(["\'](.+?)["\']\)\)\.to_be_visible', line)
    if m:
        return {"action": "assert_visible", "selector": m.group(1), "value": "", "description": f"Assert {m.group(1)} is visible"}

    # page.wait_for_timeout(ms)
    m = re.match(r'page\.wait_for_timeout\((\d+)\)', line)
    if m:
        return {"action": "wait", "selector": "", "value": m.group(1), "description": f"Wait {m.group(1)}ms"}

    return None
