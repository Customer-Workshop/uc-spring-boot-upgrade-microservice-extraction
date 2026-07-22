"""Parity tests: the FastAPI responses must match the golden responses captured
from the running Java/Spring Boot service (tests/golden/*.json) for the same
inputs, modulo the necessarily time-relative timestamps (see normalize())."""

from __future__ import annotations

import re

from conftest import load_golden, normalize

TS_RE = re.compile(r"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z")

# article id -> relative age in days (matches the seed data).
ARTICLE_DAYS = {
    "getting-started-with-spring-boot": 7,
    "rest-api-best-practices": 5,
    "microservices-architecture-guide": 3,
    "docker-for-java-developers": 2,
    "testing-spring-boot-applications": 1,
}


def _assert_timestamps_consistent(article: dict):
    assert TS_RE.fullmatch(article["createdAt"])
    assert TS_RE.fullmatch(article["updatedAt"])
    # Seed articles are never updated, so all three timestamps are identical.
    assert article["createdAt"] == article["updatedAt"]
    assert article["cursor"] == {"data": article["updatedAt"]}


# ---------------------------------------------------------------- GET /api/articles
def test_list_all_matches_golden(client):
    resp = client.get("/api/articles")
    assert resp.status_code == 200
    assert normalize(resp.json()) == normalize(load_golden("articles_all.json"))
    for a in resp.json()["articles"]:
        _assert_timestamps_consistent(a)


def test_list_ordered_created_desc(client):
    articles = client.get("/api/articles").json()["articles"]
    slugs = [a["slug"] for a in articles]
    assert slugs == [
        "testing-spring-boot-applications",
        "docker-for-java-developers",
        "microservices-architecture-guide",
        "rest-api-best-practices",
        "getting-started-with-spring-boot",
    ]


def test_list_limit_offset(client):
    assert normalize(client.get("/api/articles?limit=2&offset=0").json()) == normalize(
        load_golden("articles_limit2.json")
    )


def test_list_filter_tag(client):
    assert normalize(client.get("/api/articles?tag=java").json()) == normalize(
        load_golden("articles_tag_java.json")
    )


def test_list_filter_author(client):
    assert normalize(client.get("/api/articles?author=johndoe").json()) == normalize(
        load_golden("articles_author.json")
    )


def test_list_filter_favorited(client):
    assert normalize(
        client.get("/api/articles?favorited=janedoe").json()
    ) == normalize(load_golden("articles_favorited.json"))


def test_list_empty(client):
    assert client.get("/api/articles?tag=nonexistent").json() == load_golden(
        "articles_empty.json"
    )


def test_list_favorited_following_flags_for_jane(client, jane_headers):
    resp = client.get("/api/articles", headers=jane_headers)
    flags = {
        a["slug"]: (a["favorited"], a["author"]["following"])
        for a in resp.json()["articles"]
    }
    assert flags["getting-started-with-spring-boot"] == (True, True)
    assert flags["microservices-architecture-guide"] == (True, True)
    assert flags["testing-spring-boot-applications"] == (False, False)


# --------------------------------------------------------- GET /api/articles/:slug
def test_get_by_slug_anonymous_matches_golden(client):
    resp = client.get("/api/articles/getting-started-with-spring-boot")
    assert resp.status_code == 200
    assert normalize(resp.json()) == normalize(load_golden("article_slug.json"))
    _assert_timestamps_consistent(resp.json()["article"])


def test_get_by_slug_author_matches_golden(client, john_headers):
    resp = client.get(
        "/api/articles/getting-started-with-spring-boot", headers=john_headers
    )
    assert normalize(resp.json()) == normalize(load_golden("article_slug_auth.json"))


def test_get_by_slug_favorited_following_true(client, jane_headers):
    resp = client.get(
        "/api/articles/getting-started-with-spring-boot", headers=jane_headers
    )
    assert normalize(resp.json()) == normalize(load_golden("article_jane_auth.json"))
    art = resp.json()["article"]
    assert art["favorited"] is True
    assert art["author"]["following"] is True
    assert art["favoritesCount"] == 2


def test_get_by_slug_not_found(client):
    assert client.get("/api/articles/does-not-exist").status_code == 404


# --------------------------------------------------------- GET /api/articles/feed
def test_feed_requires_auth(client):
    assert client.get("/api/articles/feed").status_code == 401


def test_feed_matches_golden(client, john_headers):
    resp = client.get("/api/articles/feed", headers=john_headers)
    assert resp.status_code == 200
    assert normalize(resp.json()) == normalize(load_golden("feed_auth.json"))


def test_feed_order_and_count(client, jane_headers):
    resp = client.get("/api/articles/feed", headers=jane_headers).json()
    assert resp["articlesCount"] == 2
    assert [a["slug"] for a in resp["articles"]] == [
        "getting-started-with-spring-boot",
        "microservices-architecture-guide",
    ]


# --------------------------------------------------------- POST /api/articles
def test_create_requires_auth(client):
    body = {"article": {"title": "x", "description": "y", "body": "z"}}
    assert client.post("/api/articles", json=body).status_code == 401


def test_create_validation_error(client, jane_headers):
    body = {"article": {"title": "", "description": "d", "body": "b", "tagList": []}}
    resp = client.post("/api/articles", json=body, headers=jane_headers)
    assert resp.status_code == 422
    assert resp.json() == load_golden("create_invalid.json")


def test_create_then_get_and_delete(client, john_headers):
    body = {
        "article": {
            "title": "My New Test Article",
            "description": "A test desc",
            "body": "Some body content",
            "tagList": ["python", "testing"],
        }
    }
    resp = client.post("/api/articles", json=body, headers=john_headers)
    assert resp.status_code == 200
    art = resp.json()["article"]
    # Same shape as the Java create response.
    assert set(art.keys()) == {
        "id", "slug", "title", "description", "body", "favorited",
        "favoritesCount", "createdAt", "updatedAt", "tagList", "cursor", "author",
    }
    assert art["slug"] == "my-new-test-article"
    assert art["tagList"] == ["python", "testing"]
    assert art["favorited"] is False
    assert art["favoritesCount"] == 0
    assert art["author"]["username"] == "johndoe"
    assert art["createdAt"] == art["updatedAt"] == art["cursor"]["data"]
    assert TS_RE.fullmatch(art["createdAt"])

    # Reachable by slug.
    assert (
        client.get("/api/articles/my-new-test-article").status_code == 200
    )
    # Delete (204) then 404.
    assert (
        client.delete("/api/articles/my-new-test-article", headers=john_headers).status_code
        == 204
    )
    assert client.get("/api/articles/my-new-test-article").status_code == 404


# --------------------------------------------------------- PUT /api/articles/:slug
def test_update_does_not_change_updated_at(client, john_headers):
    body = {
        "article": {
            "title": "Update Me Article",
            "description": "d",
            "body": "b",
            "tagList": ["java"],
        }
    }
    created = client.post("/api/articles", json=body, headers=john_headers).json()[
        "article"
    ]
    original_updated = created["updatedAt"]

    put = client.put(
        "/api/articles/update-me-article",
        json={"article": {"title": "Renamed Article", "body": "New body"}},
        headers=john_headers,
    )
    assert put.status_code == 200
    updated = put.json()["article"]
    assert updated["slug"] == "renamed-article"
    assert updated["title"] == "Renamed Article"
    assert updated["body"] == "New body"
    assert updated["description"] == "d"  # untouched (empty in request)
    # Mirrors the Java quirk: ArticleMapper.update never writes updated_at.
    assert updated["updatedAt"] == original_updated
    assert updated["createdAt"] == original_updated

    client.delete("/api/articles/renamed-article", headers=john_headers)


def test_update_not_found(client, jane_headers):
    resp = client.put(
        "/api/articles/nope", json={"article": {"title": "x"}}, headers=jane_headers
    )
    assert resp.status_code == 404


def test_update_forbidden_for_non_author(client, jane_headers):
    resp = client.put(
        "/api/articles/getting-started-with-spring-boot",
        json={"article": {"title": "Hacked"}},
        headers=jane_headers,
    )
    assert resp.status_code == 403


def test_delete_forbidden_for_non_author(client, jane_headers):
    resp = client.delete(
        "/api/articles/getting-started-with-spring-boot", headers=jane_headers
    )
    assert resp.status_code == 403
