import uuid
from datetime import datetime, timezone

import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from app.database import Base, get_db
from app.main import app
from app.models import Article, Tag, User, article_favorites, article_tags, follows
from app.security import create_token

TEST_DATABASE_URL = "sqlite:///./test.db"

engine = create_engine(TEST_DATABASE_URL, connect_args={"check_same_thread": False})
TestSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


def override_get_db():
    db = TestSessionLocal()
    try:
        yield db
    finally:
        db.close()


app.dependency_overrides[get_db] = override_get_db


@pytest.fixture(autouse=True)
def setup_database():
    Base.metadata.create_all(bind=engine)
    yield
    Base.metadata.drop_all(bind=engine)


@pytest.fixture
def db():
    db = TestSessionLocal()
    try:
        yield db
    finally:
        db.close()


@pytest.fixture
def user1(db):
    user = User(
        id="user-1",
        username="johndoe",
        email="john@example.com",
        password="$2a$10$AbglDchyhkogGBIxNoHdN.pBDK86VNXtF.Vh6N72G9s1rjw7z2b4u",
        bio="Full-stack developer and tech enthusiast",
        image="https://api.dicebear.com/7.x/avataaars/svg?seed=John",
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    return user


@pytest.fixture
def user2(db):
    user = User(
        id="user-2",
        username="janedoe",
        email="jane@example.com",
        password="$2a$10$AbglDchyhkogGBIxNoHdN.pBDK86VNXtF.Vh6N72G9s1rjw7z2b4u",
        bio="Software architect passionate about clean code",
        image="https://api.dicebear.com/7.x/avataaars/svg?seed=Jane",
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    return user


@pytest.fixture
def user1_token(user1):
    return create_token(user1)


@pytest.fixture
def user2_token(user2):
    return create_token(user2)


@pytest.fixture
def sample_tags(db):
    tags = []
    for name in ["java", "spring-boot", "web-development", "tutorial", "best-practices"]:
        tag = Tag(id=str(uuid.uuid4()), name=name)
        db.add(tag)
        tags.append(tag)
    db.commit()
    return tags


@pytest.fixture
def sample_article(db, user1, sample_tags):
    now = datetime.now(timezone.utc)
    article = Article(
        id="article-1",
        user_id=user1.id,
        slug="getting-started-with-spring-boot",
        title="Getting Started with Spring Boot",
        description="A comprehensive guide",
        body="Spring Boot makes it easy...",
        created_at=now,
        updated_at=now,
    )
    article.tags.append(sample_tags[0])  # java
    article.tags.append(sample_tags[1])  # spring-boot
    db.add(article)
    db.commit()
    db.refresh(article)
    return article


@pytest.fixture
def multiple_articles(db, user1, user2, sample_tags):
    now = datetime.now(timezone.utc)
    articles = []

    a1 = Article(
        id="article-1",
        user_id=user1.id,
        slug="getting-started-with-spring-boot",
        title="Getting Started with Spring Boot",
        description="A comprehensive guide",
        body="Spring Boot makes it easy...",
        created_at=now,
        updated_at=now,
    )
    a1.tags.append(sample_tags[0])
    a1.tags.append(sample_tags[1])
    db.add(a1)
    articles.append(a1)

    a2 = Article(
        id="article-2",
        user_id=user2.id,
        slug="rest-api-best-practices",
        title="REST API Best Practices",
        description="Learn the essential principles",
        body="Building a great REST API...",
        created_at=now,
        updated_at=now,
    )
    a2.tags.append(sample_tags[2])
    a2.tags.append(sample_tags[4])
    db.add(a2)
    articles.append(a2)

    db.commit()
    return articles


@pytest.fixture
def follow_user1_user2(db, user1, user2):
    db.execute(follows.insert().values(user_id=user1.id, follow_id=user2.id))
    db.commit()


@pytest.fixture
def favorite_article(db, user2, sample_article):
    db.execute(
        article_favorites.insert().values(
            article_id=sample_article.id, user_id=user2.id
        )
    )
    db.commit()
