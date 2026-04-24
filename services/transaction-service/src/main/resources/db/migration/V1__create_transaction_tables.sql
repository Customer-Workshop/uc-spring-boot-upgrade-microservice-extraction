CREATE TABLE entries (
    id CHAR(36) NOT NULL PRIMARY KEY,
    account_id CHAR(36) NOT NULL,
    entryable_type VARCHAR(50) NOT NULL,
    name VARCHAR(255),
    date DATE NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    notes TEXT,
    excluded BOOLEAN NOT NULL DEFAULT FALSE,
    enriched_at TIMESTAMP NULL,
    marked_as_transfer BOOLEAN NOT NULL DEFAULT FALSE,
    plaid_id VARCHAR(255),
    pending BOOLEAN NOT NULL DEFAULT FALSE,
    parent_entry_id CHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE transactions (
    id CHAR(36) NOT NULL PRIMARY KEY,
    entry_id CHAR(36) NOT NULL UNIQUE,
    category_id CHAR(36),
    merchant_id CHAR(36),
    kind VARCHAR(50) NOT NULL DEFAULT 'standard',
    nature VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE trades (
    id CHAR(36) NOT NULL PRIMARY KEY,
    entry_id CHAR(36) NOT NULL UNIQUE,
    security_id CHAR(36),
    qty DECIMAL(19,6) NOT NULL DEFAULT 0,
    price DECIMAL(19,4) NOT NULL DEFAULT 0,
    trade_type VARCHAR(20) NOT NULL DEFAULT 'buy',
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE valuations (
    id CHAR(36) NOT NULL PRIMARY KEY,
    entry_id CHAR(36) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE transfers (
    id CHAR(36) NOT NULL PRIMARY KEY,
    inflow_entry_id CHAR(36) NOT NULL,
    outflow_entry_id CHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE entry_tags (
    id CHAR(36) NOT NULL PRIMARY KEY,
    entry_id CHAR(36) NOT NULL,
    tag_id CHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_entry_tags (entry_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE recurring_transactions (
    id CHAR(36) NOT NULL PRIMARY KEY,
    family_id CHAR(36) NOT NULL,
    account_id CHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    frequency VARCHAR(20) NOT NULL DEFAULT 'monthly',
    start_date DATE NOT NULL,
    end_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_entries_account_id ON entries(account_id);
CREATE INDEX idx_entries_date ON entries(date);
CREATE INDEX idx_entries_entryable_type ON entries(entryable_type);
CREATE INDEX idx_entries_pending ON entries(pending);
CREATE INDEX idx_transactions_entry_id ON transactions(entry_id);
CREATE INDEX idx_transactions_category_id ON transactions(category_id);
CREATE INDEX idx_transactions_merchant_id ON transactions(merchant_id);
CREATE INDEX idx_trades_entry_id ON trades(entry_id);
CREATE INDEX idx_trades_security_id ON trades(security_id);
CREATE INDEX idx_entry_tags_entry_id ON entry_tags(entry_id);
CREATE INDEX idx_entry_tags_tag_id ON entry_tags(tag_id);
