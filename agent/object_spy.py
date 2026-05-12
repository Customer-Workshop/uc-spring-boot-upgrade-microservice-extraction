"""Object Spy — Interactive Element Inspector for Test Automation.

Captures all element properties from any page, providing:
- All available selectors (CSS, XPath, data-testid, aria, etc.)
- Element attributes, dimensions, visibility state
- Recommended "best selector" based on stability scoring
- Bulk capture mode for entire page sections
- Screenshot of individual elements
"""

import json
import logging
from dataclasses import dataclass, field
from typing import Optional

from agent.auto_healer import ElementFingerprint, recommend_selector, score_selector_stability

logger = logging.getLogger(__name__)


@dataclass
class SpiedElement:
    """Complete inspection result for a single element."""

    tag_name: str = ""
    text_content: str = ""
    inner_html: str = ""
    attributes: dict = field(default_factory=dict)
    computed_styles: dict = field(default_factory=dict)
    bounding_box: dict = field(default_factory=dict)
    is_visible: bool = False
    is_enabled: bool = False
    is_editable: bool = False

    # All discovered selectors ranked by stability
    selectors: list = field(default_factory=list)
    recommended_selector: str = ""
    recommended_strategy: str = ""
    stability_score: float = 0.0

    # Structural info
    parent_tag: str = ""
    parent_id: str = ""
    siblings_count: int = 0
    child_index: int = 0
    children_count: int = 0
    xpath: str = ""

    # Element fingerprint for auto-healing
    fingerprint: Optional[ElementFingerprint] = None

    def to_dict(self) -> dict:
        result = {
            "tag_name": self.tag_name,
            "text_content": self.text_content,
            "attributes": self.attributes,
            "bounding_box": self.bounding_box,
            "is_visible": self.is_visible,
            "is_enabled": self.is_enabled,
            "is_editable": self.is_editable,
            "selectors": self.selectors,
            "recommended_selector": self.recommended_selector,
            "recommended_strategy": self.recommended_strategy,
            "stability_score": self.stability_score,
            "xpath": self.xpath,
            "parent_tag": self.parent_tag,
            "children_count": self.children_count,
        }
        return result


class ObjectSpy:
    """Interactive element inspector for capturing page element properties."""

    def __init__(self):
        self._capture_history: list[SpiedElement] = []

    def inspect_element(self, page, selector: str) -> Optional[SpiedElement]:
        """
        Inspect a single element by selector and return all its properties.

        Args:
            page: Playwright Page object
            selector: CSS/XPath/Playwright selector to inspect

        Returns:
            SpiedElement with all properties, or None if element not found
        """
        try:
            locator = page.locator(selector)
            if locator.count() == 0:
                logger.warning(f"Object Spy: element not found with selector '{selector}'")
                return None

            if locator.count() > 1:
                logger.warning(f"Object Spy: multiple elements found ({locator.count()}), inspecting first")

            element = SpiedElement()

            # Basic properties
            element.tag_name = locator.first.evaluate("e => e.tagName.toLowerCase()")
            element.text_content = (locator.first.text_content() or "").strip()[:500]
            element.is_visible = locator.first.is_visible()
            element.is_enabled = locator.first.is_enabled()
            element.is_editable = locator.first.is_editable()

            # All attributes
            element.attributes = locator.first.evaluate("""e => {
                const attrs = {};
                for (const attr of e.attributes) {
                    attrs[attr.name] = attr.value;
                }
                return attrs;
            }""")

            # Bounding box
            bbox = locator.first.bounding_box()
            if bbox:
                element.bounding_box = {
                    "x": round(bbox["x"], 1),
                    "y": round(bbox["y"], 1),
                    "width": round(bbox["width"], 1),
                    "height": round(bbox["height"], 1),
                }

            # Structural info
            structural = locator.first.evaluate("""e => {
                const parent = e.parentElement;
                let childIdx = 0;
                let sibling = e.previousElementSibling;
                while (sibling) { childIdx++; sibling = sibling.previousElementSibling; }

                return {
                    parent_tag: parent ? parent.tagName.toLowerCase() : '',
                    parent_id: parent ? (parent.id || '') : '',
                    siblings_count: parent ? parent.children.length : 0,
                    child_index: childIdx,
                    children_count: e.children.length,
                };
            }""")
            element.parent_tag = structural["parent_tag"]
            element.parent_id = structural["parent_id"]
            element.siblings_count = structural["siblings_count"]
            element.child_index = structural["child_index"]
            element.children_count = structural["children_count"]

            # XPath
            element.xpath = locator.first.evaluate("""e => {
                let path = '';
                while (e && e.nodeType === Node.ELEMENT_NODE) {
                    let idx = 0;
                    let sibling = e.previousElementSibling;
                    while (sibling) {
                        if (sibling.tagName === e.tagName) idx++;
                        sibling = sibling.previousElementSibling;
                    }
                    const tagName = e.tagName.toLowerCase();
                    const suffix = idx > 0 ? '[' + (idx + 1) + ']' : '';
                    path = '/' + tagName + suffix + path;
                    e = e.parentElement;
                }
                return path;
            }""")

            # Build fingerprint
            fp = ElementFingerprint(
                tag_name=element.tag_name,
                text_content=element.text_content[:100],
                data_testid=element.attributes.get("data-testid", ""),
                aria_label=element.attributes.get("aria-label", ""),
                role=element.attributes.get("role", ""),
                id_attr=element.attributes.get("id", ""),
                name_attr=element.attributes.get("name", ""),
                class_names=(element.attributes.get("class", "")).split(),
                placeholder=element.attributes.get("placeholder", ""),
                href=element.attributes.get("href", ""),
                type_attr=element.attributes.get("type", ""),
                parent_tag=element.parent_tag,
                parent_id=element.parent_id,
                siblings_count=element.siblings_count,
                child_index=element.child_index,
                xpath=element.xpath,
            )
            element.fingerprint = fp

            # Generate all possible selectors with stability scores
            recommendation = recommend_selector(fp)
            all_selectors = []

            if recommendation["recommended"]:
                element.recommended_selector = recommendation["recommended"]["selector"]
                element.recommended_strategy = recommendation["recommended"]["strategy"]
                element.stability_score = recommendation["recommended"]["stability"]
                all_selectors.append(recommendation["recommended"])

            all_selectors.extend(recommendation.get("alternatives", []))
            element.selectors = all_selectors

            # Store in history
            self._capture_history.append(element)
            return element

        except Exception as e:
            logger.error(f"Object Spy: failed to inspect element '{selector}': {e}")
            return None

    def inspect_page(self, page, scope_selector: str = "body") -> list[SpiedElement]:
        """
        Bulk capture mode — inspect all interactive elements within a scope.

        Captures buttons, inputs, links, selects, textareas, and elements
        with explicit roles or data-testid attributes.

        Args:
            page: Playwright Page object
            scope_selector: CSS selector to limit the inspection scope

        Returns:
            List of SpiedElement for all interactive elements found
        """
        try:
            # Find all interactive elements
            interactive_selectors = [
                f"{scope_selector} button",
                f"{scope_selector} input",
                f"{scope_selector} a[href]",
                f"{scope_selector} select",
                f"{scope_selector} textarea",
                f"{scope_selector} [role]",
                f"{scope_selector} [data-testid]",
                f"{scope_selector} [onclick]",
            ]

            seen_xpaths = set()
            results = []

            for sel in interactive_selectors:
                try:
                    locator = page.locator(sel)
                    count = locator.count()

                    for i in range(min(count, 50)):  # Cap at 50 per selector type
                        try:
                            nth_sel = f"{sel} >> nth={i}"
                            element = self.inspect_element(page, nth_sel)
                            if element and element.xpath not in seen_xpaths:
                                seen_xpaths.add(element.xpath)
                                results.append(element)
                        except Exception:
                            continue
                except Exception:
                    continue

            logger.info(f"Object Spy: captured {len(results)} interactive elements")
            return results

        except Exception as e:
            logger.error(f"Object Spy: page inspection failed: {e}")
            return []

    def get_element_screenshot(self, page, selector: str, output_path: str) -> Optional[str]:
        """
        Take a screenshot of a specific element.

        Args:
            page: Playwright Page object
            selector: Element selector
            output_path: Path to save the screenshot

        Returns:
            Path to the screenshot file, or None on failure
        """
        try:
            locator = page.locator(selector)
            if locator.count() > 0:
                locator.first.screenshot(path=output_path)
                return output_path
        except Exception as e:
            logger.error(f"Object Spy: screenshot failed for '{selector}': {e}")
        return None

    def suggest_test_steps(self, elements: list[SpiedElement]) -> list[dict]:
        """
        Given a list of captured elements, suggest test steps that could interact with them.

        Returns a list of suggested test step dictionaries.
        """
        suggestions = []

        for el in elements:
            if el.tag_name == "input":
                input_type = el.attributes.get("type", "text")
                if input_type in ("text", "email", "password", "search", "tel", "url"):
                    suggestions.append({
                        "action": "fill",
                        "selector": el.recommended_selector,
                        "value": f"{{{{test_{el.attributes.get('name', 'input')}}}}}",
                        "description": f"Fill {el.attributes.get('placeholder', el.attributes.get('name', 'input field'))}",
                    })
                elif input_type in ("checkbox", "radio"):
                    suggestions.append({
                        "action": "click",
                        "selector": el.recommended_selector,
                        "value": "",
                        "description": f"Toggle {el.attributes.get('name', input_type)}",
                    })
            elif el.tag_name == "button" or el.attributes.get("role") == "button":
                suggestions.append({
                    "action": "click",
                    "selector": el.recommended_selector,
                    "value": "",
                    "description": f"Click {el.text_content[:30] or 'button'}",
                })
            elif el.tag_name == "a":
                suggestions.append({
                    "action": "click",
                    "selector": el.recommended_selector,
                    "value": "",
                    "description": f"Navigate via link: {el.text_content[:30] or el.attributes.get('href', 'link')}",
                })
            elif el.tag_name == "select":
                suggestions.append({
                    "action": "select",
                    "selector": el.recommended_selector,
                    "value": "",
                    "description": f"Select option from {el.attributes.get('name', 'dropdown')}",
                })
            elif el.tag_name == "textarea":
                suggestions.append({
                    "action": "fill",
                    "selector": el.recommended_selector,
                    "value": f"{{{{test_{el.attributes.get('name', 'textarea')}}}}}",
                    "description": f"Fill textarea {el.attributes.get('name', '')}",
                })

        return suggestions

    def get_capture_history(self) -> list[dict]:
        """Return all previously captured elements as dictionaries."""
        return [e.to_dict() for e in self._capture_history]

    def clear_history(self):
        """Clear capture history."""
        self._capture_history.clear()

    def export_locators(self, elements: list[SpiedElement], format: str = "json") -> str:
        """
        Export captured elements as a locator repository.

        Args:
            elements: List of SpiedElement to export
            format: 'json' or 'page_object' (Python POM)

        Returns:
            Exported content as string
        """
        if format == "page_object":
            return self._export_as_page_object(elements)
        return json.dumps([e.to_dict() for e in elements], indent=2)

    def _export_as_page_object(self, elements: list[SpiedElement]) -> str:
        """Export elements as a Playwright Python Page Object Model class."""
        lines = [
            '"""Auto-generated Page Object Model from Object Spy capture."""',
            "",
            "from playwright.sync_api import Page",
            "",
            "",
            "class CapturedPage:",
            '    """Page object with captured locators."""',
            "",
            "    def __init__(self, page: Page):",
            "        self.page = page",
            "",
        ]

        for el in elements:
            if not el.recommended_selector:
                continue
            # Create a safe property name
            name = el.attributes.get("name") or el.attributes.get("id") or el.text_content[:20]
            name = "".join(c if c.isalnum() else "_" for c in name).strip("_").lower()
            if not name:
                name = f"{el.tag_name}_{elements.index(el)}"

            lines.append(f"    @property")
            lines.append(f"    def {name}(self):")
            lines.append(f'        """Stability: {el.stability_score:.0%} ({el.recommended_strategy})"""')
            lines.append(f'        return self.page.locator(\'{el.recommended_selector}\')')
            lines.append("")

        return "\n".join(lines)


# Singleton instance
object_spy = ObjectSpy()
