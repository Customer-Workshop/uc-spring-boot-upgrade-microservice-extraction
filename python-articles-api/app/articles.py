from __future__ import annotations

from typing import Optional

from fastapi import APIRouter, Depends, Response
from sqlalchemy.orm import Session

from . import schemas, service
from .database import get_db
from .domain import is_empty, new_id, now, to_slug
from .errors import (
    InvalidRequestException,
    NoAuthorizationException,
    ResourceNotFoundException,
    UnauthorizedException,
)
from .models import Article, Tag, User, article_tags
from .security import get_current_user_optional

router = APIRouter(prefix="/api/articles", tags=["articles"])


@router.get("", response_model=schemas.MultipleArticlesResponse)
def get_articles(
    offset: int = 0,
    limit: int = 20,
    tag: Optional[str] = None,
    favorited: Optional[str] = None,
    author: Optional[str] = None,
    db: Session = Depends(get_db),
    user: Optional[User] = Depends(get_current_user_optional),
):
    return service.find_recent_articles(db, tag, author, favorited, offset, limit, user)


@router.get("/feed", response_model=schemas.MultipleArticlesResponse)
def get_feed(
    offset: int = 0,
    limit: int = 20,
    db: Session = Depends(get_db),
    user: Optional[User] = Depends(get_current_user_optional),
):
    if user is None:
        raise UnauthorizedException()
    return service.find_user_feed(db, user, offset, limit)


@router.get("/{slug}", response_model=schemas.SingleArticleResponse)
def get_article(
    slug: str,
    db: Session = Depends(get_db),
    user: Optional[User] = Depends(get_current_user_optional),
):
    article = service.find_by_slug(db, slug)
    if article is None:
        raise ResourceNotFoundException()
    return schemas.SingleArticleResponse(
        article=service.build_article_data(db, article, user)
    )


@router.post("", response_model=schemas.SingleArticleResponse)
def create_article(
    payload: schemas.NewArticleRequest,
    db: Session = Depends(get_db),
    user: Optional[User] = Depends(get_current_user_optional),
):
    if user is None:
        raise UnauthorizedException()
    param = payload.article

    errors: dict[str, list[str]] = {}
    if is_empty(param.title):
        errors["title"] = ["can't be empty"]
    elif service.find_by_slug(db, to_slug(param.title)) is not None:
        errors["title"] = ["article name exists"]
    if is_empty(param.description):
        errors["description"] = ["can't be empty"]
    if is_empty(param.body):
        errors["body"] = ["can't be empty"]
    if errors:
        raise InvalidRequestException(errors)

    created = now()
    article = Article(
        id=new_id(),
        slug=to_slug(param.title),
        title=param.title,
        description=param.description,
        body=param.body,
        user_id=user.id,
        created_at=created,
        updated_at=created,
    )
    db.add(article)
    db.flush()
    _attach_tags(db, article, param.tagList or [])
    db.commit()

    article = service.find_by_id(db, article.id)
    return schemas.SingleArticleResponse(
        article=service.build_article_data(db, article, user)
    )


@router.put("/{slug}", response_model=schemas.SingleArticleResponse)
def update_article(
    slug: str,
    payload: schemas.UpdateArticleRequest,
    db: Session = Depends(get_db),
    user: Optional[User] = Depends(get_current_user_optional),
):
    if user is None:
        raise UnauthorizedException()
    article = service.find_by_slug(db, slug)
    if article is None:
        raise ResourceNotFoundException()
    if user.id != article.user_id:
        raise NoAuthorizationException()

    param = payload.article
    # Mirrors Article.update: only non-empty fields change. Note the Java
    # ArticleMapper.update never writes updated_at, so it is left untouched.
    if not is_empty(param.title):
        article.title = param.title
        article.slug = to_slug(param.title)
    if not is_empty(param.description):
        article.description = param.description
    if not is_empty(param.body):
        article.body = param.body
    db.commit()

    article = service.find_by_slug(db, article.slug)
    return schemas.SingleArticleResponse(
        article=service.build_article_data(db, article, user)
    )


@router.delete("/{slug}", status_code=204)
def delete_article(
    slug: str,
    db: Session = Depends(get_db),
    user: Optional[User] = Depends(get_current_user_optional),
):
    if user is None:
        raise UnauthorizedException()
    article = service.find_by_slug(db, slug)
    if article is None:
        raise ResourceNotFoundException()
    if user.id != article.user_id:
        raise NoAuthorizationException()
    db.delete(article)
    db.commit()
    return Response(status_code=204)


def _attach_tags(db: Session, article: Article, tag_names: list[str]) -> None:
    # Mirrors MyBatisArticleRepository.createNew: reuse existing tags by name,
    # otherwise insert a new tag with a generated id. De-duplicate names first
    # (Java wraps the list in a HashSet).
    seen: set[str] = set()
    for name in tag_names:
        if name in seen:
            continue
        seen.add(name)
        tag = db.query(Tag).filter(Tag.name == name).first()
        if tag is None:
            tag = Tag(id=new_id(), name=name)
            db.add(tag)
            db.flush()
        db.execute(
            article_tags.insert().values(article_id=article.id, tag_id=tag.id)
        )
