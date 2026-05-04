CREATE TABLE risk_assessments (
    id UUID PRIMARY KEY, customer_id UUID NOT NULL, risk_score DECIMAL(4,1) NOT NULL,
    risk_category VARCHAR(20) NOT NULL, feature_vector JSONB NOT NULL DEFAULT '{}',
    shap_values JSONB NOT NULL DEFAULT '{}', explanation TEXT, model_version VARCHAR(50) NOT NULL,
    is_current BOOLEAN NOT NULL DEFAULT TRUE, assessed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_risk_customer ON risk_assessments(customer_id);
CREATE INDEX idx_risk_current ON risk_assessments(customer_id, is_current) WHERE is_current = TRUE;
