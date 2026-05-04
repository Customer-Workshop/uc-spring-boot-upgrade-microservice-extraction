from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


class TestGetArticles:
    def test_get_articles_returns_list(self, multiple_articles):
        resp = client.get("/api/articles")
        assert resp.status_code == 200
        data = resp.json()
        assert "articles" in data
        assert "articlesCount" in data
        assert data["articlesCount"] == 2
        assert len(data["articles"]) == 2

    def test_get_articles_empty(self):
        resp = client.get("/api/articles")
        assert resp.status_code == 200
        data = resp.json()
        assert data["articles"] == []
        assert data["articlesCount"] == 0

    def test_get_articles_filter_by_tag(self, multiple_articles):
        resp = client.get("/api/articles", params={"tag": "java"})
        assert resp.status_code == 200
        data = resp.json()
        assert data["articlesCount"] == 1
        assert data["articles"][0]["slug"] == "getting-started-with-spring-boot"

    def test_get_articles_filter_by_author(self, multiple_articles):
        resp = client.get("/api/articles", params={"author": "janedoe"})
        assert resp.status_code == 200
        data = resp.json()
        assert data["articlesCount"] == 1
        assert data["articles"][0]["slug"] == "rest-api-best-practices"

    def test_get_articles_filter_by_favorited(
        self, db, multiple_articles, user2
    ):
        from app.models import article_favorites

        db.execute(
            article_favorites.insert().values(
                article_id=multiple_articles[0].id, user_id=user2.id
            )
        )
        db.commit()
        resp = client.get("/api/articles", params={"favorited": "janedoe"})
        assert resp.status_code == 200
        data = resp.json()
        assert data["articlesCount"] == 1

    def test_get_articles_with_pagination(self, multiple_articles):
        resp = client.get("/api/articles", params={"offset": 0, "limit": 1})
        assert resp.status_code == 200
        data = resp.json()
        assert len(data["articles"]) == 1
        assert data["articlesCount"] == 2

    def test_article_response_shape(self, sample_article):
        resp = client.get("/api/articles")
        assert resp.status_code == 200
        article = resp.json()["articles"][0]

        assert "slug" in article
        assert "title" in article
        assert "description" in article
        assert "body" in article
        assert "favorited" in article
        assert "favoritesCount" in article
        assert "createdAt" in article
        assert "updatedAt" in article
        assert "tagList" in article
        assert "author" in article

        author = article["author"]
        assert "username" in author
        assert "bio" in author
        assert "image" in author
        assert "following" in author

        assert "id" not in author

    def test_article_favorited_flag_when_authenticated(
        self, sample_article, favorite_article, user2_token
    ):
        resp = client.get(
            "/api/articles",
            headers={"Authorization": f"Token {user2_token}"},
        )
        assert resp.status_code == 200
        article = resp.json()["articles"][0]
        assert article["favorited"] is True
        assert article["favoritesCount"] == 1


class TestGetArticleBySlug:
    def test_get_article_success(self, sample_article):
        resp = client.get("/api/articles/getting-started-with-spring-boot")
        assert resp.status_code == 200
        data = resp.json()
        assert "article" in data
        assert data["article"]["slug"] == "getting-started-with-spring-boot"
        assert data["article"]["title"] == "Getting Started with Spring Boot"
        assert data["article"]["body"] == "Spring Boot makes it easy..."
        assert data["article"]["favorited"] is False
        assert data["article"]["favoritesCount"] == 0

    def test_get_article_not_found(self):
        resp = client.get("/api/articles/nonexistent-slug")
        assert resp.status_code == 404

    def test_get_article_datetime_format(self, sample_article):
        resp = client.get("/api/articles/getting-started-with-spring-boot")
        article = resp.json()["article"]
        created = article["createdAt"]
        assert created.endswith("Z")
        assert "T" in created

    def test_get_article_tag_list_sorted(self, sample_article):
        resp = client.get("/api/articles/getting-started-with-spring-boot")
        article = resp.json()["article"]
        assert article["tagList"] == sorted(article["tagList"])


class TestCreateArticle:
    def test_create_article_success(self, user1_token):
        payload = {
            "article": {
                "title": "How to train your dragon",
                "description": "Ever wonder how?",
                "body": "You have to believe",
                "tagList": ["reactjs", "angularjs", "dragons"],
            }
        }
        resp = client.post(
            "/api/articles",
            json=payload,
            headers={"Authorization": f"Token {user1_token}"},
        )
        assert resp.status_code == 200
        data = resp.json()
        assert "article" in data
        article = data["article"]
        assert article["title"] == "How to train your dragon"
        assert article["slug"] == "how-to-train-your-dragon"
        assert article["description"] == "Ever wonder how?"
        assert article["body"] == "You have to believe"
        assert article["favorited"] is False
        assert article["favoritesCount"] == 0
        assert article["author"]["username"] == "johndoe"
        assert set(article["tagList"]) == {"reactjs", "angularjs", "dragons"}

    def test_create_article_without_auth(self):
        payload = {
            "article": {
                "title": "Test",
                "description": "Test",
                "body": "Test",
            }
        }
        resp = client.post("/api/articles", json=payload)
        assert resp.status_code == 401

    def test_create_article_empty_body_returns_422(self, user1_token):
        payload = {
            "article": {
                "title": "How to train your dragon",
                "description": "Ever wonder how?",
                "body": "",
                "tagList": ["reactjs"],
            }
        }
        resp = client.post(
            "/api/articles",
            json=payload,
            headers={"Authorization": f"Token {user1_token}"},
        )
        assert resp.status_code == 422

    def test_create_article_duplicate_title(self, user1_token, sample_article):
        payload = {
            "article": {
                "title": "Getting Started with Spring Boot",
                "description": "Another guide",
                "body": "Content",
            }
        }
        resp = client.post(
            "/api/articles",
            json=payload,
            headers={"Authorization": f"Token {user1_token}"},
        )
        assert resp.status_code == 422

    def test_create_article_without_tags(self, user1_token):
        payload = {
            "article": {
                "title": "Article Without Tags",
                "description": "No tags",
                "body": "Content without tags",
            }
        }
        resp = client.post(
            "/api/articles",
            json=payload,
            headers={"Authorization": f"Token {user1_token}"},
        )
        assert resp.status_code == 200
        article = resp.json()["article"]
        assert article["tagList"] == []


class TestUpdateArticle:
    def test_update_article_success(self, user1_token, sample_article):
        payload = {
            "article": {
                "title": "Updated Title",
                "body": "Updated body",
                "description": "Updated description",
            }
        }
        resp = client.put(
            f"/api/articles/{sample_article.slug}",
            json=payload,
            headers={"Authorization": f"Token {user1_token}"},
        )
        assert resp.status_code == 200
        article = resp.json()["article"]
        assert article["title"] == "Updated Title"
        assert article["slug"] == "updated-title"
        assert article["body"] == "Updated body"
        assert article["description"] == "Updated description"

    def test_update_article_partial(self, user1_token, sample_article):
        payload = {"article": {"title": "Only Title Changed"}}
        resp = client.put(
            f"/api/articles/{sample_article.slug}",
            json=payload,
            headers={"Authorization": f"Token {user1_token}"},
        )
        assert resp.status_code == 200
        article = resp.json()["article"]
        assert article["title"] == "Only Title Changed"
        assert article["body"] == "Spring Boot makes it easy..."

    def test_update_article_not_found(self, user1_token):
        payload = {"article": {"title": "New Title"}}
        resp = client.put(
            "/api/articles/nonexistent",
            json=payload,
            headers={"Authorization": f"Token {user1_token}"},
        )
        assert resp.status_code == 404

    def test_update_article_not_authorized(self, user2_token, sample_article):
        payload = {"article": {"title": "Hacked Title"}}
        resp = client.put(
            f"/api/articles/{sample_article.slug}",
            json=payload,
            headers={"Authorization": f"Token {user2_token}"},
        )
        assert resp.status_code == 403

    def test_update_article_without_auth(self, sample_article):
        payload = {"article": {"title": "No Auth"}}
        resp = client.put(
            f"/api/articles/{sample_article.slug}",
            json=payload,
        )
        assert resp.status_code == 401


class TestDeleteArticle:
    def test_delete_article_success(self, user1_token, sample_article):
        resp = client.delete(
            f"/api/articles/{sample_article.slug}",
            headers={"Authorization": f"Token {user1_token}"},
        )
        assert resp.status_code == 204

        resp = client.get(f"/api/articles/{sample_article.slug}")
        assert resp.status_code == 404

    def test_delete_article_not_found(self, user1_token):
        resp = client.delete(
            "/api/articles/nonexistent",
            headers={"Authorization": f"Token {user1_token}"},
        )
        assert resp.status_code == 404

    def test_delete_article_not_authorized(self, user2_token, sample_article):
        resp = client.delete(
            f"/api/articles/{sample_article.slug}",
            headers={"Authorization": f"Token {user2_token}"},
        )
        assert resp.status_code == 403

    def test_delete_article_without_auth(self, sample_article):
        resp = client.delete(f"/api/articles/{sample_article.slug}")
        assert resp.status_code == 401


class TestGetFeed:
    def test_feed_returns_followed_articles(
        self, user1_token, multiple_articles, follow_user1_user2
    ):
        resp = client.get(
            "/api/articles/feed",
            headers={"Authorization": f"Token {user1_token}"},
        )
        assert resp.status_code == 200
        data = resp.json()
        assert data["articlesCount"] == 1
        assert data["articles"][0]["author"]["username"] == "janedoe"

    def test_feed_empty_when_not_following(self, user1_token, multiple_articles):
        resp = client.get(
            "/api/articles/feed",
            headers={"Authorization": f"Token {user1_token}"},
        )
        assert resp.status_code == 200
        data = resp.json()
        assert data["articles"] == []
        assert data["articlesCount"] == 0

    def test_feed_requires_auth(self):
        resp = client.get("/api/articles/feed")
        assert resp.status_code == 401

    def test_feed_response_shape(
        self, user1_token, multiple_articles, follow_user1_user2
    ):
        resp = client.get(
            "/api/articles/feed",
            headers={"Authorization": f"Token {user1_token}"},
        )
        assert resp.status_code == 200
        data = resp.json()
        assert "articles" in data
        assert "articlesCount" in data
        article = data["articles"][0]
        assert "slug" in article
        assert "author" in article
        assert "following" in article["author"]


class TestResponseParity:
    """Tests ensuring the Python API returns responses identical in shape
    to the Java Spring Boot version."""

    def test_single_article_wrapper_key(self, sample_article):
        resp = client.get(f"/api/articles/{sample_article.slug}")
        data = resp.json()
        assert list(data.keys()) == ["article"]

    def test_multiple_articles_wrapper_keys(self, multiple_articles):
        resp = client.get("/api/articles")
        data = resp.json()
        assert set(data.keys()) == {"articles", "articlesCount"}

    def test_datetime_iso_format(self, sample_article):
        resp = client.get(f"/api/articles/{sample_article.slug}")
        article = resp.json()["article"]
        created = article["createdAt"]
        assert "T" in created
        assert created.endswith("Z")
        parts = created.split(".")
        assert len(parts) == 2
        assert len(parts[1]) == 4  # 3 digits + 'Z'

    def test_author_profile_excludes_id(self, sample_article):
        resp = client.get(f"/api/articles/{sample_article.slug}")
        author = resp.json()["article"]["author"]
        assert "id" not in author
        assert "username" in author
        assert "bio" in author
        assert "image" in author
        assert "following" in author

    def test_create_response_matches_java_shape(self, user1_token):
        payload = {
            "article": {
                "title": "How to train your dragon",
                "description": "Ever wonder how?",
                "body": "You have to believe",
                "tagList": ["reactjs", "angularjs", "dragons"],
            }
        }
        resp = client.post(
            "/api/articles",
            json=payload,
            headers={"Authorization": f"Token {user1_token}"},
        )
        assert resp.status_code == 200
        data = resp.json()
        assert list(data.keys()) == ["article"]

        article = data["article"]
        expected_keys = {
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
        assert set(article.keys()) == expected_keys

    def test_favorited_defaults_false(self, sample_article):
        resp = client.get(f"/api/articles/{sample_article.slug}")
        assert resp.json()["article"]["favorited"] is False

    def test_favorites_count_integer(self, sample_article, favorite_article):
        resp = client.get(f"/api/articles/{sample_article.slug}")
        count = resp.json()["article"]["favoritesCount"]
        assert isinstance(count, int)
        assert count == 1

    def test_delete_returns_204_no_content(self, user1_token, sample_article):
        resp = client.delete(
            f"/api/articles/{sample_article.slug}",
            headers={"Authorization": f"Token {user1_token}"},
        )
        assert resp.status_code == 204
        assert resp.content == b""
