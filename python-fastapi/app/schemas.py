from datetime import datetime

from pydantic import BaseModel, Field


class ProfileResponse(BaseModel):
    username: str
    bio: str | None = None
    image: str | None = None
    following: bool = False


class ArticleResponse(BaseModel):
    slug: str
    title: str
    description: str | None = None
    body: str | None = None
    favorited: bool = False
    favoritesCount: int = 0
    createdAt: str
    updatedAt: str
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
    tagList: list[str] = Field(default_factory=list)


class NewArticleWrapper(BaseModel):
    article: NewArticleRequest


class UpdateArticleRequest(BaseModel):
    title: str | None = None
    body: str | None = None
    description: str | None = None


class UpdateArticleWrapper(BaseModel):
    article: UpdateArticleRequest
