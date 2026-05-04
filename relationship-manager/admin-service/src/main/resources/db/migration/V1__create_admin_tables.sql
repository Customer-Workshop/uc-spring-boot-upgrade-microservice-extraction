CREATE TABLE system_configs (
    id UUID PRIMARY KEY, config_key VARCHAR(200) NOT NULL UNIQUE,
    config_value TEXT NOT NULL, description TEXT, updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
