"""Tests for agent/framework_exporter.py"""

import json
import os
import sys
import zipfile
from io import BytesIO

import pytest

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from agent.framework_exporter import ExportConfig, ExportResult, FrameworkExporter


class TestExportConfig:
    def test_defaults(self):
        config = ExportConfig()
        assert config.project_name == "playwright_tests"
        assert config.include_page_objects is True
        assert config.include_api_tests is True


class TestExportResult:
    def test_to_dict(self):
        result = ExportResult(
            success=True,
            project_name="my_tests",
            file_count=5,
            total_tests=10,
            files={"a.py": "code", "b.py": "code2"},
        )
        d = result.to_dict()
        assert d["success"] is True
        assert d["file_count"] == 5
        assert d["total_tests"] == 10
        assert "a.py" in d["files"]


class TestFrameworkExporter:
    def setup_method(self):
        self.exporter = FrameworkExporter()

    def test_export_empty_suites(self):
        result = self.exporter.export([])
        assert result.success is True
        assert result.total_tests == 0
        # Still generates boilerplate
        assert "requirements.txt" in result.files
        assert "conftest.py" in result.files

    def test_export_web_suite(self):
        suites = [{
            "name": "Login Suite",
            "flow_type": "web",
            "test_cases": [{
                "name": "Login Test",
                "flow_type": "web",
                "base_url": "https://example.com",
                "steps": [
                    {"action": "navigate", "selector": "", "value": "https://example.com/login", "description": "Go to login"},
                    {"action": "fill", "selector": "#username", "value": "admin", "description": "Enter username"},
                    {"action": "fill", "selector": "#password", "value": "secret", "description": "Enter password"},
                    {"action": "click", "selector": ".btn-login", "value": "", "description": "Click login"},
                    {"action": "assert_url", "selector": "", "value": "dashboard", "description": "Verify dashboard"},
                ],
                "data": [],
                "endpoints": [],
            }],
        }]

        result = self.exporter.export(suites)
        assert result.success is True
        assert result.total_tests == 1
        assert any("test_login_suite" in f for f in result.files)

    def test_export_generates_playwright_code(self):
        suites = [{
            "name": "My Tests",
            "flow_type": "web",
            "test_cases": [{
                "name": "Basic Click",
                "flow_type": "web",
                "base_url": "http://localhost",
                "steps": [
                    {"action": "click", "selector": "#btn", "value": "", "description": ""},
                ],
                "data": [],
                "endpoints": [],
            }],
        }]

        result = self.exporter.export(suites)
        test_file = next(v for k, v in result.files.items() if k.startswith("tests/"))
        assert 'page.locator("#btn").click()' in test_file

    def test_export_api_suite(self):
        suites = [{
            "name": "API Tests",
            "flow_type": "api",
            "test_cases": [{
                "name": "Get Users",
                "flow_type": "api",
                "base_url": "https://api.example.com",
                "steps": [],
                "data": [],
                "endpoints": [
                    {"method": "GET", "path": "/users", "summary": "List users", "expected_status": 200, "request_headers": {}, "request_body": None},
                    {"method": "POST", "path": "/users", "summary": "Create user", "expected_status": 201, "request_headers": {}, "request_body": {"name": "test"}},
                ],
            }],
        }]

        result = self.exporter.export(suites)
        assert result.total_tests == 1
        test_file = next(v for k, v in result.files.items() if k.startswith("tests/"))
        assert "requests.get" in test_file
        assert "requests.post" in test_file
        assert "assert response.status_code ==" in test_file

    def test_export_with_test_data(self):
        suites = [{
            "name": "Data Driven",
            "flow_type": "web",
            "test_cases": [{
                "name": "Login Multi",
                "flow_type": "web",
                "base_url": "http://localhost",
                "steps": [{"action": "fill", "selector": "#user", "value": "{{username}}", "description": ""}],
                "data": [
                    {"data_json": {"username": "alice", "password": "pass1"}},
                    {"data_json": {"username": "bob", "password": "pass2"}},
                ],
                "endpoints": [],
            }],
        }]

        config = ExportConfig(include_test_data=True)
        result = self.exporter.export(suites, config)
        # Should generate data file
        data_files = [k for k in result.files if k.startswith("data/") and k.endswith(".json")]
        assert len(data_files) >= 1

    def test_export_generates_zip(self):
        suites = [{
            "name": "Zip Test",
            "flow_type": "web",
            "test_cases": [{
                "name": "Test1",
                "flow_type": "web",
                "base_url": "",
                "steps": [{"action": "click", "selector": "#x", "value": "", "description": ""}],
                "data": [],
                "endpoints": [],
            }],
        }]

        result = self.exporter.export(suites)
        assert result.zip_bytes is not None

        # Verify it's a valid zip
        zf = zipfile.ZipFile(BytesIO(result.zip_bytes))
        names = zf.namelist()
        assert any("conftest.py" in n for n in names)
        assert any("requirements.txt" in n for n in names)

    def test_export_page_objects(self):
        suites = [{
            "name": "POM Test",
            "flow_type": "web",
            "test_cases": [{
                "name": "Navigate Test",
                "flow_type": "web",
                "base_url": "",
                "steps": [
                    {"action": "navigate", "selector": "", "value": "https://example.com/login", "description": ""},
                    {"action": "fill", "selector": "#username", "value": "test", "description": "Enter user"},
                    {"action": "click", "selector": ".submit-btn", "value": "", "description": "Submit"},
                ],
                "data": [],
                "endpoints": [],
            }],
        }]

        config = ExportConfig(include_page_objects=True)
        result = self.exporter.export(suites, config)
        page_files = [k for k in result.files if k.startswith("pages/") and k.endswith("_page.py")]
        assert len(page_files) >= 1

        # Check POM content
        pom_content = next(v for k, v in result.files.items() if k.endswith("_page.py"))
        assert "class " in pom_content
        assert "self.page" in pom_content

    def test_export_conftest_has_fixtures(self):
        result = self.exporter.export([])
        conftest = result.files["conftest.py"]
        assert "@pytest.fixture" in conftest
        assert "def page" in conftest
        assert "set_default_timeout" in conftest

    def test_export_readme_has_info(self):
        suites = [{
            "name": "S1",
            "flow_type": "web",
            "test_cases": [{"name": "T1", "flow_type": "web", "base_url": "", "steps": [], "data": [], "endpoints": []}],
        }]
        result = self.exporter.export(suites)
        readme = result.files["README.md"]
        assert "pytest" in readme
        assert "playwright" in readme.lower()
