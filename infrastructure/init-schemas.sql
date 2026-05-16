-- Initialize PostgreSQL schemas for each microservice
-- Each service owns its own schema for data isolation

CREATE SCHEMA IF NOT EXISTS user_schema;
CREATE SCHEMA IF NOT EXISTS article_schema;
CREATE SCHEMA IF NOT EXISTS comment_schema;
CREATE SCHEMA IF NOT EXISTS favorite_schema;

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA user_schema TO realworld;
GRANT ALL PRIVILEGES ON SCHEMA article_schema TO realworld;
GRANT ALL PRIVILEGES ON SCHEMA comment_schema TO realworld;
GRANT ALL PRIVILEGES ON SCHEMA favorite_schema TO realworld;
