-- V3: Enhance tags as a first-class entity
-- Add created_at column to tags table for auditing
-- SQLite requires constant defaults for ALTER TABLE ADD COLUMN
ALTER TABLE tags ADD COLUMN created_at TIMESTAMP DEFAULT '1970-01-01 00:00:00';

-- Add unique index on tag name to prevent duplicates
CREATE UNIQUE INDEX IF NOT EXISTS idx_tags_name ON tags(name);

-- Add indexes on article_tags for query performance
CREATE INDEX IF NOT EXISTS idx_article_tags_article_id ON article_tags(article_id);
CREATE INDEX IF NOT EXISTS idx_article_tags_tag_id ON article_tags(tag_id);

-- Add unique constraint on article_tags to prevent duplicate associations
CREATE UNIQUE INDEX IF NOT EXISTS idx_article_tags_unique ON article_tags(article_id, tag_id);
