-- Insert sample tags
INSERT INTO tags (id, name) VALUES
('tag-1', 'java'),
('tag-2', 'spring-boot'),
('tag-3', 'web-development'),
('tag-4', 'tutorial'),
('tag-5', 'best-practices'),
('tag-6', 'microservices'),
('tag-7', 'api-design');

-- Insert sample articles (user IDs reference the main app's users table)
INSERT INTO articles (id, user_id, slug, title, description, body, created_at, updated_at) VALUES
('article-1', 'user-1', 'getting-started-with-spring-boot', 'Getting Started with Spring Boot', 'A comprehensive guide to building your first Spring Boot application', 'Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications that you can "just run".', datetime('now', '-7 days'), datetime('now', '-7 days')),
('article-2', 'user-2', 'rest-api-best-practices', 'REST API Best Practices', 'Learn the essential principles for designing robust REST APIs', 'Building a great REST API requires more than just exposing endpoints.', datetime('now', '-5 days'), datetime('now', '-5 days')),
('article-3', 'user-1', 'microservices-architecture-guide', 'Microservices Architecture Guide', 'Understanding microservices patterns and when to use them', 'Microservices architecture has become increasingly popular.', datetime('now', '-3 days'), datetime('now', '-3 days')),
('article-4', 'user-3', 'docker-for-java-developers', 'Docker for Java Developers', 'Containerize your Java applications with Docker', 'Docker has revolutionized how we deploy applications.', datetime('now', '-2 days'), datetime('now', '-2 days')),
('article-5', 'user-2', 'testing-spring-boot-applications', 'Testing Spring Boot Applications', 'A complete guide to testing strategies in Spring Boot', 'Testing is crucial for maintaining code quality.', datetime('now', '-1 days'), datetime('now', '-1 days'));

-- Link articles to tags
INSERT INTO article_tags (article_id, tag_id) VALUES
('article-1', 'tag-1'),
('article-1', 'tag-2'),
('article-1', 'tag-4'),
('article-2', 'tag-3'),
('article-2', 'tag-5'),
('article-2', 'tag-7'),
('article-3', 'tag-2'),
('article-3', 'tag-6'),
('article-3', 'tag-5'),
('article-4', 'tag-1'),
('article-4', 'tag-2'),
('article-4', 'tag-4'),
('article-5', 'tag-1'),
('article-5', 'tag-2'),
('article-5', 'tag-5');

-- Add some favorites
INSERT INTO article_favorites (article_id, user_id) VALUES
('article-1', 'user-2'),
('article-1', 'user-3'),
('article-2', 'user-1'),
('article-3', 'user-2'),
('article-4', 'user-1'),
('article-5', 'user-3');

-- Add some comments
INSERT INTO comments (id, body, article_id, user_id, created_at, updated_at) VALUES
('comment-1', 'Great introduction to Spring Boot!', 'article-1', 'user-2', datetime('now', '-6 days'), datetime('now', '-6 days')),
('comment-2', 'Very helpful REST practices.', 'article-2', 'user-1', datetime('now', '-4 days'), datetime('now', '-4 days')),
('comment-3', 'Microservices can be complex.', 'article-3', 'user-3', datetime('now', '-2 days'), datetime('now', '-2 days'));
