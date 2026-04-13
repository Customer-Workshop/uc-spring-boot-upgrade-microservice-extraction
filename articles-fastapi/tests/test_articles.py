import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from app.database import Base, User, get_db
from app.main import app

SQLALCHEMY_TEST_URL = "sqlite:///./test.db"
engine = create_engine(SQLALCHEMY_TEST_URL, connect_args={"check_same_thread": False})
TestSession = sessionmaker(autocommit=False, autoflush=False, bind=engine)


def override_get_db():
    db = TestSession()
    try:
        yield db
    finally:
        db.close()


app.dependency_overrides[get_db] = override_get_db
client = TestClient(app)


@pytest.fixture(autouse=True)
def setup_db():
    Base.metadata.create_all(bind=engine)
    db = TestSession()
    # Create test user
    user = User(
        id="test-user-1",
        username="johndoe",
        email="john@example.com",
        password="hashed_password",
        bio="Test bio",
        image="",
    )
    db.merge(user)
    db.commit()
    db.close()
    yield
    Base.metadata.drop_all(bind=engine)


def test_create_article():
    response = client.post(
        "/articles",
        json={
            "article": {
                "title": "How to learn FastAPI",
                "description": "A guide to FastAPI",
                "body": "FastAPI is great!",
                "tagList": ["python", "fastapi"],
            }
        },
    )
    assert response.status_code == 201
    data = response.json()
    assert data["article"]["title"] == "How to learn FastAPI"
    assert data["article"]["slug"] == "how-to-learn-fastapi"
    assert "python" in data["article"]["tagList"]
    assert "fastapi" in data["article"]["tagList"]
    assert data["article"]["author"]["username"] == "johndoe"


def test_list_articles():
    # Create an article first
    client.post(
        "/articles",
        json={
            "article": {
                "title": "Test Article",
                "description": "Test",
                "body": "Body",
                "tagList": [],
            }
        },
    )
    response = client.get("/articles")
    assert response.status_code == 200
    data = response.json()
    assert data["articlesCount"] >= 1
    assert len(data["articles"]) >= 1


def test_get_article_by_slug():
    client.post(
        "/articles",
        json={
            "article": {
                "title": "Unique Article",
                "description": "Desc",
                "body": "Body",
                "tagList": [],
            }
        },
    )
    response = client.get("/articles/unique-article")
    assert response.status_code == 200
    assert response.json()["article"]["title"] == "Unique Article"


def test_get_article_not_found():
    response = client.get("/articles/nonexistent-slug")
    assert response.status_code == 404


def test_update_article():
    client.post(
        "/articles",
        json={
            "article": {
                "title": "Original Title",
                "description": "Original",
                "body": "Original body",
                "tagList": [],
            }
        },
    )
    response = client.put(
        "/articles/original-title",
        json={"article": {"title": "Updated Title", "body": "Updated body"}},
    )
    assert response.status_code == 200
    data = response.json()
    assert data["article"]["title"] == "Updated Title"
    assert data["article"]["body"] == "Updated body"


def test_delete_article():
    client.post(
        "/articles",
        json={
            "article": {
                "title": "To Delete",
                "description": "Will be deleted",
                "body": "Delete me",
                "tagList": [],
            }
        },
    )
    response = client.delete("/articles/to-delete")
    assert response.status_code == 204

    # Verify deleted
    response = client.get("/articles/to-delete")
    assert response.status_code == 404


def test_list_articles_with_tag_filter():
    client.post(
        "/articles",
        json={
            "article": {
                "title": "Tagged Article",
                "description": "Has tags",
                "body": "Body",
                "tagList": ["python"],
            }
        },
    )
    response = client.get("/articles?tag=python")
    assert response.status_code == 200
    data = response.json()
    assert all("python" in a["tagList"] for a in data["articles"])


def test_list_articles_with_author_filter():
    client.post(
        "/articles",
        json={
            "article": {
                "title": "Author Filter Test",
                "description": "Test",
                "body": "Body",
                "tagList": [],
            }
        },
    )
    response = client.get("/articles?author=johndoe")
    assert response.status_code == 200
    data = response.json()
    assert all(a["author"]["username"] == "johndoe" for a in data["articles"])


def test_list_articles_pagination():
    for i in range(5):
        client.post(
            "/articles",
            json={
                "article": {
                    "title": f"Paginated {i}",
                    "description": "Test",
                    "body": "Body",
                    "tagList": [],
                }
            },
        )
    response = client.get("/articles?limit=2&offset=0")
    assert response.status_code == 200
    data = response.json()
    assert len(data["articles"]) == 2


def test_get_tags():
    client.post(
        "/articles",
        json={
            "article": {
                "title": "Tag Test Article",
                "description": "Test",
                "body": "Body",
                "tagList": ["java", "spring"],
            }
        },
    )
    response = client.get("/tags")
    assert response.status_code == 200
    tags = response.json()["tags"]
    assert "java" in tags
    assert "spring" in tags


def test_duplicate_article_title():
    client.post(
        "/articles",
        json={
            "article": {
                "title": "Duplicate Check",
                "description": "First",
                "body": "First body",
                "tagList": [],
            }
        },
    )
    response = client.post(
        "/articles",
        json={
            "article": {
                "title": "Duplicate Check",
                "description": "Second",
                "body": "Second body",
                "tagList": [],
            }
        },
    )
    assert response.status_code == 422
