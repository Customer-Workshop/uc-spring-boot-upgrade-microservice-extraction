-- Article Service tables extracted from the monolith
-- Tables: articles, tags, article_tags

create table if not exists articles (
  id varchar(255) primary key,
  user_id varchar(255),
  slug varchar(255) UNIQUE,
  title varchar(255),
  description text,
  body text,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

create table if not exists tags (
  id varchar(255) primary key,
  name varchar(255)
);

create table if not exists article_tags (
  article_id varchar(255) not null,
  tag_id varchar(255) not null
);

-- Note: In a full microservices deployment, the users table would live in the User Service.
-- This local copy is kept for JOIN queries in ArticleReadService until cross-service data
-- fetching is fully implemented via HTTP clients.
create table if not exists users (
  id varchar(255) primary key,
  username varchar(255) UNIQUE,
  password varchar(255),
  email varchar(255) UNIQUE,
  bio text,
  image varchar(512)
);

-- Note: article_favorites table is owned by the Favorite Service.
-- This local copy is kept for JOIN queries until cross-service data fetching is implemented.
create table if not exists article_favorites (
  article_id varchar(255) not null,
  user_id varchar(255) not null,
  primary key(article_id, user_id)
);
