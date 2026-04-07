CREATE TABLE comments (
  id VARCHAR(255) PRIMARY KEY,
  body CLOB NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  article_id VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_comments_article_id ON comments(article_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
