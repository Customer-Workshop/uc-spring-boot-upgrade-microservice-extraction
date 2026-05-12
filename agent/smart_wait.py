"""Smart Wait Strategies for Reliable Test Execution.

Modern test automation requires intelligent waiting rather than fixed delays.
This module provides adaptive wait strategies that reduce flakiness while
keeping execution fast.

Strategies:
- Network Idle: Wait until no pending network requests
- DOM Stable: Wait until DOM stops changing
- Animation Complete: Wait for CSS animations/transitions to finish
- Custom Conditions: Wait for arbitrary JavaScript conditions
"""

import logging
import time
from typing import Callable, Optional

logger = logging.getLogger(__name__)


class SmartWait:
    """Adaptive wait strategies for Playwright pages."""

    DEFAULT_TIMEOUT_MS = 10000
    POLL_INTERVAL_MS = 100

    @staticmethod
    def for_network_idle(page, timeout_ms: int = 5000) -> bool:
        """Wait until no network requests are pending.

        More reliable than fixed waits after form submissions or navigation.
        """
        try:
            page.wait_for_load_state("networkidle", timeout=timeout_ms)
            return True
        except Exception:
            logger.debug("Network idle wait timed out")
            return False

    @staticmethod
    def for_dom_stable(page, stability_ms: int = 500, timeout_ms: int = 5000) -> bool:
        """Wait until the DOM stops changing.

        Monitors DOM mutations and waits until no changes occur for stability_ms.
        Useful after dynamic content loading or SPA navigation.
        """
        try:
            page.evaluate(f"""() => {{
                return new Promise((resolve, reject) => {{
                    let timer = null;
                    let settled = false;
                    const timeout = setTimeout(() => {{
                        if (!settled) {{ settled = true; resolve(false); }}
                    }}, {timeout_ms});

                    const observer = new MutationObserver(() => {{
                        if (timer) clearTimeout(timer);
                        timer = setTimeout(() => {{
                            if (!settled) {{
                                settled = true;
                                clearTimeout(timeout);
                                observer.disconnect();
                                resolve(true);
                            }}
                        }}, {stability_ms});
                    }});

                    observer.observe(document.body, {{
                        childList: true,
                        subtree: true,
                        attributes: true,
                        characterData: true
                    }});

                    // Start the initial stability timer
                    timer = setTimeout(() => {{
                        if (!settled) {{
                            settled = true;
                            clearTimeout(timeout);
                            observer.disconnect();
                            resolve(true);
                        }}
                    }}, {stability_ms});
                }});
            }}""")
            return True
        except Exception as e:
            logger.debug(f"DOM stable wait failed: {e}")
            return False

    @staticmethod
    def for_animation_complete(page, selector: str = "*", timeout_ms: int = 5000) -> bool:
        """Wait for CSS animations and transitions to complete.

        Prevents interaction with elements during animation.
        """
        try:
            page.evaluate(f"""(selector) => {{
                return new Promise((resolve) => {{
                    const timeout = setTimeout(() => resolve(false), {timeout_ms});
                    const elements = document.querySelectorAll(selector);
                    const animations = [];
                    elements.forEach(el => {{
                        const anims = el.getAnimations();
                        animations.push(...anims);
                    }});
                    if (animations.length === 0) {{
                        clearTimeout(timeout);
                        resolve(true);
                        return;
                    }}
                    Promise.all(animations.map(a => a.finished)).then(() => {{
                        clearTimeout(timeout);
                        resolve(true);
                    }}).catch(() => {{
                        clearTimeout(timeout);
                        resolve(false);
                    }});
                }});
            }}""", selector)
            return True
        except Exception as e:
            logger.debug(f"Animation wait failed: {e}")
            return False

    @staticmethod
    def for_condition(page, js_condition: str, timeout_ms: int = 10000,
                      poll_ms: int = 100) -> bool:
        """Wait for a custom JavaScript condition to become true.

        Args:
            page: Playwright page
            js_condition: JavaScript expression that returns boolean
            timeout_ms: Maximum wait time
            poll_ms: Poll interval

        Example:
            SmartWait.for_condition(page, "document.querySelector('.loaded') !== null")
        """
        start = time.time()
        deadline = start + (timeout_ms / 1000)

        while time.time() < deadline:
            try:
                result = page.evaluate(f"() => {{ return Boolean({js_condition}); }}")
                if result:
                    return True
            except Exception:
                pass
            page.wait_for_timeout(poll_ms)

        logger.debug(f"Condition wait timed out: {js_condition}")
        return False

    @staticmethod
    def for_element_stable(page, selector: str, timeout_ms: int = 5000) -> bool:
        """Wait until an element's position and size stabilize.

        Useful for elements that animate into view or resize dynamically.
        """
        try:
            page.evaluate(f"""(selector) => {{
                return new Promise((resolve) => {{
                    const timeout = setTimeout(() => resolve(false), {timeout_ms});
                    const el = document.querySelector(selector);
                    if (!el) {{ clearTimeout(timeout); resolve(false); return; }}

                    let lastRect = el.getBoundingClientRect();
                    let stableCount = 0;

                    const check = () => {{
                        const rect = el.getBoundingClientRect();
                        if (rect.x === lastRect.x && rect.y === lastRect.y &&
                            rect.width === lastRect.width && rect.height === lastRect.height) {{
                            stableCount++;
                            if (stableCount >= 3) {{
                                clearTimeout(timeout);
                                resolve(true);
                                return;
                            }}
                        }} else {{
                            stableCount = 0;
                            lastRect = rect;
                        }}
                        requestAnimationFrame(check);
                    }};
                    requestAnimationFrame(check);
                }});
            }}""", selector)
            return True
        except Exception as e:
            logger.debug(f"Element stable wait failed: {e}")
            return False

    @staticmethod
    def for_url_change(page, current_url: str, timeout_ms: int = 10000) -> bool:
        """Wait until the page URL changes from the current one.

        Useful after clicking navigation links or form submissions.
        """
        start = time.time()
        deadline = start + (timeout_ms / 1000)

        while time.time() < deadline:
            if page.url != current_url:
                return True
            page.wait_for_timeout(100)

        return False

    @staticmethod
    def for_text_change(page, selector: str, current_text: str,
                        timeout_ms: int = 5000) -> bool:
        """Wait until an element's text content changes.

        Useful for loading indicators or dynamic content.
        """
        start = time.time()
        deadline = start + (timeout_ms / 1000)

        while time.time() < deadline:
            try:
                locator = page.locator(selector)
                text = locator.text_content(timeout=1000)
                if text != current_text:
                    return True
            except Exception:
                pass
            page.wait_for_timeout(100)

        return False
