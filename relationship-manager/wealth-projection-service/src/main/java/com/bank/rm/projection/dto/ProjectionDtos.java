package com.bank.rm.projection.dto;

import com.bank.rm.common.dto.RiskCategory;
import java.util.UUID;

public class ProjectionDtos {

    public record ProjectionInput(
        double initialInvestment, double annualContribution, int years,
        double targetAmount, RiskCategory riskCategory
    ) {}

    public record ProjectionRequest(
        double initialInvestment, double annualContribution, int years,
        double targetAmount, RiskCategory riskCategory
    ) {}

    public record ProjectionResult(
        PercentileValues nominal, PercentileValues real,
        int scenariosRun, double probabilityOfTarget
    ) {}

    public record PercentileValues(double p10, double p25, double p50, double p75, double p90) {}

    public record ProjectionResponse(
        UUID projectionId, UUID customerId, ProjectionResult result
    ) {}

    private ProjectionDtos() {}
}
