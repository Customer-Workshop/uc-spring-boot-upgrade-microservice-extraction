create table article_views (
  article_id varchar(255) not null,
  user_id varchar(255),
  viewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

alter table article_favorites add column created_at TIMESTAMP;
