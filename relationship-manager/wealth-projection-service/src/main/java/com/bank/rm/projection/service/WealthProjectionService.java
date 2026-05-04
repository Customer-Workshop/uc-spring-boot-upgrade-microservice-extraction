package com.bank.rm.projection.service;

import com.bank.rm.projection.domain.WealthProjection;
import com.bank.rm.projection.dto.ProjectionDtos.*;
import com.bank.rm.projection.repository.WealthProjectionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WealthProjectionService {
    private final WealthProjectionRepository repository;
    private final MonteCarloEngine monteCarloEngine;
    private final ObjectMapper objectMapper;

    public WealthProjectionService(WealthProjectionRepository repository,
                                    MonteCarloEngine monteCarloEngine, ObjectMapper objectMapper) {
        this.repository = repository;
        this.monteCarloEngine = monteCarloEngine;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProjectionResponse project(UUID customerId, ProjectionRequest request) {
        ProjectionInput input = new ProjectionInput(request.initialInvestment(),
                request.annualContribution(), request.years(),
                request.targetAmount(), request.riskCategory());
        ProjectionResult result = monteCarloEngine.runProjection(input);

        UUID projectionId = UUID.randomUUID();
        WealthProjection entity = new WealthProjection(projectionId, customerId, request.riskCategory(),
                BigDecimal.valueOf(request.initialInvestment()), BigDecimal.valueOf(request.annualContribution()),
                request.years(), BigDecimal.valueOf(request.targetAmount()),
                BigDecimal.valueOf(result.nominal().p50()), BigDecimal.valueOf(result.real().p50()),
                result.probabilityOfTarget(), result.scenariosRun(), toJson(result));
        repository.save(entity);

        return new ProjectionResponse(projectionId, customerId, result);
    }

    private String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); } catch (Exception e) { return "{}"; }
    }
}
