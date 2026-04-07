"""SQLAlchemy ORM models mirroring the Java domain entities."""

import re
import uuid
from datetime import datetime, timezone

from sqlalchemy import Column, DateTime, ForeignKey, String, Table, Text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database import Base

# ---------- association tables ----------

article_tags = Table(
    "article_tags",
    Base.metadata,
    Column("article_id", String(36), ForeignKey("articles.id"), primary_key=True),
    Column("tag_id", String(36), ForeignKey("tags.id"), primary_key=True),
)

article_favorites = Table(
    "article_favorites",
    Base.metadata,
    Column("article_id", String(36), ForeignKey("articles.id"), primary_key=True),
    Column("user_id", String(36), ForeignKey("users.id"), primary_key=True),
)

user_follows = Table(
    "user_follows",
    Base.metadata,
    Column("follower_id", String(36), ForeignKey("users.id"), primary_key=True),
    Column("followed_id", String(36), ForeignKey("users.id"), primary_key=True),
)


def _utcnow() -> datetime:
    return datetime.now(timezone.utc)


def _new_id() -> str:
    return str(uuid.uuid4())


def _to_slug(title: str) -> str:
    """Replicate Java Article.toSlug(): lower-case, replace special chars with '-'."""
    return re.sub(r"[&\ufe30-\uffa0'\"\\s?,\\.]+|\\s+", "-", title.lower()).strip("-")


# ---------- ORM models ----------


class User(Base):
    __tablename__ = "users"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=_new_id)
    email: Mapped[str] = mapped_column(String(255), unique=True, nullable=False)
    username: Mapped[str] = mapped_column(String(255), unique=True, nullable=False)
    password: Mapped[str] = mapped_column(String(255), nullable=False)
    bio: Mapped[str | None] = mapped_column(Text, nullable=True, default="")
    image: Mapped[str | None] = mapped_column(String(512), nullable=True, default="")

    articles: Mapped[list["Article"]] = relationship(back_populates="author")

    following: Mapped[list["User"]] = relationship(
        "User",
        secondary=user_follows,
        primaryjoin=id == user_follows.c.follower_id,
        secondaryjoin=id == user_follows.c.followed_id,
        lazy="select",
    )


class Tag(Base):
    __tablename__ = "tags"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=_new_id)
    name: Mapped[str] = mapped_column(String(255), unique=True, nullable=False)


class Article(Base):
    __tablename__ = "articles"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=_new_id)
    slug: Mapped[str] = mapped_column(String(255), unique=True, nullable=False)
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    description: Mapped[str] = mapped_column(Text, nullable=False)
    body: Mapped[str] = mapped_column(Text, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=_utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=_utcnow, onupdate=_utcnow)
    user_id: Mapped[str] = mapped_column(String(36), ForeignKey("users.id"), nullable=False)

    author: Mapped["User"] = relationship(back_populates="articles", lazy="joined")
    tags: Mapped[list["Tag"]] = relationship(secondary=article_tags, lazy="joined")
    favorited_by: Mapped[list["User"]] = relationship(secondary=article_favorites, lazy="select")

    def update(self, title: str | None, description: str | None, body: str | None) -> None:
        if title:
            self.title = title
            self.slug = _to_slug(title)
        if description:
            self.description = description
        if body:
            self.body = body
        self.updated_at = _utcnow()

    @staticmethod
    def create(title: str, description: str, body: str, tag_list: list[str], user_id: str, db) -> "Article":
        """Factory that mirrors the Java constructor logic."""
        slug = _to_slug(title)
        article = Article(
            id=_new_id(),
            slug=slug,
            title=title,
            description=description,
            body=body,
            user_id=user_id,
        )
        # resolve or create tags
        for tag_name in set(tag_list or []):
            existing = db.query(Tag).filter(Tag.name == tag_name).first()
            if existing is None:
                existing = Tag(id=_new_id(), name=tag_name)
                db.add(existing)
            article.tags.append(existing)
        return article
