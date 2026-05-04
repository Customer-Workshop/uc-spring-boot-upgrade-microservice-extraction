package com.bank.rm.risk.domain;

import com.bank.rm.common.dto.RiskCategory;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_assessments")
public class RiskAssessment {
    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "risk_score", nullable = false)
    private double riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_category", nullable = false)
    private RiskCategory riskCategory;

    @Column(name = "feature_vector", columnDefinition = "jsonb")
    private String featureVector;

    @Column(name = "shap_values", columnDefinition = "jsonb")
    private String shapValues;

    @Column(columnDefinition = "text")
    private String explanation;

    @Column(name = "model_version", nullable = false)
    private String modelVersion;

    @Column(name = "is_current")
    private boolean current;

    @Column(name = "assessed_at")
    private Instant assessedAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected RiskAssessment() {}

    public RiskAssessment(UUID id, UUID customerId, double riskScore, RiskCategory riskCategory,
                           String featureVector, String shapValues, String explanation, String modelVersion) {
        this.id = id;
        this.customerId = customerId;
        this.riskScore = riskScore;
        this.riskCategory = riskCategory;
        this.featureVector = featureVector;
        this.shapValues = shapValues;
        this.explanation = explanation;
        this.modelVersion = modelVersion;
        this.current = true;
        this.assessedAt = Instant.now();
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public double getRiskScore() { return riskScore; }
    public RiskCategory getRiskCategory() { return riskCategory; }
    public String getFeatureVector() { return featureVector; }
    public String getShapValues() { return shapValues; }
    public String getExplanation() { return explanation; }
    public String getModelVersion() { return modelVersion; }
    public boolean isCurrent() { return current; }
    public void setCurrent(boolean current) { this.current = current; }
}
