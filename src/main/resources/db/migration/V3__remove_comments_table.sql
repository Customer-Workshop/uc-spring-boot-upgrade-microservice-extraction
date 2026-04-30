-- Comments have been extracted to a standalone comments microservice.
-- This migration removes the comments table from the monolith database.
DROP TABLE IF EXISTS comments;
