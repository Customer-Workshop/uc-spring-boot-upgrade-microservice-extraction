from __future__ import annotations

from typing import List, Optional

from pydantic import BaseModel, Field


# ---- Request models (body is wrapped in an "article" root, mirroring
# @JsonRootName("article") + spring.jackson.deserialization.UNWRAP_ROOT_VALUE) ----
class NewArticleParam(BaseModel):
    title: str = ""
    description: str = ""
    body: str = ""
    tagList: List[str] = Field(default_factory=list)


class NewArticleRequest(BaseModel):
    article: NewArticleParam


class UpdateArticleParam(BaseModel):
    title: str = ""
    body: str = ""
    description: str = ""


class UpdateArticleRequest(BaseModel):
    article: UpdateArticleParam


# ---- Response models ----
class ProfileData(BaseModel):
    # ProfileData.id is @JsonIgnore in Java, so it is intentionally omitted.
    username: str
    bio: Optional[str]
    image: Optional[str]
    following: bool


class Cursor(BaseModel):
    data: str


class ArticleData(BaseModel):
    id: str
    slug: str
    title: str
    description: str
    body: str
    favorited: bool
    favoritesCount: int
    createdAt: str
    updatedAt: str
    tagList: List[str]
    # Serialized side effect of ArticleData implementing the Node interface
    # (getCursor()); the Java REST response includes it, so we preserve it.
    cursor: Cursor
    author: ProfileData


class SingleArticleResponse(BaseModel):
    article: ArticleData


class MultipleArticlesResponse(BaseModel):
    articles: List[ArticleData]
    articlesCount: int
