-- Add created_at column to article_favorites for trending article queries
-- SQLite requires a constant default for ALTER TABLE ADD COLUMN
ALTER TABLE article_favorites ADD COLUMN created_at TIMESTAMP DEFAULT '1970-01-01 00:00:00';

-- Backfill existing favorites with the article's created_at as an approximation
UPDATE article_favorites
SET created_at = (
    SELECT a.created_at FROM articles a WHERE a.id = article_favorites.article_id
);
