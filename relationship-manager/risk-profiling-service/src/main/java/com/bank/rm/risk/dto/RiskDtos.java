package com.bank.rm.risk.dto;

import com.bank.rm.common.dto.RiskCategory;
import java.util.Map;
import java.util.UUID;

public class RiskDtos {

    public record CustomerRiskInput(
        UUID customerId, int ageMidpoint, double annualIncome, double savings,
        double investments, double investmentDiversity, int retirementHorizon,
        double equityExposure, double monthlyExpenses, int dependents,
        double riskTolerance, double employmentStability, double insuranceCoverage,
        double emergencyFundMonths, double debtToIncome, double financialLiteracy
    ) {}

    public record RiskScoringResult(
        double riskScore, RiskCategory riskCategory, Map<String, Double> featureVector,
        Map<String, Double> shapValues, String modelVersion
    ) {}

    public record RiskAssessmentRequest(
        int ageMidpoint, double annualIncome, double savings, double investments,
        double investmentDiversity, int retirementHorizon, double equityExposure,
        double monthlyExpenses, int dependents, double riskTolerance,
        double employmentStability, double insuranceCoverage,
        double emergencyFundMonths, double debtToIncome, double financialLiteracy
    ) {}

    public record RiskAssessmentResponse(
        UUID assessmentId, UUID customerId, double riskScore, RiskCategory riskCategory,
        String explanation, Map<String, Double> shapValues,
        Map<String, double[]> portfolioAllocation, String modelVersion
    ) {}

    private RiskDtos() {}
}
