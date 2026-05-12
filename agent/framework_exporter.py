"""Framework Export Engine — Export tests as a standalone Playwright Python project.

Generates a complete, ready-to-run Playwright Python test framework from the
platform's test cases, including:
- conftest.py with fixtures
- Page Object Model files
- Test files organized by suite
- requirements.txt
- pytest.ini configuration
- README with setup instructions
"""

import json
import logging
import os
import re
import zipfile
from dataclasses import dataclass, field
from io import BytesIO
from typing import Optional

logger = logging.getLogger(__name__)


@dataclass
class ExportConfig:
    """Configuration for framework export."""

    project_name: str = "playwright_tests"
    include_page_objects: bool = True
    include_test_data: bool = True
    include_api_tests: bool = True
    include_conftest: bool = True
    include_readme: bool = True
    base_url: str = ""
    author: str = ""


@dataclass
class ExportResult:
    """Result of framework export."""

    success: bool
    project_name: str
    file_count: int
    total_tests: int
    files: dict = field(default_factory=dict)  # {path: content}
    zip_bytes: Optional[bytes] = None
    warnings: list = field(default_factory=list)

    def to_dict(self) -> dict:
        return {
            "success": self.success,
            "project_name": self.project_name,
            "file_count": self.file_count,
            "total_tests": self.total_tests,
            "files": list(self.files.keys()),
            "warnings": self.warnings,
        }


class FrameworkExporter:
    """Exports platform tests as a standalone Playwright Python framework."""

    def export(self, suites: list[dict], config: Optional[ExportConfig] = None) -> ExportResult:
        """Export all test suites as a Playwright Python project.

        Args:
            suites: List of suite dicts, each containing:
                - name, flow_type
                - test_cases: [{name, flow_type, base_url, steps, data, endpoints}]
            config: Export configuration

        Returns:
            ExportResult with all generated files
        """
        if config is None:
            config = ExportConfig()

        result = ExportResult(
            success=True,
            project_name=config.project_name,
            file_count=0,
            total_tests=0,
        )

        files = {}

        # Generate requirements.txt
        files["requirements.txt"] = self._gen_requirements()

        # Generate pytest.ini
        files["pytest.ini"] = self._gen_pytest_ini(config)

        # Generate conftest.py
        if config.include_conftest:
            files["conftest.py"] = self._gen_conftest(config)

        # Generate README
        if config.include_readme:
            files["README.md"] = self._gen_readme(config, suites)

        # Generate page objects
        if config.include_page_objects:
            page_objects = self._gen_page_objects(suites)
            for name, content in page_objects.items():
                files[f"pages/{name}"] = content
            if page_objects:
                files["pages/__init__.py"] = ""

        # Generate test files per suite
        for suite in suites:
            suite_name = self._to_snake_case(suite.get("name", "default"))
            flow_type = suite.get("flow_type", "web")
            test_cases = suite.get("test_cases", [])

            if not test_cases:
                continue

            if flow_type == "web":
                test_file = self._gen_web_test_file(suite_name, test_cases, config)
            else:
                if config.include_api_tests:
                    test_file = self._gen_api_test_file(suite_name, test_cases, config)
                else:
                    continue

            files[f"tests/test_{suite_name}.py"] = test_file
            result.total_tests += len(test_cases)

        # Generate test data files
        if config.include_test_data:
            data_files = self._gen_test_data(suites)
            for name, content in data_files.items():
                files[f"data/{name}"] = content
            if data_files:
                files["data/__init__.py"] = ""

        # Add __init__.py files
        files["tests/__init__.py"] = ""

        result.files = files
        result.file_count = len(files)

        # Generate zip
        result.zip_bytes = self._create_zip(files, config.project_name)

        return result

    def _gen_requirements(self) -> str:
        return """playwright>=1.44.0
pytest>=8.0.0
pytest-playwright>=0.5.0
pytest-html>=4.0.0
pytest-xdist>=3.5.0
python-dotenv>=1.0.1
requests>=2.32.2
"""

    def _gen_pytest_ini(self, config: ExportConfig) -> str:
        lines = ["[pytest]", "testpaths = tests", "python_files = test_*.py"]
        if config.base_url:
            lines.append(f"base_url = {config.base_url}")
        lines.append("markers =")
        lines.append("    smoke: Quick smoke tests")
        lines.append("    regression: Full regression suite")
        lines.append("    api: API tests")
        lines.append("    web: Web UI tests")
        return "\n".join(lines) + "\n"

    def _gen_conftest(self, config: ExportConfig) -> str:
        return f'''"""Pytest configuration and fixtures for {config.project_name}."""

import json
import os

import pytest
from playwright.sync_api import Page, BrowserContext


@pytest.fixture(scope="session")
def browser_context_args(browser_context_args):
    """Configure browser context."""
    return {{
        **browser_context_args,
        "viewport": {{"width": 1280, "height": 720}},
        "ignore_https_errors": True,
    }}


@pytest.fixture
def page(page: Page):
    """Page fixture with configured defaults."""
    page.set_default_timeout(30000)
    page.set_default_navigation_timeout(30000)
    yield page


@pytest.fixture
def base_url():
    """Base URL from environment or default."""
    return os.getenv("BASE_URL", "{config.base_url or "http://localhost:3000"}")


@pytest.fixture
def test_data():
    """Load test data from JSON files."""
    data_dir = os.path.join(os.path.dirname(__file__), "data")

    def _load(filename):
        filepath = os.path.join(data_dir, filename)
        if os.path.exists(filepath):
            with open(filepath) as f:
                return json.load(f)
        return {{}}

    return _load
'''

    def _gen_readme(self, config: ExportConfig, suites: list[dict]) -> str:
        total_tc = sum(len(s.get("test_cases", [])) for s in suites)
        web_count = sum(len(s.get("test_cases", [])) for s in suites if s.get("flow_type") == "web")
        api_count = sum(len(s.get("test_cases", [])) for s in suites if s.get("flow_type") == "api")

        return f"""# {config.project_name}

Playwright Python test automation framework.

## Overview

- **Total Test Cases:** {total_tc}
- **Web Tests:** {web_count}
- **API Tests:** {api_count}
- **Suites:** {len(suites)}

## Setup

```bash
python -m venv .venv
source .venv/bin/activate  # Linux/Mac
pip install -r requirements.txt
playwright install chromium
```

## Running Tests

```bash
# Run all tests
pytest tests/ -v

# Run with HTML report
pytest tests/ --html=report.html --self-contained-html

# Run only web tests
pytest tests/ -m web -v

# Run only API tests
pytest tests/ -m api -v

# Run in parallel (4 workers)
pytest tests/ -n 4

# Run specific suite
pytest tests/test_<suite_name>.py -v
```

## Configuration

Set `BASE_URL` environment variable to override the default base URL:

```bash
export BASE_URL=https://staging.example.com
pytest tests/ -v
```

## Project Structure

```
{config.project_name}/
├── tests/          # Test files organized by suite
├── pages/          # Page Object Model classes
├── data/           # Test data JSON files
├── conftest.py     # Fixtures and configuration
├── requirements.txt
├── pytest.ini
└── README.md
```
"""

    def _gen_web_test_file(self, suite_name: str, test_cases: list[dict],
                           config: ExportConfig) -> str:
        """Generate a web test file for a suite."""
        lines = [f'"""Web tests for suite: {suite_name}."""', ""]
        lines.append("import pytest")
        lines.append("from playwright.sync_api import Page, expect")
        lines.append("")
        lines.append("")
        lines.append(f"@pytest.mark.web")
        lines.append(f"class Test{self._to_class_name(suite_name)}:")
        lines.append(f'    """Web test suite: {suite_name}."""')
        lines.append("")

        for tc in test_cases:
            tc_name = self._to_snake_case(tc.get("name", "unnamed"))
            base_url = tc.get("base_url", config.base_url or "")
            steps = tc.get("steps", [])
            data_rows = tc.get("data", [])

            if data_rows:
                # Data-driven test with parametrize
                param_keys = list(data_rows[0].get("data_json", {}).keys()) if data_rows else []
                if param_keys:
                    param_values = [
                        tuple(row.get("data_json", {}).get(k, "") for k in param_keys)
                        for row in data_rows
                    ]
                    lines.append(f"    @pytest.mark.parametrize(")
                    lines.append(f'        "{", ".join(param_keys)}",')
                    lines.append(f"        {param_values},")
                    lines.append(f"    )")
                    param_signature = ", ".join(param_keys)
                    lines.append(f"    def test_{tc_name}(self, page: Page, {param_signature}):")
                else:
                    lines.append(f"    def test_{tc_name}(self, page: Page):")
            else:
                lines.append(f"    def test_{tc_name}(self, page: Page):")

            lines.append(f'        """Test: {tc.get("name", tc_name)}."""')

            if base_url:
                lines.append(f'        page.goto("{base_url}")')

            for step in steps:
                pw_line = self._step_to_playwright(step)
                if pw_line:
                    lines.append(f"        {pw_line}")

            if not steps:
                lines.append("        pass  # TODO: Add test steps")

            lines.append("")

        return "\n".join(lines)

    def _gen_api_test_file(self, suite_name: str, test_cases: list[dict],
                           config: ExportConfig) -> str:
        """Generate an API test file for a suite."""
        lines = [f'"""API tests for suite: {suite_name}."""', ""]
        lines.append("import json")
        lines.append("")
        lines.append("import pytest")
        lines.append("import requests")
        lines.append("")
        lines.append("")
        lines.append(f"@pytest.mark.api")
        lines.append(f"class Test{self._to_class_name(suite_name)}Api:")
        lines.append(f'    """API test suite: {suite_name}."""')
        lines.append("")

        for tc in test_cases:
            tc_name = self._to_snake_case(tc.get("name", "unnamed"))
            base_url = tc.get("base_url", config.base_url or "http://localhost:8080")
            endpoints = tc.get("endpoints", [])

            for ep in endpoints:
                method = ep.get("method", "GET").upper()
                path = ep.get("path", "/")
                expected_status = ep.get("expected_status", 200)
                summary = ep.get("summary", "")

                ep_name = self._to_snake_case(f"{method}_{path.replace('/', '_')}")
                lines.append(f"    def test_{ep_name}(self):")
                lines.append(f'        """API: {method} {path} — {summary}."""')
                lines.append(f'        url = "{base_url}{path}"')

                headers = ep.get("request_headers")
                if headers:
                    lines.append(f"        headers = {json.dumps(headers)}")
                else:
                    lines.append('        headers = {"Content-Type": "application/json"}')

                body = ep.get("request_body")
                if body and method in ("POST", "PUT", "PATCH"):
                    lines.append(f"        body = {json.dumps(body)}")
                    lines.append(f'        response = requests.{method.lower()}(url, json=body, headers=headers, timeout=30)')
                else:
                    lines.append(f'        response = requests.{method.lower()}(url, headers=headers, timeout=30)')

                lines.append(f"        assert response.status_code == {expected_status}")
                lines.append("")

            if not endpoints:
                lines.append(f"    def test_{tc_name}(self):")
                lines.append("        pass  # TODO: Add API test endpoints")
                lines.append("")

        return "\n".join(lines)

    def _gen_page_objects(self, suites: list[dict]) -> dict[str, str]:
        """Generate Page Object Model files from test steps."""
        pages = {}  # page_name -> {selectors}
        
        for suite in suites:
            for tc in suite.get("test_cases", []):
                if tc.get("flow_type") != "web":
                    continue
                # Group selectors by page (inferred from navigate steps)
                current_page = "base"
                page_selectors = {}

                for step in tc.get("steps", []):
                    if step.get("action") == "navigate":
                        url = step.get("value", "")
                        current_page = self._url_to_page_name(url)
                    elif step.get("selector"):
                        if current_page not in page_selectors:
                            page_selectors[current_page] = set()
                        page_selectors[current_page].add(
                            (step.get("selector"), step.get("action"), step.get("description", ""))
                        )

                for page_name, selectors in page_selectors.items():
                    if page_name not in pages:
                        pages[page_name] = set()
                    pages[page_name].update(selectors)

        result = {}
        for page_name, selectors in pages.items():
            if not selectors:
                continue
            result[f"{page_name}_page.py"] = self._gen_page_object_file(page_name, selectors)

        return result

    def _gen_page_object_file(self, page_name: str, selectors: set) -> str:
        """Generate a single Page Object Model file."""
        class_name = self._to_class_name(page_name) + "Page"
        lines = [f'"""Page Object: {class_name}."""', ""]
        lines.append("from playwright.sync_api import Page")
        lines.append("")
        lines.append("")
        lines.append(f"class {class_name}:")
        lines.append(f'    """Page object for {page_name}."""')
        lines.append("")
        lines.append("    def __init__(self, page: Page):")
        lines.append("        self.page = page")
        lines.append("")

        # Generate locator properties
        seen_names = set()
        for selector, action, description in sorted(selectors):
            prop_name = self._selector_to_prop_name(selector)
            if prop_name in seen_names:
                continue
            seen_names.add(prop_name)

            lines.append("    @property")
            lines.append(f"    def {prop_name}(self):")
            if description:
                lines.append(f'        """{description}."""')
            lines.append(f"        return self.page.locator('{selector}')")
            lines.append("")

        return "\n".join(lines)

    def _gen_test_data(self, suites: list[dict]) -> dict[str, str]:
        """Generate test data JSON files."""
        data_files = {}

        for suite in suites:
            for tc in suite.get("test_cases", []):
                data_rows = tc.get("data", [])
                if not data_rows:
                    continue
                tc_name = self._to_snake_case(tc.get("name", "data"))
                data_content = [row.get("data_json", {}) for row in data_rows]
                data_files[f"{tc_name}.json"] = json.dumps(data_content, indent=2)

        return data_files

    def _step_to_playwright(self, step: dict) -> str:
        """Convert a test step to a Playwright Python line."""
        action = step.get("action", "").lower()
        selector = step.get("selector", "")
        value = step.get("value", "")

        if action == "navigate":
            return f'page.goto("{value}")'
        elif action == "click":
            return f'page.locator("{selector}").click()'
        elif action == "dblclick":
            return f'page.locator("{selector}").dblclick()'
        elif action == "fill":
            return f'page.locator("{selector}").fill("{value}")'
        elif action == "type":
            return f'page.locator("{selector}").type("{value}")'
        elif action == "press":
            return f'page.locator("{selector}").press("{value}")'
        elif action == "select":
            return f'page.locator("{selector}").select_option("{value}")'
        elif action == "hover":
            return f'page.locator("{selector}").hover()'
        elif action == "check":
            return f'page.locator("{selector}").check()'
        elif action == "uncheck":
            return f'page.locator("{selector}").uncheck()'
        elif action == "clear":
            return f'page.locator("{selector}").clear()'
        elif action == "assert_text":
            return f'expect(page.locator("{selector}")).to_contain_text("{value}")'
        elif action == "assert_visible":
            return f'expect(page.locator("{selector}")).to_be_visible()'
        elif action == "assert_url":
            return f'expect(page).to_have_url(re.compile(".*{value}.*"))'
        elif action == "assert_value":
            return f'expect(page.locator("{selector}")).to_have_value("{value}")'
        elif action == "wait":
            return f'page.wait_for_timeout({value or 1000})'
        elif action == "screenshot":
            return 'page.screenshot(path="screenshot.png")'
        elif action == "scroll":
            return f'page.locator("{selector}").scroll_into_view_if_needed()' if selector else f'page.evaluate("window.scrollBy(0, {value or 300})")'
        elif action == "upload":
            return f'page.locator("{selector}").set_input_files("{value}")'
        return f"# TODO: {action} {selector} {value}"

    def _create_zip(self, files: dict[str, str], project_name: str) -> bytes:
        """Create a zip archive from the generated files."""
        buffer = BytesIO()
        with zipfile.ZipFile(buffer, 'w', zipfile.ZIP_DEFLATED) as zf:
            for path, content in files.items():
                zf.writestr(f"{project_name}/{path}", content)
        return buffer.getvalue()

    def _to_snake_case(self, name: str) -> str:
        name = re.sub(r'[^a-zA-Z0-9]', '_', name)
        name = re.sub(r'([A-Z]+)([A-Z][a-z])', r'\1_\2', name)
        name = re.sub(r'([a-z])([A-Z])', r'\1_\2', name)
        return name.lower().strip('_')

    def _to_class_name(self, name: str) -> str:
        parts = re.split(r'[_\-\s]+', name)
        return "".join(p.capitalize() for p in parts if p)

    def _url_to_page_name(self, url: str) -> str:
        path = url.split("://")[-1].split("/", 1)[-1] if "://" in url else url
        path = path.split("?")[0].strip("/")
        if not path:
            return "home"
        parts = path.split("/")
        return self._to_snake_case(parts[-1]) or "home"

    def _selector_to_prop_name(self, selector: str) -> str:
        # Extract meaningful name from selector
        id_match = re.search(r'#([\w-]+)', selector)
        if id_match:
            return self._to_snake_case(id_match.group(1))
        name_match = re.search(r'\[name=["\']?([\w-]+)', selector)
        if name_match:
            return self._to_snake_case(name_match.group(1))
        testid_match = re.search(r'\[data-testid=["\']?([\w-]+)', selector)
        if testid_match:
            return self._to_snake_case(testid_match.group(1))
        # Fallback: clean the selector
        clean = re.sub(r'[^a-zA-Z0-9]', '_', selector)
        return self._to_snake_case(clean)[:30] or "element"


# Singleton
framework_exporter = FrameworkExporter()
