CREATE TABLE products (
    id                  UUID PRIMARY KEY,
    name                VARCHAR(200) NOT NULL,
    description         TEXT,
    category            VARCHAR(30) NOT NULL,
    risk_level          VARCHAR(20) NOT NULL,
    expected_return_min DECIMAL(5,2),
    expected_return_max DECIMAL(5,2),
    min_investment      DECIMAL(15,2) DEFAULT 0,
    max_investment      DECIMAL(15,2),
    tenure_min_months   INT,
    tenure_max_months   INT,
    tax_benefit_section VARCHAR(20),
    suitable_for        JSONB DEFAULT '[]',
    features            JSONB DEFAULT '{}',
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    priority            INT DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_risk_level ON products(risk_level);
CREATE INDEX idx_products_active ON products(is_active);

CREATE TABLE product_risk_mappings (
    id                UUID PRIMARY KEY,
    product_id        UUID NOT NULL REFERENCES products(id),
    risk_category     VARCHAR(20) NOT NULL,
    allocation_percent DECIMAL(5,2) NOT NULL,
    priority          INT DEFAULT 0
);

CREATE INDEX idx_prm_product ON product_risk_mappings(product_id);
CREATE INDEX idx_prm_risk ON product_risk_mappings(risk_category);

-- Seed default products
INSERT INTO products (id, name, description, category, risk_level, expected_return_min, expected_return_max, min_investment, tax_benefit_section) VALUES
    (gen_random_uuid(), 'High-Yield Savings Account', 'Premium savings with competitive interest rates', 'SAVINGS_ACCOUNT', 'LOW', 3.5, 5.0, 10000, NULL),
    (gen_random_uuid(), 'Fixed Deposit - 5 Year', 'Guaranteed returns with capital protection', 'FIXED_DEPOSIT', 'LOW', 6.5, 7.5, 25000, '80C'),
    (gen_random_uuid(), 'Large Cap Equity Fund', 'Index-tracking large cap mutual fund', 'MUTUAL_FUND', 'MODERATE', 10.0, 14.0, 5000, NULL),
    (gen_random_uuid(), 'Multi Cap Growth Fund', 'Diversified equity fund across market caps', 'MUTUAL_FUND', 'HIGH', 12.0, 18.0, 5000, NULL),
    (gen_random_uuid(), 'ELSS Tax Saver Fund', 'Equity linked savings scheme with tax benefits', 'TAX_SAVER', 'MODERATE', 10.0, 15.0, 500, '80C'),
    (gen_random_uuid(), 'National Pension System', 'Government-backed pension scheme', 'PENSION', 'LOW', 8.0, 10.0, 1000, '80CCD'),
    (gen_random_uuid(), 'Sovereign Gold Bond', 'Government securities denominated in gold', 'GOLD', 'LOW', 6.0, 10.0, 5000, NULL),
    (gen_random_uuid(), 'PPF Account', 'Public Provident Fund with tax-free returns', 'PPF', 'LOW', 7.0, 7.5, 500, '80C'),
    (gen_random_uuid(), 'Small Cap Equity Fund', 'High-growth potential small cap fund', 'MUTUAL_FUND', 'VERY_HIGH', 15.0, 25.0, 5000, NULL),
    (gen_random_uuid(), 'Corporate Bond Fund', 'Investment grade corporate bonds', 'BOND', 'MODERATE', 7.0, 9.0, 10000, NULL),
    (gen_random_uuid(), 'Term Insurance Plan', 'Pure life protection plan', 'INSURANCE', 'LOW', NULL, NULL, 500, '80C');
