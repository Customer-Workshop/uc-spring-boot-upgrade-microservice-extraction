create table article_views (
  id varchar(255) primary key,
  article_id varchar(255) not null,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
