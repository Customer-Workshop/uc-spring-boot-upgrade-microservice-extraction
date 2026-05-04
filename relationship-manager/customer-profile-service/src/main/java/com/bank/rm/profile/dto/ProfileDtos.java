package com.bank.rm.profile.dto;

import com.bank.rm.common.dto.*;
import java.math.BigDecimal;
import java.util.UUID;

public class ProfileDtos {

    public record CreateProfileRequest(UUID userId, String name, String phone, String email, String location) {}

    public record UpdateFinancialRequest(
        AgeGroup ageGroup,
        IncomeSource incomeSource,
        IncomeRange incomeRange,
        BigDecimal currentSavings,
        BigDecimal currentInvestments,
        BigDecimal monthlyExpenses,
        Integer dependents
    ) {}

    public record UpdateGoalsRequest(
        BigDecimal retirementTarget,
        Integer retirementAge
    ) {}

    public record UpdateChannelRequest(CommunicationChannel preferredChannel) {}

    public record ProfileResponse(
        UUID id,
        UUID userId,
        String name,
        String phone,
        String email,
        String location,
        AgeGroup ageGroup,
        IncomeSource incomeSource,
        IncomeRange incomeRange,
        BigDecimal currentSavings,
        BigDecimal currentInvestments,
        BigDecimal monthlyExpenses,
        BigDecimal retirementTarget,
        Integer retirementAge,
        Integer dependents,
        RiskCategory riskCategory,
        Double riskScore,
        CommunicationChannel preferredChannel
    ) {}

    private ProfileDtos() {}
}
