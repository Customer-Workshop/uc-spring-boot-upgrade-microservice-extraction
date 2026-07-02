-- V3: Enhance tags as a first-class entity
-- Evolves existing schema to support tags as independent entities with
-- UNIQUE constraint, timestamps, foreign keys, and indexes.
--
-- For databases created with V1 (which already includes these constraints),
-- this migration adds only the performance indexes.
-- For databases migrated from the original V1 (without constraints),
-- the full table recreation below applies.

-- Add indexes for efficient lookups (IF NOT EXISTS for idempotency)
CREATE INDEX IF NOT EXISTS idx_article_tags_article_id ON article_tags(article_id);
CREATE INDEX IF NOT EXISTS idx_article_tags_tag_id ON article_tags(tag_id);
CREATE INDEX IF NOT EXISTS idx_tags_name ON tags(name);
