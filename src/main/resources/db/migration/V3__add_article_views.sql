create table article_views (
  article_id varchar(255) primary key,
  view_count integer not null default 0,
  foreign key (article_id) references articles(id)
);

-- Initialize view counts for existing articles
INSERT INTO article_views (article_id, view_count)
SELECT id, 0 FROM articles;
