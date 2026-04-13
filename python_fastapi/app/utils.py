"""Utility functions mirroring helpers from the Java codebase."""

import re


def to_slug(title: str) -> str:
    """Convert a title to a URL-friendly slug.

    Mirrors ``Article.toSlug()`` in the Java code which uses the regex:
      ``[\\&|[\\uFE30-\\uFFA0]|\\'|\\"|\\s\\?\\,\\.]+``
    to replace matched characters with hyphens.
    """
    pattern = r"[&\uFE30-\uFFA0'\"\s?,.]+"
    return re.sub(pattern, "-", title.lower()).strip("-")


def is_empty(value: str | None) -> bool:
    """Mirrors ``io.spring.Util.isEmpty``."""
    return value is None or value == ""
