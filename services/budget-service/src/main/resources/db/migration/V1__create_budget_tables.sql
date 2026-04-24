CREATE TABLE categories (
    id CHAR(36) NOT NULL PRIMARY KEY,
    family_id CHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(7),
    icon VARCHAR(50),
    parent_id CHAR(36),
    classification VARCHAR(20) NOT NULL DEFAULT 'expense',
    lucide_icon VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE budgets (
    id CHAR(36) NOT NULL PRIMARY KEY,
    family_id CHAR(36) NOT NULL,
    category_id CHAR(36),
    name VARCHAR(255) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    period_type VARCHAR(20) NOT NULL DEFAULT 'monthly',
    start_date DATE NOT NULL,
    end_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_budgets_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE budget_categories (
    id CHAR(36) NOT NULL PRIMARY KEY,
    budget_id CHAR(36) NOT NULL,
    category_id CHAR(36) NOT NULL,
    amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_budget_categories_budget FOREIGN KEY (budget_id) REFERENCES budgets(id) ON DELETE CASCADE,
    CONSTRAINT fk_budget_categories_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    UNIQUE KEY uk_budget_categories (budget_id, category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE tags (
    id CHAR(36) NOT NULL PRIMARY KEY,
    family_id CHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(7),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE merchants (
    id CHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    icon_url VARCHAR(512),
    color VARCHAR(7),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE family_merchant_associations (
    id CHAR(36) NOT NULL PRIMARY KEY,
    family_id CHAR(36) NOT NULL,
    merchant_id CHAR(36) NOT NULL,
    category_id CHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_fma_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id) ON DELETE CASCADE,
    CONSTRAINT fk_fma_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    UNIQUE KEY uk_family_merchant (family_id, merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE rules (
    id CHAR(36) NOT NULL PRIMARY KEY,
    family_id CHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE rule_conditions (
    id CHAR(36) NOT NULL PRIMARY KEY,
    rule_id CHAR(36) NOT NULL,
    condition_type VARCHAR(50) NOT NULL,
    operator VARCHAR(20) NOT NULL,
    value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rule_conditions_rule FOREIGN KEY (rule_id) REFERENCES rules(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE rule_actions (
    id CHAR(36) NOT NULL PRIMARY KEY,
    rule_id CHAR(36) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rule_actions_rule FOREIGN KEY (rule_id) REFERENCES rules(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_categories_family_id ON categories(family_id);
CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_budgets_family_id ON budgets(family_id);
CREATE INDEX idx_tags_family_id ON tags(family_id);
CREATE INDEX idx_rules_family_id ON rules(family_id);
