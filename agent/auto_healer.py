"""Auto-Healing Engine for Playwright Test Execution.

When a selector fails during execution, the auto-healer automatically tries
alternative locator strategies to find the target element. It logs healing events
and provides suggestions to update the original locator for future stability.

Healing Strategy Priority:
1. data-testid (most stable, immune to styling changes)
2. aria-label / role (accessibility-based, stable)
3. ID / name attributes (classic DOM selectors)
4. CSS selector variants (parent-child relationships)
5. XPath (full path-based fallback)
6. Text content (visual text matching)
"""

import json
import logging
import re
from dataclasses import dataclass, field
from typing import Optional

logger = logging.getLogger(__name__)


@dataclass
class HealingResult:
    """Result of a healing attempt."""

    success: bool
    original_selector: str
    healed_selector: str = ""
    strategy_used: str = ""
    confidence: float = 0.0
    suggestions: list = field(default_factory=list)
    attempts: list = field(default_factory=list)

    def to_dict(self) -> dict:
        return {
            "success": self.success,
            "original_selector": self.original_selector,
            "healed_selector": self.healed_selector,
            "strategy_used": self.strategy_used,
            "confidence": self.confidence,
            "suggestions": self.suggestions,
            "attempts": self.attempts,
        }


@dataclass
class ElementFingerprint:
    """Captures multiple identifying attributes of an element for healing."""

    tag_name: str = ""
    text_content: str = ""
    data_testid: str = ""
    aria_label: str = ""
    role: str = ""
    id_attr: str = ""
    name_attr: str = ""
    class_names: list = field(default_factory=list)
    placeholder: str = ""
    href: str = ""
    src: str = ""
    type_attr: str = ""
    parent_tag: str = ""
    parent_id: str = ""
    siblings_count: int = 0
    child_index: int = 0
    xpath: str = ""
    css_path: str = ""


class AutoHealer:
    """Self-healing locator engine that tries alternative strategies when selectors fail."""

    # Strategy priority order (higher confidence = tried first)
    STRATEGIES = [
        ("data_testid", 0.95),
        ("aria_label", 0.90),
        ("role_based", 0.85),
        ("id_attr", 0.80),
        ("name_attr", 0.75),
        ("css_variant", 0.65),
        ("xpath_variant", 0.55),
        ("text_content", 0.50),
        ("placeholder", 0.45),
        ("structural", 0.35),
    ]

    def __init__(self):
        self.healing_log: list[dict] = []

    def heal(self, page, original_selector: str, action: str = "click") -> HealingResult:
        """
        Attempt to heal a broken selector by trying alternative strategies.

        Args:
            page: Playwright Page object
            original_selector: The selector that failed
            action: The action being attempted (for context)

        Returns:
            HealingResult with success/failure and the healed selector if found
        """
        result = HealingResult(success=False, original_selector=original_selector)

        # First, gather element fingerprint from the page context
        fingerprint = self._extract_fingerprint_from_selector(original_selector)

        # Try each strategy in priority order
        for strategy_name, confidence in self.STRATEGIES:
            alt_selector = self._generate_alternative(strategy_name, original_selector, fingerprint)
            if not alt_selector or alt_selector == original_selector:
                continue

            result.attempts.append({"strategy": strategy_name, "selector": alt_selector})

            try:
                locator = page.locator(alt_selector)
                if locator.count() == 1:
                    # Verify element is visible and actionable
                    if locator.is_visible():
                        result.success = True
                        result.healed_selector = alt_selector
                        result.strategy_used = strategy_name
                        result.confidence = confidence
                        result.suggestions = self._generate_suggestions(
                            original_selector, alt_selector, strategy_name
                        )

                        self._log_healing(original_selector, alt_selector, strategy_name, confidence)
                        logger.info(
                            f"Auto-healed: '{original_selector}' -> '{alt_selector}' "
                            f"(strategy={strategy_name}, confidence={confidence})"
                        )
                        return result
                elif locator.count() > 1:
                    # Multiple matches - try to narrow down
                    narrowed = self._narrow_down(page, alt_selector, fingerprint)
                    if narrowed:
                        result.success = True
                        result.healed_selector = narrowed
                        result.strategy_used = f"{strategy_name}_narrowed"
                        result.confidence = confidence * 0.8
                        result.suggestions = self._generate_suggestions(
                            original_selector, narrowed, strategy_name
                        )
                        self._log_healing(original_selector, narrowed, strategy_name, confidence * 0.8)
                        return result
            except Exception as e:
                result.attempts[-1]["error"] = str(e)
                continue

        # If all strategies fail, try a fuzzy text search as last resort
        fuzzy_result = self._try_fuzzy_match(page, original_selector)
        if fuzzy_result:
            result.success = True
            result.healed_selector = fuzzy_result
            result.strategy_used = "fuzzy_match"
            result.confidence = 0.25
            result.suggestions = [
                f"Consider replacing '{original_selector}' with a more stable selector like data-testid"
            ]
            self._log_healing(original_selector, fuzzy_result, "fuzzy_match", 0.25)

        return result

    def _extract_fingerprint_from_selector(self, selector: str) -> ElementFingerprint:
        """Extract identifying information from the original selector."""
        fp = ElementFingerprint()

        # Parse common selector patterns
        id_match = re.search(r'#([\w-]+)', selector)
        if id_match:
            fp.id_attr = id_match.group(1)

        class_match = re.findall(r'\.([\w-]+)', selector)
        if class_match:
            fp.class_names = class_match

        # data-testid
        testid_match = re.search(r'\[data-testid=["\']?([\w-]+)', selector)
        if testid_match:
            fp.data_testid = testid_match.group(1)

        # aria-label
        aria_match = re.search(r'\[aria-label=["\']?([^"\'\]]+)', selector)
        if aria_match:
            fp.aria_label = aria_match.group(1)

        # name attribute
        name_match = re.search(r'\[name=["\']?([\w-]+)', selector)
        if name_match:
            fp.name_attr = name_match.group(1)

        # placeholder
        ph_match = re.search(r'\[placeholder=["\']?([^"\'\]]+)', selector)
        if ph_match:
            fp.placeholder = ph_match.group(1)

        # Tag name
        tag_match = re.match(r'^(\w+)', selector)
        if tag_match and tag_match.group(1) not in ('div', 'span'):
            fp.tag_name = tag_match.group(1)

        # Text content (Playwright text= syntax)
        text_match = re.search(r'text=["\']?([^"\']+)', selector)
        if text_match:
            fp.text_content = text_match.group(1)

        return fp

    def _generate_alternative(self, strategy: str, original: str, fp: ElementFingerprint) -> Optional[str]:
        """Generate an alternative selector based on the strategy."""

        if strategy == "data_testid" and fp.id_attr:
            return f'[data-testid="{fp.id_attr}"]'

        elif strategy == "aria_label" and fp.aria_label:
            return f'[aria-label="{fp.aria_label}"]'

        elif strategy == "role_based":
            if fp.tag_name == "button":
                return 'role=button'
            elif fp.tag_name == "input":
                if fp.name_attr:
                    return f'input[name="{fp.name_attr}"]'
            elif fp.tag_name == "a":
                if fp.text_content:
                    return f'role=link[name="{fp.text_content}"]'

        elif strategy == "id_attr" and fp.id_attr:
            return f'#{fp.id_attr}'

        elif strategy == "name_attr" and fp.name_attr:
            return f'[name="{fp.name_attr}"]'

        elif strategy == "css_variant":
            if fp.class_names:
                # Try just the most specific class
                return f'.{fp.class_names[-1]}'

        elif strategy == "xpath_variant":
            if fp.id_attr:
                return f'//*[@id="{fp.id_attr}"]'
            elif fp.name_attr:
                return f'//*[@name="{fp.name_attr}"]'

        elif strategy == "text_content" and fp.text_content:
            return f'text="{fp.text_content}"'

        elif strategy == "placeholder" and fp.placeholder:
            return f'[placeholder="{fp.placeholder}"]'

        elif strategy == "structural":
            if fp.tag_name and fp.class_names:
                return f'{fp.tag_name}.{".".join(fp.class_names[:2])}'

        return None

    def _narrow_down(self, page, selector: str, fp: ElementFingerprint) -> Optional[str]:
        """Try to narrow down multiple matches to a single element."""
        # Add visibility filter
        try:
            locator = page.locator(f"{selector}:visible")
            if locator.count() == 1:
                return f"{selector}:visible"
        except Exception:
            pass

        # Add nth-child constraint
        if fp.child_index >= 0:
            try:
                nth_selector = f"{selector} >> nth={fp.child_index}"
                locator = page.locator(nth_selector)
                if locator.count() == 1:
                    return nth_selector
            except Exception:
                pass

        return None

    def _try_fuzzy_match(self, page, original: str) -> Optional[str]:
        """Last resort: try fuzzy text matching based on selector hints."""
        # Extract any text-like content from the selector
        text_hints = re.findall(r'[A-Z][a-z]+|[a-z]+', original)
        meaningful_words = [w for w in text_hints if len(w) > 3]

        for word in meaningful_words[:3]:
            try:
                locator = page.locator(f'text="{word}"')
                if locator.count() == 1 and locator.is_visible():
                    return f'text="{word}"'
            except Exception:
                continue
        return None

    def _generate_suggestions(self, original: str, healed: str, strategy: str) -> list[str]:
        """Generate improvement suggestions for the user."""
        suggestions = []

        if strategy == "text_content" or strategy == "fuzzy_match":
            suggestions.append(
                "Text-based selectors are fragile. Consider adding a data-testid attribute to this element."
            )

        if strategy.startswith("css_variant") or strategy == "structural":
            suggestions.append(
                "CSS class selectors can break on styling changes. "
                "Prefer data-testid or aria-label for stability."
            )

        suggestions.append(
            f"Update selector from '{original}' to '{healed}' in your test steps "
            f"for better stability."
        )

        return suggestions

    def _log_healing(self, original: str, healed: str, strategy: str, confidence: float):
        """Record a healing event for reporting."""
        self.healing_log.append({
            "original": original,
            "healed": healed,
            "strategy": strategy,
            "confidence": confidence,
        })

    def get_healing_report(self) -> list[dict]:
        """Return all healing events from this session."""
        return self.healing_log.copy()

    def clear_log(self):
        """Clear the healing log."""
        self.healing_log.clear()


def capture_element_fingerprint(page, selector: str) -> Optional[ElementFingerprint]:
    """
    Capture a full fingerprint of an element for future healing reference.
    This is called after a successful interaction to store fallback data.
    """
    try:
        el = page.locator(selector)
        if el.count() != 1:
            return None

        fp = ElementFingerprint()
        fp.tag_name = el.evaluate("e => e.tagName.toLowerCase()")
        fp.text_content = (el.text_content() or "").strip()[:100]
        fp.data_testid = el.get_attribute("data-testid") or ""
        fp.aria_label = el.get_attribute("aria-label") or ""
        fp.role = el.get_attribute("role") or ""
        fp.id_attr = el.get_attribute("id") or ""
        fp.name_attr = el.get_attribute("name") or ""
        fp.placeholder = el.get_attribute("placeholder") or ""
        fp.href = el.get_attribute("href") or ""
        fp.type_attr = el.get_attribute("type") or ""

        class_attr = el.get_attribute("class") or ""
        fp.class_names = class_attr.split() if class_attr else []

        # Get structural info
        fp.xpath = el.evaluate("""e => {
            let path = '';
            while (e && e.nodeType === Node.ELEMENT_NODE) {
                let idx = 0;
                let sibling = e.previousElementSibling;
                while (sibling) { idx++; sibling = sibling.previousElementSibling; }
                path = '/' + e.tagName.toLowerCase() + '[' + (idx+1) + ']' + path;
                e = e.parentElement;
            }
            return path;
        }""")

        return fp
    except Exception as e:
        logger.debug(f"Failed to capture fingerprint for '{selector}': {e}")
        return None


def score_selector_stability(selector: str) -> float:
    """
    Score a selector from 0.0 to 1.0 based on predicted stability.
    Higher scores = more stable, less likely to break.
    """
    if not selector:
        return 0.0

    # data-testid is the gold standard
    if "data-testid" in selector:
        return 0.95

    # aria-label is very stable
    if "aria-label" in selector:
        return 0.90

    # role-based selectors
    if selector.startswith("role="):
        return 0.85

    # ID selectors are usually stable
    if re.match(r'^#[\w-]+$', selector):
        return 0.80

    # name attribute
    if "[name=" in selector:
        return 0.75

    # Simple tag + attribute
    if re.match(r'^\w+\[[\w-]+=["\'][^"\']+["\']\]$', selector):
        return 0.70

    # Class-based selectors
    if re.match(r'^\.[\w-]+$', selector):
        return 0.50

    # Complex CSS selectors (chained classes, child combinators)
    if " > " in selector or " " in selector:
        return 0.35

    # XPath selectors
    if selector.startswith("//") or selector.startswith("/"):
        return 0.30

    # Text-based
    if "text=" in selector:
        return 0.25

    # nth-child based
    if "nth-child" in selector or "nth=" in selector:
        return 0.20

    return 0.40


def recommend_selector(fingerprint: ElementFingerprint) -> dict:
    """
    Given an element fingerprint, recommend the best selector strategy.
    Returns dict with recommended selector and alternatives ranked by stability.
    """
    candidates = []

    if fingerprint.data_testid:
        candidates.append({
            "selector": f'[data-testid="{fingerprint.data_testid}"]',
            "strategy": "data-testid",
            "stability": 0.95,
        })

    if fingerprint.aria_label:
        candidates.append({
            "selector": f'[aria-label="{fingerprint.aria_label}"]',
            "strategy": "aria-label",
            "stability": 0.90,
        })

    if fingerprint.role:
        sel = f'role={fingerprint.role}'
        if fingerprint.aria_label:
            sel += f'[name="{fingerprint.aria_label}"]'
        candidates.append({
            "selector": sel,
            "strategy": "role",
            "stability": 0.85,
        })

    if fingerprint.id_attr:
        candidates.append({
            "selector": f'#{fingerprint.id_attr}',
            "strategy": "id",
            "stability": 0.80,
        })

    if fingerprint.name_attr:
        candidates.append({
            "selector": f'[name="{fingerprint.name_attr}"]',
            "strategy": "name",
            "stability": 0.75,
        })

    if fingerprint.placeholder:
        candidates.append({
            "selector": f'[placeholder="{fingerprint.placeholder}"]',
            "strategy": "placeholder",
            "stability": 0.60,
        })

    if fingerprint.text_content and len(fingerprint.text_content) < 50:
        candidates.append({
            "selector": f'text="{fingerprint.text_content}"',
            "strategy": "text",
            "stability": 0.40,
        })

    if fingerprint.xpath:
        candidates.append({
            "selector": fingerprint.xpath,
            "strategy": "xpath",
            "stability": 0.30,
        })

    # Sort by stability descending
    candidates.sort(key=lambda x: x["stability"], reverse=True)

    return {
        "recommended": candidates[0] if candidates else None,
        "alternatives": candidates[1:],
    }


# Singleton instance
auto_healer = AutoHealer()
