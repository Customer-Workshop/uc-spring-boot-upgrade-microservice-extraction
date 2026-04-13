"""Pydantic models preserving the same JSON response shape as the Java API."""

from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel, Field


# ---------------------------------------------------------------------------
# Profile (nested inside ArticleData as "author")
# ---------------------------------------------------------------------------

class ProfileData(BaseModel):
    """Mirrors io.spring.application.data.ProfileData (excludes id via @JsonIgnore)."""

    username: str
    bio: Optional[str] = None
    image: Optional[str] = None
    following: bool = False


# ---------------------------------------------------------------------------
# Article
# ---------------------------------------------------------------------------

class ArticleData(BaseModel):
    """Mirrors io.spring.application.data.ArticleData.

    JSON field mapping:
      - profileData -> "author" (via @JsonProperty in Java)
    """

    id: str
    slug: str
    title: str
    description: str
    body: str
    favorited: bool = False
    favoritesCount: int = Field(default=0, alias="favoritesCount")
    createdAt: datetime
    updatedAt: datetime
    tagList: List[str] = []
    author: ProfileData

    model_config = {"populate_by_name": True}


class SingleArticleResponse(BaseModel):
    """Wraps a single article: {"article": {...}}."""

    article: ArticleData


class MultipleArticlesResponse(BaseModel):
    """Wraps a list of articles: {"articles": [...], "articlesCount": N}.

    Mirrors io.spring.application.data.ArticleDataList.
    """

    articles: List[ArticleData]
    articlesCount: int


# ---------------------------------------------------------------------------
# Request bodies (wrapped under "article" key like Java @JsonRootName)
# ---------------------------------------------------------------------------

class NewArticleRequest(BaseModel):
    """Inner body for creating an article."""

    title: str
    description: str
    body: str
    tagList: Optional[List[str]] = None


class NewArticleRequestWrapper(BaseModel):
    """Outer wrapper: {"article": {...}}."""

    article: NewArticleRequest


class UpdateArticleRequest(BaseModel):
    """Inner body for updating an article."""

    title: Optional[str] = ""
    body: Optional[str] = ""
    description: Optional[str] = ""


class UpdateArticleRequestWrapper(BaseModel):
    """Outer wrapper: {"article": {...}}."""

    article: UpdateArticleRequest


# ---------------------------------------------------------------------------
# Error responses (mirrors Java CustomizeExceptionHandler)
# ---------------------------------------------------------------------------

class ErrorResponse(BaseModel):
    errors: dict
