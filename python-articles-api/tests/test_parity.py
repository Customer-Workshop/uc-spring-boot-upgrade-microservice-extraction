"""Parity tests: the FastAPI endpoints must return responses identical to the
golden captures taken from the running Java/Spring Boot service against the same
seed database (tests/fixtures/seed.db)."""

import json

import pytest

from conftest import GOLDEN, auth_header

with open(GOLDEN / "_index.json") as f:
    GET_CASES = json.load(f)


@pytest.mark.parametrize("name", sorted(GET_CASES.keys()))
def test_get_parity(client, token, name):
    meta = GET_CASES[name]
    with open(GOLDEN / f"{name}.json") as f:
        expected = json.load(f)

    headers = auth_header(token, meta["auth"]) if meta["auth"] else {}
    # The Java routes live under /..., the FastAPI port under /api/...
    resp = client.get("/api" + meta["path"], headers=headers)

    assert resp.status_code == expected["status"], name

    if expected["status"] == 200:
        assert resp.json() == expected["body"], name
    elif expected["status"] == 404:
        body = resp.json()
        assert body["status"] == 404
        assert body["error"] == "Not Found"


def _norm_article(article: dict) -> dict:
    """Drop values that are inherently non-deterministic between runs
    (generated id/slug/timestamps) so structural parity can be asserted."""
    a = dict(article)
    a.pop("id", None)
    a.pop("slug", None)
    a.pop("createdAt", None)
    a.pop("updatedAt", None)
    a.pop("cursor", None)
    a["tagList"] = sorted(a.get("tagList", []))
    return a


def test_create_parity(client, token):
    with open(GOLDEN / "crud_create.json") as f:
        expected = json.load(f)
    body = {
        "article": {
            "title": "How to Train Your Dragon",
            "description": "Ever wonder how?",
            "body": "You have to believe",
            "tagList": ["dragons", "training"],
        }
    }
    resp = client.post("/api/articles", json=body, headers=auth_header(token, "john"))
    assert resp.status_code == expected["status"]
    got = resp.json()["article"]
    exp = expected["body"]["article"]
    assert _norm_article(got) == _norm_article(exp)
    # slug is deterministic from the title.
    assert got["slug"] == "how-to-train-your-dragon"
    assert got["createdAt"] == got["updatedAt"]
    assert got["cursor"]["data"] == got["updatedAt"]


def test_create_requires_auth(client):
    with open(GOLDEN / "crud_create_noauth.json") as f:
        expected = json.load(f)
    resp = client.post(
        "/api/articles",
        json={"article": {"title": "x", "description": "y", "body": "z", "tagList": []}},
    )
    assert resp.status_code == expected["status"] == 401


def test_create_validation(client, token):
    with open(GOLDEN / "crud_create_invalid.json") as f:
        expected = json.load(f)
    resp = client.post(
        "/api/articles",
        json={"article": {"title": "", "description": "d", "body": "b", "tagList": []}},
        headers=auth_header(token, "john"),
    )
    assert resp.status_code == expected["status"] == 422
    assert resp.json() == expected["body"]


def test_update_parity(client, token):
    create = client.post(
        "/api/articles",
        json={
            "article": {
                "title": "How to Train Your Dragon",
                "description": "Ever wonder how?",
                "body": "You have to believe",
                "tagList": ["dragons", "training"],
            }
        },
        headers=auth_header(token, "john"),
    )
    slug = create.json()["article"]["slug"]
    resp = client.put(
        f"/api/articles/{slug}",
        json={
            "article": {
                "title": "How to Train Your Dragon EVEN MORE",
                "body": "With even more resolve",
            }
        },
        headers=auth_header(token, "john"),
    )
    assert resp.status_code == 200
    art = resp.json()["article"]
    assert art["slug"] == "how-to-train-your-dragon-even-more"
    assert art["title"] == "How to Train Your Dragon EVEN MORE"
    assert art["body"] == "With even more resolve"
    # description was omitted (empty) -> unchanged
    assert art["description"] == "Ever wonder how?"


def test_update_forbidden(client, token):
    with open(GOLDEN / "crud_update_forbidden.json") as f:
        expected = json.load(f)
    resp = client.put(
        "/api/articles/rest-api-best-practices",
        json={"article": {"title": "hijack"}},
        headers=auth_header(token, "john"),
    )
    assert resp.status_code == expected["status"] == 403
    assert resp.json()["error"] == "Forbidden"


def test_delete_parity(client, token):
    create = client.post(
        "/api/articles",
        json={
            "article": {
                "title": "Ephemeral Article",
                "description": "d",
                "body": "b",
                "tagList": [],
            }
        },
        headers=auth_header(token, "john"),
    )
    slug = create.json()["article"]["slug"]
    resp = client.delete(f"/api/articles/{slug}", headers=auth_header(token, "john"))
    assert resp.status_code == 204
    assert resp.content == b""
    # subsequent fetch 404s
    assert client.get(f"/api/articles/{slug}").status_code == 404


def test_delete_not_found(client, token):
    resp = client.delete("/api/articles/nope-nope", headers=auth_header(token, "john"))
    assert resp.status_code == 404


def test_feed_requires_auth(client):
    assert client.get("/api/articles/feed").status_code == 401
