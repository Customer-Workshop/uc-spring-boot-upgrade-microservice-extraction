package com.bank.rm.projection.domain;

import com.bank.rm.common.dto.RiskCategory;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wealth_projections")
public class WealthProjection {
    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_category")
    private RiskCategory riskCategory;

    @Column(name = "initial_investment", precision = 15, scale = 2)
    private BigDecimal initialInvestment;

    @Column(name = "annual_contribution", precision = 15, scale = 2)
    private BigDecimal annualContribution;

    @Column(name = "projection_years")
    private int projectionYears;

    @Column(name = "target_amount", precision = 15, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "nominal_p50", precision = 15, scale = 2)
    private BigDecimal nominalP50;

    @Column(name = "real_p50", precision = 15, scale = 2)
    private BigDecimal realP50;

    @Column(name = "probability_of_target")
    private Double probabilityOfTarget;

    @Column(name = "scenarios_run")
    private int scenariosRun;

    @Column(name = "result_data", columnDefinition = "jsonb")
    private String resultData;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected WealthProjection() {}

    public WealthProjection(UUID id, UUID customerId, RiskCategory riskCategory,
                             BigDecimal initialInvestment, BigDecimal annualContribution,
                             int projectionYears, BigDecimal targetAmount,
                             BigDecimal nominalP50, BigDecimal realP50,
                             Double probabilityOfTarget, int scenariosRun, String resultData) {
        this.id = id;
        this.customerId = customerId;
        this.riskCategory = riskCategory;
        this.initialInvestment = initialInvestment;
        this.annualContribution = annualContribution;
        this.projectionYears = projectionYears;
        this.targetAmount = targetAmount;
        this.nominalP50 = nominalP50;
        this.realP50 = realP50;
        this.probabilityOfTarget = probabilityOfTarget;
        this.scenariosRun = scenariosRun;
        this.resultData = resultData;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public RiskCategory getRiskCategory() { return riskCategory; }
    public Instant getCreatedAt() { return createdAt; }
}
