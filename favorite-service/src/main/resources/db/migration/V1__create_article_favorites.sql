create table article_favorites (
  article_id varchar(255) not null,
  user_id varchar(255) not null,
  primary key(article_id, user_id)
);
