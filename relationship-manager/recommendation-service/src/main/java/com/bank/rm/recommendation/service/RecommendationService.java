package com.bank.rm.recommendation.service;

import com.bank.rm.common.dto.RiskCategory;
import com.bank.rm.common.event.KafkaTopics;
import com.bank.rm.common.event.RecommendationGeneratedEvent;
import com.bank.rm.recommendation.dto.RecommendationDtos.*;
import dev.langchain4j.model.chat.ChatLanguageModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RecommendationService {
    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);
    private final ChatLanguageModel chatModel;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final List<SeedProduct> SEED_PRODUCTS = List.of(
        new SeedProduct("High-Yield Savings Account", "SAVINGS_ACCOUNT", 85, 15, Set.of(RiskCategory.CONSERVATIVE, RiskCategory.MODERATE)),
        new SeedProduct("Fixed Deposit - 5 Year", "FIXED_DEPOSIT", 90, 20, Set.of(RiskCategory.CONSERVATIVE)),
        new SeedProduct("Large Cap Equity Fund", "MUTUAL_FUND", 75, 25, Set.of(RiskCategory.MODERATE, RiskCategory.AGGRESSIVE)),
        new SeedProduct("ELSS Tax Saver Fund", "TAX_SAVER", 80, 10, Set.of(RiskCategory.MODERATE, RiskCategory.AGGRESSIVE)),
        new SeedProduct("National Pension System", "PENSION", 85, 10, Set.of(RiskCategory.CONSERVATIVE, RiskCategory.MODERATE)),
        new SeedProduct("Sovereign Gold Bond", "GOLD", 70, 10, Set.of(RiskCategory.CONSERVATIVE, RiskCategory.MODERATE)),
        new SeedProduct("PPF Account", "PPF", 90, 10, Set.of(RiskCategory.CONSERVATIVE)),
        new SeedProduct("Small Cap Equity Fund", "MUTUAL_FUND", 65, 20, Set.of(RiskCategory.AGGRESSIVE, RiskCategory.VERY_AGGRESSIVE)),
        new SeedProduct("Corporate Bond Fund", "BOND", 75, 15, Set.of(RiskCategory.MODERATE)),
        new SeedProduct("Multi Cap Growth Fund", "MUTUAL_FUND", 70, 30, Set.of(RiskCategory.AGGRESSIVE, RiskCategory.VERY_AGGRESSIVE)),
        new SeedProduct("Term Insurance Plan", "INSURANCE", 95, 5, Set.of(RiskCategory.CONSERVATIVE, RiskCategory.MODERATE, RiskCategory.AGGRESSIVE, RiskCategory.VERY_AGGRESSIVE))
    );

    public RecommendationService(ChatLanguageModel chatModel, KafkaTemplate<String, String> kafkaTemplate,
                                  ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public RecommendationResponse generateRecommendations(UUID customerId, RecommendationRequest request) {
        List<ProductRecommendation> ruleBasedResults = new ArrayList<>();
        int rank = 1;
        for (SeedProduct sp : SEED_PRODUCTS) {
            if (sp.suitableFor().contains(request.riskCategory())) {
                ruleBasedResults.add(new ProductRecommendation(sp.name(), sp.category(),
                        sp.matchScore(), sp.allocationPercent(), null, rank++));
            }
        }

        List<ProductRecommendation> finalResults = aiRerank(ruleBasedResults, request);

        UUID recommendationId = UUID.randomUUID();
        publishEvent(new RecommendationGeneratedEvent(customerId, recommendationId, request.riskCategory()));

        return new RecommendationResponse(recommendationId, customerId, request.riskCategory(), finalResults);
    }

    private List<ProductRecommendation> aiRerank(List<ProductRecommendation> products, RecommendationRequest request) {
        try {
            StringBuilder productList = new StringBuilder();
            for (int i = 0; i < products.size(); i++) {
                productList.append(String.format("%d. %s (%s)\n", i, products.get(i).productName(), products.get(i).category()));
            }
            String prompt = String.format(
                "Given a %s risk profile customer, re-rank these products by suitability and " +
                "provide a one-line rationale for each:\n%s\n" +
                "Return as numbered list with format: rank. product_name | rationale",
                request.riskCategory(), productList);
            String response = chatModel.generate(prompt);

            List<ProductRecommendation> reranked = new ArrayList<>();
            int newRank = 1;
            for (ProductRecommendation p : products) {
                String rationale = extractRationale(response, p.productName());
                reranked.add(new ProductRecommendation(p.productName(), p.category(),
                        p.matchScore(), p.allocationPercent(),
                        rationale != null ? rationale : "Suitable for your risk profile", newRank++));
            }
            return reranked;
        } catch (Exception e) {
            log.warn("AI re-ranking failed, using rule-based order: {}", e.getMessage());
            return products;
        }
    }

    private String extractRationale(String response, String productName) {
        for (String line : response.split("\n")) {
            if (line.toLowerCase().contains(productName.toLowerCase().substring(0, Math.min(15, productName.length()))) && line.contains("|")) {
                return line.substring(line.indexOf("|") + 1).trim();
            }
        }
        return null;
    }

    private void publishEvent(RecommendationGeneratedEvent event) {
        try {
            kafkaTemplate.send(KafkaTopics.RECOMMENDATION_EVENTS, objectMapper.writeValueAsString(event));
        } catch (Exception e) { log.error("Failed to publish recommendation event: {}", e.getMessage()); }
    }

    private record SeedProduct(String name, String category, int matchScore, int allocationPercent, Set<RiskCategory> suitableFor) {}
}
