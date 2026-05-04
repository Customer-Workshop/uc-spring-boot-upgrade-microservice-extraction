package com.bank.rm.common.event;

import com.bank.rm.common.dto.RiskCategory;
import java.util.UUID;

public class RecommendationGeneratedEvent extends DomainEvent {
    private final UUID customerId;
    private final UUID recommendationId;
    private final RiskCategory riskCategory;

    public RecommendationGeneratedEvent(UUID customerId, UUID recommendationId, RiskCategory riskCategory) {
        super("recommendation-service");
        this.customerId = customerId;
        this.recommendationId = recommendationId;
        this.riskCategory = riskCategory;
    }

    public UUID getCustomerId() { return customerId; }
    public UUID getRecommendationId() { return recommendationId; }
    public RiskCategory getRiskCategory() { return riskCategory; }
}
