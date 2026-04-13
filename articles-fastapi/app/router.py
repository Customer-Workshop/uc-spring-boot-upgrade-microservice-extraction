from datetime import datetime
from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, Query
from slugify import slugify
from sqlalchemy.orm import Session

from .database import get_db, Article, Tag, User
from .models import (
    ArticleCreate,
    ArticleUpdate,
    ArticleData,
    ArticleResponse,
    ArticlesResponse,
    ProfileData,
)

router = APIRouter(prefix="/articles", tags=["Articles"])


def _to_article_data(article: Article, current_user: Optional[User] = None) -> ArticleData:
    author = article.author
    return ArticleData(
        slug=article.slug,
        title=article.title,
        description=article.description or "",
        body=article.body or "",
        tagList=[tag.name for tag in article.tags],
        createdAt=article.created_at,
        updatedAt=article.updated_at,
        favorited=current_user in article.favorited_by if current_user else False,
        favoritesCount=len(article.favorited_by),
        author=ProfileData(
            username=author.username,
            bio=author.bio,
            image=author.image,
            following=False,
        ),
    )


def _get_or_create_tags(db: Session, tag_names: list[str]) -> list[Tag]:
    tags = []
    for name in tag_names:
        tag = db.query(Tag).filter(Tag.name == name).first()
        if not tag:
            tag = Tag(name=name)
            db.add(tag)
        tags.append(tag)
    db.flush()
    return tags


@router.post("", response_model=ArticleResponse, status_code=201)
def create_article(
    payload: dict,
    db: Session = Depends(get_db),
):
    article_data = payload.get("article", payload)
    param = ArticleCreate(**article_data)
    slug = slugify(param.title)

    existing = db.query(Article).filter(Article.slug == slug).first()
    if existing:
        raise HTTPException(status_code=422, detail="Article with this title already exists")

    # Use first user as author (simplified for microservice demo)
    user = db.query(User).first()
    if not user:
        raise HTTPException(status_code=401, detail="No authenticated user")

    tags = _get_or_create_tags(db, param.tag_list)

    article = Article(
        slug=slug,
        title=param.title,
        description=param.description,
        body=param.body,
        user_id=user.id,
    )
    article.tags = tags
    db.add(article)
    db.commit()
    db.refresh(article)

    return ArticleResponse(article=_to_article_data(article))


@router.get("", response_model=ArticlesResponse)
def list_articles(
    tag: Optional[str] = Query(None),
    author: Optional[str] = Query(None),
    favorited: Optional[str] = Query(None),
    offset: int = Query(0, ge=0),
    limit: int = Query(20, ge=1, le=100),
    db: Session = Depends(get_db),
):
    query = db.query(Article)

    if tag:
        query = query.filter(Article.tags.any(Tag.name == tag))
    if author:
        query = query.join(Article.author).filter(User.username == author)
    if favorited:
        query = query.filter(
            Article.favorited_by.any(User.username == favorited)
        )

    total = query.count()
    articles = query.order_by(Article.created_at.desc()).offset(offset).limit(limit).all()

    return ArticlesResponse(
        articles=[_to_article_data(a) for a in articles],
        articlesCount=total,
    )


@router.get("/{slug}", response_model=ArticleResponse)
def get_article(slug: str, db: Session = Depends(get_db)):
    article = db.query(Article).filter(Article.slug == slug).first()
    if not article:
        raise HTTPException(status_code=404, detail="Article not found")
    return ArticleResponse(article=_to_article_data(article))


@router.put("/{slug}", response_model=ArticleResponse)
def update_article(
    slug: str,
    payload: dict,
    db: Session = Depends(get_db),
):
    article = db.query(Article).filter(Article.slug == slug).first()
    if not article:
        raise HTTPException(status_code=404, detail="Article not found")

    article_data = payload.get("article", payload)
    update = ArticleUpdate(**article_data)

    if update.title is not None:
        article.title = update.title
        article.slug = slugify(update.title)
    if update.description is not None:
        article.description = update.description
    if update.body is not None:
        article.body = update.body

    article.updated_at = datetime.utcnow()
    db.commit()
    db.refresh(article)

    return ArticleResponse(article=_to_article_data(article))


@router.delete("/{slug}", status_code=204)
def delete_article(slug: str, db: Session = Depends(get_db)):
    article = db.query(Article).filter(Article.slug == slug).first()
    if not article:
        raise HTTPException(status_code=404, detail="Article not found")
    db.delete(article)
    db.commit()
