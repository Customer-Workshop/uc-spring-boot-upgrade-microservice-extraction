"""Article REST endpoints matching the Java ArticlesApi + ArticleApi controllers.

Endpoints:
  GET    /api/articles           — list articles (filter by tag/author/favorited, offset/limit)
  GET    /api/articles/feed      — feed of followed authors' articles
  POST   /api/articles           — create article
  GET    /api/articles/{slug}    — get single article
  PUT    /api/articles/{slug}    — update article
  DELETE /api/articles/{slug}    — delete article
"""

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy import func
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import Article, Tag, User, article_favorites, article_tags, user_follows
from app.schemas import (
    ArticleData,
    ArticleDataList,
    NewArticleWrapper,
    ProfileData,
    SingleArticleResponse,
    UpdateArticleWrapper,
)

router = APIRouter(prefix="/api/articles", tags=["Articles"])


# ---- helpers ----


def _get_current_user(db: Session) -> User | None:
    """Simplified auth stub: returns the first user in the DB or None.

    In the real Java app this is handled by Spring Security + JWT.
    For the Python translation we keep authentication pluggable —
    see MIGRATION_NOTES.md for details.
    """
    return db.query(User).first()


def _article_to_data(article: Article, current_user: User | None, db: Session) -> ArticleData:
    """Convert an ORM Article into the Pydantic ArticleData that matches
    the Java ArticleData JSON shape."""
    tag_names = sorted([t.name for t in article.tags])

    # favourites count
    fav_count = (
        db.query(func.count())
        .select_from(article_favorites)
        .filter(article_favorites.c.article_id == article.id)
        .scalar()
    ) or 0

    # is current user favouriting?
    favorited = False
    is_following = False
    if current_user is not None:
        favorited = (
            db.query(func.count())
            .select_from(article_favorites)
            .filter(
                article_favorites.c.article_id == article.id,
                article_favorites.c.user_id == current_user.id,
            )
            .scalar()
            or 0
        ) > 0

        is_following = (
            db.query(func.count())
            .select_from(user_follows)
            .filter(
                user_follows.c.follower_id == current_user.id,
                user_follows.c.followed_id == article.user_id,
            )
            .scalar()
            or 0
        ) > 0

    author = article.author
    profile = ProfileData(
        username=author.username,
        bio=author.bio,
        image=author.image,
        following=is_following,
    )

    return ArticleData.model_validate(
        {
            "id": article.id,
            "slug": article.slug,
            "title": article.title,
            "description": article.description,
            "body": article.body,
            "favorited": favorited,
            "favoritesCount": fav_count,
            "createdAt": article.created_at,
            "updatedAt": article.updated_at,
            "tagList": tag_names,
            "author": profile,
        }
    )


# ---- endpoints ----


@router.get("", response_model=None)
def list_articles(
    tag: str | None = Query(None),
    author: str | None = Query(None),
    favorited: str | None = Query(None),
    offset: int = Query(0, ge=0),
    limit: int = Query(20, ge=1, le=100),
    db: Session = Depends(get_db),
):
    """GET /api/articles — mirrors Java ArticlesApi.getArticles()."""
    current_user = _get_current_user(db)
    query = db.query(Article)

    if tag:
        query = query.filter(Article.tags.any(Tag.name == tag))
    if author:
        query = query.join(Article.author).filter(User.username == author)
    if favorited:
        fav_user = db.query(User).filter(User.username == favorited).first()
        if fav_user:
            query = query.filter(
                Article.id.in_(
                    db.query(article_favorites.c.article_id).filter(
                        article_favorites.c.user_id == fav_user.id
                    )
                )
            )
        else:
            # no such user → empty result set
            return {"articles": [], "articlesCount": 0}

    total = query.count()
    articles = query.order_by(Article.created_at.desc()).offset(offset).limit(limit).all()

    return {
        "articles": [
            _article_to_data(a, current_user, db).model_dump(by_alias=True) for a in articles
        ],
        "articlesCount": total,
    }


@router.get("/feed", response_model=None)
def get_feed(
    offset: int = Query(0, ge=0),
    limit: int = Query(20, ge=1, le=100),
    db: Session = Depends(get_db),
):
    """GET /api/articles/feed — mirrors Java ArticlesApi.getFeed()."""
    current_user = _get_current_user(db)
    if current_user is None:
        raise HTTPException(status_code=401, detail="Login required")

    followed_ids = [u.id for u in current_user.following]
    if not followed_ids:
        return {"articles": [], "articlesCount": 0}

    query = db.query(Article).filter(Article.user_id.in_(followed_ids))
    total = query.count()
    articles = query.order_by(Article.created_at.desc()).offset(offset).limit(limit).all()

    return {
        "articles": [
            _article_to_data(a, current_user, db).model_dump(by_alias=True) for a in articles
        ],
        "articlesCount": total,
    }


@router.post("", response_model=None, status_code=200)
def create_article(
    payload: NewArticleWrapper,
    db: Session = Depends(get_db),
):
    """POST /api/articles — mirrors Java ArticlesApi.createArticle()."""
    current_user = _get_current_user(db)
    if current_user is None:
        raise HTTPException(status_code=401, detail="Login required")

    data = payload.article
    article = Article.create(
        title=data.title,
        description=data.description,
        body=data.body,
        tag_list=data.tag_list,
        user_id=current_user.id,
        db=db,
    )
    db.add(article)
    db.commit()
    db.refresh(article)

    return {"article": _article_to_data(article, current_user, db).model_dump(by_alias=True)}


@router.get("/{slug}", response_model=None)
def get_article(slug: str, db: Session = Depends(get_db)):
    """GET /api/articles/{slug} — mirrors Java ArticleApi.article()."""
    current_user = _get_current_user(db)
    article = db.query(Article).filter(Article.slug == slug).first()
    if article is None:
        raise HTTPException(status_code=404, detail="Article not found")
    return {"article": _article_to_data(article, current_user, db).model_dump(by_alias=True)}


@router.put("/{slug}", response_model=None)
def update_article(
    slug: str,
    payload: UpdateArticleWrapper,
    db: Session = Depends(get_db),
):
    """PUT /api/articles/{slug} — mirrors Java ArticleApi.updateArticle()."""
    current_user = _get_current_user(db)
    article = db.query(Article).filter(Article.slug == slug).first()
    if article is None:
        raise HTTPException(status_code=404, detail="Article not found")
    if current_user is None or article.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized")

    data = payload.article
    article.update(data.title or None, data.description or None, data.body or None)
    db.commit()
    db.refresh(article)

    return {"article": _article_to_data(article, current_user, db).model_dump(by_alias=True)}


@router.delete("/{slug}", status_code=204)
def delete_article(slug: str, db: Session = Depends(get_db)):
    """DELETE /api/articles/{slug} — mirrors Java ArticleApi.deleteArticle()."""
    current_user = _get_current_user(db)
    article = db.query(Article).filter(Article.slug == slug).first()
    if article is None:
        raise HTTPException(status_code=404, detail="Article not found")
    if current_user is None or article.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized")

    db.delete(article)
    db.commit()
    return None
