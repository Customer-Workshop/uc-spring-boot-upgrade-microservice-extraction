-- Performance indexes for frequently queried columns

-- articles: user_id used in feed queries (findArticlesOfAuthors, countFeedSize)
CREATE INDEX idx_articles_user_id ON articles(user_id);

-- articles: created_at used in ORDER BY for all article list/cursor queries
CREATE INDEX idx_articles_created_at ON articles(created_at DESC);

-- article_tags: article_id used in JOINs for tag lookups
CREATE INDEX idx_article_tags_article_id ON article_tags(article_id);

-- article_tags: tag_id used in JOINs to resolve tag names
CREATE INDEX idx_article_tags_tag_id ON article_tags(tag_id);

-- article_favorites: indexed individually for count queries and user-favorite checks
CREATE INDEX idx_article_favorites_article_id ON article_favorites(article_id);
CREATE INDEX idx_article_favorites_user_id ON article_favorites(user_id);

-- follows: user_id used in isUserFollowing and followedUsers queries
CREATE INDEX idx_follows_user_id ON follows(user_id);

-- follows: follow_id used in followingAuthors queries
CREATE INDEX idx_follows_follow_id ON follows(follow_id);

-- comments: article_id used in findByArticleId queries
CREATE INDEX idx_comments_article_id ON comments(article_id);

-- comments: created_at used in cursor-based pagination ordering
CREATE INDEX idx_comments_created_at ON comments(created_at DESC);

-- tags: name used in tag-based article filtering
CREATE INDEX idx_tags_name ON tags(name);
