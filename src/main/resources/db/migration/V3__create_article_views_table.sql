create table article_views (
  id integer primary key autoincrement,
  article_id varchar(255) not null,
  viewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
