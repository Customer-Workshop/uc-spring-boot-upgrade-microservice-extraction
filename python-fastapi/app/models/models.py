import uuid
from datetime import datetime, timezone

from sqlalchemy import Column, DateTime, ForeignKey, String, Table, Text
from sqlalchemy.orm import relationship

from app.database import Base

article_tags = Table(
    "article_tags",
    Base.metadata,
    Column("article_id", String(255), ForeignKey("articles.id"), nullable=False),
    Column("tag_id", String(255), ForeignKey("tags.id"), nullable=False),
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
    Column("user_id", String(255), ForeignKey("users.id"), nullable=False),
    Column("follow_id", String(255), ForeignKey("users.id"), nullable=False),
)


class User(Base):
    __tablename__ = "users"

    id = Column(String(255), primary_key=True, default=lambda: str(uuid.uuid4()))
    username = Column(String(255), unique=True)
    password = Column(String(255))
    email = Column(String(255), unique=True)
    bio = Column(Text)
    image = Column(String(511))

    articles = relationship("Article", back_populates="author")
    favorites = relationship("Article", secondary=article_favorites, back_populates="favorited_by")
    following = relationship(
        "User",
        secondary=follows,
        primaryjoin=id == follows.c.user_id,
        secondaryjoin=id == follows.c.follow_id,
        backref="followers",
    )


class Tag(Base):
    __tablename__ = "tags"

    id = Column(String(255), primary_key=True, default=lambda: str(uuid.uuid4()))
    name = Column(String(255), nullable=False)


class Article(Base):
    __tablename__ = "articles"

    id = Column(String(255), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String(255), ForeignKey("users.id"))
    slug = Column(String(255), unique=True)
    title = Column(String(255))
    description = Column(Text)
    body = Column(Text)
    created_at = Column(DateTime, nullable=False, default=lambda: datetime.now(timezone.utc))
    updated_at = Column(
        DateTime,
        nullable=False,
        default=lambda: datetime.now(timezone.utc),
        onupdate=lambda: datetime.now(timezone.utc),
    )

    author = relationship("User", back_populates="articles")
    tags = relationship("Tag", secondary=article_tags)
    favorited_by = relationship("User", secondary=article_favorites, back_populates="favorites")
