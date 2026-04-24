CREATE TABLE families (
    id CHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    country VARCHAR(2),
    locale VARCHAR(50),
    date_format VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE users (
    id CHAR(36) NOT NULL PRIMARY KEY,
    family_id CHAR(36) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    password_digest VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'member',
    profile_image VARCHAR(512),
    onboarded_at TIMESTAMP NULL,
    otp_secret VARCHAR(255),
    otp_required BOOLEAN NOT NULL DEFAULT FALSE,
    ai_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_family FOREIGN KEY (family_id) REFERENCES families(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sessions (
    id CHAR(36) NOT NULL PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE invitations (
    id CHAR(36) NOT NULL PRIMARY KEY,
    family_id CHAR(36) NOT NULL,
    inviter_id CHAR(36) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'member',
    token VARCHAR(255) NOT NULL UNIQUE,
    accepted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_invitations_family FOREIGN KEY (family_id) REFERENCES families(id) ON DELETE CASCADE,
    CONSTRAINT fk_invitations_inviter FOREIGN KEY (inviter_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_family_id ON users(family_id);
CREATE INDEX idx_sessions_user_id ON sessions(user_id);
CREATE INDEX idx_invitations_token ON invitations(token);
CREATE INDEX idx_invitations_family_id ON invitations(family_id);
