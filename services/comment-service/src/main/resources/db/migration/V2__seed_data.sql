INSERT INTO users (id, username, email, password, bio, image) VALUES
('user-1', 'johndoe', 'john@example.com', '$2a$10$AbglDchyhkogGBIxNoHdN.pBDK86VNXtF.Vh6N72G9s1rjw7z2b4u', 'Full-stack developer and tech enthusiast', 'https://api.dicebear.com/7.x/avataaars/svg?seed=John'),
('user-2', 'janedoe', 'jane@example.com', '$2a$10$AbglDchyhkogGBIxNoHdN.pBDK86VNXtF.Vh6N72G9s1rjw7z2b4u', 'Software architect passionate about clean code', 'https://api.dicebear.com/7.x/avataaars/svg?seed=Jane'),
('user-3', 'bobsmith', 'bob@example.com', '$2a$10$AbglDchyhkogGBIxNoHdN.pBDK86VNXtF.Vh6N72G9s1rjw7z2b4u', 'DevOps engineer and cloud enthusiast', 'https://api.dicebear.com/7.x/avataaars/svg?seed=Bob');

INSERT INTO articles (id, user_id, slug, title, description, body, created_at, updated_at) VALUES
('article-1', 'user-1', 'getting-started-with-spring-boot', 'Getting Started with Spring Boot', 'A comprehensive guide', 'Spring Boot content...', datetime('now', '-7 days'), datetime('now', '-7 days')),
('article-2', 'user-2', 'rest-api-best-practices', 'REST API Best Practices', 'Essential principles', 'REST API content...', datetime('now', '-5 days'), datetime('now', '-5 days')),
('article-3', 'user-1', 'microservices-architecture-guide', 'Microservices Architecture Guide', 'Understanding patterns', 'Microservices content...', datetime('now', '-3 days'), datetime('now', '-3 days')),
('article-4', 'user-3', 'docker-for-java-developers', 'Docker for Java Developers', 'Containerize apps', 'Docker content...', datetime('now', '-2 days'), datetime('now', '-2 days')),
('article-5', 'user-2', 'testing-spring-boot-applications', 'Testing Spring Boot Applications', 'Testing strategies', 'Testing content...', datetime('now', '-1 days'), datetime('now', '-1 days'));

INSERT INTO comments (id, body, article_id, user_id, created_at, updated_at) VALUES
('comment-1', 'Great article! This really helped me understand Spring Boot basics.', 'article-1', 'user-2', datetime('now', '-6 days'), datetime('now', '-6 days')),
('comment-2', 'Thanks for sharing. The code examples are very clear.', 'article-1', 'user-3', datetime('now', '-6 days'), datetime('now', '-6 days')),
('comment-3', 'Excellent best practices guide.', 'article-2', 'user-1', datetime('now', '-4 days'), datetime('now', '-4 days')),
('comment-4', 'Very comprehensive overview of microservices.', 'article-3', 'user-2', datetime('now', '-2 days'), datetime('now', '-2 days')),
('comment-5', 'Docker tutorial was exactly what I needed.', 'article-4', 'user-1', datetime('now', '-1 days'), datetime('now', '-1 days'));

INSERT INTO follows (user_id, follow_id) VALUES
('user-1', 'user-2'),
('user-2', 'user-1'),
('user-3', 'user-1'),
('user-3', 'user-2');
