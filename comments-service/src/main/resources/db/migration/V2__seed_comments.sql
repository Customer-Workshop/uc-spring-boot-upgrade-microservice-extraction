INSERT INTO comments (id, body, article_id, user_id, created_at, updated_at) VALUES
('comment-1', 'Great article! This really helped me understand Spring Boot basics.', 'article-1', 'user-2', DATEADD('DAY', -6, CURRENT_TIMESTAMP), DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
('comment-2', 'Thanks for sharing. The code examples are very clear.', 'article-1', 'user-3', DATEADD('DAY', -6, CURRENT_TIMESTAMP), DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
('comment-3', 'Excellent best practices guide. I''ll be implementing these in my project.', 'article-2', 'user-1', DATEADD('DAY', -4, CURRENT_TIMESTAMP), DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
('comment-4', 'Very comprehensive overview of microservices. Well written!', 'article-3', 'user-2', DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
('comment-5', 'Docker tutorial was exactly what I needed. Thanks!', 'article-4', 'user-1', DATEADD('DAY', -1, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP));
