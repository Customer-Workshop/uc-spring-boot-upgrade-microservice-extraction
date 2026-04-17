CREATE TABLE articles (
  id VARCHAR(255) PRIMARY KEY,
  user_id VARCHAR(255),
  slug VARCHAR(255) UNIQUE,
  title VARCHAR(255),
  description TEXT,
  body TEXT,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tags (
  id VARCHAR(255) PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

CREATE TABLE article_tags (
  article_id VARCHAR(255) NOT NULL,
  tag_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (article_id, tag_id),
  FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
  FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);
