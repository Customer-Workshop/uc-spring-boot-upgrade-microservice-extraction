"""Executes single API endpoint via requests; handles timeouts, SSL skip."""

import json

import requests


def execute_endpoint(ep: dict) -> dict:
    """Execute a single API endpoint request.

    Args:
        ep: dict with keys: method, path, base_url, request_headers, request_params, request_body, expected_status

    Returns:
        dict with keys: actual_status, response_body
    """
    method = ep.get("method", "GET").upper()
    path = ep.get("path", "/")
    base_url = ep.get("base_url", "").rstrip("/")
    url = f"{base_url}{path}" if base_url else path

    # Parse headers
    headers = _parse_json_field(ep.get("request_headers", "{}"))
    if "Content-Type" not in headers:
        headers["Content-Type"] = "application/json"

    # Remove performance threshold header from actual request
    headers.pop("X-Perf-Threshold-Ms", None)

    # Parse params
    params = _parse_json_field(ep.get("request_params", "{}"))

    # Parse body
    body = _parse_json_field(ep.get("request_body", "{}"))

    try:
        response = requests.request(
            method=method,
            url=url,
            headers=headers,
            params=params if params else None,
            json=body if method in ("POST", "PUT", "PATCH") and body else None,
            timeout=30,
            verify=False,
        )

        # Format response body
        try:
            resp_json = response.json()
            response_body = json.dumps(resp_json, indent=2)
        except (json.JSONDecodeError, ValueError):
            response_body = response.text

        # Cap response body at 5000 chars
        if len(response_body) > 5000:
            response_body = response_body[:5000] + "\n... (truncated)"

        return {
            "actual_status": response.status_code,
            "response_body": response_body,
        }

    except requests.Timeout:
        return {
            "actual_status": 0,
            "response_body": "ERROR: Request timed out after 30 seconds",
        }
    except requests.ConnectionError as e:
        return {
            "actual_status": 0,
            "response_body": f"ERROR: Connection failed - {str(e)[:200]}",
        }
    except Exception as e:
        return {
            "actual_status": 0,
            "response_body": f"ERROR: {str(e)[:200]}",
        }


def _parse_json_field(value) -> dict:
    """Parse a JSON string or return dict as-is."""
    if isinstance(value, dict):
        return value
    if isinstance(value, str):
        try:
            parsed = json.loads(value)
            return parsed if isinstance(parsed, dict) else {}
        except (json.JSONDecodeError, TypeError):
            return {}
    return {}
