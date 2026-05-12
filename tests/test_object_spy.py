"""Tests for agent/object_spy.py"""

import os
import sys

import pytest

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from agent.auto_healer import ElementFingerprint
from agent.object_spy import ObjectSpy, SpiedElement


class TestSpiedElement:
    def test_to_dict(self):
        el = SpiedElement(
            tag_name="button",
            text_content="Submit",
            is_visible=True,
            is_enabled=True,
            recommended_selector='[data-testid="submit"]',
            recommended_strategy="data-testid",
            stability_score=0.95,
        )
        d = el.to_dict()
        assert d["tag_name"] == "button"
        assert d["text_content"] == "Submit"
        assert d["is_visible"] is True
        assert d["recommended_selector"] == '[data-testid="submit"]'
        assert d["stability_score"] == 0.95

    def test_default_values(self):
        el = SpiedElement()
        d = el.to_dict()
        assert d["tag_name"] == ""
        assert d["selectors"] == []
        assert d["is_visible"] is False


class TestObjectSpy:
    def test_init(self):
        spy = ObjectSpy()
        assert spy._capture_history == []

    def test_get_capture_history_empty(self):
        spy = ObjectSpy()
        assert spy.get_capture_history() == []

    def test_clear_history(self):
        spy = ObjectSpy()
        spy._capture_history.append(SpiedElement(tag_name="div"))
        spy.clear_history()
        assert spy._capture_history == []

    def test_suggest_test_steps_button(self):
        spy = ObjectSpy()
        elements = [
            SpiedElement(
                tag_name="button",
                text_content="Login",
                recommended_selector='[data-testid="login-btn"]',
                attributes={"data-testid": "login-btn"},
            )
        ]
        steps = spy.suggest_test_steps(elements)
        assert len(steps) == 1
        assert steps[0]["action"] == "click"
        assert steps[0]["selector"] == '[data-testid="login-btn"]'
        assert "Login" in steps[0]["description"]

    def test_suggest_test_steps_input(self):
        spy = ObjectSpy()
        elements = [
            SpiedElement(
                tag_name="input",
                recommended_selector='[name="email"]',
                attributes={"type": "email", "name": "email", "placeholder": "Enter email"},
            )
        ]
        steps = spy.suggest_test_steps(elements)
        assert len(steps) == 1
        assert steps[0]["action"] == "fill"
        assert "{{" in steps[0]["value"]

    def test_suggest_test_steps_link(self):
        spy = ObjectSpy()
        elements = [
            SpiedElement(
                tag_name="a",
                text_content="Home",
                recommended_selector='text="Home"',
                attributes={"href": "/home"},
            )
        ]
        steps = spy.suggest_test_steps(elements)
        assert len(steps) == 1
        assert steps[0]["action"] == "click"

    def test_suggest_test_steps_select(self):
        spy = ObjectSpy()
        elements = [
            SpiedElement(
                tag_name="select",
                recommended_selector='[name="country"]',
                attributes={"name": "country"},
            )
        ]
        steps = spy.suggest_test_steps(elements)
        assert steps[0]["action"] == "select"

    def test_suggest_test_steps_checkbox(self):
        spy = ObjectSpy()
        elements = [
            SpiedElement(
                tag_name="input",
                recommended_selector='[name="agree"]',
                attributes={"type": "checkbox", "name": "agree"},
            )
        ]
        steps = spy.suggest_test_steps(elements)
        assert steps[0]["action"] == "click"

    def test_export_json_format(self):
        spy = ObjectSpy()
        elements = [SpiedElement(tag_name="div", text_content="test")]
        result = spy.export_locators(elements, format="json")
        import json
        parsed = json.loads(result)
        assert len(parsed) == 1
        assert parsed[0]["tag_name"] == "div"

    def test_export_pom_format(self):
        spy = ObjectSpy()
        elements = [
            SpiedElement(
                tag_name="button",
                recommended_selector='[data-testid="submit"]',
                recommended_strategy="data-testid",
                stability_score=0.95,
                attributes={"id": "submit_btn"},
            )
        ]
        result = spy.export_locators(elements, format="page_object")
        assert "class CapturedPage:" in result
        assert "self.page" in result
        assert "[data-testid=\"submit\"]" in result
        assert "Stability: 95%" in result

    def test_export_pom_no_selector_skipped(self):
        spy = ObjectSpy()
        elements = [SpiedElement(tag_name="div")]  # No recommended_selector
        result = spy.export_locators(elements, format="page_object")
        assert "@property" not in result  # Skipped because no selector
