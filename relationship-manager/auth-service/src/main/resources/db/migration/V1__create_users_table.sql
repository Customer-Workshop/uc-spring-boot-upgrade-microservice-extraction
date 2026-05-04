CREATE TABLE users (
    id                   UUID PRIMARY KEY,
    email                VARCHAR(255) NOT NULL UNIQUE,
    password_hash        VARCHAR(255) NOT NULL,
    phone                VARCHAR(20) UNIQUE,
    role                 VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER',
    is_active            BOOLEAN NOT NULL DEFAULT TRUE,
    anonymous_session_id UUID,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_login_at        TIMESTAMPTZ
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);
