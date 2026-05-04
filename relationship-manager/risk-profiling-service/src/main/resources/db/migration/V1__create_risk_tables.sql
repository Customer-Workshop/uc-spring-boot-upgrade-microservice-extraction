CREATE TABLE risk_assessments (
    id              UUID PRIMARY KEY,
    customer_id     UUID NOT NULL,
    risk_score      DECIMAL(4,1) NOT NULL,
    risk_category   VARCHAR(20) NOT NULL,
    feature_vector  JSONB NOT NULL DEFAULT '{}',
    shap_values     JSONB NOT NULL DEFAULT '{}',
    explanation     TEXT,
    model_version   VARCHAR(50) NOT NULL,
    is_current      BOOLEAN NOT NULL DEFAULT TRUE,
    assessed_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_risk_customer ON risk_assessments(customer_id);
CREATE INDEX idx_risk_current ON risk_assessments(customer_id, is_current) WHERE is_current = TRUE;

CREATE TABLE portfolio_allocations (
    id                  UUID PRIMARY KEY,
    assessment_id       UUID NOT NULL REFERENCES risk_assessments(id),
    customer_id         UUID NOT NULL,
    risk_category       VARCHAR(20) NOT NULL,
    equity_percent      DECIMAL(5,2) NOT NULL,
    debt_percent        DECIMAL(5,2) NOT NULL,
    gold_percent        DECIMAL(5,2) NOT NULL,
    cash_percent        DECIMAL(5,2) NOT NULL,
    alternative_percent DECIMAL(5,2) NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_portfolio_assessment ON portfolio_allocations(assessment_id);
CREATE INDEX idx_portfolio_customer ON portfolio_allocations(customer_id);
