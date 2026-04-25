package com.sure.budget.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BudgetDto(
        String id,
        String familyId,
        String categoryId,
        String categoryName,
        String name,
        BigDecimal amount,
        String currency,
        String periodType,
        LocalDate startDate,
        LocalDate endDate) {}
