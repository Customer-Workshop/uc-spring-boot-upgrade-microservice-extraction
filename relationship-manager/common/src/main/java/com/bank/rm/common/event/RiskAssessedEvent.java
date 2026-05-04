package com.bank.rm.common.event;

import com.bank.rm.common.dto.RiskCategory;
import java.util.UUID;

public class RiskAssessedEvent extends DomainEvent {
    private final UUID customerId;
    private final double riskScore;
    private final RiskCategory riskCategory;
    private final RiskCategory previousCategory;

    public RiskAssessedEvent(UUID customerId, double riskScore,
                              RiskCategory riskCategory, RiskCategory previousCategory) {
        super("risk-profiling-service");
        this.customerId = customerId;
        this.riskScore = riskScore;
        this.riskCategory = riskCategory;
        this.previousCategory = previousCategory;
    }

    public UUID getCustomerId() { return customerId; }
    public double getRiskScore() { return riskScore; }
    public RiskCategory getRiskCategory() { return riskCategory; }
    public RiskCategory getPreviousCategory() { return previousCategory; }
}
