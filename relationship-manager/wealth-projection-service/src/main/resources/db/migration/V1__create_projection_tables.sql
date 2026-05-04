CREATE TABLE wealth_projections (
    id                    UUID PRIMARY KEY,
    customer_id           UUID NOT NULL,
    risk_category         VARCHAR(20) NOT NULL,
    initial_investment    DECIMAL(15,2) NOT NULL,
    annual_contribution   DECIMAL(15,2) NOT NULL,
    projection_years      INT NOT NULL,
    target_amount         DECIMAL(15,2),
    nominal_p50           DECIMAL(15,2),
    real_p50              DECIMAL(15,2),
    probability_of_target DECIMAL(5,1),
    scenarios_run         INT NOT NULL,
    result_data           JSONB NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_projections_customer ON wealth_projections(customer_id);
