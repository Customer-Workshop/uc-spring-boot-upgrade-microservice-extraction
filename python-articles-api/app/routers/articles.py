from typing import Optional

from fastapi import APIRouter, Depends, Query, Request, Response
from sqlalchemy.orm import Session

from .. import repository as repo
from .. import service
from ..database import get_db
from ..errors import InvalidRequest, NoAuthorization, ResourceNotFound, Unauthorized
from ..models import User
from ..schemas import (
    MultipleArticlesResponse,
    NewArticleRequest,
    SingleArticleResponse,
    UpdateArticleRequest,
)
from ..security import optional_current_user
from ..slug import to_slug

router = APIRouter(prefix="/api/articles", tags=["articles"])


def _single(article: dict) -> SingleArticleResponse:
    return SingleArticleResponse(article=service.to_article_out(article))


def _is_blank(value: Optional[str]) -> bool:
    return value is None or value.strip() == ""


@router.get("", response_model=MultipleArticlesResponse)
def get_articles(
    offset: int = Query(0),
    limit: int = Query(20),
    tag: Optional[str] = Query(None),
    favorited: Optional[str] = Query(None),
    author: Optional[str] = Query(None),
    db: Session = Depends(get_db),
    user: Optional[User] = Depends(optional_current_user),
):
    articles, count = service.find_recent_articles(
        db, tag, author, favorited, offset, limit, user
    )
    return MultipleArticlesResponse(
        articles=[service.to_article_out(a) for a in articles], articlesCount=count
    )


@router.get("/feed", response_model=MultipleArticlesResponse)
def get_feed(
    offset: int = Query(0),
    limit: int = Query(20),
    db: Session = Depends(get_db),
    user: Optional[User] = Depends(optional_current_user),
):
    if user is None:
        raise Unauthorized()
    articles, count = service.find_user_feed(db, user, offset, limit)
    return MultipleArticlesResponse(
        articles=[service.to_article_out(a) for a in articles], articlesCount=count
    )


@router.get("/{slug}", response_model=SingleArticleResponse)
def get_article(
    slug: str,
    db: Session = Depends(get_db),
    user: Optional[User] = Depends(optional_current_user),
):
    article = service.find_by_slug(db, slug, user)
    if article is None:
        raise ResourceNotFound()
    return _single(article)


@router.post("", response_model=SingleArticleResponse)
def create_article(
    payload: NewArticleRequest,
    db: Session = Depends(get_db),
    user: Optional[User] = Depends(optional_current_user),
):
    if user is None:
        raise Unauthorized()
    param = payload.article
    errors: dict = {}
    if _is_blank(param.title):
        errors["title"] = ["can't be empty"]
    elif repo.slug_exists(db, to_slug(param.title)):
        errors["title"] = ["article name exists"]
    if _is_blank(param.description):
        errors["description"] = ["can't be empty"]
    if _is_blank(param.body):
        errors["body"] = ["can't be empty"]
    if errors:
        raise InvalidRequest(errors)

    article_id = repo.create_article(
        db, param.title, param.description, param.body, param.tagList, user.id
    )
    article = service.find_by_id(db, article_id, user)
    return _single(article)


@router.put("/{slug}", response_model=SingleArticleResponse)
def update_article(
    slug: str,
    payload: UpdateArticleRequest,
    db: Session = Depends(get_db),
    user: Optional[User] = Depends(optional_current_user),
):
    if user is None:
        raise Unauthorized()
    entity = repo.find_article_entity(db, slug)
    if entity is None:
        raise ResourceNotFound()
    if entity.user_id != user.id:
        raise NoAuthorization()
    param = payload.article
    updated = repo.update_article(db, entity, param.title, param.description, param.body)
    article = service.find_by_slug(db, updated.slug, user)
    return _single(article)


@router.delete("/{slug}", status_code=204)
def delete_article(
    slug: str,
    db: Session = Depends(get_db),
    user: Optional[User] = Depends(optional_current_user),
):
    if user is None:
        raise Unauthorized()
    entity = repo.find_article_entity(db, slug)
    if entity is None:
        raise ResourceNotFound()
    if entity.user_id != user.id:
        raise NoAuthorization()
    repo.delete_article(db, entity)
    return Response(status_code=204)
