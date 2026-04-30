create table article_views (
  article_id varchar(255) not null,
  user_id varchar(255),
  viewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

alter table article_favorites add column created_at TIMESTAMP;

create trigger set_favorite_created_at after insert on article_favorites
for each row
when NEW.created_at is null
begin
    update article_favorites
    set created_at = datetime('now')
    where article_id = NEW.article_id and user_id = NEW.user_id and created_at is null;
end;
