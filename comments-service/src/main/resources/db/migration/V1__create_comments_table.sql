create table comments (
  id varchar(255) primary key,
  body text,
  article_id varchar(255) not null,
  user_id varchar(255) not null,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

create index idx_comments_article_id on comments(article_id);
