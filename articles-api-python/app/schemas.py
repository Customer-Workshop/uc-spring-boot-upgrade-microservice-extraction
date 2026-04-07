"""Pydantic models that mirror the Java DTOs / JSON response shapes."""

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict, Field, field_serializer


# ---------- Profile (author) ----------


class ProfileData(BaseModel):
    """Maps to Java ProfileData. The `id` field is excluded from JSON (JsonIgnore)."""

    model_config = ConfigDict(from_attributes=True)

    username: str
    bio: Optional[str] = None
    image: Optional[str] = None
    following: bool = False


# ---------- Article ----------


class ArticleData(BaseModel):
    """Maps to Java ArticleData. JSON key 'author' comes from @JsonProperty('author')."""

    model_config = ConfigDict(from_attributes=True)

    id: str
    slug: str
    title: str
    description: str
    body: str
    favorited: bool = False
    favorites_count: int = Field(0, alias="favoritesCount")
    created_at: datetime = Field(..., alias="createdAt")
    updated_at: datetime = Field(..., alias="updatedAt")
    tag_list: list[str] = Field(default_factory=list, alias="tagList")
    author: ProfileData

    model_config = ConfigDict(
        from_attributes=True,
        populate_by_name=True,
    )

    @field_serializer("created_at", "updated_at")
    @classmethod
    def serialize_datetime(cls, v: datetime) -> str:
        return v.strftime("%Y-%m-%dT%H:%M:%S.") + f"{v.microsecond // 1000:03d}Z"


class SingleArticleResponse(BaseModel):
    """Wraps a single article: { "article": { ... } }"""

    article: ArticleData


class ArticleDataList(BaseModel):
    """Maps to Java ArticleDataList: { "articles": [...], "articlesCount": N }"""

    articles: list[ArticleData]
    articles_count: int = Field(..., alias="articlesCount")

    model_config = ConfigDict(populate_by_name=True)


# ---------- Request bodies ----------


class NewArticleRequest(BaseModel):
    """Inner body of POST /api/articles: { "article": { ... } }"""

    title: str
    description: str
    body: str
    tag_list: list[str] = Field(default_factory=list, alias="tagList")

    model_config = ConfigDict(populate_by_name=True)


class NewArticleWrapper(BaseModel):
    """Outer wrapper matching Jackson @JsonRootName("article")."""

    article: NewArticleRequest


class UpdateArticleRequest(BaseModel):
    """Inner body of PUT /api/articles/:slug"""

    title: Optional[str] = ""
    description: Optional[str] = ""
    body: Optional[str] = ""


class UpdateArticleWrapper(BaseModel):
    """Outer wrapper."""

    article: UpdateArticleRequest
