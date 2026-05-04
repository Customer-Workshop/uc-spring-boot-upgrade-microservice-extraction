CREATE TABLE customer_profiles (
    id                    UUID PRIMARY KEY,
    user_id               UUID NOT NULL UNIQUE,
    name                  VARCHAR(200),
    phone                 VARCHAR(20),
    email                 VARCHAR(255),
    location              VARCHAR(200),
    age_group             VARCHAR(20),
    income_source         VARCHAR(20),
    income_range          VARCHAR(20),
    current_savings       DECIMAL(15,2),
    current_investments   DECIMAL(15,2),
    monthly_expenses      DECIMAL(15,2),
    retirement_target     DECIMAL(15,2),
    retirement_age        INT,
    dependents            INT,
    risk_category         VARCHAR(20),
    risk_score            DECIMAL(4,1),
    preferred_channel     VARCHAR(20),
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cp_user ON customer_profiles(user_id);
