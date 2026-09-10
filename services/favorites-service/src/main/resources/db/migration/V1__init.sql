create table article_favorites (
  article_id varchar(255) not null,
  user_id varchar(255) not null,
  primary key(article_id, user_id)
);

INSERT INTO article_favorites (article_id, user_id) VALUES
('article-1', 'user-2'),
('article-1', 'user-3'),
('article-2', 'user-1'),
('article-3', 'user-2'),
('article-4', 'user-1'),
('article-5', 'user-3');
