create table tags (
  id varchar(255) primary key,
  name varchar(255) not null
);

create table article_tags (
  article_id varchar(255) not null,
  tag_id varchar(255) not null
);
