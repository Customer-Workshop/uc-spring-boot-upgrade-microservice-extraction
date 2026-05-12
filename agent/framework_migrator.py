"""Framework Migration Engine — Convert test frameworks to Playwright Python.

Supports migration from:
- Selenium (Java, Python, C#, JavaScript)
- TestNG / JUnit (Java)
- Cypress (JavaScript/TypeScript)
- Protractor (JavaScript/TypeScript)
- Robot Framework
- Katalon / Appium

Migration Process:
1. Upload source files (zip or individual files)
2. Detect source framework and language
3. Parse test structure (classes, methods, locators, assertions)
4. Convert to Playwright Python equivalent
5. Generate output with proper project structure
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
class MigratedTest:
    """A single migrated test method/function."""

    name: str
    steps: list = field(default_factory=list)
    assertions: list = field(default_factory=list)
    original_code: str = ""
    playwright_code: str = ""


@dataclass
class MigratedFile:
    """A migrated test file."""

    original_path: str
    original_language: str
    output_path: str
    tests: list = field(default_factory=list)
    page_objects: list = field(default_factory=list)
    imports_needed: set = field(default_factory=set)
    playwright_code: str = ""


@dataclass
class MigrationResult:
    """Complete migration result."""

    source_framework: str
    source_language: str
    files_processed: int
    tests_migrated: int
    warnings: list = field(default_factory=list)
    migrated_files: list = field(default_factory=list)
    project_structure: dict = field(default_factory=dict)

    def to_dict(self) -> dict:
        return {
            "source_framework": self.source_framework,
            "source_language": self.source_language,
            "files_processed": self.files_processed,
            "tests_migrated": self.tests_migrated,
            "warnings": self.warnings,
            "migrated_files": [
                {"path": f.output_path, "tests_count": len(f.tests), "code": f.playwright_code}
                for f in self.migrated_files
            ],
            "project_structure": self.project_structure,
        }


# Selenium Java → Playwright Python mappings
SELENIUM_JAVA_ACTIONS = {
    r'driver\.findElement\(By\.id\("([^"]+)"\)\)\.click\(\)': 'page.locator("#{0}").click()',
    r'driver\.findElement\(By\.id\("([^"]+)"\)\)\.sendKeys\("([^"]+)"\)': 'page.locator("#{0}").fill("{1}")',
    r'driver\.findElement\(By\.className\("([^"]+)"\)\)\.click\(\)': 'page.locator(".{0}").click()',
    r'driver\.findElement\(By\.cssSelector\("([^"]+)"\)\)\.click\(\)': 'page.locator("{0}").click()',
    r'driver\.findElement\(By\.cssSelector\("([^"]+)"\)\)\.sendKeys\("([^"]+)"\)': 'page.locator("{0}").fill("{1}")',
    r'driver\.findElement\(By\.xpath\("([^"]+)"\)\)\.click\(\)': 'page.locator("xpath={0}").click()',
    r'driver\.findElement\(By\.xpath\("([^"]+)"\)\)\.sendKeys\("([^"]+)"\)': 'page.locator("xpath={0}").fill("{1}")',
    r'driver\.findElement\(By\.name\("([^"]+)"\)\)\.click\(\)': 'page.locator("[name=\\"{0}\\"]").click()',
    r'driver\.findElement\(By\.name\("([^"]+)"\)\)\.sendKeys\("([^"]+)"\)': 'page.locator("[name=\\"{0}\\"]").fill("{1}")',
    r'driver\.findElement\(By\.linkText\("([^"]+)"\)\)\.click\(\)': 'page.locator("text={0}").click()',
    r'driver\.get\("([^"]+)"\)': 'page.goto("{0}")',
    r'driver\.getTitle\(\)': 'page.title()',
    r'driver\.getCurrentUrl\(\)': 'page.url',
    r'driver\.navigate\(\)\.to\("([^"]+)"\)': 'page.goto("{0}")',
    r'driver\.navigate\(\)\.back\(\)': 'page.go_back()',
    r'driver\.navigate\(\)\.forward\(\)': 'page.go_forward()',
    r'driver\.navigate\(\)\.refresh\(\)': 'page.reload()',
    r'Thread\.sleep\((\d+)\)': 'page.wait_for_timeout({0})',
    r'driver\.switchTo\(\)\.frame\("([^"]+)"\)': 'page.frame_locator("#{0}")',
    r'driver\.switchTo\(\)\.defaultContent\(\)': '# Switch back to main frame',
    r'Select\s+\w+\s*=\s*new\s+Select\(.*?\);\s*\w+\.selectByVisibleText\("([^"]+)"\)': 'page.locator("select").select_option(label="{0}")',
}

# Selenium Java assertions → Playwright Python
SELENIUM_JAVA_ASSERTIONS = {
    r'Assert\.assertEquals\("([^"]+)",\s*(.+?)\)': 'assert {1} == "{0}"',
    r'Assert\.assertTrue\((.+?)\)': 'assert {0}',
    r'Assert\.assertFalse\((.+?)\)': 'assert not {0}',
    r'assertEquals\("([^"]+)",\s*(.+?)\)': 'assert {1} == "{0}"',
    r'assertTrue\((.+?)\.isDisplayed\(\)\)': 'expect(page.locator({0})).to_be_visible()',
}

# Cypress → Playwright Python mappings
CYPRESS_ACTIONS = {
    r"cy\.visit\(['\"]([^'\"]+)['\"]\)": 'page.goto("{0}")',
    r"cy\.get\(['\"]([^'\"]+)['\"]\)\.click\(\)": 'page.locator("{0}").click()',
    r"cy\.get\(['\"]([^'\"]+)['\"]\)\.type\(['\"]([^'\"]+)['\"]\)": 'page.locator("{0}").fill("{1}")',
    r"cy\.get\(['\"]([^'\"]+)['\"]\)\.clear\(\)": 'page.locator("{0}").clear()',
    r"cy\.get\(['\"]([^'\"]+)['\"]\)\.should\(['\"]be\.visible['\"]\)": 'expect(page.locator("{0}")).to_be_visible()',
    r"cy\.get\(['\"]([^'\"]+)['\"]\)\.should\(['\"]have\.text['\"],\s*['\"]([^'\"]+)['\"]\)": 'expect(page.locator("{0}")).to_have_text("{1}")',
    r"cy\.get\(['\"]([^'\"]+)['\"]\)\.should\(['\"]contain['\"],\s*['\"]([^'\"]+)['\"]\)": 'expect(page.locator("{0}")).to_contain_text("{1}")',
    r"cy\.get\(['\"]([^'\"]+)['\"]\)\.select\(['\"]([^'\"]+)['\"]\)": 'page.locator("{0}").select_option("{1}")',
    r"cy\.get\(['\"]([^'\"]+)['\"]\)\.check\(\)": 'page.locator("{0}").check()',
    r"cy\.get\(['\"]([^'\"]+)['\"]\)\.uncheck\(\)": 'page.locator("{0}").uncheck()',
    r"cy\.url\(\)\.should\(['\"]include['\"],\s*['\"]([^'\"]+)['\"]\)": 'expect(page).to_have_url(re.compile(".*{0}.*"))',
    r"cy\.title\(\)\.should\(['\"]eq['\"],\s*['\"]([^'\"]+)['\"]\)": 'expect(page).to_have_title("{0}")',
    r"cy\.wait\((\d+)\)": 'page.wait_for_timeout({0})',
    r"cy\.contains\(['\"]([^'\"]+)['\"]\)\.click\(\)": 'page.locator("text={0}").click()',
}

# Selenium Python → Playwright Python
SELENIUM_PYTHON_ACTIONS = {
    r'driver\.find_element\(By\.ID,\s*["\']([^"\']+)["\']\)\.click\(\)': 'page.locator("#{0}").click()',
    r'driver\.find_element\(By\.ID,\s*["\']([^"\']+)["\']\)\.send_keys\(["\']([^"\']+)["\']\)': 'page.locator("#{0}").fill("{1}")',
    r'driver\.find_element\(By\.CSS_SELECTOR,\s*["\']([^"\']+)["\']\)\.click\(\)': 'page.locator("{0}").click()',
    r'driver\.find_element\(By\.CSS_SELECTOR,\s*["\']([^"\']+)["\']\)\.send_keys\(["\']([^"\']+)["\']\)': 'page.locator("{0}").fill("{1}")',
    r'driver\.find_element\(By\.XPATH,\s*["\']([^"\']+)["\']\)\.click\(\)': 'page.locator("xpath={0}").click()',
    r'driver\.find_element\(By\.XPATH,\s*["\']([^"\']+)["\']\)\.send_keys\(["\']([^"\']+)["\']\)': 'page.locator("xpath={0}").fill("{1}")',
    r'driver\.find_element\(By\.NAME,\s*["\']([^"\']+)["\']\)\.click\(\)': 'page.locator("[name=\\"{0}\\"]").click()',
    r'driver\.find_element\(By\.NAME,\s*["\']([^"\']+)["\']\)\.send_keys\(["\']([^"\']+)["\']\)': 'page.locator("[name=\\"{0}\\"]").fill("{1}")',
    r'driver\.get\(["\']([^"\']+)["\']\)': 'page.goto("{0}")',
    r'driver\.title': 'page.title()',
    r'driver\.current_url': 'page.url',
    r'driver\.back\(\)': 'page.go_back()',
    r'driver\.forward\(\)': 'page.go_forward()',
    r'driver\.refresh\(\)': 'page.reload()',
    r'time\.sleep\((\d+)\)': 'page.wait_for_timeout({0}000)',
    r'WebDriverWait\(driver,\s*(\d+)\)\.until\(': '# Playwright auto-waits; timeout={0}s',
}


class FrameworkMigrator:
    """Migrates test frameworks from various languages to Playwright Python."""

    SUPPORTED_FRAMEWORKS = {
        "selenium_java": {"extensions": [".java"], "markers": ["WebDriver", "selenium", "By."]},
        "selenium_python": {"extensions": [".py"], "markers": ["webdriver", "selenium", "By."]},
        "testng": {"extensions": [".java"], "markers": ["@Test", "testng", "TestNG"]},
        "junit": {"extensions": [".java"], "markers": ["@Test", "junit", "JUnit"]},
        "cypress": {"extensions": [".js", ".ts", ".cy.js", ".cy.ts"], "markers": ["cy.", "describe(", "it("]},
        "protractor": {"extensions": [".js", ".ts"], "markers": ["protractor", "browser.", "element("]},
        "robot": {"extensions": [".robot", ".txt"], "markers": ["*** Test Cases ***", "*** Keywords ***"]},
    }

    def detect_framework(self, files: dict[str, str]) -> tuple[str, str]:
        """Detect the source framework and language from file contents.

        Args:
            files: dict of {filename: content}

        Returns:
            Tuple of (framework_name, language)
        """
        scores = {fw: 0 for fw in self.SUPPORTED_FRAMEWORKS}

        for filename, content in files.items():
            ext = os.path.splitext(filename)[1].lower()

            for fw, info in self.SUPPORTED_FRAMEWORKS.items():
                if ext in info["extensions"]:
                    scores[fw] += 1
                for marker in info["markers"]:
                    if marker in content:
                        scores[fw] += 2

        best = max(scores, key=scores.get)
        if scores[best] == 0:
            return "unknown", "unknown"

        language_map = {
            "selenium_java": "java",
            "selenium_python": "python",
            "testng": "java",
            "junit": "java",
            "cypress": "javascript",
            "protractor": "javascript",
            "robot": "robot",
        }
        return best, language_map.get(best, "unknown")

    def migrate(self, files: dict[str, str], target_dir: str = "migrated") -> MigrationResult:
        """Migrate uploaded framework files to Playwright Python.

        Args:
            files: dict of {filename: file_content}
            target_dir: output directory prefix

        Returns:
            MigrationResult with all migrated files
        """
        framework, language = self.detect_framework(files)
        result = MigrationResult(
            source_framework=framework,
            source_language=language,
            files_processed=0,
            tests_migrated=0,
        )

        if framework == "unknown":
            result.warnings.append("Could not detect source framework. Attempting best-effort migration.")

        for filename, content in files.items():
            ext = os.path.splitext(filename)[1].lower()
            if ext not in (".java", ".py", ".js", ".ts", ".robot", ".txt"):
                continue

            result.files_processed += 1

            if framework in ("selenium_java", "testng", "junit"):
                migrated = self._migrate_java(filename, content)
            elif framework == "selenium_python":
                migrated = self._migrate_selenium_python(filename, content)
            elif framework in ("cypress",):
                migrated = self._migrate_cypress(filename, content)
            elif framework == "protractor":
                migrated = self._migrate_protractor(filename, content)
            elif framework == "robot":
                migrated = self._migrate_robot(filename, content)
            else:
                migrated = self._migrate_generic(filename, content, language)

            if migrated:
                result.migrated_files.append(migrated)
                result.tests_migrated += len(migrated.tests)

        # Generate project structure
        result.project_structure = self._generate_project_structure(result)
        return result

    def _migrate_java(self, filename: str, content: str) -> MigratedFile:
        """Migrate Selenium Java/TestNG/JUnit to Playwright Python."""
        migrated = MigratedFile(
            original_path=filename,
            original_language="java",
            output_path=self._java_to_python_path(filename),
        )

        # Extract class name
        class_match = re.search(r'class\s+(\w+)', content)
        class_name = class_match.group(1) if class_match else "MigratedTest"

        # Extract test methods
        test_methods = re.findall(
            r'@Test[^}]*?(?:public|private|protected)?\s+void\s+(\w+)\s*\([^)]*\)\s*\{([^}]+(?:\{[^}]*\}[^}]*)*)\}',
            content, re.DOTALL
        )

        # Simpler fallback pattern
        if not test_methods:
            test_methods = re.findall(
                r'(?:public|private)?\s*void\s+(\w+)\s*\([^)]*\)\s*(?:throws\s+\w+\s*)?\{([\s\S]*?)(?=\n\s*(?:public|private|protected|\}|@))',
                content
            )

        lines = ['"""Migrated from: ' + filename + '"""', ""]
        lines.append("import re")
        lines.append("from playwright.sync_api import Page, expect")
        lines.append("")
        lines.append("")
        lines.append(f"class Test{class_name}:")
        lines.append(f'    """Migrated from {class_name} (Java/Selenium)."""')
        lines.append("")

        for method_name, method_body in test_methods:
            test = MigratedTest(name=method_name, original_code=method_body.strip())
            converted_lines = self._convert_java_body(method_body)
            test.playwright_code = "\n        ".join(converted_lines)
            migrated.tests.append(test)

            py_name = self._to_snake_case(method_name)
            lines.append(f"    def test_{py_name}(self, page: Page):")
            lines.append(f'        """Migrated from {method_name}."""')
            for cl in converted_lines:
                lines.append(f"        {cl}")
            lines.append("")

        if not test_methods:
            # Fallback: convert the whole file as one test
            converted_lines = self._convert_java_body(content)
            lines.append("    def test_migrated(self, page: Page):")
            lines.append('        """Migrated test (whole file)."""')
            for cl in converted_lines:
                lines.append(f"        {cl}")
            migrated.tests.append(MigratedTest(name="migrated", original_code=content))

        migrated.playwright_code = "\n".join(lines)
        return migrated

    def _migrate_selenium_python(self, filename: str, content: str) -> MigratedFile:
        """Migrate Selenium Python to Playwright Python."""
        migrated = MigratedFile(
            original_path=filename,
            original_language="python",
            output_path=filename.replace(".py", "_playwright.py"),
        )

        lines = ['"""Migrated from: ' + filename + '"""', ""]
        lines.append("import re")
        lines.append("from playwright.sync_api import Page, expect")
        lines.append("")

        # Convert line by line
        converted = content
        for pattern, replacement in SELENIUM_PYTHON_ACTIONS.items():
            converted = re.sub(pattern, replacement, converted)

        # Remove selenium imports
        converted = re.sub(r'from selenium.*\n', '', converted)
        converted = re.sub(r'import selenium.*\n', '', converted)
        converted = re.sub(r'from webdriver.*\n', '', converted)

        # Replace driver setup/teardown
        converted = re.sub(r'driver\s*=\s*webdriver\.\w+\([^)]*\)', '# Browser managed by Playwright fixture', converted)
        converted = re.sub(r'driver\.quit\(\)', '# Browser cleanup handled by Playwright', converted)
        converted = re.sub(r'driver\.close\(\)', '# Browser cleanup handled by Playwright', converted)

        lines.append(converted)

        # Count test functions
        test_count = len(re.findall(r'def test_\w+', converted))
        for _ in range(test_count):
            migrated.tests.append(MigratedTest(name="test"))

        migrated.playwright_code = "\n".join(lines)
        return migrated

    def _migrate_cypress(self, filename: str, content: str) -> MigratedFile:
        """Migrate Cypress JS/TS to Playwright Python."""
        migrated = MigratedFile(
            original_path=filename,
            original_language="javascript",
            output_path=self._cypress_to_python_path(filename),
        )

        # Extract describe blocks
        describe_match = re.search(r"describe\(['\"]([^'\"]+)['\"]", content)
        class_name = describe_match.group(1) if describe_match else "MigratedCypress"
        class_name = re.sub(r'[^a-zA-Z0-9]', '', class_name)

        # Extract it() blocks
        it_blocks = re.findall(
            r"it\(['\"]([^'\"]+)['\"]\s*,\s*(?:(?:function\s*\(\))|(?:\(\)\s*=>))\s*\{([\s\S]*?)\}\s*\)",
            content
        )

        lines = ['"""Migrated from: ' + filename + '"""', ""]
        lines.append("import re")
        lines.append("from playwright.sync_api import Page, expect")
        lines.append("")
        lines.append("")
        lines.append(f"class Test{class_name}:")
        lines.append(f'    """Migrated from Cypress: {filename}."""')
        lines.append("")

        # Extract beforeEach for setup
        before_each = re.search(
            r"beforeEach\(\s*(?:(?:function\s*\(\))|(?:\(\)\s*=>))\s*\{([\s\S]*?)\}\s*\)",
            content
        )
        if before_each:
            setup_lines = self._convert_cypress_body(before_each.group(1))
            lines.append("    def setup_method(self, page: Page):")
            for sl in setup_lines:
                lines.append(f"        {sl}")
            lines.append("")

        for test_name, test_body in it_blocks:
            test = MigratedTest(name=test_name, original_code=test_body.strip())
            converted_lines = self._convert_cypress_body(test_body)
            test.playwright_code = "\n        ".join(converted_lines)
            migrated.tests.append(test)

            py_name = self._to_snake_case(test_name)
            lines.append(f"    def test_{py_name}(self, page: Page):")
            lines.append(f'        """Migrated: {test_name}."""')
            for cl in converted_lines:
                lines.append(f"        {cl}")
            lines.append("")

        if not it_blocks:
            converted_lines = self._convert_cypress_body(content)
            lines.append("    def test_migrated(self, page: Page):")
            for cl in converted_lines:
                lines.append(f"        {cl}")
            migrated.tests.append(MigratedTest(name="migrated"))

        migrated.playwright_code = "\n".join(lines)
        return migrated

    def _migrate_protractor(self, filename: str, content: str) -> MigratedFile:
        """Migrate Protractor to Playwright Python (similar to Cypress)."""
        # Protractor uses similar patterns to Cypress, reuse with extra transforms
        content = content.replace("browser.get(", "cy.visit(")
        content = content.replace("element(by.css(", "cy.get(")
        content = re.sub(r"element\(by\.id\(['\"]([^'\"]+)['\"]\)\)", r"cy.get('#\1')", content)
        return self._migrate_cypress(filename, content)

    def _migrate_robot(self, filename: str, content: str) -> MigratedFile:
        """Migrate Robot Framework to Playwright Python."""
        migrated = MigratedFile(
            original_path=filename,
            original_language="robot",
            output_path=filename.replace(".robot", "_playwright.py").replace(".txt", "_playwright.py"),
        )

        lines = ['"""Migrated from Robot Framework: ' + filename + '"""', ""]
        lines.append("from playwright.sync_api import Page, expect")
        lines.append("")
        lines.append("")
        lines.append("class TestMigratedRobot:")
        lines.append("")

        # Parse Robot test cases
        in_test_cases = False
        current_test = None
        test_lines = []

        for line in content.split("\n"):
            if "*** Test Cases ***" in line:
                in_test_cases = True
                continue
            elif line.startswith("***"):
                in_test_cases = False
                continue

            if in_test_cases:
                if line and not line.startswith(" ") and not line.startswith("\t"):
                    if current_test:
                        migrated.tests.append(MigratedTest(name=current_test))
                        py_name = self._to_snake_case(current_test)
                        lines.append(f"    def test_{py_name}(self, page: Page):")
                        for tl in test_lines:
                            lines.append(f"        {tl}")
                        lines.append("")
                    current_test = line.strip()
                    test_lines = []
                elif line.strip():
                    converted = self._convert_robot_keyword(line.strip())
                    if converted:
                        test_lines.append(converted)

        # Last test case
        if current_test:
            migrated.tests.append(MigratedTest(name=current_test))
            py_name = self._to_snake_case(current_test)
            lines.append(f"    def test_{py_name}(self, page: Page):")
            for tl in test_lines:
                lines.append(f"        {tl}")

        migrated.playwright_code = "\n".join(lines)
        return migrated

    def _migrate_generic(self, filename: str, content: str, language: str) -> MigratedFile:
        """Best-effort migration for unrecognized frameworks."""
        migrated = MigratedFile(
            original_path=filename,
            original_language=language,
            output_path=f"test_migrated_{os.path.splitext(os.path.basename(filename))[0]}.py",
        )

        lines = ['"""Migrated from: ' + filename + ' (best effort)."""', ""]
        lines.append("from playwright.sync_api import Page, expect")
        lines.append("")
        lines.append("")
        lines.append("class TestMigrated:")
        lines.append("    # TODO: Manual review needed - source framework not fully recognized")
        lines.append("")
        lines.append("    def test_migrated(self, page: Page):")
        lines.append('        """Original code requires manual conversion."""')
        lines.append("        pass")
        lines.append("")
        lines.append("# Original source code for reference:")
        for line in content.split("\n")[:50]:
            lines.append(f"# {line}")

        migrated.tests.append(MigratedTest(name="migrated"))
        migrated.playwright_code = "\n".join(lines)
        return migrated

    def _convert_java_body(self, body: str) -> list[str]:
        """Convert Java Selenium code to Playwright Python lines."""
        result = []
        for line in body.strip().split("\n"):
            line = line.strip()
            if not line or line.startswith("//"):
                if line.startswith("//"):
                    result.append(f"# {line[2:].strip()}")
                continue

            converted = False
            for pattern, template in SELENIUM_JAVA_ACTIONS.items():
                match = re.search(pattern, line)
                if match:
                    groups = match.groups()
                    pw_line = template
                    for i, g in enumerate(groups):
                        pw_line = pw_line.replace(f"{{{i}}}", g)
                    result.append(pw_line)
                    converted = True
                    break

            if not converted:
                for pattern, template in SELENIUM_JAVA_ASSERTIONS.items():
                    match = re.search(pattern, line)
                    if match:
                        groups = match.groups()
                        pw_line = template
                        for i, g in enumerate(groups):
                            pw_line = pw_line.replace(f"{{{i}}}", g)
                        result.append(pw_line)
                        converted = True
                        break

            if not converted and line.strip() not in ("}", "{", ""):
                # Add as comment for manual review
                clean = line.rstrip(";").strip()
                if clean:
                    result.append(f"# TODO: {clean}")

        return result if result else ["pass"]

    def _convert_cypress_body(self, body: str) -> list[str]:
        """Convert Cypress JS code to Playwright Python lines."""
        result = []
        for line in body.strip().split("\n"):
            line = line.strip().rstrip(";")
            if not line or line.startswith("//"):
                if line.startswith("//"):
                    result.append(f"# {line[2:].strip()}")
                continue

            converted = False
            for pattern, template in CYPRESS_ACTIONS.items():
                match = re.search(pattern, line)
                if match:
                    groups = match.groups()
                    pw_line = template
                    for i, g in enumerate(groups):
                        pw_line = pw_line.replace(f"{{{i}}}", g)
                    result.append(pw_line)
                    converted = True
                    break

            if not converted and line.strip():
                result.append(f"# TODO: {line}")

        return result if result else ["pass"]

    def _convert_robot_keyword(self, keyword_line: str) -> Optional[str]:
        """Convert a Robot Framework keyword call to Playwright Python."""
        parts = re.split(r'\s{2,}|\t', keyword_line)
        if not parts:
            return None

        keyword = parts[0].strip().lower()
        args = [p.strip() for p in parts[1:] if p.strip()]

        if keyword in ("open browser", "go to"):
            url = args[0] if args else "''"
            return f'page.goto("{url}")'
        elif keyword == "click element":
            return f'page.locator("{args[0] if args else ""}").click()'
        elif keyword == "input text":
            sel = args[0] if args else ""
            val = args[1] if len(args) > 1 else ""
            return f'page.locator("{sel}").fill("{val}")'
        elif keyword == "wait until element is visible":
            return f'page.locator("{args[0] if args else ""}").wait_for(state="visible")'
        elif keyword == "page should contain":
            return f'expect(page.locator("body")).to_contain_text("{args[0] if args else ""}")'
        elif keyword == "element should be visible":
            return f'expect(page.locator("{args[0] if args else ""}")).to_be_visible()'
        elif keyword in ("sleep", "wait"):
            return f'page.wait_for_timeout({int(args[0].rstrip("s")) * 1000 if args else 1000})'
        elif keyword == "close browser":
            return "# Browser cleanup handled by Playwright fixture"

        return f"# TODO: {keyword_line}"

    def _generate_project_structure(self, result: MigrationResult) -> dict:
        """Generate the project structure for the migrated framework."""
        structure = {
            "conftest.py": self._generate_conftest(),
            "requirements.txt": "playwright>=1.44.0\npytest>=8.0.0\npytest-playwright>=0.5.0\n",
            "pytest.ini": "[pytest]\ntestpaths = tests\npython_files = test_*.py\n",
        }

        for mf in result.migrated_files:
            structure[f"tests/{mf.output_path}"] = mf.playwright_code

        return structure

    def _generate_conftest(self) -> str:
        """Generate conftest.py for the migrated framework."""
        return '''"""Pytest configuration for migrated Playwright tests."""

import pytest
from playwright.sync_api import Page


@pytest.fixture(scope="session")
def browser_context_args(browser_context_args):
    """Configure browser context for all tests."""
    return {
        **browser_context_args,
        "viewport": {"width": 1280, "height": 720},
        "ignore_https_errors": True,
    }


@pytest.fixture
def page(page: Page):
    """Page fixture with default timeout."""
    page.set_default_timeout(30000)
    yield page
'''

    def _java_to_python_path(self, java_path: str) -> str:
        """Convert Java file path to Python test file path."""
        name = os.path.splitext(os.path.basename(java_path))[0]
        py_name = self._to_snake_case(name)
        if not py_name.startswith("test_"):
            py_name = f"test_{py_name}"
        return f"{py_name}.py"

    def _cypress_to_python_path(self, cy_path: str) -> str:
        """Convert Cypress file path to Python test file path."""
        name = os.path.splitext(os.path.basename(cy_path))[0]
        name = name.replace(".cy", "").replace(".spec", "")
        py_name = self._to_snake_case(name)
        if not py_name.startswith("test_"):
            py_name = f"test_{py_name}"
        return f"{py_name}.py"

    def _to_snake_case(self, name: str) -> str:
        """Convert camelCase/PascalCase to snake_case."""
        name = re.sub(r'[^a-zA-Z0-9]', '_', name)
        name = re.sub(r'([A-Z]+)([A-Z][a-z])', r'\1_\2', name)
        name = re.sub(r'([a-z])([A-Z])', r'\1_\2', name)
        return name.lower().strip('_')


def extract_files_from_zip(zip_bytes: bytes) -> dict[str, str]:
    """Extract text files from a zip archive.

    Returns dict of {filename: content}
    """
    files = {}
    try:
        with zipfile.ZipFile(BytesIO(zip_bytes), 'r') as zf:
            for name in zf.namelist():
                if name.endswith('/'):
                    continue
                ext = os.path.splitext(name)[1].lower()
                if ext in ('.java', '.py', '.js', '.ts', '.robot', '.txt', '.json', '.xml'):
                    try:
                        content = zf.read(name).decode('utf-8', errors='replace')
                        files[name] = content
                    except Exception:
                        continue
    except Exception as e:
        logger.error(f"Failed to extract zip: {e}")
    return files


# Singleton instance
framework_migrator = FrameworkMigrator()
