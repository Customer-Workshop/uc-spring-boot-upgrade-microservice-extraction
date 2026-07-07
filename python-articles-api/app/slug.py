import re

# Port of io.spring.core.article.Article#toSlug. The original Java pattern is
#   [\&|[\uFE30-\uFFA0]|\’|\”|\s\?\,\.]+
# which is a (union) character class matching: & | ’ ” ? , . whitespace and the
# CJK compatibility range U+FE30..U+FFA0. Java's nested-class union is flattened
# here into a single Python character class.
_SLUG_RE = re.compile("[&|’”?,.\\s\uFE30-\uFFA0]+")


def to_slug(title: str) -> str:
    return _SLUG_RE.sub("-", title.lower())
