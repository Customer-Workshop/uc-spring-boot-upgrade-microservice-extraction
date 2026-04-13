create table article_views (
  article_id varchar(255) not null,
  viewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

create index idx_article_views_article_id on article_views(article_id);
