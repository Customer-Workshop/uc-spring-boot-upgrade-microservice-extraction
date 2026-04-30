from typing import Optional

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.models import Article
from app.models.models import User as UserModel
from app.schemas.article import (
    MultipleArticlesResponse,
    NewArticleWrapper,
    SingleArticleResponse,
    UpdateArticleWrapper,
)
from app.services.article_service import (
    create_article,
    delete_article,
    get_article_by_slug,
    get_feed,
    list_articles,
    update_article,
)
from app.services.auth import get_current_user_optional, get_current_user_required

router = APIRouter(prefix="/api/articles", tags=["Articles"])


@router.get("", response_model=MultipleArticlesResponse)
def get_articles(
    tag: Optional[str] = Query(None),
    author: Optional[str] = Query(None),
    favorited: Optional[str] = Query(None),
    offset: int = Query(0),
    limit: int = Query(20),
    db: Session = Depends(get_db),
    current_user: Optional[UserModel] = Depends(get_current_user_optional),
):
    articles, total = list_articles(
        db, current_user, tag=tag, author=author, favorited=favorited, offset=offset, limit=limit
    )
    return MultipleArticlesResponse(articles=articles, articlesCount=total)


@router.get("/feed", response_model=MultipleArticlesResponse)
def get_article_feed(
    offset: int = Query(0),
    limit: int = Query(20),
    db: Session = Depends(get_db),
    current_user: UserModel = Depends(get_current_user_required),
):
    articles, total = get_feed(db, current_user, offset=offset, limit=limit)
    return MultipleArticlesResponse(articles=articles, articlesCount=total)


@router.get("/{slug}", response_model=SingleArticleResponse)
def get_single_article(
    slug: str,
    db: Session = Depends(get_db),
    current_user: Optional[UserModel] = Depends(get_current_user_optional),
):
    article_data = get_article_by_slug(slug, current_user, db)
    if article_data is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Article not found")
    return SingleArticleResponse(article=article_data)


@router.post("", response_model=SingleArticleResponse, status_code=status.HTTP_200_OK)
def create_new_article(
    payload: NewArticleWrapper,
    db: Session = Depends(get_db),
    current_user: UserModel = Depends(get_current_user_required),
):
    try:
        article_data = create_article(
            db,
            current_user,
            title=payload.article.title,
            description=payload.article.description,
            body=payload.article.body,
            tag_list=payload.article.tagList,
        )
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail=str(e))
    return SingleArticleResponse(article=article_data)


@router.put("/{slug}", response_model=SingleArticleResponse)
def update_existing_article(
    slug: str,
    payload: UpdateArticleWrapper,
    db: Session = Depends(get_db),
    current_user: UserModel = Depends(get_current_user_required),
):
    article = db.query(Article).filter(Article.slug == slug).first()
    if article is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Article not found")
    if article.user_id != current_user.id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Not authorized")
    article_data = update_article(
        db,
        article,
        current_user,
        title=payload.article.title,
        description=payload.article.description,
        body=payload.article.body,
    )
    return SingleArticleResponse(article=article_data)


@router.delete("/{slug}", status_code=status.HTTP_204_NO_CONTENT)
def delete_existing_article(
    slug: str,
    db: Session = Depends(get_db),
    current_user: UserModel = Depends(get_current_user_required),
):
    article = db.query(Article).filter(Article.slug == slug).first()
    if article is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Article not found")
    if article.user_id != current_user.id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Not authorized")
    delete_article(db, article)
