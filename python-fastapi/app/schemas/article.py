from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field


class ProfileResponse(BaseModel):
    username: str
    bio: Optional[str] = None
    image: Optional[str] = None
    following: bool = False


class ArticleResponse(BaseModel):
    id: str
    slug: str
    title: str
    description: str
    body: str
    favorited: bool = False
    favoritesCount: int = 0
    createdAt: datetime
    updatedAt: datetime
    tagList: list[str] = Field(default_factory=list)
    author: ProfileResponse


class SingleArticleResponse(BaseModel):
    article: ArticleResponse


class MultipleArticlesResponse(BaseModel):
    articles: list[ArticleResponse]
    articlesCount: int


class NewArticleRequest(BaseModel):
    title: str
    description: str
    body: str
    tagList: Optional[list[str]] = None


class NewArticleWrapper(BaseModel):
    article: NewArticleRequest


class UpdateArticleRequest(BaseModel):
    title: Optional[str] = ""
    description: Optional[str] = ""
    body: Optional[str] = ""


class UpdateArticleWrapper(BaseModel):
    article: UpdateArticleRequest
