"""Tests for agent/swagger_parser.py"""

import json
import os
import sys
import tempfile

import pytest
import yaml

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from agent.swagger_parser import (
    _build_default_payload,
    _extract_endpoints_openapi3,
    _extract_endpoints_swagger2,
    _make_wrong_types,
    generate_negative_variants,
    generate_performance_variants,
    parse_swagger,
)


class TestParseSwaggerV2:
    def test_extract_endpoints_count(self, sample_swagger_v2):
        endpoints = _extract_endpoints_swagger2(sample_swagger_v2)
        assert len(endpoints) == 3  # GET /pets, POST /pets, GET /pets/{petId}

    def test_extract_endpoints_methods(self, sample_swagger_v2):
        endpoints = _extract_endpoints_swagger2(sample_swagger_v2)
        methods = [ep["method"] for ep in endpoints]
        assert "GET" in methods
        assert "POST" in methods

    def test_extract_endpoints_paths(self, sample_swagger_v2):
        endpoints = _extract_endpoints_swagger2(sample_swagger_v2)
        paths = [ep["path"] for ep in endpoints]
        assert "/pets" in paths
        assert "/pets/{petId}" in paths

    def test_extract_post_has_body(self, sample_swagger_v2):
        endpoints = _extract_endpoints_swagger2(sample_swagger_v2)
        post_ep = [ep for ep in endpoints if ep["method"] == "POST"][0]
        body = json.loads(post_ep["request_body"])
        assert "name" in body
        assert "id" in body

    def test_expected_status_codes(self, sample_swagger_v2):
        endpoints = _extract_endpoints_swagger2(sample_swagger_v2)
        get_pets = [ep for ep in endpoints if ep["method"] == "GET" and ep["path"] == "/pets"][0]
        assert get_pets["expected_status"] == 200

        post_pets = [ep for ep in endpoints if ep["method"] == "POST"][0]
        assert post_pets["expected_status"] == 201

    def test_query_params(self, sample_swagger_v2):
        endpoints = _extract_endpoints_swagger2(sample_swagger_v2)
        get_pets = [ep for ep in endpoints if ep["method"] == "GET" and ep["path"] == "/pets"][0]
        params = json.loads(get_pets["request_params"])
        assert "limit" in params


class TestParseSwaggerV3:
    def test_extract_endpoints_count(self, sample_swagger_v3):
        endpoints = _extract_endpoints_openapi3(sample_swagger_v3)
        assert len(endpoints) == 4  # GET /pets, POST /pets, GET /pets/{petId}, DELETE /pets/{petId}

    def test_extract_endpoints_methods(self, sample_swagger_v3):
        endpoints = _extract_endpoints_openapi3(sample_swagger_v3)
        methods = [ep["method"] for ep in endpoints]
        assert "GET" in methods
        assert "POST" in methods
        assert "DELETE" in methods

    def test_request_body_example(self, sample_swagger_v3):
        endpoints = _extract_endpoints_openapi3(sample_swagger_v3)
        post_ep = [ep for ep in endpoints if ep["method"] == "POST"][0]
        body = json.loads(post_ep["request_body"])
        assert body["name"] == "Fido"
        assert body["id"] == 1

    def test_expected_status(self, sample_swagger_v3):
        endpoints = _extract_endpoints_openapi3(sample_swagger_v3)
        delete_ep = [ep for ep in endpoints if ep["method"] == "DELETE"][0]
        assert delete_ep["expected_status"] == 204


class TestParseSwaggerFromFile:
    def test_parse_yaml_file(self, sample_swagger_v3):
        with tempfile.NamedTemporaryFile(mode="w", suffix=".yaml", delete=False) as f:
            yaml.dump(sample_swagger_v3, f)
            f.flush()
            endpoints = parse_swagger(f.name)
        os.unlink(f.name)
        assert len(endpoints) == 4

    def test_parse_json_file(self, sample_swagger_v2):
        with tempfile.NamedTemporaryFile(mode="w", suffix=".json", delete=False) as f:
            json.dump(sample_swagger_v2, f)
            f.flush()
            endpoints = parse_swagger(f.name)
        os.unlink(f.name)
        assert len(endpoints) == 3

    def test_parse_invalid_file(self):
        endpoints = parse_swagger("/nonexistent/file.yaml")
        assert endpoints == []


class TestNegativeVariants:
    def test_neg_401_generated(self, sample_swagger_v3):
        endpoints = _extract_endpoints_openapi3(sample_swagger_v3)
        variants = generate_negative_variants(endpoints)
        neg_401 = [v for v in variants if "[NEG-401]" in v["summary"]]
        assert len(neg_401) == len(endpoints)
        for v in neg_401:
            assert v["expected_status"] == 401
            assert json.loads(v["request_headers"]) == {}

    def test_neg_400_only_for_post_put_patch(self, sample_swagger_v3):
        endpoints = _extract_endpoints_openapi3(sample_swagger_v3)
        variants = generate_negative_variants(endpoints)
        neg_400 = [v for v in variants if "[NEG-400]" in v["summary"]]
        # Only POST endpoints should have NEG-400
        assert len(neg_400) == 1  # Only POST /pets
        assert neg_400[0]["expected_status"] == 400
        assert neg_400[0]["request_body"] == "{}"

    def test_neg_422_wrong_types(self, sample_swagger_v3):
        endpoints = _extract_endpoints_openapi3(sample_swagger_v3)
        variants = generate_negative_variants(endpoints)
        neg_422 = [v for v in variants if "[NEG-422]" in v["summary"]]
        assert len(neg_422) == 1  # Only POST
        body = json.loads(neg_422[0]["request_body"])
        # String fields should become numbers
        assert isinstance(body.get("name"), int)
        # Integer fields should become strings
        assert isinstance(body.get("id"), str)

    def test_neg_404_path_params(self, sample_swagger_v3):
        endpoints = _extract_endpoints_openapi3(sample_swagger_v3)
        variants = generate_negative_variants(endpoints)
        neg_404 = [v for v in variants if "[NEG-404]" in v["summary"]]
        assert len(neg_404) >= 2  # GET /pets/{petId} and DELETE /pets/{petId}
        for v in neg_404:
            assert "99999999" in v["path"]
            assert v["expected_status"] == 404


class TestPerformanceVariants:
    def test_perf_count_matches_positive(self, sample_swagger_v3):
        endpoints = _extract_endpoints_openapi3(sample_swagger_v3)
        variants = generate_performance_variants(endpoints)
        assert len(variants) == len(endpoints)

    def test_perf_prefix(self, sample_swagger_v3):
        endpoints = _extract_endpoints_openapi3(sample_swagger_v3)
        variants = generate_performance_variants(endpoints)
        for v in variants:
            assert v["summary"].startswith("[PERF]")

    def test_perf_threshold_header(self, sample_swagger_v3):
        endpoints = _extract_endpoints_openapi3(sample_swagger_v3)
        variants = generate_performance_variants(endpoints, threshold_ms=3000)
        for v in variants:
            headers = json.loads(v["request_headers"])
            assert "X-Perf-Threshold-Ms" in headers
            assert headers["X-Perf-Threshold-Ms"] == "3000"

    def test_perf_default_threshold(self, sample_swagger_v3):
        endpoints = _extract_endpoints_openapi3(sample_swagger_v3)
        variants = generate_performance_variants(endpoints)
        headers = json.loads(variants[0]["request_headers"])
        assert headers["X-Perf-Threshold-Ms"] == "2000"


class TestBuildDefaultPayload:
    def test_simple_object(self):
        schema = {
            "type": "object",
            "properties": {
                "name": {"type": "string"},
                "age": {"type": "integer"},
                "active": {"type": "boolean"},
            },
        }
        result = _build_default_payload(schema, {})
        assert result == {"name": "string", "age": 0, "active": True}

    def test_with_examples(self):
        schema = {
            "type": "object",
            "properties": {
                "name": {"type": "string", "example": "Fido"},
                "age": {"type": "integer", "example": 5},
            },
        }
        result = _build_default_payload(schema, {})
        assert result == {"name": "Fido", "age": 5}

    def test_array_type(self):
        schema = {"type": "array", "items": {"type": "string"}}
        result = _build_default_payload(schema, {})
        assert result == ["string"]

    def test_ref_resolution(self):
        spec = {
            "definitions": {
                "Pet": {
                    "type": "object",
                    "properties": {"name": {"type": "string", "example": "Rex"}},
                }
            }
        }
        schema = {"$ref": "#/definitions/Pet"}
        result = _build_default_payload(schema, spec)
        assert result == {"name": "Rex"}


class TestMakeWrongTypes:
    def test_string_to_number(self):
        result = _make_wrong_types('{"name": "hello"}')
        assert result["name"] == 99999

    def test_number_to_string(self):
        result = _make_wrong_types('{"age": 25}')
        assert result["age"] == "not_a_number"

    def test_bool_to_string(self):
        result = _make_wrong_types('{"active": true}')
        assert result["active"] == "not_a_boolean"

    def test_invalid_json(self):
        result = _make_wrong_types("not json")
        assert result == {"invalid": True}
