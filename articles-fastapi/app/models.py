from datetime import datetime
from typing import Optional
from pydantic import BaseModel, Field


class ArticleCreate(BaseModel):
    title: str
    description: str
    body: str
    tag_list: list[str] = Field(default_factory=list, alias="tagList")

    class Config:
        populate_by_name = True


class ArticleUpdate(BaseModel):
    title: Optional[str] = None
    description: Optional[str] = None
    body: Optional[str] = None


class ProfileData(BaseModel):
    username: str
    bio: Optional[str] = None
    image: Optional[str] = None
    following: bool = False


class ArticleData(BaseModel):
    slug: str
    title: str
    description: str
    body: str
    tag_list: list[str] = Field(default_factory=list, alias="tagList")
    created_at: datetime = Field(alias="createdAt")
    updated_at: datetime = Field(alias="updatedAt")
    favorited: bool = False
    favorites_count: int = Field(default=0, alias="favoritesCount")
    author: ProfileData

    class Config:
        populate_by_name = True


class ArticleResponse(BaseModel):
    article: ArticleData


class ArticlesResponse(BaseModel):
    articles: list[ArticleData]
    articles_count: int = Field(alias="articlesCount")

    class Config:
        populate_by_name = True
