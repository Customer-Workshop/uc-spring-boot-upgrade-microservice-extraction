CREATE TABLE customers (
    id                    UUID PRIMARY KEY,
    external_id           VARCHAR(64) UNIQUE,
    name                  VARCHAR(255) NOT NULL,
    email                 VARCHAR(255) UNIQUE,
    phone                 VARCHAR(20),
    age_group             VARCHAR(10) NOT NULL CHECK (age_group IN ('AGE_20_30', 'AGE_30_40', 'AGE_40_50', 'AGE_50_PLUS')),
    location              VARCHAR(255),
    status                VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_anonymous          BOOLEAN NOT NULL DEFAULT FALSE,
    anonymous_session_id  UUID,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_phone ON customers(phone);
CREATE INDEX idx_customers_status ON customers(status);

CREATE TABLE financial_profiles (
    id                        UUID PRIMARY KEY,
    customer_id               UUID NOT NULL UNIQUE REFERENCES customers(id) ON DELETE CASCADE,
    income_source             VARCHAR(30) NOT NULL,
    income_range              VARCHAR(20) NOT NULL,
    current_investments       JSONB DEFAULT '{}',
    current_savings           DECIMAL(15,2) DEFAULT 0,
    monthly_expenses          DECIMAL(15,2),
    monthly_savings_capacity  DECIMAL(15,2),
    retirement_target_amount  DECIMAL(15,2),
    retirement_target_age     INTEGER,
    currency                  VARCHAR(3) DEFAULT 'INR',
    created_at                TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE risk_profiles (
    id               UUID PRIMARY KEY,
    customer_id      UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    risk_score       DECIMAL(3,1) NOT NULL CHECK (risk_score >= 1.0 AND risk_score <= 10.0),
    risk_category    VARCHAR(20) NOT NULL,
    assessment_data  JSONB NOT NULL DEFAULT '{}',
    model_version    VARCHAR(20) NOT NULL,
    explanation      TEXT,
    assessed_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at       TIMESTAMPTZ,
    is_current       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_risk_profiles_customer ON risk_profiles(customer_id);
CREATE INDEX idx_risk_profiles_current ON risk_profiles(customer_id, is_current) WHERE is_current = TRUE;

CREATE TABLE communication_preferences (
    id                    UUID PRIMARY KEY,
    customer_id           UUID NOT NULL UNIQUE REFERENCES customers(id) ON DELETE CASCADE,
    preferred_channel     VARCHAR(20) NOT NULL,
    preferred_time_start  TIME,
    preferred_time_end    TIME,
    preferred_days        VARCHAR(50),
    timezone              VARCHAR(50) DEFAULT 'Asia/Kolkata',
    opt_in_sms            BOOLEAN DEFAULT FALSE,
    opt_in_whatsapp       BOOLEAN DEFAULT FALSE,
    opt_in_email          BOOLEAN DEFAULT TRUE,
    opt_in_phone          BOOLEAN DEFAULT FALSE,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
