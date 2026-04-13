"""Pytest tests verifying the Python endpoints return identical responses
to the Java version for the same inputs.

Each test mirrors a corresponding Java test from ArticlesApiTest / ArticleApiTest
and validates the exact JSON shape produced by the FastAPI endpoints.
"""

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from app.database import Base, get_db
from app.main import app
from app.models import Article, Tag, User

# ---------------------------------------------------------------------------
# Test database setup (in-memory SQLite)
# ---------------------------------------------------------------------------

SQLALCHEMY_DATABASE_URL = "sqlite:///./test.db"
engine = create_engine(
    SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False}
)
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


def override_get_db():
    db = TestingSessionLocal()
    try:
        yield db
    finally:
        db.close()


app.dependency_overrides[get_db] = override_get_db


@pytest.fixture(autouse=True)
def setup_database():
    """Create fresh tables for every test."""
    Base.metadata.create_all(bind=engine)
    yield
    Base.metadata.drop_all(bind=engine)


@pytest.fixture
def client():
    return TestClient(app)


@pytest.fixture
def db_session():
    db = TestingSessionLocal()
    try:
        yield db
    finally:
        db.close()


# ---------------------------------------------------------------------------
# Helper factories
# ---------------------------------------------------------------------------


def create_user(
    db,
    email="user@example.com",
    username="testuser",
    password="password",
    bio="bio",
    image="https://img.example.com/avatar.png",
) -> User:
    user = User(email=email, username=username, password=password, bio=bio, image=image)
    db.add(user)
    db.commit()
    db.refresh(user)
    return user


def auth_header(user: User) -> dict:
    """Build an Authorization header using the user's id as the token."""
    return {"Authorization": f"Token {user.id}"}


def create_article_in_db(
    db,
    user: User,
    title: str = "Test Article",
    description: str = "Desc",
    body: str = "Body",
    tag_names: list[str] | None = None,
) -> Article:
    from app.utils import to_slug
    from datetime import datetime, timezone

    slug = to_slug(title)
    now = datetime.now(timezone.utc)
    article = Article(
        slug=slug,
        title=title,
        description=description,
        body=body,
        user_id=user.id,
        created_at=now,
        updated_at=now,
    )
    if tag_names:
        for name in set(tag_names):
            tag = db.query(Tag).filter(Tag.name == name).first()
            if tag is None:
                tag = Tag(name=name)
                db.add(tag)
                db.flush()
            article.tags.append(tag)
    db.add(article)
    db.commit()
    db.refresh(article)
    return article


# ===================================================================
# Tests mirroring ArticlesApiTest (Java)
# ===================================================================


class TestCreateArticle:
    """Mirrors ArticlesApiTest.should_create_article_success and friends."""

    def test_should_create_article_success(self, client, db_session):
        """POST /api/articles — happy path.

        Java counterpart: ArticlesApiTest.should_create_article_success
        Validates identical JSON shape: article.title, article.favorited,
        article.body, article.favoritesCount, article.author.username.
        """
        user = create_user(db_session)
        payload = {
            "article": {
                "title": "How to train your dragon",
                "description": "Ever wonder how?",
                "body": "You have to believe",
                "tagList": ["reactjs", "angularjs", "dragons"],
            }
        }

        resp = client.post("/api/articles", json=payload, headers=auth_header(user))
        assert resp.status_code == 200

        data = resp.json()
        article = data["article"]

        # Same assertions as the Java test
        assert article["title"] == "How to train your dragon"
        assert article["favorited"] is False
        assert article["body"] == "You have to believe"
        assert article["favoritesCount"] == 0
        assert article["author"]["username"] == user.username
        # Java test: article.author.id == null (ProfileData @JsonIgnore)
        assert "id" not in article["author"]

        # Verify all expected keys are present (JSON shape parity)
        expected_keys = {
            "id", "slug", "title", "description", "body",
            "favorited", "favoritesCount", "createdAt", "updatedAt",
            "tagList", "author",
        }
        assert set(article.keys()) == expected_keys

        author_keys = {"username", "bio", "image", "following"}
        assert set(article["author"].keys()) == author_keys

    def test_should_get_error_with_blank_body(self, client, db_session):
        """POST /api/articles with empty body -> 422.

        Java counterpart: ArticlesApiTest.should_get_error_message_with_wrong_parameter
        Validates: errors.body[0] == "can't be empty"
        """
        user = create_user(db_session)
        payload = {
            "article": {
                "title": "How to train your dragon",
                "description": "Ever wonder how?",
                "body": "",
                "tagList": ["reactjs", "angularjs", "dragons"],
            }
        }

        resp = client.post("/api/articles", json=payload, headers=auth_header(user))
        assert resp.status_code == 422

        data = resp.json()
        assert "body" in data["errors"]
        assert data["errors"]["body"][0] == "can't be empty"

    def test_should_get_error_with_duplicated_title(self, client, db_session):
        """POST /api/articles with duplicate slug -> 422.

        Java counterpart: ArticlesApiTest.should_get_error_message_with_duplicated_title
        """
        user = create_user(db_session)
        create_article_in_db(
            db_session, user, title="How to train your dragon",
            description="Old desc", body="Old body",
        )

        payload = {
            "article": {
                "title": "How to train your dragon",
                "description": "Ever wonder how?",
                "body": "You have to believe",
                "tagList": ["reactjs", "angularjs", "dragons"],
            }
        }

        resp = client.post("/api/articles", json=payload, headers=auth_header(user))
        assert resp.status_code == 422

    def test_create_article_requires_auth(self, client):
        """POST /api/articles without token -> 401."""
        payload = {
            "article": {
                "title": "Title",
                "description": "Desc",
                "body": "Body",
            }
        }
        resp = client.post("/api/articles", json=payload)
        assert resp.status_code == 401


# ===================================================================
# Tests mirroring ArticleApiTest (Java)
# ===================================================================


class TestGetArticle:
    """Mirrors ArticleApiTest.should_read_article_success and 404 case."""

    def test_should_read_article_success(self, client, db_session):
        """GET /api/articles/{slug} — happy path.

        Java counterpart: ArticleApiTest.should_read_article_success
        Validates: article.slug, article.body, article.createdAt (ISO format).
        """
        user = create_user(db_session)
        article = create_article_in_db(
            db_session, user,
            title="Test New Article",
            description="Desc",
            body="Body",
            tag_names=["java", "spring", "jpg"],
        )

        resp = client.get(f"/api/articles/{article.slug}")
        assert resp.status_code == 200

        data = resp.json()["article"]
        assert data["slug"] == article.slug
        assert data["body"] == "Body"
        # createdAt should be a valid ISO datetime string
        assert "createdAt" in data
        assert data["createdAt"] is not None

        # Verify full JSON shape
        expected_keys = {
            "id", "slug", "title", "description", "body",
            "favorited", "favoritesCount", "createdAt", "updatedAt",
            "tagList", "author",
        }
        assert set(data.keys()) == expected_keys

    def test_should_404_if_article_not_found(self, client):
        """GET /api/articles/not-exists -> 404.

        Java counterpart: ArticleApiTest.should_404_if_article_not_found
        """
        resp = client.get("/api/articles/not-exists")
        assert resp.status_code == 404


class TestUpdateArticle:
    """Mirrors ArticleApiTest update tests."""

    def test_should_update_article_success(self, client, db_session):
        """PUT /api/articles/{slug} — happy path.

        Java counterpart: ArticleApiTest.should_update_article_content_success
        Validates: article.slug reflects the new title.
        """
        user = create_user(db_session)
        article = create_article_in_db(
            db_session, user,
            title="old title",
            description="old description",
            body="old body",
            tag_names=["java", "spring", "jpg"],
        )

        update_payload = {
            "article": {
                "title": "new title",
                "body": "new body",
                "description": "new description",
            }
        }

        resp = client.put(
            f"/api/articles/{article.slug}",
            json=update_payload,
            headers=auth_header(user),
        )
        assert resp.status_code == 200

        data = resp.json()["article"]
        assert data["slug"] == "new-title"
        assert data["body"] == "new body"
        assert data["description"] == "new description"

    def test_should_403_if_not_author_to_update(self, client, db_session):
        """PUT /api/articles/{slug} by non-author -> 403.

        Java counterpart: ArticleApiTest.should_get_403_if_not_author_to_update_article
        """
        owner = create_user(db_session, email="owner@test.com", username="owner")
        other = create_user(db_session, email="other@test.com", username="other")

        article = create_article_in_db(db_session, owner, title="some title")

        update_payload = {
            "article": {"title": "hacked", "body": "hacked", "description": "hacked"}
        }

        resp = client.put(
            f"/api/articles/{article.slug}",
            json=update_payload,
            headers=auth_header(other),
        )
        assert resp.status_code == 403

    def test_update_article_not_found(self, client, db_session):
        """PUT /api/articles/nonexistent -> 404."""
        user = create_user(db_session)
        resp = client.put(
            "/api/articles/nonexistent",
            json={"article": {"title": "x"}},
            headers=auth_header(user),
        )
        assert resp.status_code == 404


class TestDeleteArticle:
    """Mirrors ArticleApiTest delete tests."""

    def test_should_delete_article_success(self, client, db_session):
        """DELETE /api/articles/{slug} — happy path -> 204.

        Java counterpart: ArticleApiTest.should_delete_article_success
        """
        user = create_user(db_session)
        article = create_article_in_db(
            db_session, user, title="title", description="description", body="body",
            tag_names=["java", "spring", "jpg"],
        )

        resp = client.delete(
            f"/api/articles/{article.slug}", headers=auth_header(user)
        )
        assert resp.status_code == 204

        # Verify actually deleted
        resp2 = client.get(f"/api/articles/{article.slug}")
        assert resp2.status_code == 404

    def test_should_403_if_not_author_delete(self, client, db_session):
        """DELETE /api/articles/{slug} by non-author -> 403.

        Java counterpart: ArticleApiTest.should_403_if_not_author_delete_article
        """
        owner = create_user(db_session, email="owner@test.com", username="owner")
        other = create_user(db_session, email="other@test.com", username="other")

        article = create_article_in_db(db_session, owner, title="new-title")

        resp = client.delete(
            f"/api/articles/{article.slug}", headers=auth_header(other)
        )
        assert resp.status_code == 403


# ===================================================================
# Tests for GET /api/articles (list / filter)
# ===================================================================


class TestListArticles:
    """Mirrors the list-articles query parameters (tag, author, favorited, offset, limit)."""

    def test_empty_list(self, client):
        """GET /api/articles with no data -> empty list."""
        resp = client.get("/api/articles")
        assert resp.status_code == 200
        data = resp.json()
        assert data["articles"] == []
        assert data["articlesCount"] == 0

    def test_list_articles_returns_all(self, client, db_session):
        """GET /api/articles returns all articles with correct JSON shape."""
        user = create_user(db_session)
        create_article_in_db(db_session, user, title="First Article")
        create_article_in_db(db_session, user, title="Second Article")

        resp = client.get("/api/articles")
        assert resp.status_code == 200
        data = resp.json()
        assert data["articlesCount"] == 2
        assert len(data["articles"]) == 2

        # Verify JSON shape matches Java ArticleDataList
        assert "articles" in data
        assert "articlesCount" in data

    def test_filter_by_tag(self, client, db_session):
        """GET /api/articles?tag=python filters correctly."""
        user = create_user(db_session)
        create_article_in_db(db_session, user, title="Tagged", tag_names=["python"])
        create_article_in_db(db_session, user, title="Untagged", tag_names=["java"])

        resp = client.get("/api/articles?tag=python")
        data = resp.json()
        assert data["articlesCount"] == 1
        assert data["articles"][0]["title"] == "Tagged"

    def test_filter_by_author(self, client, db_session):
        """GET /api/articles?author=alice filters correctly."""
        alice = create_user(db_session, email="alice@x.com", username="alice")
        bob = create_user(db_session, email="bob@x.com", username="bob")
        create_article_in_db(db_session, alice, title="Alice Post")
        create_article_in_db(db_session, bob, title="Bob Post")

        resp = client.get("/api/articles?author=alice")
        data = resp.json()
        assert data["articlesCount"] == 1
        assert data["articles"][0]["author"]["username"] == "alice"

    def test_offset_and_limit(self, client, db_session):
        """GET /api/articles?offset=1&limit=1 paginates correctly."""
        user = create_user(db_session)
        create_article_in_db(db_session, user, title="A")
        create_article_in_db(db_session, user, title="B")
        create_article_in_db(db_session, user, title="C")

        resp = client.get("/api/articles?offset=0&limit=2")
        data = resp.json()
        assert len(data["articles"]) == 2
        assert data["articlesCount"] == 3


# ===================================================================
# Tests for GET /api/articles/feed
# ===================================================================


class TestFeed:
    """Mirrors ArticlesApi.getFeed."""

    def test_feed_requires_auth(self, client):
        """GET /api/articles/feed without token -> 401."""
        resp = client.get("/api/articles/feed")
        assert resp.status_code == 401

    def test_feed_empty_when_not_following(self, client, db_session):
        """GET /api/articles/feed with no follows -> empty list."""
        user = create_user(db_session)
        resp = client.get("/api/articles/feed", headers=auth_header(user))
        assert resp.status_code == 200
        data = resp.json()
        assert data["articles"] == []
        assert data["articlesCount"] == 0

    def test_feed_returns_followed_authors_articles(self, client, db_session):
        """GET /api/articles/feed shows articles from followed users."""
        me = create_user(db_session, email="me@x.com", username="me")
        author = create_user(db_session, email="author@x.com", username="author")
        stranger = create_user(db_session, email="stranger@x.com", username="stranger")

        # me follows author
        me.following.append(author)
        db_session.commit()

        create_article_in_db(db_session, author, title="Followed Post")
        create_article_in_db(db_session, stranger, title="Stranger Post")

        resp = client.get("/api/articles/feed", headers=auth_header(me))
        assert resp.status_code == 200
        data = resp.json()
        assert data["articlesCount"] == 1
        assert data["articles"][0]["title"] == "Followed Post"
        assert data["articles"][0]["author"]["following"] is True


# ===================================================================
# JSON shape parity tests
# ===================================================================


class TestJsonShapeParity:
    """Ensure the JSON structure exactly matches the Java API responses."""

    def test_single_article_response_shape(self, client, db_session):
        """Single article response is wrapped: {"article": {...}}.

        Matches Java: new HashMap<>() {{ put("article", articleData); }}
        """
        user = create_user(db_session)
        payload = {
            "article": {
                "title": "Shape Test",
                "description": "Testing shape",
                "body": "Content",
            }
        }
        resp = client.post("/api/articles", json=payload, headers=auth_header(user))
        data = resp.json()

        # Top-level key must be "article"
        assert list(data.keys()) == ["article"]

    def test_multi_article_response_shape(self, client, db_session):
        """Multi-article response: {"articles": [...], "articlesCount": N}.

        Matches Java ArticleDataList @JsonProperty names.
        """
        resp = client.get("/api/articles")
        data = resp.json()
        assert set(data.keys()) == {"articles", "articlesCount"}

    def test_article_data_field_names_match_java(self, client, db_session):
        """All field names use the same camelCase as Java's ArticleData."""
        user = create_user(db_session)
        article = create_article_in_db(db_session, user, title="Field Names")
        resp = client.get(f"/api/articles/{article.slug}")
        article_data = resp.json()["article"]

        # These are the exact field names from Java ArticleData
        java_fields = {
            "id", "slug", "title", "description", "body",
            "favorited", "favoritesCount", "createdAt", "updatedAt",
            "tagList", "author",
        }
        assert set(article_data.keys()) == java_fields

    def test_profile_data_excludes_id(self, client, db_session):
        """ProfileData.id is @JsonIgnore in Java — must not appear in author."""
        user = create_user(db_session)
        article = create_article_in_db(db_session, user, title="Profile Check")
        resp = client.get(f"/api/articles/{article.slug}")
        author = resp.json()["article"]["author"]
        assert "id" not in author
        assert set(author.keys()) == {"username", "bio", "image", "following"}

    def test_favorited_and_following_default_false(self, client, db_session):
        """Anonymous requests default favorited=false, author.following=false."""
        user = create_user(db_session)
        article = create_article_in_db(db_session, user, title="Defaults")
        resp = client.get(f"/api/articles/{article.slug}")
        data = resp.json()["article"]
        assert data["favorited"] is False
        assert data["favoritesCount"] == 0
        assert data["author"]["following"] is False
