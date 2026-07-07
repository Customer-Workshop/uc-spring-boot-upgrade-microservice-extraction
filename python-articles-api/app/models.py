from sqlalchemy import Column, DateTime, String, Text

from .database import Base


class User(Base):
    __tablename__ = "users"

    id = Column(String(255), primary_key=True)
    username = Column(String(255), unique=True)
    password = Column(String(255))
    email = Column(String(255), unique=True)
    bio = Column(Text)
    image = Column(String(511))


class Article(Base):
    __tablename__ = "articles"

    id = Column(String(255), primary_key=True)
    user_id = Column(String(255))
    slug = Column(String(255), unique=True)
    title = Column(String(255))
    description = Column(Text)
    body = Column(Text)
    created_at = Column(DateTime, nullable=False)
    updated_at = Column(DateTime, nullable=False)


class Tag(Base):
    __tablename__ = "tags"

    id = Column(String(255), primary_key=True)
    name = Column(String(255), nullable=False)


class ArticleTag(Base):
    __tablename__ = "article_tags"

    article_id = Column(String(255), primary_key=True)
    tag_id = Column(String(255), primary_key=True)


class ArticleFavorite(Base):
    __tablename__ = "article_favorites"

    article_id = Column(String(255), primary_key=True)
    user_id = Column(String(255), primary_key=True)


class Follow(Base):
    __tablename__ = "follows"

    user_id = Column(String(255), primary_key=True)
    follow_id = Column(String(255), primary_key=True)
