package com.bank.rm.risk.service;

import com.bank.rm.common.dto.RiskCategory;
import com.bank.rm.risk.dto.RiskDtos.CustomerRiskInput;
import com.bank.rm.risk.dto.RiskDtos.RiskScoringResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.LinkedHashMap;
import java.util.Map;

public class RiskScoringEngine {
    private static final Logger log = LoggerFactory.getLogger(RiskScoringEngine.class);

    private static final Map<String, Double> FEATURE_WEIGHTS = new LinkedHashMap<>(Map.ofEntries(
        Map.entry("age_midpoint", 0.08), Map.entry("income_level", 0.10),
        Map.entry("savings_ratio", 0.09), Map.entry("investment_diversity", 0.07),
        Map.entry("retirement_horizon", 0.08), Map.entry("equity_exposure", 0.10),
        Map.entry("debt_exposure", 0.06), Map.entry("monthly_surplus", 0.07),
        Map.entry("dependents", 0.05), Map.entry("risk_tolerance", 0.12),
        Map.entry("employment_stability", 0.05), Map.entry("insurance_coverage", 0.04),
        Map.entry("emergency_fund", 0.04), Map.entry("debt_to_income", 0.03),
        Map.entry("financial_literacy", 0.02)
    ));

    public RiskScoringResult computeRiskScore(CustomerRiskInput input) {
        Map<String, Double> featureVector = buildFeatureVector(input);
        double weightedSum = 0.0;
        Map<String, Double> shapValues = new LinkedHashMap<>();

        for (Map.Entry<String, Double> entry : FEATURE_WEIGHTS.entrySet()) {
            String feature = entry.getKey();
            double weight = entry.getValue();
            double featureValue = featureVector.getOrDefault(feature, 5.0);
            double contribution = featureValue * weight;
            weightedSum += contribution;
            shapValues.put(feature, Math.round(contribution * 100.0) / 100.0);
        }

        double score = Math.max(1.0, Math.min(10.0, weightedSum));
        RiskCategory category = RiskCategory.fromScore(score);

        return new RiskScoringResult(
            Math.round(score * 10.0) / 10.0,
            category,
            featureVector,
            shapValues,
            "weighted-scoring-v1.0"
        );
    }

    public Map<String, double[]> getPortfolioAllocation(RiskCategory category) {
        return switch (category) {
            case CONSERVATIVE -> Map.of("equity", new double[]{20}, "debt", new double[]{50},
                    "gold", new double[]{15}, "cash", new double[]{10}, "alternative", new double[]{5});
            case MODERATE -> Map.of("equity", new double[]{40}, "debt", new double[]{30},
                    "gold", new double[]{15}, "cash", new double[]{5}, "alternative", new double[]{10});
            case AGGRESSIVE -> Map.of("equity", new double[]{60}, "debt", new double[]{15},
                    "gold", new double[]{10}, "cash", new double[]{5}, "alternative", new double[]{10});
            case VERY_AGGRESSIVE -> Map.of("equity", new double[]{75}, "debt", new double[]{5},
                    "gold", new double[]{5}, "cash", new double[]{0}, "alternative", new double[]{15});
        };
    }

    private Map<String, Double> buildFeatureVector(CustomerRiskInput input) {
        Map<String, Double> features = new LinkedHashMap<>();
        features.put("age_midpoint", normalizeAge(input.ageMidpoint()));
        features.put("income_level", normalizeIncome(input.annualIncome()));
        double totalAssets = input.savings() + input.investments();
        features.put("savings_ratio", totalAssets > 0 ? Math.min(10.0, (input.savings() / totalAssets) * 10) : 5.0);
        features.put("investment_diversity", input.investmentDiversity());
        features.put("retirement_horizon", normalizeHorizon(input.retirementHorizon()));
        features.put("equity_exposure", input.equityExposure());
        features.put("debt_exposure", 10.0 - input.equityExposure());
        double annualExpenses = input.monthlyExpenses() * 12;
        features.put("monthly_surplus", input.annualIncome() > 0 ?
                Math.min(10.0, ((input.annualIncome() - annualExpenses) / input.annualIncome()) * 10) : 5.0);
        features.put("dependents", Math.max(1.0, 10.0 - input.dependents() * 2.0));
        features.put("risk_tolerance", input.riskTolerance());
        features.put("employment_stability", input.employmentStability());
        features.put("insurance_coverage", input.insuranceCoverage());
        features.put("emergency_fund", input.emergencyFundMonths() >= 6 ? 8.0 : input.emergencyFundMonths());
        features.put("debt_to_income", input.annualIncome() > 0 ?
                Math.max(1.0, 10.0 - (input.debtToIncome() * 10)) : 5.0);
        features.put("financial_literacy", input.financialLiteracy());
        return features;
    }

    private double normalizeAge(int ageMidpoint) {
        if (ageMidpoint <= 25) return 8.0;
        if (ageMidpoint <= 35) return 7.0;
        if (ageMidpoint <= 45) return 5.0;
        return 3.0;
    }

    private double normalizeIncome(double income) {
        if (income >= 150000) return 8.0;
        if (income >= 100000) return 7.0;
        if (income >= 80000) return 6.0;
        if (income >= 60000) return 5.0;
        return 4.0;
    }

    private double normalizeHorizon(int years) {
        if (years >= 25) return 9.0;
        if (years >= 15) return 7.0;
        if (years >= 10) return 5.0;
        return 3.0;
    }
}
