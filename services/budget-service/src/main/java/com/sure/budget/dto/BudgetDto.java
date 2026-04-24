package com.sure.budget.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BudgetDto(
        UUID id,
        UUID familyId,
        UUID categoryId,
        String categoryName,
        String name,
        BigDecimal amount,
        String currency,
        String periodType,
        LocalDate startDate,
        LocalDate endDate) {}
