-- Favorite service schema tables

CREATE TABLE IF NOT EXISTS article_favorites (
  article_id VARCHAR(255) NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (article_id, user_id)
);
