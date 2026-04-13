"""Article REST endpoints mirroring the Java ArticlesApi + ArticleApi controllers.

Endpoint mapping
----------------
Java Controller         | Method | Path                    | Python function
------------------------|--------|-------------------------|----------------
ArticlesApi.getArticles | GET    | /api/articles           | get_articles
ArticlesApi.getFeed     | GET    | /api/articles/feed      | get_feed
ArticlesApi.createArticle| POST  | /api/articles           | create_article
ArticleApi.article      | GET    | /api/articles/{slug}    | get_article
ArticleApi.updateArticle| PUT   | /api/articles/{slug}    | update_article
ArticleApi.deleteArticle| DELETE | /api/articles/{slug}    | delete_article
"""

from datetime import datetime, timezone
from typing import Optional

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session

from app.auth import get_current_user_optional, get_current_user_required
from app.database import get_db
from app.models import Article, Tag, User
from app.schemas import (
    ArticleData,
    MultipleArticlesResponse,
    NewArticleRequestWrapper,
    ProfileData,
    SingleArticleResponse,
    UpdateArticleRequestWrapper,
)
from app.utils import is_empty, to_slug

router = APIRouter(prefix="/api/articles", tags=["Articles"])

# ---- Maximum page limit (mirrors Java Page.MAX_LIMIT = 100) ----
MAX_LIMIT = 100


def _clamp_limit(limit: int) -> int:
    if limit > MAX_LIMIT:
        return MAX_LIMIT
    if limit > 0:
        return limit
    return 20


def _clamp_offset(offset: int) -> int:
    return offset if offset > 0 else 0


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------


def _build_article_data(
    article: Article,
    current_user: Optional[User],
) -> ArticleData:
    """Build an ``ArticleData`` response matching the Java JSON shape.

    Populates ``favorited``, ``favoritesCount``, and ``author.following``
    relative to *current_user* (may be ``None`` for anonymous requests).
    """
    favorited = False
    following = False
    favorites_count = len(article.favorited_by)

    if current_user is not None:
        favorited = current_user in article.favorited_by
        following = current_user in article.author.followers

    tag_names = sorted([t.name for t in article.tags])

    return ArticleData(
        id=article.id,
        slug=article.slug,
        title=article.title,
        description=article.description,
        body=article.body,
        favorited=favorited,
        favoritesCount=favorites_count,
        createdAt=article.created_at,
        updatedAt=article.updated_at,
        tagList=tag_names,
        author=ProfileData(
            username=article.author.username,
            bio=article.author.bio,
            image=article.author.image,
            following=following,
        ),
    )


def _get_or_create_tag(db: Session, tag_name: str) -> Tag:
    """Return an existing Tag or create a new one."""
    tag = db.query(Tag).filter(Tag.name == tag_name).first()
    if tag is None:
        tag = Tag(name=tag_name)
        db.add(tag)
        db.flush()
    return tag


# ---------------------------------------------------------------------------
# Endpoints
# ---------------------------------------------------------------------------


@router.get("/feed", response_model=MultipleArticlesResponse)
def get_feed(
    offset: int = Query(0),
    limit: int = Query(20),
    current_user: User = Depends(get_current_user_required),
    db: Session = Depends(get_db),
):
    """GET /api/articles/feed - Articles from followed authors.

    Mirrors ``ArticlesApi.getFeed``.
    """
    limit = _clamp_limit(limit)
    offset = _clamp_offset(offset)

    followed_ids = [u.id for u in current_user.following]
    if not followed_ids:
        return MultipleArticlesResponse(articles=[], articlesCount=0)

    query = (
        db.query(Article)
        .filter(Article.user_id.in_(followed_ids))
        .order_by(Article.created_at.desc())
    )
    total = query.count()
    articles = query.offset(offset).limit(limit).all()

    return MultipleArticlesResponse(
        articles=[_build_article_data(a, current_user) for a in articles],
        articlesCount=total,
    )


@router.get("", response_model=MultipleArticlesResponse)
def get_articles(
    offset: int = Query(0),
    limit: int = Query(20),
    tag: Optional[str] = Query(None),
    favorited: Optional[str] = Query(None),
    author: Optional[str] = Query(None),
    current_user: Optional[User] = Depends(get_current_user_optional),
    db: Session = Depends(get_db),
):
    """GET /api/articles - List / filter articles.

    Mirrors ``ArticlesApi.getArticles``.
    """
    limit = _clamp_limit(limit)
    offset = _clamp_offset(offset)

    query = db.query(Article)

    if tag:
        query = query.filter(Article.tags.any(Tag.name == tag))
    if author:
        query = query.filter(Article.author.has(User.username == author))
    if favorited:
        query = query.filter(
            Article.favorited_by.any(User.username == favorited)
        )

    query = query.order_by(Article.created_at.desc())
    total = query.count()
    articles = query.offset(offset).limit(limit).all()

    return MultipleArticlesResponse(
        articles=[_build_article_data(a, current_user) for a in articles],
        articlesCount=total,
    )


@router.post("", response_model=SingleArticleResponse, status_code=200)
def create_article(
    payload: NewArticleRequestWrapper,
    current_user: User = Depends(get_current_user_required),
    db: Session = Depends(get_db),
):
    """POST /api/articles - Create a new article.

    Mirrors ``ArticlesApi.createArticle``.
    Validates that title, description, and body are not blank.
    Returns 422 for validation errors (matching Java @Valid behaviour).
    """
    params = payload.article

    # --- Validation (mirrors Java @NotBlank + DuplicatedArticleConstraint) ---
    errors: dict[str, list[str]] = {}
    if not params.title or not params.title.strip():
        errors.setdefault("title", []).append("can't be empty")
    if not params.description or not params.description.strip():
        errors.setdefault("description", []).append("can't be empty")
    if not params.body or not params.body.strip():
        errors.setdefault("body", []).append("can't be empty")

    if params.title and params.title.strip():
        slug = to_slug(params.title)
        existing = db.query(Article).filter(Article.slug == slug).first()
        if existing is not None:
            errors.setdefault("title", []).append("article already exists")

    if errors:
        raise HTTPException(status_code=422, detail={"errors": errors})

    slug = to_slug(params.title)
    now = datetime.now(timezone.utc)

    tag_objects = []
    if params.tagList:
        for t in set(params.tagList):
            tag_objects.append(_get_or_create_tag(db, t))

    article = Article(
        slug=slug,
        title=params.title,
        description=params.description,
        body=params.body,
        user_id=current_user.id,
        created_at=now,
        updated_at=now,
    )
    article.tags = tag_objects
    db.add(article)
    db.commit()
    db.refresh(article)

    return SingleArticleResponse(
        article=_build_article_data(article, current_user)
    )


@router.get("/{slug}", response_model=SingleArticleResponse)
def get_article(
    slug: str,
    current_user: Optional[User] = Depends(get_current_user_optional),
    db: Session = Depends(get_db),
):
    """GET /api/articles/{slug} - Fetch a single article.

    Mirrors ``ArticleApi.article``.
    Returns 404 if not found (matching Java ResourceNotFoundException).
    """
    article = db.query(Article).filter(Article.slug == slug).first()
    if article is None:
        raise HTTPException(status_code=404, detail="Article not found")

    return SingleArticleResponse(
        article=_build_article_data(article, current_user)
    )


@router.put("/{slug}", response_model=SingleArticleResponse)
def update_article(
    slug: str,
    payload: UpdateArticleRequestWrapper,
    current_user: User = Depends(get_current_user_required),
    db: Session = Depends(get_db),
):
    """PUT /api/articles/{slug} - Update an existing article.

    Mirrors ``ArticleApi.updateArticle``.
    Returns 404 if not found, 403 if the current user is not the author.
    """
    article = db.query(Article).filter(Article.slug == slug).first()
    if article is None:
        raise HTTPException(status_code=404, detail="Article not found")

    # Authorization check mirrors AuthorizationService.canWriteArticle
    if article.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized")

    params = payload.article

    if not is_empty(params.title):
        article.title = params.title
        article.slug = to_slug(params.title)
        article.updated_at = datetime.now(timezone.utc)
    if not is_empty(params.description):
        article.description = params.description
        article.updated_at = datetime.now(timezone.utc)
    if not is_empty(params.body):
        article.body = params.body
        article.updated_at = datetime.now(timezone.utc)

    db.commit()
    db.refresh(article)

    return SingleArticleResponse(
        article=_build_article_data(article, current_user)
    )


@router.delete("/{slug}", status_code=204)
def delete_article(
    slug: str,
    current_user: User = Depends(get_current_user_required),
    db: Session = Depends(get_db),
):
    """DELETE /api/articles/{slug} - Remove an article.

    Mirrors ``ArticleApi.deleteArticle``.
    Returns 404 if not found, 403 if the current user is not the author.
    """
    article = db.query(Article).filter(Article.slug == slug).first()
    if article is None:
        raise HTTPException(status_code=404, detail="Article not found")

    if article.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized")

    db.delete(article)
    db.commit()
    return None
