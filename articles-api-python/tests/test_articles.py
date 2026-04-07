"""Pytest tests for the Articles API.

These tests verify that the Python FastAPI endpoints return responses
with the **same JSON shape** as the Java/Spring Boot version for
identical inputs.  We use an in-memory SQLite database so each test
run starts clean.
"""

from fastapi.testclient import TestClient

from app.main import app
from app.models import Article, User, _new_id, _to_slug, article_favorites, user_follows
from tests.conftest import TestingSessionLocal

# ---------- fixtures come from conftest.py ----------

client = TestClient(app)

# ---------- JSON-shape field sets (mirrors Java ArticleData / ArticleDataList) ----------

ARTICLE_FIELDS = {
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

PROFILE_FIELDS = {"username", "bio", "image", "following"}


def assert_article_shape(article_json: dict) -> None:
    """Assert the article JSON has all the fields from Java ArticleData."""
    assert set(article_json.keys()) == ARTICLE_FIELDS
    assert set(article_json["author"].keys()) == PROFILE_FIELDS


# ---------- GET /api/articles ----------


class TestListArticles:
    def test_empty_list(self, seed_user):
        """No articles → empty list with articlesCount 0 (same as Java)."""
        resp = client.get("/api/articles")
        assert resp.status_code == 200
        data = resp.json()
        assert data["articles"] == []
        assert data["articlesCount"] == 0

    def test_returns_articles(self, seed_article):
        resp = client.get("/api/articles")
        assert resp.status_code == 200
        data = resp.json()
        assert data["articlesCount"] == 1
        assert len(data["articles"]) == 1
        assert_article_shape(data["articles"][0])

    def test_filter_by_tag(self, seed_article):
        resp = client.get("/api/articles", params={"tag": "reactjs"})
        assert resp.status_code == 200
        data = resp.json()
        assert data["articlesCount"] == 1

        resp2 = client.get("/api/articles", params={"tag": "nonexistent"})
        data2 = resp2.json()
        assert data2["articlesCount"] == 0

    def test_filter_by_author(self, seed_article):
        resp = client.get("/api/articles", params={"author": "johndoe"})
        assert resp.status_code == 200
        assert resp.json()["articlesCount"] == 1

        resp2 = client.get("/api/articles", params={"author": "nobody"})
        assert resp2.json()["articlesCount"] == 0

    def test_filter_by_favorited(self, seed_article, seed_user, db):
        # mark as favorited
        db.execute(
            article_favorites.insert().values(
                article_id=seed_article.id, user_id=seed_user.id
            )
        )
        db.commit()

        resp = client.get("/api/articles", params={"favorited": "johndoe"})
        assert resp.status_code == 200
        assert resp.json()["articlesCount"] == 1

    def test_offset_limit(self, seed_user, db):
        for i in range(5):
            a = Article.create(
                title=f"Article {i}",
                description="desc",
                body="body",
                tag_list=[],
                user_id=seed_user.id,
                db=db,
            )
            db.add(a)
        db.commit()

        resp = client.get("/api/articles", params={"offset": 2, "limit": 2})
        data = resp.json()
        assert len(data["articles"]) == 2
        assert data["articlesCount"] == 5


# ---------- GET /api/articles/feed ----------


class TestFeed:
    def test_feed_empty_when_not_following(self, seed_user):
        resp = client.get("/api/articles/feed")
        assert resp.status_code == 200
        data = resp.json()
        assert data["articles"] == []
        assert data["articlesCount"] == 0

    def test_feed_returns_followed_authors(self, seed_user, db):
        # create another user and make seed_user follow them
        other = User(
            id=_new_id(),
            email="jane@example.com",
            username="janedoe",
            password="password",
        )
        db.add(other)
        db.commit()

        db.execute(
            user_follows.insert().values(
                follower_id=seed_user.id, followed_id=other.id
            )
        )
        db.commit()

        a = Article.create(
            title="Jane article",
            description="d",
            body="b",
            tag_list=[],
            user_id=other.id,
            db=db,
        )
        db.add(a)
        db.commit()

        resp = client.get("/api/articles/feed")
        data = resp.json()
        assert resp.status_code == 200
        assert data["articlesCount"] == 1
        assert_article_shape(data["articles"][0])


# ---------- POST /api/articles ----------


class TestCreateArticle:
    def test_create(self, seed_user):
        body = {
            "article": {
                "title": "How to train your dragon",
                "description": "Ever wonder how?",
                "body": "You have to believe",
                "tagList": ["reactjs", "angularjs"],
            }
        }
        resp = client.post("/api/articles", json=body)
        assert resp.status_code == 200
        data = resp.json()
        assert "article" in data
        article = data["article"]
        assert_article_shape(article)
        assert article["title"] == "How to train your dragon"
        assert article["slug"] == _to_slug("How to train your dragon")
        assert sorted(article["tagList"]) == sorted(["angularjs", "reactjs"])
        assert article["favorited"] is False
        assert article["favoritesCount"] == 0
        assert article["author"]["username"] == "johndoe"

    def test_create_missing_required_field(self, seed_user):
        body = {"article": {"title": "x", "description": "d"}}
        resp = client.post("/api/articles", json=body)
        assert resp.status_code == 422  # validation error


# ---------- GET /api/articles/{slug} ----------


class TestGetArticle:
    def test_found(self, seed_article):
        resp = client.get(f"/api/articles/{seed_article.slug}")
        assert resp.status_code == 200
        data = resp.json()
        assert "article" in data
        assert_article_shape(data["article"])
        assert data["article"]["slug"] == seed_article.slug

    def test_not_found(self, seed_user):
        resp = client.get("/api/articles/nonexistent-slug")
        assert resp.status_code == 404


# ---------- PUT /api/articles/{slug} ----------


class TestUpdateArticle:
    def test_update_title(self, seed_article):
        body = {"article": {"title": "Updated title", "description": "", "body": ""}}
        resp = client.put(f"/api/articles/{seed_article.slug}", json=body)
        assert resp.status_code == 200
        data = resp.json()
        assert data["article"]["title"] == "Updated title"
        assert data["article"]["slug"] == _to_slug("Updated title")

    def test_update_body(self, seed_article):
        body = {"article": {"title": "", "description": "", "body": "New body"}}
        resp = client.put(f"/api/articles/{seed_article.slug}", json=body)
        assert resp.status_code == 200
        assert resp.json()["article"]["body"] == "New body"

    def test_update_not_found(self, seed_user):
        body = {"article": {"title": "x", "description": "", "body": ""}}
        resp = client.put("/api/articles/nonexistent", json=body)
        assert resp.status_code == 404


# ---------- DELETE /api/articles/{slug} ----------


class TestDeleteArticle:
    def test_delete(self, seed_article):
        resp = client.delete(f"/api/articles/{seed_article.slug}")
        assert resp.status_code == 204

        # confirm gone
        resp2 = client.get(f"/api/articles/{seed_article.slug}")
        assert resp2.status_code == 404

    def test_delete_not_found(self, seed_user):
        resp = client.delete("/api/articles/nonexistent")
        assert resp.status_code == 404


# ---------- Response-shape parity checks ----------


class TestResponseShapeParity:
    """Verify JSON keys match the Java DTOs exactly."""

    def test_single_article_keys(self, seed_article):
        resp = client.get(f"/api/articles/{seed_article.slug}")
        data = resp.json()
        # outer wrapper is {"article": {...}}
        assert set(data.keys()) == {"article"}
        assert_article_shape(data["article"])

    def test_article_list_keys(self, seed_article):
        resp = client.get("/api/articles")
        data = resp.json()
        assert set(data.keys()) == {"articles", "articlesCount"}
        for a in data["articles"]:
            assert_article_shape(a)

    def test_feed_keys(self, seed_user):
        resp = client.get("/api/articles/feed")
        data = resp.json()
        assert set(data.keys()) == {"articles", "articlesCount"}

    def test_datetime_format(self, seed_article):
        """Java serialises Joda DateTime as ISO-8601; we do the same."""
        resp = client.get(f"/api/articles/{seed_article.slug}")
        article = resp.json()["article"]
        # createdAt / updatedAt should be parseable ISO strings
        assert "T" in article["createdAt"]
        assert "T" in article["updatedAt"]

    def test_author_excludes_id(self, seed_article):
        """Java ProfileData has @JsonIgnore on id — id must NOT appear in JSON."""
        resp = client.get(f"/api/articles/{seed_article.slug}")
        author = resp.json()["article"]["author"]
        assert "id" not in author

    def test_tag_list_is_sorted(self, seed_article):
        """Tags are returned sorted alphabetically."""
        resp = client.get(f"/api/articles/{seed_article.slug}")
        tags = resp.json()["article"]["tagList"]
        assert tags == sorted(tags)
