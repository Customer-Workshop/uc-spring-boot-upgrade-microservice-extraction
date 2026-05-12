"""Parses Swagger 2.0 + OpenAPI 3.x; generates positive/negative/performance variants."""

import json
import os
from typing import Any

import requests
import yaml


def parse_swagger(source: str) -> list[dict]:
    """Parse a Swagger/OpenAPI spec from URL or file path.

    Returns list of endpoint dicts: {method, path, summary, request_headers, request_params, request_body, expected_status}
    """
    spec = _load_spec(source)
    if not spec:
        return []

    version = spec.get("openapi", spec.get("swagger", ""))
    if str(version).startswith("3"):
        return _extract_endpoints_openapi3(spec)
    else:
        return _extract_endpoints_swagger2(spec)


def _load_spec(source: str) -> dict:
    """Load spec from URL or file path."""
    if source.startswith("http://") or source.startswith("https://"):
        try:
            resp = requests.get(source, timeout=30, verify=False)
            resp.raise_for_status()
            content = resp.text
        except Exception:
            return {}
    elif os.path.isfile(source):
        with open(source, "r") as f:
            content = f.read()
    else:
        return {}

    try:
        return yaml.safe_load(content)
    except Exception:
        try:
            return json.loads(content)
        except Exception:
            return {}


def _extract_endpoints_openapi3(spec: dict) -> list[dict]:
    """Extract endpoints from OpenAPI 3.x spec."""
    endpoints = []
    paths = spec.get("paths", {})

    for path, path_item in paths.items():
        for method in ["get", "post", "put", "patch", "delete", "head", "options"]:
            if method not in path_item:
                continue

            operation = path_item[method]
            summary = operation.get("summary", "") or operation.get("operationId", "")

            request_body = {}
            if "requestBody" in operation:
                rb = operation["requestBody"]
                content = rb.get("content", {})
                json_schema = content.get("application/json", {}).get("schema", {})
                if json_schema:
                    request_body = _build_default_payload(json_schema, spec)

            request_params = {}
            request_headers = {}
            for param in operation.get("parameters", []):
                param_in = param.get("in", "query")
                name = param.get("name", "")
                example = param.get("example", param.get("schema", {}).get("example", ""))
                if param_in == "query":
                    request_params[name] = example or ""
                elif param_in == "header":
                    request_headers[name] = example or ""

            responses = operation.get("responses", {})
            expected_status = 200
            for code in sorted(responses.keys()):
                try:
                    status_code = int(code)
                    if 200 <= status_code < 300:
                        expected_status = status_code
                        break
                except (ValueError, TypeError):
                    continue

            endpoints.append({
                "method": method.upper(),
                "path": path,
                "summary": summary,
                "request_headers": json.dumps(request_headers),
                "request_params": json.dumps(request_params),
                "request_body": json.dumps(request_body),
                "expected_status": expected_status,
            })

    return endpoints


def _extract_endpoints_swagger2(spec: dict) -> list[dict]:
    """Extract endpoints from Swagger 2.0 spec."""
    endpoints = []
    paths = spec.get("paths", {})

    for path, path_item in paths.items():
        for method in ["get", "post", "put", "patch", "delete", "head", "options"]:
            if method not in path_item:
                continue

            operation = path_item[method]
            summary = operation.get("summary", "") or operation.get("operationId", "")

            request_body = {}
            request_params = {}
            request_headers = {}

            for param in operation.get("parameters", []):
                param_in = param.get("in", "query")
                name = param.get("name", "")

                if param_in == "body":
                    schema = param.get("schema", {})
                    request_body = _build_default_payload(schema, spec)
                elif param_in == "query":
                    request_params[name] = param.get("default", "")
                elif param_in == "header":
                    request_headers[name] = param.get("default", "")

            responses = operation.get("responses", {})
            expected_status = 200
            for code in sorted(responses.keys()):
                try:
                    status_code = int(code)
                    if 200 <= status_code < 300:
                        expected_status = status_code
                        break
                except (ValueError, TypeError):
                    continue

            endpoints.append({
                "method": method.upper(),
                "path": path,
                "summary": summary,
                "request_headers": json.dumps(request_headers),
                "request_params": json.dumps(request_params),
                "request_body": json.dumps(request_body),
                "expected_status": expected_status,
            })

    return endpoints


def _build_default_payload(schema: dict, spec: dict, depth: int = 0) -> Any:
    """Recursively build example payload from schema, resolving $ref."""
    if depth > 10:
        return {}

    if "$ref" in schema:
        schema = _resolve_ref(schema["$ref"], spec)

    schema_type = schema.get("type", "object")

    if schema_type == "object":
        result = {}
        properties = schema.get("properties", {})
        for prop_name, prop_schema in properties.items():
            if "example" in prop_schema:
                result[prop_name] = prop_schema["example"]
            else:
                result[prop_name] = _build_default_payload(prop_schema, spec, depth + 1)
        return result

    elif schema_type == "array":
        items = schema.get("items", {})
        return [_build_default_payload(items, spec, depth + 1)]

    elif schema_type == "string":
        return schema.get("example", "string")
    elif schema_type == "integer":
        return schema.get("example", 0)
    elif schema_type == "number":
        return schema.get("example", 0.0)
    elif schema_type == "boolean":
        return schema.get("example", True)
    else:
        return {}


def _resolve_ref(ref: str, spec: dict) -> dict:
    """Resolve a JSON $ref path within the spec."""
    parts = ref.lstrip("#/").split("/")
    current = spec
    for part in parts:
        if isinstance(current, dict) and part in current:
            current = current[part]
        else:
            return {}
    return current if isinstance(current, dict) else {}


def generate_negative_variants(positive_eps: list[dict]) -> list[dict]:
    """Generate negative test variants for each positive endpoint.

    Returns up to 4 variants per endpoint:
    - [NEG-401]: No Authorization header
    - [NEG-400]: Empty body (POST/PUT/PATCH only)
    - [NEG-422]: Wrong-type fields in body
    - [NEG-404]: Path params replaced with 99999999
    """
    variants = []

    for ep in positive_eps:
        method = ep.get("method", "GET").upper()
        path = ep.get("path", "/")
        summary = ep.get("summary", "")

        # NEG-401: No Authorization header
        neg_headers = {}
        variants.append({
            "method": method,
            "path": path,
            "summary": f"[NEG-401] {summary}",
            "request_headers": json.dumps(neg_headers),
            "request_params": ep.get("request_params", "{}"),
            "request_body": ep.get("request_body", "{}"),
            "expected_status": 401,
        })

        # NEG-400: Empty body (POST/PUT/PATCH only)
        if method in ("POST", "PUT", "PATCH"):
            variants.append({
                "method": method,
                "path": path,
                "summary": f"[NEG-400] {summary}",
                "request_headers": ep.get("request_headers", "{}"),
                "request_params": ep.get("request_params", "{}"),
                "request_body": "{}",
                "expected_status": 400,
            })

        # NEG-422: Wrong-type fields in body
        if method in ("POST", "PUT", "PATCH"):
            bad_body = _make_wrong_types(ep.get("request_body", "{}"))
            variants.append({
                "method": method,
                "path": path,
                "summary": f"[NEG-422] {summary}",
                "request_headers": ep.get("request_headers", "{}"),
                "request_params": ep.get("request_params", "{}"),
                "request_body": json.dumps(bad_body),
                "expected_status": 422,
            })

        # NEG-404: Path params replaced with 99999999
        import re
        if re.search(r"\{[^}]+\}", path):
            fake_path = re.sub(r"\{[^}]+\}", "99999999", path)
            variants.append({
                "method": method,
                "path": fake_path,
                "summary": f"[NEG-404] {summary}",
                "request_headers": ep.get("request_headers", "{}"),
                "request_params": ep.get("request_params", "{}"),
                "request_body": ep.get("request_body", "{}"),
                "expected_status": 404,
            })

    return variants


def _make_wrong_types(body_json: str) -> dict:
    """Replace field values with wrong types for NEG-422 variant."""
    try:
        body = json.loads(body_json) if isinstance(body_json, str) else body_json
    except (json.JSONDecodeError, TypeError):
        return {"invalid": True}

    if not isinstance(body, dict):
        return {"invalid": True}

    result = {}
    for key, value in body.items():
        if isinstance(value, bool):
            result[key] = "not_a_boolean"
        elif isinstance(value, str):
            result[key] = 99999  # string → number
        elif isinstance(value, (int, float)):
            result[key] = "not_a_number"  # number → string
        elif isinstance(value, list):
            result[key] = "not_an_array"
        elif isinstance(value, dict):
            result[key] = "not_an_object"
        else:
            result[key] = None
    return result


def generate_performance_variants(positive_eps: list[dict], threshold_ms: int = 2000) -> list[dict]:
    """Clone all endpoints with [PERF] prefix and X-Perf-Threshold-Ms header."""
    variants = []
    for ep in positive_eps:
        headers = {}
        try:
            headers = json.loads(ep.get("request_headers", "{}"))
        except (json.JSONDecodeError, TypeError):
            pass
        headers["X-Perf-Threshold-Ms"] = str(threshold_ms)

        variants.append({
            "method": ep.get("method", "GET"),
            "path": ep.get("path", "/"),
            "summary": f"[PERF] {ep.get('summary', '')}",
            "request_headers": json.dumps(headers),
            "request_params": ep.get("request_params", "{}"),
            "request_body": ep.get("request_body", "{}"),
            "expected_status": ep.get("expected_status", 200),
        })

    return variants
