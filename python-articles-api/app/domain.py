import re
import uuid
from datetime import datetime, timezone

# Port of io.spring.core.article.Article.toSlug.
# Java: title.toLowerCase().replaceAll("[\\&|[\\uFE30-\\uFFA0]|\\’|\\”|\\s\\?\\,\\.]+", "-")
_SLUG_PATTERN = re.compile(r"[&|\uFE30-\uFFA0\u2019\u201D\s?,.]+")


def to_slug(title: str) -> str:
    return _SLUG_PATTERN.sub("-", title.lower())


def new_id() -> str:
    return str(uuid.uuid4())


def now() -> datetime:
    return datetime.now(timezone.utc)


def is_empty(value: str | None) -> bool:
    # Mirrors io.spring.Util.isEmpty
    return value is None or value.strip() == ""


def format_datetime(value: datetime) -> str:
    """Serialize like Jodaʼs default Jackson ISO output, e.g. 2026-07-15T06:53:44.000Z."""
    if value.tzinfo is not None:
        value = value.astimezone(timezone.utc).replace(tzinfo=None)
    millis = value.microsecond // 1000
    return value.strftime("%Y-%m-%dT%H:%M:%S") + f".{millis:03d}Z"
