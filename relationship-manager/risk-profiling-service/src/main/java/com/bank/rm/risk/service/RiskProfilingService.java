package com.bank.rm.risk.service;

import com.bank.rm.common.dto.RiskCategory;
import com.bank.rm.common.event.KafkaTopics;
import com.bank.rm.common.event.RiskAssessedEvent;
import com.bank.rm.risk.domain.RiskAssessment;
import com.bank.rm.risk.dto.RiskDtos.*;
import com.bank.rm.risk.repository.RiskAssessmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class RiskProfilingService {
    private static final Logger log = LoggerFactory.getLogger(RiskProfilingService.class);
    private final RiskAssessmentRepository repository;
    private final RiskScoringEngine scoringEngine;
    private final ShapExplanationService shapService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public RiskProfilingService(RiskAssessmentRepository repository, RiskScoringEngine scoringEngine,
                                 ShapExplanationService shapService,
                                 KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.repository = repository;
        this.scoringEngine = scoringEngine;
        this.shapService = shapService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public RiskAssessmentResponse assessRisk(UUID customerId, RiskAssessmentRequest request) {
        CustomerRiskInput input = new CustomerRiskInput(customerId, request.ageMidpoint(),
                request.annualIncome(), request.savings(), request.investments(),
                request.investmentDiversity(), request.retirementHorizon(), request.equityExposure(),
                request.monthlyExpenses(), request.dependents(), request.riskTolerance(),
                request.employmentStability(), request.insuranceCoverage(),
                request.emergencyFundMonths(), request.debtToIncome(), request.financialLiteracy());

        RiskScoringResult result = scoringEngine.computeRiskScore(input);
        String explanation = shapService.generateExplanation(
                result.shapValues(), result.riskScore(), result.riskCategory());

        RiskCategory previousCategory = repository.findByCustomerIdAndCurrentTrue(customerId)
                .map(existing -> { existing.setCurrent(false); repository.save(existing); return existing.getRiskCategory(); })
                .orElse(null);

        UUID assessmentId = UUID.randomUUID();
        RiskAssessment assessment = new RiskAssessment(assessmentId, customerId,
                result.riskScore(), result.riskCategory(),
                toJson(result.featureVector()), toJson(result.shapValues()),
                explanation, result.modelVersion());
        repository.save(assessment);

        publishEvent(new RiskAssessedEvent(customerId, result.riskScore(), result.riskCategory(), previousCategory));

        return new RiskAssessmentResponse(assessmentId, customerId, result.riskScore(),
                result.riskCategory(), explanation, result.shapValues(),
                scoringEngine.getPortfolioAllocation(result.riskCategory()), result.modelVersion());
    }

    private String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); } catch (Exception e) { return "{}"; }
    }

    private void publishEvent(RiskAssessedEvent event) {
        try {
            kafkaTemplate.send(KafkaTopics.RISK_EVENTS, objectMapper.writeValueAsString(event));
        } catch (Exception e) { log.error("Failed to publish risk event: {}", e.getMessage()); }
    }
}
