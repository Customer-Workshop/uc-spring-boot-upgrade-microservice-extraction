package com.bank.rm.recommendation.dto;

import com.bank.rm.common.dto.RiskCategory;
import java.util.List;
import java.util.UUID;

public class RecommendationDtos {

    public record RecommendationRequest(RiskCategory riskCategory, double riskScore) {}

    public record ProductRecommendation(String productName, String category, double matchScore,
                                         double allocationPercent, String rationale, int rank) {}

    public record RecommendationResponse(UUID recommendationId, UUID customerId,
                                          RiskCategory riskCategory, List<ProductRecommendation> products) {}

    private RecommendationDtos() {}
}
