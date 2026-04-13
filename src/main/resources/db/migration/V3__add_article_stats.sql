-- Add view_count to articles for tracking article views
ALTER TABLE articles ADD COLUMN view_count INTEGER DEFAULT 0;

-- Add created_at to article_favorites for trending queries
-- SQLite requires constant defaults for ALTER TABLE ADD COLUMN
ALTER TABLE article_favorites ADD COLUMN created_at TIMESTAMP DEFAULT '2000-01-01 00:00:00';

-- Update existing articles with sample view counts
UPDATE articles SET view_count = 150 WHERE id = 'article-1';
UPDATE articles SET view_count = 89 WHERE id = 'article-2';
UPDATE articles SET view_count = 210 WHERE id = 'article-3';
UPDATE articles SET view_count = 45 WHERE id = 'article-4';
UPDATE articles SET view_count = 120 WHERE id = 'article-5';

-- Update existing favorites with timestamps within last 7 days
UPDATE article_favorites SET created_at = datetime('now', '-6 days') WHERE article_id = 'article-1' AND user_id = 'user-2';
UPDATE article_favorites SET created_at = datetime('now', '-5 days') WHERE article_id = 'article-1' AND user_id = 'user-3';
UPDATE article_favorites SET created_at = datetime('now', '-4 days') WHERE article_id = 'article-2' AND user_id = 'user-1';
UPDATE article_favorites SET created_at = datetime('now', '-3 days') WHERE article_id = 'article-3' AND user_id = 'user-2';
UPDATE article_favorites SET created_at = datetime('now', '-2 days') WHERE article_id = 'article-4' AND user_id = 'user-1';
UPDATE article_favorites SET created_at = datetime('now', '-1 days') WHERE article_id = 'article-5' AND user_id = 'user-3';
