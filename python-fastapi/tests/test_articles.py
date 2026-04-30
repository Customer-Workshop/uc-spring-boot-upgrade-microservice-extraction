"""
Tests verifying the Python FastAPI Articles API returns identical JSON shapes
and behaviour to the Java Spring Boot version.
"""

from tests.conftest import auth_header

# ---------------------------------------------------------------------------
# GET /api/articles  (list)
# ---------------------------------------------------------------------------


class TestListArticles:
    def test_list_articles_returns_correct_shape(self, client, seed_articles, seed_favorites):
        resp = client.get("/api/articles")
        assert resp.status_code == 200
        body = resp.json()
        assert "articles" in body
        assert "articlesCount" in body
        assert isinstance(body["articles"], list)
        assert body["articlesCount"] == 3

    def test_article_object_has_all_fields(self, client, seed_articles, seed_favorites):
        resp = client.get("/api/articles")
        article = resp.json()["articles"][0]
        expected_keys = {
            "id",
            "slug",
            "title",
            "description",
            "body",
            "favorited",
            "favoritesCount",
            "createdAt",
            "updatedAt",
            "tagList",
            "author",
        }
        assert expected_keys.issubset(article.keys())

    def test_author_profile_shape(self, client, seed_articles, seed_favorites):
        resp = client.get("/api/articles")
        author = resp.json()["articles"][0]["author"]
        assert "username" in author
        assert "bio" in author
        assert "image" in author
        assert "following" in author

    def test_filter_by_tag(self, client, seed_articles):
        resp = client.get("/api/articles", params={"tag": "java"})
        assert resp.status_code == 200
        body = resp.json()
        assert body["articlesCount"] == 1
        assert body["articles"][0]["slug"] == "getting-started-with-spring-boot"

    def test_filter_by_author(self, client, seed_articles):
        resp = client.get("/api/articles", params={"author": "johndoe"})
        assert resp.status_code == 200
        body = resp.json()
        assert body["articlesCount"] == 2

    def test_filter_by_favorited(self, client, seed_articles, seed_favorites):
        resp = client.get("/api/articles", params={"favorited": "johndoe"})
        assert resp.status_code == 200
        body = resp.json()
        assert body["articlesCount"] == 1
        assert body["articles"][0]["slug"] == "rest-api-best-practices"

    def test_pagination_offset_limit(self, client, seed_articles):
        resp = client.get("/api/articles", params={"limit": 1, "offset": 0})
        assert resp.status_code == 200
        assert len(resp.json()["articles"]) == 1

    def test_authenticated_user_sees_favorited_flag(self, client, seed_articles, seed_favorites):
        headers = auth_header("user-2")
        resp = client.get("/api/articles", headers=headers)
        assert resp.status_code == 200
        articles = resp.json()["articles"]
        slugs_favorited = {a["slug"]: a["favorited"] for a in articles}
        assert slugs_favorited["getting-started-with-spring-boot"] is True

    def test_authenticated_user_sees_following_flag(self, client, seed_articles, seed_follows):
        headers = auth_header("user-1")
        resp = client.get("/api/articles", headers=headers)
        articles = resp.json()["articles"]
        for a in articles:
            if a["author"]["username"] == "janedoe":
                assert a["author"]["following"] is True


# ---------------------------------------------------------------------------
# GET /api/articles/:slug  (single)
# ---------------------------------------------------------------------------


class TestGetArticle:
    def test_get_existing_article(self, client, seed_articles, seed_favorites):
        resp = client.get("/api/articles/getting-started-with-spring-boot")
        assert resp.status_code == 200
        body = resp.json()
        assert "article" in body
        article = body["article"]
        assert article["slug"] == "getting-started-with-spring-boot"
        assert article["title"] == "Getting Started with Spring Boot"
        assert article["favoritesCount"] == 2

    def test_get_nonexistent_article_returns_404(self, client):
        resp = client.get("/api/articles/no-such-slug")
        assert resp.status_code == 404

    def test_single_article_tag_list_sorted(self, client, seed_articles):
        resp = client.get("/api/articles/getting-started-with-spring-boot")
        tags = resp.json()["article"]["tagList"]
        assert tags == sorted(tags)


# ---------------------------------------------------------------------------
# POST /api/articles  (create)
# ---------------------------------------------------------------------------


class TestCreateArticle:
    def test_create_article_requires_auth(self, client):
        resp = client.post(
            "/api/articles",
            json={"article": {"title": "T", "description": "D", "body": "B"}},
        )
        assert resp.status_code == 401

    def test_create_article_success(self, client, seed_users):
        headers = auth_header("user-1")
        payload = {
            "article": {
                "title": "How to train your dragon",
                "description": "Ever wonder how?",
                "body": "You have to believe",
                "tagList": ["dragons", "training"],
            }
        }
        resp = client.post("/api/articles", json=payload, headers=headers)
        assert resp.status_code == 200
        body = resp.json()
        assert "article" in body
        article = body["article"]
        assert article["title"] == "How to train your dragon"
        assert article["slug"] == "how-to-train-your-dragon"
        assert article["description"] == "Ever wonder how?"
        assert article["body"] == "You have to believe"
        assert set(article["tagList"]) == {"dragons", "training"}
        assert article["author"]["username"] == "johndoe"
        assert article["favorited"] is False
        assert article["favoritesCount"] == 0

    def test_create_duplicate_slug_returns_422(self, client, seed_users, seed_articles):
        headers = auth_header("user-1")
        payload = {
            "article": {
                "title": "Getting Started with Spring Boot",
                "description": "dup",
                "body": "dup",
            }
        }
        resp = client.post("/api/articles", json=payload, headers=headers)
        assert resp.status_code == 422


# ---------------------------------------------------------------------------
# PUT /api/articles/:slug  (update)
# ---------------------------------------------------------------------------


class TestUpdateArticle:
    def test_update_article_requires_auth(self, client, seed_articles):
        resp = client.put(
            "/api/articles/getting-started-with-spring-boot",
            json={"article": {"title": "Updated"}},
        )
        assert resp.status_code == 401

    def test_update_article_success(self, client, seed_articles, seed_users):
        headers = auth_header("user-1")
        resp = client.put(
            "/api/articles/getting-started-with-spring-boot",
            json={"article": {"title": "Updated Title"}},
            headers=headers,
        )
        assert resp.status_code == 200
        article = resp.json()["article"]
        assert article["title"] == "Updated Title"
        assert article["slug"] == "updated-title"

    def test_update_article_forbidden_for_non_author(self, client, seed_articles, seed_users):
        headers = auth_header("user-2")
        resp = client.put(
            "/api/articles/getting-started-with-spring-boot",
            json={"article": {"title": "Hacked"}},
            headers=headers,
        )
        assert resp.status_code == 403

    def test_update_nonexistent_article_returns_404(self, client, seed_users):
        headers = auth_header("user-1")
        resp = client.put(
            "/api/articles/nonexistent",
            json={"article": {"title": "X"}},
            headers=headers,
        )
        assert resp.status_code == 404


# ---------------------------------------------------------------------------
# DELETE /api/articles/:slug
# ---------------------------------------------------------------------------


class TestDeleteArticle:
    def test_delete_requires_auth(self, client, seed_articles):
        resp = client.delete("/api/articles/getting-started-with-spring-boot")
        assert resp.status_code == 401

    def test_delete_article_success(self, client, seed_articles, seed_users):
        headers = auth_header("user-1")
        resp = client.delete(
            "/api/articles/getting-started-with-spring-boot",
            headers=headers,
        )
        assert resp.status_code == 204
        resp2 = client.get("/api/articles/getting-started-with-spring-boot")
        assert resp2.status_code == 404

    def test_delete_forbidden_for_non_author(self, client, seed_articles, seed_users):
        headers = auth_header("user-2")
        resp = client.delete(
            "/api/articles/getting-started-with-spring-boot",
            headers=headers,
        )
        assert resp.status_code == 403

    def test_delete_nonexistent_returns_404(self, client, seed_users):
        headers = auth_header("user-1")
        resp = client.delete("/api/articles/nonexistent", headers=headers)
        assert resp.status_code == 404


# ---------------------------------------------------------------------------
# GET /api/articles/feed
# ---------------------------------------------------------------------------


class TestFeed:
    def test_feed_requires_auth(self, client):
        resp = client.get("/api/articles/feed")
        assert resp.status_code == 401

    def test_feed_returns_articles_from_followed_users(
        self, client, seed_articles, seed_follows, seed_users
    ):
        headers = auth_header("user-1")
        resp = client.get("/api/articles/feed", headers=headers)
        assert resp.status_code == 200
        body = resp.json()
        assert "articles" in body
        assert "articlesCount" in body
        for article in body["articles"]:
            assert article["author"]["username"] == "janedoe"

    def test_feed_empty_when_no_follows(self, client, seed_articles, seed_users):
        headers = auth_header("user-1")
        resp = client.get("/api/articles/feed", headers=headers)
        assert resp.status_code == 200
        body = resp.json()
        assert body["articles"] == []
        assert body["articlesCount"] == 0

    def test_feed_response_shape_matches_list(
        self, client, seed_articles, seed_follows, seed_users
    ):
        headers = auth_header("user-1")
        resp = client.get("/api/articles/feed", headers=headers)
        body = resp.json()
        if body["articles"]:
            article = body["articles"][0]
            expected_keys = {
                "id",
                "slug",
                "title",
                "description",
                "body",
                "favorited",
                "favoritesCount",
                "createdAt",
                "updatedAt",
                "tagList",
                "author",
            }
            assert expected_keys.issubset(article.keys())


# ---------------------------------------------------------------------------
# JSON shape parity with Java API
# ---------------------------------------------------------------------------


class TestJsonShapeParity:
    """Verify the Python API JSON responses have identical field names
    and nesting to the Java Spring Boot version."""

    def test_single_article_wrapper_key(self, client, seed_articles):
        resp = client.get("/api/articles/getting-started-with-spring-boot")
        assert list(resp.json().keys()) == ["article"]

    def test_list_wrapper_keys(self, client, seed_articles):
        resp = client.get("/api/articles")
        keys = set(resp.json().keys())
        assert keys == {"articles", "articlesCount"}

    def test_article_field_names_match_java(self, client, seed_articles, seed_favorites):
        resp = client.get("/api/articles/getting-started-with-spring-boot")
        article = resp.json()["article"]
        assert "favoritesCount" in article
        assert "tagList" in article
        assert "createdAt" in article
        assert "updatedAt" in article
        assert "favorites_count" not in article
        assert "tag_list" not in article

    def test_author_field_name_is_author_not_profile(self, client, seed_articles):
        resp = client.get("/api/articles/getting-started-with-spring-boot")
        article = resp.json()["article"]
        assert "author" in article
        assert "profileData" not in article

    def test_create_returns_article_wrapper(self, client, seed_users):
        headers = auth_header("user-1")
        payload = {
            "article": {
                "title": "Test Shape",
                "description": "desc",
                "body": "body",
            }
        }
        resp = client.post("/api/articles", json=payload, headers=headers)
        assert list(resp.json().keys()) == ["article"]

    def test_update_returns_article_wrapper(self, client, seed_articles, seed_users):
        headers = auth_header("user-1")
        resp = client.put(
            "/api/articles/getting-started-with-spring-boot",
            json={"article": {"body": "new body"}},
            headers=headers,
        )
        assert list(resp.json().keys()) == ["article"]
