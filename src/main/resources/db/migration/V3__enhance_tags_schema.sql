-- Enhance tags table with unique constraint on name
CREATE UNIQUE INDEX IF NOT EXISTS idx_tags_name ON tags(name);

-- Add indexes to article_tags for efficient lookups
CREATE INDEX IF NOT EXISTS idx_article_tags_article_id ON article_tags(article_id);
CREATE INDEX IF NOT EXISTS idx_article_tags_tag_id ON article_tags(tag_id);

-- Add composite unique constraint to prevent duplicate article-tag associations
CREATE UNIQUE INDEX IF NOT EXISTS idx_article_tags_unique ON article_tags(article_id, tag_id);
