-- Seed tags
INSERT INTO tags (id, name) VALUES ('tag-1', 'java');
INSERT INTO tags (id, name) VALUES ('tag-2', 'spring-boot');
INSERT INTO tags (id, name) VALUES ('tag-3', 'web-development');
INSERT INTO tags (id, name) VALUES ('tag-4', 'tutorial');
INSERT INTO tags (id, name) VALUES ('tag-5', 'best-practices');
INSERT INTO tags (id, name) VALUES ('tag-6', 'microservices');
INSERT INTO tags (id, name) VALUES ('tag-7', 'api-design');

-- Seed articles (user_id references users in the monolith)
INSERT INTO articles (id, user_id, slug, title, description, body, created_at, updated_at)
VALUES ('article-1', 'user-1', 'getting-started-with-spring-boot', 'Getting Started with Spring Boot',
        'A comprehensive guide to Spring Boot', 'Spring Boot makes it easy to create stand-alone applications...',
        '2024-01-15 10:00:00', '2024-01-15 10:00:00');

INSERT INTO articles (id, user_id, slug, title, description, body, created_at, updated_at)
VALUES ('article-2', 'user-1', 'advanced-java-techniques', 'Advanced Java Techniques',
        'Deep dive into advanced Java', 'Explore advanced Java features including generics, lambdas...',
        '2024-02-01 14:30:00', '2024-02-01 14:30:00');

INSERT INTO articles (id, user_id, slug, title, description, body, created_at, updated_at)
VALUES ('article-3', 'user-2', 'building-rest-apis', 'Building REST APIs',
        'Best practices for REST API design', 'REST APIs are the backbone of modern web applications...',
        '2024-02-15 09:00:00', '2024-02-15 09:00:00');

INSERT INTO articles (id, user_id, slug, title, description, body, created_at, updated_at)
VALUES ('article-4', 'user-2', 'microservices-architecture', 'Microservices Architecture',
        'Understanding microservices patterns', 'Microservices architecture decomposes applications...',
        '2024-03-01 11:00:00', '2024-03-01 11:00:00');

INSERT INTO articles (id, user_id, slug, title, description, body, created_at, updated_at)
VALUES ('article-5', 'user-3', 'web-development-fundamentals', 'Web Development Fundamentals',
        'Essential web development concepts', 'Web development encompasses many technologies...',
        '2024-03-15 16:00:00', '2024-03-15 16:00:00');

-- Seed article_tags
INSERT INTO article_tags (article_id, tag_id) VALUES ('article-1', 'tag-1');
INSERT INTO article_tags (article_id, tag_id) VALUES ('article-1', 'tag-2');
INSERT INTO article_tags (article_id, tag_id) VALUES ('article-1', 'tag-4');
INSERT INTO article_tags (article_id, tag_id) VALUES ('article-2', 'tag-1');
INSERT INTO article_tags (article_id, tag_id) VALUES ('article-2', 'tag-5');
INSERT INTO article_tags (article_id, tag_id) VALUES ('article-3', 'tag-7');
INSERT INTO article_tags (article_id, tag_id) VALUES ('article-3', 'tag-5');
INSERT INTO article_tags (article_id, tag_id) VALUES ('article-4', 'tag-6');
INSERT INTO article_tags (article_id, tag_id) VALUES ('article-4', 'tag-2');
INSERT INTO article_tags (article_id, tag_id) VALUES ('article-5', 'tag-3');
INSERT INTO article_tags (article_id, tag_id) VALUES ('article-5', 'tag-4');

-- Seed article_favorites
INSERT INTO article_favorites (article_id, user_id) VALUES ('article-1', 'user-2');
INSERT INTO article_favorites (article_id, user_id) VALUES ('article-1', 'user-3');
INSERT INTO article_favorites (article_id, user_id) VALUES ('article-2', 'user-2');
INSERT INTO article_favorites (article_id, user_id) VALUES ('article-3', 'user-1');
INSERT INTO article_favorites (article_id, user_id) VALUES ('article-3', 'user-3');
INSERT INTO article_favorites (article_id, user_id) VALUES ('article-4', 'user-1');

-- Seed comments
INSERT INTO comments (id, body, user_id, article_id, created_at, updated_at)
VALUES ('comment-1', 'Great introduction to Spring Boot!', 'user-2', 'article-1', '2024-01-16 08:00:00', '2024-01-16 08:00:00');

INSERT INTO comments (id, body, user_id, article_id, created_at, updated_at)
VALUES ('comment-2', 'Very helpful tutorial, thanks!', 'user-3', 'article-1', '2024-01-17 12:00:00', '2024-01-17 12:00:00');

INSERT INTO comments (id, body, user_id, article_id, created_at, updated_at)
VALUES ('comment-3', 'Learned a lot from this article.', 'user-1', 'article-3', '2024-02-16 10:00:00', '2024-02-16 10:00:00');

INSERT INTO comments (id, body, user_id, article_id, created_at, updated_at)
VALUES ('comment-4', 'Excellent explanation of microservices!', 'user-3', 'article-4', '2024-03-02 14:00:00', '2024-03-02 14:00:00');
