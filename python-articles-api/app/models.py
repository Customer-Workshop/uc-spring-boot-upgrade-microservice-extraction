from __future__ import annotations

from datetime import datetime

from sqlalchemy import Column, DateTime, ForeignKey, String, Table, Text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from .database import Base

# Mirrors src/main/resources/db/migration/V1__create_tables.sql

article_tags = Table(
    "article_tags",
    Base.metadata,
    Column("article_id", String(255), ForeignKey("articles.id"), nullable=False),
    Column("tag_id", String(255), ForeignKey("tags.id"), nullable=False),
)


class User(Base):
    __tablename__ = "users"

    id: Mapped[str] = mapped_column(String(255), primary_key=True)
    username: Mapped[str] = mapped_column(String(255), unique=True)
    password: Mapped[str] = mapped_column(String(255))
    email: Mapped[str] = mapped_column(String(255), unique=True)
    bio: Mapped[str | None] = mapped_column(Text, nullable=True)
    image: Mapped[str | None] = mapped_column(String(511), nullable=True)


class Tag(Base):
    __tablename__ = "tags"

    id: Mapped[str] = mapped_column(String(255), primary_key=True)
    name: Mapped[str] = mapped_column(String(255), nullable=False)


class Article(Base):
    __tablename__ = "articles"

    id: Mapped[str] = mapped_column(String(255), primary_key=True)
    user_id: Mapped[str] = mapped_column(String(255), ForeignKey("users.id"))
    slug: Mapped[str] = mapped_column(String(255), unique=True)
    title: Mapped[str] = mapped_column(String(255))
    description: Mapped[str] = mapped_column(Text)
    body: Mapped[str] = mapped_column(Text)
    created_at: Mapped[datetime] = mapped_column(DateTime, nullable=False)
    updated_at: Mapped[datetime] = mapped_column(DateTime, nullable=False)

    author: Mapped[User] = relationship("User")
    tags: Mapped[list[Tag]] = relationship("Tag", secondary=article_tags)


class ArticleFavorite(Base):
    __tablename__ = "article_favorites"

    article_id: Mapped[str] = mapped_column(String(255), primary_key=True)
    user_id: Mapped[str] = mapped_column(String(255), primary_key=True)


class Follow(Base):
    __tablename__ = "follows"

    # follows has no primary key in the Java schema; use a composite key so
    # SQLAlchemy can map it.
    user_id: Mapped[str] = mapped_column(String(255), primary_key=True)
    follow_id: Mapped[str] = mapped_column(String(255), primary_key=True)
