"""Pytest tests for the Article Statistics endpoints.

Tests:
  GET /api/articles/{slug}/stats
  GET /api/stats/trending
"""

from datetime import datetime, timedelta, timezone

from fastapi.testclient import TestClient

from app.main import app
from app.models import Article, User, _new_id, article_favorites

# ---------- fixtures come from conftest.py ----------

client = TestClient(app)

STATS_FIELDS = {"slug", "title", "viewCount", "favoriteCount", "commentCount", "daysSincePublished"}
TRENDING_ARTICLE_FIELDS = {"slug", "title", "description", "favoriteCount", "daysSincePublished", "tagList", "author"}


# ---------- GET /api/articles/{slug}/stats ----------


class TestArticleStats:
    def test_stats_basic(self, seed_article):
        """Stats endpoint returns correct shape and zero counts for a fresh article."""
        resp = client.get(f"/api/articles/{seed_article.slug}/stats")
        assert resp.status_code == 200
        data = resp.json()
        assert "stats" in data
        stats = data["stats"]
        assert set(stats.keys()) == STATS_FIELDS
        assert stats["slug"] == seed_article.slug
        assert stats["title"] == seed_article.title
        assert stats["viewCount"] == 0
        assert stats["favoriteCount"] == 0
        assert stats["commentCount"] == 0
        assert stats["daysSincePublished"] >= 0

    def test_stats_not_found(self, seed_user):
        """Returns 404 for a non-existent slug."""
        resp = client.get("/api/articles/nonexistent-slug/stats")
        assert resp.status_code == 404

    def test_stats_favorite_count(self, seed_article, seed_user, db):
        """Favorite count reflects actual favorites."""
        # Add a favorite
        db.execute(
            article_favorites.insert().values(
                article_id=seed_article.id, user_id=seed_user.id
            )
        )
        db.commit()

        resp = client.get(f"/api/articles/{seed_article.slug}/stats")
        assert resp.status_code == 200
        stats = resp.json()["stats"]
        assert stats["favoriteCount"] == 1

    def test_stats_multiple_favorites(self, seed_article, seed_user, db):
        """Favorite count increments with multiple users."""
        # Create another user
        other = User(
            id=_new_id(),
            email="jane@example.com",
            username="janedoe",
            password="password",
        )
        db.add(other)
        db.commit()

        # Both users favorite the article
        db.execute(
            article_favorites.insert().values(
                article_id=seed_article.id, user_id=seed_user.id
            )
        )
        db.execute(
            article_favorites.insert().values(
                article_id=seed_article.id, user_id=other.id
            )
        )
        db.commit()

        resp = client.get(f"/api/articles/{seed_article.slug}/stats")
        stats = resp.json()["stats"]
        assert stats["favoriteCount"] == 2

    def test_stats_days_since_published(self, seed_user, db):
        """Days since published is calculated correctly."""
        # Create an article with a specific created_at in the past
        article = Article.create(
            title="Old article",
            description="desc",
            body="body",
            tag_list=[],
            user_id=seed_user.id,
            db=db,
        )
        article.created_at = datetime.now(timezone.utc) - timedelta(days=10)
        db.add(article)
        db.commit()
        db.refresh(article)

        resp = client.get(f"/api/articles/{article.slug}/stats")
        stats = resp.json()["stats"]
        assert stats["daysSincePublished"] == 10


# ---------- GET /api/stats/trending ----------


class TestTrending:
    def test_trending_empty(self, seed_user):
        """No articles → empty trending list."""
        resp = client.get("/api/stats/trending")
        assert resp.status_code == 200
        data = resp.json()
        assert data["articles"] == []
        assert data["articlesCount"] == 0

    def test_trending_returns_recent_articles(self, seed_article, seed_user):
        """Recent articles appear in trending."""
        resp = client.get("/api/stats/trending")
        assert resp.status_code == 200
        data = resp.json()
        assert data["articlesCount"] == 1
        article = data["articles"][0]
        assert set(article.keys()) == TRENDING_ARTICLE_FIELDS
        assert article["slug"] == seed_article.slug
        assert article["title"] == seed_article.title
        assert "username" in article["author"]

    def test_trending_excludes_old_articles(self, seed_user, db):
        """Articles older than 7 days are excluded from trending."""
        article = Article.create(
            title="Old trending article",
            description="desc",
            body="body",
            tag_list=[],
            user_id=seed_user.id,
            db=db,
        )
        article.created_at = datetime.now(timezone.utc) - timedelta(days=10)
        db.add(article)
        db.commit()

        resp = client.get("/api/stats/trending")
        data = resp.json()
        assert data["articlesCount"] == 0

    def test_trending_ordered_by_favorites(self, seed_user, db):
        """Articles are ordered by favorite count descending."""
        # Create 3 articles
        articles = []
        for i in range(3):
            a = Article.create(
                title=f"Trending article {i}",
                description="desc",
                body="body",
                tag_list=[],
                user_id=seed_user.id,
                db=db,
            )
            db.add(a)
            articles.append(a)
        db.commit()
        for a in articles:
            db.refresh(a)

        # Create extra users to favorite
        users = []
        for i in range(3):
            u = User(
                id=_new_id(),
                email=f"user{i}@example.com",
                username=f"user{i}",
                password="password",
            )
            db.add(u)
            users.append(u)
        db.commit()

        # Article 2 gets 3 favorites, Article 1 gets 1, Article 0 gets 0
        for u in users:
            db.execute(
                article_favorites.insert().values(
                    article_id=articles[2].id, user_id=u.id
                )
            )
        db.execute(
            article_favorites.insert().values(
                article_id=articles[1].id, user_id=users[0].id
            )
        )
        db.commit()

        resp = client.get("/api/stats/trending")
        data = resp.json()
        assert data["articlesCount"] == 3
        assert data["articles"][0]["favoriteCount"] == 3
        assert data["articles"][1]["favoriteCount"] == 1
        assert data["articles"][2]["favoriteCount"] == 0

    def test_trending_limit_10(self, seed_user, db):
        """Trending returns at most 10 articles."""
        for i in range(15):
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

        resp = client.get("/api/stats/trending")
        data = resp.json()
        assert data["articlesCount"] <= 10

    def test_trending_includes_tags(self, seed_article, seed_user):
        """Trending articles include sorted tag list."""
        resp = client.get("/api/stats/trending")
        article = resp.json()["articles"][0]
        assert article["tagList"] == sorted(article["tagList"])

    def test_trending_author_shape(self, seed_article, seed_user):
        """Author object has username, bio, image fields."""
        resp = client.get("/api/stats/trending")
        author = resp.json()["articles"][0]["author"]
        assert set(author.keys()) == {"username", "bio", "image"}
        assert author["username"] == "johndoe"
