import re
import uuid
from datetime import datetime, timezone

from sqlalchemy import Column, DateTime, ForeignKey, String, Table, Text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database import Base

article_tags = Table(
    "article_tags",
    Base.metadata,
    Column("article_id", String(255), ForeignKey("articles.id"), primary_key=True),
    Column("tag_id", String(255), ForeignKey("tags.id"), primary_key=True),
)

article_favorites = Table(
    "article_favorites",
    Base.metadata,
    Column("article_id", String(255), ForeignKey("articles.id"), primary_key=True),
    Column("user_id", String(255), ForeignKey("users.id"), primary_key=True),
)

follows = Table(
    "follows",
    Base.metadata,
    Column("user_id", String(255), ForeignKey("users.id"), primary_key=True),
    Column("follow_id", String(255), ForeignKey("users.id"), primary_key=True),
)


class User(Base):
    __tablename__ = "users"

    id: Mapped[str] = mapped_column(String(255), primary_key=True, default=lambda: str(uuid.uuid4()))
    username: Mapped[str] = mapped_column(String(255), unique=True)
    password: Mapped[str] = mapped_column(String(255))
    email: Mapped[str] = mapped_column(String(255), unique=True)
    bio: Mapped[str | None] = mapped_column(Text, default=None)
    image: Mapped[str | None] = mapped_column(String(511), default=None)

    articles: Mapped[list["Article"]] = relationship("Article", back_populates="author")

    favorites: Mapped[list["Article"]] = relationship(
        "Article", secondary=article_favorites, backref="favorited_by"
    )

    following: Mapped[list["User"]] = relationship(
        "User",
        secondary=follows,
        primaryjoin=id == follows.c.user_id,
        secondaryjoin=id == follows.c.follow_id,
        backref="followers",
    )


class Tag(Base):
    __tablename__ = "tags"

    id: Mapped[str] = mapped_column(String(255), primary_key=True, default=lambda: str(uuid.uuid4()))
    name: Mapped[str] = mapped_column(String(255), nullable=False)


class Article(Base):
    __tablename__ = "articles"

    id: Mapped[str] = mapped_column(String(255), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id: Mapped[str] = mapped_column(String(255), ForeignKey("users.id"))
    slug: Mapped[str] = mapped_column(String(255), unique=True)
    title: Mapped[str] = mapped_column(String(255))
    description: Mapped[str | None] = mapped_column(Text)
    body: Mapped[str | None] = mapped_column(Text)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=lambda: datetime.now(timezone.utc))
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=lambda: datetime.now(timezone.utc))

    author: Mapped["User"] = relationship("User", back_populates="articles")

    tags: Mapped[list["Tag"]] = relationship("Tag", secondary=article_tags)

    @staticmethod
    def to_slug(title: str) -> str:
        return re.sub(r"[&\ufe30-\uffa0'\"\\s?,\\.]+|\\s+", "-", title.lower()).strip("-")
