create table projects (
  id varchar(255) primary key,
  name varchar(255) not null,
  description text,
  client varchar(255),
  start_date TIMESTAMP,
  status varchar(50) not null default 'active',
  user_id varchar(255),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample projects
INSERT INTO projects (id, name, description, client, start_date, status, user_id, created_at, updated_at) VALUES
('project-1', 'Website Redesign', 'Complete overhaul of the company website with modern design', 'Acme Corp', datetime('now', '-30 days'), 'active', 'user-1', datetime('now', '-30 days'), datetime('now', '-30 days')),
('project-2', 'Mobile App Development', 'Build a cross-platform mobile application', 'TechStart Inc', datetime('now', '-20 days'), 'active', 'user-2', datetime('now', '-20 days'), datetime('now', '-20 days')),
('project-3', 'API Integration', 'Integrate third-party payment and shipping APIs', 'ShopEasy Ltd', datetime('now', '-45 days'), 'completed', 'user-1', datetime('now', '-45 days'), datetime('now', '-10 days')),
('project-4', 'Database Migration', 'Migrate legacy database to cloud infrastructure', 'DataFlow Systems', datetime('now', '-15 days'), 'on-hold', 'user-3', datetime('now', '-15 days'), datetime('now', '-15 days')),
('project-5', 'Security Audit', 'Comprehensive security review and penetration testing', 'SecureNet', datetime('now', '-5 days'), 'active', 'user-2', datetime('now', '-5 days'), datetime('now', '-5 days'));
