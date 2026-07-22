"""Port of src/main/resources/db/migration/V2__seed_data.sql.

Timestamps use the same relative-day offsets as the Java seed
(datetime('now', '-N days')) truncated to whole seconds, so the migrated API
returns the same data for the same inputs.
"""

from __future__ import annotations

from datetime import datetime, timedelta, timezone

from sqlalchemy.orm import Session

from .config import DEFAULT_IMAGE  # noqa: F401  (kept for parity/config surface)
from .database import Base, SessionLocal, engine
from .models import Article, ArticleFavorite, Follow, Tag, User, article_tags

# BCrypt hash of "password123" (matches the Java seed).
PASSWORD_HASH = "$2a$10$AbglDchyhkogGBIxNoHdN.pBDK86VNXtF.Vh6N72G9s1rjw7z2b4u"

USERS = [
    ("user-1", "johndoe", "john@example.com", "Full-stack developer and tech enthusiast", "https://api.dicebear.com/7.x/avataaars/svg?seed=John"),
    ("user-2", "janedoe", "jane@example.com", "Software architect passionate about clean code", "https://api.dicebear.com/7.x/avataaars/svg?seed=Jane"),
    ("user-3", "bobsmith", "bob@example.com", "DevOps engineer and cloud enthusiast", "https://api.dicebear.com/7.x/avataaars/svg?seed=Bob"),
]

TAGS = [
    ("tag-1", "java"),
    ("tag-2", "spring-boot"),
    ("tag-3", "web-development"),
    ("tag-4", "tutorial"),
    ("tag-5", "best-practices"),
    ("tag-6", "microservices"),
    ("tag-7", "api-design"),
]

ARTICLES = [
    ("article-1", "user-1", "getting-started-with-spring-boot", "Getting Started with Spring Boot", "A comprehensive guide to building your first Spring Boot application", "Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications that you can \"just run\". In this article, we will explore the fundamentals of Spring Boot and build a simple REST API.\\n\\n## Prerequisites\\n- Java 11 or higher\\n- Basic understanding of Spring Framework\\n- Maven or Gradle\\n\\n## Creating Your First Application\\nStart by visiting start.spring.io and selecting your dependencies...", 7),
    ("article-2", "user-2", "rest-api-best-practices", "REST API Best Practices", "Learn the essential principles for designing robust REST APIs", "Building a great REST API requires more than just exposing endpoints. In this article, we'll cover the best practices that will make your API intuitive, maintainable, and scalable.\\n\\n## Key Principles\\n1. Use proper HTTP methods\\n2. Implement consistent naming conventions\\n3. Version your API\\n4. Handle errors gracefully\\n5. Document everything\\n\\nLet's dive into each principle...", 5),
    ("article-3", "user-1", "microservices-architecture-guide", "Microservices Architecture Guide", "Understanding microservices patterns and when to use them", "Microservices architecture has become increasingly popular, but it's not a silver bullet. This guide will help you understand when and how to implement microservices effectively.\\n\\n## What are Microservices?\\nMicroservices are an architectural style that structures an application as a collection of loosely coupled services...\\n\\n## Benefits\\n- Independent deployment\\n- Technology diversity\\n- Fault isolation\\n- Scalability", 3),
    ("article-4", "user-3", "docker-for-java-developers", "Docker for Java Developers", "Containerize your Java applications with Docker", "Docker has revolutionized how we deploy applications. In this tutorial, we'll learn how to containerize a Spring Boot application and deploy it using Docker.\\n\\n## Why Docker?\\n- Consistent environments\\n- Easy deployment\\n- Isolation\\n- Portability\\n\\n## Creating a Dockerfile\\nHere's a simple Dockerfile for a Spring Boot app...", 2),
    ("article-5", "user-2", "testing-spring-boot-applications", "Testing Spring Boot Applications", "A complete guide to testing strategies in Spring Boot", "Testing is crucial for maintaining code quality. This article covers unit testing, integration testing, and end-to-end testing in Spring Boot applications.\\n\\n## Testing Layers\\n1. Unit Tests with JUnit and Mockito\\n2. Integration Tests with @SpringBootTest\\n3. API Tests with MockMvc\\n4. Database Tests with @DataJpaTest\\n\\nLet's explore each testing strategy...", 1),
]

ARTICLE_TAGS = [
    ("article-1", "tag-1"), ("article-1", "tag-2"), ("article-1", "tag-4"),
    ("article-2", "tag-3"), ("article-2", "tag-5"), ("article-2", "tag-7"),
    ("article-3", "tag-2"), ("article-3", "tag-6"), ("article-3", "tag-5"),
    ("article-4", "tag-1"), ("article-4", "tag-2"), ("article-4", "tag-4"),
    ("article-5", "tag-1"), ("article-5", "tag-2"), ("article-5", "tag-5"),
]

ARTICLE_FAVORITES = [
    ("article-1", "user-2"), ("article-1", "user-3"),
    ("article-2", "user-1"), ("article-3", "user-2"),
    ("article-4", "user-1"), ("article-5", "user-3"),
]

FOLLOWS = [
    ("user-1", "user-2"), ("user-2", "user-1"),
    ("user-3", "user-1"), ("user-3", "user-2"),
]


def _days_ago(days: int) -> datetime:
    # Java datetime('now', '-N days') is truncated to whole seconds and UTC.
    return (datetime.now(timezone.utc) - timedelta(days=days)).replace(
        microsecond=0, tzinfo=None
    )


def seed(db: Session) -> None:
    for uid, username, email, bio, image in USERS:
        db.add(User(id=uid, username=username, email=email, password=PASSWORD_HASH, bio=bio, image=image))
    for tid, name in TAGS:
        db.add(Tag(id=tid, name=name))
    for aid, uid, slug, title, desc, body, days in ARTICLES:
        ts = _days_ago(days)
        db.add(Article(id=aid, user_id=uid, slug=slug, title=title, description=desc, body=body, created_at=ts, updated_at=ts))
    db.flush()
    for aid, tid in ARTICLE_TAGS:
        db.execute(article_tags.insert().values(article_id=aid, tag_id=tid))
    for aid, uid in ARTICLE_FAVORITES:
        db.add(ArticleFavorite(article_id=aid, user_id=uid))
    for uid, fid in FOLLOWS:
        db.add(Follow(user_id=uid, follow_id=fid))
    db.commit()


def init_and_seed() -> None:
    Base.metadata.create_all(bind=engine)
    db = SessionLocal()
    try:
        if db.query(Article).count() == 0:
            seed(db)
    finally:
        db.close()
