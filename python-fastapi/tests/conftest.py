from datetime import datetime, timezone

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine, event
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

from app.database import Base, get_db
from app.main import app
from app.models.models import Article, Tag, User, article_favorites, follows
from app.services.jwt_service import create_token


@pytest.fixture()
def db_session():
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )

    @event.listens_for(engine, "connect")
    def _fk_pragma(dbapi_conn, _):
        cursor = dbapi_conn.cursor()
        cursor.execute("PRAGMA foreign_keys=ON")
        cursor.close()

    Base.metadata.create_all(bind=engine)
    TestingSession = sessionmaker(autocommit=False, autoflush=False, bind=engine)
    session = TestingSession()
    try:
        yield session
    finally:
        session.close()
        Base.metadata.drop_all(bind=engine)


@pytest.fixture()
def client(db_session):
    def override_get_db():
        try:
            yield db_session
        finally:
            pass

    app.dependency_overrides[get_db] = override_get_db
    with TestClient(app) as c:
        yield c
    app.dependency_overrides.clear()


@pytest.fixture()
def seed_users(db_session):
    users = []
    for i, (uid, uname, email) in enumerate(
        [
            ("user-1", "johndoe", "john@example.com"),
            ("user-2", "janedoe", "jane@example.com"),
            ("user-3", "bobsmith", "bob@example.com"),
        ]
    ):
        user = User(
            id=uid,
            username=uname,
            email=email,
            password="$2a$10$AbglDchyhkogGBIxNoHdN.pBDK86VNXtF.Vh6N72G9s1rjw7z2b4u",
            bio=f"Bio for {uname}",
            image=f"https://api.dicebear.com/7.x/avataaars/svg?seed={uname}",
        )
        db_session.add(user)
        users.append(user)
    db_session.commit()
    return users


@pytest.fixture()
def seed_tags(db_session):
    tag_data = [
        ("tag-1", "java"),
        ("tag-2", "spring-boot"),
        ("tag-3", "web-development"),
        ("tag-4", "tutorial"),
        ("tag-5", "best-practices"),
    ]
    tags = []
    for tid, name in tag_data:
        tag = Tag(id=tid, name=name)
        db_session.add(tag)
        tags.append(tag)
    db_session.commit()
    return tags


@pytest.fixture()
def seed_articles(db_session, seed_users, seed_tags):
    now = datetime.now(timezone.utc)
    articles_data = [
        {
            "id": "article-1",
            "user_id": "user-1",
            "slug": "getting-started-with-spring-boot",
            "title": "Getting Started with Spring Boot",
            "description": "A comprehensive guide",
            "body": "Spring Boot makes it easy...",
            "tag_ids": ["tag-1", "tag-2", "tag-4"],
        },
        {
            "id": "article-2",
            "user_id": "user-2",
            "slug": "rest-api-best-practices",
            "title": "REST API Best Practices",
            "description": "Learn the essential principles",
            "body": "Building a great REST API...",
            "tag_ids": ["tag-3", "tag-5"],
        },
        {
            "id": "article-3",
            "user_id": "user-1",
            "slug": "microservices-architecture-guide",
            "title": "Microservices Architecture Guide",
            "description": "Understanding microservices",
            "body": "Microservices architecture...",
            "tag_ids": ["tag-2", "tag-5"],
        },
    ]

    articles = []
    for a in articles_data:
        article = Article(
            id=a["id"],
            user_id=a["user_id"],
            slug=a["slug"],
            title=a["title"],
            description=a["description"],
            body=a["body"],
            created_at=now,
            updated_at=now,
        )
        for tid in a["tag_ids"]:
            tag = db_session.query(Tag).filter(Tag.id == tid).first()
            article.tags.append(tag)
        db_session.add(article)
        articles.append(article)

    db_session.commit()
    return articles


@pytest.fixture()
def seed_favorites(db_session, seed_articles, seed_users):
    db_session.execute(
        article_favorites.insert(),
        [
            {"article_id": "article-1", "user_id": "user-2"},
            {"article_id": "article-1", "user_id": "user-3"},
            {"article_id": "article-2", "user_id": "user-1"},
        ],
    )
    db_session.commit()


@pytest.fixture()
def seed_follows(db_session, seed_users):
    db_session.execute(
        follows.insert(),
        [
            {"user_id": "user-1", "follow_id": "user-2"},
            {"user_id": "user-2", "follow_id": "user-1"},
            {"user_id": "user-3", "follow_id": "user-1"},
        ],
    )
    db_session.commit()


def auth_header(user_id: str) -> dict[str, str]:
    token = create_token(user_id)
    return {"Authorization": f"Token {token}"}
