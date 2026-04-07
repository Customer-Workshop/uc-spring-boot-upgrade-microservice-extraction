"""Shared test fixtures for all test modules.

Uses a single in-memory SQLite engine so that app.dependency_overrides[get_db]
is set exactly once — avoiding conflicts when multiple test files are collected.
"""

import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

from app.database import Base, get_db
from app.main import app
from app.models import Article, User, _new_id

SQLALCHEMY_DATABASE_URL = "sqlite://"

engine = create_engine(
    SQLALCHEMY_DATABASE_URL,
    connect_args={"check_same_thread": False},
    poolclass=StaticPool,
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
    """Create tables before each test and drop them after."""
    Base.metadata.create_all(bind=engine)
    yield
    Base.metadata.drop_all(bind=engine)


@pytest.fixture()
def db():
    db = TestingSessionLocal()
    try:
        yield db
    finally:
        db.close()


@pytest.fixture()
def seed_user(db):
    """Create a default user (mirrors the Java test fixtures)."""
    user = User(
        id=_new_id(),
        email="john@example.com",
        username="johndoe",
        password="password",
        bio="A short bio",
        image="https://example.com/photo.jpg",
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    return user


@pytest.fixture()
def seed_article(db, seed_user):
    """Create a single article with tags."""
    article = Article.create(
        title="How to train your dragon",
        description="Ever wonder how?",
        body="You have to believe",
        tag_list=["reactjs", "angularjs", "dragons"],
        user_id=seed_user.id,
        db=db,
    )
    db.add(article)
    db.commit()
    db.refresh(article)
    return article
