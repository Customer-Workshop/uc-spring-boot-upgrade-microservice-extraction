CREATE TABLE recommendations (
    id UUID PRIMARY KEY, customer_id UUID NOT NULL, risk_category VARCHAR(20) NOT NULL,
    risk_score DECIMAL(4,1) NOT NULL, customer_profile JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_rec_customer ON recommendations(customer_id);
CREATE TABLE recommended_products (
    id UUID PRIMARY KEY, recommendation_id UUID NOT NULL REFERENCES recommendations(id),
    product_name VARCHAR(200) NOT NULL, category VARCHAR(30) NOT NULL,
    match_score DECIMAL(5,1) NOT NULL, allocation_percent DECIMAL(5,2) DEFAULT 0,
    rationale TEXT, rank INT NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
