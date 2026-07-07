from typing import List, Optional

from pydantic import BaseModel


class AuthorOut(BaseModel):
    username: Optional[str] = None
    bio: Optional[str] = None
    image: Optional[str] = None
    following: bool = False


class CursorOut(BaseModel):
    data: str


class ArticleOut(BaseModel):
    id: str
    slug: str
    title: str
    description: Optional[str] = None
    body: Optional[str] = None
    favorited: bool = False
    favoritesCount: int = 0
    createdAt: str
    updatedAt: str
    tagList: List[str]
    cursor: CursorOut
    author: AuthorOut


class SingleArticleResponse(BaseModel):
    article: ArticleOut


class MultipleArticlesResponse(BaseModel):
    articles: List[ArticleOut]
    articlesCount: int


# Request payloads. The Spring app enables Jackson UNWRAP_ROOT_VALUE, so bodies
# are wrapped in an "article" root, e.g. {"article": {"title": ...}}.
class NewArticleParam(BaseModel):
    title: Optional[str] = None
    description: Optional[str] = None
    body: Optional[str] = None
    tagList: Optional[List[str]] = None


class NewArticleRequest(BaseModel):
    article: NewArticleParam = NewArticleParam()


class UpdateArticleParam(BaseModel):
    # Defaults mirror UpdateArticleParam in Java: empty strings, not null.
    title: str = ""
    body: str = ""
    description: str = ""


class UpdateArticleRequest(BaseModel):
    article: UpdateArticleParam = UpdateArticleParam()
