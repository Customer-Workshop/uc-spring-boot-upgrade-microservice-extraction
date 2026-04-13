"""SQLAlchemy ORM models mirroring the Java entity classes."""

import uuid
from datetime import datetime, timezone

from sqlalchemy import Column, DateTime, ForeignKey, String, Table, Text
from sqlalchemy.orm import relationship

from app.database import Base

# Association table for Article <-> Tag many-to-many relationship
article_tags = Table(
    "article_tags",
    Base.metadata,
    Column("article_id", String(36), ForeignKey("articles.id"), primary_key=True),
    Column("tag_id", String(36), ForeignKey("tags.id"), primary_key=True),
)

# Association table for user follow relationships
user_follows = Table(
    "user_follows",
    Base.metadata,
    Column("user_id", String(36), ForeignKey("users.id"), primary_key=True),
    Column("target_id", String(36), ForeignKey("users.id"), primary_key=True),
)

# Association table for article favorites
article_favorites = Table(
    "article_favorites",
    Base.metadata,
    Column("article_id", String(36), ForeignKey("articles.id"), primary_key=True),
    Column("user_id", String(36), ForeignKey("users.id"), primary_key=True),
)


class User(Base):
    """Mirrors io.spring.core.user.User."""

    __tablename__ = "users"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    email = Column(String(255), unique=True, nullable=False)
    username = Column(String(255), unique=True, nullable=False)
    password = Column(String(255), nullable=False)
    bio = Column(Text, default="")
    image = Column(String(512), default="")

    articles = relationship("Article", back_populates="author")
    following = relationship(
        "User",
        secondary=user_follows,
        primaryjoin=id == user_follows.c.user_id,
        secondaryjoin=id == user_follows.c.target_id,
        backref="followers",
    )
    favorited_articles = relationship(
        "Article", secondary=article_favorites, back_populates="favorited_by"
    )


class Tag(Base):
    """Mirrors io.spring.core.article.Tag."""

    __tablename__ = "tags"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    name = Column(String(255), unique=True, nullable=False)


class Article(Base):
    """Mirrors io.spring.core.article.Article."""

    __tablename__ = "articles"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    slug = Column(String(255), unique=True, nullable=False, index=True)
    title = Column(String(255), nullable=False)
    description = Column(Text, nullable=False)
    body = Column(Text, nullable=False)
    user_id = Column(String(36), ForeignKey("users.id"), nullable=False)
    created_at = Column(
        DateTime(timezone=True),
        nullable=False,
        default=lambda: datetime.now(timezone.utc),
    )
    updated_at = Column(
        DateTime(timezone=True),
        nullable=False,
        default=lambda: datetime.now(timezone.utc),
        onupdate=lambda: datetime.now(timezone.utc),
    )

    author = relationship("User", back_populates="articles")
    tags = relationship("Tag", secondary=article_tags, lazy="joined")
    favorited_by = relationship(
        "User", secondary=article_favorites, back_populates="favorited_articles"
    )
