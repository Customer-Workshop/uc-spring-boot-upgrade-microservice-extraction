CREATE TABLE comments (
  id VARCHAR(255) PRIMARY KEY,
  body TEXT,
  user_id VARCHAR(255),
  article_id VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE article_favorites (
  article_id VARCHAR(255) NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (article_id, user_id)
);
