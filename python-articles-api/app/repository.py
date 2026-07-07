"""Data access mirroring the MyBatis mappers of the Java service.

Read queries are expressed as raw SQL that reproduces the exact SQL emitted by
the original MyBatis XML mappers (including a couple of quirks — see
MIGRATION_NOTES.md). This guarantees byte-for-byte identical result ordering
against the same SQLite database. Writes go through the SQLAlchemy ORM.
"""

import uuid
from datetime import datetime, timezone
from typing import Dict, List, Optional, Set

from sqlalchemy import text
from sqlalchemy.orm import Session

from .models import Article, ArticleFavorite, ArticleTag, Tag
from .slug import to_slug

# Mirrors ArticleReadService.xml `selectArticleData`.
_SELECT_ARTICLE_DATA = """
select
  A.id articleId,
  A.slug articleSlug,
  A.title articleTitle,
  A.description articleDescription,
  A.body articleBody,
  A.created_at articleCreatedAt,
  A.updated_at articleUpdatedAt,
  T.name tagName,
  U.id userId,
  U.username userUsername,
  U.bio userBio,
  U.image userImage
from articles A
left join article_tags AT on A.id = AT.article_id
left join tags T on T.id = AT.tag_id
left join users U on U.id = A.user_id
"""


def _parse_dt(value) -> Optional[datetime]:
    if value is None:
        return None
    if isinstance(value, datetime):
        return value
    s = str(value).strip().replace("T", " ").rstrip("Z")
    for fmt in ("%Y-%m-%d %H:%M:%S.%f", "%Y-%m-%d %H:%M:%S"):
        try:
            return datetime.strptime(s, fmt)
        except ValueError:
            continue
    # Last resort: ISO parsing.
    return datetime.fromisoformat(s)


def _group_articles(rows) -> List[dict]:
    """Collapse the tag-joined rows into article dicts, preserving row order.

    Reproduces the MyBatis `articleData` resultMap: one entry per article id
    (first row wins for scalar/author fields), tag names collected per row.
    """
    order: List[str] = []
    by_id: Dict[str, dict] = {}
    for r in rows:
        m = r._mapping
        aid = m["articleId"]
        if aid not in by_id:
            order.append(aid)
            by_id[aid] = {
                "id": aid,
                "slug": m["articleSlug"],
                "title": m["articleTitle"],
                "description": m["articleDescription"],
                "body": m["articleBody"],
                "createdAt": _parse_dt(m["articleCreatedAt"]),
                "updatedAt": _parse_dt(m["articleUpdatedAt"]),
                "tagList": [],
                "author": {
                    "id": m["userId"],
                    "username": m["userUsername"],
                    "bio": m["userBio"],
                    "image": m["userImage"],
                    "following": False,
                },
                "favorited": False,
                "favoritesCount": 0,
            }
        if m["tagName"] is not None:
            by_id[aid]["tagList"].append(m["tagName"])
    return [by_id[aid] for aid in order]


def _in_clause(prefix: str, values: List[str]):
    keys = [f"{prefix}{i}" for i in range(len(values))]
    clause = "(" + ", ".join(f":{k}" for k in keys) + ")"
    params = {k: v for k, v in zip(keys, values)}
    return clause, params


def find_by_id(db: Session, article_id: str) -> Optional[dict]:
    rows = db.execute(
        text(_SELECT_ARTICLE_DATA + " where A.id = :id"), {"id": article_id}
    ).fetchall()
    articles = _group_articles(rows)
    return articles[0] if articles else None


def find_by_slug(db: Session, slug: str) -> Optional[dict]:
    rows = db.execute(
        text(_SELECT_ARTICLE_DATA + " where A.slug = :slug"), {"slug": slug}
    ).fetchall()
    articles = _group_articles(rows)
    return articles[0] if articles else None


def find_articles(db: Session, ids: List[str]) -> List[dict]:
    if not ids:
        return []
    clause, params = _in_clause("id", ids)
    rows = db.execute(
        text(
            _SELECT_ARTICLE_DATA
            + f" where A.id in {clause} order by A.created_at desc"
        ),
        params,
    ).fetchall()
    return _group_articles(rows)


def find_articles_of_authors(
    db: Session, authors: List[str], offset: int, limit: int
) -> List[dict]:
    if not authors:
        return []
    clause, params = _in_clause("a", authors)
    params.update({"offset": offset, "limit": limit})
    # NOTE: mirrors findArticlesOfAuthors — there is deliberately no ORDER BY and
    # the LIMIT is applied to the tag-joined rows, not to distinct articles.
    rows = db.execute(
        text(
            _SELECT_ARTICLE_DATA
            + f" where A.user_id in {clause} limit :offset, :limit"
        ),
        params,
    ).fetchall()
    return _group_articles(rows)


# Mirrors ArticleReadService.xml `selectArticleIds`.
_SELECT_ARTICLE_IDS_FROM = """
from articles A
left join article_tags AT on A.id = AT.article_id
left join tags T on T.id = AT.tag_id
left join article_favorites AF on AF.article_id = A.id
left join users AU on AU.id = A.user_id
left join users AFU on AFU.id = AF.user_id
"""


def _article_filter(tag, author, favorited_by):
    conditions = []
    params: Dict[str, str] = {}
    if tag is not None:
        conditions.append("T.name = :tag")
        params["tag"] = tag
    if author is not None:
        conditions.append("AU.username = :author")
        params["author"] = author
    if favorited_by is not None:
        conditions.append("AFU.username = :favoritedBy")
        params["favoritedBy"] = favorited_by
    where = (" where " + " AND ".join(conditions)) if conditions else ""
    return where, params


def query_article_ids(
    db: Session, tag, author, favorited_by, offset: int, limit: int
) -> List[str]:
    where, params = _article_filter(tag, author, favorited_by)
    params.update({"offset": offset, "limit": limit})
    sql = (
        "select DISTINCT(A.id) articleId, A.created_at "
        + _SELECT_ARTICLE_IDS_FROM
        + where
        + " order by A.created_at desc limit :offset, :limit"
    )
    rows = db.execute(text(sql), params).fetchall()
    return [r._mapping["articleId"] for r in rows]


def count_article(db: Session, tag, author, favorited_by) -> int:
    where, params = _article_filter(tag, author, favorited_by)
    sql = "select count(DISTINCT A.id) " + _SELECT_ARTICLE_IDS_FROM + where
    return int(db.execute(text(sql), params).scalar() or 0)


def count_feed_size(db: Session, authors: List[str]) -> int:
    if not authors:
        return 0
    clause, params = _in_clause("a", authors)
    sql = f"select count(1) from articles A where A.user_id in {clause}"
    return int(db.execute(text(sql), params).scalar() or 0)


# --- favorites ---
def articles_favorite_count(db: Session, ids: List[str]) -> Dict[str, int]:
    if not ids:
        return {}
    clause, params = _in_clause("id", ids)
    sql = (
        "select A.id, count(AF.user_id) as favoriteCount from articles A "
        "left join article_favorites AF on A.id = AF.article_id "
        f"where id in {clause} group by A.id"
    )
    rows = db.execute(text(sql), params).fetchall()
    return {r._mapping["id"]: int(r._mapping["favoriteCount"]) for r in rows}


def user_favorites(db: Session, ids: List[str], user_id: str) -> Set[str]:
    if not ids:
        return set()
    clause, params = _in_clause("id", ids)
    params["userId"] = user_id
    sql = (
        "select A.id from articles A "
        "left join article_favorites AF on A.id = AF.article_id "
        f"where id in {clause} and AF.user_id = :userId"
    )
    rows = db.execute(text(sql), params).fetchall()
    return {r._mapping["id"] for r in rows}


def is_user_favorite(db: Session, user_id: str, article_id: str) -> bool:
    sql = (
        "select count(1) from article_favorites "
        "where user_id = :userId and article_id = :articleId"
    )
    count = db.execute(
        text(sql), {"userId": user_id, "articleId": article_id}
    ).scalar()
    return bool(count)


def article_favorite_count(db: Session, article_id: str) -> int:
    sql = "select count(1) from article_favorites where article_id = :articleId"
    return int(db.execute(text(sql), {"articleId": article_id}).scalar() or 0)


# --- follows ---
def followed_users(db: Session, user_id: str) -> List[str]:
    sql = "select F.follow_id from follows F where F.user_id = :userId"
    rows = db.execute(text(sql), {"userId": user_id}).fetchall()
    return [r._mapping["follow_id"] for r in rows]


def following_authors(db: Session, user_id: str, ids: List[str]) -> Set[str]:
    if not ids:
        return set()
    clause, params = _in_clause("id", ids)
    params["userId"] = user_id
    sql = (
        f"select F.follow_id from follows F where F.follow_id in {clause} "
        "and F.user_id = :userId"
    )
    rows = db.execute(text(sql), params).fetchall()
    return {r._mapping["follow_id"] for r in rows}


def is_user_following(db: Session, user_id: str, other_user_id: str) -> bool:
    sql = (
        "select count(1) from follows "
        "where user_id = :userId and follow_id = :otherId"
    )
    count = db.execute(
        text(sql), {"userId": user_id, "otherId": other_user_id}
    ).scalar()
    return bool(count)


# --- writes (mirror MyBatisArticleRepository) ---
def find_article_entity(db: Session, slug: str) -> Optional[Article]:
    return db.query(Article).filter(Article.slug == slug).first()


def create_article(
    db: Session,
    title: str,
    description: Optional[str],
    body: Optional[str],
    tag_list: Optional[List[str]],
    user_id: str,
) -> str:
    now = datetime.now(timezone.utc).replace(tzinfo=None)
    article_id = str(uuid.uuid4())
    # Article() constructor dedups tags via a HashSet (iteration order undefined).
    for name in set(tag_list or []):
        tag = db.query(Tag).filter(Tag.name == name).first()
        if tag is None:
            tag = Tag(id=str(uuid.uuid4()), name=name)
            db.add(tag)
            db.flush()
        db.add(ArticleTag(article_id=article_id, tag_id=tag.id))
    db.add(
        Article(
            id=article_id,
            user_id=user_id,
            slug=to_slug(title),
            title=title,
            description=description,
            body=body,
            created_at=now,
            updated_at=now,
        )
    )
    db.commit()
    return article_id


def update_article(
    db: Session, article: Article, title: str, description: str, body: str
) -> Article:
    # Mirror Article#update: only non-empty fields are applied.
    if title:
        article.title = title
        article.slug = to_slug(title)
        article.updated_at = datetime.now(timezone.utc).replace(tzinfo=None)
    if description:
        article.description = description
        article.updated_at = datetime.now(timezone.utc).replace(tzinfo=None)
    if body:
        article.body = body
        article.updated_at = datetime.now(timezone.utc).replace(tzinfo=None)
    db.commit()
    return article


def delete_article(db: Session, article: Article) -> None:
    # Mirror ArticleMapper.delete — only the articles row is removed.
    db.delete(article)
    db.commit()


def slug_exists(db: Session, slug: str) -> bool:
    return find_by_slug(db, slug) is not None
