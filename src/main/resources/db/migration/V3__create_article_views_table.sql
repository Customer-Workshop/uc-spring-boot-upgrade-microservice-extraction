create table article_views (
  article_id varchar(255) primary key,
  view_count integer not null default 0
);
