-- Insert sample comments (user_id and article_id reference other services)
INSERT INTO comments (id, body, user_id, article_id, created_at) VALUES
('comment-1', 'Great article! Very helpful for beginners.', 'user-2', 'article-1', NOW() - INTERVAL '6 days'),
('comment-2', 'I have been using these practices for years. Highly recommended!', 'user-3', 'article-2', NOW() - INTERVAL '4 days'),
('comment-3', 'Could you elaborate more on the strangler fig pattern?', 'user-2', 'article-3', NOW() - INTERVAL '2 days'),
('comment-4', 'Docker has really simplified our deployment process.', 'user-1', 'article-4', NOW() - INTERVAL '1 days');

-- Insert sample favorites
INSERT INTO article_favorites (article_id, user_id) VALUES
('article-1', 'user-2'),
('article-1', 'user-3'),
('article-2', 'user-1'),
('article-2', 'user-3'),
('article-3', 'user-2'),
('article-4', 'user-1'),
('article-4', 'user-2'),
('article-4', 'user-3'),
('article-5', 'user-1');
