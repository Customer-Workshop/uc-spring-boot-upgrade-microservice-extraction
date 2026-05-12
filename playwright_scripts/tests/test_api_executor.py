"""Tests for api_integration/executor.py"""

import json
import os
import sys
from unittest.mock import MagicMock, patch

import pytest

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from api_integration.executor import _parse_json_field, execute_endpoint


class TestExecuteEndpoint:
    @patch("api_integration.executor.requests.request")
    def test_successful_get(self, mock_request):
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"pets": []}
        mock_response.text = '{"pets": []}'
        mock_request.return_value = mock_response

        ep = {
            "method": "GET",
            "path": "/pets",
            "base_url": "http://localhost:8080",
            "request_headers": "{}",
            "request_params": "{}",
            "request_body": "{}",
            "expected_status": 200,
        }
        result = execute_endpoint(ep)
        assert result["actual_status"] == 200
        assert "pets" in result["response_body"]

    @patch("api_integration.executor.requests.request")
    def test_post_with_body(self, mock_request):
        mock_response = MagicMock()
        mock_response.status_code = 201
        mock_response.json.return_value = {"id": 1, "name": "Fido"}
        mock_response.text = '{"id": 1, "name": "Fido"}'
        mock_request.return_value = mock_response

        ep = {
            "method": "POST",
            "path": "/pets",
            "base_url": "http://localhost:8080",
            "request_headers": '{"Authorization": "Bearer token"}',
            "request_params": "{}",
            "request_body": '{"name": "Fido", "tag": "dog"}',
            "expected_status": 201,
        }
        result = execute_endpoint(ep)
        assert result["actual_status"] == 201

        # Verify the request was called with correct params
        call_kwargs = mock_request.call_args[1]
        assert call_kwargs["method"] == "POST"
        assert call_kwargs["url"] == "http://localhost:8080/pets"
        assert call_kwargs["json"] == {"name": "Fido", "tag": "dog"}

    @patch("api_integration.executor.requests.request")
    def test_timeout_handling(self, mock_request):
        import requests
        mock_request.side_effect = requests.Timeout("Connection timed out")

        ep = {
            "method": "GET",
            "path": "/slow",
            "base_url": "http://localhost:8080",
            "request_headers": "{}",
            "request_params": "{}",
            "request_body": "{}",
            "expected_status": 200,
        }
        result = execute_endpoint(ep)
        assert result["actual_status"] == 0
        assert "timed out" in result["response_body"]

    @patch("api_integration.executor.requests.request")
    def test_connection_error(self, mock_request):
        import requests
        mock_request.side_effect = requests.ConnectionError("Failed to connect")

        ep = {
            "method": "GET",
            "path": "/unreachable",
            "base_url": "http://nonexistent.local",
            "request_headers": "{}",
            "request_params": "{}",
            "request_body": "{}",
            "expected_status": 200,
        }
        result = execute_endpoint(ep)
        assert result["actual_status"] == 0
        assert "Connection failed" in result["response_body"]

    @patch("api_integration.executor.requests.request")
    def test_ssl_skip(self, mock_request):
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {}
        mock_response.text = "{}"
        mock_request.return_value = mock_response

        ep = {
            "method": "GET",
            "path": "/secure",
            "base_url": "https://self-signed.local",
            "request_headers": "{}",
            "request_params": "{}",
            "request_body": "{}",
            "expected_status": 200,
        }
        execute_endpoint(ep)
        call_kwargs = mock_request.call_args[1]
        assert call_kwargs["verify"] is False

    @patch("api_integration.executor.requests.request")
    def test_response_body_truncation(self, mock_request):
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"data": "x" * 10000}
        mock_response.text = '{"data": "' + "x" * 10000 + '"}'
        mock_request.return_value = mock_response

        ep = {
            "method": "GET",
            "path": "/large",
            "base_url": "http://localhost",
            "request_headers": "{}",
            "request_params": "{}",
            "request_body": "{}",
            "expected_status": 200,
        }
        result = execute_endpoint(ep)
        assert len(result["response_body"]) <= 5020  # 5000 + truncation message

    @patch("api_integration.executor.requests.request")
    def test_perf_header_stripped(self, mock_request):
        """X-Perf-Threshold-Ms should be removed from actual request."""
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {}
        mock_response.text = "{}"
        mock_request.return_value = mock_response

        ep = {
            "method": "GET",
            "path": "/test",
            "base_url": "http://localhost",
            "request_headers": '{"X-Perf-Threshold-Ms": "2000", "Accept": "application/json"}',
            "request_params": "{}",
            "request_body": "{}",
            "expected_status": 200,
        }
        execute_endpoint(ep)
        call_kwargs = mock_request.call_args[1]
        assert "X-Perf-Threshold-Ms" not in call_kwargs["headers"]
        assert "Accept" in call_kwargs["headers"]

    @patch("api_integration.executor.requests.request")
    def test_non_json_response(self, mock_request):
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.side_effect = ValueError("No JSON")
        mock_response.text = "Plain text response"
        mock_request.return_value = mock_response

        ep = {
            "method": "GET",
            "path": "/text",
            "base_url": "http://localhost",
            "request_headers": "{}",
            "request_params": "{}",
            "request_body": "{}",
            "expected_status": 200,
        }
        result = execute_endpoint(ep)
        assert result["response_body"] == "Plain text response"


class TestParseJsonField:
    def test_valid_json_string(self):
        result = _parse_json_field('{"key": "value"}')
        assert result == {"key": "value"}

    def test_dict_passthrough(self):
        result = _parse_json_field({"key": "value"})
        assert result == {"key": "value"}

    def test_invalid_json(self):
        result = _parse_json_field("not json")
        assert result == {}

    def test_empty_string(self):
        result = _parse_json_field("")
        assert result == {}

    def test_none(self):
        result = _parse_json_field(None)
        assert result == {}

    def test_non_dict_json(self):
        result = _parse_json_field("[1, 2, 3]")
        assert result == {}
