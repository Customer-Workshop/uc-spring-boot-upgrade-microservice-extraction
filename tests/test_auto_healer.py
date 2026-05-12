"""Tests for agent/auto_healer.py"""

import os
import sys

import pytest

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from agent.auto_healer import (
    AutoHealer,
    ElementFingerprint,
    HealingResult,
    recommend_selector,
    score_selector_stability,
)


class TestSelectorStabilityScore:
    def test_data_testid_highest(self):
        assert score_selector_stability('[data-testid="submit-btn"]') == 0.95

    def test_aria_label_high(self):
        assert score_selector_stability('[aria-label="Submit form"]') == 0.90

    def test_role_based(self):
        assert score_selector_stability('role=button') == 0.85

    def test_id_selector(self):
        assert score_selector_stability('#submit-button') == 0.80

    def test_name_attr(self):
        assert score_selector_stability('[name="email"]') == 0.75

    def test_class_selector(self):
        assert score_selector_stability('.btn-primary') == 0.50

    def test_complex_css(self):
        assert score_selector_stability('div > form > button.submit') == 0.35

    def test_xpath(self):
        assert score_selector_stability('//div[@class="form"]/button') == 0.30

    def test_text_based(self):
        assert score_selector_stability('text="Submit"') == 0.25

    def test_empty_selector(self):
        assert score_selector_stability('') == 0.0


class TestRecommendSelector:
    def test_recommends_testid_first(self):
        fp = ElementFingerprint(
            data_testid="login-btn",
            aria_label="Login",
            id_attr="btn-login",
            name_attr="login",
        )
        result = recommend_selector(fp)
        assert result["recommended"]["strategy"] == "data-testid"
        assert result["recommended"]["selector"] == '[data-testid="login-btn"]'

    def test_fallback_to_aria(self):
        fp = ElementFingerprint(
            aria_label="Submit Form",
            id_attr="submit",
        )
        result = recommend_selector(fp)
        assert result["recommended"]["strategy"] == "aria-label"

    def test_fallback_to_id(self):
        fp = ElementFingerprint(id_attr="main-form")
        result = recommend_selector(fp)
        assert result["recommended"]["strategy"] == "id"

    def test_alternatives_ranked(self):
        fp = ElementFingerprint(
            data_testid="x",
            aria_label="y",
            id_attr="z",
            name_attr="w",
        )
        result = recommend_selector(fp)
        # Should have multiple alternatives sorted by stability
        stabilities = [a["stability"] for a in result["alternatives"]]
        assert stabilities == sorted(stabilities, reverse=True)

    def test_empty_fingerprint(self):
        fp = ElementFingerprint()
        result = recommend_selector(fp)
        assert result["recommended"] is None
        assert result["alternatives"] == []


class TestAutoHealer:
    def test_init(self):
        healer = AutoHealer()
        assert healer.healing_log == []

    def test_healing_report_empty(self):
        healer = AutoHealer()
        assert healer.get_healing_report() == []

    def test_clear_log(self):
        healer = AutoHealer()
        healer.healing_log.append({"original": "x", "healed": "y"})
        healer.clear_log()
        assert healer.healing_log == []

    def test_extract_fingerprint_from_id_selector(self):
        healer = AutoHealer()
        fp = healer._extract_fingerprint_from_selector("#login-button")
        assert fp.id_attr == "login-button"

    def test_extract_fingerprint_from_class(self):
        healer = AutoHealer()
        fp = healer._extract_fingerprint_from_selector(".btn-primary.large")
        assert fp.class_names == ["btn-primary", "large"]

    def test_extract_fingerprint_from_testid(self):
        healer = AutoHealer()
        fp = healer._extract_fingerprint_from_selector('[data-testid="submit"]')
        assert fp.data_testid == "submit"

    def test_extract_fingerprint_from_name(self):
        healer = AutoHealer()
        fp = healer._extract_fingerprint_from_selector('[name="email"]')
        assert fp.name_attr == "email"

    def test_extract_fingerprint_from_aria(self):
        healer = AutoHealer()
        fp = healer._extract_fingerprint_from_selector('[aria-label="Search"]')
        assert fp.aria_label == "Search"

    def test_generate_alternative_data_testid(self):
        healer = AutoHealer()
        fp = ElementFingerprint(id_attr="my-btn")
        alt = healer._generate_alternative("data_testid", "#my-btn", fp)
        assert alt == '[data-testid="my-btn"]'

    def test_generate_alternative_name(self):
        healer = AutoHealer()
        fp = ElementFingerprint(name_attr="username")
        alt = healer._generate_alternative("name_attr", "#old-selector", fp)
        assert alt == '[name="username"]'

    def test_generate_alternative_returns_none(self):
        healer = AutoHealer()
        fp = ElementFingerprint()
        alt = healer._generate_alternative("data_testid", "#x", fp)
        assert alt is None


class TestHealingResult:
    def test_to_dict(self):
        result = HealingResult(
            success=True,
            original_selector="#old",
            healed_selector='[data-testid="new"]',
            strategy_used="data_testid",
            confidence=0.95,
        )
        d = result.to_dict()
        assert d["success"] is True
        assert d["original_selector"] == "#old"
        assert d["healed_selector"] == '[data-testid="new"]'
        assert d["confidence"] == 0.95

    def test_failed_result(self):
        result = HealingResult(success=False, original_selector="#broken")
        d = result.to_dict()
        assert d["success"] is False
        assert d["healed_selector"] == ""
