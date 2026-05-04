CREATE TABLE users (
    id                   UUID PRIMARY KEY,
    email                VARCHAR(255) NOT NULL UNIQUE,
    password_hash        VARCHAR(255) NOT NULL,
    name                 VARCHAR(200),
    phone                VARCHAR(20),
    anonymous_session_id UUID,
    is_anonymous         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_anon_session ON users(anonymous_session_id);
