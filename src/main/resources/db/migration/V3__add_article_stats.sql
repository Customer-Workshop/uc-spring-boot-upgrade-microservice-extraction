ALTER TABLE articles ADD COLUMN view_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE article_favorites ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE articles SET view_count = 42 WHERE id = 'article-1';
UPDATE articles SET view_count = 28 WHERE id = 'article-2';
UPDATE articles SET view_count = 15 WHERE id = 'article-3';
UPDATE articles SET view_count = 10 WHERE id = 'article-4';
UPDATE articles SET view_count = 35 WHERE id = 'article-5';
