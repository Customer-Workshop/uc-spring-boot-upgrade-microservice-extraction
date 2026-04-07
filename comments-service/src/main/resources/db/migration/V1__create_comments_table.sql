CREATE TABLE comments (
  id VARCHAR(255) NOT NULL PRIMARY KEY,
  body CLOB,
  user_id VARCHAR(255),
  article_id VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_comments_article_id ON comments(article_id);
