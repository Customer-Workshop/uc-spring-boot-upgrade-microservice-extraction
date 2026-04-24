CREATE TABLE accounts (
    id CHAR(36) NOT NULL PRIMARY KEY,
    family_id CHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    subtype VARCHAR(50),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    balance DECIMAL(19,4) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    institution_name VARCHAR(255),
    institution_url VARCHAR(512),
    logo_url VARCHAR(512),
    scheduled_for_deletion BOOLEAN NOT NULL DEFAULT FALSE,
    hide_from_enrich BOOLEAN NOT NULL DEFAULT FALSE,
    excluded_from_totals BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE balances (
    id CHAR(36) NOT NULL PRIMARY KEY,
    account_id CHAR(36) NOT NULL,
    date DATE NOT NULL,
    balance DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_balances_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    UNIQUE KEY uk_balances_account_date (account_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE securities (
    id CHAR(36) NOT NULL PRIMARY KEY,
    ticker VARCHAR(20),
    name VARCHAR(255),
    country_code VARCHAR(5),
    exchange_mic VARCHAR(10),
    exchange_acronym VARCHAR(20),
    logo_url VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE security_prices (
    id CHAR(36) NOT NULL PRIMARY KEY,
    security_id CHAR(36) NOT NULL,
    date DATE NOT NULL,
    price DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_security_prices_security FOREIGN KEY (security_id) REFERENCES securities(id) ON DELETE CASCADE,
    UNIQUE KEY uk_security_prices_security_date (security_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE holdings (
    id CHAR(36) NOT NULL PRIMARY KEY,
    account_id CHAR(36) NOT NULL,
    security_id CHAR(36),
    date DATE NOT NULL,
    qty DECIMAL(19,6) NOT NULL DEFAULT 0,
    price DECIMAL(19,4) NOT NULL DEFAULT 0,
    amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    cost_basis DECIMAL(19,4),
    cost_basis_source VARCHAR(20),
    cost_basis_locked BOOLEAN NOT NULL DEFAULT FALSE,
    name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_holdings_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_holdings_security FOREIGN KEY (security_id) REFERENCES securities(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE account_shares (
    id CHAR(36) NOT NULL PRIMARY KEY,
    account_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    permission VARCHAR(20) NOT NULL DEFAULT 'read_only',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_shares_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    UNIQUE KEY uk_account_shares (account_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_accounts_family_id ON accounts(family_id);
CREATE INDEX idx_accounts_account_type ON accounts(account_type);
CREATE INDEX idx_balances_account_date ON balances(account_id, date);
CREATE INDEX idx_holdings_account_id ON holdings(account_id);
CREATE INDEX idx_holdings_security_id ON holdings(security_id);
CREATE INDEX idx_securities_ticker ON securities(ticker);
