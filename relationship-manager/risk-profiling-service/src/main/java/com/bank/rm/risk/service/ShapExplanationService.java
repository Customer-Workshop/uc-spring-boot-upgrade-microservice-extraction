package com.bank.rm.risk.service;

import com.bank.rm.common.dto.RiskCategory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;

public class ShapExplanationService {
    private static final Logger log = LoggerFactory.getLogger(ShapExplanationService.class);
    private final ChatLanguageModel chatModel;

    public ShapExplanationService(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }

    public String generateExplanation(Map<String, Double> shapValues, double riskScore, RiskCategory category) {
        try {
            List<Map.Entry<String, Double>> topFactors = shapValues.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(5).collect(Collectors.toList());

            StringBuilder factorDesc = new StringBuilder();
            for (Map.Entry<String, Double> factor : topFactors) {
                factorDesc.append(String.format("- %s: %.2f impact\n",
                        formatFeatureName(factor.getKey()), factor.getValue()));
            }

            String prompt = String.format(
                "Generate a 2-3 sentence explanation of a customer's risk profile assessment.\n" +
                "Risk score: %.1f/10 (%s)\nTop contributing factors:\n%s\n" +
                "Explain in plain English why they received this risk category. Be concise and professional.",
                riskScore, category, factorDesc);

            return chatModel.generate(prompt);
        } catch (Exception e) {
            log.warn("LLM explanation failed, using template: {}", e.getMessage());
            return templateFallback(shapValues, riskScore, category);
        }
    }

    private String templateFallback(Map<String, Double> shapValues, double riskScore, RiskCategory category) {
        List<String> topFactors = shapValues.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .map(e -> formatFeatureName(e.getKey()))
                .collect(Collectors.toList());

        return String.format("Your risk profile is %s (score: %.1f/10). " +
                "The main factors are: %s. " +
                "We recommend a portfolio allocation suited to your risk tolerance.",
                category.name().toLowerCase().replace("_", " "),
                riskScore, String.join(", ", topFactors));
    }

    private String formatFeatureName(String feature) {
        return feature.replace("_", " ");
    }
}
