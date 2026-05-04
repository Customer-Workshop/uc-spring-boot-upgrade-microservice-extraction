package com.bank.rm.profile.service;

import com.bank.rm.common.event.KafkaTopics;
import com.bank.rm.common.event.ProfileUpdatedEvent;
import com.bank.rm.common.exception.ResourceNotFoundException;
import com.bank.rm.profile.domain.CustomerProfile;
import com.bank.rm.profile.dto.ProfileDtos.*;
import com.bank.rm.profile.repository.CustomerProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ProfileService {
    private final CustomerProfileRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ProfileService(CustomerProfileRepository repository,
                          KafkaTemplate<String, String> kafkaTemplate,
                          ObjectMapper objectMapper) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProfileResponse createProfile(CreateProfileRequest request) {
        CustomerProfile profile = new CustomerProfile(UUID.randomUUID(), request.userId());
        profile.setName(request.name());
        profile.setPhone(request.phone());
        profile.setEmail(request.email());
        profile.setLocation(request.location());
        repository.save(profile);
        return toResponse(profile);
    }

    public ProfileResponse getProfile(UUID customerId) {
        return toResponse(findProfile(customerId));
    }

    @Transactional
    public ProfileResponse updateFinancial(UUID customerId, UpdateFinancialRequest request) {
        CustomerProfile profile = findProfile(customerId);
        profile.setAgeGroup(request.ageGroup());
        profile.setIncomeSource(request.incomeSource());
        profile.setIncomeRange(request.incomeRange());
        profile.setCurrentSavings(request.currentSavings());
        profile.setCurrentInvestments(request.currentInvestments());
        profile.setMonthlyExpenses(request.monthlyExpenses());
        profile.setDependents(request.dependents());
        profile.setUpdatedAt(Instant.now());
        repository.save(profile);
        publishEvent(new ProfileUpdatedEvent(customerId,
                List.of("ageGroup", "incomeSource", "incomeRange", "savings", "investments")));
        return toResponse(profile);
    }

    @Transactional
    public ProfileResponse updateGoals(UUID customerId, UpdateGoalsRequest request) {
        CustomerProfile profile = findProfile(customerId);
        profile.setRetirementTarget(request.retirementTarget());
        profile.setRetirementAge(request.retirementAge());
        profile.setUpdatedAt(Instant.now());
        repository.save(profile);
        publishEvent(new ProfileUpdatedEvent(customerId, List.of("retirementTarget", "retirementAge")));
        return toResponse(profile);
    }

    @Transactional
    public ProfileResponse updateChannel(UUID customerId, UpdateChannelRequest request) {
        CustomerProfile profile = findProfile(customerId);
        profile.setPreferredChannel(request.preferredChannel());
        profile.setUpdatedAt(Instant.now());
        repository.save(profile);
        return toResponse(profile);
    }

    private CustomerProfile findProfile(UUID customerId) {
        return repository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found: " + customerId));
    }

    private void publishEvent(ProfileUpdatedEvent event) {
        try {
            kafkaTemplate.send(KafkaTopics.PROFILE_EVENTS, objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            // log and continue
        }
    }

    private ProfileResponse toResponse(CustomerProfile p) {
        return new ProfileResponse(p.getId(), p.getUserId(), p.getName(), p.getPhone(), p.getEmail(),
                p.getLocation(), p.getAgeGroup(), p.getIncomeSource(), p.getIncomeRange(),
                p.getCurrentSavings(), p.getCurrentInvestments(), p.getMonthlyExpenses(),
                p.getRetirementTarget(), p.getRetirementAge(), p.getDependents(),
                p.getRiskCategory(), p.getRiskScore(), p.getPreferredChannel());
    }
}
